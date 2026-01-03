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

package com.eup.codeopsstudio.ui.recents.models;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.eup.codeopsstudio.common.ILog;

import java.io.File;
import java.util.Objects;

/**
 * Represents a recent project in the CodeOps Studio application.
 * This class encapsulates information about a project, including its file location,
 * bookmark status, and associated history.
 *
 * <p>A {@code Project} is identified by its file path. Two {@code Project} objects are considered
 * equal if their file paths are the same.
 *
 * <p>The project's history, if available, is represented by a {@link ProjectHistory} object.
 * Projects can be bookmarked for quick access.
 *
 * @author Etido Peter
 * @see ProjectHistory
 */
public class Project {

    private static final String TAG = "Project";
    private final File file;
    private final ProjectHistory history;
    private boolean isBookmarked;

    public Project(@NonNull File file, @NonNull ProjectHistory history) {
        this(file, false, history);
    }

    public Project(@NonNull File file, boolean isBookmarked, @NonNull ProjectHistory history) {
        Objects.requireNonNull(file, "File cannot be null for a Project");
        Objects.requireNonNull(history, "ProjectHistory cannot be null for a Project");
        this.file         = file;
        this.isBookmarked = isBookmarked;
        this.history      = history;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPath());
    }

    @NonNull
    public String getPath() {
        if (file == null) {
            ILog.error(TAG, "Project file reference is null, returning empty path");
            return "";
        }
        return this.file.getAbsolutePath();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        var project = (Project) o;

        return Objects.equals(this.getPath(), project.getPath());
    }

    @NonNull
    @Override
    public String toString() {
        return "Project{" + "file=" + (file != null ? getPath() : "null_file_ref") + ", "
            + "isBookmarked=" + isBookmarked + ", history=" + history + '}';
    }

    public boolean exists() {
        try {
            return this.file.exists();
        } catch (SecurityException se) {
            ILog.error(TAG,
                "SecurityException while checking if file exists: " + this.file.getPath(), se);
            return false;
        }
    }

    @NonNull
    public File getFile() {
        return this.file;
    }

    @Nullable
    public ProjectHistory getHistory() {
        return this.history;
    }

    public long getLastModified() {
        return file.lastModified();
    }

    public String getName() {
        return this.file.getName();
    }

    public boolean isBookMarked() {
        return this.isBookmarked;
    }

    public void setBookMarked(boolean enabled) {
        isBookmarked = enabled;
    }

    public boolean isDirectory() {
        return this.file.isDirectory();
    }

    public boolean isFile() {
        return this.file.isFile();
    }
}