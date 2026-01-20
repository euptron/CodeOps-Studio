package com.eup.codeops.ast.core.transversal;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.eup.codeops.ast.core.node.Node;
import com.eup.codeops.ast.core.visitors.NodeVisitor;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

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

  public static final String TAG = "BreadthFirstSearch";

  /** Safe breadth threshold to prevent excessive memory usage. */
  static final int SAFE_BREADTH_THRESHOLD = 500;

  private final Executor asyncExecutor =
      Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

  @Override
  public Node<T> traverse(
      @NonNull Node<T> node, @NonNull NodeVisitor<T> visitor, @NonNull INext<T> next) {
    return traverse(node, visitor, next, null);
  }

  @Override
  public Node<T> traverse(
      @NonNull Node<T> node,
      @NonNull NodeVisitor<T> visitor,
      @NonNull INext<T> next,
      @Nullable IExit<T> exitCondition) {
    Objects.requireNonNull(node, "Node cannot be null");
    Objects.requireNonNull(visitor, "NodeVisitor cannot be null");
    Objects.requireNonNull(next, "INext cannot be null");

    // TODO: Use better heuristics like determining number of children and grand, depth etc
    if (node.getDepth() >= SAFE_BREADTH_THRESHOLD) {
      traverseAsync(node, visitor, next, exitCondition);
    } else {
      traverseIterative(node, visitor, next, exitCondition);
    }
    return node;
  }

  /**
   * Performs an iterative BFS traversal, using a queue to store nodes to visit.
   *
   * @param node the node to start the traversal from
   * @param visitor the visitor to apply to each node
   * @param next the strategy for determining the next nodes to visit
   * @param exitCondition the condition that determines when we exit search
   */
  private void traverseIterative(
      Node<T> node, NodeVisitor<T> visitor, INext<T> next, IExit<T> exitCondition) {
    Queue<Node<T>> queue = new LinkedList<>();
    queue.add(node);

    while (!queue.isEmpty()) {
      Node<T> current = queue.poll();
      visitor.visit(current);
      if (exitCondition != null && exitCondition.exit(current)) return;
      if (next.hasNext(current)) {
        queue.addAll(current.getChildren());
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
   * @param exitCondition the condition that determines when we exit search
   */
  private void traverseAsync(
      Node<T> node, NodeVisitor<T> visitor, INext<T> next, IExit<T> exitCondition) {
    Queue<Node<T>> queue = new ConcurrentLinkedQueue<>();
    AtomicBoolean shouldExit = new AtomicBoolean(false);
    queue.add(node);

    while (!queue.isEmpty() && !shouldExit.get()) {
      int levelSize = queue.size();
      List<CompletableFuture<Void>> futures = new ArrayList<>(levelSize);

      for (int i = 0; i < levelSize; i++) {
        Node<T> current = queue.poll();
        if (current == null) continue;
        futures.add(
            CompletableFuture.runAsync(
                () -> {
                  processNode(current, visitor, next, queue, exitCondition, shouldExit);
                },
                asyncExecutor));
      }
      // Wait for level completion before proceeding
      CompletableFuture.allOf(futures.toArray(new CompletableFuture<?>[0])).join();
      if (shouldExit.get()) break;
    }
  }

  /**
   * Processes a single node, visiting it and adding its children to the queue if necessary.
   *
   * @param node the node to process
   * @param visitor the visitor to apply to the node
   * @param next the strategy for determining the next nodes to visit
   * @param queue the queue of nodes to visit
   * @param exitCondition the condition that determines when we exit search
   */
  private void processNode(
      Node<T> node,
      NodeVisitor<T> visitor,
      INext<T> next,
      Queue<Node<T>> queue,
      IExit<T> exitCondition,
      AtomicBoolean shouldExit) {
    try {
      if (shouldExit.get()) return;

      visitor.visit(node);
      if (exitCondition.exit(node)) {
        shouldExit.set(true);
        return;
      }

      if (next.hasNext(node)) {
        queue.addAll(node.getChildren());
      }
    } catch (Exception e) {
      System.out.println(
          TAG
              + " Parallel node processing failure\n"
              + "│ Node: %s (Level: %d, Children: %d)\n"
              + "│ Error: %s (%s)\n"
              + "│ Context: Async traversal stage [Level processing]\n"
              + "╰> Stacktrace: %s");
      shouldExit.set(true);
    }
  }
}
