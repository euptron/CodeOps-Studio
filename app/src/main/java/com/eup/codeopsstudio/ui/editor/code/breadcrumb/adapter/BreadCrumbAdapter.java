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
 
   package com.eup.codeopsstudio.ui.editor.code.breadcrumb.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.AsyncListDiffer;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import com.eup.codeopsstudio.res.databinding.LayoutBreadCrumbItemBinding;
import com.eup.codeopsstudio.ui.editor.code.breadcrumb.model.BreadCrumb;
import com.google.android.material.R;
import com.google.android.material.color.MaterialColors;
import io.github.rosemoe.sora.widget.schemes.EditorColorScheme;
import java.util.List;

public class BreadCrumbAdapter extends RecyclerView.Adapter<BreadCrumbAdapter.ViewHolder> {

  private EditorColorScheme colorScheme;

  protected OnItemClickListener<BreadCrumb> itemClickListener;

  protected OnItemLongClickListener<BreadCrumb> itemLongClickListener;

  public final DiffUtil.ItemCallback<BreadCrumb> DIFF_CALLBACK =
      new DiffUtil.ItemCallback<BreadCrumb>() {
        @Override
        public boolean areItemsTheSame(@NonNull BreadCrumb oldItem, @NonNull BreadCrumb newItem) {
          return oldItem.hashCode() == newItem.hashCode();
        }

        @Override
        public boolean areContentsTheSame(
            @NonNull BreadCrumb oldItem, @NonNull BreadCrumb newItem) {
          return oldItem.equals(newItem);
        }
      };

  private final AsyncListDiffer<BreadCrumb> mDiffer =
      new AsyncListDiffer<BreadCrumb>(this, DIFF_CALLBACK);

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    var binding =
        LayoutBreadCrumbItemBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false);
    return new ViewHolder(binding);
  }

  @Override
  public void onBindViewHolder(ViewHolder holder, int position) {
    var crumb = mDiffer.getCurrentList().get(position);
    holder.bind(crumb);

    if (position == getItemCount() - 1) {
      holder.crumb_icon.setVisibility(View.GONE);
    } else {
      holder.itemView.setOnClickListener(
          v -> {
            if (itemClickListener != null) {
              if (position != RecyclerView.NO_POSITION) {
                itemClickListener.onItemClick(v, crumb, position);
              }
            }
          });
      holder.itemView.setOnLongClickListener(
          v -> {
            if (itemLongClickListener != null) {
              if (position != RecyclerView.NO_POSITION) {
                return itemLongClickListener.onItemLongClick(v, crumb, position);
              }
            }
            return false;
          });
    }
  }

  @Override
  public int getItemCount() {
    return mDiffer.getCurrentList().size();
  }

  /**
   * Sets the click listener for the adapter's items.
   *
   * @param listener The item click listener.
   */
  public void setOnItemClickListener(OnItemClickListener<BreadCrumb> listener) {
    this.itemClickListener = listener;
  }

  /**
   * Sets the long click listener for the adapter's items.
   *
   * @param listener The item long click listener.
   */
  public void setOnItemLongClickListener(OnItemLongClickListener<BreadCrumb> listener) {
    this.itemLongClickListener = listener;
  }

  public void submitList(List<BreadCrumb> newItems) {
    mDiffer.submitList(newItems);
    notifyDataSetChanged();
  }

  public class ViewHolder extends RecyclerView.ViewHolder {

    private final ImageView crumb_icon;
    private final TextView crumb_text;

    public ViewHolder(LayoutBreadCrumbItemBinding binding) {
      super(binding.getRoot());
      crumb_icon = binding.crumbIcon;
      crumb_text = binding.crumbText;
    }

    public void bind(BreadCrumb crumb) {
      if (crumb.getFile().isFile()) {
        crumb_icon.setVisibility(View.INVISIBLE);
      } else {
        crumb_icon.setVisibility(View.VISIBLE);
      }
      crumb_text.setText(crumb.getName());
    }
  }

  /**
   * Interface definition for a callback to be invoked when an item in the adapter has been clicked.
   *
   * @param <BreadCrumb> The type of data to be bound to the view holder.
   */
  public interface OnItemClickListener<BreadCrumb> {
    /**
     * Called when an item in the adapter has been clicked.
     *
     * @param v The clicked item view
     * @param item The clicked item.
     * @param position The position of the clicked item.
     */
    void onItemClick(View view, BreadCrumb item, int position);
  }

  /**
   * Interface definition for a callback to be invoked when an item in the adapter has been long
   * clicked.
   *
   * @param <BreadCrumb> The type of data to be bound to the view holder.
   */
  public interface OnItemLongClickListener<BreadCrumb> {
    /**
     * Called when an item in the adapter has been long clicked.
     *
     * @param v The clicked item view
     * @param item The long clicked item.
     * @param position The position of the long clicked item.
     */
    boolean onItemLongClick(View view, BreadCrumb item, int position);
  }

  public void setColorScheme(EditorColorScheme colorScheme) {
    this.colorScheme = colorScheme;
  }
}
