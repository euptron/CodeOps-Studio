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
 
   package com.eup.codeopsstudio.ui.editor.panes;

import static android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import static com.eup.codeopsstudio.common.Constants.SharedPreferenceKeys;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.documentfile.provider.DocumentFile;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.elevation.SurfaceColors;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.eup.codeopsstudio.MainFragment;
import com.eup.codeopsstudio.common.AsyncTask;
import com.eup.codeopsstudio.common.Constants;
import com.eup.codeopsstudio.common.util.Archive;
import com.eup.codeopsstudio.common.util.FileUtil;
import com.eup.codeopsstudio.common.util.PreferencesUtils;
import com.eup.codeopsstudio.common.util.TextWatcherAdapter;
import com.eup.codeopsstudio.databinding.LayoutLoggingSheetBinding;
import com.eup.codeopsstudio.databinding.LayoutPaneWelcomeBinding;
import com.eup.codeopsstudio.git.GitRepository;
import com.eup.codeopsstudio.logging.LogAdapter;
import com.eup.codeopsstudio.logging.Logger;
import com.eup.codeopsstudio.pane.Pane;
import com.eup.codeopsstudio.res.R;
import com.eup.codeopsstudio.res.databinding.LayoutDialogTextInputBinding;
import com.eup.codeopsstudio.res.databinding.LayoutSheetRecentProjectsBinding;
import com.eup.codeopsstudio.ui.editor.panes.recent.adapter.ProjectAdapter;
import com.eup.codeopsstudio.ui.editor.panes.recent.model.Project;
import com.eup.codeopsstudio.util.BaseUtil;
import com.eup.codeopsstudio.viewmodel.MainViewModel;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class WelcomePane extends Pane
    implements SharedPreferences.OnSharedPreferenceChangeListener {

  private String title;
  private MainViewModel mMainViewModel;
  private LayoutPaneWelcomeBinding binding;
  private GitRepository gitRepository;
  public static final String LOG_TAG = WelcomePane.class.getSimpleName();
  private ActivityResultLauncher<Intent> mPickFolder;
  private ActivityResultLauncher<String> devicePickFile;
  private Logger logger;
  private LogAdapter logAdapter;
  private AlertDialog alertDialog;
  private LayoutDialogTextInputBinding dialogTextInputBinding;
  private LayoutLoggingSheetBinding layoutLoggingSheetBinding;
  private Archive.onUnzippedListener listener;
  // recent
  private SharedPreferences sharedPreferences;
  private List<Project> projects = new ArrayList<>();
  private BottomSheetDialog bottomSheetDialog;
  private LayoutSheetRecentProjectsBinding bind;
  private ProjectAdapter adapter;

  public WelcomePane(Context context, String title) {
    this(context, title, /* generate new uuid= */ true);
  }

  public WelcomePane(Context context, String title, boolean generateUUID) {
    super(context, title, generateUUID);
  }

  @Override
  public View onCreateView() {
    binding = LayoutPaneWelcomeBinding.inflate(LayoutInflater.from(getContext()));
    mMainViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
    logAdapter = new LogAdapter();
    logger = new Logger(Logger.LogClass.IDE);
    return binding.getRoot();
  }

  @Override
  public void onViewCreated(View view) {
    super.onViewCreated(view);
    logger.attach(requireActivity());
    PreferencesUtils.getDefaultPreferences().registerOnSharedPreferenceChangeListener(this);
    // recent
    sharedPreferences =
        requireActivity()
            .getSharedPreferences(
                Constants.SharedPreferenceKeys.KEY_RECENT_PROJECTS, Context.MODE_PRIVATE);
    sharedPreferences.registerOnSharedPreferenceChangeListener(this);

    adapter = new ProjectAdapter();
    projects = getRecentProjects();

    mPickFolder =
        requireActivity()
            .registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                  if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent intent = result.getData();
                    if (intent != null) {
                      Uri folderUri = intent.getData();
                      if (folderUri != null) {
                        onFolderSelected(folderUri);
                      }
                    }
                  }
                });

    devicePickFile =
        requireActivity()
            .registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                  if (uri != null) {
                    try {
                      openFile(new File(FileUtil.getPathFromUri(getContext(), uri)));
                    } catch (Exception e) {
                      logger.e(LOG_TAG, e.getMessage());
                    }
                  }
                });
    gitRepository = new GitRepository(getContext(), requireActivity());
    // TODO: Support git preferences
    gitRepository.setAuthenticationDetails(null, null);

    dialogTextInputBinding =
        LayoutDialogTextInputBinding.inflate(LayoutInflater.from(getContext()));

    gitRepository.setCloneCompletionListener(
        project -> {
          mMainViewModel.setTreeViewFragmentTreeDir(project);
        });

    binding.welcomeCheckbox.setOnCheckedChangeListener(
        (button, isChecked) -> PreferencesUtils.setCanShowWelcomePane(isChecked));
    binding.newFile.setOnClickListener(
        v -> {
          callFragmentMethod(MainFragment.TAG, "createFileFromManager");
        });
    binding.openFile.setOnClickListener(
        v -> {
          callFragmentMethod(MainFragment.TAG, "openFileFromManager");
        });
    binding.openFolder.setOnClickListener(
        v -> {
          callFragmentMethod(MainFragment.TAG, "openFolderFromManager");
        });
    binding.gitVcs.setOnClickListener(
        v -> {
          gitRepository.initalize();
        });
    binding.importZipBtn.setOnClickListener(v -> devicePickFile.launch("*/*"));
    binding.recentProjectBtn.setOnClickListener(v -> createRecentSheet());
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    PreferencesUtils.getDefaultPreferences().unregisterOnSharedPreferenceChangeListener(this);
    sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    binding = null;
  }

  @Override
  public void onSharedPreferenceChanged(SharedPreferences pref, String key) {
    switch (key) {
      case SharedPreferenceKeys.KEY_SHOW_WELCOME_PANE:
        boolean isChecked = PreferencesUtils.canShowWelcomePanel();
        binding.welcomeCheckbox.setChecked(isChecked);
        break;
      case SharedPreferenceKeys.KEY_RECENT_PROJECTS:
         projects = getRecentProjects();
         populateAdapter(projects);
        break;
    }
  }

  @Override
  public void persist() {
    super.persist();
    // No-op
  }

  private void onFolderSelected(Uri uri) {
    try {
      DocumentFile pickedDir = DocumentFile.fromTreeUri(getContext(), uri);
      File selectedFolder = new File(FileUtil.getPathFromUri(getContext(), pickedDir.getUri()));
      String folderPath = selectedFolder.getAbsolutePath();
      if (folderPath != null) {
        dialogTextInputBinding.tilOther.getEditText().setText(folderPath);
      }
    } catch (Exception e) {
      logger.e(LOG_TAG, e.getMessage());
    }
  }

  private void openFile(File file) {
    if (file == null) {
      return;
    }
    if (file.isFile() && file.exists()) {
      initalize(file);
    }
  }

  public void initalize(File zipFile) {
    if (zipFile.getName().endsWith(".zip")) {

      MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getContext());
      builder.setTitle(R.string.create_project);
      builder.setMessage(R.string.msg_unzip_project_into_dir_based_on_project_name);
      builder.setView(dialogTextInputBinding.getRoot());
      dialogTextInputBinding.tilOther.setVisibility(View.VISIBLE);
      dialogTextInputBinding.tilName.setHint(getString(R.string.project_name));
      dialogTextInputBinding
          .tilName
          .getEditText()
          .setText(FileUtils.getFileNameNoExtension(zipFile));
      dialogTextInputBinding.tilOther.setHint(getString(R.string.save_location));
      dialogTextInputBinding.tilOther.setEndIconMode(TextInputLayout.END_ICON_CUSTOM);
      dialogTextInputBinding.tilOther.setEndIconDrawable(R.drawable.ic_folder_outline);
      dialogTextInputBinding.tilOther.setEndIconOnClickListener(
          v -> {
            mPickFolder.launch(new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE));
          });

      builder.setPositiveButton(
          getString(R.string.create),
          (dialog, which) -> {
            String projectName = dialogTextInputBinding.tilName.getEditText().getText().toString();
            String pesudoDir = dialogTextInputBinding.tilOther.getEditText().getText().toString();
            unzip(zipFile, new File(pesudoDir, projectName));
          });
      builder.setNegativeButton(android.R.string.cancel, null);
      builder.setCancelable(false);

      alertDialog = builder.create();

      alertDialog.setOnShowListener(
          d -> {
            final Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setEnabled(false);

            dialogTextInputBinding
                .tilOther
                .getEditText()
                .addTextChangedListener(
                    new TextWatcherAdapter() {
                      @Override
                      public void afterTextChanged(Editable editable) {
                        String url =
                            dialogTextInputBinding.tilName.getEditText().getText().toString();
                        final File output = new File(editable.toString());
                        if ((output != null && !output.exists())) {
                          positiveButton.setEnabled(false);
                          dialogTextInputBinding.tilOther.setErrorEnabled(true);
                          dialogTextInputBinding.tilOther.setError(
                              getString(R.string.msg_dir_not_exist));
                        } else {
                          positiveButton.setEnabled(true);
                          if (dialogTextInputBinding.tilOther.isErrorEnabled()) {
                            dialogTextInputBinding.tilOther.setErrorEnabled(false);
                          }
                        }
                      }
                    });
            dialogTextInputBinding
                .tilName
                .getEditText()
                .addTextChangedListener(
                    new TextWatcherAdapter() {
                      @Override
                      public void afterTextChanged(Editable editable) {
                        String projectName =
                            dialogTextInputBinding.tilName.getEditText().getText().toString();
                        final File output = new File(editable.toString(), projectName);
                        if ((output != null && output.exists())) {
                          positiveButton.setEnabled(false);
                          dialogTextInputBinding.tilName.setErrorEnabled(true);
                          dialogTextInputBinding.tilName.setError(
                              getString(R.string.msg_dir_does_exist));
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
      ToastUtils.showShort(getString(R.string.msg_selected_file_not_valid_type, getString(R.string.zip)));
    }
  }

  /**
   * Unzips an archive.
   *
   * @param zipFile the archive file
   * @param destinationDir the folder to extract the {@param zipFile}
   */
  private void unzip(File zipFile, File destinationDir) {
    layoutLoggingSheetBinding =
        LayoutLoggingSheetBinding.inflate(LayoutInflater.from(getContext()));

    BottomSheetDialog sheetDialog = new BottomSheetDialog(getContext());

    sheetDialog.setContentView(layoutLoggingSheetBinding.getRoot());
    sheetDialog.setCancelable(false);
    layoutLoggingSheetBinding.title.setText(
        getString(R.string.unzipping) + Constants.SPACE + zipFile.getName());

    layoutLoggingSheetBinding.progressbar.setProgress(100);

    layoutLoggingSheetBinding.loggingList.setLayoutManager(new LinearLayoutManager(getContext()));
    layoutLoggingSheetBinding.loggingList.setAdapter(logAdapter);

    logger.d("Archive", getString(R.string.initialilizing));
    mMainViewModel
        .getIDELogs()
        .observe(
            requireActivity(),
            data -> {
              if (!data.isEmpty()) {
                logAdapter.submitList(data);
                scrollToLastItem();
              }
            });

    Archive archive = new Archive();

    listener =
        new Archive.onUnzippedListener() {

          @Override
          public void onFileUnArchiving(
              int unzippedFileCount, int totalFileCount, String currentFileName) {
            ThreadUtils.runOnUiThread(
                () -> {
                  logger.d(
                      getString(R.string.unarchiving_file)
                          + " "
                          + currentFileName
                          + " ("
                          + unzippedFileCount
                          + "/"
                          + totalFileCount
                          + ")");
                  int progress =
                      (int) (((double) unzippedFileCount / (double) totalFileCount) * 100);
                  layoutLoggingSheetBinding.progressbar.setProgressCompat(progress, true);
                });
          }

          @Override
          public void onLog(String message) {
            ThreadUtils.runOnUiThread(
                () -> {
                  ToastUtils.showShort(message);
                });
          }
        };

    archive.setListener(listener);
    CompletableFuture<String> task =
        AsyncTask.run(
            () -> {
              // if (!destinationDir.exists()) {
              // try {
              // org.apache.commons.io.FileUtils.forceMkdir(destinationDir);
              // ThreadUtils.runOnUiThread(
              // () -> {
              // logger.d(getString(R.string.msg_creating_destination_dir));
              // });
              // } catch (IOException e) {
              // throw new IOException(
              // WelcomePane.class.getCanonicalName()
              // + " "
              // + getString(R.string.msg_failed_creating_destination_dir)
              // + ": ",
              // e);
              // }
              // }
              archive.unzipIntoDestination(zipFile, destinationDir);
              return getString(R.string.successfully_imported)
                  + " "
                  + zipFile.getName()
                  + " "
                  + getString(R.string.into)
                  + " "
                  + destinationDir.getAbsolutePath(); // task completed
            });

    layoutLoggingSheetBinding.btnClose.setOnClickListener(
        v -> {
          ToastUtils.showLong(
              getString(R.string.unzipping)
                  + " "
                  + zipFile.getName()
                  + " "
                  + getString(R.string.msg_wait_till_complete, R.string.task));
        });

    sheetDialog.show();

    task.whenComplete(
        (result, throwable) -> {
          ThreadUtils.runOnUiThread(
              () -> {
                layoutLoggingSheetBinding.btnClose.setOnClickListener(
                    v -> {
                      task.cancel(true);
                      clearLogs();
                      sheetDialog.dismiss();
                    });
                layoutLoggingSheetBinding.progressbar.setVisibility(View.GONE);
                if (result != null && throwable == null) {
                  logger.d(result);
                  // open imported project in tree
                  mMainViewModel.setToolbarSubTitle(
                      FileUtils.getFileNameNoExtension(destinationDir));
                  mMainViewModel.setTreeViewFragmentTreeDir(destinationDir);
                } else {
                  logger.e(
                      "Archive",
                      getString(R.string.Importing_zip)
                          + ": "
                          + zipFile.getName()
                          + " "
                          + getString(R.string.failed)
                          + ": "
                          + throwable.getMessage());
                }
              });
        });
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

  public void addToRecent(@NonNull Project project) {
    projects.add(project);
    sharedPreferences
        .edit()
        .putString(SharedPreferenceKeys.KEY_RECENT_PROJECTS, new Gson().toJson(projects))
        .apply();
    if (adapter != null) {
      adapter.notifyDataSetChanged();
    }
  }

  public void removeFromRecent(@NonNull Project project) {
    projects.removeIf(currentProject -> project.equals(currentProject));
    sharedPreferences
        .edit()
        .putString(SharedPreferenceKeys.KEY_RECENT_PROJECTS, new Gson().toJson(projects))
        .apply();
    if (adapter != null) {
      adapter.notifyDataSetChanged();
    }
  }

  public ArrayList<Project> getRecentProjects() {
    var json = sharedPreferences.getString(SharedPreferenceKeys.KEY_RECENT_PROJECTS, "");
    ArrayList<Project> recentProjects =
        new Gson().fromJson(json, new TypeToken<ArrayList<Project>>() {}.getType());
    if (recentProjects != null && !recentProjects.isEmpty()) {
      recentProjects.removeIf(project -> 
        Optional.ofNullable(project.getFile())
                .map(file -> !file.exists())
                .orElse(true));
      return recentProjects;
    }
    return new ArrayList<Project>();
  }

  private void createRecentSheet() {
    bottomSheetDialog = new BottomSheetDialog(getContext());
    bind = LayoutSheetRecentProjectsBinding.inflate(requireActivity().getLayoutInflater());
    bottomSheetDialog.setContentView(bind.getRoot());
    populateAdapter(projects);
    adapter.setOnItemClickListener(this::openProject);
    adapter.setOnItemLongClickListener(this::inflateProjectDialogs);
    bind.list.setLayoutManager(new LinearLayoutManager(getContext()));
    bind.list.setAdapter(adapter);
    bottomSheetDialog.show();
  }
  
  private void populateAdapter(List<Project> projects) {
    Collections.sort(projects, COMBINED_ORDER);
    adapter.submitList(projects);
  }
  
  private void openProject(Project project) {
    if (project != null) {
      // BaseFragment performs sanity check for invalid files
      var file = project.getFile();
      if (file.exists() && file.isFile()) {
        callFragmentMethod(MainFragment.TAG, "openFileInPane", file);
      } else if (file.exists() && file.isDirectory()) {
        callFragmentMethod(MainFragment.TAG, "openFolderInTreeViewFragment", file);
      }
      bottomSheetDialog.dismiss();
    }
  }

  private boolean inflateProjectDialogs(View view, Project project) {
    CharSequence[] options = {getString(R.string.remove), getString(R.string.check_history)};

    new MaterialAlertDialogBuilder(getContext())
        .setItems(
            options,
            (dialog, which) -> {
              if (which == 0) {
                dialog.dismiss();
                String message = getString(R.string.prompt_remove_from_recent, project.getName());
                new MaterialAlertDialogBuilder(getContext())
                    .setMessage(message)
                    .setPositiveButton(
                        R.string.yes, (dialogInterface, item) -> removeFromRecent(project))
                    .setNegativeButton(R.string.no, null)
                    .show();
              } else if (which == 1) {
                dialog.dismiss();
                String message =
                    getContext()
                        .getString(
                            R.string.msg_recent_project_history,
                            getDate(project.getHistory().creationDate),
                            project.getHistory().fileAction);
                new MaterialAlertDialogBuilder(getContext())
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
    SimpleDateFormat dateFormat =
        new SimpleDateFormat("EEE, MMM dd, yyyy HH:mm:ss:S", Locale.getDefault());
    return dateFormat.format(new Date(time));
  }

  public static final Comparator<Project> PROJECT_FIRST_ORDER =
      (project1, project2) -> {
        if (project1.getFile().isFile() && project2.getFile().isDirectory()) {
          return 1;
        } else if (project2.getFile().isFile() && project1.getFile().isDirectory()) {
          return -1;
        } else {
          return String.CASE_INSENSITIVE_ORDER.compare(project1.getName(), project2.getName());
        }
      };

  public static final Comparator<Project> COMBINED_ORDER =
      PROJECT_FIRST_ORDER.thenComparing(
          Comparator.comparingLong(project -> project.getFile().lastModified()));

}
