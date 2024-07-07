/*************************************************************************
 * This file is part of CodeOps Studio.
 * CodeOps Studio - code anywhere anytime
 * https://github.com/etidoUP/CodeOps-Studio
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

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.blankj.utilcode.util.ToastUtils;
import com.eup.codeopsstudio.common.Constants;
import com.eup.codeopsstudio.editor.ContextualCodeEditor;
import com.eup.codeopsstudio.res.R;
import com.eup.codeopsstudio.res.databinding.LayoutEditorShortcutItemBinding;
import java.util.ArrayList;
import java.util.List;

public class EditorShortcutAdapter
    extends EditorShortcutView.Adapter<EditorShortcutAdapter.ViewHolder> {

  private ContextualCodeEditor editor;
  private List<EditorAction> actionList = new ArrayList<>();

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    LayoutEditorShortcutItemBinding binding =
        LayoutEditorShortcutItemBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false);
    return new ViewHolder(binding);
  }

  @Override
  public void onBindViewHolder(ViewHolder holder, int position) {
    EditorAction editorAction = actionList.get(position);
    holder.itemView.setAnimation(
        AnimationUtils.loadAnimation(holder.itemView.getContext(), android.R.anim.fade_in));
    holder.bind(actionList.get(position));

    holder.itemView.setOnClickListener(
        v -> {
          if (editor != null && editorAction != null) {
            String value = editorAction.getValue();
            String name = editorAction.getName();
            if (value != null) {
              if (editor.isEditable()) {
                if (Constants.TAB.equals(value) && editor.getSnippetController().isInSnippet()) {
                  editor.getSnippetController().shiftToNextTabStop();
                } else {
                  editor.commitText(value);
                }
              } else {
                ToastUtils.showShort(R.string.alrt_rom_cannot_edit_editor); // prompt only inserters
              }
            }
            switch (name) {
              case "←":
                editor.moveSelectionLeft();
                break;
              case "→":
                editor.moveSelectionRight();
                break;
              case "↑":
                editor.moveSelectionUp();
                break;
              case "↓":
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

  /**
   * Sets the data to be displayed by the adapter.
   *
   * @param data The list of data.
   */
  public void submitList(List<EditorAction> action) {
    actionList.addAll(action);
    notifyDataSetChanged();
  }

  public void bindEditor(@NonNull ContextualCodeEditor editor) {
    this.editor = editor;
  }

  public class ViewHolder extends EditorShortcutView.ViewHolder {
    private final TextView name;

    public ViewHolder(LayoutEditorShortcutItemBinding binding) {
      super(binding.getRoot());
      name = binding.shortcutLabel;
    }

    public void bind(EditorAction item) {
      name.setText(item.getName());
    }
  }
}
