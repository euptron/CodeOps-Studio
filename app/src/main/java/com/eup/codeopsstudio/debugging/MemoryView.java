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
 
   package com.eup.codeopsstudio.debugging;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.text.format.Formatter;
import android.util.AttributeSet;
import android.util.TypedValue;
import androidx.appcompat.widget.AppCompatTextView;
import com.google.common.collect.EvictingQueue;
import com.eup.codeopsstudio.res.R;
import java.util.Queue;

/** A widget that shows the memory usage of the app. */
public final class MemoryView extends AppCompatTextView {

  private static final int PLOT_MARGIN = 10;
  private static final int STROKE_WIDTH = 5;
  private static final int MIN_PIXELS_FOR_GRAPH = 60;

  private final Queue<Long> memSnapshots = EvictingQueue.create(5);
  private final Paint paint = new Paint();
  private final Path path = new Path();

  private long maxMemoryInBytes;

  public MemoryView(Context context) {
    this(context, null);
  }

  public MemoryView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public MemoryView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    if (VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN_MR1) {
      setGravity(TEXT_ALIGNMENT_CENTER);
    }

    paint.setStyle(Style.FILL_AND_STROKE);
    paint.setStrokeWidth(STROKE_WIDTH);
    paint.setAntiAlias(true);
  }

  /** Draw a chart next to the text based on the last memory snapshots data points. */
  @Override
  protected void onDraw(Canvas canvas) {
    System.out.println("marian draw");
    super.onDraw(canvas);
    plotMemoryUsage();

    canvas.drawPath(path, paint);
  }

  private void plotMemoryUsage() {
    path.reset();
    int textWidth = (int) getPaint().measureText(getText().toString());
    int availableWidth = getMeasuredWidth() - textWidth - getPaddingLeft() - getPaddingRight();
    if (availableWidth < MIN_PIXELS_FOR_GRAPH || memSnapshots.isEmpty()) {
      // don't show chart
      return;
    }

    int startX = textWidth + getPaddingLeft() + PLOT_MARGIN;
    int availableHeight = getMeasuredHeight() / 2;
    int startY = availableHeight;
    path.moveTo(startX, startY);

    float prevPercentage = 0;
    byte index = 0;
    for (long snapshot : memSnapshots) {
      index++;
      float percentage = snapshot / (float) maxMemoryInBytes;
      float x = startX + (availableWidth / (float) memSnapshots.size()) * index;
      float amplificationFactor = 1;
      // exaggerate differences to see the change in the chart
      if (prevPercentage > percentage) {
        amplificationFactor = 5f;
      } else if (prevPercentage < percentage) {
        amplificationFactor = .2f;
      }

      float y = startY - (percentage * availableHeight) * amplificationFactor;

      prevPercentage = percentage;
      path.lineTo(x, y);
      path.addCircle(x, y, 4, Direction.CCW);
    }

    System.out.println("marian path");
  }

  /** Load color for chart now that we are going to start drawing. */
  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    TypedValue typedValue = new TypedValue();
    getContext().getTheme().resolveAttribute(android.R.attr.colorPrimary, typedValue, true);

    int colorPrimary = typedValue.data;
    paint.setColor(colorPrimary);
  }

  public void refreshMemStats(Runtime runtime) {
    maxMemoryInBytes = runtime.maxMemory();
    long availableMemInBytes = maxMemoryInBytes - (runtime.totalMemory() - runtime.freeMemory());
    long usedMemInBytes = maxMemoryInBytes - availableMemInBytes;
    long usedMemInPercentage = usedMemInBytes * 100 / maxMemoryInBytes;

    memSnapshots.add(usedMemInBytes);

    Context context = getContext();
    setText(
        context.getString(
            R.string.app_memory_usage,
            Formatter.formatShortFileSize(context, usedMemInBytes),
            Formatter.formatShortFileSize(context, maxMemoryInBytes),
            usedMemInPercentage));
  }
}
