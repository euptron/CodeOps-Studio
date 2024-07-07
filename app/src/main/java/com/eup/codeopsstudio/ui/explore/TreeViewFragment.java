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
 
   package com.eup.codeopsstudio.ui.explore;

import android.os.Bundle;
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
import com.blankj.utilcode.util.FileUtils;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.eup.codeopsstudio.MainFragment;
import com.eup.codeopsstudio.adapters.ActionAdapter;
import com.eup.codeopsstudio.common.AsyncTask;
import com.eup.codeopsstudio.common.Constants;
import com.eup.codeopsstudio.common.models.ProjectEvent;
import com.eup.codeopsstudio.common.util.PreferencesUtils;
import com.eup.codeopsstudio.databinding.FragmentTreeviewBinding;
import com.eup.codeopsstudio.file.FileAction;
import com.eup.codeopsstudio.file.FileManager;
import com.eup.codeopsstudio.logging.Logger;
import com.eup.codeopsstudio.models.ActionModel;
import com.eup.codeopsstudio.res.R;
import com.eup.codeopsstudio.res.databinding.LayoutSheetListBinding;
import com.eup.codeopsstudio.tv.model.TreeNode;
import com.eup.codeopsstudio.tv.view.AndroidTreeView;
import com.eup.codeopsstudio.ui.explore.holder.FileTreeViewHolder;
import com.eup.codeopsstudio.ui.explore.template.TemplateFragment;
import com.eup.codeopsstudio.util.BaseUtil;
import com.eup.codeopsstudio.viewmodel.MainViewModel;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.greenrobot.eventbus.EventBus;

