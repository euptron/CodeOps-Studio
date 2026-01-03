/*
 * This file is part of CodeOps Studio.
 * CodeOps Studio - Code anywhere anytime
 * https://github.com/euptron/CodeOps-Studio
 * Copyright (C) 2024-2026 Etido Peter
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

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.eup.codeopsstudio.common.Constants;
import com.eup.codeopsstudio.common.ILog;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Objects;

/**
 * A class to handle the core functionalities of a {@code Uri}
 *
 * @author Etido Peter
 */
public class UriUtils {

    public static final String TAG = "UriUtils";

    /**
     * Checks if a uri is writeable
     *
     * @param uri     the uri to check is writeable
     * @param context the context used to open the uri
     */
    public boolean canWrite(Uri uri, Context context) {
        try {
            OutputStream os = context.getContentResolver().openOutputStream(uri, "wa");
            if (os != null) {
                os.close();
                return true;
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    public static boolean isEditable(Context context, Uri uri) {
        if (context.checkCallingOrSelfUriPermission(uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            != PackageManager.PERMISSION_GRANTED) {
            return false;
        }

        final String type = getRawType(context, uri);
        final int flags = queryForInt(context, uri, DocumentsContract.Document.COLUMN_FLAGS, 0);

        if (TextUtils.isEmpty(type)) return false;

        if (DocumentsContract.Document.MIME_TYPE_DIR.equals(type)
            && (flags & DocumentsContract.Document.FLAG_DIR_SUPPORTS_CREATE) != 0) {
            return true;
        } else {
            return !TextUtils.isEmpty(type)
                && (flags & DocumentsContract.Document.FLAG_SUPPORTS_WRITE) != 0;
        }
    }

    @Nullable
    private static String getRawType(Context context, Uri self) {
        return queryForString(context, self, DocumentsContract.Document.COLUMN_MIME_TYPE, null);
    }

    @Nullable
    private static String queryForString(Context context, Uri self, String column,
        @Nullable String defaultValue) {
        final ContentResolver resolver = context.getContentResolver();

        Cursor c = null;
        try {
            c = resolver.query(self, new String[]{column}, null, null, null);
            if (c.moveToFirst() && !c.isNull(0)) {
                return c.getString(0);
            } else {
                return defaultValue;
            }
        } catch (Exception e) {
            ILog.warning(TAG, "Failed query: " + e);
            return defaultValue;
        } finally {
            closeQuietly(c);
        }
    }

    private static void closeQuietly(@Nullable AutoCloseable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (RuntimeException rethrown) {
                throw rethrown;
            } catch (Exception ignored) {
            }
        }
    }

    private static int queryForInt(Context context, Uri self, String column, int defaultValue) {
        return (int) queryForLong(context, self, column, defaultValue);
    }

    private static long queryForLong(Context context, Uri self, String column, long defaultValue) {
        final ContentResolver resolver = context.getContentResolver();

        Cursor c = null;
        try {
            c = resolver.query(self, new String[]{column}, null, null, null);
            if (c.moveToFirst() && !c.isNull(0)) {
                return c.getLong(0);
            } else {
                return defaultValue;
            }
        } catch (Exception e) {
            ILog.warning(TAG, "Failed query: " + e);
            return defaultValue;
        } finally {
            closeQuietly(c);
        }
    }

    /**
     * Over writes a document uri with a new content.
     *
     * @param uri  the uri to write
     * @param data the content to write to the uri
     * @throws IOException           in case of an I/O error
     * @throws FileNotFoundException
     */
    public static void overWriteDocument(final Context context, final Uri uri,
        final String data) throws IOException {
        try {
            ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "w");
            var fos = new FileOutputStream(pfd.getFileDescriptor());
            fos.write(data.getBytes());
            fos.close();
            pfd.close();
            ILog.info(TAG, "File saved successfully");
        } catch (Exception e) {
            throw new IOException(e.getMessage(), e.getCause());
        }
    }

    public static String readDocumentToString(Context context, Uri uri) throws IOException {
        return readDocumentToString(context, uri, Constants.DEFAULT_CHAR_SET);
    }

    /**
     * Reads the contents of a uri into a String. The uri is always closed.
     *
     * @param uri         the uri to read, must not be {@code null}
     * @param charsetName the name of the requested charset, {@code null} means platform default
     * @return the uri contents, never {@code null}
     * @throws NullPointerException if uri is {@code null}.
     * @throws IOException          if an I/O error occurs, including when the uri does not
     *                              exist, is a
     *                              directory rather than a regular uri, or for some other reason
     *                              why the uri cannot be opened
     *                              for reading.
     */
    public static String readDocumentToString(Context context, Uri uri,
        final Charset charset) throws IOException {
        return IOUtils.toString(() -> getInputStream(context, uri), Charsets.toCharset(charset));
    }

    public static InputStream getInputStream(Context context, Uri uri) throws IOException {
        try {
            return context.getContentResolver().openInputStream(uri);
        } catch (FileNotFoundException e) {
            throw new IOException(e.getMessage(), e.getCause());
        }
    }

    public static String readDocumentToString(Context context, Uri uri,
        final String charsetName) throws IOException {
        return readDocumentToString(context, uri, Charsets.toCharset(charsetName));
    }

    /**
     * Maps an existing directory {@code Uri} to its root directory
     *
     * <p>Root access always requires a tree URI, regardless of input type
     *
     * @param uri   Original URI (used to extract authority)
     * @param docID Root document ID (e.g., "primary:", "home:") also the first part of the
     *              documentID
     * @return Root URI in tree format for directory access
     */
    public static Uri revertPathToRoot(final Uri uri, final String docID) {
        final String authority = uri.getAuthority();
        return DocumentsContract.buildTreeDocumentUri(authority, docID);
    }

    public static void takePersistableUriPermission(Context context, Uri uri, int flags) {
        try {
            context.getContentResolver().takePersistableUriPermission(uri, flags);
            toast(context, "Permission granted for selected resource");
        } catch (Exception e) {
            toast(context, "Error taking permission: for selected resource" + e.getMessage());
        }
    }

    private static void toast(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }

    public static void writeStringToDocument(final Context context, final Uri uri,
        final String data) throws IOException {
        writeStringToDocument(context, uri, data, Constants.DEFAULT_CHAR_SET, false);
    }

    public static void writeStringToDocument(final Context context, final Uri uri,
        final String data, final String charsetName) throws IOException {
        writeStringToDocument(context, uri, data, Charsets.toCharset(charsetName), false);
    }

    // Adopted from androidx.documentfile.provider.DocumentsContractApi19

    public static void writeStringToDocument(final Context context, final Uri uri,
        final String data, final Charset charset) throws IOException {
        writeStringToDocument(context, uri, data, charset, false);
    }

    public static void writeToDocument(final Context context, final Uri uri,
        final CharSequence data) throws IOException {
        writeToDocument(context, uri, Objects.toString(data, null), Constants.DEFAULT_CHAR_SET,
            false);
    }

    /**
     * Writes a CharSequence to a uri creating the uri if it does not exist.
     *
     * @param uri     the uri to write
     * @param data    the content to write to the uri
     * @param charset the charset to use, {@code null} means platform default
     * @param append  if {@code true}, then the data will be added to the end of the uri rather than
     *                overwriting
     * @throws IOException in case of an I/O error
     */
    public static void writeToDocument(final Context context, final Uri uri,
        final CharSequence data, final Charset charset, final boolean append) throws IOException {
        writeStringToDocument(context, uri, Objects.toString(data, null), charset, append);
    }

    /**
     * Writes a String to a uri, creating the uri if it does not exist. The parent directories of
     * the
     * uri are created if they do not exist.
     *
     * @param uri         the uri to write
     * @param data        the content to write to the uri
     * @param charsetName the name of the requested charset, {@code null} means platform default
     * @param append      if {@code true}, then the String will be added to the end of the uri
     *                    rather than
     *                    overwriting
     * @throws IOException                                  in case of an I/O error
     * @throws java.nio.charset.UnsupportedCharsetException if the encoding is not supported by
     *                                                      the VM
     */
    public static void writeStringToDocument(final Context context, final Uri uri,
        final String data, final Charset charset, final boolean append) throws IOException {

        String mode = (append) ? "wa" : " w";

        try (OutputStream out = context.getContentResolver().openOutputStream(uri, mode)) {
            IOUtils.write(data, out, charset);
        }
    }

    public static void writeToDocument(final Context context, final Uri uri,
        final CharSequence data, final String charsetName) throws IOException {
        writeToDocument(context, uri, Objects.toString(data, null),
            Charsets.toCharset(charsetName), false);
    }

    public static void writeToDocument(final Context context, final Uri uri,
        final CharSequence data, final String charsetName,
        final boolean append) throws IOException {
        writeToDocument(context, uri, Objects.toString(data, null),
            Charsets.toCharset(charsetName), append);
    }
}
