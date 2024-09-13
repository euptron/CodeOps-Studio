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

package com.eup.codeops.ast.core.transversal;

import com.eup.codeops.ast.core.node.Node;
import com.eup.codeops.ast.core.visitors.NodeVisitor;
import java.util.Stack;

/**
 * Implements a Depth-First Search (DFS) traversal strategy for visiting nodes in a data structure.
 * This class supports both iterative and recursive traversal methods, with a safe depth threshold
 * to avoid stack overflow in deep structures.
 *
 * <p>Usage example:
 *
 * <pre>
 * Node&lt;String&gt; root = new Node&lt;&gt;("Root");
 * Node&lt;String&gt; child1 = new Node&lt;&gt;("Child 1");
 * Node&lt;String&gt; child2 = new Node&lt;&gt;("Child 2");
 * root.addChild(child1);
 * root.addChild(child2);
 *
 * NodeVisitor&lt;String&gt; visitor = new NodeVisitor&lt;&gt;() {
 *     &#64;Override
 *     public void visit(Node&lt;String&gt; node) {
 *         System.out.println(node.getValue());
 *     }
 * };
 * INext&lt;String&gt; next = new INext&lt;&gt;() {
 *     &#64;Override
 *     public boolean hasNext(Node&lt;String&gt; node) {
 *         return !node.getChildren().isEmpty();
 *     }
 * };
 * DFS&lt;String&gt; dfs = new DFS&lt;&gt;();
 * dfs.traverse(root, visitor, next);
 * </pre>
 *
 * <p><strong>Note:</strong> DFS is ideal for scenarios where deep exploration of nodes is required.
 * It is particularly useful for searching in deep or complex data structures and for tasks like
 * finding paths or cycles in graphs.
 *
 * <p><strong>Comparison to BFS:</strong> Unlike Breadth-First Search (BFS), which explores nodes
 * level by level, DFS dives deep into each branch before backtracking. This makes DFS suitable for
 * tasks requiring thorough exploration of branches or paths, while BFS is better for level-order
 * processing and shortest path discovery in unweighted graphs.
 *
 * @param <T> the type of data stored in the nodes
 * @version 1.0
 * @since 1.0
 * @author EUP (2024-08-18)
 */
public class DFS<T> implements Traversable<T> {

  /**
   * Traverses the data structure starting from the given node, applying the provided visitor to
   * each node. If the node's depth exceeds the safe threshold, iterative traversal is used;
   * otherwise, recursive traversal is used.
   *
   * @param node the node to start the traversal from
   * @param visitor the visitor to apply to each node
   * @param next the strategy for determining the next nodes to visit
   */

  /** {@inheritDoc Traversable#traverse} */
  @Override
  public Node<T> traverse(Node<T> node, NodeVisitor<T> visitor, INext<T> next) {
    final int SAFE_DEPTH_THRESHOLD = 500;
    if (node.getDepth() >= SAFE_DEPTH_THRESHOLD) {
      traverseIterative(node, visitor, next);
    } else {
      traverseRecursive(node, visitor, next);
    }
    return node;
  }

  /**
   * Performs an iterative DFS traversal, using a stack to store nodes to visit.
   *
   * @param node the node to start the traversal from
   * @param visitor the visitor to apply to each node
   * @param next the strategy for determining the next nodes to visit
   */
  private void traverseIterative(Node<T> node, NodeVisitor<T> visitor, INext<T> next) {
    Stack<Node<T>> stack = new Stack<>();
    stack.push(node);
    while (!stack.isEmpty()) {
      Node<T> currentNode = stack.pop();
      visitor.visit(currentNode);
      for (int i = currentNode.getChildren().size() - 1; i >= 0; i--) {
        stack.push(currentNode.getChildren().get(i));
      }
    }
  }

  /**
   * Performs a recursive DFS traversal, visiting each node and recursing into child nodes.
   *
   * @param node the node to visit
   * @param visitor the visitor to apply to each node
   * @param next the strategy for determining the next nodes to visit
   */
  private void traverseRecursive(Node<T> node, NodeVisitor<T> visitor, INext<T> next) {
    visitor.visit(node);
    if (next.hasNext(node)) {
      for (Node<T> child : node.getChildren()) {
        traverseRecursive(child, visitor, next); // Recurse into child nodes
      }
    }
  }
}
