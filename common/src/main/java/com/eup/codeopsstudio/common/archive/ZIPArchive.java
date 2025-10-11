/*
 * This file is part of CodeOps Studio.
 * CodeOps Studio - Code anywhere anytime
 * https://github.com/euptron/CodeOps-Studio
 * Copyright (C) 2024-2025 Etido Peter
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/
 *
 * If you have more questions, feel free to message Etido Peter if you have any
 * questions or need additional information. Email: etido.up@gmail.com
 */

package com.eup.codeopsstudio.common.archive;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.eup.codeopsstudio.common.AsyncTask;
import com.eup.codeopsstudio.common.ILog;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 * @author Etido Peter
 */
public class ZIPArchive implements Archive {

    public static final String TAG = "ZIPArchive";
    private static final long MIN_UPDATE_INTERVAL_IN_MS = 100;
    private static final int MAX_MEMORY_STREAM_SIZE = 10 * 1024 * 1024; // 10MB
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final File destDir;
    private final File archiveFile;
    private final int userBufferSize;
    private final InputStream archiveStream;
    private final Object pauseLock = new Object();
    private final OnArchiveListener archiveListener;
    private volatile boolean paused = false;
    private volatile boolean canceled = false;
    private int totalItems = 0;
    private int processedItems = 0;
    private int firstPassIndexedEntries;
    private long lastUpdateTime;
    private long totalFileBytes;
    private long bytesWritten = 0;
    private long lastPauseTime = 0;
    private long totalPausedTime = 0;
    private Future<?> unzipFuture;

    private InputStream firstPassStream;
    private InputStream secondPassStream;
    /**
     * To store large streams temporarily on disk
     */
    private File tempFile;

    private ZIPArchive(@Nullable File archiveFile, @Nullable InputStream archiveStream,
        @NonNull File destDir, int bufferSize, @NonNull OnArchiveListener listener) {
        Objects.requireNonNull(destDir);
        Objects.requireNonNull(listener);
        this.archiveFile     = archiveFile;
        this.archiveStream   = archiveStream;
        this.destDir         = destDir;
        this.userBufferSize  = bufferSize;
        this.archiveListener = listener;
    }

    @NonNull
    @Override
    public String getType() {
        return "Compressed::ZIPArchive";
    }

    public void cancelAndShutdown() {
        cancel();
        executor.shutdownNow();
    }

    public void cancel() {
        if (unzipFuture != null) {
            canceled = true;
            unzipFuture.cancel(true);
        }
    }

    public void cancelPreviousTask() {
        if (unzipFuture != null && !unzipFuture.isDone()) {
            canceled = true;
            unzipFuture.cancel(true);
        }
    }

    @NonNull
    public static ZIPArchive fromAssets(Context context, String zipAssetPath, File destDir,
        int bufferSize) throws IOException {
        return fromAssets(context, zipAssetPath, destDir, bufferSize, new NoOPListener());
    }

    @NonNull
    public static ZIPArchive fromAssets(Context context, String zipAssetPath, File destDir,
        int bufferSize, OnArchiveListener listener) throws IOException {
        Objects.requireNonNull(context);
        Objects.requireNonNull(zipAssetPath);
        InputStream is = context.getAssets().open(zipAssetPath);
        Objects.requireNonNull(is);
        return fromInputStream(is, destDir, bufferSize, listener);
    }

    public static ZIPArchive fromInputStream(InputStream inputStream, File destDir, int bufferSize,
        OnArchiveListener listener) {
        return new ZIPArchive(null, inputStream, destDir, bufferSize, listener);
    }

    public static ZIPArchive fromFile(String zipPath, String destDir, int bufferSize) {
        return fromFile(zipPath, destDir, bufferSize, new NoOPListener());
    }

    public static ZIPArchive fromFile(String zipPath, String destDir, int bufferSize,
        OnArchiveListener listener) {
        return fromFile(new File(zipPath), new File(destDir), bufferSize, listener);
    }

