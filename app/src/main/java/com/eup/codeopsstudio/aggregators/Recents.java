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

package com.eup.codeopsstudio.aggregators;

import static com.eup.codeopsstudio.common.Constants.SharedPreferenceKeys;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import com.eup.codeopsstudio.file.FileAction;
import com.eup.codeopsstudio.models.recents.Project;
import com.eup.codeopsstudio.models.recents.ProjectHistory;
import com.eup.codeopsstudio.util.Wizard;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Aggregator for managing recently opened files and directories
 *
 * @author EUP
 */
public class Recents {

  private Context context;
  private SharedPreferences sharedPreferences;

  public static Recents initialize(@NonNull Context context) {
    return new Recents(context);
  }

  private Recents(Context context) {
    this.context = context;
    sharedPreferences =
        context.getSharedPreferences(
            SharedPreferenceKeys.KEY_RECENT_PROJECTS, Context.MODE_PRIVATE);
  }

  public void recordFolderCreation(File file) {
    set(file, new ProjectHistory(Wizard.getTime(), FileAction.OPEN_FOLDER));
  }

  public void recordFileCreation(File file) {
    set(file, new ProjectHistory(Wizard.getTime(), FileAction.CREATE_FILE));
  }

  public void recordFileOpening(File file) {
    set(file, new ProjectHistory(Wizard.getTime(), FileAction.OPEN_FILE));
  }

  private void set(File file, ProjectHistory history) {
    if (file == null || !file.exists()) return;

    Project newProject = new Project(file, history);
    List<Project> recents = getRecentProjects();

    for (Project curr : recents) {
      if (curr == null || curr.equals(newProject) || curr.equals(file.getPath())) {
        remove(curr);
      }
    }
    add(newProject);
  }

  private void add(@NonNull Project project) {
    List<Project> recents = getRecentProjects();
    recents.add(project);
    sharedPreferences
        .edit()
        .putString(SharedPreferenceKeys.KEY_RECENT_PROJECTS, new Gson().toJson(recents))
        .apply();
  }

  public void remove(@NonNull Project project) {
    List<Project> recents = getRecentProjects();
    recents.removeIf(
        currentProject -> {
          return currentProject == null || currentProject.equals(project);
        });
    sharedPreferences
        .edit()
        .putString(SharedPreferenceKeys.KEY_RECENT_PROJECTS, new Gson().toJson(recents))
        .apply();
  }

  public List<Project> getRecentProjects() {
    var json = sharedPreferences.getString(SharedPreferenceKeys.KEY_RECENT_PROJECTS, "");
    if (json == null || json.isEmpty()) return new ArrayList<>();

    var type = new TypeToken<ArrayList<Project>>() {}.getType();
    ArrayList<Project> recentProjects = new Gson().fromJson(json, type);

    recentProjects.removeIf(
        project -> {
          var file = project.getFile();
          return file == null || !file.exists();
        });

    if (recentProjects != null && !recentProjects.isEmpty()) return recentProjects;

    return new ArrayList<Project>();
  }

  public SharedPreferences getSharedPreferences() {
    return this.sharedPreferences;
  }
}
