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
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;

/**
 * A pane designed for displaying non editable text content within a scrollable view.
 *
 * @author Etido Peter
 * @version 1.0
 * @see com.eup.codeopsstudio.pane
 */
public class TextPane extends Pane {

    private ScrollView scrollView;
    private TextView textView;
    private String content;

    public TextPane(Context context, String title, boolean generateUUID) {
        super(context, title, generateUUID);
        content = "";
    }

    @Override
    public View onCreateView() {
        scrollView = new ScrollView(getContext());
        scrollView.setLayoutParams(new ScrollView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        textView = new TextView(getContext());
        textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT));

        scrollView.addView(textView);
        return scrollView;
    }

    @Override
    public void onViewCreated(@NonNull View view) {
        super.onViewCreated(view);
        int padding = pxToDp(requireContext(), 8);
        textView.setPadding(padding, padding, padding, padding);
        textView.setTextSize(14);
        setText(content);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        textView   = null;
        scrollView = null;
    }

    @Override
    public void persist() {
        super.persist();
        addArguments(PaneConstants.TEXT_PANE_ARGUMENT_KEY, getText());
    }

    public String getText() {
        if (hasPerformedCreateView() && textView != null) {
            return textView
                .getText()
                .toString();
        }

        return "";
    }

    /**
     * Sets the text content of this TextPane.
     *
     * @param text The text content to be set.
     */
    public void setText(String text) {
        if (hasPerformedCreateView() && textView != null) textView.setText(text);
    }

    public static int pxToDp(@NonNull Context context, float px) {
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, px, context
            .getResources()
            .getDisplayMetrics()));
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
