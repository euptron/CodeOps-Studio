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

package com.eup.codeopsstudio.ui;

import static com.google.android.material.theme.overlay.MaterialThemeOverlay.wrap;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.ColorStateListDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.eup.codeopsstudio.R;
import com.eup.codeopsstudio.util.ThemeExporter;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.ShapeAppearanceModel;


/**
 * A customized {@link DrawerLayout} implementation with Material Design enhancements for CodeOps
 * Studio application.
 *
 * <p>This drawer layout extends the standard Android {@link DrawerLayout} with several advanced
 * features:
 *
 * <ul>
 *   <li>Rounded corners on drawer views with configurable corner radius
 *   <li>Smart touch interception to prevent conflicts with horizontally scrolling content
 *   <li>Material Design theme integration with automatic color theming
 *   <li>Support for elevation and Material Shape theming
 *   <li>Automatic drawer background color resolution from theme attributes
 * </ul>
 *
 * <h3>Usage Example:</h3>
 *
 * <pre>{@code
 * <com.eup.codeopsstudio.ui.PrimaryDrawerLayout
 *     android:id="@+id/drawer_layout"
 *     android:layout_width="match_parent"
 *     android:layout_height="match_parent"
 *     app:drawerCornerSize="16dp"
 *     app:elevation="2dp">
 *
 *     <!-- Your main content -->
 *     <FrameLayout
 *         android:id="@+id/content_frame"
 *         android:layout_width="match_parent"
 *         android:layout_height="match_parent" />
 *
 *     <!-- Your drawer content -->
 *     <ListView
 *         android:id="@+id/left_drawer"
 *         android:layout_width="240dp"
 *         android:layout_height="match_parent"
 *         android:layout_gravity="start" />
 *
 * </com.eup.codeopsstudio.ui.PrimaryDrawerLayout>
 * }</pre>
 *
 * <h3>Custom Attributes:</h3>
 *
 * <ul>
 *   <li>{@code drawerCornerSize} - Corner radius for drawer views (dimension)
 *   <li>{@code elevation} - Elevation shadow depth (dimension)
 *   <li>{@code android:colorBackground} - Custom background color for drawers (color)
 * </ul>
 *
 * <h3>Key Features:</h3>
 *
 * <ol>
 *   <li><b>Smart Touch Handling</b>: Automatically detects horizontally scrolling child views and
 *       prevents drawer gesture conflicts
 *   <li><b>Theme Integration</b>: Automatically picks drawer background colors from Material Design
 *       theme attributes (colorSurface, colorSurfaceContainerLow)
 *   <li><b>Direction-Aware Corners</b>: Applies rounded corners only to the outer edges of drawers
 *       (left drawer has right corners rounded, right drawer has left corners rounded)
 *   <li><b>Background Preservation</b>: Maintains existing drawer backgrounds when applying corner
 *       effects
 * </ol>
 *
 * <h3>Theme Requirements:</h3>
 *
 * <p>For proper theming, ensure your app theme inherits from a Material Design theme (e.g., {@code
 * Theme.Material3.DayNight}) and defines the following attributes:
 *
 * <ul>
 *   <li>{@code colorSurface} - Primary surface color
 *   <li>{@code colorSurfaceContainerLow} - Low-emphasis surface variant
 * </ul>
 *
 * @see DrawerLayout
 * @see MaterialShapeDrawable
 * @see ThemeExporter
 * @author Etido Peter
 */
public class PrimaryDrawerLayout extends DrawerLayout {

  public static final String TAG = PrimaryDrawerLayout.class.getSimpleName();

  private static final int DEF_STYLE_RES = R.style.AppTheme_Primary_DrawerLayout;

  private final int elevation;
  private final int cornerSize;
  private final int colorBackground;
  private final Context themedContext;
  private final Rect rect = new Rect();

  public PrimaryDrawerLayout(@NonNull Context context) {
    this(context, null);
  }

