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

package com.eup.codeopsstudio.pane;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * A custom FrameLayout designed to host panes or similar content, suitable for use
 * within a RecyclerView's onCreateViewHolder view holder or when attaching to a parent layout.
 * This layout automatically fills its parent's width and height if no explicit layout parameters
 * are defined.
 *
 * <p>
 * When using this layout within a RecyclerView's onCreateViewHolder method,
 * it ensures that the pane occupies the entire space of its parent view,
 * which is a common requirement for RecyclerView item layouts.
 * </p>
 *
 * <p>
 * Note: If used within a parent layout, ensure that the parent's layout parameters
 * are set accordingly for proper rendering.
 * </p>
 *
 * @author Etido Peter
 */
public class PaneLayout extends FrameLayout {

    public static final ViewGroup.LayoutParams FILL_LAYOUT =
        new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT);

    public PaneLayout(@NonNull Context context) {
        super(context);
        // Set default layout parameters to match parent if not explicitly defined
        setLayoutParams(FILL_LAYOUT);
    }

    public PaneLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PaneLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public PaneLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr,
        int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        // Set default layout parameters to match parent if not explicitly defined
        setLayoutParams(FILL_LAYOUT);
    }
}
