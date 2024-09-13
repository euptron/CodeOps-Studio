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

package com.eup.codeopsstudio.ui.settings.api;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.AsyncListDiffer;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import com.eup.codeopsstudio.common.Constants;
import com.eup.codeopsstudio.databinding.LayoutChangeLogItemBinding;
import com.eup.codeopsstudio.res.R;
import com.eup.codeopsstudio.util.BaseUtil;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import com.eup.codeopsstudio.common.ContextManager;
import com.eup.codeopsstudio.util.Wizard;
import java.util.Locale;
import java.util.TimeZone;

public class ChangelogAdapter extends RecyclerView.Adapter<ChangelogAdapter.ViewHolder> {

  private final AsyncListDiffer<ChangelogItem> mDiffer =
      new AsyncListDiffer<ChangelogItem>(this, DIFF_CALLBACK);

  public ChangelogAdapter(List<ChangelogItem> newData) {
    mDiffer.submitList(newData);
  }

  public static final DiffUtil.ItemCallback<ChangelogItem> DIFF_CALLBACK =
      new DiffUtil.ItemCallback<ChangelogItem>() {
        @Override
        public boolean areItemsTheSame(
            @NonNull ChangelogItem oldLog, @NonNull ChangelogItem newLog) {
          return oldLog.getReleaseType().equals(newLog.getReleaseType());
        }

        @Override
        public boolean areContentsTheSame(
            @NonNull ChangelogItem oldLog, @NonNull ChangelogItem newLog) {
          return oldLog.getReleaseType().equals(newLog.getReleaseType());
        }
      };

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    return new ViewHolder(
        LayoutChangeLogItemBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false));
  }

  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    ChangelogItem item = mDiffer.getCurrentList().get(position);
    holder.bind(item, position);
  }

  @Override
  public int getItemCount() {
    return mDiffer.getCurrentList().size();
  }

  public class ViewHolder extends RecyclerView.ViewHolder {

    private LayoutChangeLogItemBinding binding;

    public ViewHolder(LayoutChangeLogItemBinding binding) {
      super(binding.getRoot());
      this.binding = binding;
    }

    public void bind(ChangelogItem item, int position) {
      if (item == null) {
        return;
      }
      String release = item.getReleaseType().releaseName;
      String title = ContextManager.getStringRes(R.string.release);
      title += Constants.SPACE + item.getVersionName() + ((release != null) ? "-" + release : "");
      binding.title.setText(title);
      binding.log.setText(item.getDescription());

      long releaseDate = item.getReleaseDate();

      if (releaseDate > 0) {
        binding.summary.setVisibility(View.VISIBLE);
        long timestamp = releaseDate;
        SimpleDateFormat formatter =
            new SimpleDateFormat(
                "EEE, dd MMM yyyy HH:mm",
                Locale.US); // "EEE, d MMM yyyy HH:mm a" yyyy-MM-dd -> (2023-11-04)
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        binding.summary.setText(
            itemView.getContext().getString(R.string.released_on)
                + ": "
                + formatter.format(new Date(timestamp))
                + " UTC");
      } else if (releaseDate <= 0) {
        binding.summary.setVisibility(View.GONE);
      }

      binding.expandablePane.setVisibility(item.getIsExpanded() ? View.VISIBLE : View.GONE);
      binding.chevron.setRotation(item.getIsExpanded() ? 0 : -180); // 180:0,0:180

      if (item.getVersionName()
          .equalsIgnoreCase(Wizard.getAppVersionName(ContextManager.getApplicationContext()))) {
        advancedCorners(
            binding.versionIndicator,
            "#FFB0F0C0"); // light blue:FFAAC7FF , light green (aelo-green): FFA6DABD (normal) |
                          // FFB0F0C0 (prime)
      } else {
        binding.versionIndicator.setBackground(null);
      }

      itemView.setOnClickListener(
          v -> {
            if (BaseUtil.isExpanded(binding.expandablePane)) {
              BaseUtil.startObjectAnimation(binding.chevron, "rotation", 180, 220);
              binding.expandablePane.setVisibility(View.GONE);
              item.setIsExpanded(false);
            } else {
              BaseUtil.startObjectAnimation(binding.chevron, "rotation", 0, 220);
              binding.expandablePane.setVisibility(View.VISIBLE);
              item.setIsExpanded(true);
            }
            animateLayoutChanges(binding.logBase);
            notifyItemChanged(position);
          });
    }
  }

  private static void animateLayoutChanges(LinearLayout view) {
    // i used this instead of the xml attribute because this one looks better and smoother.
    AutoTransition autoTransition = new AutoTransition();
    autoTransition.setDuration((short) 300);
    TransitionManager.beginDelayedTransition(view, autoTransition);
  }

  private static void advancedCorners(View view, String color) {
    GradientDrawable gd = new GradientDrawable();
    gd.setColor(Color.parseColor(color));
    gd.setCornerRadii(new float[] {0, 0, 30, 30, 30, 30, 0, 0});
    view.setBackground(gd);
  }
}
