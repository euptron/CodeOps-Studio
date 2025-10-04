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

package com.eup.codeopsstudio.observers;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.ActivityResultRegistry;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;

import com.eup.codeopsstudio.R;
import com.eup.codeopsstudio.aggregators.Recents;
import com.eup.codeopsstudio.common.Constants;
import com.eup.codeopsstudio.common.ILog;
import com.eup.codeopsstudio.common.models.MetaDocument;
import com.eup.codeopsstudio.common.util.FileUriMediator;
import com.eup.codeopsstudio.common.util.PreferencesUtils;
import com.eup.codeopsstudio.common.util.UriUtils;
import com.eup.codeopsstudio.viewmodel.FileViewModel;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

/**
 * Lifecycle observer to monitor file
 *
 * @author Etido Peter
 */
public class ContextualLifecycleObserver implements DefaultLifecycleObserver {

    public static final String KEY_PICK_DIRECTORY = "ide.pick.folder";
    public static final String KEY_CREATE_FILE = "ide.create.file";
    public static final String KEY_PICK_FILE = "ide.pick.file";
    public static final String KEY_REQUEST_DIR_PERMISSION = "ide.request.dir.permission";
    public static final String KEY_SAVE_DOCUMENT_AS = "ide.save.as";
    public static final String EXTRA_SAVE_AS = KEY_SAVE_DOCUMENT_AS + "intent.extra.save.as";
    public static final String TAG = ContextualLifecycleObserver.class.getSimpleName();
    private final Context context;
    private final FragmentActivity activity;
    private final ActivityResultRegistry resultRegistry;
    private FileViewModel fileViewModel;
    private ActivityResultLauncher<String> pickFileLauncher;
    private ActivityResultLauncher<String> createFileLauncher;
    private ActivityResultLauncher<Intent> saveFileAsLauncher;
    private ActivityResultLauncher<Uri> pickFolderLauncher;
    private ActivityResultLauncher<Uri> requestFolderPermissionLauncher;
    private Recents recentProjects;

    public ContextualLifecycleObserver(@NonNull Context context,
        @NonNull ActivityResultRegistry registry, @NonNull FragmentActivity activity) {

        this.context        = Objects.requireNonNull(context, "Context must not be null");
        this.resultRegistry = Objects.requireNonNull(registry,
            "ActivityResultRegistry must not " + "be null");
        this.activity       = Objects.requireNonNull(activity, "FragmentActivity must not be null");
    }

    @Override
    public void onCreate(@NonNull LifecycleOwner owner) {
        fileViewModel  = new ViewModelProvider(activity).get(FileViewModel.class);
        recentProjects = Recents.initialize(context);

        pickFileLauncher = resultRegistry.register(KEY_PICK_FILE, owner,
            new ActivityResultContracts.GetContent(), this::mediateFileUri);

        createFileLauncher = resultRegistry.register(KEY_CREATE_FILE, owner,
            new ActivityResultContracts.CreateDocument(MetaDocument.MimeType.ALL.toString()),
            this::mediateFileCreation);

        pickFolderLauncher = resultRegistry.register(KEY_PICK_DIRECTORY, owner,
            new ActivityResultContracts.OpenDocumentTree(), this::mediateFolderUri);

        requestFolderPermissionLauncher = resultRegistry.register(KEY_REQUEST_DIR_PERMISSION,
            owner, new ActivityResultContracts.OpenDocumentTree(), uri -> {
            if (uri != null) {
                int flags =
                    Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
                UriUtils.takePersistableUriPermission(context, uri, flags);
            } else {
                toast(context.getString(R.string.msg_no_directory_selected));
            }
        });

        saveFileAsLauncher = resultRegistry.register(KEY_SAVE_DOCUMENT_AS, owner,
            new ActivityResultContracts.StartActivityForResult(), this::performSaveAs);
    }

