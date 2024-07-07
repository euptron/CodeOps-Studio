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
 
   package com.eup.codeopsstudio.observers;

import static com.eup.codeopsstudio.common.Constants.SharedPreferenceKeys;
import static com.eup.codeopsstudio.common.models.Document.MimeType.*;
import static com.eup.codeopsstudio.ui.editor.panes.recent.model.Project.History;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.ActivityResultRegistry;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.activity.result.contract.ActivityResultContracts.GetContent;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.documentfile.provider.DocumentFile;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.eup.codeopsstudio.common.util.FileUtil;
import com.eup.codeopsstudio.common.util.PreferencesUtils;
import com.eup.codeopsstudio.file.FileAction;
import com.eup.codeopsstudio.listeners.FileActionListener;
import com.eup.codeopsstudio.ui.editor.panes.recent.model.Project;
import com.eup.codeopsstudio.util.Wizard;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ContextualLifecycleObserver implements DefaultLifecycleObserver {

  private FileActionListener mFileActionListener;
  private Context obCtx;
  private final ActivityResultRegistry mRegistry;
  private ActivityResultLauncher<String> mPickFile;
  private ActivityResultLauncher<String> mPickArchiveFile;
  private ActivityResultLauncher<String> mCreateFile;
  private ActivityResultLauncher<Intent> mPickFolder;
  private SharedPreferences sharedPreferences;
  private List<Project> projects = new ArrayList<>();

  public ContextualLifecycleObserver(
      @NonNull Context context, @NonNull ActivityResultRegistry registry) {
    obCtx = context;
    mRegistry = registry;
  }

  @Override
  public void onCreate(@NonNull LifecycleOwner owner) {
    sharedPreferences =
        obCtx.getSharedPreferences(SharedPreferenceKeys.KEY_RECENT_PROJECTS, Context.MODE_PRIVATE);
    projects = getRecentProjects();

    mPickFile =
        mRegistry.register(
            "select_file",
            owner,
            new GetContent(),
            new ActivityResultCallback<Uri>() {
              @Override
              public void onActivityResult(@Nullable Uri uri) {
                try {
                 if (uri != null && mFileActionListener != null) {
                   var mPickedFile = new File(FileUtil.getPathFromUri(obCtx, uri));
                   var history = new History(Wizard.getTime(), FileAction.OPEN_FILE);
                   if (mPickedFile != null && history != null) {
                     addToRecentOrReplace(mPickedFile, history);
                     mFileActionListener.onFilePicked(mPickedFile);
                    }
                  }
                } catch (Exception e) {
                  mFileActionListener.onActionFailed(e.getMessage());
                }
              }
            });

    mPickArchiveFile =
        mRegistry.register(
            "select_zip_file",
            owner,
            new GetContent(),
            new ActivityResultCallback<Uri>() {
              @Override
              public void onActivityResult(@Nullable Uri uri) {
                if (uri != null && mFileActionListener != null) {
                  mFileActionListener.onFilePicked(uri);
                }
              }
            });

    mCreateFile =
        mRegistry.register(
            "create_file",
            owner,
            new ActivityResultContracts.CreateDocument(ALL.toString()),
            new ActivityResultCallback<Uri>() {
              @Override
              public void onActivityResult(@Nullable Uri uri) {
                try {
                  if (uri != null && mFileActionListener != null) {
                    var mCreatedFile = new File(FileUtil.getPathFromUri(obCtx, uri));
                    var history = new History(Wizard.getTime(), FileAction.CREATE_FILE);
                    if (mCreatedFile != null && history != null) {
                      addToRecentOrReplace(mCreatedFile, history);
                      mFileActionListener.onCreateFile(mCreatedFile);
                    }
                  }
                } catch (Exception e) {
                  mFileActionListener.onActionFailed(e.getMessage());
                }
              }
            });

    mPickFolder =
        mRegistry.register(
            "select_folder",
            owner,
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
              @Override
              public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == Activity.RESULT_OK) {
                  Intent intent = result.getData();
                  if (intent != null) {
                    Uri uri = intent.getData();
                    if (uri != null && mFileActionListener != null) {
                      try {
                        var pickedDir = DocumentFile.fromTreeUri(obCtx, uri);
                        var mFile = new File(FileUtil.getPathFromUri(obCtx, pickedDir.getUri()));

                        if (mFile != null) {
                          createHistory(mFile);
                          mFileActionListener.onFolderPicked(mFile);
                        }
                      } catch (Exception e) {
                        mFileActionListener.onActionFailed(e.getMessage());
                      }
                    }
                  }
                }
              }
            });
  }

  public void setFileActionListener(FileActionListener fileActionListener) {
    mFileActionListener = fileActionListener;
  }

  public void pickFile() {
    // Open the activity to select a file
    mPickFile.launch(ALL.toString());
  }

  public void pickArchiveFile() {
    // Open the activity to select an archive file
    mPickArchiveFile.launch(ALL.toString());
  }

  public void pickFolder() {
    // Open the activity to select a folder
    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
    mPickFolder.launch(intent);
  }

  /**
   * Create a new file
   *
   * @param name The file name
   */
  public void createFile(String name) {
    mCreateFile.launch(name);
  }

  // ..//

  private void createHistory(File file) {
    History history = new History(Wizard.getTime(), FileAction.OPEN_FOLDER);
    addToRecentOrReplace(file, history);
  }

  private void addToRecentOrReplace(File file, History history) {
    Project newProject = new Project(file, history);
    for (Project existingProject : projects) {
      if (existingProject.equals(newProject) || existingProject.getFile().getPath().equals(file.getPath())) {
        removeFromRecent(existingProject);
        break;
      }
    }
    addToRecent(newProject);
  }

  public void addToRecent(@NonNull Project project) {
    List<Project> projects = getRecentProjects();
    projects.add(project);
    sharedPreferences
        .edit()
        .putString(SharedPreferenceKeys.KEY_RECENT_PROJECTS, new Gson().toJson(projects))
        .apply();
  }

  public void removeFromRecent(@NonNull Project project) {
    List<Project> recents = getRecentProjects();
    recents.removeIf(currentProject -> currentProject.equals(project));
    sharedPreferences
        .edit()
        .putString(SharedPreferenceKeys.KEY_RECENT_PROJECTS, new Gson().toJson(recents))
        .apply();
  }

  public ArrayList<Project> getRecentProjects() {
    var json = sharedPreferences.getString(SharedPreferenceKeys.KEY_RECENT_PROJECTS, "");
    ArrayList<Project> recentProjects = new Gson().fromJson(json, new TypeToken<ArrayList<Project>>() {}.getType());
    if (recentProjects != null && !recentProjects.isEmpty()) {
      return recentProjects;
    }
    return new ArrayList<Project>();
  }
}
