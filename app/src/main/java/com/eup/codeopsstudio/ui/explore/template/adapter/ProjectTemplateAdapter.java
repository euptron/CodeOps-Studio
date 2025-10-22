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

package com.eup.codeopsstudio.ui.explore.template.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.eup.codeopsstudio.databinding.LayoutProjectTemplateItemBinding;
import com.eup.codeopsstudio.ui.explore.template.model.ProjectTemplateModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A shared recyclerview for projects and templates
 */
public class ProjectTemplateAdapter extends RecyclerView.Adapter<ProjectTemplateAdapter.TemplateViewHolder> {

    private final List<ProjectTemplateModel> mItems = new ArrayList<>();
    private OnTemplateClickListener templateClickListener;
    private OnTemplateLongClickListener templateLongClickListener;

    public ProjectTemplateAdapter() {
        // Default
    }

    @NonNull
    @Override
    public TemplateViewHolder onCreateViewHolder(@NonNull ViewGroup viewgroup, int viewType) {
        LayoutProjectTemplateItemBinding binding;
        binding =
            LayoutProjectTemplateItemBinding.inflate(LayoutInflater.from(viewgroup.getContext()),
                viewgroup, false);
        TemplateViewHolder holder = new TemplateViewHolder(binding);

        holder.itemView.setOnClickListener(v -> {
            if (templateClickListener != null) {
                int pos = holder.getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    templateClickListener.onItemClick(mItems.get(pos), pos);
                }
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (templateLongClickListener != null) {
                int position = holder.getAdapterPosition();
                var model = mItems.get(position);
                if (position != RecyclerView.NO_POSITION) {
                    return templateLongClickListener.onItemLongClick(v, model);
                }
            }
            return false;
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull TemplateViewHolder holder, int position) {
        holder.bind(mItems.get(position));
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public void setOnTemplateClickListener(OnTemplateClickListener listener) {
        templateClickListener = listener;
    }

    public void setOnTemplateLongClickListener(OnTemplateLongClickListener listener) {
        templateLongClickListener = listener;
    }

    public void submitTemplateList(@NonNull List<ProjectTemplateModel> newItems) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return mItems.size();
            }

            @Override
            public int getNewListSize() {
                return newItems.size();
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                return Objects.equals(mItems.get(oldItemPosition), newItems.get(newItemPosition));
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                return Objects.equals(mItems.get(oldItemPosition), newItems.get(newItemPosition));
            }
        });
        mItems.clear();
        mItems.addAll(newItems);
        diffResult.dispatchUpdatesTo(this);
    }

    public interface OnTemplateClickListener {
        void onItemClick(ProjectTemplateModel model, int position);
    }

    public interface OnTemplateLongClickListener {
        boolean onItemLongClick(View view, ProjectTemplateModel model);
    }

    public static class TemplateViewHolder extends RecyclerView.ViewHolder {

        private final TextView name;

        public TemplateViewHolder(@NonNull LayoutProjectTemplateItemBinding binding) {
            super(binding.getRoot());
            name = binding.templateName;
        }

        private void bind(@NonNull ProjectTemplateModel template) {
            name.setText(template.getName());
        }
    }
}
