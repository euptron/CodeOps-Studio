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
import com.eup.codeopsstudio.R;
import com.eup.codeopsstudio.common.Constants;
import com.eup.codeopsstudio.common.ILog;
import com.eup.codeopsstudio.editor.langs.textmate.provider.JsonLanguageInfoProvider;
import com.eup.codeopsstudio.palette.PaletteItem;
import com.eup.codeopsstudio.common.util.PreferencesUtils;
import com.eup.codeopsstudio.ui.editor.code.CodeEditorPane;
import java.util.*;

/**
 * A command provider specialized in listing and filtering syntax highlighting languages.
 *
 * <p>Triggered by the {@code @syntax} prefix, this provider bridges the UI with the
 * {@link com.eup.codeopsstudio.editor.langs.textmate.provider.JsonLanguageInfoProvider}
 * to dynamically populate available language modes (e.g., Java, Kotlin, Python).
 *
 * @author Etido Peter
 */
public class SyntaxProvider implements CommandProvider {

  public static final String TAG = "SyntaxProvider";
  private static final String SCOPE_PATH = Constants.TEXTMATE_ASSET_SCOPE_PATH;

  private final Context context;
  private final CodeEditorPane codeEditorPane;
  private JsonLanguageInfoProvider languageInfoProvider;
  private final List<PaletteItem> syntaxItems = new ArrayList<>();

  public SyntaxProvider(Context context, CodeEditorPane codeEditorPane) {
    this.context = context;
    this.codeEditorPane = codeEditorPane;
    try {
      this.languageInfoProvider =
          new JsonLanguageInfoProvider(context.getAssets().open(SCOPE_PATH));
      buildSyntax();
    } catch (Exception e) {
      ILog.error(TAG, "Failed to initialize provider", e);
    }
  }

  @Override
  public String getTrigger() {
    return "@syntax";
  }

  @Override
  public List<PaletteItem> getItems(String rawQuery, String contentQuery) {
    List<PaletteItem> results = new ArrayList<>();
    results.add(new PaletteItem(context.getString(R.string.select_syntax_highlight)));

    String search = contentQuery.replace("-", "").trim().toLowerCase();

    for (PaletteItem item : syntaxItems) {
      boolean matchesTitle = item.getTitle().toLowerCase().contains(search);
      boolean matchesSubtitle =
          item.getSubtitle() != null && item.getSubtitle().toLowerCase().contains(search);

      if (matchesTitle || matchesSubtitle) {
        results.add(item);
      }
    }
    return results;
  }

  private void buildSyntax() {
    Map<String, String> scopeMap = languageInfoProvider.getScopeMap();
    int index = 1;

    for (Map.Entry<String, String> entry : scopeMap.entrySet()) {
      String ext = entry.getKey();
      String scope = entry.getValue();

      syntaxItems.add(
          new PaletteItem(
              "syntax_" + index++,
              ext,
              scope,
              context.getString(R.string.cpt_syntax),
              () -> {
                if (codeEditorPane != null) {
                  try {
                    final boolean autoComplete = PreferencesUtils.enableAutoComplete();
                    final boolean autoCloseBrackets = PreferencesUtils.enableBracketAutoClosing();
                    codeEditorPane
                        .getEditor()
                        .setEditorLanguage(ext, scope, autoComplete, autoCloseBrackets, false);
                  } catch (Exception e) {
                    String msg =
                        context.getString(
                            R.string.msg_editor_load_configs_failed,
                            context.getString(R.string.load).toLowerCase());
                    showToast(msg);
                    ILog.error(TAG, msg, e);
                  }
                } else {
                  showToast("Invalid CodeEditorPane");
                }
              }));
    }

    syntaxItems.sort((item1, item2) -> item1.getTitle().compareToIgnoreCase(item2.getTitle()));
  }

  public void showToast(String msg) {
    showToast(context, msg);
  }
}
