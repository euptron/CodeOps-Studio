package com.eup.codeops.ast.core.node;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Represents a node in a tree data structure.
 *
 * <pre>
 * +---------------+
 * |  Node        |
 * +---------------+
 * |  +--------+  |
 * |  |  Data  |  |
 * |  +--------+  |
 * |  +--------+  |
 * |  | Parent  | |
 * |  +--------+  |
 * |  +--------+  |
 * |  | Child  |  |
 * |  +--------+  |
 * +---------------+
 * </pre>
 *
 * A Node represents a single element in the tree, containing:
 * <li>Data
 *
 *     <ul>
 *       The actual value stored in the Node
 * </ul>
 *
 * <li>Parent the Node's parent in the tree (null for the root Node)
 * </ul>
 *
 * <li>Child
 *
 *     <ul>
 *       The Node's child in the tree (null for leaf Nodes)
 * </ul>
 *
 * <p>This interface provides methods for managing the Node's value, parent, and children, as well
 * as calculating its depth and level in the tree.
 *
 * <pre>
 *         +---------------+
 *         |  Root (0,0)  |
 *         +---------------+
 *                  |
 *                  |
 *                  v
 *         +---------------+
 *         |  Child (1,1)  |
 *         +---------------+
 *                  |
 *                  |
 *                  v
 *         +-------------------+
 *         |  Grandchild (2,2) |
 *         +-------------------+
 *                  |
 *                  |
 *                  v
 *         +-------------------------+
 *         |  Great-Grandchild (3,3) |
 *         +-------------------------+
 * </pre>
 *
 * Note: The numbers in parentheses represent the level and depth of each node also The level of a
 * node is the number of generations or tiers from the root node. The depth of a node is the number
 * of edges between the node and the root node.
 *
 * @param <T> the type of data stored in the node
 * @see #getDepth() for calculating the node's depth
 * @see #getLevel() for calculating the node's level
 * @see AbstractNode
 * @version 1.0
 * @since 1.0
 * @author EUP (2024-08-15)
 */
public interface Node<T> {

  /** Used to separate node levels in the level path. */
  public static final String NODE_SEPARATOR = "/";

  /**
   * Resets the node by removing all references to its children, parent, and value.
   *
   * <p>This method is intended to be used internally by the node implementation to clean up
   * resources when a node is removed from the tree. It is not recommended to call this method
   * directly, as it can leave the node in an inconsistent state.
   *
   * <p>After calling this method, the node's state will be similar to its initial state, but it may
   * not be suitable for reuse without reinitializing its properties.
   *
   * <p><strong>Warning:</strong> Calling this method manually can lead to unexpected behavior if
   * not handled properly. It is generally safer to rely on the node's implementation to manage its
   * own lifecycle.
   */
  default void reset() {
    // No-op
  }

  /**
   * Add arbitrary object(s) to hold for later use if it is not already present.
   *
   * @param tag the object to store
   * @return The current instance for call chaining
   */
  Node<T> addTag(Object tag);

  /**
   * Returns the tag at the specified position in this Node's tag list.
   *
   * @param index index of the tag to return
   * @return the tag at the specified position in this Node's tag list.
   * @throws IndexOutOfBoundsException if the index is out of range ({@code index < 0 || index >=
   *     size()})
   */
  Object getTag(int index);

  /**
   * Checks if a tag exists in the tag list.
   *
   * @param tag the non-null tag to check is present
   * @return {@code true} if the tag is already present, {@code false} otherwise
   */
  boolean containsTag(Object tag);

  /**
   * Gets the value stored in the node.
   *
   * @return the value
   */
  T getValue();

  /**
   * Sets the value stored in the node.
   *
   * @param value the new value * @return the node with a value
   */
  Node<T> setValue(T value);

  /**
   * Increments this node's value frequency and returns the parent node to facilitate upward
   * propagation.
   *
   * <p>Usage pattern:
   *
   * <pre>{@code
   * // Propagate frequency increments up to root
   * for (Node<?> n = currentNode; n != null; n = n.incrementValueFrequency())
   * }</pre>
   *
   * @return the parent node for continued propagation, or {@code null} at root
   */
  Node<T> incrementValueFrequency();

