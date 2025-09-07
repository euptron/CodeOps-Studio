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
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.eup.codeopsstudio.common.archive.ZIPArchive;
import com.eup.codeopsstudio.common.util.FileUtil;
import com.eup.codeopsstudio.databinding.DialogFragmentUnzipBinding;
import com.eup.codeopsstudio.models.logger.Logger;
import com.eup.codeopsstudio.res.R;
import com.eup.codeopsstudio.util.BaseUtil;
import com.eup.codeopsstudio.viewmodel.MainViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;
import java.io.FileInputStream;

public class UnzipDialogFragment extends DialogFragment implements ZIPArchive.OnArchiveListener {
    public static final String TAG = "UnzipDialogFragment";
    private static final String KEY_ARGUMENT_ZIP_FILE_PATH = "zip_file_path";
    private static final String KEY_ARGUMENT_DESTINATION_DIRECTORY = "destination_directory";
    private static final String KEY_ARGUMENT_BUFFER_SIZE = "buffer_size";

    private DialogFragmentUnzipBinding binding;
    private ZIPArchive zipArchive;
    private String zipFilePath;
    private String destDirectory;
    private MainViewModel mainViewModel;
    private Logger logger;

    @NonNull
    public static UnzipDialogFragment newInstance(String zipFilePath, String destDirectory,
        int bufferSize) {
        UnzipDialogFragment fragment = new UnzipDialogFragment();
        Bundle arguments = new Bundle();
        arguments.putString(KEY_ARGUMENT_ZIP_FILE_PATH, zipFilePath);
        arguments.putString(KEY_ARGUMENT_DESTINATION_DIRECTORY, destDirectory);
        arguments.putInt(KEY_ARGUMENT_BUFFER_SIZE, bufferSize);
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        logger        = new Logger(Logger.LogClass.IDE);
        zipFilePath   = requireArguments().getString(KEY_ARGUMENT_ZIP_FILE_PATH);
        destDirectory = requireArguments().getString(KEY_ARGUMENT_DESTINATION_DIRECTORY);

        mainViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        logger.attach(requireActivity());

        int bufferSize = requireArguments().getInt(KEY_ARGUMENT_BUFFER_SIZE, -1);
        try {
            zipArchive = ZIPArchive.fromInputStream(new FileInputStream(zipFilePath),
                new File(destDirectory), bufferSize, this);
        } catch (Exception e) {
            BaseUtil.toastLong(e.getMessage());
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        binding = DialogFragmentUnzipBinding.inflate(getLayoutInflater());

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        builder.setTitle(R.string.unzip_dialog_fragment_title);
        builder.setPositiveButton(R.string.pause, null);
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> {
            if (zipArchive != null) {
                zipArchive.cancel();
            }
            BaseUtil.toastShort(R.string.unzip_canceled);
            dialog.dismiss();
        });
        builder.setView(binding.getRoot());
        String sb = getString(R.string.from).concat(": ") + zipFilePath + "\n"
            + getString(R.string.to).concat(": ") + destDirectory;
        binding.zipFilePathText.setText(sb);
        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setOnShowListener(d -> configurePauseButton(dialog));
        return dialog;
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        if (zipArchive != null && !zipArchive.isCanceled()) {
            zipArchive.cancelAndShutdown();
        }
        super.onDismiss(dialog);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (isDialogVisible()) {
            zipArchive.unzip();
        }
    }

    private boolean isDialogVisible() {
        return getDialog() != null && getDialog().isShowing();
    }

    private void configurePauseButton(@NonNull AlertDialog dialog) {
        Button pauseButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        pauseButton.setOnClickListener(v -> {
            if (zipArchive.isPaused()) {
                zipArchive.resume();
                pauseButton.setText(R.string.pause);
            } else {
                zipArchive.pause();
                pauseButton.setText(R.string.resume);
            }
        });
    }

    @Override
    public void onInitialize(String message) {
        if (isDialogVisible()) {
            binding.progressText.setText(message);
        }
    }

    @Override
    public void onStart(int totalItems) {
        if (isDialogVisible()) {
            binding.progressBar.setMax(totalItems);
        }
    }

    @Override
    public void onUpdateProgress(int progress, int total, String currentFile, int itemsLeft) {
        if (isDialogVisible()) {
            if (total <= 0) {
                binding.progressBar.setIndeterminate(true);
                return;
            }

            int scaledProgress = (total < 100) ? (int) (1000f * progress / total) : progress;
            binding.progressBar.setIndeterminate(false);
            binding.progressBar.setMax(total < 100 ? 1000 : total);
            binding.progressBar.setProgress(scaledProgress);
            binding.progressText.setText(getString(R.string.msg_items_progress, progress, total));
            binding.itemsLeftText.setText(getString(R.string.msg_items_left, itemsLeft));
        }
    }

    @Override
    public void onFileProgress(long bytesWritten, long totalBytes, String fileName) {
        if (isDialogVisible()) {
            binding.fileNameText.setText(fileName);

            if (totalBytes <= 0) {
                binding.currentFileProgressBar.setIndeterminate(true);
            } else {
                int scaledProgress = (totalBytes < 100) ? (int) (1000 * bytesWritten / totalBytes)
                    : (int) Math.min(bytesWritten, Integer.MAX_VALUE);
                binding.currentFileProgressBar.setIndeterminate(false);
                binding.currentFileProgressBar.setMax(
                    totalBytes < 100 ? 1000 : (int) Math.min(totalBytes, Integer.MAX_VALUE));
                binding.currentFileProgressBar.setProgress(scaledProgress);
            }
        }
    }

    @Override
    public void onComplete(String message) {
        if (zipArchive != null && !zipArchive.isCanceled()) {
            BaseUtil.toastShort(message);
            logger.d(TAG, message);
            // open imported project in tree
            mainViewModel.setToolbarSubTitle(FileUtil.getFileNameWithoutExtension(new File(destDirectory)));
            mainViewModel.setTreeViewFragmentTreeDir(new File(destDirectory));
        }
        if (isDialogVisible()) {
            dismiss();
        }
    }

    @Override
    public void onError(Exception exception) {
        if (isDialogVisible()) {
            BaseUtil.toastLong(exception.getMessage());
            dismiss();
        }
    }

    @Override
    public void onSpeedUpdate(String message) {
        if (isDialogVisible()) {
            binding.speedText.setText(getString(R.string.msg_speed, message));
        }
    }
}
