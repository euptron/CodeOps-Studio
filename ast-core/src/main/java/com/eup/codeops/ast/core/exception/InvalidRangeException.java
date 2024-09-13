package com.eup.codeops.ast.core.exception;

/**
 * Thrown when a program attempts to access a value outside the range or an indexable collection
 *
 * @version 1.0
 * @since 1.0
 * @author EUP (2024-08-26)
 */
public final class InvalidRangeException extends IndexOutOfBoundsException {

  /**
   * Returns a new {@code InvalidRangeException} that includes the current stack trace.
   *
   * @param formIndex the start location of the indexable collection
   * @param toIndex the end location of the indexable collection
   * @param size the size of the indexable collection
   */
  public static InvalidRangeException throwException(int fromIndex, int toIndex, int size) {
    String detailedMessage =
        String.format(
            "range=%s, size=%s, formIndex=%s, toIndex=%s",
            (toIndex - fromIndex), size, fromIndex, toIndex);
    return new InvalidRangeException(detailedMessage);
  }

  public InvalidRangeException(String detailMessage) {
    super(detailMessage);
  }
}