  /**
   * Decrements this node's value frequency (minimum 0). Signals whether propagation should
   * continue.
   *
   * <p>Usage pattern:
   *
   * <pre>{@code
   * Node<?> n = currentNode;
   * while (n != null && n.decrementValueFrequency()) {
   *     n = n.getParent();
   * }
   * }</pre>
   *
   * @return {@code true} if frequency changed and parent propagation should continue, {@code false}
   *     otherwise
   */
  boolean decrementValueFrequency();

  /**
   * Returns the current reference count for this value.
   *
   * @return Non-negative integer representing active usage instances
   */
  int getValueFrequency();

  /**
   * Gets the parent node.
   *
   * @return the parent node, or null if this is the root node
   */
  Node<T> getParent();

  /**
   * Sets the parent node.
   *
   * @param parent the new parent node
   * @return the node with set parent
   */
  Node<T> setParent(Node<T> parent);

  /**
   * Adds a single child node to this node's child list.
   *
   * @param child the child node to add
   * @return the node containing a new child node
   */
  Node<T> addChild(@NonNull Node<T> child);

  /**
   * Adds a single child node at the specified index in this node's child list.
   *
   * @param child the child node to add
   * @param index the index at which to add the child node
   * @return the node containing a new child node
   */
  Node<T> addChild(@NonNull Node<T> child, int index);

  /**
   * Adds a child node to this node's child list, using the specified comparator to determine the
   * insertion point.
   *
   * <p>Time complexity: O(n log n) if a comparator is provided, O(1) otherwise Space complexity:
   * O(1)
   *
   * @param child the child node to add, must not be null
   * @param comparator the comparator to use for determining the insertion point, may be null
   * @see Comparator#compare(Object, Object)
   * @return the node containing a new child node
   */
  Node<T> addChild(@NonNull Node<T> child, @Nullable Comparator<Node<T>> comparator);

  /**
   * Adds a list of child nodes to this node's child list.
   *
   * @param children the list of child nodes to add
   * @return the node containing new child nodes
   */
  Node<T> addChildren(@NonNull List<Node<T>> children);

  /**
   * Adds an array of child nodes to this node's child list.
   *
   * @param children the array of child nodes to add
   * @return the node containing new child nodes
   */
  Node<T> addChildren(@NonNull Node<T>[] children);

  /**
   * Adds a list of child nodes to this node's child list, using the specified comparator to
   * determine the insertion point.
   *
   * @param children the list of child nodes to add
   * @param comparator the comparator to use for determining the insertion point, may be null
   * @return the node containing new child nodes
   */
  Node<T> addChildren(@NonNull List<Node<T>> children, Comparator<Node<T>> comparator);

  /**
   * Re-orders the children of this node
   *
   * @param comparator the comparator to use for determining the insertion point, may be null
   */
  Node<T> sortChildren(@NonNull Comparator<Node<T>> comparator);

  /**
   * Replaces the current child nodes with new ones.
   *
   * @return this node
   * @param children the new child nodes
   * @param comparator the comparator to use for determining the insertion point, may be {@code
   *     null}
   * @return the node containing updated children
   */
  Node<T> updateChildren(@NonNull List<Node<T>> children, @Nullable Comparator<Node<T>> comparator);

  /**
   * Gets the child nodes.
   *
   * @return the list of child nodes
   */
  List<Node<T>> getChildren();

  /**
   * Returns the child node at the specified index.
   *
   * @param index the index of the child node to retrieve
   * @return the child node at the specified index, or null if index is out of bounds
   */
  Node<T> getChild(int index);

  /**
   * Removes the specified child node from this node's child list.
   *
   * @param child the child node to remove
   * @return the index of the removed child, or -1 if the child was not found
   */
  int removeChild(Node<T> child);

  /**
   * Removes all child nodes from this node's child list.
   *
   * @return the number of children removed
   */
  int removeAllChildren();

