package com.eup.codeops.ast.core.visitors;

import com.eup.codeops.ast.core.node.Node;

/**
 * Interface for visiting nodes in a data structure, such as an Abstract Syntax Tree (AST).
 * Implementations can perform custom operations on nodes during traversal.
 *
 * @param <T> the type of data stored in the nodes
 * @version 1.0
 * @since 1.0
 * @author EUP (2024-08-18)
 */
public interface NodeVisitor<T> {

  /**
   * Visits the specified node to perform custom operations.
   *
   * @param node the node to visit
   */
  void visit(Node<T> node);
}
