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
 
   package com.eup.codeopsstudio.ui.editor.panes.recent.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.AsyncListDiffer;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import com.eup.codeopsstudio.common.util.FileUtil;
import com.eup.codeopsstudio.models.ExtensionTable;
import com.eup.codeopsstudio.res.R;
import com.eup.codeopsstudio.res.databinding.RecentProjectItemBinding;
import com.eup.codeopsstudio.databinding.LayoutEmptyProjectsBinding;
import com.eup.codeopsstudio.ui.editor.panes.recent.model.Project;
import java.util.List;

public class ProjectAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

  final int EMPTY_VIEW = 66666;

  public ProjectAdapter() {
    // default
  }

  public interface onItemClickListener {
    void onClick(Project project);
  }

  public interface onItemLongClickListener {
    boolean onLongClick(View view, Project project);
  }

  private onItemClickListener itemClickListener;
  private onItemLongClickListener itemLongClickListener;

  private final AsyncListDiffer<Project> mDiffer =
      new AsyncListDiffer<Project>(this, DIFF_CALLBACK);

  public void setOnItemClickListener(onItemClickListener itemClickListener) {
    this.itemClickListener = itemClickListener;
  }

  public void setOnItemLongClickListener(onItemLongClickListener itemLongClickListener) {
    this.itemLongClickListener = itemLongClickListener;
  }

  public void submitList(List<Project> newData) {
    mDiffer.submitList(newData);
  }

  public static final DiffUtil.ItemCallback<Project> DIFF_CALLBACK =
      new DiffUtil.ItemCallback<Project>() {
        @Override
        public boolean areItemsTheSame(@NonNull Project oldProject, @NonNull Project newProject) {
          return oldProject.getPath().equals(newProject.getPath());
        }

        @Override
        public boolean areContentsTheSame(
            @NonNull Project oldProject, @NonNull Project newProject) {
          return oldProject.equals(newProject);
        }
      };

  @Override
  public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    if (viewType == EMPTY_VIEW) {
      LayoutEmptyProjectsBinding binding =
          LayoutEmptyProjectsBinding.inflate(
              LayoutInflater.from(parent.getContext()), parent, false);
      return new EmptyViewHolder(binding);
    } else {
      RecentProjectItemBinding binding =
          RecentProjectItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
      final ItemViewHolder holder = new ItemViewHolder(binding);

      holder.itemView.setOnClickListener(
          v -> {
            if (itemClickListener != null) {
              int position = holder.getAdapterPosition();
              Project project = mDiffer.getCurrentList().get(position);
              if (position != RecyclerView.NO_POSITION) {
                itemClickListener.onClick(project);
              }
            }
          });

      holder.itemView.setOnLongClickListener(
          v -> {
            if (itemLongClickListener != null) {
              int position = holder.getAdapterPosition();
              Project project = mDiffer.getCurrentList().get(position);
              if (position != RecyclerView.NO_POSITION) {
                return itemLongClickListener.onLongClick(v, project);
              }
            }
            return false;
          });

      return holder;
    }
  }

  @Override
  public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
    if (getItemViewType(position) != EMPTY_VIEW) {
      ItemViewHolder itemHolder = (ItemViewHolder) holder;
      Project project = mDiffer.getCurrentList().get(position);
      itemHolder.bind(project);
    }
  }

  @Override
  public int getItemCount() {
    var count = mDiffer.getCurrentList().size();
    return count > 0 ? count : 1;
  }

  public static class EmptyViewHolder extends RecyclerView.ViewHolder {
    public EmptyViewHolder(LayoutEmptyProjectsBinding binding) {
      super(binding.getRoot());
    }
  }

  @Override
  public int getItemViewType(int position) {
    if (mDiffer.getCurrentList().size() == 0) return EMPTY_VIEW;
    return super.getItemViewType(position);
  }

  public class ItemViewHolder extends RecyclerView.ViewHolder {

    private ImageView icon;
    private TextView name, path;

    public ItemViewHolder(RecentProjectItemBinding binding) {
      super(binding.getRoot());
      icon = binding.fileIcon;
      name = binding.fileName;
      path = binding.filePath;
    }

    private void bind(Project project) {
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
