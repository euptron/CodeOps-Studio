package com.eup.codeops.ast.core.transversal;

import androidx.annotation.NonNull;
import com.eup.codeops.ast.core.node.Node;

/**
 * Observes changes to a Node.
 *
 * @param <T> the inner type of the Node being observed
 * @version 1.0
 * @since 1.0.0
 * @author EUP (2024-08-24)
 */
public interface Observer<T> {

  /**
   * Notifies the observer of changes to the observed Node.
   *
   * @param t the Node that has changed
   */
  void onChange(@NonNull Node<T> t);
}
