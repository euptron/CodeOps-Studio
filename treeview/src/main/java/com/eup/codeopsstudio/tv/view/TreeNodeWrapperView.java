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
 
   package com.eup.codeopsstudio.tv.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.eup.codeopsstudio.tv.R;

/** Created by Bogdan Melnychuk on 2/10/15. */
@SuppressLint("ViewConstructor")
public class TreeNodeWrapperView extends LinearLayout {
  private final int containerStyle;
  private LinearLayout nodeItemsContainer;
  private ViewGroup nodeContainer;

  public TreeNodeWrapperView(Context context, int containerStyle) {
    super(context);
    this.containerStyle = containerStyle;
    init();
  }

  private void init() {
    setOrientation(LinearLayout.VERTICAL);

    nodeContainer = new RelativeLayout(getContext());
    nodeContainer.setLayoutParams(
        new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
    nodeContainer.setId(R.id.node_header);

    ContextThemeWrapper newContext = new ContextThemeWrapper(getContext(), containerStyle);
    nodeItemsContainer = new LinearLayout(newContext, null, containerStyle);
    nodeItemsContainer.setLayoutParams(
        new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
    nodeItemsContainer.setId(R.id.node_items);
    nodeItemsContainer.setOrientation(LinearLayout.VERTICAL);
    nodeItemsContainer.setVisibility(View.GONE);

    addView(nodeContainer);
    addView(nodeItemsContainer);
  }

  public void insertNodeView(View nodeView) {
    nodeContainer.addView(nodeView);
  }

  public ViewGroup getNodeContainer() {
    return nodeContainer;
  }
}
