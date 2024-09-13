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

package com.eup.codeops.ast.core.node;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.eup.codeops.ast.core.exception.InvalidRangeException;
import com.eup.codeops.ast.core.transversal.BFS;
import com.eup.codeops.ast.core.transversal.INext;
import com.eup.codeops.ast.core.visitors.NodeVisitor;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;

/**
 * Provides a foundational implementation of the {@link Node} interface, serving as a base class for
 * nodes in a tree data structure.
 *
 * <p>This abstract class defines the essential properties and behaviors of a node, including:
 *
 * <ul>
 *   <li>Value storage: Nodes can store a value of type {@code T}.
 *   <li>Parent-child relationships: Nodes can have a parent node and child nodes.
 *   <li>Traversal capabilities: Nodes can be traversed using various methods, such as {@link
 *       #getChildren()} and {@link #getParent()}.
 * </ul>
 *
 * Subclasses can extend this class to create specialized node types, inheriting its core
 * functionality while adding custom features and optimizations tailored to their specific use
 * cases.
 *
 * <h2>Creating Custom Nodes</h2>
 *
 * To create a custom node, simply extend the {@code AbstractNode} class and implement the desired
 * functionality. For example:
 *
 * <pre>
 * public class CustomNode extends AbstractNode&lt;String&gt; {
 *     // Custom node implementation
 * }
 * </pre>
 *
 * <h2>Example Usage</h2>
 *
 * Here's an example of creating a tree structure using custom nodes:
 *
 * <pre>
 * // Create the root node
 * CustomNode root = new CustomNode("Root");
 *
 * // Create child nodes
 * CustomNode child1 = new CustomNode("Child 1");
 * CustomNode child2 = new CustomNode("Child 2");
 *
 * // Add child nodes to the root node
 * root.addChild(child1);
 * root.addChild(child2);
 *
 * // Traverse the tree
 * for (Node&lt;String&gt; child : root.getChildren()) {
 *     System.out.println(child.getValue());
 * }
 * </pre>
 *
 * TODO: Implement method to get leaf count
 *
 * @param <T> the type of data stored in the node
 * @version 1.0
 * @since 1.0
 * @author EUP
 * @author EUP (2024-08-15)
 */
public abstract class AbstractNode<T> implements Node<T> {

  /** Object associated to this node */
  private T value;

  /**
   * A shared set of generated IDs to ensure uniqueness across all instances of subclasses. This set
   * is static to allow sharing among all subclasses, and is accessed in a thread-safe manner via
   * the {@link #generateID()} method, which synchronizes access to prevent concurrent
   * modifications.
   */
  private static final Set<UUID> generatedIds = new HashSet<>();

  /** The unique identifier for this node */
  public UUID mUUID;

  /**
   * The level of a node is the number of edges along the unique path between it and the root node.
   * This is the same as depth
   */
  private int level;

  /** Parent of this node not necessarily the root */
  private Node<T> parent;

  /** Child nodes of this node */
  private List<Node<T>> children;

  /** Position of node accross the tree */
  private int index;

  /** Attributed to invalid "index, size, level, depth, position" */
  private static final int INVALID_LOCATION = -1;

  /** Level attributed to nodes without value */
  public static final int UNDEFINED_LEVEL = INVALID_LOCATION;

  /** The level of a root node */
  public static final int ROOT_LEVEL = 0;

  public AbstractNode() {
    this(null, UNDEFINED_LEVEL);
  }

  /**
   * Constructor.
   *
   * @param value the initial value
   */
  public AbstractNode(T value) {
    this(value, ROOT_LEVEL);
  }

  /**
   * Constructor.
   *
   * @param value the initial value
   * @param level the initial level
   */
  public AbstractNode(T value, int level) {
    this(value, level, true);
  }

  /**
   * Constructor.
   *
   * @param value the initial value
   * @param level the initial level
   * @param genID generates unique identifier for this node is true
   */
  public AbstractNode(T value, int level, boolean genID) {
    this.value = value;
    this.children = new LinkedList<>();
    this.level = level;
    if (genID) {
      generateID();
    }
  }

  @Override
  public T getValue() {
    return value;
  }

  @Override
  public Node<T> setValue(T value) {
    // this node is likely a root
    if (level == UNDEFINED_LEVEL && parent == null) {
      level = 0;
    }
    this.value = value;
    return this;
  }

