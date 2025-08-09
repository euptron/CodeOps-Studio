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

package com.eup.codeopsstudio.domain;

import androidx.annotation.NonNull;

/**
 * Enumeration of actions performed on file
 *
 * @author Etido Peter
 */
public enum FileAction {
    CREATE_FILE("Create File"),
    CREATE_FOLDER("Create Folder"),
    DELETE_FILE("Delete File"),
    DELETE_FOLDER("Delete Folder"),
    RENAME_FILE("Rename File"),
    RENAME_FOLDER("Rename Folder"),
    OPEN_FOLDER("Open Folder"),
    OPEN_FILE("Open File");

    private final String actionDescription;

    FileAction(String actionDescription) {
        this.actionDescription = actionDescription;
    }

    @NonNull
    @Override
    public String toString() {
        return actionDescription;
    }
}
