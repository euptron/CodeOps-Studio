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

package com.eup.codeopsstudio.common.models;

import android.content.Context;
import android.net.Uri;
import android.provider.DocumentsContract;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.documentfile.provider.DocumentFile;

import com.eup.codeopsstudio.common.ILog;
import com.eup.codeopsstudio.common.util.FileUriMediator;
import com.eup.codeopsstudio.common.util.FileUtil;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * A hybrid class that bridges the gap between traditional File objects and SAF DocumentFile
 * objects.
 *
 * @author Etido Peter
 */
public class MetaDocument {

    public static final String TAG = "MetaDocument";
    private static final List<UUID> generatedIds = new ArrayList<>();

    private Uri uri;
    private UUID identity;
    private String name, mimeType;
    private long lastModified, length;
    private boolean exists, canWrite, canRead, isVirtual, isDirectory, isFile;

    /**
     * TRADITIONAL FILE SYSTEM REQUIREMENTS
     *
     * <p>{@code File}
     */
    private File mFile;

    /**
     * SAF FILE SYSTEM REQUIREMENTS
     *
     * <p>{@code Context} and {@code DocumentFile}
     */
    private DocumentFile mDocumentFile;

    private Context mContext;

    private MetaDocument() {
        // default
    }

    public MetaDocument(File file) {
        this(file, true);
    }

    public MetaDocument(File file, boolean generateId) {
        this.mFile = file;
        setName(file.getName());
        setLastModified(file.lastModified());
        setLength(file.length());
        setExists(file.exists());
        setWriteAble(file.canWrite());
        setReadAble(file.canRead());
        setIsVirtual(false);
        setIsDirectory(file.isDirectory());
        setIsFile(file.isFile());
        final DocumentFile documentFile = DocumentFile.fromFile(file);
        setUri(documentFile.getUri());
        setMIME(documentFile.getType());
        if (generateId) setID(generateUUID());
    }

    protected MetaDocument setMIME(String type) {
        this.mimeType = type;
        return this;
    }

    protected MetaDocument setLastModified(long time) {
        this.lastModified = time;
        return this;
    }

    protected MetaDocument setLength(long length) {
        this.length = length;
        return this;
    }

    protected MetaDocument setExists(boolean exists) {
        this.exists = exists;
        return this;
    }

    protected MetaDocument setWriteAble(boolean canWrite) {
        this.canWrite = canWrite;
        return this;
    }

    protected MetaDocument setReadAble(boolean canRead) {
        this.canRead = canRead;
        return this;
    }

    protected MetaDocument setIsVirtual(boolean isVirtual) {
        this.isVirtual = isVirtual;
        return this;
    }

    protected MetaDocument setIsDirectory(boolean isDirectory) {
        this.isDirectory = isDirectory;
        return this;
    }

    protected MetaDocument setIsFile(boolean isFile) {
        this.isFile = isFile;
        return this;
    }

    private UUID generateUUID() {
        UUID generatedId;
        do {
            generatedId = UUID.randomUUID();
        } while (generatedIds.contains(generatedId));
        generatedIds.add(generatedId);
        return generatedId;
    }

    public MetaDocument(DocumentFile documentFile, Context context) {
        this(documentFile, context, true);
    }

    public MetaDocument(DocumentFile documentFile, Context context, boolean generateId) {
        this.mDocumentFile = documentFile;
        this.mContext      = context;
        setName(documentFile.getName());
        setLastModified(documentFile.lastModified());
        setLength(documentFile.length());
        setExists(documentFile.exists());
        setWriteAble(documentFile.canWrite());
        setReadAble(documentFile.canRead());
        setIsVirtual(documentFile.isVirtual());
        setIsDirectory(documentFile.isDirectory());
        setIsFile(documentFile.isFile());
        setUri(documentFile.getUri());
        setMIME(documentFile.getType());
        if (generateId) setID(generateUUID());
    }

    /**
     * Copy constructor
     *
     * @param other the document to clone
     */
    public MetaDocument(MetaDocument other) {
        if (other.isFileBased()) {
            this.mFile = other.mFile;
        } else if (other.isDocumentBased()) {
            this.mDocumentFile = other.mDocumentFile;
            this.mContext      = other.mContext;
        }

        setName(other.getName());
        setLastModified(other.lastModified());
        setLength(other.length());
        setExists(other.exists());
        setWriteAble(other.canWrite());
        setReadAble(other.canRead());
        setIsVirtual(other.isVirtual());
        setIsDirectory(other.isDirectory());
        setIsFile(other.isFile());
        setUri(other.getUri());
        setMIME(other.getMIMEType());
        setID(other.getID());
    }

    public String getName() {
        return name;
    }

