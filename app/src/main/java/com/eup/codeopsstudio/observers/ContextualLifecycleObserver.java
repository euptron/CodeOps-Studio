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

package com.eup.codeopsstudio.observers;

import static com.eup.codeopsstudio.common.models.Document.MimeType.*;

import android.content.Context;
import android.net.Uri;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.ActivityResultRegistry;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import com.eup.codeopsstudio.aggregators.Recents;
import com.eup.codeopsstudio.common.util.FileUriMediator;
import com.eup.codeopsstudio.listeners.FileActionListener;

public class ContextualLifecycleObserver implements DefaultLifecycleObserver {

  private Context context;
  private FileActionListener fileActionListener;
  private final ActivityResultRegistry resultRegistry;
  private ActivityResultLauncher<String> pickFileLauncher, createFileLauncher;
  private ActivityResultLauncher<Uri> pickFolderLauncher;
  private Recents recentProjects;

  public static final String KEY_PICK_DIRECTORY = "pick_folder_key";
  public static final String KEY_CREATE_FILE = "create_file";
  public static final String KEY_PICK_FILE = "pick_file";

  public ContextualLifecycleObserver(
      @NonNull Context context,
      @NonNull ActivityResultRegistry registry,
      @NonNull FileActionListener fileActionListener) {
    this.context = context;
    this.resultRegistry = registry;
    this.fileActionListener = fileActionListener;

    if (fileActionListener == null) {
      throw new IllegalArgumentException(
          "ContextualLifeCycleObserver: FileActionListener is invalid");
    }
  }

  @Override
  public void onCreate(@NonNull LifecycleOwner owner) {
    recentProjects = Recents.initialize(context);

    pickFileLauncher =
        resultRegistry.register(
            KEY_PICK_FILE, owner, new ActivityResultContracts.GetContent(), this::mediateFileUri);

    createFileLauncher =
        resultRegistry.register(
            KEY_CREATE_FILE,
            owner,
            new ActivityResultContracts.CreateDocument(ALL.toString()),
            this::mediateFileCreation);

    pickFolderLauncher =
        resultRegistry.register(
            KEY_PICK_DIRECTORY,
            owner,
            new ActivityResultContracts.OpenDocumentTree(),
            this::mediateFolderUri);
  }

  public void pickFile() {
    pickFileLauncher.launch(ALL.toString());
  }

  public void pickZipFile() {
    pickFileLauncher.launch(ZIP.toString());
  }

  public void pickFolder() {
    pickFolderLauncher.launch(/*null to open root=*/ null);
  }

  /**
   * Creates a new file
   *
   * @param name The file name
   */
  public void createFile(String name) {
    createFileLauncher.launch(name);
  }

  private void mediateFolderUri(Uri uri) {
    if (uri == null) {
      toast("Invalid folder selection. Please try again.");
      return;
    }

    FileUriMediator mediator = FileUriMediator.resolveTree(uri, context);

    if (!mediator.isAllowedAuthority(mediator.getAuthority())) {
      toast("The selected folder cannot be accessed due to unsupported authority.");
      return;
    }

    try {
      var pickedFolder = mediator.getFile();

      if (pickedFolder == null || !pickedFolder.exists() || !pickedFolder.isDirectory()) {
        toast("The selected folder either does not exist or is not a valid directory.");
        return;
      }

      recentProjects.recordFolderCreation(pickedFolder);
      fileActionListener.onFolderPicked(pickedFolder);
    } catch (Exception e) {
      fileActionListener.onActionFailed(
          String.format("Error: %s - %s", e.getClass().getSimpleName(), e.getMessage()));
    }
  }

  private void mediateFileCreation(Uri uri) {
    if (uri == null) {
      toast("Invalid file selection. Please try again.");
      return;
    }

    var mediator = FileUriMediator.resolveDocument(uri, context);

    try {
      var createdFile = mediator.getFile();

      if (createdFile == null || !createdFile.exists() || !createdFile.isFile()) {
        toast("The newly created file either does not exist or is invalid.");
        return;
      }

      recentProjects.recordFileCreation(createdFile);
      fileActionListener.onCreateFile(createdFile);
    } catch (Exception e) {
      fileActionListener.onActionFailed(
          String.format("Error: %s - %s", e.getClass().getSimpleName(), e.getMessage()));
    }
  }

  private void mediateFileUri(Uri uri) {
    if (uri == null) {
      toast("Invalid file selection. Please try again.");
      return;
    }

    FileUriMediator mediator = FileUriMediator.resolveDocument(uri, context);

    if (!mediator.isAllowedAuthority(mediator.getAuthority())) {
      toast("The selected file cannot be accessed due to unsupported authority.");
      return;
    }

    try {
      var pickedFile = mediator.getFile();

      if (pickedFile == null || !pickedFile.exists() || !pickedFile.isFile()) {
        toast("The selected file either does not exist or is not a valid file.");
        return;
      }

      recentProjects.recordFileOpening(pickedFile);
      fileActionListener.onFilePicked(pickedFile);
    } catch (Exception e) {
      fileActionListener.onActionFailed(
          String.format("Error: %s - %s", e.getClass().getSimpleName(), e.getMessage()));
    }
  }

  void toast(String msg) {
    Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
  }
}
