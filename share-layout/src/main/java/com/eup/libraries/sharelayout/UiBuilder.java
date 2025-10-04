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

package com.eup.libraries.sharelayout;

import android.content.Context;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

public class UiBuilder {

    private final Context context;

    public UiBuilder(@NonNull Context context) {
        this.context = context;
    }

    public LinearLayout.LayoutParams createLinear(int width, int height, float weight,
        int gravity) {
        var layoutParams = new LinearLayout.LayoutParams(getSize(width), getSize(height), weight);
        layoutParams.gravity = gravity;
        return layoutParams;
    }

    public int getSize(float size) {
        return (int) (size < 0 ? size : dp(size));
    }

    public int dp(float px) {
        if (px == 0) {
            return 0;
        }
        return Math.round(context.getResources().getDisplayMetrics().density * px);
    }

    public LinearLayout.LayoutParams createLinear(int width, int height, int gravity) {
        var layoutParams = new LinearLayout.LayoutParams(getSize(width), getSize(height));
        layoutParams.gravity = gravity;
        return layoutParams;
    }

    public LinearLayout.LayoutParams createLinear(int width, int height) {
        return new LinearLayout.LayoutParams(getSize(width), getSize(height));
    }
}
