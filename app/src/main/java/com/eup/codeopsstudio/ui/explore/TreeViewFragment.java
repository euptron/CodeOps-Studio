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
import com.eup.codeopsstudio.adapters.ActionAdapter;
import com.eup.codeopsstudio.adapters.holder.FileTreeViewHolder;
import com.eup.codeopsstudio.common.AsyncTask;
import com.eup.codeopsstudio.common.Constants;
import com.eup.codeopsstudio.common.models.ProjectEvent;
import com.eup.codeopsstudio.common.util.FileUtil;
import com.eup.codeopsstudio.common.util.PreferencesUtils;
import com.eup.codeopsstudio.databinding.FragmentTreeviewBinding;
import com.eup.codeopsstudio.domain.FileAction;
import com.eup.codeopsstudio.models.ActionModel;
import com.eup.codeopsstudio.models.logger.Logger;
import com.eup.codeopsstudio.observers.FileWatcher;
import com.eup.codeopsstudio.R;
import com.eup.codeopsstudio.databinding.LayoutSheetListBinding;
import com.eup.codeopsstudio.service.FileWatcherService;
import com.eup.codeopsstudio.service.FileWatcherServiceConnection;
import com.eup.codeopsstudio.tv.model.TreeNode;
import com.eup.codeopsstudio.tv.view.AndroidTreeView;
import com.eup.codeopsstudio.util.BaseUtil;
import com.eup.codeopsstudio.util.Wizard;
import com.eup.codeopsstudio.util.manager.FileManager;
import com.eup.codeopsstudio.viewmodel.MainViewModel;
import com.eup.codeopsstudio.viewmodel.SavedStateViewModel;
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

