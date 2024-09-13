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

import com.blankj.utilcode.util.FileUtils;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.io.InputStream;
import java.io.FileInputStream;
import android.content.Context;
import java.util.Enumeration;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.util.Enumeration;

/**
 * Class to handle archive files
 *
 * @author EUP
 */
public class Archive {

  public static final int BYTE_SIZE = 10240; // 10MB Cap

  protected static final int BUFFER_SIZE = BYTE_SIZE;

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
   * Unzips a file to the specified destination folder.
   *
   * @param sourceFilePath The file object representing the ZIP file to unzip.
   * @param destinationFolder The file object representing the destination folder to unzip the
   *     files.
   * @throws IOException if an I/O error occurs.
   */
  public void unzip(File sourceFilePath, File destinationFolder) throws IOException {
    unzip(sourceFilePath.getAbsolutePath(), destinationFolder.getAbsolutePath());
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

  public void unzip(String sourceFilePath, String destinationFolder) throws IOException {
    byte[] buffer = new byte[BYTE_SIZE];
    FileInputStream fis = new FileInputStream(sourceFilePath);
    ZipInputStream zipInputStream = new ZipInputStream(fis);
    ZipEntry zipEntry = zipInputStream.getNextEntry();
    ZipFile zipFile = new ZipFile(new File(sourceFilePath), ZipFile.OPEN_READ);

    int fileIndex = 0; // Track the file index

    while (zipEntry != null) {
      String entryName = zipEntry.getName();

      File file = new File(destinationFolder + File.separator + entryName);

      // Increment file index and get total count for callback
      fileIndex++;
      int totalFiles = zipFile.size();

      // Update the listener with the correct index and total count
      if (listener != null) {
        listener.onFileUnArchiving(fileIndex, totalFiles, zipEntry.getName());
      }

      if (zipEntry.isDirectory()) {
        file.mkdirs();
      } else {
        file.getParentFile().mkdirs();
        try (OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file))) {
          int len;
          while ((len = zipInputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, len);
          }
        }
      }

      // close zipInputStream to prevent memory leaks
      zipInputStream.closeEntry();
      zipEntry = zipInputStream.getNextEntry();
    }
    // just added below
    zipInputStream.close();
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
        byte buffer[] = new byte[BUFFER_SIZE];
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
}