  /**
   * Checks if the node is a leaf node (has no children).
   *
   * @return true if the node is a leaf node, false otherwise
   */
  boolean isLeaf();

  /**
   * Checks if the node has a child node (children).
   *
   * @return true if the node has children, false otherwise
   */
  boolean hasChildren();

  /**
   * Returns the number of children of this node in the
   *
   * @return the children size
   */
  int childrenSize();

  /**
   * Returns the depth of this node in the tree, which is the total number of nodes from the root to
   * this node, including this node itself. This represents the node's position or distance from the
   * root, where the root node has a depth of 1, its children have a depth of 2, their children have
   * a depth of 3, and so on.
   *
   * <p>Note: This differs from the level of a node, which represents the number of ancestors (or
   * generations) between this node and the root. The depth includes the current node, whereas the
   * level does not.
   *
   * @return the depth of this node (total nodes from root to this node, including this node)
   */
  int getDepth();

  /**
   * Returns the total number of descendant nodes in the subtree rooted at this node. This count
   * includes all child nodes, grandchild nodes, and so on.
   *
   * @return the total count of descendant nodes in this node's subtree
   */
  int getDescendantCount();

  /**
   * Returns the level of this node in the tree, which is the number of generations or tiers from
   * the root node. The root node is at level 0.
   *
   * @return the level of this node
   */
  int getLevel();

  /**
   * Sets the level of a node.
   *
   * @param level the node level
   * @return the node with updated level
   */
  Node<T> setLevel(int level);

  /**
   * Dynamically calculates the apparent level of this node by traversing up to the Unlike {@link
   * #getLevel()}, which returns the stored hierarchy level, this method recalculates the level
   * on-the-fly based on the current parent hierarchy.
   *
   * <p><b>Key differences from {@code getLevel()}:</b>
   *
   * <ul>
   *   <li>Guaranteed to reflect the current parent-child relationships, even if the stored {@code
   *       level} field is outdated
   *   <li>Slower performance due to parent traversal
   *   <li>Useful for validating tree structure integrity
   * </ul>
   *
   * @return The number of edges between this node and the root node. Returns {@value #ROOT_LEVEL}
   *     (0) if this is a root node.
   * @see #getLevel() For the faster but potentially cached level value
   * @see #updateChildrenLevels() To synchronize stored levels with actual hierarchy
   */
  int getApparentLevel();

  /**
   * Updates the levels of all child nodes in the subtree rooted at this node and their descendants
   * in the tree.
   *
   * <p>Implementations should traverse the tree structure and set the level of each node based on
   * its position in the tree, handling deep trees to avoid stack overflow.
   *
   * <p>For example, if this node is at level 3, its children will be updated to level 4, its
   * grandchildren will be updated to level 5, and so on.
   *
   * @return the node with updated children levels
   */
  Node<T> updateChildrenLevels();

  /**
   * Updates the levels of direct children of this node.
   *
   * @param level the level to set the children based on.
   * @return this node with updated children levels
   */
  Node<T> updateDirectChildrenLevels(int level);

  /**
   * Returns the ancestor path of this node, representing its position in the tree.
   *
   * <p>The ancestor path is a string of node levels separated by the node separator ({@link
   * #NODE_SEPARATOR}). Each level is represented by its numeric value, starting from the root node
   * (level 0).
   *
   * @return the ancestor path of this node, or an empty string if this node is the root node
   */
  public String getAncestorPath();

  /**
   * Checks if this node is the root of the tree.
   *
   * @return true if this node is the root, false otherwise
   */
  boolean isRoot();

  /**
   * Returns the root node of the tree.
   *
   * @return the root node
   */
  Node<T> getRoot();

  /**
   * Checks if this node is the last child of its parent.
   *
   * @return true if this node is the last child, false otherwise
   */
  boolean isLastChild();

  /**
   * Checks if this node is the first child of its parent.
   *
   * @return true if this node is the first child, false otherwise
   */
  boolean isFirstChild();

  boolean containsChild(Node<T> node);

