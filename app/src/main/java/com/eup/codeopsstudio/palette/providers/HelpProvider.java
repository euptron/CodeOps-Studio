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
import android.widget.EditText;
import com.eup.codeopsstudio.R;
import com.eup.codeopsstudio.palette.PaletteItem;
import java.util.ArrayList;
import java.util.List;


/**
 * A command provider responsible for aiding navigation.
 *
 * @author Etido Peter
 */
public class HelpProvider implements CommandProvider {
  private final Context context;
  private final EditText searchInput;

  public HelpProvider(Context context, EditText searchInput) {
    this.context = context;
    this.searchInput = searchInput;
  }

  @Override
  public String getTrigger() {
    return "?";
  }

  @Override
  public List<PaletteItem> getItems(String rawQuery, String contentQuery) {
    List<PaletteItem> results = new ArrayList<>();
    results.add(new PaletteItem(context.getString(R.string.help)));

    results.add(
        new PaletteItem(
            "h1",
            ">",
           context.getString(R.string.show_commands),
            context.getString(R.string.help),
            () -> {
              searchInput.setText(">");
              searchInput.setSelection(1);
            }));

    results.add(
        new PaletteItem(
            "h2",
            "@",
            context.getString(R.string.goto_symbols_or_plugins),
           context.getString(R.string.help),
            () -> {
              searchInput.setText("@");
              searchInput.setSelection(1);
            }));

    results.add(
        new PaletteItem(
            "h3",
            ":",
            context.getString(R.string.go_to_line) ,
            context.getString(R.string.help),
            () -> {
              searchInput.setText(":");
              searchInput.setSelection(1);
            }));

    return results;
  }
}
