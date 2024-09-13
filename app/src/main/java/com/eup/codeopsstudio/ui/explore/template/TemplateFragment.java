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
 
   package com.eup.codeopsstudio.ui.explore.template;

import static com.eup.codeopsstudio.common.Constants.SharedPreferenceKeys;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.documentfile.provider.DocumentFile;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.transition.TransitionManager;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.transition.MaterialFadeThrough;
import com.google.android.material.transition.MaterialSharedAxis;
import com.eup.codeopsstudio.common.AsyncTask;
import com.eup.codeopsstudio.common.util.FileUtil;
import com.eup.codeopsstudio.common.util.PreferencesUtils;
import com.eup.codeopsstudio.common.util.TextWatcherAdapter;
import com.eup.codeopsstudio.databinding.FragmentTemplateBinding;
import com.eup.codeopsstudio.logging.Logger;
import com.eup.codeopsstudio.res.R;
import com.eup.codeopsstudio.ui.explore.template.adapters.ProjectTemplateAdapter;
import com.eup.codeopsstudio.ui.explore.template.models.ProjectTemplateModel;
import com.eup.codeopsstudio.util.BaseUtil;
import com.eup.codeopsstudio.viewmodel.MainViewModel;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import org.apache.commons.io.FileUtils;

public class TemplateFragment extends BottomSheetDialogFragment {

