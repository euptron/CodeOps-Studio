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

import static com.eup.codeopsstudio.common.Constants.SharedPreferenceKeys;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.eup.codeopsstudio.MainActivity;
import com.eup.codeopsstudio.MainFragment;
import com.eup.codeopsstudio.adapters.ProjectAdapter;
import com.eup.codeopsstudio.adapters.logger.LogAdapter;
import com.eup.codeopsstudio.aggregators.Recents;
import com.eup.codeopsstudio.common.AsyncTask;
import com.eup.codeopsstudio.common.Constants;
import com.eup.codeopsstudio.common.util.Archive;
import com.eup.codeopsstudio.common.util.FileUtil;
import com.eup.codeopsstudio.common.util.PreferencesUtils;
import com.eup.codeopsstudio.common.util.TextWatcherAdapter;
import com.eup.codeopsstudio.databinding.LayoutLoggingSheetBinding;
import com.eup.codeopsstudio.databinding.LayoutPaneWelcomeBinding;
import com.eup.codeopsstudio.domain.FormatDateUseCase;
import com.eup.codeopsstudio.git.GitUI;
import com.eup.codeopsstudio.models.logger.Logger;
import com.eup.codeopsstudio.models.recents.Project;
import com.eup.codeopsstudio.models.user.User;
import com.eup.codeopsstudio.pane.Pane;
import com.eup.codeopsstudio.res.R;
import com.eup.codeopsstudio.res.databinding.LayoutDialogTextInputBinding;
import com.eup.codeopsstudio.res.databinding.LayoutSheetRecentProjectsBinding;
import com.eup.codeopsstudio.util.BaseUtil;
import com.eup.codeopsstudio.viewmodel.FileViewModel;
import com.eup.codeopsstudio.viewmodel.MainViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class WelcomePane extends Pane implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String LOG_TAG = WelcomePane.class.getSimpleName();
    public static final Comparator<Project> PROJECT_FIRST_ORDER = (project1, project2) -> {
        if (project1
            .getFile()
            .isFile() && project2
            .getFile()
            .isDirectory()) {
            return 1;
        } else if (project2
            .getFile()
            .isFile() && project1
            .getFile()
            .isDirectory()) {
            return -1;
        } else {
            return String.CASE_INSENSITIVE_ORDER.compare(project1.getName(), project2.getName());
        }
    };
    public static final Comparator<Project> COMBINED_ORDER =
        PROJECT_FIRST_ORDER.thenComparing(Comparator.comparingLong(project -> project
        .getFile()
        .lastModified()));
    private MainViewModel mainViewModel;
    private LayoutPaneWelcomeBinding binding;
    private GitUI gitUI;
    private Logger logger;
    private LogAdapter logAdapter;
    private AlertDialog alertDialog;
    private LayoutDialogTextInputBinding dialogTextInputBinding;
    private LayoutLoggingSheetBinding layoutLoggingSheetBinding;
    private SharedPreferences sharedPreferences;
    private BottomSheetDialog bottomSheetDialog;
    private LayoutSheetRecentProjectsBinding bind;
    private Recents recentProjects;
    private ProjectAdapter adapter;
    private FileViewModel fileViewModel;

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
        fileViewModel = new ViewModelProvider(requireActivity()).get(FileViewModel.class);
        logAdapter    = new LogAdapter();
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
        // recent
        recentProjects    = Recents.initialize(requireContext());
        sharedPreferences = recentProjects.getSharedPreferences();
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        adapter = new ProjectAdapter();

        mainViewModel
            .getZipFile()
            .observe(requireActivity(), this::openFile);
        fileViewModel.observePickedFolders(requireActivity(), this::handlePickedFolder);

        gitUI = new GitUI(requireContext());

        dialogTextInputBinding =
            LayoutDialogTextInputBinding.inflate(LayoutInflater.from(getContext()));

        binding.welcomeCheckbox.setOnCheckedChangeListener((button, isChecked) -> PreferencesUtils.setCanShowWelcomePane(isChecked));
        binding.newFile.setOnClickListener(v -> callFragmentMethod(MainFragment.TAG,
            "createFileFromManager"));
        binding.openFile.setOnClickListener(v -> callFragmentMethod(MainFragment.TAG,
            "openFileFromManager"));
        binding.openFolder.setOnClickListener(v -> callFragmentMethod(MainFragment.TAG,
            "openFolderFromManager"));
        binding.gitVcs.setOnClickListener(v -> gitUI.showCloneDialog(project -> mainViewModel.setTreeViewFragmentTreeDir(project)));
        binding.importZipBtn.setOnClickListener(v -> callFragmentMethod(MainFragment.TAG,
            "openZipFileFromManager"));
        binding.recentProjectBtn.setOnClickListener(v -> createRecentSheet());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        PreferencesUtils
            .getDefaultPreferences()
            .unregisterOnSharedPreferenceChangeListener(this);
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
        binding = null;
    }

    private void handlePickedFolder(@NonNull File file) {
        if (file != null || file.exists()) {
            var folderPath = file.getAbsolutePath();
            dialogTextInputBinding.tilOther
                .getEditText()
                .setText(folderPath);
            logger.d(LOG_TAG, getString(R.string.folder_selection_success));
        }
    }

    private void openFile(File file) {
        if (file == null || !file.exists() || !file.isFile()) {
            logger.w(LOG_TAG, "Cannot open invalid zip file");
            return;
        }
        try {
            initializeUnzipping(file);
        } catch (Exception e) {
            logger.e(LOG_TAG, e.getMessage());
        }
    }

    private void initializeUnzipping(File zipFile) {
        if (zipFile
            .getName()
            .endsWith(".zip")) {

            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
            builder.setTitle(R.string.create_project);
            builder.setMessage(R.string.msg_unzip_project_into_dir_based_on_project_name);
            builder.setView(dialogTextInputBinding.getRoot());
            dialogTextInputBinding.tilOther.setVisibility(View.VISIBLE);
            dialogTextInputBinding.tilName.setHint(getString(R.string.project_name));
            dialogTextInputBinding.tilName
                .getEditText()
                .setText(FileUtil.getFileNameWithoutExtension(zipFile));
            dialogTextInputBinding.tilOther.setHint(getString(R.string.save_location));
            dialogTextInputBinding.tilOther.setEndIconMode(TextInputLayout.END_ICON_CUSTOM);
            dialogTextInputBinding.tilOther.setEndIconDrawable(R.drawable.ic_folder_outline);
            dialogTextInputBinding.tilOther.setEndIconOnClickListener(v -> {
                MainActivity mainActivity = (MainActivity) requireActivity();
                mainActivity
                    .getLifecycleObserver()
                    .pickFolder();
            });

            builder.setPositiveButton(getString(R.string.create), (dialog, which) -> {
                String projectName = dialogTextInputBinding.tilName
                    .getEditText()
                    .getText()
                    .toString();
                String pesudoDir = dialogTextInputBinding.tilOther
                    .getEditText()
                    .getText()
                    .toString();
                unzip(zipFile, new File(pesudoDir, projectName));
            });
            builder.setNegativeButton(android.R.string.cancel, null);
            builder.setCancelable(false);

            alertDialog = builder.create();

            alertDialog.setOnShowListener(d -> {
                final Button positiveButton =
                    alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                positiveButton.setEnabled(false);

                dialogTextInputBinding.tilOther
                    .getEditText()
                    .addTextChangedListener(new TextWatcherAdapter() {
                        @Override
                        public void afterTextChanged(Editable editable) {
                            final File output = new File(editable.toString());
                            if ((output != null && !output.exists())) {
                                positiveButton.setEnabled(false);
                                dialogTextInputBinding.tilOther.setErrorEnabled(true);
                                dialogTextInputBinding.tilOther.setError(getString(R.string.msg_dir_not_exist));
                            } else {
                                positiveButton.setEnabled(true);
                                if (dialogTextInputBinding.tilOther.isErrorEnabled()) {
                                    dialogTextInputBinding.tilOther.setErrorEnabled(false);
                                }
                            }
                        }
                    });
                dialogTextInputBinding.tilName
                    .getEditText()
                    .addTextChangedListener(new TextWatcherAdapter() {
                        @Override
                        public void afterTextChanged(Editable editable) {
                            String projectName = dialogTextInputBinding.tilName
                                .getEditText()
                                .getText()
                                .toString();
                            final File output = new File(editable.toString(), projectName);
                            if ((output != null && output.exists())) {
                                positiveButton.setEnabled(false);
                                dialogTextInputBinding.tilName.setErrorEnabled(true);
                                dialogTextInputBinding.tilName.setError(getString(R.string.msg_dir_does_exist));
                            } else {
                                positiveButton.setEnabled(true);
                                if (dialogTextInputBinding.tilName.isErrorEnabled()) {
                                    dialogTextInputBinding.tilName.setErrorEnabled(false);
                                }
                            }
                        }
                    });
            });
            alertDialog.show();
        } else {
            BaseUtil.toastShort(getString(R.string.msg_selected_file_not_valid_type,
                getString(R.string.zip)));
        }
    }

    /**
     * Unzips an archive.
     *
     * @param zipFile        the archive file
     * @param destinationDir the folder to extract the {@param zipFile}
     */
    private void unzip(File zipFile, File destinationDir) {
        layoutLoggingSheetBinding =
            LayoutLoggingSheetBinding.inflate(LayoutInflater.from(requireContext()));

        BottomSheetDialog sheetDialog = new BottomSheetDialog(getContext());

        sheetDialog.setContentView(layoutLoggingSheetBinding.getRoot());
        sheetDialog.setCancelable(false);
        layoutLoggingSheetBinding.title.setText(
            getString(R.string.unzipping) + Constants.SPACE + zipFile.getName());

        layoutLoggingSheetBinding.progressbar.setProgress(100);
        layoutLoggingSheetBinding.loggingList.setLayoutManager(new LinearLayoutManager(getContext()));
        layoutLoggingSheetBinding.loggingList.setAdapter(logAdapter);
        logger.d("Archive", getString(R.string.initialilizing));

        mainViewModel
            .getIDELogs()
            .observe(requireActivity(), data -> {
                if (data != null && !data.isEmpty()) {
                    logAdapter.submitList(data);
                    scrollToLastItem();
                }
            });

        Archive archive = new Archive();

        Archive.OnUnzippedListener listener = new Archive.OnUnzippedListener() {
            @Override
            public void onFileUnArchiving(int unzippedFileCount, int totalFileCount,
                String currentFileName) {
                AsyncTask.runOnUiThread(() -> {
                    logger.d(getString(R.string.unarchiving_file) + " " + currentFileName + " ("
                        + unzippedFileCount + "/" + totalFileCount + ")");
                    int progress = (int) (((double) unzippedFileCount / (double) totalFileCount)
                        * 100);
                    layoutLoggingSheetBinding.progressbar.setProgressCompat(progress, true);
                });
            }

            @Override
            public void onLog(String message) {
                AsyncTask.runOnUiThread(() -> BaseUtil.toastShort(message));
            }
        };

        archive.setListener(listener);
        CompletableFuture<String> task = AsyncTask.run(() -> {

            archive.unzipIntoDestination(PreferencesUtils.getCurrentBufferSize(), zipFile,
                destinationDir);
            return getString(R.string.successfully_imported) + " " + zipFile.getName() + " "
                + getString(R.string.into) + " "
                + destinationDir.getAbsolutePath(); // task completed
        });

        layoutLoggingSheetBinding.btnClose.setOnClickListener(v -> BaseUtil.toastLong(
            getString(R.string.unzipping) + " " + zipFile.getName() + " "
                + getString(R.string.msg_wait_till_complete, R.string.task)));

        sheetDialog.show();

        task.whenComplete((result, throwable) -> AsyncTask.runOnUiThread(() -> {
            layoutLoggingSheetBinding.btnClose.setOnClickListener(v -> {
                task.cancel(true);
                clearLogs();
                sheetDialog.dismiss();
            });
            layoutLoggingSheetBinding.progressbar.setVisibility(View.GONE);
            if (result != null && throwable == null) {
                logger.d(result);
                // open imported project in tree
                mainViewModel.setToolbarSubTitle(FileUtil.getFileNameWithoutExtension(destinationDir));
                mainViewModel.setTreeViewFragmentTreeDir(destinationDir);
            } else {
                logger.e("Archive",
                    getString(R.string.Importing_zip) + ": " + zipFile.getName() + " "
                        + getString(R.string.failed) + ": " + throwable.getMessage());
            }
        }));
    }

    private void clearLogs() {
        if (logger != null) {
            logger.clear();
            logAdapter.notifyDataSetChanged();
        }
    }

    private void scrollToLastItem() {
        int itemCount = logAdapter.getItemCount();
        if (itemCount > 0) {
            layoutLoggingSheetBinding.loggingList.scrollToPosition(itemCount - 1);
        }
    }

    private void createRecentSheet() {
        bottomSheetDialog = new BottomSheetDialog(requireContext());
        bind              =
            LayoutSheetRecentProjectsBinding.inflate(requireActivity().getLayoutInflater());
        bottomSheetDialog.setContentView(bind.getRoot());
        populateRecentsAdapter(recentProjects.getRecentProjects());
        adapter.setOnItemClickListener(this::openProject);
        adapter.setOnItemLongClickListener(this::inflateProjectDialogs);
        bind.list.setLayoutManager(new LinearLayoutManager(getContext()));
        bind.list.setAdapter(adapter);
        bottomSheetDialog.show();
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
     *                 This list should contain all projects to be shown in the RecyclerView.
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
        if (file == null || !file.exists()) {
            logger.w(LOG_TAG, "Cannot open invalid document");
            return;
        }

        if (file.isFile()) {
            callFragmentMethod(MainFragment.TAG, "openFileInPane", file);
        } else if (file.isDirectory()) {
            callFragmentMethod(MainFragment.TAG, "openFolderInTreeViewFragment", file);
        }
        bottomSheetDialog.dismiss();
    }

    private boolean inflateProjectDialogs(View view, Project project) {
        CharSequence[] options = {getString(R.string.remove), getString(R.string.check_history)};

        new MaterialAlertDialogBuilder(requireContext())
            .setItems(options, (dialog, which) -> {
                if (which == 0) {
                    dialog.dismiss();
                    String message = getString(R.string.prompt_remove_from_recent,
                        project.getName());
                    new MaterialAlertDialogBuilder(requireContext())
                        .setMessage(message)
                        .setPositiveButton(R.string.yes, (dialogInterface, item) -> {
                            recentProjects.remove(project);
                            if (adapter != null) adapter.notifyDataSetChanged();
                        })
                        .setNegativeButton(R.string.no, null)
                        .show();
                } else if (which == 1) {
                    dialog.dismiss();
                    long date = Objects.requireNonNull(project.getHistory()).creationDate;
                    String message = getString(R.string.msg_recent_project_history, getDate(date)
                        , project.getHistory().fileAction.toString());
                    new MaterialAlertDialogBuilder(requireContext())
                        .setTitle(project.getName() + " " + getString(R.string.history))
                        .setMessage(message)
                        .setPositiveButton(android.R.string.cancel, null)
                        .show();
                }
            })
            .show();
        return true;
    }

    private String getDate(long time) {
        return new FormatDateUseCase(User.newInstance()).format(new Date(time));
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences pref, String key) {
        switch (key) {
            case SharedPreferenceKeys.KEY_SHOW_WELCOME_PANE:
                boolean isChecked = PreferencesUtils.canShowWelcomePanel();
                binding.welcomeCheckbox.setChecked(isChecked);
                break;
            case SharedPreferenceKeys.KEY_RECENT_PROJECTS:
                populateRecentsAdapter(recentProjects.getRecentProjects());
                break;
            default:
                break;
        }
    }
}
