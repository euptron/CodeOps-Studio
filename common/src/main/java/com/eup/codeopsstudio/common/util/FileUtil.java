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
import android.os.Environment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.eup.codeopsstudio.common.ContextManager;
import com.eup.codeopsstudio.common.ILog;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Objects;

/**
 * @author Etido Peter
 */
public class FileUtil {

    public static final String TAG = "FileUtil";

    @Nullable
    public static String calculateMD5(int bufferSize, File updateFile) {
        InputStream is;
        try {
            is = new FileInputStream(updateFile);
        } catch (FileNotFoundException e) {
            ILog.error(TAG, "Exception while getting FileInputStream for calculateMD5", e);
            return null;
        }
        return calculateMD5(bufferSize, is);
    }

    @Nullable
    public static String calculateMD5(int bufferSize, @NonNull InputStream is) {
        byte[] buffer = new byte[bufferSize];

        try (is) {
            MessageDigest digest = MessageDigest.getInstance("MD5");

            int read;
            while ((read = is.read(buffer)) > 0) {
                digest.update(buffer, 0, read);
            }

            BigInteger bigInteger = new BigInteger(1, digest.digest());
            String md5 = bigInteger.toString(16);

            return String.format("%32s", md5).replace(' ', '0');
        } catch (IOException e) {
            ILog.error(TAG, "Unable to process file for MD5 calculation", e);
            return null;
        } catch (NoSuchAlgorithmException e) {
            ILog.error(TAG, "Exception while getting MD5 Digest", e);
            return null;
        } catch (NullPointerException e) {
            ILog.error(TAG, "NPE occurred while calculating MD5", e);
            return null;
        }
    }

    public static void clearAppCache(Context context) {
        try {
            File dir = context.getCacheDir();
            org.apache.commons.io.FileUtils.delete(new File(dir.getAbsolutePath()));
        } catch (Exception e) {
            ILog.debug(TAG, "Failed to clear cache", e);
        }
    }

    /**
     * Checks if a directory contains files or subdirectories (non-empty).
     *
     * @param dir The directory to check
     * @return {@code true} if the directory contains files or folders, otherwise {@code false}
     */
    public static boolean containsFiles(@NonNull File dir) {
        File[] files = dir.listFiles();
        return files != null && files.length > 0;
    }

    public static boolean createOrExistsFile(final File file) {
        if (file == null) return false;
        if (file.exists()) return file.isFile();
        if (!createOrExistsDir(file.getParentFile())) return false;
        try {
            return file.createNewFile();
        } catch (IOException e) {
            ILog.warning(TAG, "Failed to createOrExistsFile", e);
            return false;
        }
    }

    /**
     * Create a directory if it doesn't exist, otherwise do nothing.
     *
     * @param file The file.
     * @return {@code true}: exists or creates successfully<br>
     * {@code false}: otherwise
     */
    public static boolean createOrExistsDir(final File file) {
        return file != null && (file.exists() ? file.isDirectory() : file.mkdirs());
    }

    public static String findFreeFileName(String filename) {
        if (filename == null) {
            return null;
        }

        File f = new File(filename);
        if (!f.exists()) {
            return filename;
        }
        int dot = filename.lastIndexOf('.');

        String name;
        String ext;
        if (dot != -1) {
            name = filename.substring(0, dot);
            ext  = filename.substring(dot);
        } else {
            name = filename;
            ext  = "";
        }

        int num = 0;
        do {
            f = new File(name + (++num == 1 ? "" : " (" + num + ")") + ext);
        } while (f.exists());

        return name + (num == 1 ? "" : " (" + num + ")") + ext;
    }

    /**
     * Searches a directory for a file with the given relative path.
     *
     * @param currentDir   the root directory to search in
     * @param relativePath the path relative to the parent volume e.g /storage/emulated/0/
     * @return the file if it exists in any volume, may be {@code null}
     */
    public static File findInStorageDirectory(@Nullable File currentDir,
        @Nullable String relativePath) {
        if (currentDir == null || !currentDir.exists() || !currentDir.isDirectory()
            || relativePath == null) {
            return null;
        }

        File targetFile = new File(currentDir, relativePath);
        ILog.debug(TAG, "Searching " + targetFile.getPath());

        if (targetFile.exists()) {
            ILog.debug(TAG, "File found in " + targetFile.getPath());
            return targetFile;
        }

        File[] files = currentDir.listFiles();

        if (files == null) {
            return null;
        }

        for (File file : files) {
            if (file.isDirectory() && containsFiles(file)) {
                File result = findInStorageDirectory(file, relativePath);
                if (result != null) {
                    ILog.debug(TAG, "Found at " + result.getPath());
                    return result;
                }
            }
        }

        return null;
    }

    @Nullable
    public static File findInVolumes(@NonNull List<File> volumes, @NonNull String relativePath) {
        for (File dir : volumes) {
            File found = findInStorageDirectory(dir, relativePath);
            if (found != null) {
                ILog.info(TAG, "File has been found in" + dir.getPath());
                return found;
            }
        }
        return null;
    }

    @NonNull
    public static String getExternalStorageDir() {
        return Environment.getExternalStorageDirectory().getAbsolutePath();
    }

    @NonNull
    public static File[] getExternalStorageVolumeDirs(Context context) {
        return ContextCompat.getExternalFilesDirs(context, null);
    }

    @NonNull
    public static String getFileExtension(final File file) {
        if (file == null) return "";
        return getFileExtension(file.getPath());
    }

