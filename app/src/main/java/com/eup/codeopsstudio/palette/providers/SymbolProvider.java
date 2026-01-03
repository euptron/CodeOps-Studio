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
import android.widget.EditText;
import com.eup.codeopsstudio.palette.PaletteItem;
import java.util.ArrayList;
import java.util.List;

/**
 * A multi-functional provider handling symbol navigation and plugin management.
 *
 * <p>Triggered by the {@code @} prefix, this provider manages complex sub-queries such as:
 *
 * <ul>
 *   <li><b>Symbol Search:</b> Navigating to classes or methods in the current file.
 *   <li><b>Plugin Commands:</b> Handling arguments like {@code @plugins -git} to expose specific
 *       sub-menus.
 * </ul>
 *
 * @author Etido Peter
 */
public class SymbolProvider implements CommandProvider {
  private final Context context;
  private final EditText searchInput;

  public SymbolProvider(Context context, EditText searchInput) {
    this.context = context;
    this.searchInput = searchInput;
  }

  @Override
  public String getTrigger() {
    return "@";
  }

  @Override
  public List<PaletteItem> getItems(String rawQuery, String contentQuery) {
    List<PaletteItem> results = new ArrayList<>();

    if (rawQuery.contains("-git")) {
      handleGitPlugin(results, rawQuery);
    } else if (rawQuery.contains("-java")) {
      handleJavaPlugin(results);
    } else {
      results.add(new PaletteItem("Go to Symbol / Plugins"));
      String search = contentQuery.toLowerCase();

      // Suggestions to enter plugin modes
      if ("plugins -git".contains(search)) {
        results.add(
            new PaletteItem(
                "p_git",
                "plugins -git",
                "Git commands",
                "Plugin",
                () -> {
                  searchInput.setText("@plugins -git ");
                  searchInput.setSelection(searchInput.getText().length());
                }));
      }
      if ("plugins -java".contains(search)) {
        results.add(
            new PaletteItem(
                "p_java",
                "plugins -java",
                "Java Compiler",
                "Plugin",
                () -> {
                  searchInput.setText("@plugins -java ");
                  searchInput.setSelection(searchInput.getText().length());
                }));
      }
    }
    return results;
  }

  private void handleGitPlugin(List<PaletteItem> list, String query) {
    list.add(new PaletteItem("Git Plugin Active"));

    // Logic: "@plugins -git > commit"
    if (query.contains(">")) {
      String gitCmd = query.substring(query.indexOf(">") + 1).trim();
      list.add(
          new PaletteItem(
              "exec_git_" + gitCmd,
              "git " + gitCmd,
              "Execute JGit command",
              "JGit",
              null,
              () -> {
                toast("Executing JGit: " + gitCmd);
                // TODO: call GitManager here
              }));
    } else {
      // TODO:  Show available Git options
      list.add(
          new PaletteItem(
              "g_commit",
              "Commit",
              "Stage and commit changes",
              "JGit",
              () -> searchInput.append(" > commit")));

      list.add(
          new PaletteItem(
              "g_pull", "Pull", "Pull from origin", "JGit", () -> searchInput.append(" > pull")));

      list.add(
          new PaletteItem(
              "g_status", "Status", "Show status", "JGit", () -> toast("Executing Status")));
    }
  }

  private void handleJavaPlugin(List<PaletteItem> list) {
    list.add(new PaletteItem("Java Plugin Active"));
    list.add(
        new PaletteItem(
            "j_comp",
            "Javac: Compile",
            "Compile current file",
            "Java",
            () -> toast("Running javac...")));
    list.add(
        new PaletteItem(
            "j_run", "Java: Run", "Run main method", "Java", () -> toast("Running java...")));
  }
}