  public PrimaryDrawerLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, R.attr.PrimaryDrawerLayoutStyle);
  }

  public PrimaryDrawerLayout(
      @NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(wrap(context, attrs, defStyleAttr, DEF_STYLE_RES), attrs, defStyleAttr);
    themedContext = getContext();

    final TypedArray a =
        context
            .getTheme()
            .obtainStyledAttributes(
                attrs, R.styleable.PrimaryDrawerLayout, defStyleAttr, DEF_STYLE_RES);

    try {
      elevation = a.getDimensionPixelSize(R.styleable.PrimaryDrawerLayout_elevation, 0);
      colorBackground = a.getColor(R.styleable.PrimaryDrawerLayout_android_colorBackground, 0);
      cornerSize = a.getDimensionPixelSize(R.styleable.PrimaryDrawerLayout_drawerCornerSize, 0);
    } finally {
      a.recycle();
    }

    setElevation(elevation);
  }

  /**
   * Intercepts touch events to prevent drawer gestures from interfering with horizontally scrolling
   * child views.
   *
   * <p>This method detects if the touch event occurs within a horizontally scrollable view (like a
   * RecyclerView or HorizontalScrollView) and allows the child to handle horizontal scrolling
   * gestures instead of triggering drawer open/close actions.
   *
   * @param ev The touch screen motion event
   * @return {@code true} if the event should be intercepted, {@code false} otherwise
   * @see #findHorizontalScrollingChild(ViewGroup, float, float)
   */
  @Override
  public boolean onInterceptTouchEvent(MotionEvent ev) {
    View child = findHorizontalScrollingChild(this, ev.getX(), ev.getY());
    return child == null && super.onInterceptTouchEvent(ev);
  }

  /**
   * Applies rounded corners to all drawer views after inflation.
   *
   * <p>This method is called after all child views have been inflated and sets up
   * MaterialShapeDrawable backgrounds with appropriate corner rounding based on drawer position
   * (left or right gravity).
   *
   * @see #setDrawerViewCorners(View)
   */
  @Override
  protected void onFinishInflate() {
    super.onFinishInflate();
    applyDrawerViewCorners();
  }

  /**
   * Recursively searches for horizontally scrolling child views at the given coordinates.
   *
   * <p>This method traverses the view hierarchy to find any view that can scroll horizontally
   * (returns {@code true} for {@link View#canScrollHorizontally(int)}) at the specified touch
   * coordinates. This prevents drawer swipe gestures from interfering with scrolling content.
   *
   * @param parent The parent view group to search within
   * @param x The X coordinate relative to parent
   * @param y The Y coordinate relative to parent
   * @return The horizontally scrolling child view, or {@code null} if not found
   */
  @Nullable
  View findHorizontalScrollingChild(ViewGroup parent, float x, float y) {
    int n = parent.getChildCount();
    if (parent == this && n <= 1) return null;

    int start = 0;
    if (parent == this) start = 1;

    for (int i = start; i < n; i++) {
      View child = parent.getChildAt(i);
      if (child.getVisibility() != View.VISIBLE) continue;

      child.getHitRect(rect);
      if (rect.contains((int) x, (int) y)) {
        if (child.canScrollHorizontally(1)) {
          return child;
        } else if (child instanceof ViewGroup) {
          View v = findHorizontalScrollingChild((ViewGroup) child, x - rect.left, y - rect.top);
          if (v != null) {
            return v;
          }
        }
      }
    }
    return null;
  }

  void applyDrawerViewCorners() {
    for (int i = 0; i < getChildCount(); i++) {
      View child = getChildAt(i);
      if (isDrawerView(child)) {
        setDrawerViewCorners(child);
      }
    }
  }

  void setDrawerViewCorners(View view) {
    Drawable existingBackground = view.getBackground();
    MaterialShapeDrawable shapeDrawable;

    if (existingBackground instanceof MaterialShapeDrawable) {
      shapeDrawable = (MaterialShapeDrawable) existingBackground;
    } else {
      shapeDrawable = new MaterialShapeDrawable();
      ColorStateList fillColor = null;

      if (existingBackground != null) {
        fillColor = getColorStateListOrNull(existingBackground);
      }

      if (fillColor == null) {
        int themeColor = getThemedDrawerBackground();
        fillColor = ColorStateList.valueOf(themeColor);
      }

      shapeDrawable.setFillColor(fillColor);
      shapeDrawable.initializeElevationOverlay(themedContext);
    }

    ShapeAppearanceModel.Builder shapeAppearanceBuilder =
        shapeDrawable.getShapeAppearanceModel().toBuilder();

    if (isAbsoluteGravityLeft(view)) {
      shapeAppearanceBuilder
          .setTopLeftCornerSize(0)
          .setBottomLeftCornerSize(0)
          .setTopRightCornerSize(cornerSize)
          .setBottomRightCornerSize(cornerSize);
    } else {
      shapeAppearanceBuilder
          .setTopLeftCornerSize(cornerSize)
          .setBottomLeftCornerSize(cornerSize)
          .setTopRightCornerSize(0)
          .setBottomRightCornerSize(0);
    }

    shapeDrawable.setShapeAppearanceModel(shapeAppearanceBuilder.build());
    view.setBackground(shapeDrawable);
  }

  int getThemedDrawerBackground() {
    int color = ThemeExporter.getMaterialColor(themedContext, "colorSurfaceContainerLow");

    if (color == Color.TRANSPARENT) {
      color = ThemeExporter.getMaterialColor(themedContext, "colorSurface");
    }

    if (color == Color.TRANSPARENT && colorBackground > 0) {
      color = colorBackground;
    }

    if (color == Color.TRANSPARENT) {
      color = Color.DKGRAY;
    }

    return color;
  }

  boolean isDrawerView(View child) {
    if (child == null) return false;

    final LayoutParams lp = (LayoutParams) child.getLayoutParams();
    if (lp == null) return false;
    final int gravity = lp.gravity;
    final int absGravity = getAbsoluteGravity(child, gravity);

    return (absGravity & Gravity.LEFT) != 0 || (absGravity & Gravity.RIGHT) != 0;
  }

  boolean isAbsoluteGravityLeft(View view) {
    final LayoutParams lp = (LayoutParams) view.getLayoutParams();
    int gravity = lp.gravity;
    return getAbsoluteGravity(view, gravity) == Gravity.LEFT;
  }

  int getAbsoluteGravity(View view, int gravity) {
    return Gravity.getAbsoluteGravity(gravity, ViewCompat.getLayoutDirection(view));
  }

  @Nullable
  ColorStateList getColorStateListOrNull(@Nullable final Drawable drawable) {
    if (drawable == null) {
      return null;
    }

    if (drawable instanceof ColorDrawable) {
      return ColorStateList.valueOf(((ColorDrawable) drawable).getColor());
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      if (drawable instanceof ColorStateListDrawable) {
        return ((ColorStateListDrawable) drawable).getColorStateList();
      }
    }

    if (drawable instanceof MaterialShapeDrawable) {
      return ((MaterialShapeDrawable) drawable).getFillColor();
    }

    return null;
  }
}
