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
 * questions or need additional information. Email: euptron@gmail.com
 */

package com.eup.codeopsstudio.common.util;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.eup.codeopsstudio.common.ILog;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 * Class to handle archive files
 *
 * @author EUP
 */
public class Archive {

    public static final String TAG = "Archive";
    private OnUnzippedListener listener;

    public Archive() {
        // Default
    }

    public static void unzipFromAssets(@NonNull Context context, int bufferSize,
        @NonNull String zipFile, @NonNull String destination) throws IOException {
        unzipFromAssets(context, bufferSize, zipFile, new File(destination));
    }

    /**
     * Unzips a zip file in the app assets folder to into a specified destination folder
     *
     * @param context     the context to work in
     * @param bufferSize  the throughput size
     * @param zipFilePath the path of the ZIP file to unzip.
     * @param destination the existing or non-existing destination folder to unzip the files.
     * @throws IOException          if an I/O error occurs.
     * @throws NullPointerException if required parameters are null
     */
    public static void unzipFromAssets(@NonNull Context context, int bufferSize,
        @NonNull String zipFilePath, @NonNull File destination) throws IOException {
        Objects.requireNonNull(context);
        Objects.requireNonNull(zipFilePath);
        Objects.requireNonNull(destination);

        try (InputStream is = context
            .getAssets()
            .open(zipFilePath)) {
            unzip(bufferSize, is, destination);
        }
    }

    public static void unzip(int bufferSize, InputStream inputStream,
        File destination) throws IOException {
        createDirectory(destination);
        byte[] buffer = new byte[bufferSize];

        try (ZipInputStream zipInputStream = new ZipInputStream(inputStream)) {
            ZipEntry zipEntry = zipInputStream.getNextEntry();

            while (zipEntry != null) {
                String entryName = zipEntry.getName();
                Log.v(TAG, "Unzipping " + entryName);

                var cue = new File(destination, entryName);
                if (zipEntry.isDirectory()) {
                    createDirectory(cue);
                } else {
                    if (!createDirectory(cue.getParentFile())) {
                        zipInputStream.closeEntry();
                        zipEntry = zipInputStream.getNextEntry();
                        continue;
                    }

                    boolean created = cue.createNewFile();

                    if (created) {
                        try (
                            BufferedOutputStream outputStream =
                                new BufferedOutputStream(new FileOutputStream(cue))) {
                            int bytesRead;
                            while ((bytesRead = zipInputStream.read(buffer)) != -1) {
                                outputStream.write(buffer, 0, bytesRead);
                            }
                        }
                    } else {
                        ILog.warning(TAG, "Skipping existing file: " + cue.getName());
                    }
                }
                zipInputStream.closeEntry();
                zipEntry = zipInputStream.getNextEntry();
            }
        }
    }

    private static boolean createDirectory(@Nullable File dir) {
        if (dir == null) return false;

        String directoryPath = dir.getAbsolutePath();

        if (dir.mkdirs()) {
            return true;
        }

        if (dir.exists()) {
            ILog.warning(TAG,
                "Cannot create directory, a file already exists with that name: " + directoryPath);
        } else {
            ILog.warning(TAG, "Failed to create directory " + directoryPath);
        }
        return false;
    }

    public void setListener(OnUnzippedListener listener) {
        this.listener = listener;
    }

    /**
     * Unzips a file to the specified destination folder without creating an intermediate folder.
     *
     * @param zipFile The path of the ZIP file to unzip.
     * @param destDir The destination folder to unzip the files.
     * @throws IOException if an I/O error occurs.
     */
    public void unzipIntoDestination(int bufferSize, final File zipFile,
        final File destDir) throws IOException {
        if (zipFile == null || destDir == null) return;

        try (ZipFile zip = new ZipFile(zipFile, ZipFile.OPEN_READ)) {
            int totalFiles = zip.size();
            int fileIndex = 0;
            Enumeration<?> entries = zip.entries();

            while (entries.hasMoreElements()) {
                ZipEntry entry = ((ZipEntry) entries.nextElement());
                String entryName = entry
                    .getName()
                    .replace("\\", "/");
                if (entryName.contains("../")) {
                    if (listener != null) {
                        listener.onLog("Archive: " + "entryName: " + entryName + " is dangerous!");
                    }
                    continue;
                }
                if (!unzipChildFile(bufferSize, destDir, zip, entry, entryName, fileIndex,
                    totalFiles)) {
                    return;
                }
            }
        }
    }

    private boolean unzipChildFile(int byteSize, final File destDir, final ZipFile zip,
        final ZipEntry entry, final String name, int index, int totalFiles) throws IOException {
        File file = new File(destDir, name);
        index++;

        if (listener != null) {
            listener.onFileUnArchiving(index, totalFiles, entry.getName());
        }

        if (entry.isDirectory()) {
            return FileUtil.createOrExistsDir(file);
        } else {
            if (!FileUtil.createOrExistsFile(file)) return false;
            try (InputStream in = new BufferedInputStream(zip.getInputStream(entry));
                OutputStream out = new BufferedOutputStream(new FileOutputStream(file))) {
                byte[] buffer = new byte[byteSize];
                int len;
                while ((len = in.read(buffer)) != -1) {
                    out.write(buffer, 0, len);
                }
            }
        }
        return true;
    }

    public interface OnUnzippedListener {
        void onFileUnArchiving(int unzippedFileCount, int totalFileCount, String currentFileName);

        void onLog(String message);
    }
}
