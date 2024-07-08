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
 
   package com.eup.codeopsstudio.util;

import android.animation.ObjectAnimator;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.view.animation.Transformation;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.StringRes;
import androidx.core.app.ShareCompat;
import com.eup.codeopsstudio.common.Constants;
import com.eup.codeopsstudio.common.ContextManager;
import com.eup.codeopsstudio.res.R;

public class BaseUtil {

  private static final Context context = ContextManager.getApplicationContext();
  public static final int LENGTH_SHORT = 0;
  public static final int LENGTH_LONG = 1;
  public static final int MEDIUM_SCREEN_WIDTH_SIZE = 600;
  public static final int LARGE_SCREEN_WIDTH_SIZE = 1240;

  public static int dp(float px) {
    if (px == 0) {
      return 0;
    }
    return Math.round(context.getResources().getDisplayMetrics().density * px);
  }

  /**
   * method to display toast with a certain duration @StringRes returns the string id of a string
   * res
   */
  public static void showToast(@StringRes int id, int duration) {
    switch (duration) {
      case LENGTH_SHORT:
        Toast.makeText(context, id, Toast.LENGTH_SHORT).show();
        break;
      case LENGTH_LONG:
        Toast.makeText(context, id, Toast.LENGTH_LONG).show();
        break;
    }
  }

  /*
   * method to display toast with a certain duration
   */
  public static void showToast(String message, int duration) {
    switch (duration) {
      case LENGTH_SHORT:
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        break;
      case LENGTH_LONG:
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
        break;
    }
  }

