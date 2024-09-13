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
 
   package com.eup.codeopsstudio.pane;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import com.eup.codeopsstudio.editor.ContextualCodeEditor;
import com.eup.codeopsstudio.pane.databinding.LayoutPaneEditorBinding;

/**
 * A pane designed for displaying editable text content.
 *
 * @see com.eup.codeopsstudio.pane
 * @author EUP
 * @version 1.0
 */
public class EditorPane extends Pane {

  private LayoutPaneEditorBinding binding;
  private ContextualCodeEditor editor;
  private String content;

  public EditorPane(Context context, String title) {
    this(context, title,  /* generate new uuid= */true);
  }

  public EditorPane(Context context, String title, boolean generateUUID) {
    super(context, title, generateUUID);
    content = "";
  }

  public ContextualCodeEditor getEditor() {
    return this.editor;
  }

  @Override
  public View onCreateView() {
    binding = LayoutPaneEditorBinding.inflate(LayoutInflater.from(getContext()));
    return binding.getRoot();
  }

  @Override
  public void onViewCreated(View view) {
    super.onViewCreated(view);
    editor = binding.editor;
    editor.getProps().stickyScroll = true;
    editor.setLineSpacing(2f, 1.1f);
    editor.setNonPrintablePaintingFlags(
        ContextualCodeEditor.FLAG_DRAW_WHITESPACE_LEADING
            | ContextualCodeEditor.FLAG_DRAW_LINE_SEPARATOR
            | ContextualCodeEditor.FLAG_DRAW_WHITESPACE_IN_SELECTION);

    editor.setTextSize(14);
    setText(content);
  }

  @Override
  public void persist() {
    super.persist();
    addArguments("editor_text", getText());
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    if (editor != null) {
      editor.release(); // Release editor resources
    }
    // Clear references to avoid potential memory leaks
    editor = null;
    binding = null;
  }

  /**
   * Sets the text content of this EditorPane.
   *
   * @param text The text content to be set.
   */
  public void setText(String text) {
    if (isAlive()) {
      if (editor != null)  editor.setText(text);
    }
  }

  public String getText() {
    if (isAlive()) {
      if (editor != null) return editor.getText().toString();
    }
    return "";
  }

  public void setContent(String textContent) {
    this.content = content;
  }

  public String getContent() {
    return content;
  }

  private boolean isAlive() {
    return getState() != PaneState.INVALID_STATE && hasPerformedCreateView;
  }
}
