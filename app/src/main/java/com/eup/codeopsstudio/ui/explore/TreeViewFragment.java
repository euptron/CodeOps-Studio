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

package com.eup.codeopsstudio.ui.explore;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.FileObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.CallSuper;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.transition.ChangeBounds;
import androidx.transition.TransitionManager;

import com.eup.codeopsstudio.MainFragment;
import com.eup.codeopsstudio.R;
import com.eup.codeopsstudio.adapters.ActionAdapter;
import com.eup.codeopsstudio.adapters.holder.FileTreeViewHolder;
import com.eup.codeopsstudio.common.AsyncTask;
import com.eup.codeopsstudio.common.Constants;
import com.eup.codeopsstudio.common.ILog;
import com.eup.codeopsstudio.common.models.ProjectEvent;
import com.eup.codeopsstudio.common.util.FileUtil;
import com.eup.codeopsstudio.common.util.PreferencesUtils;
import com.eup.codeopsstudio.databinding.FragmentTreeviewBinding;
import com.eup.codeopsstudio.databinding.LayoutSheetListBinding;
import com.eup.codeopsstudio.domain.FileAction;
import com.eup.codeopsstudio.models.ActionModel;
import com.eup.codeopsstudio.models.logger.Logger;
import com.eup.codeopsstudio.observers.FileWatcher;
import com.eup.codeopsstudio.service.FileWatcherService;
import com.eup.codeopsstudio.service.FileWatcherServiceConnection;
import com.eup.codeopsstudio.tv.model.TreeNode;
import com.eup.codeopsstudio.tv.view.AndroidTreeView;
import com.eup.codeopsstudio.util.BaseUtil;
import com.eup.codeopsstudio.util.Wizard;
import com.eup.codeopsstudio.util.manager.FileManager;
import com.eup.codeopsstudio.viewmodel.MainViewModel;
import com.eup.codeopsstudio.viewmodel.SavedStateViewModel;
import com.eup.codeopsstudio.ui.explore.template.TemplateFragment;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * TODO: Extend AbstractFragment and call onViewLaidOut to defer heavy task like loading the
 * treeview because current impl causes lags since ui is not ye laid out and we try to load the
 * treeview data, also work on a new treeview
 */