  public static void showToast(String message) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
  }

  /**
   * Converts a dp value into px that can be applied on margins, paddings etc
   *
   * @param dp The dp value that will be converted into px
   * @return The converted px value from the dp argument given
   */
  public static int dpToPx(float dp) {
    return Math.round(
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics()));
  }

  public static int getRowCount(int itemWidth) {
    DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
    return (displayMetrics.widthPixels / itemWidth);
  }

  public static void expand(final View v) {
    int matchParentMeasureSpec =
        View.MeasureSpec.makeMeasureSpec(
            ((View) v.getParent()).getWidth(), View.MeasureSpec.EXACTLY);
    int wrapContentMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
    v.measure(matchParentMeasureSpec, wrapContentMeasureSpec);
    final int targetHeight = v.getMeasuredHeight();

    // Older versions of android (pre API 21) cancel animations for views with a height of 0.
    v.getLayoutParams().height = 1;
    v.setVisibility(View.VISIBLE);
    Animation a =
        new Animation() {
          @Override
          protected void applyTransformation(float interpolatedTime, Transformation t) {
            v.getLayoutParams().height =
                interpolatedTime == 1
                    ? ViewGroup.LayoutParams.WRAP_CONTENT
                    : (int) (targetHeight * interpolatedTime);
            v.requestLayout();
          }

          @Override
          public boolean willChangeBounds() {
            return true;
          }
        };

    // Expansion speed of 1dp/ms
    a.setDuration(
        (int) (targetHeight / v.getContext().getResources().getDisplayMetrics().density) / 2);
    v.startAnimation(a);
  }

  public static void collapse(final View v) {
    final int initialHeight = v.getMeasuredHeight();

    Animation a =
        new Animation() {
          @Override
          protected void applyTransformation(float interpolatedTime, Transformation t) {
            if (interpolatedTime == 1) {
              v.setVisibility(View.GONE);
            } else {
              v.getLayoutParams().height = initialHeight - (int) (initialHeight * interpolatedTime);
              v.requestLayout();
            }
          }

          @Override
          public boolean willChangeBounds() {
            return true;
          }
        };

    // Collapse speed of 1dp/ms
    a.setDuration(
        (int) (initialHeight / v.getContext().getResources().getDisplayMetrics().density) / 2);
    v.startAnimation(a);
  }

  public static boolean isExpanded(View v) {
    return v.getVisibility() == View.VISIBLE;
  }

  public static boolean isCollapsed(View v) {
    return v.getVisibility() == View.GONE;
  }

  public static void rotateChevron(boolean isOpen, ImageView chevronView) {
    float startRotation = isOpen ? -90f : 0f;
    float endRotation = isOpen ? 0f : -90f;

    RotateAnimation rotateAnimation =
        new RotateAnimation(
            startRotation,
            endRotation,
            Animation.RELATIVE_TO_SELF,
            0.5f,
            Animation.RELATIVE_TO_SELF,
            0.5f);
    rotateAnimation.setDuration(200);
    rotateAnimation.setFillAfter(true);
    chevronView.startAnimation(rotateAnimation);
  }

  public static void copyToClipBoard(String text) {
    ClipboardManager clipboardManager =
        (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
    ClipData clipData = ClipData.newPlainText("", text);
    clipboardManager.setPrimaryClip(clipData);
  }

  public static void copyToClipBoard(String text, boolean withToast) {
    copyToClipBoard(text);
    if (withToast) showToast(context.getString(R.string.copied_to_clipboard), LENGTH_SHORT);
  }

  public static void startObjectAnimation(
      View view, String propertyName, double value, double duration) {
    ObjectAnimator anim = new ObjectAnimator();
    anim.setTarget(view);
    anim.setPropertyName(propertyName);
    anim.setFloatValues((float) value);
    anim.setDuration((long) duration);
    anim.start();
  }

  public static boolean isConnected() {
    ConnectivityManager connectivityManager =
        (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
    return activeNetworkInfo != null && activeNetworkInfo.isConnected();
  }

  /**
   * Method to share application link. Sharing the app as a file is not recommended
   *
   * @param context The context where the share menu would appear
   */
  public static void shareAppPlayStoreLink() {
    ShareCompat.IntentBuilder shareIntent = new ShareCompat.IntentBuilder(context);
    shareIntent.setType("text/plain");
    shareIntent.setChooserTitle(context.getString(R.string.app_name));
    shareIntent.setText(context.getString(R.string.share_app_info, Constants./*GOOGLE_PLAY_APP_URL*/CHECK_UPDATE_GITHUB_URL));
    shareIntent.getIntent().addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    shareIntent.startChooser();
  }

  /**
   * Method to share application link. Sharing the app as a file is not recommended
   *
   * @param context The context where the share menu would appear
   */
  public static void shareAppPlayStoreLink(Context ctx) {
    ShareCompat.IntentBuilder shareIntent = new ShareCompat.IntentBuilder(ctx);
    shareIntent.setType("text/plain");
    shareIntent.setChooserTitle(ctx.getString(R.string.app_name));
    shareIntent.setText(ctx.getString(R.string.share_app_info, Constants./*GOOGLE_PLAY_APP_URL*/CHECK_UPDATE_GITHUB_URL));
    shareIntent.getIntent().addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    shareIntent.startChooser();
  }

  public static void openUrl(String url) {
    // TODO: Revamp to openUrl(FragmentActivity activity, String url)
    try {
      var mIntent = new Intent(Intent.ACTION_VIEW);
      mIntent.setData(Uri.parse(url));
      context.startActivity(mIntent);
    } catch (Throwable throwable) {
      showToast(throwable.getMessage());
      throwable.printStackTrace();
    }
  }

  public static void openUrlOutsideActivity(String url) {
    try {
      var mIntent = new Intent(Intent.ACTION_VIEW);
      mIntent.setData(Uri.parse(url));
      mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      context.startActivity(mIntent);
    } catch (Throwable throwable) {
      showToast(throwable.getMessage());
      throwable.printStackTrace();
    }
  }

  public static int getScreenSize() {
    Configuration configuration = context.getResources().getConfiguration();
    return configuration.screenWidthDp;
  }

  public static boolean isSmallScreenSize() {
    return getScreenSize() < MEDIUM_SCREEN_WIDTH_SIZE;
  }

  public static boolean isMediumScreenSize() {
    int size = getScreenSize();
    return getScreenSize() >= MEDIUM_SCREEN_WIDTH_SIZE && size < LARGE_SCREEN_WIDTH_SIZE;
  }

  public static boolean isLargeScreenSize() {
    return getScreenSize() >= LARGE_SCREEN_WIDTH_SIZE;
  }
}