    public void createFile(String name) {
        try {
            createFileLauncher.launch(name);
        } catch (ActivityNotFoundException e) {
            String errorMsg = context.getString(R.string.msg_create_file_failed, e.toString());
            notifyError(new ActivityNotFoundException(errorMsg));
        }
    }

    public void pickFile() {
        launchFilePicker(MetaDocument.MimeType.ALL.toString());
    }

    public void launchFilePicker(String mimeType) {
        try {
            pickFileLauncher.launch(mimeType);
        } catch (ActivityNotFoundException e) {
            String errorMsg = context.getString(R.string.msg_file_selection_failed_no_act);
            notifyError(new ActivityNotFoundException(errorMsg));
        }
    }

    private void notifyError(Exception e) {
        var message = String.format("Error: %s - %s", e.getClass().getSimpleName(), e.getMessage());
        toast(message);
        fileViewModel.setMonitorMessage(null, message);
    }

    private void toast(String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    public void pickFolder() {
        try {
            pickFolderLauncher.launch(null); // null to open root
        } catch (ActivityNotFoundException e) {
            String errorMsg = context.getString(R.string.msg_folder_selection_failed_no_act);
            notifyError(new ActivityNotFoundException(errorMsg));
        }
    }

    public void pickZipFile() {
        launchFilePicker(MetaDocument.MimeType.ZIP.toString());
    }

    public void requestDirPermission(final Uri uri) {
        requestDirPermission(uri, true);
    }

    public void requestDirPermission(final Uri uri, boolean mustBeRoot) {
        if (isInValidUri(uri)) return;

        try {
            boolean isTreeUri = DocumentsContract.isTreeUri(uri);
            FileUriMediator mediator = FileUriMediator.resolveTree(uri, context);

            if (!isTreeUri) {
                toast(context.getString(R.string.msg_directory_not_tree));
                return;
            }

            if (isInValidUriAuthority(mediator.getAuthority())) return;
            String storageType = mediator.getStorageType();

            // only allow internal storage to be modified for now until doc api is ready
            if (!Constants.MODIFIABLE_EXTERNAL_STORAGE_IDS.contains(storageType)) {
                toast(context.getString(R.string.msg_internal_storage_only));
                return;
            }

            var documentID = storageType + ":";
            Uri plausibleRoot = UriUtils.revertPathToRoot(uri, documentID);

            if (mustBeRoot) {
                if (plausibleRoot.equals(uri)) {
                    requestFolderPermissionLauncher.launch(plausibleRoot);
                } else {
                    toast(context.getString(R.string.msg_not_storage_root));
                }
            } else {
                requestFolderPermissionLauncher.launch(plausibleRoot);
            }
        } catch (Exception e) {
            notifyError(new IOException(context.getString(R.string.msg_directory_access_config),
                e));
        }
    }

    private boolean isInValidUri(Uri uri) {
        if (uri == null) {
            toast(context.getString(R.string.msg_invalid_resource_selection));
            return true;
        } else {
            return false;
        }
    }

    private boolean isInValidUriAuthority(String authority) {
        if (FileUriMediator.isAllowedAuthority(authority)) {
            return false;
        } else {
            toast(context.getString(R.string.msg_unsupported_authority_generic));
            return true;
        }
    }

    public void saveFileAs(@NonNull String name, @NonNull Uri uri) {
        try {
            Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("text/*");
            intent.putExtra(Intent.EXTRA_TITLE, name);
            intent.putExtra(EXTRA_SAVE_AS, uri.toString());
            saveFileAsLauncher.launch(intent);
        } catch (ActivityNotFoundException e) {
            String errorMsg = context.getString(R.string.msg_save_as_failed_no_act);
            notifyError(new ActivityNotFoundException(errorMsg));
        }
    }

    private void mediateFileCreation(Uri uri) {
        if (isInValidUri(uri)) return;

        try {
            FileUriMediator mediator = FileUriMediator.resolveDocument(uri, context);

            if (isInValidUriAuthority(mediator.getAuthority())) return;
            String storageType = mediator.getStorageType();

            // only allow internal storage to be modified for now until doc api is ready
            if (!Constants.MODIFIABLE_EXTERNAL_STORAGE_IDS.contains(storageType)) {
                toast(context.getString(R.string.msg_internal_storage_only));
                return;
            }

            File createdFile = mediator.getFile();

            if (createdFile == null || !createdFile.exists() || !createdFile.isFile()) {
                toast(context.getString(R.string.msg_invalid_file_created));
                return;
            }

            recentProjects.recordFileCreation(createdFile);

            fileViewModel.setPickedFile(createdFile);
        } catch (Exception e) {
            notifyError(new IOException(context.getString(R.string.msg_file_creation), e));
        }
    }

    private void mediateFileUri(Uri uri) {
        mediateFileUri(uri, context.getString(R.string.msg_unsupported_authority_access));
    }

    private void mediateFileUri(Uri uri, String invalidAuthMsg) {
        if (isInValidUri(uri)) return;

        try {
            FileUriMediator mediator = FileUriMediator.resolveDocument(uri, context);

            if (!FileUriMediator.isAllowedAuthority(mediator.getAuthority())) {
                toast(invalidAuthMsg);
                return;
            }

            if (isInValidUriAuthority(mediator.getAuthority())) return;
            String storageType = mediator.getStorageType();

            // only allow internal storage to be modified for now until doc api is ready
            if (!Constants.MODIFIABLE_EXTERNAL_STORAGE_IDS.contains(storageType)) {
                toast(context.getString(R.string.msg_internal_storage_only));
                return;
            }

            File pickedFile = mediator.getFile();

            if (pickedFile == null || !pickedFile.exists() || !pickedFile.isFile()) {
                toast(context.getString(R.string.msg_invalid_file_selected));
                return;
            }

            recentProjects.recordFileOpening(pickedFile);
            fileViewModel.setPickedFile(pickedFile);
        } catch (Exception e) {
            notifyError(new IOException(context.getString(R.string.msg_file_access_violation), e));
        }
    }

    private void mediateFolderUri(Uri uri) {
        if (isInValidUri(uri)) return;

        int flags = Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
        UriUtils.takePersistableUriPermission(context, uri, flags);

        try {
            FileUriMediator mediator = FileUriMediator.resolveTree(uri, context);

            if (isInValidUriAuthority(mediator.getAuthority())) return;
            String storageType = mediator.getStorageType();

            // only allow internal storage to be modified for now until doc api is ready
            if (!Constants.MODIFIABLE_EXTERNAL_STORAGE_IDS.contains(storageType)) {
                toast(context.getString(R.string.msg_internal_storage_only));
                return;
            }

            File pickedFolder = mediator.getFile();

            if (pickedFolder == null || !pickedFolder.exists() || !pickedFolder.isDirectory()) {
                toast(context.getString(R.string.msg_invalid_folder_selected));
                return;
            }

            recentProjects.recordFolderCreation(pickedFolder);
            fileViewModel.setPickedFolder(pickedFolder);
        } catch (Exception e) {
            notifyError(new IOException(context.getString(R.string.msg_folder_access_violation),
                e));
        }
    }

    private void performSaveAs(ActivityResult result) {
        if (result.getResultCode() != Activity.RESULT_OK) return;

        try {
            Intent intent = result.getData();
            Uri uri = Objects.requireNonNull(result.getData()).getData();
            if (isInValidUri(uri)) return;

            String encoding = PreferencesUtils.getDefaultFileEncoding();
            var otherUri = Uri.parse(intent.getStringExtra(EXTRA_SAVE_AS));

            UriUtils.overWriteDocument(context, uri, UriUtils.readDocumentToString(context,
                otherUri, encoding));
            mediateFileUri(uri, context.getString(R.string.msg_unsupported_authority_create));
        } catch (IOException e) {
            ILog.error(TAG, "Failed to perform save as", e);
            notifyError(new IOException(context.getString(R.string.msg_save_as_failed), e));
        }
    }
}
