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

package com.eup.codeopsstudio.util;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.format.Formatter;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.eup.codeopsstudio.R;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.LinearProgressIndicator;

/**
 * A class to display the runtime memory usage.
 *
 * @author Etido Peter
 */
public class MemoryOverlayView extends MaterialCardView {
  private TextView memoryText;
  private LinearProgressIndicator progressBar;

  public MemoryOverlayView(@NonNull Context context) {
    super(context);
    init();
  }

  public MemoryOverlayView(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public MemoryOverlayView(
      @NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init();
  }

  private void init() {
    setCardBackgroundColor(0xE6000000); // Dark semi-transparent
    setRadius(0f);
    setCardElevation(1f);

    LinearLayout layout = new LinearLayout(getContext());
    layout.setOrientation(LinearLayout.VERTICAL);
    layout.setPadding(32, 24, 32, 24);

    memoryText = new TextView(getContext());
    memoryText.setTextColor(Color.WHITE);
    memoryText.setTextSize(14);
    memoryText.setTypeface(Typeface.MONOSPACE);

    progressBar = new LinearProgressIndicator(getContext());
    progressBar.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, 8));
    progressBar.setProgress(0);

    layout.addView(memoryText);
    layout.addView(progressBar);
    addView(layout);
  }

  public void updateMemoryInfo(Runtime runtime) {
    long maxMemoryInBytes = runtime.maxMemory();
    long availableMemInBytes = maxMemoryInBytes - (runtime.totalMemory() - runtime.freeMemory());
    long usedMemInBytes = maxMemoryInBytes - availableMemInBytes;
    int usedMemInPercentage = (int) ((usedMemInBytes * 100f) / maxMemoryInBytes);

    Context context = getContext();
    String text =
        context.getString(
            R.string.app_memory_usage,
            Formatter.formatShortFileSize(context, usedMemInBytes),
            Formatter.formatShortFileSize(context, maxMemoryInBytes),
            usedMemInPercentage);

    memoryText.setText(text);
    progressBar.setProgress(usedMemInPercentage);

    if (usedMemInPercentage > 80) {
      progressBar.setIndicatorColor(0xFFF44336); // Red
    } else if (usedMemInPercentage > 60) {
      progressBar.setIndicatorColor(0xFFFFC107); // Amber
    } else {
      progressBar.setIndicatorColor(0xFF4CAF50); // Green
    }
  }
}
