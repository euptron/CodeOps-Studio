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

package com.eup.codeopsstudio.palette.registry;

import android.content.Context;
import android.widget.EditText;
import com.eup.codeopsstudio.palette.PaletteItem;
import com.eup.codeopsstudio.ui.editor.code.CodeEditorPane;
import com.eup.codeopsstudio.palette.providers.*;
import java.util.ArrayList;
import java.util.List;

/**
 * The central registry and dispatcher for command palette queries.
 *
 * <p>This class acts as the "brain" of the palette. It maintains a list of registered
 * {@link com.eup.codeopsstudio.palette.providers.CommandProvider} instances and routes
 * user input to the appropriate provider based on the query prefix (trigger).
 *
 * <p>It implements a "Longest Prefix Match" strategy to ensure that specific triggers
 * (e.g., "@syntax") take precedence over general ones (e.g., "@").
 *
 * @author Etido Peter
 */
public class PaletteRegistry {

  private final RecentProvider recentProvider;
  private final List<CommandProvider> providers = new ArrayList<>();

  public PaletteRegistry(Context context, EditText searchInput, CodeEditorPane cep) {
    this.recentProvider = new RecentProvider(context);

    providers.add(new SyntaxProvider(context, cep));
    providers.add(new SymbolProvider(context, searchInput));
    providers.add(new ActionProvider(context));
    providers.add(new LineProvider(context, cep));
    providers.add(new HelpProvider(context, searchInput));

    // Order Matters: Longest triggers should be registered first for specific matching
    providers.sort((p1, p2) -> Integer.compare(p2.getTrigger().length(), p1.getTrigger().length()));
    // Fallback (Empty trigger)
    providers.add(new FileProvider(context));
  }

  public List<PaletteItem> search(String query) {
    if (query == null || query.isEmpty()) {
      return recentProvider.getItems("", "");
    }

    int maxLen = -1;
    String cleanQuery = query;
    CommandProvider bestMatch = null;

    // use provider with longest matching trigger
    for (CommandProvider provider : providers) {
      String trigger = provider.getTrigger();
      if (cleanQuery.startsWith(trigger)) {
        if (trigger.length() > maxLen) {
          maxLen = trigger.length();
          bestMatch = provider;
        }
      }
    }

    if (bestMatch != null) {
      String content = cleanQuery.substring(bestMatch.getTrigger().length());
      return bestMatch.getItems(cleanQuery, content);
    }

    return new ArrayList<>();
  }
}
