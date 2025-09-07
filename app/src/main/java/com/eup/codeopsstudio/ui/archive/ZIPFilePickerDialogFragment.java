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
 * questions or need additional information. Email: etido.up@gmail.com
 */

package com.eup.codeopsstudio.ui.archive;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.eup.codeopsstudio.MainActivity;
import com.eup.codeopsstudio.R;
import com.eup.codeopsstudio.common.util.FileUtil;
import com.eup.codeopsstudio.common.util.PreferencesUtils;
import com.eup.codeopsstudio.common.util.TextWatcherAdapter;
import com.eup.codeopsstudio.models.logger.Logger;
import com.eup.codeopsstudio.res.databinding.LayoutDialogTextInputBinding;
import com.eup.codeopsstudio.util.BaseUtil;
import com.eup.codeopsstudio.viewmodel.FileViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;

import java.io.File;
import java.util.Objects;

/**
 * @author Etido Peter
 */
public class ZIPFilePickerDialogFragment extends DialogFragment {
    public static final String TAG = "ZIPFilePickerDialogFragment";
    private static final String KEY_ARGUMENT_SELECTED_ZIP_FILE_PATH = "zip_file_path";

    private Logger logger;
    private LayoutDialogTextInputBinding dialogTextInputBinding;
    private File zipFile;
    private String zipPath;
    private FileViewModel fileViewModel;

    @NonNull
    public static ZIPFilePickerDialogFragment newInstance(String selectedZIPFilePath) {
        ZIPFilePickerDialogFragment fragment = new ZIPFilePickerDialogFragment();
        Bundle arguments = new Bundle();
        arguments.putString(KEY_ARGUMENT_SELECTED_ZIP_FILE_PATH, selectedZIPFilePath);
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        logger  = new Logger(Logger.LogClass.IDE);
        zipPath = requireArguments().getString(KEY_ARGUMENT_SELECTED_ZIP_FILE_PATH);
        zipFile = new File(Objects.requireNonNull(zipPath));

        fileViewModel = new ViewModelProvider(requireActivity()).get(FileViewModel.class);
        logger.attach(requireActivity());
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        dialogTextInputBinding = LayoutDialogTextInputBinding.inflate(getLayoutInflater());

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        builder.setTitle(com.eup.codeopsstudio.res.R.string.create_project);
        builder.setView(dialogTextInputBinding.getRoot());
        dialogTextInputBinding.tilOther.setVisibility(View.VISIBLE);
        dialogTextInputBinding.inputDescription.setVisibility(View.VISIBLE);
        dialogTextInputBinding.inputDescription.setText(com.eup.codeopsstudio.res.R.string.msg_unzip_project_into_dir_based_on_project_name);
        dialogTextInputBinding.tilName.setHint(getString(com.eup.codeopsstudio.res.R.string.project_name));
        Objects
            .requireNonNull(dialogTextInputBinding.tilName.getEditText())
            .setText(FileUtil.getFileNameWithoutExtension(zipFile));
        dialogTextInputBinding.tilOther.setHint(getString(com.eup.codeopsstudio.res.R.string.save_location));
        dialogTextInputBinding.tilOther.setEndIconMode(TextInputLayout.END_ICON_CUSTOM);
        dialogTextInputBinding.tilOther.setEndIconDrawable(com.eup.codeopsstudio.res.R.drawable.ic_folder_outline);
        dialogTextInputBinding.tilOther.setEndIconOnClickListener(v -> {
            MainActivity mainActivity = (MainActivity) requireActivity();
            mainActivity
                .getLifecycleObserver()
                .pickFolder();
        });

        builder.setPositiveButton(getString(com.eup.codeopsstudio.res.R.string.create), (dialog,
            which) -> {
            String projectName = dialogTextInputBinding.tilName
                .getEditText()
                .getText()
                .toString();
            String destDirPath = Objects
                .requireNonNull(dialogTextInputBinding.tilOther.getEditText())
                .getText()
                .toString();
            // show unzip dialog
            File destDir = new File(destDirPath, projectName);
            int bufferSize = PreferencesUtils.getCurrentBufferSize();
            UnzipDialogFragment unzipDialog = UnzipDialogFragment.newInstance(zipPath,
                destDir.getAbsolutePath(), bufferSize);
            unzipDialog.show(getParentFragmentManager(), UnzipDialogFragment.TAG);
        });
        builder.setNegativeButton(android.R.string.cancel, null);

        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setOnShowListener(d -> configurePositiveButton(dialog));
        return dialog;
    }