    protected MetaDocument setName(String name) {
        this.name = name;
        return this;
    }

    public boolean isFile() {
        return isFile;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public boolean isVirtual() {
        return isVirtual;
    }

    public long lastModified() {
        return lastModified;
    }

    public boolean canRead() {
        return canRead;
    }

    public boolean canWrite() {
        return canWrite;
    }

    public boolean exists() {
        return exists;
    }

    public long length() {
        return length;
    }

    @Nullable
    public Uri getUri() {
        return uri;
    }

    protected MetaDocument setUri(Uri uri) {
        this.uri = uri;
        return this;
    }

    public String getMIMEType() {
        return mimeType;
    }

    public UUID getID() {
        return this.identity;
    }

    public MetaDocument setID(UUID id) {
        this.identity = id;
        return this;
    }

    public boolean isFileBased() {
        return mFile != null;
    }

    public boolean isDocumentBased() {
        return mDocumentFile != null;
    }

    public String getDocumentId(Context context) {
        if (DocumentsContract.isDocumentUri(context, uri)) {
            return DocumentsContract.getDocumentId(uri);
        } else {
            return DocumentsContract.getTreeDocumentId(uri);
        }
    }

    @Nullable
    public String getPath() {
        if (mFile != null) {
            return mFile.getAbsolutePath();
        } else if (mDocumentFile != null) return buildVirtualPath();
        return null;
    }

    private String buildVirtualPath() {
        if (mContext == null || mDocumentFile == null) return null;

        String path = null;
        FileUriMediator mediator = null;
        Uri uri = mDocumentFile.getUri();

        if (DocumentsContract.isDocumentUri(mContext, uri)) {
            mediator = FileUriMediator.resolveDocument(uri, mContext);
        } else if (DocumentsContract.isTreeUri(uri)) {
            mediator = FileUriMediator.resolveTree(uri, mContext);
        }

        if (mediator != null) {
            path = mediator.getRelativePath();
        }

        return (path != null || !path.isEmpty()) ? path : uri.getPath();
    }

    @Nullable
    public String getAuthority() {
        if (mDocumentFile != null) {
            return mDocumentFile
                .getUri()
                .getAuthority();
        }
        return null;
    }

    public DocumentFile getFile() {
        return mDocumentFile;
    }

    public List<MetaDocument> listFiles() {
        List<MetaDocument> files = new ArrayList<>();

        if (mFile != null && mFile.isDirectory()) {
            File[] fileList = mFile.listFiles();
            if (fileList != null) {
                for (File f : fileList) {
                    files.add(new MetaDocument(f));
                }
            }
        } else if (mDocumentFile != null && mDocumentFile.isDirectory()) {
            for (DocumentFile docFile : mDocumentFile.listFiles()) {
                files.add(new MetaDocument(docFile, mContext));
            }
        }

        return files;
    }

    /**
     * Create a new directory as a direct child of this directory.
     *
     * @param name name of new directory
     * @return true if successful otherwise false
     * @throws UnsupportedOperationException when working with a single document created from {@link
     *                                       DocumentFile#fromSingleUri(Context, Uri)}.
     * @throws SecurityException             If a security manager exists and its <code>{@link
     *                                       <p>
     *                                       <p>
     *                                       <p>
     *                                       java.lang.SecurityManager#checkWrite(java.lang.String)}
     *                                       </code> method does not permit
     *                                       the named directory to be created
     * @see androidx.documentfile.provider.DocumentFile#createDirectory(String)
     * @see File#mkdir()
     */
    public boolean createDirectory(String name) {
        if (mFile != null) {
            try {
                File newDir = new File(mFile, name);
                return newDir.mkdir();
            } catch (SecurityException ignored) {
                return false;
            }
        } else if (mDocumentFile != null) {
            try {
                DocumentFile newDir = mDocumentFile.createDirectory(name);
                return newDir != null;
            } catch (UnsupportedOperationException ignored) {
                return false;
            }
        }
        return false;
    }

    /**
     * Deletes this document.
     *
     * @throws SecurityException If the document is File and security manager exists and its <code>
     *                           {@link
     *                           java.lang.SecurityManager#checkDelete}</code> method denies
     *                           delete access to the file
     * @see org.apache.commons.io.FileUtils#deleteQuietly(File)
     */
    public boolean delete() {
        if (mFile != null) {
            return FileUtils.deleteQuietly(mFile);
        } else if (mDocumentFile != null) return mDocumentFile.delete();
        return false;
    }

    /**
     * Renames the file/directory to {@code newName}.
     *
     * @return true if rename was successful
     */
    public boolean renameTo(String newName) {
        if (mFile != null) {
            try {
                boolean success = FileUtil.rename(mFile, newName);
                if (success) mFile = new File(mFile.getParent(), newName);
                return success;
            } catch (SecurityException | NullPointerException e) {
                ILog.error(TAG, e.getMessage());
                return false;
            }
        } else if (mDocumentFile != null) {
            try {
                return renameTo(mDocumentFile, newName);
            } catch (UnsupportedOperationException e) {
                ILog.error(TAG, e.getMessage());
                return false;
            }
        }
        return false;
    }

    /**
     * Renames an existing document, handling case conflicts and provider-specific behavior.
     *
     * <p>Case-only renames (e.g., "eup" → "Etido Peter") may require multiple attempts to
     * resolve filesystem
     * conflicts ("eup" → "eup (1)" → Etido Peter). Providers might create a new document with
     * updated URI and
     * MIME type; callers must recheck {@link #getUri()} and {@link DocumentFile#getType()} post-rename.
     *
     * <p>After renaming directories, reload document lists as {@link DocumentFile#listFiles()} may
     * return stale entries.
     *
     * @param documentFile Document to rename
     * @param displayName  New case-sensitive name (must differ from current)
     * @return {@code true} if successful, {@code false} otherwise
     */
    private boolean renameTo(DocumentFile documentFile, String displayName) {
        if (documentFile == null || !documentFile.exists()) {
            ILog.error(TAG, documentFile == null ? "Null document" : "Document missing");
            return false;
        }

        final String currentName = documentFile.getName();
        if (displayName == null || displayName.equals(currentName)) {
            ILog.error(TAG, "Invalid name or identical rename");
            return false;
        }

        if (currentName.equalsIgnoreCase(displayName)) {
            boolean success = documentFile.renameTo(displayName);
            if (success && !displayName.equals(documentFile.getName())) {
                success = documentFile.renameTo(displayName);
                return success;
            }
        }

        // Standard rename for different names
        return documentFile.renameTo(displayName);
    }

    /**
     * Resolves a child file/directory by name.
     *
     * @return the resolved document
     */
    @Nullable
    public MetaDocument resolve(String name) {
        if (mFile != null) {
            return new MetaDocument(new File(mFile, name));
        } else if (mDocumentFile != null) {
            DocumentFile child = mDocumentFile.findFile(name);
            if (child != null) return new MetaDocument(child, mContext);
        }
        return null;
    }

    @Nullable
    public File toFile() {
        if (mFile != null) return mFile;
        try {
            FileUriMediator mediator = resolveUriMediator();
            return (mediator != null) ? mediator.getFile() : null;
        } catch (Exception e) {
            ILog.error(TAG, "Cannot convert DocumentFile to File: " + e.getMessage());
        }
        return null;
    }

    private FileUriMediator resolveUriMediator() {
        if (mContext != null && uri != null) {
            if (DocumentsContract.isDocumentUri(mContext, uri)) {
                return FileUriMediator.resolveDocument(uri, mContext);
            } else if (DocumentsContract.isTreeUri(uri)) {
                return FileUriMediator.resolveTree(uri, mContext);
            }
        }
        return null;
    }

    @Nullable
    public DocumentFile toDocumentFile() {
        if (mDocumentFile != null) return mDocumentFile;
        if (mFile != null) {
            return DocumentFile.fromFile(mFile);
        }
        return null;
    }

    @Override
    public int hashCode() {
        if (isFileBased()) {
            return Objects.hash(mFile);
        } else if (isDocumentBased()) return Objects.hash(mDocumentFile.getUri());

        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof MetaDocument other)) return false;

        if (isFileBased() && other.isFileBased()) {
            return Objects.equals(mFile, other.mFile);
        } else if (isDocumentBased() && other.isDocumentBased()) {
            return Objects.equals(mDocumentFile.getUri(), other.mDocumentFile.getUri());
        }

        return false;
    }

    @NonNull
    @Override
    public String toString() {
        if (isFileBased()) {
            return "FileMetaDocument{path='" + mFile.getAbsolutePath() + "'}";
        } else if (isDocumentBased()) {
            return "DocumentMetaDocument{uri='" + mDocumentFile.getUri() + "'}";
        }
        return "InvalidMetaDocument";
    }

    public enum MimeType {
        /**
         * Image files only
         */
        IMAGE("image/*"),

        /**
         * Text files only
         */
        TEXT("text/*"),

        /**
         * Audio files only
         */
        AUDIO("audio/*"),

        /**
         * Video files only
         */
        VIDEO("video/*"),

        /**
         * Zip files only
         */
        ZIP("application/zip"),

        /**
         * All files
         */
        ALL("*/*");

        private final String mType;

        MimeType(String type) {
            mType = type;
        }

        @Override
        public String toString() {
            return mType;
        }
    }
}
