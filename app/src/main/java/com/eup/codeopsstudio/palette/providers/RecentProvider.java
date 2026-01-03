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
import java.util.ArrayList;
import java.util.List;

/**
 * A fallback provider that displays recently used commands.
 *
 * <p>This provider is activated when the command palette is opened in global mode with no user
 * input, offering quick access to frequently executed actions or files.
 *
 * @author Etido Peter
 */
public class RecentProvider implements CommandProvider {
  private final Context context;

  public RecentProvider(Context context) {
    this.context = context;
  }

  @Override
  public String getTrigger() {
    return "RECENTS_INTERNAL";
  }

  @Override
  public List<PaletteItem> getItems(String rawQuery, String contentQuery) {
    List<PaletteItem> results = new ArrayList<>();
    results.add(new PaletteItem("Recently Used"));

    // mock data
    results.add(
        new PaletteItem(
            "recent_1",
            "Open Settings",
            "UI Settings",
            "Recent",
            "Ctrl+Shift+R",
            () -> toast("Settings")));
    results.add(
        new PaletteItem(
            "recent_2", "git status", "JGit", "Recent", () -> toast("git status")));

    return results;
  }
}
