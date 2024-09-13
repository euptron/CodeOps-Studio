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
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * A pane designed for displaying non editable text content within a scrollable view.
 *
 * @see com.eup.codeopsstudio.pane
 * @author EUP
 * @version 1.0
 */
public class TextPane extends Pane {

  private ScrollView scrollView;
  private TextView textView;
  private String content;

  public TextPane(Context context, String title) {
    this(context, title, /* generate new uuid= */ true);
  }

  public TextPane(Context context, String title, boolean generateUUID) {
    super(context, title, generateUUID);
    content = "";
  }

  @Override
  public View onCreateView() {
    scrollView = new ScrollView(getContext());
    scrollView.setLayoutParams(
        new ScrollView.LayoutParams(
            ScrollView.LayoutParams.MATCH_PARENT, ScrollView.LayoutParams.MATCH_PARENT));

    textView = new TextView(getContext());
    textView.setLayoutParams(
        new ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

    scrollView.addView(textView);
    return scrollView;
  }

  @Override
  public void onViewCreated(View view) {
    super.onViewCreated(view);
    int padding = pxToDp(getContext(), 8);
    textView.setPadding(padding, padding, padding, padding);
    textView.setTextSize(14);
    setText(content);
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    textView = null;
    scrollView = null;
  }

  @Override
  public void persist() {
    super.persist();
    addArguments("content", getText());
  }

  /**
   * Sets the text content of this TextPane.
   *
   * @param text The text content to be set.
   */
  public void setText(String text) {
    if (isAlive()) if (textView != null) textView.setText(text);
  }

  public String getText() {
    if (isAlive()) {
      if (textView != null) return textView.getText().toString();
    }
    return "";
  }

  public void setContent(String textContent) {
    this.content = content;
  }

  /**
   * Retrieves the text content of this TextPane.
   *
   * @return The text content of this TextPane.
   */
  public String getContent() {
    return content;
  }

  private boolean isAlive() {
    return getState() != PaneState.INVALID_STATE && hasPerformedCreateView;
  }

  public static int pxToDp(Context context, float px) {
    return Math.round(
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, px, context.getResources().getDisplayMetrics()));
  }
}
