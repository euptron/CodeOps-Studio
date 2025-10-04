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

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.BaseColumns;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.documentfile.provider.DocumentFile;

import com.eup.codeopsstudio.common.Constants;
import com.eup.codeopsstudio.common.ILog;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Utility class to mediate between URIs and Files in the Android filesystem.
 *
 * <p>This class handles the resolution of a {@code Uri} to actual {@code File}, supporting various
 * storage types such as primary storage, home directory, and external storage devices like SD
 * cards.
 *
 * <p>I can't improve this class currently due to my lack of resources, no 100% support for
 * non-system uri exists here
 *
 * @author Etido Peter
 * @version 1.0
 * @see Cursor
 * @see ContentResolver#query(Uri, String[], String, String[], String)
 * @since 1.0
 */
public class FileUriMediator {

    private static final String TAG = "FileUriMediator";
    private final Uri uri;
    private final Context context;
    private final DocumentFile documentFile;
    private final boolean isDirectory;
    private final boolean isTreeUri;

    public FileUriMediator(@NonNull Uri uri, @NonNull Context context) {
        this.uri     = uri;
        this.context = context;

        if (DocumentsContract.isDocumentUri(context, uri)) {
            documentFile = DocumentFile.fromSingleUri(context, uri);
            isTreeUri    = false;
        } else {
            documentFile = DocumentFile.fromTreeUri(context, uri);
            isTreeUri    = true;
        }

        if (documentFile != null) {
            isDirectory = documentFile.isDirectory();
            boolean isVirtual = documentFile.isVirtual();
            ILog.debug(TAG, "Document is virtual: " + isVirtual);
        } else {
            throw new IllegalArgumentException("Invalid URI");
        }
    }

    public String getAbsoluteRelativePath() {
        var path = getRelativePath();
        if (path == null) return "";
        return path.startsWith(File.separator) ? path : File.separator + path;
    }

    public String getAuthority() {
        return uri.getAuthority();
    }

    /**
     * Retrieves the display name from the URI. This name is provider-specific and may differ
     * from the
     * actual file name.
     *
     * @return String representing the display name, or null if not available
     */
    public String getDisplayName() {
        try (Cursor cursor = query(uri, new String[]{OpenableColumns.DISPLAY_NAME}, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (!cursor.isNull(nameIndex) && nameIndex != -1) {
                    return cursor.getString(nameIndex);
                }
            }
        } catch (Exception e) {
            // ignore
        }
        return null;
    }

    public DocumentFile getDocumentFile() {
        return this.documentFile;
    }

    public static String getDocumentId(Uri uri, boolean isTreeDocUri) {
        if (isTreeDocUri) return DocumentsContract.getTreeDocumentId(uri);
        return DocumentsContract.getDocumentId(uri);
    }

    public String getDocumentId() {
        return getDocumentId(uri, isTreeUri);
    }

    /**
     * Resolves the URI to a File object. Handles different storage types including primary storage,
     * home directory, and external storage.
     *
     * @return File object representing the URI location
     */
    public File getFile() {
        String authority = getAuthority();

        if (isDirectory) {
            return handleExternalStorageAuth();
        } else {
            return switch (authority) {
                case StorageVolumeAuthority.EXTERNAL -> handleExternalStorageAuth();
                case StorageVolumeAuthority.DOWNLOAD -> handleDownloadStorageAuth();
                case StorageVolumeAuthority.MEDIA -> handleMediaStorageAuth();
                case StorageVolumeAuthority.GOOGLE_DRIVE,
                     StorageVolumeAuthority.GOOGLE_DRIVE_LEGACY, StorageVolumeAuthority.WHATSAPP,
                     StorageVolumeAuthority.CONTENT -> handleContentStorageAuth();
                case StorageVolumeAuthority.FILE -> handleFileStorageAuth();
                default -> throw new UnsupportedOperationException(
                    "Unsupported URI authority: " + authority);
            };
        }
    }

    /**
     * Retrieves the file name from the URI. Falls back to parsing the URI path if the display
     * name is
     * not available.
     *
     * @return String representing the file name
     */
    public String getFileName() {
        String name = isContentUri() ? getDisplayName() : null;
        if (name != null) return name;

        String path = Objects.requireNonNull(uri.getPath());
        int lastSlash = path.lastIndexOf('/');
        return lastSlash != -1 ? path.substring(lastSlash + 1) : path;
    }

