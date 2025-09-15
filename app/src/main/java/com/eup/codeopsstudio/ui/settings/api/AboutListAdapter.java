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

package com.eup.codeopsstudio.ui.settings.api;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.eup.codeopsstudio.databinding.LayoutThreeLineListItemBinding;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.shape.CornerFamily;

import java.util.List;

public class AboutListAdapter extends RecyclerView.Adapter<AboutListAdapter.ViewHolder> {

    private final List<AboutItems> list;
    private LayoutThreeLineListItemBinding binding;
    private OnItemClickListener mListener;

    public AboutListAdapter(List<AboutItems> list) {
        this.list = list;
    }

    public void setData(List<AboutItems> newData) {
        list.clear();
        list.addAll(newData);
        notifyDataSetChanged();
    }

    /**
     * A method that defines the {@link OnItemClickListener} interface
     *
     * @param listener The listener for an item clicked
     */
    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new ViewHolder(LayoutThreeLineListItemBinding.inflate(inflater, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        int iconRes = list
            .get(position)
            .getIcon();
        AboutItems item = list.get(position);
        holder.bind(item, position);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    /**
     * An interface that defines a click listener for an item
     *
     * @param item     The item to be clicked
     * @param position The relative position of item
     */
    public interface OnItemClickListener {
        void onItemClick(AboutItems model, int position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private final ShapeableImageView icon;
        private final TextView title, summary;

        public ViewHolder(LayoutThreeLineListItemBinding binding) {
            super(binding.getRoot());
            this.icon    = binding.icon;
            this.title   = binding.title;
            this.summary = binding.summary;
        }

        private void bind(AboutItems item, int position) {
            title.setText(item != null ? item.getTitle() : null);
            summary.setText(item != null ? item.getSummary() : null);
            if (Integer.valueOf(item.getIcon()) != null) {
                icon.setImageResource(item.getIcon());
                icon.setShapeAppearanceModel(icon
                    .getShapeAppearanceModel()
                    .toBuilder()
                    .setAllCorners(CornerFamily.ROUNDED, 0)
                    .build()); // override orginal shape appearance
            }
            if (!title
                .getText()
                .toString()
                .isEmpty()) {
                title.setVisibility(View.VISIBLE);
            } else {
                title.setVisibility(View.GONE);
            }
            if (!summary
                .getText()
                .toString()
                .isEmpty()) {
                summary.setVisibility(View.VISIBLE);
            } else {
                summary.setVisibility(View.GONE);
            }
            if (position == 0) {
                title.setEnabled(false); // version
                summary.setEnabled(false); // version
            }
            itemView.setOnClickListener(v -> {
                if (mListener != null) {
                    mListener.onItemClick(item, position);
                }
            });
        }
    }
}