public class TreeViewFragment extends Fragment implements TreeNode.TreeNodeClickListener,
    TreeNode.TreeNodeLongClickListener, FileWatcher.OnFileChangeListener {

    public static final String LOG_TAG = "FileTreePane";
    public static final String TAG = TreeViewFragment.class.getSimpleName();

    private boolean isFileWatcherBound = false;
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

    public static TreeViewFragment newInstance() {
        return new TreeViewFragment();
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
        logger.attach(requireActivity() /*shared activity scope*/);
        mMainViewModel.observeSetTreeViewFragmentFile(getViewLifecycleOwner(),
            this::populateFileTree);

        mSavedStateViewModel
            .getTreeViewFragmentTreeState()
            .observe(requireActivity(), savedState -> fileTreeSavedState = savedState);

        binding.folderOptions.setOnClickListener(v -> {
            if (rootNode != null) {
                displayBottomSheetOnClickFolderOptions();
            }
        });
        binding.treeOpenFolder.setOnClickListener(v -> {
            MainFragment mainFragment = (MainFragment) requireActivity()
                .getSupportFragmentManager()
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
            var projectDir = rootNode
                .getValue()
                .getAbsolutePath();
            PreferencesUtils
                .getLastOpenedProjectPreferences()
                .edit()
                .putString(Constants.SharedPreferenceKeys.KEY_LAST_OPENED_PROJECT, projectDir)
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
        unbindFileWatcherService();
    }

    private void unbindFileWatcherService() {
        if (isFileWatcherBound && fileEventRelay != null) {
            requireActivity().unbindService(fileEventRelay);
            isFileWatcherBound = false;
            fileEventRelay     = null;
        }
    }

    @Override
    public void onClick(TreeNode node, Object value) {
        var mFile = (File) value;

        if (mFile.isFile()) {
            MainFragment mainFragment = (MainFragment) requireActivity()
                .getSupportFragmentManager()
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
        parent
            .getChildren()
            .clear();
        parent.setExpanded(false);

        AsyncTask.runNonCancelable(() -> {
            addChildrenToNode(parent);
            TreeNode currentNode = parent;
            // expand dir with only 1 folder
            while (currentNode.size() == 1) {
                currentNode = currentNode.childAt(0);
                if (!currentNode
                    .getValue()
                    .isDirectory()) {
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
        Executor executor = Executors.newFixedThreadPool(Runtime
            .getRuntime()
            .availableProcessors());
        return Stream
            .of(files)
            .sorted(FileManager.DIR_FIRST_SORT)
            .map(file -> CompletableFuture.supplyAsync(() -> file, executor))
            .toList()
            .stream()
            .map(CompletableFuture::join)
            .collect(Collectors.toList());
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
    public boolean onLongClick(TreeNode node, Object value) {
        displayBottomSheetOnLongClick(node);
        return true;
    }

    @Override
    public void onFileChanged(int event, String path) {
        switch (event) {
            case FileObserver.CREATE:
                refreshFileTree();
                break;
            case FileObserver.DELETE:
                refreshFileTree();
                break;
            case FileObserver.DELETE_SELF:
                File deletedDir = rootNode.getValue();
                String deletedDirName = deletedDir.getName();
                String delete_msg = "The folder " + deletedDirName + " has been deleted";
                new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(deletedDirName)
                    .setMessage(delete_msg)
                    .setPositiveButton(R.string.ok, (d, which) -> doCloseFolder(true))
                    .setCancelable(false)
                    .show();
                logger.i(LOG_TAG,
                    delete_msg + ", this action was probably executed by another app");
                break;
            case FileObserver.MOVE_SELF:
                File movedDir = rootNode.getValue();
                String movedDirName = movedDir.getName();
                String moved_msg =
                    "The folder " + movedDirName + " has been moved to another location";
                new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(movedDirName)
                    .setMessage(moved_msg)
                    .setPositiveButton(R.string.ok, (d, which) -> doCloseFolder(true))
                    .setCancelable(false)
                    .show();
                logger.i(LOG_TAG, moved_msg + ", this action was probably executed by another app");
                break;
            case FileObserver.MOVED_FROM:
                refreshFileTree();
                break;
            case FileObserver.MOVED_TO:
                refreshFileTree();
                break;
            case FileObserver.MODIFY:
                // Ignore
                break;
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

        mMainViewModel.setToolbarSubTitle(FileUtil.getFileNameWithoutExtension(dir));
        doCloseFolder(false);
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

                EventBus
                    .getDefault()
                    .post(new ProjectEvent(dir));
                tryRestoreSavedState();
            }
        });
        updateViewsVisibility();
        bindFileWatcherService(dir);
    }

    public void addNewChild(TreeNode parent, File file) {
        var newNode = new TreeNode(file);
        newNode.setViewHolder(new FileTreeViewHolder(requireContext()));
        parent.addChild(newNode);
    }

    private void tryRestoreSavedState() {
        if (fileTreeSavedState != null) {
            treeView.collapseAll();
            String[] openNodes = fileTreeSavedState.split(AndroidTreeView.NODES_PATH_SEPARATOR);
            restoreNodeState(rootNode, new HashSet<>(Arrays.asList(openNodes)));
        }
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

    private void refreshFileTree() {
        refreshFileTree(rootNode.getValue());
    }

    /**
     * Refreshes the file tree with a new directory
     *
     * @param dir The dir to populate into the file tree
     */
    private void refreshFileTree(File dir) {
        if (treeView != null) {
            fileTreeSavedState = treeView.getSaveState();
            populateFileTree(dir);
        }
    }

    public void doCloseFolder(boolean removePrefsAndTreeState) {
        if (rootNode != null) {
            rootNode
                .getChildren()
                .clear();
            rootNode = null;
            treeView = null;

            mMainViewModel.setToolbarSubTitle(null);
            if (removePrefsAndTreeState) {
                PreferencesUtils.clearPreference(PreferencesUtils.getLastOpenedProjectPreferences(), Constants.SharedPreferenceKeys.KEY_LAST_OPENED_PROJECT);
                fileTreeSavedState = null;
                unbindFileWatcherService();
            }

            EventBus
                .getDefault()
                .post(new ProjectEvent(null));
            updateViewsVisibility();
        }
    }

    public void updateViewsVisibility() {
        if (rootNode == null) {
            binding.folderName.setText(R.string.no_folder_opened);
            binding.noFolderLin.setVisibility(View.VISIBLE);
            binding.fileTreeArea.setVisibility(View.GONE);
            binding.folderOptions.setVisibility(View.INVISIBLE);
        } else {
            binding.folderName.setText(rootNode
                .getValue()
                .getName());
            binding.noFolderLin.setVisibility(View.GONE);
            binding.fileTreeArea.setVisibility(View.VISIBLE);
            binding.folderOptions.setVisibility(View.VISIBLE);
        }
    }

    private void chooseTemplates() {
        TemplateFragment
            .newInstance()
            .show(getChildFragmentManager(), null);
    }

    private List<ActionModel> getFolderOptionsList(File file) {
        List<ActionModel> listItems = new ArrayList<>();
        listItems.add(new ActionModel(R.drawable.ic_content_copy, getString(R.string.copy_path)));
        listItems.add(new ActionModel(R.drawable.ic_delete_outline, getString(R.string.delete)));
        if (file.isDirectory()) {
            listItems.add(new ActionModel(R.drawable.ic_file_plus_outline,
                getString(R.string.new_file)));
            listItems.add(new ActionModel(R.drawable.ic_folder_plus_outline,
                getString(R.string.new_folder)));
        }
        listItems.add(new ActionModel(R.drawable.ic_pencil_outline, getString(R.string.rename)));
        return listItems;
    }

    private List<ActionModel> getFolderOptionsList() {
        List<ActionModel> listItems = new ArrayList<>();
        listItems.add(new ActionModel(R.drawable.ic_refresh, getString(R.string.refresh)));
        listItems.add(new ActionModel(R.drawable.ic_content_copy, getString(R.string.copy_path)));
        listItems.add(new ActionModel(R.drawable.ic_delete_outline, getString(R.string.delete)));
        listItems.add(new ActionModel(R.drawable.ic_file_plus_outline,
            getString(R.string.new_file)));
        listItems.add(new ActionModel(R.drawable.ic_folder_plus_outline,
            getString(R.string.new_folder)));
        listItems.add(new ActionModel(R.drawable.ic_pencil_outline, getString(R.string.rename)));
        listItems.add(new ActionModel(R.drawable.ic_close, getString(R.string.close)));
        // listItems.add(2, new ActionModel(R.drawable.ic_powershell,
        // getString(R.string.open_terminal)));
        return listItems;
    }

    private void displayBottomSheetOnClickFolderOptions() {
        var rootDir = rootNode.getValue();
        var bottomSheetDialog = new BottomSheetDialog(requireActivity());
        var bind = LayoutSheetListBinding.inflate(getLayoutInflater());
        bottomSheetDialog.setContentView(bind.getRoot());
        // Get the options list
        var adapter = new ActionAdapter();
        adapter.submitList(getFolderOptionsList());
        adapter.setOnItemClickListener(model -> {
            String label = model.getTitle();
            if (label == getString(R.string.refresh)) {
                refreshFileTree(rootNode.getValue());
            } else if (label == getString(R.string.copy_path)) {
                BaseUtil.copyToClipBoard(rootDir.getAbsolutePath(), true);
            } else if (label == getString(R.string.delete)) {
                fileManager.startFileTask(FileAction.DELETE_FOLDER,
                    new File(rootDir.getAbsolutePath()), object -> {
                    if (object instanceof Boolean) {
                        if ((Boolean) object) doCloseFolder(true);
                    }
                });
            } else if (label == getString(R.string.new_file)) {
                fileManager.startFileTask(FileAction.CREATE_FILE,
                    new File(rootDir.getAbsolutePath()), object -> {
                    if (object != null && object instanceof File newFile) {
                        addNewChild(rootNode, newFile);
                        expandNode(rootNode);
                    }
                });
            } else if (label == getString(R.string.new_folder)) {
                fileManager.startFileTask(FileAction.CREATE_FOLDER,
                    new File(rootDir.getAbsolutePath()), object -> {
                    if (object != null && object instanceof File newFolder) {
                        addNewChild(rootNode, newFolder);
                        expandNode(rootNode);
                    }
                });
            } else if (label == getString(R.string.rename)) {
                fileManager.startFileTask(FileAction.RENAME_FOLDER,
                    new File(rootDir.getAbsolutePath()), object -> {
                    if (object != null && object instanceof File renamedFolder) {
                        refreshFileTree(renamedFolder);
                    }
                });
            } else if (label == getString(R.string.close)) {
                new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.close_project_title)
                    .setMessage(R.string.close_project_message)
                    .setPositiveButton(R.string.yes, (d, which) -> {
                        doCloseFolder(true);
                    })
                    .setNegativeButton(R.string.no, null)
                    .setCancelable(false)
                    .show();
            }
            bottomSheetDialog.dismiss();
        });
        bind.title.setText(rootDir.getName());
        bind.summary.setText(rootDir.getAbsolutePath());
        bind.sheetList.setLayoutManager(new LinearLayoutManager(requireContext()));
        bind.sheetList.setHasFixedSize(true);
        bind.sheetList.setAdapter(adapter);
        bottomSheetDialog.show();
    }

    private void displayBottomSheetOnLongClick(TreeNode node) {
        var treeFile = node.getValue();
        var bottomSheetDialog = new BottomSheetDialog(getActivity());
        var bind = LayoutSheetListBinding.inflate(getLayoutInflater());
        bottomSheetDialog.setContentView(bind.getRoot());
        List<ActionModel> optionsList = getFolderOptionsList(treeFile);
        var adapter = new ActionAdapter();
        adapter.submitList(optionsList);
        adapter.setOnItemClickListener(model -> {
            var label = model.getTitle();
            if (label == getString(R.string.copy_path)) {
                BaseUtil.copyToClipBoard(treeFile.getAbsolutePath(), true);
            } else if (label == getString(R.string.delete)) {
                if (treeFile.isFile() && treeFile.exists()) {
                    fileManager.startFileTask(FileAction.DELETE_FILE,
                        new File(treeFile.getAbsolutePath()), object -> {
                        if (object != null && object instanceof Boolean) {
                            if ((Boolean) object) treeView.removeNode(node);
                        }
                    });
                } else if (treeFile.isDirectory() && treeFile.exists()) {
                    fileManager.startFileTask(FileAction.DELETE_FOLDER,
                        new File(treeFile.getAbsolutePath()), object -> {
                        if (object != null && object instanceof Boolean) {
                            if ((Boolean) object) treeView.removeNode(node);
                        }
                    });
                }
            } else if (label == getString(R.string.new_file)) {
                fileManager.startFileTask(FileAction.CREATE_FILE,
                    new File(treeFile.getAbsolutePath()), object -> {
                    if (object != null && object instanceof File) {
                        addNewChild(node, (File) object);
                        expandNode(node);
                    }
                });
            } else if (label == getString(R.string.new_folder)) {
                fileManager.startFileTask(FileAction.CREATE_FOLDER,
                    new File(treeFile.getAbsolutePath()), object -> {
                    if (object != null && object instanceof File) {
                        addNewChild(node, (File) object);
                        expandNode(node);
                    }
                });
            } else if (label == getString(R.string.rename)) {
                if (treeFile.isFile()) {
                    fileManager.startFileTask(FileAction.RENAME_FILE,
                        new File(treeFile.getAbsolutePath()), object -> {
                        if (object != null && object instanceof File renamedFile) {
                            if (renamedFile != null) {
                                refreshFileTree();
                            }
                        }
                    });
                } else {
                    fileManager.startFileTask(FileAction.RENAME_FOLDER,
                        new File(treeFile.getAbsolutePath()), object -> {
                        if (object != null && object instanceof File renamedFolder) {
                            if (renamedFolder != null) {
                                expandNode(node.getParent());
                            }
                        }
                    });
                }
            }
            bottomSheetDialog.dismiss();
        });
        bind.title.setText(treeFile.getName());
        bind.summary.setText(treeFile.getAbsolutePath());
        bind.sheetList.setLayoutManager(new LinearLayoutManager(requireContext()));
        bind.sheetList.setHasFixedSize(true);
        bind.sheetList.setAdapter(adapter);
        bottomSheetDialog.show();
    }

    private void bindFileWatcherService(File file) {
        unbindFileWatcherService(); // unbind previous service if any

        fileEventRelay = new FileWatcherServiceConnection(this);
        fileEventRelay.setFileToWatch(file);
        Intent intent = new Intent(requireActivity(), FileWatcherService.class);
        requireActivity().startService(intent);

        if (requireActivity().bindService(intent, fileEventRelay, Context.BIND_IMPORTANT)) {
            isFileWatcherBound = true;
        } else {
            logger.e(LOG_TAG, "Error: The requested service doesn't "
                + "exist, or this client isn't allowed access to it.");
        }
    }
}
