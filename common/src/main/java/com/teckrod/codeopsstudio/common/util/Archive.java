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
import android.util.Log;
import com.blankj.utilcode.util.FileUtils;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 * Class to handle archive files
 *
 * @author EUP
 */
public class Archive {

  public static final int BYTE_SIZE = 10240;
  private static final String LOG_TAG = "Archive";

  /** Interface to listen for events when files are unzipped. */
  public interface onUnzippedListener {
    /**
     * Callback method called when a file is unarchived.
     *
     * @param unzippedFileCount The number of files unzipped so far.
     * @param totalFileCount The total number of files in the archive.
     * @param currentFileName The name of the currently unzipped file.
     */
    void onFileUnArchiving(int unzippedFileCount, int totalFileCount, String currentFileName);

    void onLog(String message);
  }

  private onUnzippedListener listener;

  /**
   * Create an Archive instance with a listener.
   *
   * @param listener The listener for unzipping events.
   * @return An Archive instance with the specified listener.
   */
  public static Archive withListener(onUnzippedListener listener) {
    return new Archive(listener);
  }

  public Archive() {
    // Default
  }

  /**
   * Constructor for Archive with a listener.
   *
   * @param listener The listener for unzipping events.
   */
  private Archive(onUnzippedListener listener) {
    this.listener = listener;
  }

  /**
   * Sets the listener for unzipping events.
   *
   * @param listener The listener for unzipping events.
   */
  public void setListener(onUnzippedListener listener) {
    this.listener = listener;
  }

  public static void unzipFromAssets(Context context, String zipFile, String destination)
      throws IOException {
    FileUtil.unzipFromAsset(context, zipFile, destination);
  }

  /**
   * Unzips a file to the specified destination folder without creating an intermediate folder.
   *
   * @param sourceFilePath The path of the ZIP file to unzip.
   * @param destinationFolder The destination folder to unzip the files.
   * @throws IOException if an I/O error occurs.
   */
  public void unzipIntoDestination(File sourceFilePath, File destinationFolder) throws IOException {
    unzipIntoDestination(sourceFilePath.getAbsolutePath(), destinationFolder.getAbsolutePath());
  }

  /**
   * Unzips a file to the specified destination folder without creating an intermediate folder.
   *
   * @param zipFilePath The ZIP file to unzip.
   * @param destDir The destination directory.
   * @throws IOException if an I/O error occurs.
   */
  public void unzipIntoDestination(String zipFilePath, String destDir) throws IOException {
    unzipIntoDir(new File(zipFilePath), new File(destDir));
  }

  public void unzipIntoDir(final File zipFile, final File destDir) throws IOException {
    if (zipFile == null || destDir == null) return;
    ZipFile zip = new ZipFile(zipFile, ZipFile.OPEN_READ);
    int totalFiles = zip.size();
    int fileIndex = 0; // Track the file index
    Enumeration<?> entries = zip.entries();
    try {
      while (entries.hasMoreElements()) {
        ZipEntry entry = ((ZipEntry) entries.nextElement());
        String entryName = entry.getName().replace("\\", "/");
        if (entryName.contains("../")) {
          if (listener != null) {
            listener.onLog("Archive: " + "entryName: " + entryName + " is dangerous!");
          }
          continue;
        }
        if (!unzipChildFile(destDir, zip, entry, entryName, fileIndex, totalFiles)) return;
      }
    } finally {
      zip.close();
    }
  }

  private boolean unzipChildFile(
      final File destDir,
      final ZipFile zip,
      final ZipEntry entry,
      final String name,
      int index,
      int totalFiles)
      throws IOException {
    File file = new File(destDir, name);
    // Increment file index and get total count for callback
    index++;
    // Update the listener with the index, total file count and current entry name
    if (listener != null) {
      listener.onFileUnArchiving(index, totalFiles, entry.getName());
    }

    if (entry.isDirectory()) {
      return FileUtils.createOrExistsDir(file);
    } else {
      if (!FileUtils.createOrExistsFile(file)) return false;
      InputStream in = null;
      OutputStream out = null;
      try {
        in = new BufferedInputStream(zip.getInputStream(entry));
        out = new BufferedOutputStream(new FileOutputStream(file));
        byte buffer[] = new byte[BYTE_SIZE];
        int len;
        while ((len = in.read(buffer)) != -1) {
          out.write(buffer, 0, len);
        }
      } finally {
        if (in != null) {
          in.close();
        }
        if (out != null) {
          out.close();
        }
      }
    }
    return true;
  }

  public static void unzip(File sourceFilePath, File destinationFolder) throws IOException {
    unzip(sourceFilePath.getAbsolutePath(), destinationFolder.getAbsolutePath());
  }

  public static void unzip(String sourceFilePath, String destinationFolder) throws IOException {
    unzip(new FileInputStream(sourceFilePath), destinationFolder);
  }

  public static void unzip(InputStream inputStream, String destination) throws IOException{
    createDirectory(destination);
    byte[] buffer = new byte[BYTE_SIZE];
    ZipInputStream zipInputStream = new ZipInputStream(inputStream);
    ZipEntry zipEntry = zipInputStream.getNextEntry();

    while (zipEntry != null) {
      String entryName = zipEntry.getName();

      Log.v(LOG_TAG, "Unzipping " + entryName);
      if (zipEntry.isDirectory()) {
        createDirectory(destination + File.separator + entryName);
      } else {
        File file = new File(destination, entryName);
        if (!file.exists()) {
          if (file.getParentFile() == null || !file.getParentFile().exists()) {
            if (!file.getParentFile().mkdirs()) {
              continue;
            }
          }

          if (!file.createNewFile()) {
            Log.w(LOG_TAG, "Failed to create file " + file.getName());
            continue;
          }

          try (OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file))) {
            int bytesRead;
            while ((bytesRead = zipInputStream.read(buffer)) != -1) {
              outputStream.write(buffer, 0, bytesRead);
            }
          }
        }
      }
      zipInputStream.closeEntry();
      zipEntry = zipInputStream.getNextEntry();
    }
    zipInputStream.close();
  }

  private static void createDirectory(String directory) {
    File dir = new File(directory);
    if (!dir.exists() && !dir.mkdirs()) {
      Log.w(LOG_TAG, "Failed to create directory " + directory);
    }
  }
}
