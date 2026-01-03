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

package com.eup.codeopsstudio.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.eup.codeopsstudio.databinding.LayoutActionItemBinding;
import com.eup.codeopsstudio.models.ActionModel;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ActionAdapter extends RecyclerView.Adapter<ActionAdapter.VH> {

    private final List<ActionModel> items = new ArrayList<>();
    private OnItemClickListener mListener;
    private OnButtonClickListener buttonClickListener;

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(LayoutActionItemBinding.inflate(LayoutInflater.from(parent.getContext()),
            parent, false));
    }

    @Override
    public void onBindViewHolder(VH holder, int position) {
        String title = items.get(position).getTitle();
        String summary = items.get(position).getSummary();
        String buttonText = items.get(position).getButtonText();

        holder.title.setText(title);
        holder.icon.setImageResource(items.get(position).getIcon());

        if (summary != null) {
            holder.summary.setVisibility(View.VISIBLE);
            holder.summary.setText(items.get(position).getSummary());
        } else {
            holder.summary.setVisibility(View.GONE);
        }

        if (buttonText != null) {
            holder.buttonText.setVisibility(View.VISIBLE);
            holder.buttonText.setText(buttonText);
            holder.buttonText.setOnClickListener(v -> {
                if (buttonClickListener != null) {
                    buttonClickListener.onButtonClick(v);
                }
            });
        } else {
            holder.buttonText.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onItemClick(items.get(holder.getAdapterPosition()));
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void refresh(List<ActionModel> actions) {
        items.clear();
        items.addAll(actions);
        notifyDataSetChanged();
    }

    public void setOnButtonClickListener(OnButtonClickListener listener) {
        buttonClickListener = listener;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public void submitList(@NonNull List<ActionModel> payloads) {
        computeDifference(items, payloads, () -> {
            // update items before dispatching updates
            items.clear();
            items.addAll(payloads);
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void computeDifference(@NonNull List<ActionModel> oldItems,
        @NonNull List<ActionModel> newItems, Runnable task) {

        DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return oldItems.size();
            }

            @Override
            public int getNewListSize() {
                return newItems.size();
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                ActionModel oldItem = oldItems.get(oldItemPosition);
                ActionModel newItem = newItems.get(newItemPosition);
                return Objects.equals(oldItem.getID(), newItem.getID());
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                ActionModel oldItem = oldItems.get(oldItemPosition);
                ActionModel newItem = newItems.get(newItemPosition);
                return Objects.equals(oldItem, newItem);
            }
        });

        if (task != null) {
            task.run();
        }

        try {
            result.dispatchUpdatesTo(this);
        } catch (IndexOutOfBoundsException e) {
            notifyDataSetChanged();
        }
    }

    public interface OnItemClickListener {
        void onItemClick(ActionModel model);
    }

    public interface OnButtonClickListener {
        void onButtonClick(View view);
    }

    public static class VH extends RecyclerView.ViewHolder {
        TextView title;
        TextView summary;
        MaterialButton buttonText;
        ImageView icon;

        public VH(@NonNull LayoutActionItemBinding binding) {
            super(binding.getRoot());
            title      = binding.title;
            summary    = binding.summary;
            icon       = binding.icon;
            buttonText = binding.buttonOpen;
        }
    }
}