    @NonNull
    public static String getFileExtension(final String filePath) {
        if (isSpace(filePath)) return "";
        int lastPoi = filePath.lastIndexOf('.');
        int lastSep = filePath.lastIndexOf(File.separator);
        if (lastPoi == -1 || lastSep >= lastPoi) return "";
        return filePath.substring(lastPoi + 1);
    }

    @NonNull
    public static String getFileNameWithoutExtension(File file) {
        if (file == null) return "";
        return getFileNameWithoutExtension(file.getPath());
    }

    public static String getFileNameWithoutExtension(String filePath) {
        if (isSpace(filePath)) return "";
        int lastPoi = filePath.lastIndexOf('.');
        int lastSep = filePath.lastIndexOf(File.separator);
        if (lastSep == -1) {
            return (lastPoi == -1 ? filePath : filePath.substring(0, lastPoi));
        }
        if (lastPoi == -1 || lastSep > lastPoi) {
            return filePath.substring(lastSep + 1);
        }
        return filePath.substring(lastSep + 1, lastPoi);
    }

    @NonNull
    public static String getPackageDataDir(Context context) {
        return Objects.requireNonNull(context.getExternalFilesDir(null)).getAbsolutePath();
    }

    @NonNull
    public static String getPackageDataDir(Context context, String dir) {
        return Objects.requireNonNull(context.getExternalFilesDir(dir)).getAbsolutePath();
    }

    @NonNull
    public static String getPublicDir(String type) {
        return Environment.getExternalStoragePublicDirectory(type).getAbsolutePath();
    }

    public static String[] listAllFileNamesInFileDir() {
        return ContextManager.getApplicationContext().fileList();
    }

    @NonNull
    public static File[] listFiles(@NonNull File parent) {
        File[] children = parent.listFiles();
        return (children == null) ? new File[0] : children;
    }

    public static void recursiveDelete(@NonNull File source,
        DeleteListener deleteListener) throws IOException {
        Objects.requireNonNull(source, "File or Directory cannot be null");

        File[] directoryFiles = source.listFiles();
        if (directoryFiles != null) {
            for (File child : directoryFiles) {
                recursiveDelete(child, deleteListener);
            }
        }

        if (deleteListener != null) {
            deleteListener.onDelete(source.getAbsolutePath());
        }

        try {
            if (source.isFile()) {
                FileUtils.delete(source);
            } else if (source.isDirectory()) {
                FileUtils.deleteDirectory(source);
            }
        } catch (IOException e) {
            ILog.error(TAG, "Failed to recursively delete file or folder", e);
            throw e;
        }
    }

    public static boolean rename(final String filePath, final String newName) {
        return rename(getFileByPath(filePath), newName);
    }

    /**
     * Return the file by path.
     *
     * @param filePath The path of file.
     * @return the file
     */
    @Nullable
    public static File getFileByPath(final String filePath) {
        return isSpace(filePath) ? null : new File(filePath);
    }

    /**
     * Rename the file.
     *
     * @param file    The file.
     * @param newName The new name of file.
     * @return {@code true}: success<br>
     * {@code false}: fail
     */
    public static boolean rename(final File file, final String newName) {
        if (file == null || !file.exists() || isSpace(newName)) return false;
        if (newName.equals(file.getName())) return false;

        File newFile = new File(file.getParent() + File.separator + newName);
        return !newFile.exists() && file.renameTo(newFile);
    }

    /**
     * Return whether the string is null or white space.
     *
     * @param s The string.
     * @return {@code true}: yes<br>
     * {@code false}: no
     */
    public static boolean isSpace(final String s) {
        if (s == null) return true;
        for (int i = 0, len = s.length(); i < len; ++i) {
            if (!Character.isWhitespace(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Listener that is called periodically as a file is deleted.
     */
    public interface DeleteListener {
        void onDelete(String filePath);
    }

    public static class Path {

        @NonNull
        public static final File PLUGINS_FOLDER = Objects.requireNonNull(ContextManager
            .getApplicationContext().getExternalFilesDir("plugins"));
        public static final File ERUDA_CONSOLE = new File(PLUGINS_FOLDER, "eruda.min.js");
        // Recommended directory for file persistence
        public static final File ANCESTOR_PERSISTENT_DIRECTORY = ContextManager
            .getApplicationContext().getFilesDir();
        /**
         * App specific persistent directory
         * The file PERSISTENT_CUES_JSON stores information about persisted cues (Opened Pane and
         * Tabs)
         *
         * @since 0.0.1 <A20>
         */
        public static final File PERSISTENT_CUES_JSON = new File(ANCESTOR_PERSISTENT_DIRECTORY,
            "persisted_cues.json");
        /**
         * App specific persistent directory The directory PERSISTENCE_DIRECTORY hosts temporary
         * persisted files
         *
         * @since 0.0.1 <A20>
         */
        public static final File PERSISTENCE_DIRECTORY = new File(
            ANCESTOR_PERSISTENT_DIRECTORY + File.separator + "temp");
        /**
         * App specific code editor persistent directory The directory
         * CODE_EDITOR_PERSISTENT_DIRECTORY
         * hosts persisted {@code CodeEditorPane} file content
         *
         * @since 0.0.1 <A20>
         */
        public static final File CODE_EDITOR_PERSISTENCE_DIRECTORY = new File(
            PERSISTENCE_DIRECTORY + File.separator + "editor");

        private Path() {
            // Hide
        }
    }
}