    public String getMimeType() {
        if (isContentUri()) {
            return context.getContentResolver().getType(uri);
        }

        String path = Objects.requireNonNull(uri.getPath());
        String extension = MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(new File(path))
                                                                  .toString());
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.toLowerCase());
    }

    private boolean isContentUri() {
        return ContentResolver.SCHEME_CONTENT.equals(uri.getScheme());
    }

    /**
     * Retrieves the path relative to the root directory. This path is independent of the actual
     * root
     * or parent directory location.
     *
     * @return String representing the relative path
     */
    public String getRelativePath() {
        String[] split = getSplit();

        try {
            final String path = split[1];
            return (path == null || path.isEmpty()) ? "" : path;
        } catch (ArrayIndexOutOfBoundsException e) {
            if (getDocumentId().contains(":")) {
                if (!hasColonSuffix(split)) {
                    if (getStorageType().equalsIgnoreCase("primary")) {
                        return Environment.getExternalStorageDirectory().getPath();
                    } else if (getStorageType().equalsIgnoreCase("home")) {
                        return new File(Environment.getExternalStorageDirectory() + File.separator
                            + Environment.DIRECTORY_DOCUMENTS).getPath();
                    } else {
                        return new File(getStorageDirectory(), getStorageType()).getPath();
                    }
                }
            } else {
                // TODO: Handle Storage access provider path instead
                return new File(getDocumentId()).getPath();
            }
        }
        return "";
    }

    public static ArrayList<File> getSearchVolumes() {
        /*
         * Search directories
         *  /storage/emulated/0/
         *  /storage/
         */
        ArrayList<File> files = new ArrayList<>();
        files.add(Environment.getExternalStorageDirectory());
        files.add(getStorageDirectory());
        return files;
    }

    /**
     * Retrieves the size of the file represented by the URI.
     *
     * @return long representing the file size in bytes, or -1 if size is unknown
     */
    public long getSize() {
        try (Cursor cursor = query(uri, new String[]{OpenableColumns.SIZE}, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                if (!cursor.isNull(sizeIndex) && sizeIndex != -1) {
                    return cursor.getLong(sizeIndex);
                }
            }
        } catch (Exception e) {
            // ignore
        }
        return -1;
    }

    /**
     * Creates a cursor for querying the URI with specified parameters. For optimal performance, use
     * explicit projections and parameter markers.
     *
     * @param uri        The URI to query
     * @param projection Column projection array
     */
    private Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs) {
        return context.getContentResolver()
                      .query(uri, projection, selection, selectionArgs, /*sortOrder*/ null);
    }

    /**
     * Determines the storage type from the document ID.
     *
     * @return String representing the storage type ("primary", "home", or external storage
     * identifier)
     */
    public String getStorageType() {
        String type = getSplit()[0];
        return (type == null || type.isEmpty()) ? "" : type;
    }

    public Uri getUri() {
        return this.uri;
    }

    public static boolean isAllowedAuthority(String authority) {
        return StorageVolumeAuthority.getAllowedAuthorities().contains(authority);
    }

    public boolean isTreeUri() {
        return this.isTreeUri;
    }

    public static FileUriMediator resolveDocument(Uri uri, Context context) {
        if (!DocumentsContract.isDocumentUri(context, uri)) {
            throw new IllegalArgumentException("Invalid URI: Not a document URI");
        }

        DocumentFile documentFile = DocumentFile.fromSingleUri(context, uri);

        if (documentFile == null || !documentFile.exists()) {
            throw new IllegalArgumentException("Invalid URI: Document does not exist");
        }

        return new FileUriMediator(documentFile.getUri(), context);
    }

    @NonNull
    public static FileUriMediator resolveTree(@NonNull Uri uri, @NonNull Context context) {
        Uri treeUri = Objects.requireNonNull(DocumentFile.fromTreeUri(context, uri)).getUri();
        return new FileUriMediator(treeUri, context);
    }

    private String getDataColumn(Uri uri) {
        return getDataColumn(uri, null, null);
    }

    /**
     * Query the given URI, returning a {@link String} value of the a data column.
     *
     * @param uri           The URI, using the content:// scheme, for the content to retrieve.
     * @param selection     A filter declaring which rows to return, formatted as an SQL WHERE
     *                      clause
     *                      (excluding the WHERE itself). Passing null will return all rows for
     *                      the given URI.
     * @param selectionArgs You may include ?s in selection, which will be replaced by the values
     *                      from
     *                      selectionArgs, in the order that they appear in the selection. The
     *                      values will be bound as
     *                      Strings.
     * @return The value of the _data column, which is typically a file path.
     */
    @Nullable
    private String getDataColumn(Uri uri, String selection, String[] selectionArgs) {
        final String column = MediaStore.MediaColumns.DATA;

        try (Cursor cursor = query(uri, new String[]{column}, selection, selectionArgs)) {
            if (cursor != null && cursor.moveToFirst()) {
                int column_index = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA);
                if (!cursor.isNull(column_index) && column_index != -1) {
                    return cursor.getString(column_index);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(
                e.getClass().getSimpleName() + "Error querying data column: " + e.getMessage());
        }
        return null;
    }

    private String[] getSplit() {
        return getDocumentId().split(":");
    }

    /**
     * Adopted from {@link Environment#getStorageDirectory()}
     *
     * @return the storage directory
     */
    @NonNull
    private static File getStorageDirectory() {
        final String ENV_EXTERNAL_STORAGE = "EXTERNAL_STORAGE";
        final String storagePath = "/storage";
        String path = System.getenv(ENV_EXTERNAL_STORAGE);
        return path == null ? new File(storagePath) : new File(path);
    }

    @Nullable
    private File handleContentStorageAuth() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ? null : new File(getDataColumn(uri));
    }

    private File handleDownloadStorageAuth() {
        String id = getDocumentId();

        if (!TextUtils.isEmpty(id)) {
            if (id.startsWith("raw:")) {
                var file = new File(id.replaceFirst("raw:", ""));
                if (file.exists()) return file;
            } else if (id.startsWith("msf:")) {
                // Android 11+
                id = getStorageType();
            }

            String[] segments = new String[]{
                "content://downloads/public_downloads",
                "content://downloads/my_downloads",
                "content://downloads/all_downloads"
            };

            for (String segment : segments) {
                try {
                    final Uri contentUri = ContentUris.withAppendedId(Uri.parse(segment),
                        Long.parseLong(id));
                    File file = new File(getDataColumn(contentUri));
                    if (file.exists()) return file;
                } catch (Exception e) {
                    String path = Objects.requireNonNull(uri.getPath());
                    // Ignore because in Android 8 and 9 the id is not a number
                    File file = new File(path.replaceFirst("^/document/raw:", "")
                                             .replaceFirst("^raw:", ""));
                    if (file.exists()) return file;
                }
            }
        }

        // final fallback, this search may be time consuming
        return FileUtil.findInVolumes(getSearchVolumes(),
            Environment.DIRECTORY_DOWNLOADS + File.separator + getFileName());
    }

    private File handleExternalStorageAuth() {
        String storageType = getStorageType();
        String relativePath = getAbsoluteRelativePath();

        if (getDocumentId().contains(":")) {
            if (hasColonSuffix(getSplit())) {
                return switch (storageType.toLowerCase()) {
                    case "primary" -> new File(
                        Environment.getExternalStorageDirectory() + relativePath);
                    case "home" -> new File(
                        Environment.getExternalStorageDirectory() + File.separator
                            + Environment.DIRECTORY_DOCUMENTS + relativePath);
                    default -> new File(getStorageDirectory(), storageType + relativePath);
                };
            } else {
                return switch (storageType.toLowerCase()) {
                    case "primary" -> Environment.getExternalStorageDirectory();
                    case
                        "home" -> new File(Environment.getExternalStorageDirectory(),
                        Environment.DIRECTORY_DOCUMENTS);
                    default -> new File(getStorageDirectory(), storageType);
                };
            }
        }
        return new File(Environment.getExternalStorageDirectory(), getDocumentId());
    }

    @NonNull
    private File handleFileStorageAuth() {
        String path = Objects.requireNonNull(uri.getPath());
        return new File(path);
    }

    private File handleMediaStorageAuth() {
        final String type = getStorageType();

        Uri contentUri = null;

        if ("image".equals(type)) {
            contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        } else if ("video".equals(type)) {
            contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        } else if ("audio".equals(type)) {
            contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        } else if ("document".equals(type)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentUri = MediaStore.Files.getContentUri(MediaStore.getVolumeName(uri));
            }
        } else {
            // Catch the case for pdfs and other "document" files.
            contentUri = MediaStore.Files.getContentUri("external");
        }

        final String selection = BaseColumns._ID + "=?";
        final String[] selectionArgs = new String[]{getRelativePath()};

        return new File(Objects.requireNonNull(getDataColumn(contentUri, selection,
            selectionArgs)));
    }

    /**
     * Checks if the split document ID has a non-empty suffix after the colon.
     *
     * @param entry Split document ID array
     * @return boolean indicating if a non-empty suffix exists
     */
    private boolean hasColonSuffix(String[] entry) {
        return entry.length > 1 && !TextUtils.isEmpty(entry[1]);
    }

    public static class StorageVolumeAuthority {
        public static final String WHATSAPP = "com.whatsapp.provider.media";
        public static final String EXTERNAL = "com.android.externalstorage.documents";
        public static final String DOWNLOAD = "com.android.providers.downloads.documents";
        public static final String MEDIA = "com.android.providers.media.documents";
        public static final String GOOGLE_PHOTOS = "com.google.android.apps.photos.content";
        public static final String GOOGLE_DRIVE = "com.google.android.apps.docs.storage";
        public static final String GOOGLE_DRIVE_LEGACY =
            "com.google.android.apps.docs.storage" + ".legacy";
        public static final String CONTENT = ContentResolver.SCHEME_CONTENT;
        public static final String FILE = ContentResolver.SCHEME_FILE;

        public static Set<String> getAllowedAuthorities() {
            Set<String> authorities = new HashSet<>();
            authorities.add(EXTERNAL);
            authorities.add(DOWNLOAD);
            authorities.add(MEDIA);
            authorities.add(WHATSAPP);
            authorities.add(GOOGLE_PHOTOS);
            authorities.add(GOOGLE_DRIVE);
            authorities.add(GOOGLE_DRIVE_LEGACY);
            authorities.add(CONTENT);
            authorities.add(FILE);
            authorities.add(Constants.APP_PACKAGE_NAME);
            return authorities;
        }
    }
}
