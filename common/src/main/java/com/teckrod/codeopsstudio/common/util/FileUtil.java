/*************************************************************************
 * This file is part of CodeOps Studio.
 * CodeOps Studio - code anywhere anytime
 * https://github.com/euptron/CodeOps-Studio
 * Copyright (C) 2024 EUP
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
 * If you have more questions, feel free to message EUP if you have any
 * questions or need additional information. Email: etido.up@gmail.com
 *************************************************************************/

package com.eup.codeopsstudio.common.util;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import androidx.core.content.ContextCompat;
import com.blankj.utilcode.util.FileUtils;
import com.eup.codeopsstudio.common.ContextManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Adapation file utility to suite that needs com.blankj.utilcode.util.FileUtil and
 * org.apache.commons.io.FileUtils doesn't provide
 *
 * @since 0.0.1
 */
public class FileUtil {

  private static final String TAG = "FileUtil";

  /** Listener that is called periodically as progress is made. */
  public interface ProgressListener {
    public void onProgress(double progress);
  }

  /** Listener that is called periodically as a file is deleted. */
  public interface DeleteListener {
    public void onDelete(String filePath);
  }

  public static InputStream openAssetFile(Context context, String filename) throws IOException {
    return context.getAssets().open(filename);
  }

  public static boolean rename(File file, String newName) {
    return FileUtils.rename(file, newName);
  }

  public static void unzipFromAsset(Context context, String zipFile, String destination)
      throws IOException {
    Archive.unzip(openAssetFile(context, zipFile), destination);
  }

  // Hash file
  public static String calculateMD5(File updateFile) {
    InputStream is;
    try {
      is = new FileInputStream(updateFile);
    } catch (FileNotFoundException e) {
      Log.e("calculateMD5", "Exception while getting FileInputStream", e);
      return null;
    }

    return calculateMD5(is);
  }

  public static String calculateMD5(InputStream is) {
    MessageDigest digest;
    try {
      digest = MessageDigest.getInstance("MD5");
    } catch (NoSuchAlgorithmException e) {
      Log.e("calculateMD5", "Exception while getting Digest", e);
      return null;
    }

    byte[] buffer = new byte[8192];
    int read;
    try {
      while ((read = is.read(buffer)) > 0) {
        digest.update(buffer, 0, read);
      }
      byte[] md5sum = digest.digest();
      BigInteger bigInt = new BigInteger(1, md5sum);
      String output = bigInt.toString(16);
      // Fill to 32 chars
      output = String.format("%32s", output).replace(' ', '0');
      return output;
    } catch (IOException e) {
      throw new RuntimeException("Unable to process file for MD5", e);
    } finally {
      try {
        is.close();
      } catch (IOException e) {
        Log.e("calculateMD5", "Exception on closing MD5 input stream", e);
      }
    }
  }

  public static String getFileNameWithoutExtension(File file) {
    if (file.getName() == null) return null;
    String ext = file.getName().substring(file.getName().lastIndexOf(".") + 1);

    if (ext != null && ext.startsWith(".")) {
      return file.getName().substring(0, file.getName().length() - ext.length() - 1);
    }
    return file.getName();
  }

  public static String getFileExtension(File file) {
    if (file.getName() == null) return null;
    String ext = file.getName().substring(file.getName().lastIndexOf(".") + 1);
    // Some file extensions may exist without `.`
    if (ext != null) return ext;
    return null;
  }

  public static String getExternalStorageDir() {
    return Environment.getExternalStorageDirectory().getAbsolutePath();
  }

  public static String getPackageDataDir(Context context) {
    return context.getExternalFilesDir(null).getAbsolutePath();
  }

  public static String getPackageDataDir(Context context, String dir) {
    return context.getExternalFilesDir(dir).getAbsolutePath();
  }

  public static String getPublicDir(String type) {
    return Environment.getExternalStoragePublicDirectory(type).getAbsolutePath();
  }

  public static void clearAppCache(Context context) {
    try {
      File dir = context.getCacheDir();
      org.apache.commons.io.FileUtils.delete(new File(dir.getAbsolutePath()));
    } catch (Exception e) {
      e.printStackTrace();
    }
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
    String name, ext;
    if (dot != -1) {
      name = filename.substring(0, dot);
      ext = filename.substring(dot);
    } else {
      name = filename;
      ext = "";
    }

    int num = 0;
    do {
      f = new File(name + (++num == 1 ? "" : " (" + num + ")") + ext);
    } while (f.exists());

    return name + (num == 1 ? "" : " (" + num + ")") + ext;
  }

