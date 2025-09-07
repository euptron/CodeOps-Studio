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

package com.eup.codeopsstudio.ui.editor.panes;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.eup.codeopsstudio.MainFragment;
import com.eup.codeopsstudio.common.Constants;
import com.eup.codeopsstudio.common.util.PreferencesUtils;
import com.eup.codeopsstudio.databinding.LayoutPaneWelcomeBinding;
import com.eup.codeopsstudio.git.GitUI;
import com.eup.codeopsstudio.models.logger.Logger;
import com.eup.codeopsstudio.pane.Pane;
import com.eup.codeopsstudio.ui.archive.ZIPFilePickerDialogFragment;
import com.eup.codeopsstudio.ui.recents.RecentProjectsBottomSheetDialogFragment;
import com.eup.codeopsstudio.util.BaseUtil;
import com.eup.codeopsstudio.viewmodel.MainViewModel;

import java.io.File;
import java.util.Objects;

/**
 * @author Etido Peter
 */
public class WelcomePane extends Pane implements SharedPreferences.OnSharedPreferenceChangeListener {
    public static final String TAG = WelcomePane.class.getSimpleName();
    private MainViewModel mainViewModel;
    private LayoutPaneWelcomeBinding binding;
    private Logger logger;

    public WelcomePane(Context context, String title) {
        this(context, title, /* generate new uuid= */ true);
    }

    public WelcomePane(Context context, String title, boolean generateUUID) {
        super(context, title, generateUUID);
    }

    @Override
    public View onCreateView() {
        binding       = LayoutPaneWelcomeBinding.inflate(LayoutInflater.from(getContext()));
        mainViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        logger        = new Logger(Logger.LogClass.IDE);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view) {
        super.onViewCreated(view);
        logger.attach(requireActivity());
        PreferencesUtils
            .getDefaultPreferences()
            .registerOnSharedPreferenceChangeListener(this);

        mainViewModel
            .getZipFile()
            .observe(requireActivity(), this::openZipFile);

        binding.welcomeCheckbox.setOnCheckedChangeListener((button, isChecked) -> PreferencesUtils.setCanShowWelcomePane(isChecked));
        binding.newFile.setOnClickListener(v -> callFragmentMethod(MainFragment.TAG,
            "createFileFromManager"));
        binding.importZipBtn.setOnClickListener(v -> callFragmentMethod(MainFragment.TAG,
            "openFileFromManager"));
        binding.openFolder.setOnClickListener(v -> callFragmentMethod(MainFragment.TAG,
            "openFolderFromManager"));
        binding.gitVcs.setOnClickListener(v -> {
            var gitUI = new GitUI(requireContext());
            gitUI.showCloneDialog(project -> mainViewModel.setTreeViewFragmentTreeDir(project));
        });
        binding.importZipBtn.setOnClickListener(v -> callFragmentMethod(MainFragment.TAG,
            "openZipFileFromManager"));
        binding.recentProjectBtn.setOnClickListener(v -> {
            RecentProjectsBottomSheetDialogFragment dialogFragment =
                new RecentProjectsBottomSheetDialogFragment();
            dialogFragment.show(requireActivity().getSupportFragmentManager(),
                RecentProjectsBottomSheetDialogFragment.TAG);
        });
    }

    private void openZipFile(File zipFile) {
        if (zipFile == null || !zipFile.exists() || !zipFile.isFile()) {
            String msg = getString(com.eup.codeopsstudio.R.string.cannot_open_invalid_zip_file);
            BaseUtil.toastShort(msg);
            logger.w(TAG, msg);
            return;
        }

        try {
            var unzipDialog = ZIPFilePickerDialogFragment.newInstance(zipFile.getAbsolutePath());
            unzipDialog.show(requireActivity().getSupportFragmentManager(),
                ZIPFilePickerDialogFragment.TAG);
        } catch (Exception e) {
            logger.e(TAG, e.getMessage());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        PreferencesUtils
            .getDefaultPreferences()
            .unregisterOnSharedPreferenceChangeListener(this);
        binding = null;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences pref, @Nullable String key) {
        if (Objects.equals(key, Constants.SharedPreferenceKeys.KEY_SHOW_WELCOME_PANE)) {
            boolean isChecked = PreferencesUtils.canShowWelcomePanel();
            binding.welcomeCheckbox.setChecked(isChecked);
        }
    }
}