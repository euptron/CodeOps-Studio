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
 
   package com.eup.codeopsstudio.ui.editor.panes.recent.model;

import com.eup.codeopsstudio.file.FileAction;
import java.io.File;
import java.util.Objects;

/**
 * Stores information about a recent project
 *
 * @author EUP
 */
public class Project {

  private final File file;
  private boolean isBookmarked;
  private History history;

  public Project(File file, History history) {
    this.file = file;
    this.history = history;
  }

  public String getName() {
    return this.file.getName();
  }

  public File getFile() {
    return this.file;
  }

  public String getPath() {
    return this.file.getAbsolutePath();
  }

  public boolean isBookMarked() {
    return this.isBookmarked;
  }

  public void setBookMarked(boolean enabled) {
    isBookmarked = enabled;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Project project = (Project) o;
    return Objects.equals(getName(), project.getName()) && Objects.equals(file, project.file);
  }

  @Override
  public int hashCode() {
    return Objects.hash(getName(), file);
  }
  
  public History getHistory() {
    return this.history;
  }

  public static final class History {

    public final long creationDate;
    public final FileAction fileAction;

    public History(long date, FileAction act) {
      this.creationDate = date;
      this.fileAction = act;
    }
  }
}
