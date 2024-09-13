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
 
package com.eup.codeopsstudio.ui.editor.code.breadcrumb.pane;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;
import androidx.annotation.NonNull;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.elevation.SurfaceColors;
import com.eup.codeopsstudio.MainFragment;
import com.eup.codeopsstudio.common.AsyncTask;
import com.eup.codeopsstudio.pane.Pane;
import com.eup.codeopsstudio.res.R;
import com.eup.codeopsstudio.common.util.FileUtil;
import com.eup.codeopsstudio.res.databinding.LayoutCrumbTreePaneBinding;
import com.eup.codeopsstudio.ui.explore.holder.FileTreeViewHolder;
import com.eup.codeopsstudio.file.FileManager;
import com.eup.codeopsstudio.util.BaseUtil;
import com.eup.codeopsstudio.tv.model.TreeNode;
import com.eup.codeopsstudio.tv.view.AndroidTreeView;
import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

public class CrumbTreePane extends Pane
    implements TreeNode.TreeNodeClickListener, TreeNode.TreeNodeLongClickListener {

  private LayoutCrumbTreePaneBinding binding;
  private PopupWindow window;
  private AndroidTreeView treeView;
  private TreeNode rootNode;
  private String path;
  private View anchorView;

  public CrumbTreePane(Context context, View anchorView) {
    super(context, "file-tree");
    this.anchorView = anchorView;
    // show crumb file tree pane
    createView();
  }

  @Override
  protected View onCreateView() {
    binding = LayoutCrumbTreePaneBinding.inflate(LayoutInflater.from(getContext()));
    return binding.getRoot();
  }

  @Override
  public void onViewCreated(View view) {
    super.onViewCreated(view);
    window = new PopupWindow(getContext());

    window.setWidth(BaseUtil.dp(190));
    window.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);

    window.setFocusable(true);
    window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    window.setElevation(5);

    window.setContentView(view);
    window.showAsDropDown(anchorView);
    applyBackground();
  }

  @Override
  public void onClick(TreeNode node, Object value) {
    File file = (File) value;

    if (file.isFile()) {
      callFragmentMethod(MainFragment.TAG, "openFileInPane", file);
      onDestroy();
    } else if (file.isDirectory() && file.exists()) {
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
    return true;
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    window.dismiss();
    treeView = null;
    rootNode = null;
    binding = null;
  }

  public void setPath(@NonNull String path) {
    this.path = path;
    listFiles();
  }

  private void listFiles() {
    treeView = null;
    rootNode = TreeNode.root(new File(path));
    rootNode.setViewHolder(new FileTreeViewHolder(getContext()));

    binding.loading.setVisibility(View.VISIBLE);
    listNode(
        rootNode,
        () -> {
          treeView = new AndroidTreeView(getContext(), rootNode, R.drawable.base_ripple);
          treeView.setUseAutoToggle(false);
          treeView.setDefaultNodeClickListener(this);
          treeView.setDefaultNodeLongClickListener(this);

          if (treeView != null) {
            View view = treeView.getView();

            binding.horizontalScroll.removeAllViews();
            binding.horizontalScroll.addView(view);

            binding.loading.setVisibility(View.GONE);
          }
        });
  }

  public void addNewChild(TreeNode parent, File file) {
    TreeNode newNode = new TreeNode(file);
    newNode.setViewHolder(new FileTreeViewHolder(getContext()));
    parent.addChild(newNode);
  }

  public void listNode(TreeNode node, Runnable post) {
    node.getChildren().clear();
    node.setExpanded(false);
    AsyncTask.runNonCancelable(
        () -> {
                addChildrenToNode(node);
                TreeNode parent = node;
                 // expand dir with only 1 folder
                while (parent.size() == 1) {
                  parent = parent.childAt(0);
                  if (!parent.getValue().isDirectory()) break;
                  addChildrenToNode(parent);
                  parent.setExpanded(true);
                }
          return null;
        },
        (result) -> {
          post.run();
        });
  }
  
  public void addChildrenToNode(TreeNode parent) {
    File[] fileArray = FileUtil.listFiles(parent.getValue());
    Arrays.sort(fileArray, FileManager.DIR_FIRST_SORT);
    for (File file : fileArray) {
      var child = new TreeNode(file);
      child.setViewHolder(new FileTreeViewHolder(getContext()));
      parent.addChild(child);
    }
  }
  
  public void expandNode(TreeNode node) {
    if (treeView == null) return;
    treeView.expandNode(node);
    updateToggle(node);
  }

  public void collapseNode(TreeNode node) {
    if (treeView == null) return;
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

  private void applyBackground() {
    GradientDrawable drawable = new GradientDrawable();
    drawable.setShape(GradientDrawable.RECTANGLE);
    drawable.setCornerRadius(BaseUtil.dp(4));
    drawable.setColor(SurfaceColors.SURFACE_1.getColor(getContext()));
    drawable.setStroke(
        1,
        MaterialColors.getColor(getContext(), com.google.android.material.R.attr.colorOutline, 0));
    binding.getRoot().setBackground(drawable);
  }
}
