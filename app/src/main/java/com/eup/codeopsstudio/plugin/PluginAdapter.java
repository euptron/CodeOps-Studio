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

package com.eup.codeopsstudio.plugin;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.eup.codeopsstudio.databinding.ItemPluginBinding;
import com.eup.codeopsstudio.databinding.LayoutNoPluginsBinding;
import com.eup.codeopsstudio.util.UninstallDialogFragment;
import com.eup.codeopsstudio.util.Wizard;
import com.eup.codeopsstudio.R;

/**
 * A {@link androidx.recyclerview.widget.ListAdapter} for rendering plugin items.
 *
 * @author Etido Peter
 */
public class PluginAdapter extends ListAdapter<PluginItem, RecyclerView.ViewHolder> {

  private static final int EMPTY_VIEW = 66666;

  private Fragment host;
  private OnItemClickListener itemClickListener;
  private OnItemLongClickListener itemLongClickListener;

  public PluginAdapter(Fragment fragment) {
    super(PluginItem.DIFF_CALLBACK);
    this.host = fragment;
  }

  @NonNull
  @Override
  public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    LayoutInflater inflater = LayoutInflater.from(parent.getContext());

    if (viewType == EMPTY_VIEW) {
      var binding = LayoutNoPluginsBinding.inflate(inflater, parent, false);
      return new EmptyViewHolder(binding);
    }

    var binding = ItemPluginBinding.inflate(inflater, parent, false);
    final PluginViewHolder holder = new PluginViewHolder(host, binding);

    holder.itemView.setOnClickListener(
        v -> {
          if (itemClickListener != null) {
            int position = holder.getAbsoluteAdapterPosition();
            PluginItem item = getItem(position);
            if (position != RecyclerView.NO_POSITION) {
              itemClickListener.onClick(item);
            }
          }
        });

    holder.itemView.setOnLongClickListener(
        v -> {
          if (itemLongClickListener != null) {
            int position = holder.getAbsoluteAdapterPosition();
            PluginItem item = getItem(position);
            if (position != RecyclerView.NO_POSITION) {
              return itemLongClickListener.onLongClick(v, item);
            }
          }
          return false;
        });

    return holder;
  }

  @Override
  public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
    if (getItemViewType(position) != EMPTY_VIEW) {
      PluginViewHolder pluginViewHolder = (PluginViewHolder) holder;
      PluginItem item = getItem(position);
      pluginViewHolder.bind(item);
    }
  }

  @Override
  public int getItemViewType(int position) {
    if (getCurrentList().isEmpty()) return EMPTY_VIEW;
    return super.getItemViewType(position);
  }

  @Override
  public int getItemCount() {
    var count = super.getItemCount();
    return count > 0 ? count : 1;
  }

  public void setOnItemClickListener(OnItemClickListener itemClickListener) {
    this.itemClickListener = itemClickListener;
  }

  public void setOnItemLongClickListener(OnItemLongClickListener itemLongClickListener) {
    this.itemLongClickListener = itemLongClickListener;
  }

  static class EmptyViewHolder extends RecyclerView.ViewHolder {
    public EmptyViewHolder(@NonNull LayoutNoPluginsBinding binding) {
      super(binding.getRoot());
    }
  }

  static class PluginViewHolder extends RecyclerView.ViewHolder {

    private final TextView name;
    private final TextView author;
    private final ImageView thumbnail;
    private final TextView description;
    private final ImageButton buttonMore;
    private Fragment fragment;

    public PluginViewHolder(Fragment fragment, @NonNull ItemPluginBinding binding) {
      super(binding.getRoot());
      name = binding.tvPluginName;
      author = binding.btnPublisher;
      thumbnail = binding.ivPluginThumbnail;
      description = binding.tvPluginDescription;
      buttonMore = binding.ivBtnMore;
      this.fragment = fragment;
    }

    private void bind(@NonNull PluginItem item) {
      name.setText(item.name);
      author.setText(item.author);
      thumbnail.setImageDrawable(item.icon);
      description.setText(item.description);
      buttonMore.setOnClickListener(
          v -> {
            showPluginPopupMenu(v, item);
          });
    }

    private void showPluginPopupMenu(View anchorView, PluginItem pluginItem) {
      Context ctx = anchorView.getContext();
      var popupMenu = new PopupMenu(ctx, anchorView);
      boolean pluginInstalled = Wizard.isPackageInstalled(ctx, pluginItem.id);
      
      popupMenu.getMenu().add(0, 1, 0, ctx.getString(R.string.install_plugin));
      popupMenu.getMenu().add(0, 2, 0, ctx.getString(R.string.uninstall_plugin));
      popupMenu.getMenu().findItem(1).setVisible(!pluginInstalled);
      popupMenu.getMenu().findItem(2).setVisible(pluginInstalled);

      popupMenu.setOnMenuItemClickListener(
          new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
              switch (item.getItemId()) {
                case 1:
                  installPlugin(pluginItem);
                  return true;
                case 2:
                  uninstallPlugin(pluginItem);
                  return true;
                default:
                  return false;
              }
            }
          });

      popupMenu.show();
    }

    private void installPlugin(@NonNull PluginItem item) {
      String downloadUrl = item.downloadUrl;
      if (Wizard.isEmpty(downloadUrl)) return;
      // TODO: download & install plugin
    }

    private void uninstallPlugin(@NonNull PluginItem item) {
      var dialog = UninstallDialogFragment.newInstance(item.id, item.name);
      dialog.show(fragment.getParentFragmentManager(), UninstallDialogFragment.TAG);
    }
  }

  public interface OnItemClickListener {
    void onClick(PluginItem item);
  }

  public interface OnItemLongClickListener {
    boolean onLongClick(View view, PluginItem item);
  }
}
