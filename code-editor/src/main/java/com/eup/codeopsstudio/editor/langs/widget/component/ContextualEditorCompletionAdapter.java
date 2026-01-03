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

package com.eup.codeopsstudio.editor.langs.widget.component;

import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.eup.codeopsstudio.editor.R;
import com.eup.codeopsstudio.editor.langs.completion.ContextualCompletionItem;
import com.google.android.material.textview.MaterialTextView;

import io.github.rosemoe.sora.lang.completion.CompletionItem;
import io.github.rosemoe.sora.widget.component.EditorCompletionAdapter;
import io.github.rosemoe.sora.widget.schemes.EditorColorScheme;

/**
 * Completion mAdapter to display results
 *
 * @author Etido Peter
 */
public final class ContextualEditorCompletionAdapter extends EditorCompletionAdapter {

    private int itemHeight = 45; // dp

    @Override
    public int getItemHeight() {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, itemHeight, getContext()
            .getResources().getDisplayMetrics());
    }

    @Override
    public View getView(int pos, View view, ViewGroup parent, boolean isCurrentCursorPosition) {
        if (view == null) {
            view = LayoutInflater.from(getContext())
                                 .inflate(R.layout.editor_completion_result_item, parent, false);
        }
        CompletionItem item = getItem(pos);

        MaterialTextView tv = view.findViewById(R.id.result_item_label);
        LinearLayout compHolder = view.findViewById(R.id.result_comp_desc_holder);

        tv.setText(item.label);
        tv = view.findViewById(R.id.result_item_desc);
        tv.setText(item.desc);

        if (item instanceof ContextualCompletionItem) {
            ContextualCompletionItem comp = (ContextualCompletionItem) getItem(pos);
            if (comp != null) {
                tv = view.findViewById(R.id.result_item_comp_desc);
                tv.setText(comp.compDescription);
                compHolder.setVisibility(View.VISIBLE);
            } else {
                if (compHolder.getVisibility() == View.VISIBLE) {
                    compHolder.setVisibility(View.GONE);
                }
            }
        }

        view.setTag(pos);
        if (isCurrentCursorPosition) {
            view.setBackgroundColor(getThemeColor(EditorColorScheme.COMPLETION_WND_ITEM_CURRENT));
        } else {
            view.setBackgroundColor(0);
        }
        ImageView iv = view.findViewById(R.id.result_item_image);
        iv.setImageDrawable(item.icon);
        return view;
    }

    public void setItemHeight(final int value) {
        this.itemHeight = value;
    }
}