  @Override
  public Node<T> getParent() {
    return parent;
  }

  @Override
  public Node<T> setParent(@NonNull Node<T> parent) {
    if (level == UNDEFINED_LEVEL) {
      level = parent.getLevel() + 1;
      updateChildrenLevels();
    }
    this.parent = parent;
    return this;
  }

  @Override
  public Node<T> addChild(Node<T> child) {
    addChild(child, null);
    return this;
  }

  @Override
  public Node<T> addChild(Node<T> child, int index) {
    updateChildProps(child);
    children.add(index, child);
    return this;
  }

  @Override
  public Node<T> addChild(@NonNull Node<T> child, @Nullable Comparator<Node<T>> comparator) {
    updateChildProps(child);
    children.add(child);

    if (comparator != null) {
      children.sort(comparator);
    }
    return this;
  }

  @Override
  public Node<T> addChildren(List<Node<T>> children) {
    addChildren(children, null);
    return this;
  }

  @Override
  public Node<T> addChildren(Node<T>[] children) {
    for (Node<T> child : children) {
      addChild(child);
    }
    return this;
  }

  @Override
  public Node<T> addChildren(
      @NonNull List<Node<T>> children, @Nullable Comparator<Node<T>> comparator) {
    for (Node<T> child : children) {
      addChild(child, comparator);
    }
    return this;
  }

  @Override
  public Node<T> sortChildren(@NonNull Comparator<Node<T>> comparator) {
    children.sort(comparator);
    return this;
  }

  @Override
  public Node<T> updateChildren(List<Node<T>> children, @Nullable Comparator<Node<T>> comparator) {
    removeAllChildren();
    addChildren(children, comparator);
    return this;
  }

  @Override
  public List<Node<T>> getChildren() {
    return isLeaf() ? Collections.emptyList() : children;
  }

  @Override
  public Node<T> getChild(int index) {
    int size = children.size();

    if (index < 0 || index >= size) {
      throwIndexOutOfBoundsException(index, size);
    }

    return children.get(index);
  }

  @Override
  public int removeChild(Node<T> child) {
    int index = children.indexOf(child);
    if (index >= 0) {
      children.remove(index);
      child.setParent(null);
      return index;
    }
    return INVALID_LOCATION; // child not found
  }

  @Override
  public int removeAllChildren() {
    int numChildren = children.size();
    Iterator<Node<T>> iterator = children.iterator();
    while (iterator.hasNext()) {
      Node<T> child = iterator.next();
      child.setParent(null); // Update child's parent reference
      iterator.remove();
    }
    return numChildren;
  }

  @Override
  public boolean isLeaf() {
    return children == null || children.isEmpty();
  }

  @Override
  public boolean hasChildren() {
    return !children.isEmpty();
  }

  @Override
  public int childrenSize() {
    return children.size();
  }

  @Override
  public int getDepth() {
    int depth = 0;

    Node<T> root = this;
    while (root != null) {
      depth++;
      root = root.getParent();
    }
    return depth;
  }

  @Override
  public int getApparentDepth() {
    int level = this.getLevel();
    int siblingCount = getSiblingCount(this);
    return level + siblingCount;
  }

  /**
   * Returns the number of siblings that comes before this node
   *
   * @param node the node to count sibilings for
   * @return the number of siblings before this node
   */
  public int getSiblingCount(Node<T> node) {
    int count = 0;
    Node<T> parent = node.getParent();
    if (parent != null)
      for (Node<T> sibling : parent.getChildren()) {
        if (sibling == node) break;
        count++;
      }
    return count;
  }

  @Override
  public int getBreadth() {
    final int[] breadth = {0};
    INext<T> next = node -> node.hasChildren();
    new BFS<T>().traverse(this, nv -> breadth[0]++, next);
    return breadth[0] - 1; // breadth = exclusion of parent
  }

  /**
   * Similar to getLevel but conclusive
   *
   * @return the node level
   */
  public int getApparentLevel() {
    int depth = 0; // Plausible root location, +1 per ancestor

    Node parent = this.getParent();
    while (parent != null) {
      depth++;
      parent = parent.getParent();
    }
    return depth;
  }

  @Override
  public int getLevel() {
    return level;
  }

