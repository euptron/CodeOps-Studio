package com.eup.codeops.ast.core.exception;

/**
 * Thrown when code requests a {@link Node} that does not exist.
 *
 * @see Property#of(java.lang.Class, java.lang.Class, java.lang.String)
 * @version 1.0
 * @since 1.0
 * @author EUP (2024-08-18)
 */
public class NodeNotFoundException extends RuntimeException {

  public NodeNotFoundException(String s) {
    super(s);
  }
}
