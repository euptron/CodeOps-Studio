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

package com.eup.codeopsstudio.tv.model;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.eup.codeopsstudio.tv.R;
import com.eup.codeopsstudio.tv.view.AndroidTreeView;
import com.eup.codeopsstudio.tv.view.TreeNodeWrapperView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/** Created by Bogdan Melnychuk on 2/10/15. */
public class TreeNode {

  public static final String NODES_ID_SEPARATOR = ":";
  private final List<TreeNode> children;
  private int mId;
  private int mLastId;
  private TreeNode mParent;
  private boolean mSelected;
  private boolean mSelectable = true;
  private BaseNodeViewHolder mViewHolder;
  private TreeNodeClickListener mClickListener;
  private TreeNodeLongClickListener mLongClickListener;
  private File mValue;
  private boolean mExpanded;

  public TreeNode(File value) {
    children = Collections.synchronizedList(new ArrayList<TreeNode>());
    mValue = value;
  }

  public static TreeNode root() {
    return root(null);
  }

  public static TreeNode root(File value) {
    TreeNode root = new TreeNode(value);
    root.setSelectable(false);
    return root;
  }

  public TreeNode addChildren(TreeNode... nodes) {
    for (TreeNode n : nodes) {
      addChild(n);
    }
    return this;
  }

  public TreeNode addChild(TreeNode childNode) {
    childNode.mParent = this;
    childNode.mId = generateId();
    children.add(childNode);
    children.sort(DIR_FIRST_SORT);    
    return this;
  }

  private int generateId() {
    return ++mLastId;
  }

  public TreeNode addChildren(Collection<TreeNode> nodes) {
    for (TreeNode n : nodes) {
      addChild(n);
    }
    return this;
  }

  public TreeNode childAt(int index) {
    return children == null ? null : children.get(index);
  }

  public void deleteAllChildren() {
    children.clear();
  }

  public int deleteChild(TreeNode child) {
    for (int i = 0; i < children.size(); i++) {
      if (child.mId == children.get(i).mId) {
        children.remove(i);
        return i;
      }
    }
    return -1;
  }

  public List<TreeNode> getChildren() {
    return children == null ? Collections.synchronizedList(new ArrayList<TreeNode>()) : children;
  }

  public TreeNode getParent() {
    return mParent;
  }

  public boolean isLeaf() {
    return size() == 0;
  }

  public int size() {
    return children == null ? 0 : children.size();
  }

  public File getValue() {
    return mValue;
  }

  public TreeNode setValue(File file) {
    this.mValue = file;
    return this;
  }

  public boolean isExpanded() {
    return mExpanded;
  }

  public TreeNode setExpanded(boolean expanded) {
    mExpanded = expanded;
    return this;
  }

  public boolean isSelected() {
    return mSelectable && mSelected;
  }

  public void setSelected(boolean selected) {
    mSelected = selected;
  }

  public boolean isSelectable() {
    return mSelectable;
  }

  public void setSelectable(boolean selectable) {
    mSelectable = selectable;
  }

  public String getPath() {
    final StringBuilder path = new StringBuilder();
    TreeNode node = this;
    while (node.mParent != null) {
      path.append(node.getId());
      node = node.mParent;
      if (node.mParent != null) {
        path.append(NODES_ID_SEPARATOR);
      }
    }
    return path.toString();
  }

  public int getId() {
    return mId;
  }

  public int getLevel() {
    int level = 0;
    TreeNode root = this;
    while (root.mParent != null) {
      root = root.mParent;
      level++;
    }
    return level;
  }

  public boolean isLastChild() {
    if (!isRoot()) {
      int parentSize = mParent.children.size();
      if (parentSize > 0) {
        final List<TreeNode> parentChildren = mParent.children;
        return parentChildren.get(parentSize - 1).mId == mId;
      }
    }
    return false;
  }

  public boolean isRoot() {
    return mParent == null;
  }

  public TreeNodeClickListener getClickListener() {
    return this.mClickListener;
  }

  public TreeNode setClickListener(TreeNodeClickListener listener) {
    mClickListener = listener;
    return this;
  }

  public TreeNodeLongClickListener getLongClickListener() {
    return mLongClickListener;
  }

  public TreeNode setLongClickListener(TreeNodeLongClickListener listener) {
    mLongClickListener = listener;
    return this;
  }

  public BaseNodeViewHolder getViewHolder() {
    return mViewHolder;
  }

  public TreeNode setViewHolder(BaseNodeViewHolder viewHolder) {
    mViewHolder = viewHolder;
    if (viewHolder != null) {
      viewHolder.mNode = this;
    }
    return this;
  }

  public boolean isFirstChild() {
    if (!isRoot()) {
      List<TreeNode> parentChildren = mParent.children;
      return parentChildren.get(0).mId == mId;
    }
    return false;
  }

  public TreeNode getRoot() {
    TreeNode root = this;
    while (root.mParent != null) {
      root = root.mParent;
    }
    return root;
  }

  public abstract static class BaseNodeViewHolder<E> {
    protected AndroidTreeView tView;
    protected TreeNode mNode;
    protected int containerStyle;
    protected Context context;
    private View mView;

    public BaseNodeViewHolder(Context context) {
      this.context = context;
    }

    public void setTreeViev(AndroidTreeView treeViev) {
      this.tView = treeViev;
    }

    public AndroidTreeView getTreeView() {
      return tView;
    }

    public ViewGroup getNodeItemsView() {
      return (ViewGroup) getView().findViewById(R.id.node_items);
    }

    public View getView() {
      if (mView != null) {
        return mView;
      }
      final View nodeView = getNodeView();
      final TreeNodeWrapperView nodeWrapperView =
          new TreeNodeWrapperView(nodeView.getContext(), getContainerStyle());
      nodeWrapperView.insertNodeView(nodeView);
      mView = nodeWrapperView;

      return mView;
    }

    public View getNodeView() {
      return createNodeView(mNode, (E) mNode.getValue());
    }

    public abstract View createNodeView(TreeNode node, E value);

    public int getContainerStyle() {
      return containerStyle;
    }

    public void setContainerStyle(int style) {
      containerStyle = style;
    }

    public boolean isInitialized() {
      return mView != null;
    }

    public void toggle(boolean active) {
      // empty
    }

    public void toggleSelectionMode(boolean editModeEnabled) {
      // empty
    }
  }

  private static final Comparator<TreeNode> DIR_FIRST_SORT =
      (node1, node2) -> {
        File a = node1.getValue();
        File b = node2.getValue();
        if (a.isDirectory() && b.isFile()) return -1;
        if (a.isFile() && b.isDirectory()) return 1;
        return String.CASE_INSENSITIVE_ORDER.compare(a.getName(), b.getName());
      };

  public interface TreeNodeClickListener {
    void onClick(TreeNode node, Object value);
  }

  public interface TreeNodeLongClickListener {
    boolean onLongClick(TreeNode node, Object value);
  }
}
