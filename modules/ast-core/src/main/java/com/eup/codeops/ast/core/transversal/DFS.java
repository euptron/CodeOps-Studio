package com.eup.codeops.ast.core.transversal;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.eup.codeops.ast.core.node.Node;
import com.eup.codeops.ast.core.visitors.NodeVisitor;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.Stack;

/**
 * Implements a Depth-First Search (DFS) traversal strategy for visiting nodes in a data structure.
 * This class supports both pre and post order DFS traversal methods.
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
  
  @Override
  public Node<T> traverse(@NonNull Node<T> node, @NonNull NodeVisitor<T> visitor, @NonNull INext<T> next) {
    return traverse(node, visitor, next, null);
  }
  
  @Override
  public Node<T> traverse(@NonNull Node<T> node, @NonNull NodeVisitor<T> visitor, @NonNull INext<T> next, @Nullable IExit<T> exitCondition) {
    Objects.requireNonNull(node, "Node cannot be null");
    Objects.requireNonNull(visitor, "NodeVisitor cannot be null");
    Objects.requireNonNull(next, "INext cannot be null");
    
    // Pre-Order DFS
    Stack<Node<T>> stack = new Stack<>();
    Set<Node<T>> visited = new HashSet<>();
    stack.push(node);
    
    while (!stack.isEmpty()) {
      Node<T> currentNode = stack.pop();
      if (visited.contains(currentNode)) continue;
      visitor.visit(currentNode);
      visited.add(currentNode);
      if (exitCondition != null && exitCondition.exit(currentNode)) return node;
      if (next.hasNext(currentNode)) {
        // Reverse to maintain natural order
        for (int i = currentNode.getChildren().size() - 1; i >= 0; i--) {
          Node<T> child = currentNode.getChildren().get(i);
          
          if (!visited.contains(child)) {
            stack.push(child);
          }
        }
      }
    }
    return node;
  }

  public void traversePostOrder(Node<T> node, NodeVisitor<T> visitor, INext<T> next, IExit<T> exitCondition) {
    Objects.requireNonNull(node, "Node cannot be null");
    Objects.requireNonNull(visitor, "NodeVisitor cannot be null");
    Objects.requireNonNull(next, "INext cannot be null");
    
    Stack<Node<T>> stack = new Stack<>();
    Stack<Node<T>> result = new Stack<>();
    Set<Node<T>> visited = new HashSet<>();

    stack.push(node);
    while (!stack.isEmpty()) {
      Node<T> current = stack.pop();
      if (visited.contains(current)) {
        result.push(current);
        continue;
      }
      
      visited.add(current);
      result.push(current);
      
      if (next.hasNext(current)) {
        for (Node<T> child : current.getChildren()) {
            stack.push(child);
        }
      }
    }

    // Process nodes in reverse order (post-order)
    while (!result.isEmpty()) {
        Node<T> current = result.pop();
        visitor.visit(current);
        if (exitCondition != null && exitCondition.exit(current)) return;
    }
  }
}