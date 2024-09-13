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
 
   package com.eup.codeopsstudio.tv.holder;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.eup.codeopsstudio.tv.model.TreeNode;

/** Created by Bogdan Melnychuk on 2/11/15. */
public class SimpleViewHolder extends TreeNode.BaseNodeViewHolder<Object> {

  public SimpleViewHolder(Context context) {
    super(context);
  }

  @Override
  public View createNodeView(TreeNode node, Object value) {
    final TextView tv = new TextView(context);
    tv.setText(String.valueOf(value));
    return tv;
  }

  @Override
  public void toggle(boolean active) {}
}
