/*
 * This file is part of CodeOps Studio.
 * CodeOps Studio - Code anywhere anytime
 * https://github.com/euptron/CodeOps-Studio
 * Copyright (C) 2024-2026 Etido Peter
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

package com.eup.codeopsstudio.ui.recents;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.eup.codeopsstudio.R;
import com.eup.codeopsstudio.ui.recents.adapter.ProjectAdapter;
import com.eup.codeopsstudio.ui.recents.domain.Recents;
import com.eup.codeopsstudio.common.Constants;
import com.eup.codeopsstudio.databinding.FragmentRecentProjectsBottomSheetDialogBinding;
import com.eup.codeopsstudio.domain.FormatDateUseCase;
import com.eup.codeopsstudio.logger.Logger;
import com.eup.codeopsstudio.ui.recents.models.Project;
import com.eup.codeopsstudio.models.user.User;
import com.eup.codeopsstudio.viewmodel.MainViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * @author Etido Peter
 */
public class RecentProjectsBottomSheetDialogFragment extends BottomSheetDialogFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    public static final String TAG = "RecentProjectsBottomSheetDialogFragment";
    public static final Comparator<Project> PROJECT_FIRST_ORDER = (p1, p2) -> {
        boolean p1IsFile = p1.isFile();
        boolean p2IsFile = p2.isFile();
        boolean p1IsDir = p1.isDirectory();
        boolean p2IsDir = p2.isDirectory();

        if (p1IsFile && p2IsDir) {
            return 1;
        } else if (p2IsFile && p1IsDir) {
            return -1;
        } else {
            return String.CASE_INSENSITIVE_ORDER.compare(p1.getName(), p2.getName());
        }
    };

    public static final Comparator<Project> COMBINED_ORDER =
        PROJECT_FIRST_ORDER.thenComparingLong(Project::getLastModified);

    private Recents recentProjects;
    private ProjectAdapter adapter;
    private Logger logger;
    private MainViewModel mainViewModel;
    private FragmentRecentProjectsBottomSheetDialogBinding binding;
    private SharedPreferences sharedPreferences;

    public RecentProjectsBottomSheetDialogFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        logger = new Logger(Logger.LogClass.IDE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState) {
        binding = FragmentRecentProjectsBottomSheetDialogBinding.inflate(inflater, container,
            false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mainViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        logger.attach(requireActivity());

        recentProjects    = Recents.initialize(requireContext());
        sharedPreferences = recentProjects.getSharedPreferences();

        adapter = new ProjectAdapter();
        populateRecentsAdapter(recentProjects.getRecentProjects());
        adapter.setOnItemClickListener(this::openProject);
        adapter.setOnItemLongClickListener(this::inflateProjectDialogs);
        binding.list.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.list.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    /**
     * Populates the RecyclerView mAdapter with the list of recent projects.
     *
     * <p>This method performs the following operations to organize the projects:
     * <ul>
     *     <li><strong>Reversal:</strong> The list of projects is reversed so that the most
     *     recently added projects
     *     appear at the beginning of the list, adhering to the principle of "first added, first
     *     seen".</li>
     *     <li><strong>Sorting:</strong> Projects are then sorted using a combined comparator
     *     ({@code COMBINED_ORDER})
     *     which considers both the file type and the creation date of each project. This ensures
     *     that projects are
     *     displayed in an order that is both type-specific and chronological.</li>
     * </ul>
     *
     * @param projects The list of {@link Project} objects representing recent projects to be
     *                 displayed.
     *                 This list contains all projects to be shown in the RecyclerView.
     */
    private void populateRecentsAdapter(List<Project> projects) {
        List<Project> modifiableRecentProjects = new ArrayList<>(projects);
        Collections.reverse(modifiableRecentProjects);
        modifiableRecentProjects.sort(COMBINED_ORDER);
        adapter.submitList(modifiableRecentProjects);
    }

    private void openProject(Project project) {
        if (project == null) return;
        var file = project.getFile();

        if (!file.exists()) {
            logger.w(TAG, getString(R.string.msg_file_does_not_exist, file.getAbsolutePath()));
            return;
        }

        if (file.isFile()) {
            mainViewModel.openEditorFile(file);
        } else if (file.isDirectory()) {
            mainViewModel.setTreeViewFragmentTreeDir(file);
        }

        if (isDialogVisible()) {
            dismiss();
        }
    }

    private boolean isDialogVisible() {
        return getDialog() != null && getDialog().isShowing();
    }

    @SuppressLint("NotifyDataSetChanged")
    private boolean inflateProjectDialogs(View view, Project project) {
        CharSequence[] options = {
            getString(R.string.remove), getString(R.string.check_history)
        };

        new MaterialAlertDialogBuilder(requireContext()).setItems(options, (dialog, which) -> {
            if (which == 0) {
                dialog.dismiss();
                String message = getString(R.string.prompt_remove_from_recent, project.getName());
                new MaterialAlertDialogBuilder(requireContext()).setMessage(message)
                                                                .setPositiveButton(R.string.yes,
                                                                    (dialogInterface, item) -> {
                                                                    recentProjects.remove(project);
                                                                    if (adapter != null) {
                                                                        notifyDataSetChanged();
                                                                    }
                                                                })
                                                                .setNegativeButton(R.string.no,
                                                                    null)
                                                                .show();
            } else if (which == 1) {
                dialog.dismiss();
                long date = Objects.requireNonNull(project.getHistory()).creationDate;
                String message = getString(R.string.msg_recent_project_history, getDate(date),
                    project.getHistory().fileAction.toString());
                new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(project.getName() + " " + getString(R.string.history))
                    .setMessage(message).setPositiveButton(android.R.string.cancel, null).show();
            }
        }).show();
        return true;
    }

    private void notifyDataSetChanged() {
        populateRecentsAdapter(recentProjects.getRecentProjects());
    }

    private String getDate(long time) {
        return new FormatDateUseCase(User.newInstance()).format(new Date(time));
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences pref, @Nullable String key) {
        if (Objects.equals(key, Constants.SharedPreferenceKeys.KEY_RECENT_PROJECTS)) {
            populateRecentsAdapter(recentProjects.getRecentProjects());
        }
    }
}