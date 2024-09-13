/*************************************************************************
 * This file is part of CodeOps Studio.
 * CodeOps Studio - code anywhere anytime
 * https://github.com/euptron/CodeOps-Studio
 * Copyright (C) 2024 EUP
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
 * If you have more questions, feel free to message EUP if you have any
 * questions or need additional information. Email: etido.up@gmail.com
 *************************************************************************/
 
   package com.eup.codeopsstudio.ui.editor.actions;

import android.content.Context;
import android.util.AttributeSet;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.eup.codeopsstudio.common.Constants;
import com.eup.codeopsstudio.common.util.PreferencesUtils;
import com.eup.codeopsstudio.editor.ContextualCodeEditor;
import java.util.ArrayList;
import java.util.List;

public class EditorShortcutView extends RecyclerView {

  private EditorShortcutAdapter adapter;
  private int numberOfTabs;
  private boolean useTabs;

  public EditorShortcutView(Context context) {
    super(context);
    init();
  }

  public EditorShortcutView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public EditorShortcutView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init();
  }

  private void init() {
    setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
    adapter = new EditorShortcutAdapter();
    setAdapter(adapter);
    adapter.submitList(getActions());
  }

  /** Bind editor for the view */
  public void bindEditor(@NonNull ContextualCodeEditor editor) {
    adapter.bindEditor(editor);
  }

  private List<EditorAction> getActions() {
    List<EditorAction> shortcutActions = new ArrayList<>();
    numberOfTabs = PreferencesUtils.getCodeEditorTabSize();
    useTabs = PreferencesUtils.useTabIndentation();

    var indentation = useTabs ? Constants.TAB.repeat(numberOfTabs) : " ".repeat(numberOfTabs);
    shortcutActions.add(new EditorAction("TAB", indentation));
    shortcutActions.add(new EditorAction("{", "{}"));
    shortcutActions.add(new EditorAction("}", "}"));
    shortcutActions.add(new EditorAction("(", "()"));
    shortcutActions.add(new EditorAction(")", ")"));
    shortcutActions.add(new EditorAction(",", ","));
    shortcutActions.add(new EditorAction(".", "."));
    shortcutActions.add(new EditorAction(";", ";"));
    shortcutActions.add(new EditorAction("\"", "\""));
    shortcutActions.add(new EditorAction("?", "?"));
    shortcutActions.add(new EditorAction("+", "+"));
    shortcutActions.add(new EditorAction("-", "-"));
    shortcutActions.add(new EditorAction("*", "*"));
    shortcutActions.add(new EditorAction("/", "/"));
    shortcutActions.add(new EditorAction("[", "[]"));
    shortcutActions.add(new EditorAction("]", "]"));
    shortcutActions.add(new EditorAction("<", "<>"));
    shortcutActions.add(new EditorAction(">", ">"));
    shortcutActions.add(new EditorAction("=", "="));
    // selection names
    shortcutActions.add(new EditorAction("←"));
    shortcutActions.add(new EditorAction("→"));
    shortcutActions.add(new EditorAction("↑"));
    shortcutActions.add(new EditorAction("↓"));
    shortcutActions.add(new EditorAction("home"));
    shortcutActions.add(new EditorAction("end"));
    return shortcutActions;
  }

  public void updateTabSize(int newSize) {
    numberOfTabs = newSize;
  }

  public void useTabIndentation(boolean enabled) {
    useTabs = enabled;
  }
}
