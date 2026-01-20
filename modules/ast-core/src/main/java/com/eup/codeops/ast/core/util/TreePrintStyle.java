package com.eup.codeops.ast.core.util;

import java.util.Objects;

/**
 * Defines visual styles for tree structure display. Configures symbols and spacing for tree
 * branches and annotations.
 *
 * @author EUP
 * @since 1.0.3 beta
 */
public final class TreePrintStyle {

  /** Default spacing between tree levels */
  public static final String DEFAULT_PADDING = " ";

  /** Symbol for final node in a branch */
  private final String lastChildPointer;

  /** Symbol for non-final node in a branch */
  private final String nonLastChildPointer;

  /** Vertical line symbol for nested levels */
  private final String verticalIndentMarker;

  /** Horizontal spacing between levels */
  private final String horizontalIndentMarker;

  /** Opening symbol for annotations */
  private final String annotationPrefix;

  /** Closing symbol for annotations */
  private final String annotationSuffix;

  /**
   * Creates new tree display style configuration.
   *
   * @param lastChildPointer Symbol for final node in branch
   * @param nonLastChildPointer Symbol for intermediate nodes
   * @param verticalIndentMarker Vertical line symbol
   * @param horizontalIndentPadding Space between levels
   * @param annotationPrefix Opening symbol for notes
   * @param annotationSuffix Closing symbol for notes
   */
  public TreePrintStyle(
      String lastChildPointer,
      String nonLastChildPointer,
      String verticalIndentMarker,
      int horizontalIndentPadding,
      String annotationPrefix,
      String annotationSuffix) {

    this.lastChildPointer = Objects.requireNonNull(lastChildPointer);
    this.nonLastChildPointer = Objects.requireNonNull(nonLastChildPointer);
    this.verticalIndentMarker = Objects.requireNonNull(verticalIndentMarker);

    if (horizontalIndentPadding < 0) {
      throw new IllegalArgumentException("Padding must be >= 0");
    }

    this.horizontalIndentMarker = DEFAULT_PADDING.repeat(horizontalIndentPadding);
    this.annotationPrefix = Objects.requireNonNull(annotationPrefix);
    this.annotationSuffix = Objects.requireNonNull(annotationSuffix);

    if (lastChildPointer.length() != nonLastChildPointer.length()) {
      throw new IllegalArgumentException("Pointer lengths must match");
    }
  }

  /**
   * @return Symbol for final node in branch
   */
  public String getLastChildPointer() {
    return lastChildPointer;
  }

  /**
   * @return Symbol for intermediate nodes
   */
  public String getNonLastChildPointer() {
    return nonLastChildPointer;
  }

  /**
   * @return Vertical line symbol
   */
  public String getVerticalIndentMarker() {
    return verticalIndentMarker;
  }

  /**
   * @return Horizontal spacing between levels
   */
  public String getHorizontalIndentMarker() {
    return horizontalIndentMarker;
  }

  /**
   * @return Opening symbol for notes
   */
  public String getAnnotationPrefix() {
    return annotationPrefix;
  }

  /**
   * @return Closing symbol for notes
   */
  public String getAnnotationSuffix() {
    return annotationSuffix;
  }
}