public class TreeViewFragment extends Fragment implements TreeNode.TreeNodeClickListener,
    TreeNode.TreeNodeLongClickListener, FileWatcher.OnFileChangeListener {

    public static final String LOG_TAG = "TreeViewPane";
    public static final String TAG =
        com.eup.codeopsstudio.ui.explore.TreeViewFragment.class.getSimpleName();

    private boolean fileWatcherBindingRequested = false;
    private FragmentTreeviewBinding binding;
    private MainViewModel mMainViewModel;
    private FileManager fileManager;
    private Logger logger;
    private TreeNode rootNode;
    private AndroidTreeView treeView;
    private String fileTreeSavedState;
    private String lastOpenedFilePath;
    private SavedStateViewModel mSavedStateViewModel;
    private FileWatcherServiceConnection fileEventRelay;
    
    private final Runnable debouncedUpdate = this::performDebouncedUpdate;
    private static final long DEBOUNCE_DELAY_MS = 300; // 300ms delay
    private boolean updatePending = false;

    @Override
    public void onClick(TreeNode node, Object value) {
        var mFile = (File) value;

        if (mFile.isFile()) {
            MainFragment mainFragment = (MainFragment) requireActivity().getSupportFragmentManager()
                                                                        .findFragmentByTag(MainFragment.TAG);
            if (mainFragment != null) {
                mainFragment.openFileInPane(mFile);
            }
        } else if (mFile.isDirectory()) {
            if (node.isExpanded()) {
                collapseNode(node);
                return;
            }
            setLoading(node, true);
            listNode(node, () -> {
                setLoading(node, false);
                expandNode(node);
            });
        }
    }

    public void listNode(TreeNode parent, Runnable post) {
        parent.getChildren().clear();
        parent.setExpanded(false);

        AsyncTask.runNonCancelable(() -> {
            addChildrenToNode(parent);
            TreeNode currentNode = parent;
            // expand dir with only 1 folder
            while (currentNode.size() == 1) {
                currentNode = currentNode.childAt(0);
                if (!currentNode.getValue().isDirectory()) {
                    break;
                }
                addChildrenToNode(currentNode);
                currentNode.setExpanded(true);
            }
            return null;
        }, (result) -> post.run());
    }

    public void addChildrenToNode(@NonNull TreeNode parent) {
        List<File> files = toSortedList(FileUtil.listFiles(parent.getValue()));

        for (File file : files) {
            var child = new TreeNode(file);
            child.setViewHolder(new FileTreeViewHolder(requireContext()));
            parent.addChild(child);
        }
    }

    public static List<File> toSortedList(File[] files) {
        Executor executor = Executors.newFixedThreadPool(Runtime.getRuntime()
                                                                .availableProcessors());
        return Stream.of(files).sorted(FileManager.DIR_FIRST_SORT)
                     .map(file -> CompletableFuture.supplyAsync(() -> file, executor)).toList()
                     .stream().map(CompletableFuture::join).collect(Collectors.toList());
    }

    public void expandNode(TreeNode node) {
        if (treeView == null) return;
        ChangeBounds cb = new ChangeBounds();
        cb.setDuration(Constants.TOGGLE_TREENODE_ANIM_TIME);
        TransitionManager.beginDelayedTransition(binding.fileTreeArea, cb);
        treeView.expandNode(node);
        updateToggle(node);
    }

    public void collapseNode(TreeNode node) {
        if (treeView == null) return;
        ChangeBounds cb = new ChangeBounds();
        cb.setDuration(Constants.TOGGLE_TREENODE_ANIM_TIME);
        TransitionManager.beginDelayedTransition(binding.fileTreeArea, cb);
        treeView.collapseNode(node);
        updateToggle(node);
    }

    private void updateToggle(TreeNode node) {
        if (node.getViewHolder() instanceof FileTreeViewHolder) {
            ((FileTreeViewHolder) node.getViewHolder()).rotateChevron(node.isExpanded());
        }
    }

    public void setLoading(TreeNode node, boolean loading) {
        if (node.getViewHolder() instanceof FileTreeViewHolder) {
            ((FileTreeViewHolder) node.getViewHolder()).setLoading(loading);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fileManager = new FileManager(requireContext(), requireActivity());
        logger      = new Logger(Logger.LogClass.IDE);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup viewgroup,
        @Nullable Bundle savedInstanceState) {
        binding = FragmentTreeviewBinding.inflate(inflater, viewgroup, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mMainViewModel       = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        mSavedStateViewModel =
            new ViewModelProvider(requireActivity()).get(SavedStateViewModel.class);
        logger.attach(requireActivity());
        mMainViewModel.observeSetTreeViewFragmentFile(getViewLifecycleOwner(),
            this::populateFileTree);

        mSavedStateViewModel.getTreeViewFragmentTreeState()
                            .observe(requireActivity(), savedState -> fileTreeSavedState =
                                savedState);

        binding.folderOptions.setOnClickListener(v -> {
            if (rootNode != null) {
                displayBottomSheetOnClickFolderOptions();
            }
        });
        binding.treeOpenFolder.setOnClickListener(v -> {
            MainFragment mainFragment = (MainFragment) requireActivity().getSupportFragmentManager()
                                                                        .findFragmentByTag(MainFragment.TAG);
            if (mainFragment != null) {
                mainFragment.openFolderFromManager();
            }
        });
        binding.chooseTemplate.setOnClickListener(v -> chooseTemplates());
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (treeView != null) {
            mSavedStateViewModel.saveTreeViewFragmentTreeState(treeView.getSaveState());
        }
    }

    @Override
    @MainThread
    @CallSuper
    public void onStop() {
        super.onStop();
        if (rootNode != null) {
            // save as last opened
            var projectDir = rootNode.getValue().getAbsolutePath();
            PreferencesUtils.getLastOpenedProjectPreferences().edit()
                            .putString(Constants.SharedPreferenceKeys.KEY_LAST_OPENED_PROJECT,
                                projectDir)
                            .apply();
        }
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding  = null;
        treeView = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        AsyncTask.cancelRunLater(debouncedUpdate);
        unbindFileWatcherService();
    }
    
    private void unbindFileWatcherService() {
        if (fileEventRelay != null) {
            if (fileEventRelay.isConnected() || !fileWatcherBindingRequested) {
                fileEventRelay.removeListenerFromService();
                requireActivity().unbindService(fileEventRelay);
            }
            fileEventRelay = null;
        }
        fileWatcherBindingRequested = false;
    }
    
    @Override
    public void onFileChanged(int event, String path) {
        if (rootNode == null || rootNode.getValue() == null) {
            return;
        }
        
        switch (event) {
            case FileObserver.DELETE_SELF:
                File deletedDir = rootNode.getValue();
                
                if (deletedDir.getAbsolutePath().equals(path)) {
                    String deletedDirName = deletedDir.getName();
                    String delete_msg = "The folder " + deletedDirName + " has been deleted";
                    showFileWatcherDialog(deletedDirName, delete_msg, () -> doCloseFolder(true));
                    logger.i(LOG_TAG, delete_msg + ", this action was probably executed by another app");
                } else {
                    // A subfolder was deleted - reload the tree view
                    updateFileTree(rootNode.getValue());
                }
                break;
            case FileObserver.MOVE_SELF:
                File movedDir = rootNode.getValue();
                
                if (movedDir.getAbsolutePath().equals(path)) {
                    String movedDirName = movedDir.getName();
                    String moved_msg = "The folder " + movedDirName + " has been moved to another location";
                    showFileWatcherDialog(movedDirName, moved_msg, () -> doCloseFolder(true));
                    logger.i(LOG_TAG, moved_msg + ", this action was probably executed by another app");
                } else {
                    // A subfolder was moved - reload the tree view
                    updateFileTree(rootNode.getValue());
                }
                break;
            case FileObserver.MOVED_FROM, FileObserver.MOVED_TO, FileObserver.CREATE,
                 FileObserver.DELETE:
                 scheduleDebouncedUpdate();
                break;
            case FileObserver.MODIFY:
                // Ignore -- to noisy
                break;
        }
    }

    @Override
    public boolean onLongClick(TreeNode node, Object value) {
        displayBottomSheetOnLongClick(node);
        return true;
    }
    
    private void scheduleDebouncedUpdate() {
        if (updatePending) {
            AsyncTask.cancelRunLater(debouncedUpdate);
        }
        updatePending = true;
        AsyncTask.runLaterOnUiThread(debouncedUpdate, DEBOUNCE_DELAY_MS);
    }

    private void performDebouncedUpdate() {
        updatePending = false;
        if (rootNode != null && rootNode.getValue() != null) {
            updateFileTree(rootNode.getValue());
        }
    }
    
    public void addNewChild(TreeNode parent, File file) {
        var newNode = new TreeNode(file);
        newNode.setViewHolder(new FileTreeViewHolder(requireContext()));
        parent.addChild(newNode);
    }

    public void doCloseFolder(boolean removePrefsAndTreeState) {
        if (rootNode != null) {
            rootNode.getChildren().clear();
            rootNode = null;
            treeView = null;

            mMainViewModel.setToolbarSubTitle(null);
            if (removePrefsAndTreeState) {
                PreferencesUtils.clearPreference(PreferencesUtils.getLastOpenedProjectPreferences(), Constants.SharedPreferenceKeys.KEY_LAST_OPENED_PROJECT);
                fileTreeSavedState = null;
                unbindFileWatcherService();
            }

            EventBus.getDefault().post(new ProjectEvent(null));
            updateViewsVisibility();
        }
    }

    public static com.eup.codeopsstudio.ui.explore.TreeViewFragment newInstance() {
        return new com.eup.codeopsstudio.ui.explore.TreeViewFragment();
    }

    public void updateViewsVisibility() {
        if (rootNode == null) {
            binding.folderName.setText(R.string.no_folder_opened);
            binding.noFolderLin.setVisibility(View.VISIBLE);
            binding.fileTreeArea.setVisibility(View.GONE);
            binding.folderOptions.setVisibility(View.INVISIBLE);
        } else {
            binding.folderName.setText(rootNode.getValue().getName());
            binding.noFolderLin.setVisibility(View.GONE);
            binding.fileTreeArea.setVisibility(View.VISIBLE);
            binding.folderOptions.setVisibility(View.VISIBLE);
        }
    }

    private ActionModel action(int iconRes, int titleRes) {
        return new ActionModel(iconRes, getString(titleRes));
    }
    
    private void bindFileWatcherService(File file) {
        // Don't unbind immediately - might be configuration change
        if (fileEventRelay != null && fileEventRelay.isConnected()) {
            // Already bound, just update the file to watch
            fileEventRelay.setFileToWatch(file);
            return;
        }
        
        if (fileEventRelay != null) {
           unbindFileWatcherService();
        }
    
        fileEventRelay = new FileWatcherServiceConnection(this);
        fileEventRelay.setFileToWatch(file);
        Intent intent = new Intent(requireContext(), FileWatcherService.class);
        
        try {
          requireActivity().startService(intent);
          fileWatcherBindingRequested = true;
          
          if (requireActivity().bindService(intent, fileEventRelay, Context.BIND_IMPORTANT)) {
              ILog.debug(LOG_TAG, "Binding to FileWatcherService requested");
          } else {
              fileWatcherBindingRequested = false;
              ILog.error(LOG_TAG, "Failed to bind to FileWatcherService");
          }
        } catch (SecurityException e) {
          fileWatcherBindingRequested = false;
          ILog.error(LOG_TAG, "Security exception binding to service", e);
        }
    }

    private List<ActionModel> buildOptions(@NonNull File file, boolean isRoot) {
        List<ActionModel> listItems = new ArrayList<>();

        if (isRoot) listItems.add(action(R.drawable.ic_refresh, R.string.refresh));

        listItems.add(action(R.drawable.ic_content_copy, R.string.copy_path));
        listItems.add(action(R.drawable.ic_delete_outline, R.string.delete));

        if (file.isDirectory()) {
            listItems.add(action(R.drawable.ic_file_plus_outline, R.string.new_file));
            listItems.add(action(R.drawable.ic_folder_plus_outline, R.string.new_folder));
        }

        listItems.add(action(R.drawable.ic_pencil_outline, R.string.rename));

        if (isRoot) {
            listItems.add(action(R.drawable.ic_close, R.string.close));
        }
        // TODO: Add the terminal support and support the below
        // listItems.add(2, new ActionModel(R.drawable.ic_powershell,
        // getString(R.string.open_terminal)));
        return listItems;
    }

    private void chooseTemplates() {
        TemplateFragment.newInstance().show(getChildFragmentManager(), null);
    }

    private void displayBottomSheetOnClickFolderOptions() {
        if (rootNode != null) {
            showOptionsBottomSheet(rootNode.getValue(), rootNode, true);
        }
    }

    private void displayBottomSheetOnLongClick(@NonNull TreeNode node) {
        showOptionsBottomSheet(node.getValue(), node, false);
    }

    private TreeNode effectiveNode(@Nullable TreeNode node) {
        return node != null ? node : rootNode;
    }

    private void handleOptionClick(ActionModel model, File file, @Nullable TreeNode node,
        boolean isRoot) {
        if (!file.exists()) {
            ILog.warning(TAG,
                "Cannot handle options file/folder " + file.getAbsolutePath() + "does not exist");
            return;
        }

        String label = model.getTitle();

        if (label.equals(getString(R.string.refresh)) && isRoot) {
            updateFileTree(rootNode.getValue());
        } else if (label.equals(getString(R.string.copy_path))) {
            BaseUtil.copyToClipBoard(file.getAbsolutePath(), true);
        } else if (label.equals(getString(R.string.delete))) {
            FileAction action = file.isFile() ? FileAction.DELETE_FILE : FileAction.DELETE_FOLDER;
            fileManager.startFileTask(action, file, object -> {
                if (object instanceof Boolean && (Boolean) object) {
                    if (isRoot) {
                        doCloseFolder(true);
                    } else if (node != null) {
                        treeView.removeNode(node);
                    }
                }
            });
        } else if (label.equals(getString(R.string.new_file))) {
            fileManager.startFileTask(FileAction.CREATE_FILE, file, object -> {
                if (object instanceof File newFile) {
                    addNewChild(effectiveNode(node), newFile);
                    expandNode(effectiveNode(node));
                }
            });
        } else if (label.equals(getString(R.string.new_folder))) {
            fileManager.startFileTask(FileAction.CREATE_FOLDER, file, object -> {
                if (object instanceof File newFolder) {
                    addNewChild(effectiveNode(node), newFolder);
                    expandNode(effectiveNode(node));
                }
            });
        } else if (label.equals(getString(R.string.rename))) {
            FileAction action = file.isFile() ? FileAction.RENAME_FILE : FileAction.RENAME_FOLDER;
            fileManager.startFileTask(action, file, object -> {
                if (object instanceof File renamed) {
                    if (isRoot) {
                        updateFileTree(renamed);
                    } else {
                        if (file.isFile()) {
                            updateFileTree(rootNode.getValue());
                        } else {
                            expandNode(node != null ? node.getParent() : rootNode);
                        }
                    }
                }
            });
        } else if (label.equals(getString(R.string.close)) && isRoot) {
            new MaterialAlertDialogBuilder(requireContext()).setTitle(R.string.close_project_title)
                                                            .setMessage(R.string.close_project_message)
                                                            .setPositiveButton(R.string.yes, (d,
                                                                which) -> doCloseFolder(true))
                                                            .setNegativeButton(R.string.no, null)
                                                            .setCancelable(false).show();
        }
    }

    /**
     * Parse a directory and it's contents into the file tree
     *
     * @param dir The directory
     */
    private void populateFileTree(File dir) {
        if (getContext() == null || dir == null) return;

        if (!Wizard.isEmpty(lastOpenedFilePath)
            && !lastOpenedFilePath.equals(dir.getAbsolutePath())) {
            fileTreeSavedState = null; // clear state if dir is different
        } else if (Wizard.isEmpty(lastOpenedFilePath)) {
            lastOpenedFilePath = dir.getAbsolutePath();
        }

        doCloseFolder(false);
        mMainViewModel.setToolbarSubTitle(FileUtil.getFileNameWithoutExtension(dir));
        rootNode = TreeNode.root(dir);
        rootNode.setViewHolder(new FileTreeViewHolder(requireContext()));

        binding.filetreeProgressIndicator.setVisibility(View.VISIBLE);

        listNode(rootNode, () -> {
            treeView = new AndroidTreeView(requireContext(), rootNode, R.drawable.base_ripple);
            treeView.setDefaultNodeClickListener(this);
            treeView.setDefaultNodeLongClickListener(this);

            if (treeView != null) {
                var view = treeView.getView();
                binding.fileTreeArea.removeAllViews();
                binding.fileTreeArea.addView(view);
                treeView.setUseAutoToggle(false);
                view.setNestedScrollingEnabled(false);
                binding.filetreeProgressIndicator.setVisibility(View.GONE);

                EventBus.getDefault().post(new ProjectEvent(dir));
                tryRestoreSavedState();
            }
        });
        updateViewsVisibility();
        bindFileWatcherService(dir);
    }

    private void restoreNodeState(TreeNode node, Set<String> openNodes) {
        for (TreeNode child : node.getChildren()) {
            if (openNodes.contains(child.getPath())) {
                listNode(child, () -> {
                    expandNode(child);
                    restoreNodeState(child, openNodes);
                });
            }
        }
    }

    private void showFileWatcherDialog(String title, String message, Runnable onConfirm) {
        new MaterialAlertDialogBuilder(requireContext()).setTitle(title).setMessage(message)
                                                        .setPositiveButton(R.string.ok, (d,
                                                            which) -> onConfirm.run())
                                                        .setCancelable(false).show();
    }

    private void showOptionsBottomSheet(@NonNull File file, @Nullable TreeNode node,
        boolean isRoot) {
        var bottomSheetDialog = new BottomSheetDialog(requireActivity());
        var bind = LayoutSheetListBinding.inflate(getLayoutInflater());
        bottomSheetDialog.setContentView(bind.getRoot());

        var adapter = new ActionAdapter();
        adapter.submitList(buildOptions(file, isRoot));
        adapter.setOnItemClickListener(model -> {
            handleOptionClick(model, file, node, isRoot);
            bottomSheetDialog.dismiss();
        });

        bind.title.setText(file.getName());
        bind.summary.setText(file.getAbsolutePath());
        bind.sheetList.setLayoutManager(new LinearLayoutManager(requireContext()));
        bind.sheetList.setHasFixedSize(true);
        bind.sheetList.setAdapter(adapter);

        bottomSheetDialog.show();
    }

    private void tryRestoreSavedState() {
        if (fileTreeSavedState != null) {
            treeView.collapseAll();
            String[] openNodes = fileTreeSavedState.split(AndroidTreeView.NODES_PATH_SEPARATOR);
            restoreNodeState(rootNode, new HashSet<>(Arrays.asList(openNodes)));
        }
    }

    /**
     * Updates the file tree view with the contents of the specified directory.
     * <p>
     * The current tree state is preserved before repopulating, so that any expanded
     * or selected nodes can be restored after the update.
     * </p>
     *
     * @param dir The directory whose contents should be displayed in the file tree.
     */
    private void updateFileTree(@NonNull File dir) {
        if (treeView != null) {
            fileTreeSavedState = treeView.getSaveState();
            populateFileTree(dir);
        }
    }
}