    public static ZIPArchive fromFile(File zipFile, File destDir, int bufferSize,
        OnArchiveListener listener) {
        return new ZIPArchive(zipFile, null, destDir, bufferSize, listener);
    }

    public static ZIPArchive fromFile(File zipFile, File destDir, int bufferSize) {
        return fromFile(zipFile, destDir, bufferSize, new NoOPListener());
    }

    public static ZIPArchive fromInputStream(InputStream inputStream, File destDir,
        int bufferSize) {
        return fromInputStream(inputStream, destDir, bufferSize, new NoOPListener());
    }

    public boolean isCanceled() {
        return canceled;
    }

    public boolean isPaused() {
        return paused;
    }

    public void pause() {
        paused        = true;
        lastPauseTime = millsNow();
    }

    private static long millsNow() {
        return System.currentTimeMillis();
    }

    public void resume() {
        paused = false;
        if (lastPauseTime > 0) {
            totalPausedTime += (millsNow() - lastPauseTime);
            lastPauseTime = 0;
        }
        synchronized (pauseLock) {
            pauseLock.notifyAll();
        }
    }

    public void unzip() {
        unzipFuture = executor.submit(this::unzipInternal);
    }

    private boolean checkCanceled() {
        if (Thread.interrupted() || canceled) {
            canceled = true;
            return true;
        }
        return false;
    }

    private boolean cleanupTempResources() {
        boolean cleaned = false;
        if (tempFile != null && tempFile.exists()) {
            cleaned = tempFile.delete();
        }
        try {
            if (firstPassStream != null) {
                firstPassStream.close();
            }
            if (secondPassStream != null) {
                secondPassStream.close();
            }
        } catch (IOException e) {
            ILog.warning(TAG, "Error closing duplicated streams");
        }
        return cleaned;
    }

    private void duplicateInputStream() throws IOException {
        if (archiveStream == null) return;

        // For small steams, memory based duplication is used
        if (archiveStream.available() > 0 && archiveStream.available() <= MAX_MEMORY_STREAM_SIZE) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[userBufferSize];
            int len;
            while ((len = archiveStream.read(buffer)) > -1) {
                baos.write(buffer, 0, len);
            }
            baos.flush();

            byte[] data = baos.toByteArray();
            firstPassStream  = new ByteArrayInputStream(data);
            secondPassStream = new ByteArrayInputStream(data);
        } else {
            // For large streams
            tempFile = File.createTempFile("zip_temp_cos", ".tmp");
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                byte[] buffer = new byte[userBufferSize];
                int len;
                while ((len = archiveStream.read(buffer)) > -1) {
                    fos.write(buffer, 0, len);
                }
            }

