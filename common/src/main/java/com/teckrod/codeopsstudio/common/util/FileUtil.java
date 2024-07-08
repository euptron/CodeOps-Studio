/*************************************************************************
 * This file is part of CodeOps Studio.
 * CodeOps Studio - code anywhere anytime
 * https://github.com/etidoUP/CodeOps-Studio
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

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.util.Log;
import com.blankj.utilcode.util.FileUtils;
import com.eup.codeopsstudio.common.ContextManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.net.URLDecoder;

/**
 * Adapation file utility to suite that needs com.blankj.utilcode.util.FileUtil and
 * org.apache.commons.io.FileUtils doesn't provide
 *
 * @since 0.0.1
 */
public class FileUtil {

  public static final int BYTE = 1;
  public static final int KB = 1024;
  public static final int MB = 1048576;
  public static final int GB = 1073741824;
  private static final int BUFFER_SIZE = MB * 10;
  private static final String TAG = "FileUtil";
  private static Uri contentUri = null;

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

  public static FileInputStream getFileInputStreamFromAssetsFile(Context context, String filename)
      throws IOException {
    File file = new File(context.getFilesDir(), filename);
    FileInputStream fis = new FileInputStream(file);
    return fis;
  }

  public static void unzipFromAssets(Context context, String zipFile, String destination) {
    try {
      if (destination == null || destination.length() == 0)
        destination = context.getFilesDir().getAbsolutePath();
      try (InputStream stream = context.getAssets().open(zipFile)) {
        unzip(stream, destination);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static boolean rename(File file, String newName) {
    return FileUtils.rename(file, newName);
  }

  public static void unzipFromAsset(Context context, String zipFile, String destination)
      throws IOException {
    unzip(openAssetFile(context, zipFile), destination);
  }

  @SuppressWarnings("unused")
  public static void unzip(String zipFile, String location) {
    try (FileInputStream fin = new FileInputStream(zipFile)) {
      unzip(fin, location);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void unzip(InputStream stream, String destination) {
    checkDir(destination, "");
    byte[] buffer = new byte[BUFFER_SIZE];
    try {
      ZipInputStream zin = new ZipInputStream(stream);
      ZipEntry ze;

      while ((ze = zin.getNextEntry()) != null) {
        Log.v(TAG, "Unzipping " + ze.getName());

        if (ze.isDirectory()) {
          checkDir(destination, ze.getName());
        } else {
          File f = new File(destination, ze.getName());
          if (!f.exists()) {
            if (f.getParentFile() == null || !f.getParentFile().exists()) {
              if (!f.getParentFile().mkdirs()) {
                continue;
              }
            }
            boolean success = f.createNewFile();
            if (!success) {
              Log.w(TAG, "Failed to create file " + f.getName());
              continue;
            }
            FileOutputStream fout = new FileOutputStream(f);
            int count;
            while ((count = zin.read(buffer)) != -1) {
              fout.write(buffer, 0, count);
            }
            zin.closeEntry();
            fout.close();
          }
        }
      }
      zin.close();
    } catch (Exception e) {
      Log.e(TAG, "unzip", e);
    }
  }

  public static void checkDir(String destination, String dir) {
    File f = new File(destination, dir);
    if (!f.isDirectory()) {
      boolean success = f.mkdirs();
      if (!success) {
        // throws exception if failed to create folder
        Log.w(TAG, "Failed to create folder " + f.getName());
      }
    }
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

  /**
   * Returns a file name without extension
   *
   * @param file The file
   */
  public static String getFileNameWithoutExtension(File file) {
    if (file.getName() == null) return null;
    String ext = file.getName().substring(file.getName().lastIndexOf(".") + 1);

    if (ext != null && ext.startsWith(".")) {
      return file.getName().substring(0, file.getName().length() - ext.length() - 1);
    }
    return file.getName();
  }

  /**
   * Returns a file extension
   *
   * @param file The file
   */
  public static String getFileExtension(File file) {
    if (file.getName() == null) return null;
    String ext = file.getName().substring(file.getName().lastIndexOf(".") + 1);

    /*
     * Some Textmate file extension may exist without `.`
     */
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

  /** Recursively deletes a directory and its content. */
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
  
  public static String getPathFromUri(final Context context, final Uri uri) throws Exception {
    String path = null;
    if (DocumentsContract.isDocumentUri(context, uri)) {
      if (isExternalStorageDocument(uri)) {
        final String docId = DocumentsContract.getDocumentId(uri);
        final String[] split = docId.split(":");
        final String type = split[0];

        if ("primary".equalsIgnoreCase(type)) {
          path = Environment.getExternalStorageDirectory() + "/" + split[1];
        }
      } else if (isDownloadsDocument(uri)) {
        final String id = DocumentsContract.getDocumentId(uri);

        if (!TextUtils.isEmpty(id)) {
          if (id.startsWith("raw:")) {
            return id.replaceFirst("raw:", "");
          }
        }

        final Uri contentUri =
            ContentUris.withAppendedId(
                Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

        path = getDataColumn(context, contentUri, null, null);
      } else if (isMediaDocument(uri)) {
        final String docId = DocumentsContract.getDocumentId(uri);
        final String[] split = docId.split(":");
        final String type = split[0];

        Uri contentUri = null;
        if ("image".equals(type)) {
          contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        } else if ("video".equals(type)) {
          contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        } else if ("audio".equals(type)) {
          contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        }

        final String selection = MediaStore.Audio.Media._ID + "=?";
        final String[] selectionArgs = new String[] {split[1]};

        path = getDataColumn(context, contentUri, selection, selectionArgs);
      }
    } else if (ContentResolver.SCHEME_CONTENT.equalsIgnoreCase(uri.getScheme())) {
      path = getDataColumn(context, uri, null, null);
    } else if (ContentResolver.SCHEME_FILE.equalsIgnoreCase(uri.getScheme())) {
      path = uri.getPath();
    }

    if (path != null) {
      try {
        return URLDecoder.decode(path, "UTF-8");
      } catch (Exception e) {
        return null;
      }
    }
    return null;
  }
  
  private static String getDataColumn(
      Context context, Uri uri, String selection, String[] selectionArgs) {
    Cursor cursor = null;

    final String column = MediaStore.Images.Media.DATA;
    final String[] projection = {column};

    try {
      cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
      if (cursor != null && cursor.moveToFirst()) {
        final int column_index = cursor.getColumnIndexOrThrow(column);
        return cursor.getString(column_index);
      }
    } catch (Exception e) {

    } finally {
      if (cursor != null) cursor.close();
    }
    return null;
  }

  private static boolean isExternalStorageDocument(Uri uri) {
    return "com.android.externalstorage.documents".equals(uri.getAuthority());
  }

  private static boolean isDownloadsDocument(Uri uri) {
    return "com.android.providers.downloads.documents".equals(uri.getAuthority());
  }

  private static boolean isMediaDocument(Uri uri) {
    return "com.android.providers.media.documents".equals(uri.getAuthority());
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
}
