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

package com.eup.codeopsstudio.ui.explore.template;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultRegistry;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.core.util.Pair;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.transition.TransitionManager;

import com.eup.codeopsstudio.R;
import com.eup.codeopsstudio.ui.explore.template.adapter.ProjectTemplateAdapter;
import com.eup.codeopsstudio.ui.explore.template.model.ProjectTemplateModel;
import com.eup.codeopsstudio.ui.recents.domain.Recents;
import com.eup.codeopsstudio.common.AsyncTask;
import com.eup.codeopsstudio.common.Constants;
import com.eup.codeopsstudio.common.ILog;
import com.eup.codeopsstudio.common.archive.ZIPArchive;
import com.eup.codeopsstudio.common.util.FileUtil;
import com.eup.codeopsstudio.common.util.PreferencesUtils;
import com.eup.codeopsstudio.common.util.TextWatcherAdapter;
import com.eup.codeopsstudio.databinding.FragmentTemplateBinding;
import com.eup.codeopsstudio.logger.Logger;
import com.eup.codeopsstudio.observers.ContextualLifecycleObserver;
import com.eup.codeopsstudio.util.BaseUtil;
import com.eup.codeopsstudio.viewmodel.FileViewModel;
import com.eup.codeopsstudio.viewmodel.MainViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.transition.MaterialFadeThrough;
import com.google.android.material.transition.MaterialSharedAxis;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TemplateFragment extends BottomSheetDialogFragment {

    public static final String LOG_TAG = "TemplateFragment";
    public static final String TAG = "TemplateFragment";
    
    private final OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            navigatePrevious();
        }
    };
    
    private Logger logger;
    private boolean previous; // used for backward navigation
    private Recents recentProjects;
    private FileViewModel fileViewModel;
    private TextInputLayout mNameLayout;
    private ProjectTemplateAdapter adapter;
    private MainViewModel mainViewModel;
    private FragmentTemplateBinding binding;
    private TextInputLayout mSaveLocationLayout;
    private ProjectTemplateModel mCurrentTemplate;
    private ContextualLifecycleObserver lifecycleObserver;
    
    public static TemplateFragment newInstance() {
        return new TemplateFragment();
    }
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new ProjectTemplateAdapter();
        logger = new Logger(Logger.LogClass.IDE);
        recentProjects = Recents.initialize(requireContext());
        fileViewModel = new ViewModelProvider(requireActivity()).get(FileViewModel.class);
        mainViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        final ActivityResultRegistry resultRegistry = requireActivity().getActivityResultRegistry();
        lifecycleObserver = new ContextualLifecycleObserver(requireContext(), resultRegistry, requireActivity());
        getLifecycle().addObserver(lifecycleObserver);
    }
    
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup viewgroup,
        @Nullable Bundle savedInstanceState) {
        binding = FragmentTemplateBinding.inflate(inflater, viewgroup, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        logger.attach(this);
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), onBackPressedCallback);

        binding.dynamicList.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.dynamicList.setHasFixedSize(true);
        binding.dynamicList.setAdapter(adapter);
        binding.footer.finish.setVisibility(View.GONE);
        binding.footer.finish.setOnClickListener(this::navigateNext);
        binding.footer.previous.setOnClickListener(v -> navigatePrevious());
        initalizeTemplateDetails();
        loadTemplates();
        binding.footer.previous.setVisibility(View.GONE);

        fileViewModel.monitorMessages(getViewLifecycleOwner(), this::logFileSelectionError);
        fileViewModel.observePickedFolders(getViewLifecycleOwner(), this::handlePickedFolder);
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
    
    private void initalizeTemplateDetails() {
        mNameLayout = binding.projectDetails.tilProjectName;
        mNameLayout.getEditText().addTextChangedListener(new TextWatcherAdapter() {
            @Override
            public void afterTextChanged(Editable editable) {
                verifyDetails(editable);
            }
        });

        mSaveLocationLayout = binding.projectDetails.tilSaveLocation;
        mSaveLocationLayout.getEditText()
                              .setText(PreferenceManager.getDefaultSharedPreferences(requireContext())
                              .getString(Constants.SharedPreferenceKeys.KEY_PROJECT_SAVE_PATH, requireContext()
                              .getExternalFilesDir("Projects")
                              .getAbsolutePath()));
        initializeSaveLocation();

        mSaveLocationLayout.getEditText().addTextChangedListener(new TextWatcherAdapter() {
            @Override
            public void afterTextChanged(Editable editable) {
                verifySaveLocation(editable);
            }
        });
    }

    private void initializeSaveLocation() {
        // mSaveLocationLayout.getEditText().setText(requireContext().getExternalFilesDir
        // ("Projects").getAbsolutePath());
        // mSaveLocationLayout.getEditText().setInputType(InputType.TYPE_NULL);
        mSaveLocationLayout.setEndIconOnClickListener(view -> {
            lifecycleObserver.pickFolder();
        });
    }
    
    private void loadTemplates() {
        TransitionManager.beginDelayedTransition((ViewGroup) requireView(), new MaterialFadeThrough());
        binding.loadingLayout.getRoot().setVisibility(View.VISIBLE);
        binding.dynamicList.setVisibility(View.GONE);
        
        Runnable completionTask = new Runnable() {
            @Override
            public void run() {
                AsyncTask.runNonCancelable(() -> {
                  File file = requireContext().getExternalFilesDir("templates");
                  File[] templateFiles = file.listFiles();
                  if (templateFiles == null) {
                      return Collections.emptyList();
                  }
                  
                  List<ProjectTemplateModel> templates = new ArrayList<>();
                  for (File child : templateFiles) {
                      ProjectTemplateModel template = ProjectTemplateModel.fromFile(child);
                      if (template != null) templates.add(template);
                  }
                  return templates;
                }, (templates, throwable) -> {
                  if (throwable != null) {
                     logger.e(TAG, getString(R.string.failed_retrieving_templates) + ": " + throwable.getMessage());
                  } else if (templates != null) {
                     TransitionManager.beginDelayedTransition((ViewGroup) requireView(), new MaterialFadeThrough());
                     binding.loadingLayout.getRoot().setVisibility(View.GONE);
                     binding.dynamicList.setVisibility(View.VISIBLE);
                     adapter.submitTemplateList(templates);
                     adapter.setOnTemplateClickListener((item, position) -> {
                        mCurrentTemplate = item;
                        navigateNext(binding.footer.finish);
                     });
                     adapter.setOnTemplateLongClickListener((v, model) -> {
                        var msg = new StringBuilder();
                        msg.append("Name: ").append(model.getName()).append("\n");
                        msg.append("Type: ").append(model.getProjectType()).append("\n");
                        msg.append("Released: ").append(model.getCreationDate()).append("\n");
                        msg.append("Description: ").append(model.getDescription()).append("\n");
                        msg.append("Version Code: ").append(model.getVersion()).append("\n");
                        msg.append("Version Name: ").append(model.getVersionName()).append("\n");

                        new MaterialAlertDialogBuilder(requireContext())
                            .setTitle(R.string.about_template).setMessage(msg.toString())
                            .setPositiveButton(R.string.cancel, null)
                            .setCancelable(true)
                            .show();
                        return true;
                    });
                  }
                });
            }
        };
        extractTemplatesIfRequired(completionTask);
    }
    
    private void createProjectAsync() {
        TransitionManager.beginDelayedTransition((ViewGroup) requireView(), new MaterialFadeThrough());
        binding.dynamicList.setVisibility(View.GONE);
        binding.projectDetails.getRoot().setVisibility(View.GONE);
        binding.loadingLayout.getRoot().setVisibility(View.VISIBLE);
        
        String savePath = mSaveLocationLayout.getEditText().getText().toString();
        
        AsyncTask.runNonCancelable(() -> {
            if (validateDetails()) {
                createProject();
                recentProjects.recordFolderCreation(new File(savePath));
            } else {
                requireActivity().runOnUiThread(this::showTemplatesDetails);
                return;
            }
            
            requireActivity().runOnUiThread(() -> openProject(new File(savePath)));
            return null;
        }, (result, throwable) -> {
            if (throwable != null) {
               logger.e(LOG_TAG, getString(R.string.project_creation_fail) + ":" + throwable.getMessage());
               showTemplatesDetails();
            }
        });
    }
    
    private void openProject(File file) {
        if (file != null) {
            mainViewModel.setTreeViewFragmentTreeDir(file);
        }
        dismiss();
    }
    
    @WorkerThread
    private void createProject() throws IOException {
        File projectRoot = new File(mSaveLocationLayout.getEditText().getText().toString());
        
        if (!projectRoot.exists() && !projectRoot.mkdirs()) {
            throw new IOException("Unable to create directory");
        }
        
        File sourcesDir = new File(mCurrentTemplate.getPath());
        FileUtils.copyDirectory(sourcesDir, projectRoot);
    }
    
    private void extractTemplatesIfRequired(Runnable completionTask) throws IOException {
        File templatesDir = requireContext().getExternalFilesDir("templates");
        File hashFile = new File(templatesDir, "hash");
        boolean needsExtraction = false;
        
        if (hashFile.exists()) {
           logger.d(LOG_TAG, getString(R.string.checking_templates));
           String currentAssetHash = calculateHash();
           String savedHash = FileUtils.readFileToString(hashFile, Charset.defaultCharset());
           
           if (currentAssetHash.equals(savedHash)) {
              logger.d(LOG_TAG, getString(R.string.templates_are_valid));
           } else {
              needsExtraction = true;
              logger.w(LOG_TAG, getString(R.string.msg_invalid_templates));
           }
        } else {
           needsExtraction = true;
        }
        
        if (needsExtraction) {
            final var asset = "templates.zip";
            int bufferSize = PreferencesUtils.getCurrentBufferSize();
            
            if (templatesDir.exists()) {
                FileUtils.deleteDirectory(templatesDir);
            }
            
            String parentPath = templatesDir.getParent();
            if (parentPath == null) {
               return;
            }
            
            File destDir = new File(parentPath);
            String templateHash = calculateHash();
            
            ZIPArchive.OnArchiveListener listener = new ZIPArchive.NoOPListener() {
                @Override
                public void onComplete(String message) {
                    AsyncTask.runNonCancelable(() -> {
                        if (!hashFile.createNewFile()) {
                           return false;
                        }
                        FileUtils.writeStringToFile(hashFile, templateHash, Charset.defaultCharset());
                        return true;
                    }, (templates, throwable) -> {
                        if (throwable != null) {
                           ILog.error(TAG, "Failed to create hash file", throwable);
                        } else {
                           if (completionTask != null) completionTask.run();
                           ILog.info(TAG, "Hash file created successfully");
                        }
                    });
                }
                
                @Override
                public void onError(Exception exception) {
                    ILog.error(TAG, "Template extraction failed", exception);
                    if (completionTask != null) completionTask.run();
                }
            };
            
            var archive = ZIPArchive.fromAssets(requireContext(), asset, destDir, bufferSize, listener);
            archive.unzip();
        } else {
            if (completionTask != null) completionTask.run();
        }
    }
    
    private String calculateHash() {
       try {
         InputStream newIs = requireContext().getAssets().open("templates.zip");
         return FileUtil.calculateMD5(PreferencesUtils.getCurrentBufferSize(), newIs);
       } catch (Exception e) {
         ILog.error(TAG, "Error calculating template hash", e);
         return "unknown-hash";
       }
    }
    
    private void handlePickedFolder(File file) {
        if (file != null) {
            String folderPath = file.getAbsolutePath();
            mSaveLocationLayout.getEditText().setText(folderPath);
            logger.d(LOG_TAG, getString(R.string.folder_selection_success));
        }
    }

    private void logFileSelectionError(Pair<Exception, String> pair) {
        String message = pair.second;
        if (message == null) return;

        logger.e(LOG_TAG,
            getString(R.string.folder_selection_error) + " [" + getString(R.string.cause) + "] "
                + message);
    }

    private void navigateNext(View view) {
        if (!previous) {
            showTemplatesDetails();
            previous = true;
        } else {
            createProjectAsync();
        }
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

    private boolean validateDetails() {
        requireActivity().runOnUiThread(() -> {
            verifyDetails(mNameLayout.getEditText().getText());
            verifySaveLocation(mSaveLocationLayout.getEditText().getText());
        });
        var templateName = mNameLayout.getEditText().getText();

        if (mNameLayout.isErrorEnabled() || (templateName != null
            && TextUtils.isEmpty(templateName))) {
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
        
        String basePath = PreferenceManager.getDefaultSharedPreferences(requireContext())
                .getString(Constants.SharedPreferenceKeys.KEY_PROJECT_SAVE_PATH, 
                        requireContext().getExternalFilesDir("Projects").getAbsolutePath());
        File file = new File(basePath, editable.toString());
        
        if (file.exists()) {
            mNameLayout.setError(getString(R.string.msg_folder_exists));
        } else {
            mNameLayout.setErrorEnabled(false);
            mSaveLocationLayout.getEditText().setText(file.getAbsolutePath());
        }
    }
    
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
}
