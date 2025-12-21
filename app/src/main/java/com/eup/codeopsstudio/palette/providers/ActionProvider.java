/*
 * This file is part of CodeOps Studio.
 * CodeOps Studio - Code anywhere anytime
 * https://github.com/euptron/CodeOps-Studio
 * Copyright (C) 2024-2025 Etido Peter
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
import java.util.ArrayList;
import java.util.List;

/**
 * A command provider that handles global editor actions.
 *
 * <p>Triggered by the {@code >} prefix, this provider exposes general IDE commands such as "Toggle
 * Word Wrap", "Format Document", or "Open Settings". It connects UI selection events to the
 * underlying {@code CommandManager}.
 *
 * @author Etido Peter
 */
public class ActionProvider implements CommandProvider {
  private final Context context;
  private final List<PaletteItem> globalCommands = new ArrayList<>();

  public ActionProvider(Context context) {
    this.context = context;
    seedCommands();
  }

  @Override
  public String getTrigger() {
    return ">";
  }

  @Override
  public List<PaletteItem> getItems(String rawQuery, String contentQuery) {
    List<PaletteItem> results = new ArrayList<>();
    results.add(new PaletteItem("Global Commands"));

    String search = contentQuery.toLowerCase().trim();
    for (PaletteItem item : globalCommands) {
      if (item.getTitle().toLowerCase().contains(search)) {
        results.add(item);
      }
    }
    return results;
  }

  private void seedCommands() {
    globalCommands.add(
        new PaletteItem("cmd1", "Toggle Word Wrap", "View", "Cmd", () -> toast("Wrap Toggled")));

    globalCommands.add(
        new PaletteItem("cmd2", "Change Color Theme", "Preferences", "Cmd", () -> toast("Theme")));

    globalCommands.add(
        new PaletteItem("cmd3", "Format Document", "Formatter", "Cmd", () -> toast("Formatted")));
  }
}