  /**
   * Returns the last child of this node.
   *
   * @return the last child node, or null if this node has no children
   */
  Node<T> getLastChildNode();

  /**
   * Returns the first child of this node.
   *
   * @return the first child node, or null if this node has no children
   */
  Node<T> getFirstChildNode();

  /**
   * Returns the last child of this node's parent.
   *
   * @return the last child node of the parent, or null if this node is the root or has no siblings
   */
  Node<T> getLastSibling();

  /**
   * Returns the first child of this node's parent.
   *
   * @return the first child node of the parent, or null if this node is the root or has no siblings
   */
  Node<T> getFirstSibling();

  /**
   * Converts the node and its descendants to a JSON string.
   *
   * @param indent the current indentation level
   * @return the JSON string representation of the node
   */
  String toJsonString(int indent);

  /**
   * Returns the index of this node in the tree, which is the position of nodes or tiers from the
   * root node. The root node is at index 0.
   *
   * @return the level of this node
   */
  int getIndex();

  /**
   * Gets all children and grand children of this node
   *
   * @param includeNode adds this node if true
   * @return a node collection
   */
  List<Node<T>> listNeighbours(boolean includeNode);

  /**
   * Removes the nodes in the specified range from the start to the end index minus one.
   *
   * @param fromIndex the index at which to start removing.
   * @param toIndex the index after the last element to remove.
   * @throws UnsupportedOperationException if removing from this list is not supported.
   * @throws InvalidRangeException if {@code childrenSize() <= 0) || fromIndex >= childrenSize() || toIndex > childrenSize() || fromIndex > toIndex
   */
  void removeRange(int fromIndex, int toIndex);

  /**
   * A node id may be {@code null}
   *
   * @return The node uuid
   */
  @Nullable
  public UUID getID();

  /**
   * Calculates the total number of leaf nodes rooted at this node.
   *
   * @param includeDescendants true to count leaf nodes in the entire subtree, false to count only
   *     direct children.
   * @return the total number of leaf nodes.
   */
  int getLeafCount(boolean includeDescendants);

  /**
   * Sets the ID for this node.
   *
   * @param id the UUID to set as the node's identifier.
   */
  void setID(UUID id);

  /**
   * Returns the first node that matches a specified value.
   *
   * <p>The returned node is the direct child of this node.
   *
   * @see #findChildByValue if you want to search this node and all it's descendants
   * @param value The value to search for
   */
  Node<T> getChildByValue(T value);

  /**
   * Returns the first node that matches a specified tag
   *
   * <p>The returned node is the direct child of this node.
   *
   * @param tag the object used to identify the node to be returned
   * @throws NullPointerException if tag is null
   */
  Node<T> getChildByTag(Object tag);

  /**
   * Finds the first node in BFS traversal order that matches the specified tag, searching this node
   * and all descendants.
   *
   * @param tag The non-null tag to search for
   * @return First matching node in BFS order, or {@code null} if not found
   * @throws NullPointerException if tag is null
   */
  Node<T> findNodeByTag(Object tag);

  /**
   * Finds all nodes matching the specified tag in BFS traversal order, searching this node and all
   * descendants.
   *
   * @param tag The non-null tag to search for
   * @return Unmodifiable list of matches in BFS order (never null)
   * @throws NullPointerException if tag is null
   */
  List<Node<T>> findNodesByTag(Object tag);

  /**
   * Finds the first node in the BFS traversal order that matches the specified value, searching
   * this node and all descendants.
   *
   * @param value The value to search for
   * @return First matching node in BFS order, or {@code null} if not found
   * @throws NullPointerException if tag is null
   */
  Node<T> findNodeByValue(T value);

  /**
   * Finds all nodes matching the specified value in BFS traversal order, searching this node and
   * all descendants.
   *
   * @param value The value to search for
   * @return Unmodifiable list of matches in BFS order (never null)
   * @throws NullPointerException if tag is null
   */
  List<Node<T>> findNodesByValue(T value);

  @Override
  public boolean equals(Object object);

  @Override
  public int hashCode();

  @Override
  public String toString();
}
