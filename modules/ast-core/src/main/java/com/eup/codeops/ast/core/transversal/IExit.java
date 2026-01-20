package com.eup.codeops.ast.core.transversal;

import androidx.annotation.NonNull;
import com.eup.codeops.ast.core.node.Node;

public interface IExit<T> {
  /**
   * Exits a traversal if the required condition is met.
   *
   * @param node the node to check
   * @return true if the node has a next node, false otherwise
   */
  boolean exit(@NonNull Node<T> node);
}
