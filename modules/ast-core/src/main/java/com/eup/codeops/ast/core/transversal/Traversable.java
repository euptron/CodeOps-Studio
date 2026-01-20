package com.eup.codeops.ast.core.transversal;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.eup.codeops.ast.core.node.Node;
import com.eup.codeops.ast.core.visitors.NodeVisitor;

/**
 * Defines a contract for traversing a data structure, such as an Abstract Syntax Tree (AST). This
 * interface provides a standardized way of iterating over nodes in a data structure, allowing for
 * flexible and reusable traversal logic.
 *
 * @param <T> the type of data stored in the nodes
 * @version 1.0
 * @since 1.0
 * @author EUP (2024-08-18)
 */
public interface Traversable<T> {

  /**
   * Traverses the data structure starting from the given node, applying the provided visitor to
   * each node.
   *
   * @param node the starting node for traversal
   * @param visitor the visitor to apply to each node
   * @param next the strategy for determining the next nodes
   * @return the traversed node
   */
  Node<T> traverse(@NonNull Node<T> node, @NonNull NodeVisitor<T> visitor, @NonNull INext<T> next);

  /**
   * Traverses the data structure starting from the given node, applying the provided visitor to
   * each node.
   *
   * @param node the starting node for traversal
   * @param visitor the visitor to apply to each node
   * @param next the strategy for determining the next nodes
   * @param exitCondition the condition that determines when we exit search
   * @return the traversed node
   * @since 1.0.3 beta
   */
  Node<T> traverse(
      @NonNull Node<T> node,
      @NonNull NodeVisitor<T> visitor,
      @NonNull INext<T> next,
      @Nullable IExit<T> exitCondition);
}
