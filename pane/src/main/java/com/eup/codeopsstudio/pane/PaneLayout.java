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
 
   package com.eup.codeopsstudio.pane;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;

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
 * @author EUP
 */
public class PaneLayout extends FrameLayout {

  /**
   * Constructs a new PaneLayout with the given context.
   *
   * @param context The Context the view is running in, through which it can access 
   *                the current theme, resources, etc.
   */
  public PaneLayout(@NonNull Context context) {
    super(context);
    
    // Set default layout parameters to match parent if not explicitly defined
    FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
    );
    setLayoutParams(layoutParams);
  }
}
