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

import com.eup.codeopsstudio.domain.FileAction;

import java.util.Objects;

/**
 * Model class for projects
 *
 * @author Etido Peter
 */
public class ProjectHistory {

    public final long creationDate;
    /**
     * The action performed on the file, represented by the {@link FileAction} enum.
     * This indicates whether the file was created, opened, or modified.
     */
    public final FileAction fileAction;

    public ProjectHistory(long date, @NonNull FileAction act) {
        Objects.requireNonNull(act, "FileAction cannot be null for ProjectHistory");
        this.creationDate = date;
        this.fileAction   = act;
    }

    @Override
    public int hashCode() {
        return Objects.hash(creationDate, fileAction);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProjectHistory that = (ProjectHistory) o;
        return creationDate == that.creationDate && fileAction == that.fileAction;
    }

    @NonNull
    @Override
    public String toString() {
        return "ProjectHistory{" + "creationDate=" + creationDate + ", fileAction=" + fileAction
            + '}';
    }
}
