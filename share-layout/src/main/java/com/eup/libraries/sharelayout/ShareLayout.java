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
 
   package com.eup.libraries.sharelayout;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleableRes;
import androidx.appcompat.content.res.AppCompatResources;
import com.google.android.material.button.MaterialButton;

/**
 * A ShareLayout is a derivative of {@link LinrearLayout} that displays information to the user. To
 * provide details about sharing an application, see {@link android.widget.EditText}.
 *
 * <p>The following code sample shows a typical use, with an XML layout and code to modify the
 * contents of the share layout:
 *
 * <pre>
 * &lt;LinearLayout
 *        xmlns:android="http://schemas.android.com/apk/res/android"
 *        android:layout_width="match_parent"
 *        android:layout_height="match_parent"&gt;
 *    &lt;ShareLayout
 *            android:id="@+id/share_layout"
 *            android:layout_width="match_parent"
 *            android:layout_height="wrap_content"
 *            app:showProgressBar="true"
 *            app:actionStartColor="@color/your_color"
 *            app:actionEndColor="@color/your_color"
 *            app:titleText="@string/share_text"
 *            app:summaryText="@string/summary_text"
 *            app:promptText="@string/prompt_text"
 *            app:actionText="@string/action_text" /&gt;
 * &lt;/LinearLayout&gt;
 * </pre>
 *
 * <p>This code sample demonstrates how to modify the contents of the share layout view defined in
 * the previous XML layout:
 *
 * <pre>
 * public class MainActivity extends Activity {
 *
 *    protected void onCreate(Bundle savedInstanceState) {
 *         super.onCreate(savedInstanceState);
 *         setContentView(R.layout.activity_main);
 *         final ShareLayout shareLayout = (ShareLayout) findViewById(R.id.share_layout_id);
 *         shareLayout.setTitleText(R.string.user_title);
 *         shareLayout.setSummaryText(R.string.user_summary);
 *         shareLayout.setSharePromptText(R.string.user_prompt);
 *         shareLayout.setShareButtonText(R.string.user_share);
 *         shareLayout.setShowProgressBar(true);
 *         if(shareLayout.isShowProgressBar()){
 *           // TODO: Implement action
 *            }
 *         shareLayout.setOnItemClickListener(v -> {
 *
 *         });
 *     }
 * }
 * </pre>
 */
public class ShareLayout extends LinearLayout {

  /** Interface definition for a callback to be invoked when the progress bar state changes. */
  public interface OnCheckProgressListener {
    /**
     * Called when the checked state of a MaterialButton has changed.
     *
     * @param progressBar The ProgressBar whose state has changed.
     * @param isVisible The new visibility state of the ProgressBar.
     */
    void onStartProgress(ProgressBar progressBar, boolean isVisible);

    void onEndProgress(ProgressBar progressBar, boolean isVisible);
  }

  private LinearLayout root, shareLinear, shareContainer;
  private TextView title, summary, sharePrompt;
  private MaterialButton shareButton;
  private ProgressBar progressBar;
  public static final int MATCH_PARENT = -1;
  public static final int WRAP_CONTENT = -2;
  private boolean mShowProgressBar;
  @ColorInt private int layoutColor;
  private String titleText;
  private String summaryText;
  private String promptText;
  private String actionText;
  private int elevation, shareActionsCornerRadius;
  private int titleTextAppearanceResId;
  private int summaryTextAppearanceResId;
  private int promptTextAppearanceResId;
  private int actionTextAppearanceResId; // TODO: rename all #actionText to #actionButton
  private static final int DEF_STYLE_RES = R.style.Widget_TeckrodMiUi_ShareLayout;
  private boolean cornerRadiusSet = false;
  private UiBuilder uiBuilder;

  public ShareLayout(@NonNull Context context) {
    this(context, null);
  }

