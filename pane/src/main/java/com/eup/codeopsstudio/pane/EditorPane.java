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

package com.eup.codeopsstudio.pane;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;

import com.eup.codeopsstudio.editor.ContextualCodeEditor;
import com.eup.codeopsstudio.pane.databinding.LayoutPaneEditorBinding;

import io.github.rosemoe.sora.widget.CodeEditor;

/**
 * A pane designed for displaying editable text content.
 *
 * @author Etido Peter
 * @version 1.0
 * @see com.eup.codeopsstudio.pane
 */
public class EditorPane extends Pane {

    private final String content;
    private LayoutPaneEditorBinding binding;
    private ContextualCodeEditor editor;

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
    public void onViewCreated(@NonNull View view) {
        super.onViewCreated(view);
        editor                         = binding.editor;
        editor.getProps().stickyScroll = true;
        editor.setLineSpacing(2f, 1.1f);
        editor.setNonPrintablePaintingFlags(
            CodeEditor.FLAG_DRAW_WHITESPACE_LEADING | CodeEditor.FLAG_DRAW_LINE_SEPARATOR
                | CodeEditor.FLAG_DRAW_WHITESPACE_IN_SELECTION);
        editor.setTextSize(14);
        setText(content);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (editor != null) {
            editor.release();
        }
        editor  = null;
        binding = null;
    }

    @Override
    public void persist() {
        super.persist();
        addArguments(PaneConstants.EDITOR_PANE_ARGUMENT_KEY, getText());
    }

    public String getText() {
        if (hasPerformedCreateView() && editor != null) {
            return editor
                .getText()
                .toString();
        }

        return "";
    }

    /**
     * Sets the text content of this EditorPane.
     *
     * @param text The text content to be set.
     */
    public void setText(String text) {
        if (hasPerformedCreateView() && editor != null) editor.setText(text);
    }
}
