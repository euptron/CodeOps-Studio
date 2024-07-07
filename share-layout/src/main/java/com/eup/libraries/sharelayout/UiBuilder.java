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
 
   package com.eup.libraries.sharelayout;

import android.content.Context;
import android.util.TypedValue;
import android.widget.LinearLayout;

public class UiBuilder {

  private Context context;

  public UiBuilder(Context context) {
    this.context = context;
  }

  public int dp(float px) {
    if (px == 0) {
      return 0;
    }
    return Math.round(context.getResources().getDisplayMetrics().density * px);
  }

  /**
   * Converts a dp value into px that can be applied on margins, paddings etc
   *
   * @param dp The dp value that will be converted into px
   * @return The converted px value from the dp argument given
   */
  public int dpToPx(float dp) {
    return Math.round(
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics()));
  }

  public int getSize(float size) {
    return (int) (size < 0 ? size : dp(size));
  }

  public LinearLayout.LayoutParams createLinear(
      int width,
      int height,
      int gravity,
      float weight,
      int leftMargin,
      int topMargin,
      int rightMargin,
      int bottomMargin) {
    var layoutParams =
        new LinearLayout.LayoutParams(getSize(width), getSize(height), weight);
    layoutParams.setMargins(dp(leftMargin), dp(topMargin), dp(rightMargin), dp(bottomMargin));
    layoutParams.gravity = gravity;
    return layoutParams;
  }

  public LinearLayout.LayoutParams createLinear(
      int width,
      int height,
      float weight,
      int leftMargin,
      int topMargin,
      int rightMargin,
      int bottomMargin) {
    var layoutParams =
        new LinearLayout.LayoutParams(getSize(width), getSize(height), weight);
    layoutParams.setMargins(dp(leftMargin), dp(topMargin), dp(rightMargin), dp(bottomMargin));
    return layoutParams;
  }

  public LinearLayout.LayoutParams createLinear(
      int width,
      int height,
      int gravity,
      int leftMargin,
      int topMargin,
      int rightMargin,
      int bottomMargin) {
    var layoutParams =
        new LinearLayout.LayoutParams(getSize(width), getSize(height));
    layoutParams.setMargins(dp(leftMargin), dp(topMargin), dp(rightMargin), dp(bottomMargin));
    layoutParams.gravity = gravity;
    return layoutParams;
  }

  public LinearLayout.LayoutParams createLinear(
      int width,
      int height,
      float leftMargin,
      float topMargin,
      float rightMargin,
      float bottomMargin) {
    var layoutParams =
        new LinearLayout.LayoutParams(getSize(width), getSize(height));
    layoutParams.setMargins(dp(leftMargin), dp(topMargin), dp(rightMargin), dp(bottomMargin));
    return layoutParams;
  }

  public LinearLayout.LayoutParams createLinear(int width, int height, float weight, int gravity) {
    var layoutParams =
        new LinearLayout.LayoutParams(getSize(width), getSize(height), weight);
    layoutParams.gravity = gravity;
    return layoutParams;
  }

  public LinearLayout.LayoutParams createLinear(int width, int height, int gravity) {
    var layoutParams =
        new LinearLayout.LayoutParams(getSize(width), getSize(height));
    layoutParams.gravity = gravity;
    return layoutParams;
  }

  public LinearLayout.LayoutParams createLinear(int width, int height, float weight) {
    return new LinearLayout.LayoutParams(getSize(width), getSize(height), weight);
  }

  public LinearLayout.LayoutParams createLinear(int width, int height) {
    return new LinearLayout.LayoutParams(getSize(width), getSize(height));
  }
}
