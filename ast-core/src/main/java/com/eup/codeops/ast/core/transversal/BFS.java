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

import androidx.annotation.NonNull;
import com.eup.codeops.ast.core.node.Node;
import com.eup.codeops.ast.core.visitors.NodeVisitor;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;

/**
 * Implements a Breadth-First Search (BFS) traversal strategy for visiting nodes in a data
 * structure. This class offers both iterative and asynchronous traversal methods, with a
 * configurable breadth threshold to manage memory usage effectively.
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
 * BFS&lt;String&gt; bfs = new BFS&lt;&gt;();
 * bfs.traverse(root, visitor, next);
 * </pre>
 *
 * <p><strong>Note:</strong> BFS is suitable for scenarios where nodes closer to the root should be
 * processed before those further away. It is particularly useful for level-order traversal,
 * shortest path algorithms, and exploring nodes in layers.
 *
 * <p><strong>Comparison to DFS:</strong> Unlike Depth-First Search (DFS), which explores as far
 * down a branch as possible before backtracking, BFS explores nodes level by level. This makes BFS
 * ideal for finding the shortest path in an unweighted graph, while DFS is more suitable for deep
 * explorations or finding paths in complex structures.
 *
 * @param <T> the type of data stored in the nodes
 * @version 1.0
 * @since 1.0
 * @author EUP (2024-08-18)
 */
public class BFS<T> implements Traversable<T> {

  /** Safe breadth threshold to prevent excessive memory usage. */
  static final int SAFE_BREADTH_THRESHOLD = 500;

  @Override
  public Node<T> traverse(@NonNull Node<T> node, NodeVisitor<T> visitor, INext<T> next) {
    if (node.getDepth() >= SAFE_BREADTH_THRESHOLD) {
      traverseAsync(node, visitor, next);
    } else {
      traverseIterative(node, visitor, next);
    }
    return node;
  }

  /**
   * Performs an iterative BFS traversal, using a queue to store nodes to visit.
   *
   * @param node the node to start the traversal from
   * @param visitor the visitor to apply to each node
   * @param next the strategy for determining the next nodes to visit
   */
  private void traverseIterative(Node<T> node, NodeVisitor<T> visitor, INext<T> next) {
    Queue<Node<T>> queue = new LinkedList<>();
    queue.add(node);
    while (!queue.isEmpty()) {
      Node<T> currentNode = queue.poll();

      visitor.visit(currentNode);
      if (next.hasNext(currentNode)) {
        for (Node<T> child : currentNode.getChildren()) {
          queue.add(child);
        }
      }
    }
  }

  /**
   * Performs an asynchronous BFS traversal, using a queue to store nodes to visit and
   * CompletableFuture to process nodes concurrently.
   *
   * @param node the node to start the traversal from
   * @param visitor the visitor to apply to each node
   * @param next the strategy for determining the next nodes to visit
   */
  private void traverseAsync(Node<T> node, NodeVisitor<T> visitor, INext<T> next) {
    Queue<Node<T>> queue = new LinkedList<>();
    queue.add(node);
    while (!queue.isEmpty()) {
      List<CompletableFuture<Void>> futures = new ArrayList<>();
      int levelSize = queue.size();
      for (int i = 0; i < levelSize; i++) {
        Node<T> currentNode = queue.poll();
        CompletableFuture<Void> future =
            CompletableFuture.runAsync(() -> processNode(currentNode, visitor, next, queue));
        futures.add(future);
      }
      CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }
  }

  /**
   * Processes a single node, visiting it and adding its children to the queue if necessary.
   *
   * @param node the node to process
   * @param visitor the visitor to apply to the node
   * @param next the strategy for determining the next nodes to visit
   * @param queue the queue of nodes to visit
   */
  private void processNode(
      Node<T> node, NodeVisitor<T> visitor, INext<T> next, Queue<Node<T>> queue) {

    visitor.visit(node);
    if (next.hasNext(node)) {
      for (Node<T> child : node.getChildren()) {
        queue.add(child);
      }
    }
  }
}