  public ShareLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, R.attr.ShareLayoutStyle);
  }

  public ShareLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    init(context, attrs, defStyle);
  }

  private void init(Context context, AttributeSet attrs, int defStyle) {
    uiBuilder = new UiBuilder(context);
    TypedArray a =
        context
            .getTheme()
            .obtainStyledAttributes(attrs, R.styleable.ShareLayout, defStyle, DEF_STYLE_RES);
    try {
      mShowProgressBar = a.getBoolean(R.styleable.ShareLayout_showProgressBar, false);
      titleText = a.getString(R.styleable.ShareLayout_titleText);
      summaryText = a.getString(R.styleable.ShareLayout_summaryText);
      promptText = a.getString(R.styleable.ShareLayout_promptText);
      actionText = a.getString(R.styleable.ShareLayout_shareButtonText);
      shareActionsCornerRadius =
          a.getDimensionPixelSize(R.styleable.ShareLayout_shareActionsCornerRadius, -1);
      if (a.hasValue(R.styleable.ShareLayout_shareActionsCornerRadius)) {
        shareActionsCornerRadius =
            a.getDimensionPixelSize(R.styleable.ShareLayout_shareActionsCornerRadius, -1);
        cornerRadiusSet = true;
      } else {
        shareActionsCornerRadius =
            getResources().getDimensionPixelSize(R.dimen.action_default_radius);
      }
      titleTextAppearanceResId = a.getResourceId(R.styleable.ShareLayout_titleTextAppearance, 0);
      summaryTextAppearanceResId =
          a.getResourceId(R.styleable.ShareLayout_summaryTextAppearance, 0);
      promptTextAppearanceResId = a.getResourceId(R.styleable.ShareLayout_promptTextAppearance, 0);
      actionTextAppearanceResId =
          a.getResourceId(R.styleable.ShareLayout_shareButtonTextAppearance, 0);
      if (a.hasValue(R.styleable.ShareLayout_elevation)) {
        elevation = a.getDimensionPixelSize(R.styleable.ShareLayout_elevation, 0);
      } else {
        elevation = getResources().getDimensionPixelSize(R.dimen.action_default_elevation);
      }
      layoutColor =
          getColorStateList(context, a, R.styleable.ShareLayout_shareLayoutColor).getDefaultColor();
    } finally {
      a.recycle();
    }
    root = new LinearLayout(context);
    shareLinear = new LinearLayout(context);
    shareContainer = new LinearLayout(context);
    title = new TextView(context);
    summary = new TextView(context);
    sharePrompt = new TextView(context);
    shareButton = new MaterialButton(context);
    progressBar = new ProgressBar(context);

    shareLinear.setOrientation(LinearLayout.VERTICAL);
    shareLinear.setGravity(Gravity.CENTER);
    addView(root, uiBuilder.createLinear(MATCH_PARENT, MATCH_PARENT));
    shareLinear.addView(title, uiBuilder.createLinear(WRAP_CONTENT, WRAP_CONTENT, Gravity.START));
    title.setText(titleText);
    title.setTextSize(16);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      title.setTextAppearance(titleTextAppearanceResId);
    } else {
      title.setTextSize(16);
    }
    // title.setPadding(dp(8), dp(8), dp(8), dp(8));
    shareLinear.addView(summary, uiBuilder.createLinear(WRAP_CONTENT, WRAP_CONTENT, Gravity.START));

    summary.setText(summaryText);
    summary.setPadding(dp(0), dp(6), dp(0), dp(8));
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      summary.setTextAppearance(summaryTextAppearanceResId);
    } else {
      summary.setTextSize(12);
    }
    shareContainer.setOrientation(LinearLayout.HORIZONTAL);
    shareContainer.setLayoutParams(uiBuilder.createLinear(MATCH_PARENT, MATCH_PARENT));
    shareContainer.addView(
        sharePrompt,
        uiBuilder.createLinear(MATCH_PARENT, MATCH_PARENT, Gravity.CENTER_VERTICAL, 1));
    sharePrompt.setText(promptText);
    sharePrompt.setPadding(dp(8), dp(8), dp(8), dp(8));
    sharePrompt.setTextSize(14);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      sharePrompt.setTextAppearance(promptTextAppearanceResId);
    } else {
      sharePrompt.setTextSize(14);
    }
    setShowProgressBar(mShowProgressBar);
    sharePrompt.setGravity(Gravity.CENTER_VERTICAL);
    shareContainer.addView(
        progressBar, uiBuilder.createLinear(WRAP_CONTENT, MATCH_PARENT, Gravity.CENTER));
    progressBar.setPadding(dp(5), dp(5), dp(5), dp(5));
    progressBar.setBackgroundColor(Color.TRANSPARENT);
    shareContainer.addView(shareButton, uiBuilder.createLinear(WRAP_CONTENT, 40, Gravity.CENTER));
    shareButton.setText(actionText);
    shareButton.setInsetTop(0);
    shareButton.setInsetBottom(0);
    shareButton.setCornerRadius(shareActionsCornerRadius);
    shareButton.setTextSize(14);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      shareButton.setTextAppearance(actionTextAppearanceResId);
    } else {
      shareButton.setTextSize(14);
    }
    shareLinear.addView(shareContainer);
    GradientDrawable gd = new GradientDrawable();
    gd.setColor(layoutColor);
    gd.setCornerRadius(shareActionsCornerRadius);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      setElevation(elevation);
    }
    shareContainer.setBackground(gd);
    root.addView(shareLinear, uiBuilder.createLinear(MATCH_PARENT, MATCH_PARENT));
    initialize();
  }

  public boolean isShowProgressBar() {
    return mShowProgressBar;
  }

  public void setShowProgressBar(boolean showProgressBar) {
    mShowProgressBar = showProgressBar;
    if (showProgressBar) {
      progressBar.setVisibility(View.VISIBLE);
    } else {
      progressBar.setVisibility(View.GONE);
    }
    invalidate();
    requestLayout();
  }

  private OnItemClickListener mListener;

  public interface OnItemClickListener {
    void onItemClick(View view);
  }

  public void setOnItemClickListener(OnItemClickListener listener) {
    mListener = listener;
  }

  private void initialize() {
    shareButton.setOnClickListener(
        v -> {
          if (mListener != null) {
            mListener.onItemClick(v);
          }
        });
  }

  void setCornerRadius(int cornerRadius) {
    // If cornerRadius wasn't set in the style, it would have a default value of -1. Therefore, for
    // setCornerRadius(-1) to take effect, we need this cornerRadiusSet flag.
    if (!cornerRadiusSet || this.shareActionsCornerRadius != cornerRadius) {
      this.shareActionsCornerRadius = cornerRadius;
      cornerRadiusSet = true;
    }
  }

  public void setTitleText(String text) {
    title.setText(text);
  }

  public void setSummaryText(String text) {
    summary.setText(text);
  }

  public void setSharePromptText(String text) {
    sharePrompt.setText(text);
  }
  
  public TextView getSharePrompt() {
    return sharePrompt;
  }

  /** Sets the */
  public void setShareButtonText(String text) {
    shareButton.setText(text);
  }

  /**
   * Returns the dp value
   *
   * @see {@link uiBuilder.dp(value)}
   */
  private int dp(float value) {
    return uiBuilder.dp(value);
  }

  @Nullable
  public static ColorStateList getColorStateList(
      @NonNull Context context, @NonNull TypedArray attributes, @StyleableRes int index) {
    if (attributes.hasValue(index)) {
      int resourceId = attributes.getResourceId(index, 0);
      if (resourceId != 0) {
        ColorStateList value = AppCompatResources.getColorStateList(context, resourceId);
        if (value != null) {
          return value;
        }
      }
    }

    // Reading a single color with getColorStateList() on API 15 and below doesn't always correctly
    // read the value. Instead we'll first try to read the color directly here.
    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
      int color = attributes.getColor(index, -1);
      if (color != -1) {
        return ColorStateList.valueOf(color);
      }
    }

    return attributes.getColorStateList(index);
  }
}