    private void configurePositiveButton(@NonNull AlertDialog dialog) {
        final Button positiveButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        positiveButton.setEnabled(false);
        Objects.requireNonNull(dialogTextInputBinding.tilOther.getEditText());

        dialogTextInputBinding.tilOther
            .getEditText()
            .addTextChangedListener(new TextWatcherAdapter() {
                @Override
                public void afterTextChanged(@NonNull Editable editable) {
                    final File output = new File(editable.toString());
                    if (!output.exists()) {
                        positiveButton.setEnabled(false);
                        dialogTextInputBinding.tilOther.setErrorEnabled(true);
                        dialogTextInputBinding.tilOther.setError(getString(com.eup.codeopsstudio.res.R.string.msg_dir_not_exist));
                    } else {
                        positiveButton.setEnabled(true);
                        if (dialogTextInputBinding.tilOther.isErrorEnabled()) {
                            dialogTextInputBinding.tilOther.setErrorEnabled(false);
                        }
                    }
                }
            });

        Objects.requireNonNull(dialogTextInputBinding.tilName.getEditText());
        dialogTextInputBinding.tilName
            .getEditText()
            .addTextChangedListener(new TextWatcherAdapter() {
                @Override
                public void afterTextChanged(@NonNull Editable editable) {
                    String projectName = dialogTextInputBinding.tilName
                        .getEditText()
                        .getText()
                        .toString();
                    final File output = new File(editable.toString(), projectName);
                    if (output.exists()) {
                        positiveButton.setEnabled(false);
                        dialogTextInputBinding.tilName.setErrorEnabled(true);
                        dialogTextInputBinding.tilName.setError(getString(com.eup.codeopsstudio.res.R.string.msg_dir_does_exist));
                    } else {
                        positiveButton.setEnabled(true);
                        if (dialogTextInputBinding.tilName.isErrorEnabled()) {
                            dialogTextInputBinding.tilName.setErrorEnabled(false);
                        }
                    }
                }
            });
    }

    @Override
    public void onStart() {
        super.onStart();
        if (isDialogVisible()) {
            fileViewModel.observePickedFolders(requireActivity(), this::handlePickedFolder);
            if (!zipFile.exists() || !zipFile.isFile()) {
                logger.w(TAG, getString(R.string.cannot_open_invalid_zip_file));
                dismiss();
            }

            if (!zipFile
                .getName()
                .endsWith(".zip")) {
                String msg =
                    getString(com.eup.codeopsstudio.res.R.string.msg_selected_file_not_valid_type
                        , getString(com.eup.codeopsstudio.res.R.string.zip));
                BaseUtil.toastShort(msg);
                logger.w(TAG, msg);
                dismiss();
            }
        }
    }

    private void handlePickedFolder(@NonNull File file) {
        if (file.exists()) {
            var folderPath = file.getAbsolutePath();
            Objects.requireNonNull(dialogTextInputBinding.tilOther.getEditText());
            dialogTextInputBinding.tilOther
                .getEditText()
                .setText(folderPath);
            logger.d(TAG, getString(com.eup.codeopsstudio.res.R.string.folder_selection_success));
        }
    }

    private boolean isDialogVisible() {
        return getDialog() != null && getDialog().isShowing();
    }
}
