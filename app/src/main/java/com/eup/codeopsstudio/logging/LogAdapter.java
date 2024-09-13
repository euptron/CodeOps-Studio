/*************************************************************************
 * This file is part of CodeOps Studio.
 * CodeOps Studio - code anywhere anytime
 * https://github.com/euptron/CodeOps-Studio
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

package com.eup.codeopsstudio.logging;

import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.AsyncListDiffer;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import com.eup.codeopsstudio.IdeApplication;
import com.eup.codeopsstudio.util.Wizard;
import java.util.List;

public class LogAdapter extends RecyclerView.Adapter<LogAdapter.ViewHolder> {

  public LogAdapter() {}

  private final AsyncListDiffer<Log> mDiffer = new AsyncListDiffer<Log>(this, DIFF_CALLBACK);

  public void submitList(List<Log> newData) {
    mDiffer.submitList(newData);
  }

  public static final DiffUtil.ItemCallback<Log> DIFF_CALLBACK =
      new DiffUtil.ItemCallback<Log>() {
        @Override
        public boolean areItemsTheSame(@NonNull Log oldLog, @NonNull Log newLog) {
          return oldLog.equals(newLog);
        }

        @Override
        public boolean areContentsTheSame(@NonNull Log oldLog, @NonNull Log newLog) {
          return oldLog.getMessage().equals(newLog.getMessage());
        }
      };

  @Override
  public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    return new ViewHolder(new FrameLayout(parent.getContext()));
  }

  @Override
  public void onBindViewHolder(ViewHolder holder, int position) {
    Log log = mDiffer.getCurrentList().get(position);
    if (log == null) return;

    SpannableStringBuilder sb = new SpannableStringBuilder();
    if (log.getDateFormat() != null && !TextUtils.isEmpty(log.getDateFormat())) {
      sb.append(" ");
      sb.append(log.getDateFormat());
    }
    if (log.getLevel() != null && !TextUtils.isEmpty(log.getLevel())) {
      sb.append("  ");
      sb.append(log.getLevel());
    }
    if (log.getTag() != null && !TextUtils.isEmpty(log.getTag())) {
      sb.append("  ");
      sb.append(log.getTag());
    }
    sb.append("  ");
    sb.append(log.getMessage());
    sb.append(" ");
    holder.mText.setText(sb);
  }

  @Override
  public int getItemCount() {
    return mDiffer.getCurrentList().size();
  }

  public static class ViewHolder extends RecyclerView.ViewHolder {

    public TextView mText;

    public ViewHolder(View view) {
      super(view);
      mText = new TextView(view.getContext());
      ((ViewGroup) view).addView(mText);
    }
  }
}