  @Override
  public Node<T> setLevel(int level) {
    this.level = level;
    return this;
  }

  @Override
  public Node<T> updateChildrenLevels() {
    NodeVisitor<T> visitor =
        child -> {
          child.setLevel(child.getParent().getLevel() + 1);
        };
    INext<T> next = child -> child.hasChildren();
    return new BFS<T>().traverse(this, visitor, next);
  }

  @Override
  public Node<T> updateDirectChildrenLevels(int level) {
    if (!children.isEmpty()) {
      for (Node<T> child : children) {
        child.setLevel(level + 1);
      }
    }
    return this;
  }

  @Override
  public String getAncestorPath() {
    StringBuilder path = new StringBuilder();
    Node<T> node = this;

    while (node.getParent() != null) {
      path.insert(
          0, NODE_SEPARATOR + node.getParent().getLevel() + NODE_SEPARATOR + node.getLevel());
      node = node.getParent();
    }
    return path.toString();
  }

  @Override
  public boolean isRoot() {
    return parent == null;
  }

  @Override
  public Node<T> getRoot() {
    Node<T> currentNode = this;
    while (currentNode.getParent() != null) {
      currentNode = currentNode.getParent();
    }
    return currentNode;
  }

  @Override
  public boolean isLastChild() {
    if (isRoot()) return false;

    int index = parent.childrenSize() - 1;
    return parent.getChildren().get(index) == this;
  }

  @Override
  public boolean isFirstChild() {
    if (isRoot()) return false;

    int index = 0; // First element location
    return parent.getChildren().get(index) == this;
  }

  @Override
  public Node<T> getLastChildNode() {
    int index = children.size() - 1; // Last element location
    return isLeaf() ? null : children.get(index);
  }

  @Override
  public Node<T> getFirstChildNode() {
    int index = 0; // First element location
    return isLeaf() ? null : children.get(index);
  }

  @Override
  public Node<T> getLastSibling() {
    if (isRoot() || parent.getChildren().isEmpty()) {
      return null;
    }
    return parent.getChildren().get(parent.childrenSize() - 1);
  }

  @Override
  public Node<T> getFirstSibling() {
    if (parent == null || parent.isLeaf()) return null;

    int index = 0; // First element location
    return parent.getChildren().get(index);
  }

  @Nullable
  @Override
  public UUID getID() {
    return mUUID;
  }

  @Override
  public void setID(UUID id) {
    this.mUUID = id;
  }

  @Override
  public int getIndex() {
    return index;
  }

  @Override
  public Node<T> setIndex(int index) {
    this.index = index;
    return this;
  }

  @Override
  public List<Node<T>> listNeighbours(boolean includeNode) {
    // No-op
    return null;
  }

  /**
   * Removes elements from the list within the specified range. This method iterates over the range
   * in reverse order to avoid index shifting issues.
   *
   * @param fromIndex the starting index (inclusive) of the range to remove
   * @param toIndex the ending index (exclusive) of the range to remove
   */
  @Override
  public void removeRange(int fromIndex, int toIndex) {
    int s = this.childrenSize();

    if (s <= 0 || fromIndex >= s || toIndex > s || fromIndex > toIndex)
      InvalidRangeException.throwException(fromIndex, toIndex, s);

    for (int i = fromIndex; i >= toIndex; i++) {
      Node<T> child = getChild(i);
      // parent should not hold a stale child
      child.setParent(null);
      removeChild(child);
    }
  }

  /** Generates a unique UUID for the node */
  protected synchronized void generateID() {
    UUID id;
    do {
      id = UUID.randomUUID();
    } while (generatedIds.contains(id));
    generatedIds.add(id);
    mUUID = id;
  }

  public boolean isSameNode(Node<T> other) {
    return this == other;
  }

  public boolean isParentOf(Node<T> node) {
    Node<T> other = node;
    while (other != null) {
      if (other == this) {
        return true;
      }

      other = other.getParent();
    }
    return false;
  }

  protected void updateChildrenProps(Collection<? extends Node<T>> collection) {
    collection.forEach(c -> updateChildProps(c));
  }

  protected void updateChildProps(Node<T> child) {
    child.setParent(this);
    child.setLevel(level + 1);
    child.setIndex(children.size());
    child.updateChildrenLevels();
  }

