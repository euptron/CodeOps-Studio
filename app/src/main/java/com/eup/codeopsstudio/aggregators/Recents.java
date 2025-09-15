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

package com.eup.codeopsstudio.aggregators;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.eup.codeopsstudio.common.Constants;
import com.eup.codeopsstudio.common.ILog;
import com.eup.codeopsstudio.domain.FileAction;
import com.eup.codeopsstudio.models.recents.Project;
import com.eup.codeopsstudio.models.recents.ProjectHistory;
import com.eup.codeopsstudio.util.FileTypeAdapter;
import com.eup.codeopsstudio.util.Wizard;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.jetbrains.annotations.Contract;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Manages a list of recently opened files and directories (projects).
 *
 * <p>This class handles the storage and retrieval of recent project information using
 * SharedPreferences. It serializes and deserializes project data using Gson.
 *
 * <p>Key functionalities include:
 * <ul>
 *     <li>Recording the creation or opening of files and folders.
 *     <li>Retrieving the list of recent projects, ensuring only valid and existing entries are
 *     returned.
 *     <li>Removing specific projects from the recents list.
 *     <li>Handling potential corruption in stored data by clearing invalid entries.
 * </ul>
 *
 * <p>Usage:
 * <pre>
 * Recents recents = Recents.initialize(context);
 * recents.recordFileOpening(new File("/path/to/your/file.txt"));
 * List<Project> recentProjects = recents.getRecentProjects();
 * // ... use recentProjects
 * </pre>
 *
 * @author Etido Peter
 */
public class Recents {

    public static final String TAG = "Recents";
    private final Context context;
    private final SharedPreferences sharedPreferences;

    private Recents(Context context) {
        this.context           = context.getApplicationContext();
        this.sharedPreferences =
            this.context.getSharedPreferences(Constants.SharedPreferenceKeys.KEY_RECENT_PROJECTS,
                Context.MODE_PRIVATE);
    }

    public static Recents initialize(@NonNull Context context) {
        return new Recents(context);
    }

    public Context getContext() {
        return context;
    }

    public void recordFolderCreation(@NonNull File file) {
        set(file, createHistory(FileAction.OPEN_FOLDER));
    }

    @NonNull
    @Contract("!null -> new")
    private ProjectHistory createHistory(FileAction action) {
        if (action == null) {
            var msg = "FileAction cannot be null when creating ProjectHistory";
            ILog.error(TAG, msg);
            throw new IllegalArgumentException(msg);
        }
        return new ProjectHistory(Wizard.getTime(), action);
    }

    private void set(@NonNull final File file, @NonNull final ProjectHistory history) {
        File projectFile = file;
        String path = projectFile.getPath();
        if (Wizard.isEmpty(path)) return;

        if (path.isEmpty()) {
            ILog.error(TAG,
                "Cannot record recent entry: File path is null or empty. File: " + projectFile);
            return;
        }

        if (!projectFile.isAbsolute()) {
            ILog.error(TAG,
                "Cannot record recent entry: File path is not absolute. File: " + projectFile.getPath());
            try {
                projectFile = projectFile.getAbsoluteFile();
            } catch (Exception e) {
                ILog.error(TAG, "Could not get absolute file for: " + file, e);
                return;
            }
            return;
        }

        Project newProject;
        try {
            String testPath = projectFile.getAbsolutePath();

            if (Wizard.isEmpty(testPath)) {
                ILog.error(TAG,
                    "Cannot record recent entry: File's absolute path is null or empty. File: "
                        + file);
                return;
            }
            newProject = new Project(projectFile, history);
        } catch (NullPointerException npe) {
            ILog.error(TAG,
                "Failed to create Project due to invalid file path details: " + projectFile, npe);
            return;
        } catch (IllegalArgumentException e) {
            ILog.error(TAG, "Failed to create Project for Recents: " + e.getMessage(), e);
            return;
        }

        List<Project> recents = getRecentProjectsInternal();
        recents.removeIf(current -> current.equals(newProject));
        recents.add(0, newProject);
        saveRecentProjects(recents);
    }

    private void saveRecentProjects(@NonNull List<Project> recents) {
        try {
            Gson gson = FileTypeAdapter.createFileAwareGson();

            String newJson = gson.toJson(recents);
            sharedPreferences
                .edit()
                .putString(Constants.SharedPreferenceKeys.KEY_RECENT_PROJECTS, newJson)
                .apply();
        } catch (Exception e) {
            ILog.error(TAG, "Error saving recent projects to JSON", e);
        }
    }

    @NonNull
    private ArrayList<Project> getRecentProjectsInternal() {
        String json =
            sharedPreferences.getString(Constants.SharedPreferenceKeys.KEY_RECENT_PROJECTS, "");

        if (json.isEmpty()) {
            return new ArrayList<>();
        }

        ArrayList<Project> loadedRecents;
        try {
            Gson gson = FileTypeAdapter.createFileAwareGson();
            TypeToken<ArrayList<Project>> typeToken = new TypeToken<>() { };
            loadedRecents = gson.fromJson(json, typeToken.getType());
        } catch (Exception e) {
            ILog.error(TAG, "Error parsing recent projects from JSON. Clearing recents.", e);
            clearAndSaveInvalidRecents(); // Clear corrupted data
            return new ArrayList<>();
        }

        if (loadedRecents == null) {
            ILog.warning(TAG, "Gson returned null for recent projects list. Returning empty list.");
            return new ArrayList<>();
        }
        return loadedRecents;
    }

    private void clearAndSaveInvalidRecents() {
        ILog.warning(TAG, "Clearing corrupted recent projects from SharedPreferences.");
        sharedPreferences
            .edit()
            .remove(Constants.SharedPreferenceKeys.KEY_RECENT_PROJECTS)
            .apply();
    }

    public void recordFileCreation(@NonNull File file) {
        set(file, createHistory(FileAction.CREATE_FILE));
    }

    public void recordFileOpening(@NonNull File file) {
        set(file, createHistory(FileAction.OPEN_FILE));
    }

    public void remove(@NonNull Project projectToRemove) {
        List<Project> recents = getRecentProjectsInternal();
        boolean removed = recents.removeIf(project -> project.equals(projectToRemove));

        if (removed) {
            saveRecentProjects(recents);
            ILog.debug(TAG, "Removed project from recents: " + projectToRemove.getPath());
        } else {
            ILog.debug(TAG,
                "Project not found in recents for removal: " + projectToRemove.getPath());
        }
    }

    public List<Project> getRecentProjects() {
        ArrayList<Project> loadedRecents = getRecentProjectsInternal();
        List<Project> validAndExistingRecents = new ArrayList<>();
        boolean listModified = false;

        for (Project p : loadedRecents) {
            if (p == null) {
                ILog.warning(TAG, "Skipping null project or project with null file in recents.");
                listModified = true;
                continue;
            }

            File projectFile = p.getFile();
            if (projectFile.isAbsolute() && projectFile.exists()) {
                validAndExistingRecents.add(p);
            } else {
                ILog.debug(TAG,
                    "Recent project does not exist or path is invalid, removing from " + "list:"
                        + " " + p.getPath());
                listModified = true;
            }
        }

        // If the list was modified due to non-existent or invalid projects, save the cleaned list
        if (listModified) {
            ILog.debug(TAG,
                "Cleaning up SharedPreferences from non-existent or invalid recent " + "projects.");
            saveRecentProjects(validAndExistingRecents);
        }

        return Collections.unmodifiableList(validAndExistingRecents);
    }

    public SharedPreferences getSharedPreferences() {
        return this.sharedPreferences;
    }
}