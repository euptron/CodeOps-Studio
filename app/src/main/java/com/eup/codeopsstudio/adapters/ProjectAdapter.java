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

package com.eup.codeopsstudio.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.AsyncListDiffer;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.eup.codeopsstudio.R;
import com.eup.codeopsstudio.common.util.FileUtil;
import com.eup.codeopsstudio.databinding.LayoutEmptyProjectsBinding;
import com.eup.codeopsstudio.databinding.RecentProjectItemBinding;
import com.eup.codeopsstudio.models.ExtensionTable;
import com.eup.codeopsstudio.models.recents.Project;

import java.util.List;

/**
 * Adapter class for recent project recyclerview
 *
 * @author Etido Peter
 */
public class ProjectAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final DiffUtil.ItemCallback<Project> DIFF_CALLBACK =
        new DiffUtil.ItemCallback<>() {
        @Override
        public boolean areItemsTheSame(@NonNull Project oldProject, @NonNull Project newProject) {
            return oldProject.getPath().equals(newProject.getPath());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Project oldProject,
            @NonNull Project newProject) {
            return oldProject.equals(newProject);
        }
    };
    static final int EMPTY_VIEW = 66666;
    private final AsyncListDiffer<Project> mDiffer = new AsyncListDiffer<>(this, DIFF_CALLBACK);
    private OnItemClickListener itemClickListener;
    private OnItemLongClickListener itemLongClickListener;

    public ProjectAdapter() {
        // default
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        var inflater = LayoutInflater.from(parent.getContext());

        if (viewType == EMPTY_VIEW) {
            var binding = LayoutEmptyProjectsBinding.inflate(inflater, parent, false);
            return new EmptyViewHolder(binding);
        }

        var binding = RecentProjectItemBinding.inflate(inflater, parent, false);
        final ItemViewHolder holder = new ItemViewHolder(binding);

        holder.itemView.setOnClickListener(v -> {
            if (itemClickListener != null) {
                int position = holder.getAbsoluteAdapterPosition();
                Project project = mDiffer.getCurrentList().get(position);
                if (position != RecyclerView.NO_POSITION) {
                    itemClickListener.onClick(project);
                }
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (itemLongClickListener != null) {
                int position = holder.getAbsoluteAdapterPosition();
                Project project = mDiffer.getCurrentList().get(position);
                if (position != RecyclerView.NO_POSITION) {
                    return itemLongClickListener.onLongClick(v, project);
                }
            }
            return false;
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) != EMPTY_VIEW) {
            ItemViewHolder itemHolder = (ItemViewHolder) holder;
            Project project = mDiffer.getCurrentList().get(position);
            itemHolder.bind(project);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (mDiffer.getCurrentList().isEmpty()) {
            return EMPTY_VIEW;
        }
        return super.getItemViewType(position);
    }

    @Override
    public int getItemCount() {
        var count = mDiffer.getCurrentList().size();
        return count > 0 ? count : 1;
    }

    public void setOnItemClickListener(OnItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener itemLongClickListener) {
        this.itemLongClickListener = itemLongClickListener;
    }

    public void submitList(List<Project> newData) {
        mDiffer.submitList(newData);
    }

    public interface OnItemClickListener {
        void onClick(Project project);
    }

    public interface OnItemLongClickListener {
        boolean onLongClick(View view, Project project);
    }

    public static class EmptyViewHolder extends RecyclerView.ViewHolder {
        public EmptyViewHolder(@NonNull LayoutEmptyProjectsBinding binding) {
            super(binding.getRoot());
        }
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {

        private final ImageView icon;
        private final TextView name;
        private final TextView path;

        public ItemViewHolder(@NonNull RecentProjectItemBinding binding) {
            super(binding.getRoot());
            icon = binding.fileIcon;
            name = binding.fileName;
            path = binding.filePath;
        }

        private void bind(@NonNull Project project) {
            if (project.getFile().isFile()) {
                icon.setImageResource(ExtensionTable.getExtensionIcon(project.getName()));
            } else {
                icon.setImageResource(R.drawable.ic_folder);
            }
            name.setText(FileUtil.getFileNameWithoutExtension(project.getFile()));
            path.setText(project.getPath());
        }
    }
}
