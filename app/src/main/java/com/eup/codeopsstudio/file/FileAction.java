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

package com.eup.codeopsstudio.file;

/**
 * Enumeration of actions performed on file
 *
 * @author EUP
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

  @Override
  public String toString() {
    return actionDescription;
  }
}
