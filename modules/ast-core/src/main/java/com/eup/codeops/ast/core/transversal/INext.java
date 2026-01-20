package com.eup.codeops.ast.core.transversal;

import androidx.annotation.NonNull;
import com.eup.codeops.ast.core.node.Node;

/**
 * Interface for determining if a node has a next node in a data structure traversal.
 *
 * @param <T> the type of data stored in the nodes
 * @version 1.0
 * @since 1.0
 * @author EUP (2024-08-18)
 */
public interface INext<T> {

  /**
   * Checks if the specified node has a subsequent node in the traversal.
   *
   * @param node the node to check
   * @return true if the node has a next node, false otherwise
   */
  boolean hasNext(@NonNull Node<T> node);
}