  static IndexOutOfBoundsException throwIndexOutOfBoundsException(int index, int size) {
    return new IndexOutOfBoundsException("Invalid index: " + index + ", size is" + size);
  }

  /**
   * Returns the hash code of objects.
   *
   * @param objects the objects to sum hash code for
   * @return the sum of objects hash codes
   */
  int getHash(final int initialHash, Object... objects) {
    int result = initialHash;
    for (Object object : objects) {
      result = 31 * result + (object != null ? object.hashCode() : 0);
    }
    return result;
  }

  @SuppressWarnings("unchecked")
  @Override
  public final boolean equals(Object object) {
    if (this == object) return true;
    if (object instanceof Node) {

      Node<T> other = (Node<T>) object;

      if (!Objects.equals(mUUID, other.getID())) return false;
      if (childrenSize() != other.childrenSize()) return false;
      if (!Objects.equals(value, other.getValue())) return false;
      if (!Objects.equals(parent, other.getParent())) return false;
      if (!Objects.equals(level, other.getLevel())) return false;
      if (!Objects.equals(index, other.getIndex())) return false;

      try {
        List<Node<T>> thisList = getChildren();
        List<Node<T>> otherList = other.getChildren();

        for (int i = 0; i < thisList.size(); i++) {
          Node<?> thisChild = thisList.get(i);
          Node<?> otherChild = otherList.get(i);

          if (thisChild == otherChild) {
            continue;
          } else if (thisChild == null || otherChild == null) {
            return false;
          }

          // recursively check child generation
          if (!thisChild.equals(otherChild)) {
            return false;
          }
        }
      } catch (NullPointerException ignored) {
        return false;
      }
      return true;
    }
    return false;
  }

  @Override
  public int getLeafCount(boolean includeDescendants) {
    int leafCount = 0;

    if (includeDescendants) {
      Queue<Node<T>> queue = new LinkedList<>();
      queue.add(this);

      while (!queue.isEmpty()) {
        Node<T> currentNode = queue.poll();
        if (currentNode.isLeaf()) {
          leafCount++;
        } else {
          queue.addAll(currentNode.getChildren());
        }
      }
    } else {
      for (Node<T> child : this.getChildren()) {
        if (child.isLeaf()) {
          leafCount++;
        }
      }
    }

    return leafCount;
  }

  /**
   * {@inheritDoc}
   *
   * <p>This implementation iterates its children, summing the hashcodes of its child nodes.
   */
  @Override
  public int hashCode() {
    int result = 1;
    Iterator<Node<T>> it = children.iterator();
    while (it.hasNext()) {
      Object object = it.next();
      result = (31 * result) + (object == null ? 0 : object.hashCode());
    }
    result = getHash(result, mUUID, value, parent);
    return result;
  }

  @Override
  public String toString() {
    return String.format(
        "%s@%s (hashCode=%d, uuid=%s, value=%s, level=%s, index=%s, leaf=%s, root=%s)",
        getClass().getSimpleName(),
        Integer.toHexString(System.identityHashCode(this)),
        hashCode(),
        mUUID.toString(),
        value,
        level,
        index,
        isLeaf(),
        isRoot());
  }

  @Override
  public String toJsonString(int indent) {
    String indentStr = "\t".repeat(indent);
    StringBuilder sb = new StringBuilder();
    List<Node<T>> children = getChildren();
    int level = getLevel();

    // Start JSON in sub-class -> {
    sb.append(indentStr).append("\t\"value\": \"").append(value).append("\",\n");
    sb.append(indentStr).append("\t\"level\": ").append(level).append(",\n");
    sb.append(indentStr).append("\t\"depth\": ").append(getDepth()).append(",\n");
    sb.append(indentStr)
        .append("\t\"parent\": ")
        .append(parent != null ? "\"" + parent.getValue() + "\"" : "null")
        .append(",\n");

    sb.append(indentStr).append("\t\"children\": [\n");

    for (int i = 0; i < children.size(); i++) {
      sb.append(children.get(i).toJsonString(indent + 1));
      if (i < children.size() - 1) {
        sb.append(",\n");
      }
    }

    sb.append("\n").append(indentStr).append("\t]\n");
    // Close JSON in sub-class -> }
    return sb.toString();
  }
}
