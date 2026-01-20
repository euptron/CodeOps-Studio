package com.eup.codeops.ast.core.util;

public final class TreePrintStyles {

  private TreePrintStyles() {
    // Prevent instantiation
  }

  public static final TreePrintStyle DEFAULT = new TreePrintStyle("└─", "├─", "│", 2, "<", ">");

  // ASCII Styles
  public static final TreePrintStyle ASCII_BASIC =
      new TreePrintStyle("\\--- ", "+--- ", "|", 4, "<", ">");

  public static final TreePrintStyle ASCII_COMPACT =
      new TreePrintStyle("`-- ", "|-- ", "|", 3, "[", "]");

  // Unicode Styles
  public static final TreePrintStyle UNICODE_CLASSIC =
      new TreePrintStyle("└── ", "├── ", "│", 3, "‹", "›");

  public static final TreePrintStyle UNICODE_ROUNDED =
      new TreePrintStyle("╰── ", "├── ", "│", 3, "«", "»");

  public static final TreePrintStyle UNICODE_BOX =
      new TreePrintStyle("└─ ", "├─ ", "│", 2, "⟪", "⟫");

  // Windows-compatible Styles
  public static final TreePrintStyle WINDOWS_CONSOLE =
      new TreePrintStyle("└──", "├──", "│", 2, "<", ">");

  // Programming-specific Styles
  public static final TreePrintStyle MARKDOWN =
      new TreePrintStyle("└── ", "├── ", "│", 4, "`", "`");

  public static final TreePrintStyle JSON = new TreePrintStyle("\"", "\"", "", 2, "\"", "\"");

  // Framework-inspired Styles
  public static final TreePrintStyle NPM = new TreePrintStyle("`-- ", "+-- ", "|", 3, "[", "]");

  public static final TreePrintStyle MAVEN = new TreePrintStyle("\\- ", "|- ", "|", 2, "{", "}");
}