  public static void recursiveDelete(File fileOrDirectory, DeleteListener deleteListener) {
    File[] directoryFiles = fileOrDirectory.listFiles();
    if (directoryFiles != null) {
      for (File child : directoryFiles) {
        recursiveDelete(child, deleteListener);
      }
    }
    if (deleteListener != null) {
      deleteListener.onDelete(fileOrDirectory.getAbsolutePath());
    }
    fileOrDirectory.delete();
  }

  /**
   * List all file names in the filesDir directory
   *
   * @return An array containing the names of all files within the filesDir directory
   */
  public static String[] listAllFileNamesInFileDir() {
    return ContextManager.getApplicationContext().fileList();
  }

  public static File[] getExternalStorageVolumeDirs(Context context) {
    return ContextCompat.getExternalFilesDirs(context, null);
  }

  /**
   * Return root directory where all external storage devices will be mounted. For example, {@link
   * #getExternalStorageDirectory()} will appear under this location.
   */
  public static File findInVolumes(String relativePath) {
    return findInStorageDirectory(Environment.getStorageDirectory(), relativePath);
  }

  /**
   * Searches a directory for a file with the given relative path.
   *
   * @param currentDir the root directory to search in
   * @param relativePath the path relative to the parent volume e.g Download/EUP
   * @return the file if it exists in any volume, may be {@code null}
   */
  public static File findInStorageDirectory(File currentDir, String relativePath) {
    if (currentDir == null || !currentDir.exists() || !currentDir.isDirectory()) {
      return null;
    }

    File targetFile = new File(currentDir, relativePath);
    if (targetFile.exists()) {
      return targetFile;
    }

    File[] files = currentDir.listFiles();

    if (files == null || files.length == 0) {
      return null; // Skip empty directories
    }

    for (File file : files) {
      if (file.isDirectory() && containsFiles(file)) {
        File result = findInStorageDirectory(file, relativePath);
        if (result != null) {
          return result;
        }
      }
    }

    return null;
  }

  /**
   * Checks if a directory contains files or subdirectories (non-empty).
   *
   * @param dir The directory to check
   * @return {@code true} if the directory contains files or folders, otherwise {@code false}
   */
  public static boolean containsFiles(File dir) {
    File[] files = dir.listFiles();
    return files != null && files.length > 0;
  }

  public static class Path {
    public static final File PLUGINS_FOLDER =
        ContextManager.getApplicationContext().getExternalFilesDir("plugins");
    public static final File ERUDA_CONSOLE = new File(PLUGINS_FOLDER, "eruda.min.js");
    // Recommended directory for file persistence
    public static final File ANCESTOR_PERSISTENT_DIRECTORY =
        ContextManager.getApplicationContext().getFilesDir();
    /*
     * App specific persistent directory
     * The file PERSISTENT_CUES_JSON stores information about persisted cues (Opened Pane and Tabs)
     * @since 0.0.1 <A20>
     */
    public static final File PERSISTENT_CUES_JSON =
        new File(ANCESTOR_PERSISTENT_DIRECTORY, "persisted_cues.json");

    /**
     * App specific persistent directory The directory PERSISTENCE_DIRECTORY hosts temporary
     * persisted files
     *
     * @since 0.0.1 <A20>
     */
    public static final File PERSISTENCE_DIRECTORY =
        new File(ANCESTOR_PERSISTENT_DIRECTORY + File.separator + "temp");

    /**
     * App specific code editor persistent directory The directory CODE_EDITOR_PERSISTENT_DIRECTORY
     * hosts persisted {@code CodeEditorPane} file content
     *
     * @since 0.0.1 <A20>
     */
    public static final File CODE_EDITOR_PERSISTENCE_DIRECTORY =
        new File(PERSISTENCE_DIRECTORY + File.separator + "editor");
  }

  public static File[] listFiles(File parent) {
    File[] children = parent.listFiles();
    return (children == null) ? new File[0] : children;
  }
}
