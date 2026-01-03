/*
 * This file is part of CodeOps Studio.
 * CodeOps Studio - Code anywhere anytime
 * https://github.com/euptron/CodeOps-Studio
 * Copyright (C) 2024-2026 Etido Peter
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/
 *
 * If you have more questions, feel free to message Etido Peter if you have any
 * questions or need additional information. Email: euptron@gmail.com
 */

package com.eup.codeopsstudio.palette.providers;

import android.content.Context;
import com.eup.codeopsstudio.palette.PaletteItem;
import com.eup.codeopsstudio.ui.editor.code.CodeEditorPane;
import com.eup.codeopsstudio.R;
import java.util.ArrayList;
import java.util.List;

/**
 * Line navigation provider for CodeOps Studio command palette.
 *
 * <p>Handles line number navigation commands triggered by the ":" prefix. This provider enables
 * quick navigation to specific lines within the code editor, improving developer productivity and
 * code navigation efficiency.
 *
 * <h3>Usage Examples</h3>
 *
 * <ul>
 *   <li>{@code :15} - Navigates to line 15
 *   <li>{@code :} - Shows line navigation help
 * </ul>
 *
 * @author Etido Peter
 * @see CommandProvider
 * @see PaletteItem
 * @see CodeEditorPane
 */
public class LineProvider implements CommandProvider {
  private final Context context;
  private final CodeEditorPane codeEditorPane;

  public LineProvider(Context context, CodeEditorPane codeEditorPane) {
    this.context = context;
    this.codeEditorPane = codeEditorPane;
  }

  @Override
  public String getTrigger() {
    return ":";
  }

  @Override
  public List<PaletteItem> getItems(String rawQuery, String contentQuery) {
    List<PaletteItem> results = new ArrayList<>();
    results.add(new PaletteItem(context.getString(R.string.go_to_line)));
    String cleanQuery = contentQuery.trim();

    if (codeEditorPane == null) {
      results.add(
          new PaletteItem(
              "invalid_code_editor",
              context.getString(R.string.invalid_code_editor),
              context.getString(R.string.line_navigation_not_available),
              context.getString(R.string.navigation_short),
              null,
              null));
      return results;
    }

    final int totalLineCount = codeEditorPane.getEditor().getLineCount();

    if (totalLineCount == -1) {
      results.add(
          new PaletteItem(
              "error_line",
              context.getString(R.string.line_navigation_not_available),
              "",
              context.getString(R.string.error),
              null,
              null));
      return results;
    }

    if (cleanQuery.isEmpty()) {
      results.add(
          new PaletteItem(
              "wait_line",
              context.getString(R.string.type_number_within_range, totalLineCount),
              context.getString(R.string.waiting),
              context.getString(R.string.navigation_short),
              null,
              null));
      return results;
    }

    try {
      final int line = Integer.parseInt(cleanQuery);

      if (line > 0) {
        if (line > totalLineCount) {
          results.add(
              new PaletteItem(
                  "error_line",
                  context.getString(R.string.invalid_goto_line_range, totalLineCount),
                  context.getString(R.string.invalid_range),
                  context.getString(R.string.error),
                  null,
                  null));
        } else {
          results.add(
              new PaletteItem(
                  "goto_" + line,
                  context.getString(R.string.goto_line, line),
                  context.getString(R.string.current_file),
                  context.getString(R.string.navigation_short),
                  () -> codeEditorPane.getEditor().jumpToLine(line - 1)));
        }
      } else {
        results.add(
            new PaletteItem(
                "wait_line",
                context.getString(R.string.line_number_rule_positive_integer),
                context.getString(R.string.waiting),
                context.getString(R.string.navigation_short),
                null,
                null));
      }
    } catch (NumberFormatException e) {
      results.add(
          new PaletteItem(
              "wait_line",
              context.getString(R.string.type_valid_line_number),
              context.getString(R.string.waiting),
              context.getString(R.string.navigation_short),
              null,
              null));
    }

    return results;
  }
}
