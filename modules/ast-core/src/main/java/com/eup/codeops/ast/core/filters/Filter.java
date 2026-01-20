package com.eup.codeops.ast.core.filters;

/**
 * Base filter class
 *
 * @param <T> the type of object to filter
 * @version 1.0
 * @since 1.0
 * @author EUP (2024-08-22)
 */
public abstract class Filter<T> {
  /**
   * Filters the object
   *
   * @param object the object to filter
   * @return true if object satisfies conditions, false otherwise
   */
  public abstract boolean filter(T object);
}