public class TreeViewFragment extends Fragment
    implements TreeNode.TreeNodeClickListener, TreeNode.TreeNodeLongClickListener {

  public static final String LOG_TAG = "FileTreeFragment";
  public static final String KEY_STORED_TREE_STATE = "treeState";
  public static final String TAG = TreeViewFragment.class.getSimpleName();

  private FragmentTreeviewBinding binding;
  private MainViewModel mMainViewModel;
  private FileManager fileManager;
  private Logger logger;
  private TreeNode rootNode;
  private AndroidTreeView treeView;
  private String fileTreeSavedState;

  public static TreeViewFragment newInstance() {
    return new TreeViewFragment();
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    fileManager = new FileManager(requireContext(), requireActivity());
    logger = new Logger(Logger.LogClass.IDE);
  }

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup viewgroup,
      @Nullable Bundle savedInstanceState) {
    binding = FragmentTreeviewBinding.inflate(inflater, viewgroup, false);
    return binding.getRoot();
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    mMainViewModel =
        new ViewModelProvider(requireActivity() /*shared activity scope*/).get(MainViewModel.class);
    logger.attach(requireActivity() /*shared activity scope*/);
    mMainViewModel.observeSetTreeViewFragmentFile(getViewLifecycleOwner(), this::populateFileTree);
    if (savedInstanceState != null) {
      fileTreeSavedState = savedInstanceState.getString(KEY_STORED_TREE_STATE, null);
    }
    // ...
    binding.folderOptions.setOnClickListener(
        v -> {
          if (rootNode != null) {
            displayBottomSheetOnClickFolderOptions();
          }
        });
    binding.treeOpenFolder.setOnClickListener(
        v -> {
          MainFragment mainFragment =
              (MainFragment)
                  requireActivity().getSupportFragmentManager().findFragmentByTag(MainFragment.TAG);
          if (mainFragment != null) {
            mainFragment.openFolderFromManager();
          }
        });
    binding.chooseTemplate.setOnClickListener(v -> chooseTemplates());
  }

  @Override
  public void onSaveInstanceState(@NonNull Bundle outState) {
    super.onSaveInstanceState(outState);
    if (treeView != null) fileTreeSavedState = treeView.getSaveState();
    outState.putString(KEY_STORED_TREE_STATE, fileTreeSavedState);
  }

  @Override
  @MainThread
  @CallSuper
  public void onStop() {
    super.onStop();
    if (rootNode != null) {
      // save as last opened
      var projectDir = rootNode.getValue().getAbsolutePath();
      PreferencesUtils.getLastOpenedProjectPreferences()
          .edit()
          .putString(Constants.SharedPreferenceKeys.KEY_LAST_OPENED_PROJECT, projectDir)
          .apply();
    }
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    // nullify references to prevent memory leaks
    binding = null;
    treeView = null;
  }

  @Override
  public void onClick(TreeNode node, Object value) {
    var mFile = (File) value;

    if (mFile.isFile()) {
      // BaseFragment performs sanity check for invalid files
      // mMainViewModel.openEditorFile(mFile);

      MainFragment mainFragment =
          (MainFragment)
              requireActivity().getSupportFragmentManager().findFragmentByTag(MainFragment.TAG);
      if (mainFragment != null) {
        mainFragment.openFileInPane(mFile);
      }
    } else if (mFile.isDirectory() && mFile.exists()) {
      if (node.isExpanded()) {
        collapseNode(node);
        return;
      }
      setLoading(node, true);
      listNode(
          node,
          () -> {
            setLoading(node, false);
            expandNode(node);
          });
    }
  }

  @Override
  public boolean onLongClick(TreeNode node, Object value) {
    displayBottomSheetOnLongClick(node);
    return true;
  }

  /**
   * Parse a directory and it's contents into the file tree
   *
   * @param dir The directory
   */
  private void populateFileTree(File dir) {
    if (dir == null) return;

    mMainViewModel.setToolbarSubTitle(FileUtils.getFileNameNoExtension(dir));
    doCloseFolder(false);
    rootNode = TreeNode.root(dir);
    rootNode.setViewHolder(new FileTreeViewHolder(requireContext()));

    binding.filetreeProgressIndicator.setVisibility(View.VISIBLE);
    listNode(
        rootNode,
        () -> {
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

            // possible to use event bus to open panes instantly rather than listeners
            EventBus.getDefault().post(new ProjectEvent(dir));
            tryRestoreSavedState();
          }
        });
    updateViewsVisibility();
  }

  public void addNewChild(TreeNode parent, File file) {
    var newNode = new TreeNode(file);
    newNode.setViewHolder(new FileTreeViewHolder(getContext()));
    parent.addChild(newNode);
  }

  public void listNode(TreeNode node, Runnable post) {
    node.getChildren().clear();
    node.setExpanded(false);
    AsyncTask.runNonCancelable(
        () -> {
          listFilesForNodeOrginal(node);
          TreeNode temp = node;

          while (temp.size() == 1) {
            temp = temp.childAt(0);
            if (!temp.getValue().isDirectory()) {
              break;
            }
            listFilesForNodeOrginal(temp);
            temp.setExpanded(true);
          }
          return null;
        },
        (result) -> {
          post.run();
        });
  }

  public void listFilesForNode(TreeNode parent) {
    Path parentPath = parent.getValue().toPath();
    try {
      Files.list(parentPath)
          .sorted(Comparator.comparing(Path::toString))
          .map(Path::toFile)
          .forEach(
              file -> {
                TreeNode child = new TreeNode(file);
                child.setViewHolder(new FileTreeViewHolder(getContext()));
                parent.addChild(child);
              });
    } catch (IOException e) {
      logger.e(LOG_TAG, getString(R.string.failed_to_populate_file_tree));
    }
  }

  public void listFilesForNodeOrginal(TreeNode parent) {
    File[] fileArray = parent.getValue().listFiles();
    if (fileArray != null) {
      Arrays.sort(fileArray, FileManager.FILE_FIRST_ORDER);
      for (File file : fileArray) {
        var child = new TreeNode(file);
        child.setViewHolder(new FileTreeViewHolder(getContext()));
        parent.addChild(child);
      }
    }
  }

  public void expandNode(TreeNode node) {
    if (treeView == null) {
      return;
    }
    ChangeBounds cb = new ChangeBounds();
    cb.setDuration(500);
    TransitionManager.beginDelayedTransition(binding.fileTreeArea, cb);
    treeView.expandNode(node);
    updateToggle(node);
  }

  public void collapseNode(TreeNode node) {
    if (treeView == null) {
      return;
    }
    ChangeBounds cb = new ChangeBounds();
    cb.setDuration(500);
    TransitionManager.beginDelayedTransition(binding.fileTreeArea, cb);
    treeView.collapseNode(node);
    updateToggle(node);
  }

  public void setLoading(TreeNode node, boolean loading) {
    if (node.getViewHolder() instanceof FileTreeViewHolder) {
      ((FileTreeViewHolder) node.getViewHolder()).setLoading(loading);
    }
  }

  private void updateToggle(TreeNode node) {
    if (node.getViewHolder() instanceof FileTreeViewHolder) {
      ((FileTreeViewHolder) node.getViewHolder()).rotateChevron(node.isExpanded());
    }
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
        listNode(
            child,
            () -> {
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
      rootNode.getChildren().clear();
      rootNode = null;
      treeView = null;
      // remove toolbar subtitle ...#is project name
      mMainViewModel.setToolbarSubTitle(null);
      if (removePrefsAndTreeState) {
        fileTreeSavedState = null;
      }
      // remove last opened project
      PreferencesUtils.clearPerference(
          PreferencesUtils.getLastOpenedProjectPreferences(),
          Constants.SharedPreferenceKeys.KEY_LAST_OPENED_PROJECT);
      // possible to use event bus to close all panes instantly
      // when event is null rather than interface listeners
      EventBus.getDefault().post(new ProjectEvent(null));
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
      binding.folderName.setText(rootNode.getValue().getName());
      binding.noFolderLin.setVisibility(View.GONE);
      binding.fileTreeArea.setVisibility(View.VISIBLE);
      binding.folderOptions.setVisibility(View.VISIBLE);
    }
  }

  private void chooseTemplates() {
    TemplateFragment.newInstance().show(getChildFragmentManager(), null);
  }

  private List<ActionModel> getFolderOptionsList() {
    List<ActionModel> listItems = new ArrayList<>();
    listItems.add(0, new ActionModel(R.drawable.ic_content_copy, getString(R.string.copy_path)));
    listItems.add(1, new ActionModel(R.drawable.ic_delete_outline, getString(R.string.delete)));
    listItems.add(
        2, new ActionModel(R.drawable.ic_file_plus_outline, getString(R.string.new_file)));
    listItems.add(
        3, new ActionModel(R.drawable.ic_folder_plus_outline, getString(R.string.new_folder)));
    listItems.add(4, new ActionModel(R.drawable.ic_pencil_outline, getString(R.string.rename)));
    listItems.add(5, new ActionModel(R.drawable.ic_close, getString(R.string.close)));
    // listItems.add(2, new ActionModel(R.drawable.ic_powershell,
    // getString(R.string.open_terminal)));
    return listItems;
  }

  private List<ActionModel> getFolderOptionsList(File file) {
    List<ActionModel> listItems = new ArrayList<>();
    listItems.add(new ActionModel(R.drawable.ic_content_copy, getString(R.string.copy_path)));
    listItems.add(new ActionModel(R.drawable.ic_delete_outline, getString(R.string.delete)));
    if (file.isDirectory()) {
      listItems.add(new ActionModel(R.drawable.ic_file_plus_outline, getString(R.string.new_file)));
      listItems.add(
          new ActionModel(R.drawable.ic_folder_plus_outline, getString(R.string.new_folder)));
    }
    listItems.add(new ActionModel(R.drawable.ic_pencil_outline, getString(R.string.rename)));
    return listItems;
  }

  private void displayBottomSheetOnClickFolderOptions() {
    var rootDir = rootNode.getValue();
    var bottomSheetDialog = new BottomSheetDialog(getActivity());
    var bind = LayoutSheetListBinding.inflate(getLayoutInflater());
    bottomSheetDialog.setContentView(bind.getRoot());
    // Get the options list
    var adapter = new ActionAdapter(getFolderOptionsList());
    adapter.setOnItemClickListener(
        model -> {
          String label = model.getTitle();
          if (label == getString(R.string.copy_path)) {
            BaseUtil.copyToClipBoard(rootDir.getAbsolutePath(), true);
          } else if (label == getString(R.string.delete)) {
            // TODO: Delete folder here
            fileManager.startFileTask(
                FileAction.DELETE_FOLDER,
                new File(rootDir.getAbsolutePath()),
                object -> {
                  if (object != null && object instanceof Boolean)
                    if ((Boolean) object) doCloseFolder(true);
                });
          } else if (label == getString(R.string.new_file)) {
            // TODO: Create new file here
            fileManager.startFileTask(
                FileAction.CREATE_FILE,
                new File(rootDir.getAbsolutePath()),
                object -> {
                  if (object != null && object instanceof File) {
                    File newFile = (File) object;
                    addNewChild(rootNode, newFile);
                    expandNode(rootNode);
                  }
                });
          } else if (label == getString(R.string.new_folder)) {
            // TODO: Create new folder here
            fileManager.startFileTask(
                FileAction.CREATE_FOLDER,
                new File(rootDir.getAbsolutePath()),
                object -> {
                  if (object != null && object instanceof File) {
                    File newFolder = (File) object;
                    addNewChild(rootNode, newFolder);
                    expandNode(rootNode);
                  }
                });
          } else if (label == getString(R.string.rename)) {
            // TODO: Rename folder here
            fileManager.startFileTask(
                FileAction.RENAME_FOLDER,
                new File(rootDir.getAbsolutePath()),
                object -> {
                  if (object != null && object instanceof File) {
                    File renamedFolder = (File) object;
                    refreshFileTree(renamedFolder);
                  }
                });
          } else if (label == getString(R.string.close)) {
            new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.close_project_title)
                .setMessage(R.string.close_project_message)
                .setPositiveButton(
                    R.string.yes,
                    (d, which) -> {
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
    bind.sheetList.setAdapter(adapter);
    bottomSheetDialog.show();
  }

  private void displayBottomSheetOnLongClick(TreeNode node) {
    var treeFile = node.getValue();
    var bottomSheetDialog = new BottomSheetDialog(getActivity());
    var bind = LayoutSheetListBinding.inflate(getLayoutInflater());
    bottomSheetDialog.setContentView(bind.getRoot());
    List<ActionModel> optionsList = getFolderOptionsList(treeFile);
    var adapter = new ActionAdapter(optionsList);

    adapter.setOnItemClickListener(
        model -> {
          var label = model.getTitle();
          if (label == getString(R.string.copy_path)) {
            BaseUtil.copyToClipBoard(treeFile.getAbsolutePath(), true);
          } else if (label == getString(R.string.delete)) {
            // TODO: Delete folder here
            if (treeFile.isFile() && treeFile.exists()) {
              fileManager.startFileTask(
                  FileAction.DELETE_FILE,
                  new File(treeFile.getAbsolutePath()),
                  object -> {
                    if (object != null && object instanceof Boolean)
                      if ((Boolean) object) treeView.removeNode(node);
                  });
            } else if (treeFile.isDirectory() && treeFile.exists()) {
              fileManager.startFileTask(
                  FileAction.DELETE_FOLDER,
                  new File(treeFile.getAbsolutePath()),
                  object -> {
                    if (object != null && object instanceof Boolean)
                      if ((Boolean) object) treeView.removeNode(node);
                  });
            }
          } else if (label == getString(R.string.new_file)) {
            fileManager.startFileTask(
                FileAction.CREATE_FILE,
                new File(treeFile.getAbsolutePath()),
                object -> {
                  if (object != null && object instanceof File) {
                    File newFile = (File) object;
                    addNewChild(node, (File) object);
                    expandNode(node);
                  }
                });
          } else if (label == getString(R.string.new_folder)) {
            fileManager.startFileTask(
                FileAction.CREATE_FOLDER,
                new File(treeFile.getAbsolutePath()),
                object -> {
                  if (object != null && object instanceof File) {
                    File newFolder = (File) object;
                    addNewChild(node, (File) object);
                    expandNode(node);
                  }
                });
          } else if (label == getString(R.string.rename)) {
            if (treeFile.isFile()) {
              fileManager.startFileTask(
                  FileAction.RENAME_FILE,
                  new File(treeFile.getAbsolutePath()),
                  object -> {
                    if (object != null && object instanceof File) {
                      var renamedFile = (File) object;
                      if (renamedFile != null) {
                        refreshFileTree();
                      }
                    }
                  });
            } else {
              fileManager.startFileTask(
                  FileAction.RENAME_FOLDER,
                  new File(treeFile.getAbsolutePath()),
                  object -> {
                    if (object != null && object instanceof File) {
                      var renamedFolder = (File) object;
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
    bind.sheetList.setAdapter(adapter);
    bottomSheetDialog.show();
  }
}
