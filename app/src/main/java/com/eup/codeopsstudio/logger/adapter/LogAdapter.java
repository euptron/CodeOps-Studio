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

package com.eup.codeopsstudio.logger.adapter;

import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.AsyncListDiffer;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import com.eup.codeopsstudio.logger.model.LogModel;
import com.eup.codeopsstudio.R;
import java.util.List;
import java.util.Objects;

public class LogAdapter extends RecyclerView.Adapter<LogAdapter.ViewHolder> {

  public static final DiffUtil.ItemCallback<LogModel> DIFF_CALLBACK =
      new DiffUtil.ItemCallback<>() {
        @Override
        public boolean areItemsTheSame(@NonNull LogModel oldLog, @NonNull LogModel newLog) {
          return oldLog.getID().equals(newLog.getID());
        }

        @Override
        public boolean areContentsTheSame(@NonNull LogModel oldLog, @NonNull LogModel newLog) {
          return Objects.equals(oldLog.getMessage(), newLog.getMessage());
        }
      };
  private final AsyncListDiffer<LogModel> mDiffer = new AsyncListDiffer<>(this, DIFF_CALLBACK);

  public LogAdapter() {
    // Default
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view =
        LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_log, parent, false);
    return new ViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    LogModel log = mDiffer.getCurrentList().get(position);
    if (log == null) return;

    SpannableStringBuilder sb = new SpannableStringBuilder();
    boolean hasPrevious = false;

    if (!TextUtils.isEmpty(log.getDateFormat())) {
      sb.append(log.getDateFormat());
      hasPrevious = true;
    }

    if (!TextUtils.isEmpty(log.getTag())) {
      if (hasPrevious) sb.append("  ");
      sb.append(log.getTag());
      hasPrevious = true;
    }

    if (!TextUtils.isEmpty(log.getLevel())) {
      if (hasPrevious) sb.append("  ");
      sb.append(log.getLevel());
      hasPrevious = true;
    }

    String message = log.getMessage().toString();
    if (!TextUtils.isEmpty(message)) {
      if (hasPrevious) sb.append("  ");
      sb.append(message);
    } else {
      if (hasPrevious) sb.append("  ");
      sb.append("[No message]");
    }

    holder.logMessageView.setText(sb);
  }

  @Override
  public int getItemCount() {
    return mDiffer.getCurrentList().size();
  }

  public void submitList(List<LogModel> newData) {
    mDiffer.submitList(newData);
  }

  public void submitList(List<LogModel> newData, Runnable action) {
    mDiffer.submitList(newData, action);
  }

  public static class ViewHolder extends RecyclerView.ViewHolder {
    public TextView logMessageView;

    public ViewHolder(@NonNull View itemView) {
      super(itemView);
      logMessageView = itemView.findViewById(R.id.logMessageView);
    }
  }
}