  public static final String LOG_TAG = "TemplateFragment";
  private boolean previous; // used for backward navigation
  private FragmentTemplateBinding binding;
  private ProjectTemplateAdapter adapter;
  private ProjectTemplateModel mCurrentTemplate;
  private MainViewModel mainViewModel;
  private Logger logger;
  private ActivityResultLauncher<Intent> mStartForResult;
  private final OnBackPressedCallback onBackPressedCallback =
      new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
          navigatePrevious();
        }
      };

  public static TemplateFragment newInstance() {
    return new TemplateFragment();
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mainViewModel =
        new ViewModelProvider(requireActivity() /*shared activity scope*/).get(MainViewModel.class);
    logger = new Logger(Logger.LogClass.IDE);
    mStartForResult =
        registerForActivityResult(
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
  }

  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup viewgroup,
      @Nullable Bundle savedInstanceState) {
    binding = FragmentTemplateBinding.inflate(inflater, viewgroup, false);
    logger.attach(this);
    requireActivity()
        .getOnBackPressedDispatcher()
        .addCallback(getViewLifecycleOwner(), onBackPressedCallback);
    binding.footer.finish.setVisibility(View.GONE);
    binding.footer.finish.setOnClickListener(this::navigateNext);
    binding.footer.previous.setOnClickListener(v -> navigatePrevious());
    adapter = new ProjectTemplateAdapter();
    binding.dynamicList.setAdapter(adapter);
    initalizeTemplateDetails();
    return binding.getRoot();
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    loadTemplates();
    binding.footer.previous.setVisibility(View.GONE);
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    this.binding = null;
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    onBackPressedCallback.setEnabled(false);
  }

  private void navigatePrevious() {
    if (!previous) {
      getParentFragmentManager().popBackStack();
    } else {
      showTemplatesView();
      previous = false;
      if (binding.loadingLayout.getRoot().getVisibility() == View.VISIBLE) {
        binding.loadingLayout.getRoot().setVisibility(View.GONE);
      }
    }
  }

  private void openFolder() {
    mStartForResult.launch(new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE));
  }

  private void onFolderSelected(Uri uri) {
    try {
      DocumentFile pickedDir = DocumentFile.fromTreeUri(requireContext(), uri);
      File file = new File(FileUtil.getPathFromUri(requireActivity(), pickedDir.getUri()));
      String folderPath = file.getAbsolutePath();
      if (folderPath != null) {
        mSaveLocationLayout.getEditText().setText(folderPath);
      }
    } catch (Exception e) {
      logger.e(
          LOG_TAG,
          getString(R.string.folder_selection_error)
              + " ["
              + getString(R.string.cause)
              + "] "
              + e.getMessage());
    }
  }

  private void navigateNext(View view) {
    if (!previous) {
      showTemplatesDetails();
      previous = true;
    } else {
      createProjectAsync();
    }
  }

  private void showTemplatesView() {
    binding.dynamicList.setVisibility(View.GONE);

    MaterialSharedAxis sharedAxis = new MaterialSharedAxis(MaterialSharedAxis.X, false);
    TransitionManager.beginDelayedTransition((ViewGroup) requireView(), sharedAxis);
    binding.projectDetails.getRoot().setVisibility(View.GONE);
    binding.dynamicList.setVisibility(View.VISIBLE);
    binding.footer.finish.setVisibility(View.GONE);
    binding.footer.previous.setVisibility(View.GONE);
    // binding.footer.finish.setText(R.string.next);
    binding.footer.previous.setText(R.string.previous);
    binding.title.setText(R.string.create_project_template_title);
  }

  private TextInputLayout mNameLayout;
  private TextInputLayout mSaveLocationLayout;

  private void initalizeTemplateDetails() {
    mNameLayout = binding.projectDetails.tilProjectName;
    mNameLayout
        .getEditText()
        .addTextChangedListener(
            new TextWatcherAdapter() {
              @Override
              public void afterTextChanged(Editable editable) {
                verifyDetails(editable);
              }
            });

    mSaveLocationLayout = binding.projectDetails.tilSaveLocation;
    mSaveLocationLayout
        .getEditText()
        .setText(
            PreferenceManager.getDefaultSharedPreferences(requireContext())
                .getString(
                    SharedPreferenceKeys.KEY_PROJECT_SAVE_PATH,
                    requireContext().getExternalFilesDir("Projects").getAbsolutePath()));
    initializeSaveLocation();

    mSaveLocationLayout
        .getEditText()
        .addTextChangedListener(
            new TextWatcherAdapter() {
              @Override
              public void afterTextChanged(Editable editable) {
                verifySaveLocation(editable);
              }
            });
  }

  private void showTemplatesDetails() {
    binding.loadingLayout.getRoot().setVisibility(View.GONE);
    MaterialSharedAxis sharedAxis = new MaterialSharedAxis(MaterialSharedAxis.X, true);
    TransitionManager.beginDelayedTransition((ViewGroup) requireView(), sharedAxis);
    binding.projectDetails.getRoot().setVisibility(View.VISIBLE);
    binding.dynamicList.setVisibility(View.GONE);
    binding.footer.finish.setText(R.string.create_project);
    binding.footer.finish.setVisibility(View.VISIBLE);
    binding.footer.previous.setVisibility(View.VISIBLE);
    binding.footer.previous.setText(R.string.previous);
    binding.title.setText(mCurrentTemplate.getName());
  }

  /**
   * Method that creates project
   *
   * @throws IOException Thrown when project can not be created
   */
  @WorkerThread
  private void createProject() throws IOException {

    File projectRoot = new File(mSaveLocationLayout.getEditText().getText().toString());
    if (!projectRoot.exists()) {
      if (!projectRoot.mkdirs()) {
        throw new IOException("Unable to create directory");
      }
    }
    File sourcesDir = new File(mCurrentTemplate.getPath());

    FileUtils.copyDirectory(sourcesDir, projectRoot);
  }

  private void initializeSaveLocation() {
    // mSaveLocationLayout.getEditText().setText(requireContext().getExternalFilesDir("Projects").getAbsolutePath());
    // mSaveLocationLayout.getEditText().setInputType(InputType.TYPE_NULL);
    mSaveLocationLayout.setEndIconOnClickListener(
        view -> {
          openFolder();
        });
  }

  private boolean validateDetails() {
    requireActivity()
        .runOnUiThread(
            () -> {
              verifyDetails(mNameLayout.getEditText().getText());
              verifySaveLocation(mSaveLocationLayout.getEditText().getText());
            });
    var templateName = mNameLayout.getEditText().getText();

    if (mNameLayout.isErrorEnabled() || (templateName != null && TextUtils.isEmpty(templateName))) {
      return false;
    }
    if (mSaveLocationLayout.isErrorEnabled()) {
      return false;
    }
    return mCurrentTemplate != null;
  }

  private void verifyDetails(Editable editable) {
    String name = editable.toString();
    if (TextUtils.isEmpty(name)) {
      mNameLayout.setError(getString(R.string.cp_error_project_name_empty));
      return;
    } else if (name.contains(File.pathSeparator) || name.contains(File.separator)) {
      mNameLayout.setError(getString(R.string.cp_error_project_name_illegal));
      return;
    } else {
      mNameLayout.setErrorEnabled(false);
    }
    File file =
        new File(
            PreferenceManager.getDefaultSharedPreferences(requireContext())
                    .getString(
                        SharedPreferenceKeys.KEY_PROJECT_SAVE_PATH,
                        requireContext().getExternalFilesDir("Projects").getAbsolutePath())
                + "/"
                + editable.toString());

    String path = file.getAbsolutePath();

    if (file.exists()) {
      mNameLayout.setError(getString(R.string.msg_folder_exists));
    } else {
      mNameLayout.setErrorEnabled(false);
      mSaveLocationLayout.getEditText().setText(path);
    }
  }

  /** Checks if #Input in project save location is valid */
  private void verifySaveLocation(Editable editable) {
    if (editable.toString().length() >= 240) {
      mSaveLocationLayout.setError(getString(R.string.cp_path_exceeds));
      return;
    } else {
      mSaveLocationLayout.setErrorEnabled(false);
    }

    File file = new File(editable.toString());
    if (file.getParentFile() == null || !file.getParentFile().canWrite()) {
      mSaveLocationLayout.setError(getString(R.string.cp_file_not_writable));
    } else {
      mSaveLocationLayout.setErrorEnabled(false);
    }
  }

  private void createProjectAsync() {
    TransitionManager.beginDelayedTransition((ViewGroup) requireView(), new MaterialFadeThrough());
    binding.dynamicList.setVisibility(View.GONE);
    binding.projectDetails.getRoot().setVisibility(View.GONE);
    binding.loadingLayout.getRoot().setVisibility(View.VISIBLE);

    AsyncTask.runNonCancelable(
        () -> {
          String savePath = mSaveLocationLayout.getEditText().getText().toString();
          try {
            if (validateDetails()) {
              createProject();
            } else {
              requireActivity().runOnUiThread(this::showTemplatesDetails);
              return;
            }
            if (getActivity() != null) {
              requireActivity()
                  .runOnUiThread(
                      () -> {
                        // Open the currently created project
                        openProject(new File(savePath));
                      });
            }
          } catch (IOException e) {
            requireActivity()
                .runOnUiThread(
                    () -> {
                      BaseUtil.showToast(e.getMessage(), BaseUtil.LENGTH_SHORT);
                      logger.e(
                          LOG_TAG,
                          getString(R.string.project_creation_fail)
                              + " ["
                              + getString(R.string.cause)
                              + "] "
                              + e.getMessage());
                      showTemplatesDetails();
                    });
          }
        });
  }

  private void loadTemplates() {
    TransitionManager.beginDelayedTransition((ViewGroup) requireView(), new MaterialFadeThrough());
    binding.loadingLayout.getRoot().setVisibility(View.VISIBLE);
    binding.dynamicList.setVisibility(View.GONE);

    AsyncTask.runNonCancelable(
        () -> {
          List<ProjectTemplateModel> templates = getTemplates();
          if (getActivity() != null) {
            getActivity()
                .runOnUiThread(
                    () -> {
                      TransitionManager.beginDelayedTransition(
                          (ViewGroup) requireView(), new MaterialFadeThrough());
                      binding.loadingLayout.getRoot().setVisibility(View.GONE);
                      binding.dynamicList.setVisibility(View.VISIBLE);

                      adapter.submitTemplateList(templates);

                      adapter.setOnTemplateClickListener(
                          (item, position) -> {
                            mCurrentTemplate = item;
                            navigateNext(binding.footer.finish);
                          });

                      adapter.setOnTemplateLongClickListener(
                          (v, model) -> {
                            var msg = new StringBuilder();
                            msg.append("Name: " + model.getName() + "\n");
                            msg.append("Type: " + model.getProjectType() + "\n");
                            msg.append("Author: " + model.getAuthor() + "\n");
                            msg.append("Released: " + model.getCreationDate() + "\n");
                            msg.append("Description : " + model.getDescription() + "\n");
                            msg.append("Version Code: " + model.getVersion() + "\n");
                            msg.append("Version Name: " + model.getVersionName());
                            
                            new MaterialAlertDialogBuilder(requireContext())
                                .setTitle(R.string.about_template)
                                .setMessage(msg.toString())
                                .setPositiveButton(R.string.cancel, null)
                                .setCancelable(true)
                                .show();
                            return true;
                          });
                    });
          }
        });
  }

  /**
   * @Param getTemplates The project templates gotten from a directory
   */
  private List<ProjectTemplateModel> getTemplates() {
    try {
      File file = requireContext().getExternalFilesDir("templates");
      extractTemplatesMaybe();

      File[] templateFiles = file.listFiles();
      if (templateFiles == null) {
        return Collections.emptyList();
      }
      if (templateFiles.length == 0) {
        extractTemplatesMaybe();
      }
      templateFiles = file.listFiles();
      if (templateFiles == null) {
        return Collections.emptyList();
      }

      List<ProjectTemplateModel> templates = new ArrayList<>();
      for (File child : templateFiles) {
        ProjectTemplateModel template = ProjectTemplateModel.fromFile(child);
        if (template != null) {
          templates.add(template);
        }
      }
      return templates;
    } catch (IOException e) {
      logger.e(
          LOG_TAG,
          getString(R.string.failed_retrieving_templates)
              + " ["
              + getString(R.string.cause)
              + "] "
              + e.getMessage());
      return Collections.emptyList();
    }
  }

  /**
   * @throws IOException Thrown if an error occurs on extraction
   */
  private void extractTemplatesMaybe() throws IOException {
    File hashFile = new File(requireContext().getExternalFilesDir("templates"), "hash");
    if (!hashFile.exists()) {
      extractZipFiles();
    } else {
      logger.d(LOG_TAG, getString(R.string.checking_templates));
      InputStream newIs = requireContext().getAssets().open("templates.zip");
      String newIsMd5 = FileUtil.calculateMD5(newIs);
      String oldMd5 = FileUtils.readFileToString(hashFile, Charset.defaultCharset());

      if (!newIsMd5.equals(oldMd5)) {
        extractZipFiles();
        logger.w(LOG_TAG, getString(R.string.msg_invalid_templates));
      } else {
        logger.d(LOG_TAG, getString(R.string.templates_are_valid));
      }
    }
  }

  /**
   * @throws IOException Thrown if an error occurs on extraction
   */
  private void extractZipFiles() throws IOException {
    File templatesDir = new File(requireContext().getExternalFilesDir(null), "templates");
    if (templatesDir.exists()) {
      FileUtils.deleteDirectory(templatesDir);
    }
    FileUtil.unzipFromAssets(requireContext(), "templates.zip", templatesDir.getParent());
    File hashFile = new File(templatesDir, "hash");
    if (!hashFile.createNewFile()) {
      throw new IOException("Unable to create hash file");
    }
    FileUtils.writeStringToFile(
        hashFile,
        FileUtil.calculateMD5(requireContext().getAssets().open("templates.zip")),
        Charset.defaultCharset());
  }

  private void openProject(File file) {
    if (file != null) {
      mainViewModel.setTreeViewFragmentTreeDir(file);
    }
    dismiss();
  }
}