            firstPassStream  = new FileInputStream(tempFile);
            secondPassStream = new FileInputStream(tempFile);
        }
    }

    @NonNull
    private String formatElapsedTime(long millis) {
        if (millis < 1000) {
            return String.format(Locale.ENGLISH, "%d ms", millis);
        }

        long seconds = millis / 1000;
        if (seconds < 60) {
            return String.format(Locale.ENGLISH, "%d sec", seconds);
        }

        long minutes = seconds / 60;
        if (minutes < 60) {
            return String.format(Locale.ENGLISH, "%d min %d sec", minutes, (seconds % 60));
        }

        long hours = minutes / 60;
        return String.format(Locale.ENGLISH, "%d hours %d min %d sec", hours, minutes, (seconds
            % 60));
    }

    @NonNull
    private String formatSpeed(double bytesPerSecond) {
        if (bytesPerSecond < 1024) {
            return String.format(Locale.ENGLISH, "%.1f B/s", bytesPerSecond);
        } else if (bytesPerSecond < 1024 * 1024) {
            return String.format(Locale.ENGLISH, "%.1f KB/s", bytesPerSecond / 1024);
        } else {
            return String.format(Locale.ENGLISH, "%.1f MB/s", bytesPerSecond / (1024 * 1024));
        }
    }

    /**
     * A lot slower than #gatherZipInfo(File)
     *
     * @param inputStream the input stream to collect info of
     */
    private void gatherZipInfo(InputStream inputStream) throws Exception {
        long lastFirstPassUiUpdateTime = 0;
        totalItems = 0;

        Log.d(TAG, "Using InputStream to gather zip info");

        try (ZipInputStream zis = new ZipInputStream(new BufferedInputStream(inputStream))) {
            ZipEntry entry;
            firstPassIndexedEntries = 0;

            while ((entry = zis.getNextEntry()) != null) {
                firstPassIndexedEntries++;
                totalItems++;
                if (checkCanceled()) break;
                if (entry.isDirectory()) continue;
                long size = entry.getSize();
                if (size <= 0) continue;

                long now = System.currentTimeMillis();
                if (now - lastFirstPassUiUpdateTime > MIN_UPDATE_INTERVAL_IN_MS) {
                    AsyncTask.runOnUiThread(() -> archiveListener.onInitialize(
                        "Indexing: " + firstPassIndexedEntries + " items"));
                    lastFirstPassUiUpdateTime = now;
                }
            }
        }
    }

    /**
     * A lot faster than #gatherZipInfo(InputStream)
     *
     * @param file the file to collect info of
     */
    private void gatherZipInfo(File file) throws Exception {
        long lastFirstPassUiUpdateTime = 0;
        totalItems = 0;

        Log.d(TAG, "Using file to gather zip info");
        try (ZipFile zipFile = new ZipFile(file)) {
            totalItems = zipFile.size();
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            firstPassIndexedEntries = 0;

            while (entries.hasMoreElements()) {
                firstPassIndexedEntries++;
                ZipEntry entry = entries.nextElement();

                if (checkCanceled()) break;
                if (entry.isDirectory()) continue;

                long size = entry.getSize();
                if (size <= 0) continue;

                long elapsedTime = getElapsedTime(lastFirstPassUiUpdateTime);
                if (elapsedTime > MIN_UPDATE_INTERVAL_IN_MS) {
                    AsyncTask.runOnUiThread(() -> archiveListener.onInitialize(
                        "Indexing: " + firstPassIndexedEntries + "/" + totalItems));
                    lastFirstPassUiUpdateTime = millsNow();
                }
            }
        }
    }

    private static long getElapsedTime(long startMills) {
        return millsNow() - startMills;
    }

    private void unzipInternal() {
        if (archiveFile == null && archiveStream == null) {
            ILog.debug(TAG, "Cannot unzip! No input (file or input stream) was given");
            return;
        }

        if (archiveFile != null && archiveStream != null) {
            ILog.error(TAG, "Cannot unzip! ZIPArchive must take in only one input (file or "
                + "input stream) and not two");
            return;
        }

        long startTime = millsNow();
        InputStream inputStream = null;

        try {
            AsyncTask.runOnUiThread(() -> archiveListener.onInitialize("Preparing..."));

            if (archiveFile != null) {
                gatherZipInfo(archiveFile);
                inputStream = new FileInputStream(archiveFile);
            }

            if (archiveStream != null) {
                duplicateInputStream(); // For two-pass processing
                gatherZipInfo(firstPassStream);
                inputStream = secondPassStream;
            }

            if (totalItems <= 0) {
                AsyncTask.runOnUiThread(() -> archiveListener.onComplete(
                    "ZIPArchive is " + "empty"));
                return;
            }

            AsyncTask.runOnUiThread(() -> archiveListener.onStart(totalItems));

            long extractionStartTime = millsNow();

            try (ZipInputStream zis = new ZipInputStream(new BufferedInputStream(inputStream))) {
                byte[] buffer = new byte[userBufferSize];
                processedItems = 0;
                long totalBytesExtractedSoFar = 0;
                ZipEntry entry;

                while ((entry = zis.getNextEntry()) != null) {
                    if (checkCanceled()) {
                        zis.closeEntry();
                        break;
                    }

                    waitIfPaused();

                    final File newFile = new File(destDir, entry.getName());
                    final String entryName = newFile.getName();

                    String canonicalDestPath = destDir.getCanonicalPath();
                    String canonicalEntryPath = newFile.getCanonicalPath();
                    if (!canonicalEntryPath.startsWith(canonicalDestPath + File.separator)) {
                        throw new SecurityException(
                            "Zip entry outside target directory: " + entry.getName());
                    }

                    if (entry.isDirectory()) {
                        if (!newFile.exists() && !newFile.mkdirs()) {
                            throw new IOException(
                                "Failed to create directory: " + newFile.getAbsolutePath());
                        }
                    } else {
                        File parent = newFile.getParentFile();
                        if (!parent.exists() && !parent.mkdirs()) {
                            throw new IOException(
                                "Failed to create parent directory: " + parent.getAbsolutePath());
                        }

                        try (FileOutputStream fos = new FileOutputStream(newFile)) {
                            int len;
                            bytesWritten   = 0;
                            totalFileBytes = entry.getSize();
                            long lastByteUpdateTime = 0;

                            if (totalFileBytes > 0) {
                                AsyncTask.runOnUiThread(() -> archiveListener.onFileProgress(0,
                                    totalFileBytes, entryName));
                            }

                            while ((len = zis.read(buffer)) > 0) {
                                if (checkCanceled()) {
                                    zis.closeEntry();
                                    //noinspection ResultOfMethodCallIgnored
                                    newFile.delete();
                                    return;
                                }

                                waitIfPaused();

                                fos.write(buffer, 0, len);
                                bytesWritten += len;
                                totalBytesExtractedSoFar += len;

                                if (getElapsedTime(lastByteUpdateTime)
                                    > MIN_UPDATE_INTERVAL_IN_MS) {
                                    long availableBytes = totalFileBytes > 0 ? totalFileBytes : -1;
                                    AsyncTask.runOnUiThread(() -> archiveListener.onFileProgress(bytesWritten, availableBytes, entryName));

                                    long actualExtractionTime =
                                        getElapsedTime(extractionStartTime) - totalPausedTime;
                                    if (actualExtractionTime > 0) {
                                        double overallSpeed = (totalBytesExtractedSoFar * 1000.0)
                                            / actualExtractionTime;

                                        if (overallSpeed > 0 && !Double.isInfinite(overallSpeed)) {
                                            AsyncTask.runOnUiThread(() -> archiveListener.onSpeedUpdate(formatSpeed(overallSpeed)));
                                        }
                                    }
                                    lastByteUpdateTime = millsNow();
                                }
                            }

                            if (totalFileBytes > 0) {
                                AsyncTask.runOnUiThread(() -> archiveListener.onFileProgress(bytesWritten, totalFileBytes, entryName));
                            }
                        }
                    }
                    processedItems++;

                    String currentFile = entry.getName();
                    int itemsLeft = totalItems - processedItems;

                    if (getElapsedTime(lastUpdateTime) > MIN_UPDATE_INTERVAL_IN_MS
                        || processedItems == totalItems) {
                        AsyncTask.runOnUiThread(() -> archiveListener.onUpdateProgress(processedItems, totalItems, currentFile, itemsLeft));
                        lastUpdateTime = millsNow();
                    }
                }
            }

            if (!canceled) {
                String timeMessage = formatElapsedTime(getElapsedTime(startTime));
                AsyncTask.runOnUiThread(() -> archiveListener.onComplete(
                    "Unzipped successfully in " + timeMessage));
            }
        } catch (Exception e) {
            Log.e(TAG, "Unzip error", e);
            String timeMessage = formatElapsedTime(getElapsedTime(startTime));
            Exception error = new Exception(
                "Error after " + timeMessage + ": " + e.getMessage(), e);
            AsyncTask.runOnUiThread(() -> archiveListener.onError(error));
        } finally {
            ILog.debug(TAG, "Cleaned temporary resources: " + cleanupTempResources());
        }
    }

    private void waitIfPaused() throws InterruptedException {
        while (paused) {
            synchronized (pauseLock) {
                pauseLock.wait();
            }
        }
    }
}
