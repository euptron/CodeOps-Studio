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

package com.eup.codeopsstudio.ui.editor.actions.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.eup.codeopsstudio.R;
import com.eup.codeopsstudio.common.Constants;
import com.eup.codeopsstudio.databinding.LayoutEditorShortcutItemBinding;
import com.eup.codeopsstudio.editor.ContextualCodeEditor;
import com.eup.codeopsstudio.util.BaseUtil;
import com.eup.codeopsstudio.ui.editor.actions.models.EditorAction;

import java.util.ArrayList;
import java.util.List;

public class EditorShortcutAdapter extends RecyclerView.Adapter<EditorShortcutAdapter.ViewHolder> {

    private final List<EditorAction> actionList = new ArrayList<>();
    private ContextualCodeEditor editor;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutEditorShortcutItemBinding binding =
            LayoutEditorShortcutItemBinding.inflate(LayoutInflater.from(parent.getContext()),
                parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        EditorAction editorAction = actionList.get(position);
        holder.itemView.setAnimation(AnimationUtils.loadAnimation(holder.itemView.getContext(),
            android.R.anim.fade_in));
        holder.bind(actionList.get(position));

        holder.itemView.setOnClickListener(v -> {
            if (editor != null && editorAction != null) {
                String value = editorAction.getValue();
                String name = editorAction.getName();
                if (value != null) {
                    if (editor.isEditable()) {
                        if (value.equals(Constants.TAB) && editor.getSnippetController()
                                                                 .isInSnippet()) {
                            editor.getSnippetController().shiftToNextTabStop();
                        } else {
                            editor.commitText(value);
                        }
                    } else {
                        BaseUtil.toastShort(R.string.alrt_rom_cannot_edit_editor); // prompt only
                        // inserters
                    }
                }
                switch (name) {
                    case "MCL":
                        editor.moveSelectionLeft();
                        break;
                    case "MCR":
                        editor.moveSelectionRight();
                        break;
                    case "MCU":
                        editor.moveSelectionUp();
                        break;
                    case "MCD":
                        editor.moveSelectionDown();
                        break;
                    case "home":
                        editor.moveSelectionHome();
                        break;
                    case "end":
                        editor.moveSelectionEnd();
                        break;
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return actionList.size();
    }

    public void bindEditor(@NonNull ContextualCodeEditor editor) {
        this.editor = editor;
    }

    /**
     * Sets the data to be displayed by the mAdapter.
     *
     * @param actions The list of data.
     */
    @SuppressLint("NotifyDataSetChanged")
    public void submitList(List<EditorAction> actions) {
        actionList.clear();
        actionList.addAll(actions);
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView name;

        public ViewHolder(@NonNull LayoutEditorShortcutItemBinding binding) {
            super(binding.getRoot());
            name = binding.shortcutLabel;
        }

        public void bind(@NonNull EditorAction item) {
            name.setText(item.getName());
        }
    }
}