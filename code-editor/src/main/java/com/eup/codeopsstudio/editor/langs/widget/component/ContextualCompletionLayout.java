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
 
   package com.eup.codeopsstudio.editor.langs.widget.component;

import android.animation.LayoutTransition;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.os.SystemClock;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.elevation.SurfaceColors;
import com.eup.codeopsstudio.editor.ContextualCodeEditor;
import io.github.rosemoe.sora.widget.component.DefaultCompletionLayout;
import io.github.rosemoe.sora.widget.component.EditorAutoCompletion;
import io.github.rosemoe.sora.widget.schemes.EditorColorScheme;

public class ContextualCompletionLayout extends DefaultCompletionLayout {

  private int LAYOUT_CORNER_RADIUS = 4; // ½ * orginal
  private ListView listView;
  private LinearLayout rootView;
  private boolean isLoading = false;
  private ContextualCodeEditor editor;
  private boolean enabledAnimation = false;
  private EditorAutoCompletion editorAutoCompletion;

  public ContextualCompletionLayout(ContextualCodeEditor codeEditor) {
    editor = codeEditor;
  }

  public ContextualCompletionLayout() {
    // Clean
  }

  @Override
  public void setEditorCompletion(@NonNull EditorAutoCompletion completion) {
    editorAutoCompletion = completion;
  }

  @Override
  public void setEnabledAnimation(boolean enabledAnimation) {
    this.enabledAnimation = enabledAnimation;

    if (enabledAnimation) {
      LayoutTransition transition = new LayoutTransition();
      transition.enableTransitionType(LayoutTransition.CHANGING);
      transition.enableTransitionType(LayoutTransition.APPEARING);
      transition.enableTransitionType(LayoutTransition.DISAPPEARING);
      transition.enableTransitionType(LayoutTransition.CHANGE_APPEARING);
      transition.enableTransitionType(LayoutTransition.CHANGE_DISAPPEARING);
      transition.addTransitionListener(
          new LayoutTransition.TransitionListener() {
            @Override
            public void startTransition(
                LayoutTransition transition, ViewGroup container, View view, int transitionType) {}

            @Override
            public void endTransition(
                LayoutTransition transition, ViewGroup container, View view, int transitionType) {
              if (view != listView) {
                return;
              }
              view.requestLayout();
            }
          });
      rootView.setLayoutTransition(transition);
      listView.setLayoutTransition(transition);
    } else {
      rootView.setLayoutTransition(null);
      listView.setLayoutTransition(null);
    }
  }

  @NonNull
  @Override
  public View inflate(@NonNull Context context) {
    LinearLayout rootLayout = new LinearLayout(context);
    rootView = rootLayout;
    listView = new ListView(context);

    rootLayout.setOrientation(LinearLayout.VERTICAL);

    setEnabledAnimation(false);

    rootLayout.addView(listView, new LinearLayout.LayoutParams(-1, -1));

    GradientDrawable gd = new GradientDrawable();
    gd.setCornerRadius(
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            LAYOUT_CORNER_RADIUS,
            context.getResources().getDisplayMetrics()));

    rootLayout.setBackground(gd);

    listView.setDividerHeight(0);
    setLoading(true);

    listView.setOnItemClickListener(
        (parent, view, position, id) -> {
          try {
            editorAutoCompletion.select(position);
          } catch (Exception e) {
            e.printStackTrace(System.err);
            Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
          }
        });

    return rootLayout;
  }

  @Override
  public void onApplyColorScheme(@NonNull EditorColorScheme colorScheme) {
    GradientDrawable gd = new GradientDrawable();
    gd.setCornerRadius(
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            LAYOUT_CORNER_RADIUS,
            editorAutoCompletion.getContext().getResources().getDisplayMetrics()));
    //gd.setColor(colorScheme.getColor(EditorColorScheme.COMPLETION_WND_BACKGROUND));
    //gd.setStroke(1, colorScheme.getColor(EditorColorScheme.COMPLETION_WND_CORNER));
    gd.setStroke(1, MaterialColors.getColor(editor.getContext(), com.google.android.material.R.attr.colorOutline, 0));
    gd.setColor(SurfaceColors.SURFACE_1.getColor(editor.getContext()));    
    rootView.setBackground(gd);
    // rootView.invalidate();
  }

  @Override
  public void setLoading(boolean state) {
    isLoading = state;
    editor.setIndexing(state);
  }

  @NonNull
  @Override
  public ListView getCompletionList() {
    return listView;
  }

  /** Perform motion events */
  private void performScrollList(int offset) {
    ListView adpView = getCompletionList();

    long down = SystemClock.uptimeMillis();
    MotionEvent ev = MotionEvent.obtain(down, down, MotionEvent.ACTION_DOWN, 0, 0, 0);
    adpView.onTouchEvent(ev);
    ev.recycle();

    ev = MotionEvent.obtain(down, down, MotionEvent.ACTION_MOVE, 0, offset, 0);
    adpView.onTouchEvent(ev);
    ev.recycle();

    ev = MotionEvent.obtain(down, down, MotionEvent.ACTION_CANCEL, 0, offset, 0);
    adpView.onTouchEvent(ev);
    ev.recycle();
  }

  @Override
  public void ensureListPositionVisible(int position, int increment) {
    listView.post(
        () -> {
          while (listView.getFirstVisiblePosition() + 1 > position && listView.canScrollList(-1)) {
            performScrollList(increment / 2);
          }
          while (listView.getLastVisiblePosition() - 1 < position && listView.canScrollList(1)) {
            performScrollList(-increment / 2);
          }
        });
  }

  public boolean isLoading() {
    return isLoading;
  }

  public void setCornerRadius(final int radius) {
    LAYOUT_CORNER_RADIUS = radius;
  }
}
