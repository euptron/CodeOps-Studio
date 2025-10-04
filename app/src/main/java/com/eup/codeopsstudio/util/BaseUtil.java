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

package com.eup.codeopsstudio.util;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.text.format.Formatter;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.view.animation.Transformation;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.app.ShareCompat;
import androidx.core.content.ContextCompat;

import com.eup.codeopsstudio.IdeApplication;
import com.eup.codeopsstudio.R;
import com.eup.codeopsstudio.common.AsyncTask;
import com.eup.codeopsstudio.common.Constants;
import com.eup.codeopsstudio.common.ILog;
import com.eup.codeopsstudio.common.models.Event;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import java.util.Objects;

public class BaseUtil {

    public static final String TAG = BaseUtil.class.getSimpleName();
    public static final int MEDIUM_SCREEN_WIDTH_SIZE = 600;
    public static final int LARGE_SCREEN_WIDTH_SIZE = 1240;
    private static final int TAG_ON_GLOBAL_LAYOUT_LISTENER = -8;
    private static int sDecorViewDelta = 0;

    public static void collapse(final View v) {
        final int initialHeight = v.getMeasuredHeight();

        Animation a = new Animation() {
            @Override
            public boolean willChangeBounds() {
                return true;
            }

            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if (interpolatedTime == 1) {
                    v.setVisibility(View.GONE);
                } else {
                    v.getLayoutParams().height =
                        initialHeight - (int) (initialHeight * interpolatedTime);
                    v.requestLayout();
                }
            }
        };

        // Collapse speed of 1dp/ms
        a.setDuration(
            (int) (initialHeight / v.getContext().getResources().getDisplayMetrics().density) / 2);
        v.startAnimation(a);
    }

    public static void copyToClipBoard(String text, boolean withToast) {
        copyToClipBoard(text);
        if (withToast) toastShort(R.string.copied_to_clipboard);
    }

    public static void copyToClipBoard(String text) {
        ClipboardManager clipboardManager =
            (ClipboardManager) IdeApplication.getGlobalSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText("", text);
        clipboardManager.setPrimaryClip(clipData);
    }

    public static void toastShort(final @StringRes int stringRes) {
        AsyncTask.runOnUiThread(() -> Toast
            .makeText(IdeApplication.getGlobalContext(), stringRes, Toast.LENGTH_SHORT).show());
    }

    public static int dp(float dimension) {
        if (dimension == 0) return 0;

        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dimension,
            IdeApplication
            .getGlobalResources().getDisplayMetrics()));
    }

    public static void expand(final View v) {
        int matchParentMeasureSpec =
            View.MeasureSpec.makeMeasureSpec(((View) v.getParent()).getWidth(),
                View.MeasureSpec.EXACTLY);
        int wrapContentMeasureSpec = View.MeasureSpec.makeMeasureSpec(0,
            View.MeasureSpec.UNSPECIFIED);
        v.measure(matchParentMeasureSpec, wrapContentMeasureSpec);
        final int targetHeight = v.getMeasuredHeight();

        // Older versions of android (pre API 21) cancel animations for views with a height of 0.
        v.getLayoutParams().height = 1;
        v.setVisibility(View.VISIBLE);
        Animation a = new Animation() {
            @Override
            public boolean willChangeBounds() {
                return true;
            }

            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                v.getLayoutParams().height =
                    interpolatedTime == 1 ? ViewGroup.LayoutParams.WRAP_CONTENT
                        : (int) (targetHeight * interpolatedTime);
                v.requestLayout();
            }
        };

        // Expansion speed of 1dp/ms
        a.setDuration(
            (int) (targetHeight / v.getContext().getResources().getDisplayMetrics().density) / 2);
        v.startAnimation(a);
    }

    public static String getMemoryUsage(Runtime runtime) {
        long maxMemoryInBytes = runtime.maxMemory();
        long availableMemInBytes =
            maxMemoryInBytes - (runtime.totalMemory() - runtime.freeMemory());
        long usedMemInBytes = maxMemoryInBytes - availableMemInBytes;
        long usedMemInPercentage = usedMemInBytes * 100 / maxMemoryInBytes;

        return IdeApplication.getGlobalContext()
                             .getString(R.string.app_memory_usage,
                                 Formatter.formatShortFileSize(IdeApplication.getGlobalContext(),
                                     usedMemInBytes),
                                 Formatter.formatShortFileSize(IdeApplication.getGlobalContext(),
                                     maxMemoryInBytes), usedMemInPercentage);
    }

    public static int getRowCount(int itemWidth) {
        DisplayMetrics displayMetrics = IdeApplication.getGlobalResources().getDisplayMetrics();
        return (displayMetrics.widthPixels / itemWidth);
    }

    public static boolean isCollapsed(View v) {
        return v.getVisibility() == View.GONE;
    }

    public static boolean isConnected() {
        ConnectivityManager connectivityManager = IdeApplication.getConnectivityManager();
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static boolean isExpanded(View v) {
        return v.getVisibility() == View.VISIBLE;
    }

    public static boolean isLargeScreenSize() {
        return getScreenSize() >= LARGE_SCREEN_WIDTH_SIZE;
    }

    public static int getScreenSize() {
        Configuration configuration = IdeApplication.getGlobalConfiguration();
        return configuration.screenWidthDp;
    }

    public static boolean isMediumScreenSize() {
        int size = getScreenSize();
        return getScreenSize() >= MEDIUM_SCREEN_WIDTH_SIZE && size < LARGE_SCREEN_WIDTH_SIZE;
    }

    public static boolean isSmallScreenSize() {
        return getScreenSize() < MEDIUM_SCREEN_WIDTH_SIZE;
    }

    /**
     * Return whether soft input is visible.
     *
     * @param activity The activity.
     * @return {@code true}: yes<br>
     * {@code false}: no
     */
    public static boolean isSoftInputVisible(@NonNull final Activity activity) {
        return getDecorViewInvisibleHeight(activity.getWindow()) > 0;
    }

    private static int getDecorViewInvisibleHeight(@NonNull final Window window) {
        final View decorView = window.getDecorView();
        final Rect outRect = new Rect();
        decorView.getWindowVisibleDisplayFrame(outRect);
        ILog.debug("KeyboardUtils",
            "getDecorViewInvisibleHeight: " + (decorView.getBottom() - outRect.bottom));
        int delta = Math.abs(decorView.getBottom() - outRect.bottom);
        if (delta <= getNavBarHeight() + getStatusBarHeight()) {
            sDecorViewDelta = delta;
            return 0;
        }
        return delta - sDecorViewDelta;
    }

    /**
     * Return the navigation bar's height.
     *
     * @return the navigation bar's height
     */
    public static int getNavBarHeight() {
        Resources res = Resources.getSystem();
        int resourceId = res.getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId != 0) {
            return res.getDimensionPixelSize(resourceId);
        } else {
            return 0;
        }
    }

    /**
     * Return the status bar's height.
     *
     * @return the status bar's height
     */
    public static int getStatusBarHeight() {
        Resources resources = Resources.getSystem();
        int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
        return resources.getDimensionPixelSize(resourceId);
    }

    public static SnackBarBuilder newSnackBarBuilder() {
        return new SnackBarBuilder();
    }

    public static void openUrl(String url) {
        // FIX: Revamp to openUrl(FragmentActivity activity, String url)
        try {
            var mIntent = new Intent(Intent.ACTION_VIEW);
            mIntent.setData(Uri.parse(url));
            IdeApplication.getGlobalContext().startActivity(mIntent);
        } catch (Throwable throwable) {
            toastShort(throwable.getMessage());
            ILog.error(TAG, "Failed to open url", throwable);
        }
    }

    public static void toastShort(final String message) {
        AsyncTask.runOnUiThread(() -> Toast
            .makeText(IdeApplication.getGlobalContext(), message, Toast.LENGTH_SHORT).show());
    }

    public static void openUrlOutsideActivity(String url) {
        try {
            var mIntent = new Intent(Intent.ACTION_VIEW);
            mIntent.setData(Uri.parse(url));
            mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            IdeApplication.getGlobalContext().startActivity(mIntent);
        } catch (Throwable throwable) {
            toastLong(throwable.getMessage());
            ILog.error(TAG, "Failed to open url", throwable);
        }
    }

    public static void toastLong(final String message) {
        AsyncTask.runOnUiThread(() -> Toast
            .makeText(IdeApplication.getGlobalContext(), message, Toast.LENGTH_LONG).show());
    }

    /**
     * Register soft input changed listener.
     *
     * @param activity The activity.
     * @param listener The soft input changed listener.
     */
    public static void registerSoftInputChangedListener(@NonNull final Activity activity,
        @NonNull final OnSoftInputChangedListener listener) {
        registerSoftInputChangedListener(activity.getWindow(), listener);
    }

    /**
     * Register soft input changed listener.
     *
     * @param window   The window.
     * @param listener The soft input changed listener.
     */
    public static void registerSoftInputChangedListener(@NonNull final Window window,
        @NonNull final OnSoftInputChangedListener listener) {
        final int flags = window.getAttributes().flags;
        if ((flags & WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS) != 0) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
        final FrameLayout contentView = window.findViewById(android.R.id.content);
        final int[] decorViewInvisibleHeightPre = {getDecorViewInvisibleHeight(window)};
        OnGlobalLayoutListener onGlobalLayoutListener = () -> {
            int height = getDecorViewInvisibleHeight(window);
            if (decorViewInvisibleHeightPre[0] != height) {
                listener.onSoftInputChanged(height);
                decorViewInvisibleHeightPre[0] = height;
            }
        };
        contentView.getViewTreeObserver().addOnGlobalLayoutListener(onGlobalLayoutListener);
        contentView.setTag(TAG_ON_GLOBAL_LAYOUT_LISTENER, onGlobalLayoutListener);
    }

    public static void rotateChevron(boolean isOpen, ImageView chevronView) {
        float startRotation = isOpen ? -90f : 0f;
        float endRotation = isOpen ? 0f : -90f;

        RotateAnimation rotateAnimation = new RotateAnimation(startRotation, endRotation,
            Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotateAnimation.setDuration(200);
        rotateAnimation.setFillAfter(true);
        chevronView.startAnimation(rotateAnimation);
    }

    /**
     * Method to share application link. Sharing the app as a file is not recommended
     */
    public static void shareAppPlayStoreLink() {
        ShareCompat.IntentBuilder shareIntent =
            new ShareCompat.IntentBuilder(IdeApplication.getGlobalContext());
        shareIntent.setType("text/plain");
        String appName = IdeApplication.getGlobalContext().getString(R.string.app_name);
        shareIntent.setChooserTitle(appName);
        shareIntent.setText(IdeApplication.getGlobalContext()
                                          .getString(R.string.share_app_info, appName,
                                              Constants.CHECK_UPDATE_GITHUB_URL));
        shareIntent.getIntent().addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        shareIntent.startChooser();
    }

    /**
     * Method to share application link. Sharing the app as a file is not recommended
     *
     * @param ctx the context where the share menu would appear
     */
    public static void shareAppPlayStoreLink(Context ctx) {
        ShareCompat.IntentBuilder shareIntent = new ShareCompat.IntentBuilder(ctx);
        shareIntent.setType("text/plain");
        String appName = ctx.getString(R.string.app_name);
        shareIntent.setChooserTitle(appName);
        shareIntent.setText(ctx.getString(R.string.share_app_info, appName,
            Constants.CHECK_UPDATE_GITHUB_URL));

        shareIntent.getIntent().addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        shareIntent.startChooser();
    }

    /**
     * Show the soft input.
     *
     * @param view The view.
     */
    public static void showSoftInput(@NonNull final View view) {
        showSoftInput(view, 0);
    }

    /**
     * Show the soft input.
     *
     * @param view  The view.
     * @param flags Provides additional operating flags. Currently may be 0 or have the {@link
     *              InputMethodManager#SHOW_IMPLICIT} bit set.
     */
    public static void showSoftInput(@NonNull final View view, final int flags) {
        var imm = (InputMethodManager) IdeApplication.getInstance()
                                                     .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm == null) {
            return;
        }
        view.setFocusable(true);
        view.setFocusableInTouchMode(true);
        view.requestFocus();
        imm.showSoftInput(view, flags, new ResultReceiver(new Handler()) {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                if (resultCode == InputMethodManager.RESULT_UNCHANGED_HIDDEN
                    || resultCode == InputMethodManager.RESULT_HIDDEN) {
                    toggleSoftInput();
                }
            }
        });
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    /**
     * Toggle the soft input display or not.
     */
    public static void toggleSoftInput() {
        InputMethodManager imm = (InputMethodManager) IdeApplication.getInstance()
                                                                    .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm == null) {
            return;
        }
        imm.toggleSoftInput(0, 0);
    }

    public static void startObjectAnimation(View view, String propertyName, double value,
        double duration) {
        ObjectAnimator anim = new ObjectAnimator();
        anim.setTarget(view);
        anim.setPropertyName(propertyName);
        anim.setFloatValues((float) value);
        anim.setDuration((long) duration);
        anim.start();
    }

    public static void toastLong(final @StringRes int stringRes) {
        AsyncTask.runOnUiThread(() -> Toast
            .makeText(IdeApplication.getGlobalContext(), stringRes, Toast.LENGTH_LONG).show());
    }

    /**
     * Unregister soft input changed listener.
     *
     * @param window The window.
     */
    public static void unregisterSoftInputChangedListener(@NonNull final Window window) {
        final View contentView = window.findViewById(android.R.id.content);
        if (contentView == null) {
            return;
        }
        Object tag = contentView.getTag(TAG_ON_GLOBAL_LAYOUT_LISTENER);
        if (tag instanceof OnGlobalLayoutListener) {
            contentView.getViewTreeObserver()
                       .removeOnGlobalLayoutListener((OnGlobalLayoutListener) tag);
            contentView.setTag(TAG_ON_GLOBAL_LAYOUT_LISTENER, null);
        }
    }

    private void showExitDialog(@NonNull Context context, @NonNull Event<Boolean> event,
        @Nullable Runnable action) {
        if (event.getContentIfNotHandled()) {
            new MaterialAlertDialogBuilder(context).setTitle(R.string.close_app)
                                                   .setMessage(context.getString(R.string.close_app_confirmation, context.getString(R.string.app_name)))
                                                   .setNegativeButton(android.R.string.no, null)
                                                   .setPositiveButton(android.R.string.yes, (d,
                                                       w) -> {
                                                       if (action != null) {
                                                           action.run();
                                                       }
                                                   }).show();
        }
    }

    public interface OnSoftInputChangedListener {
        void onSoftInputChanged(int height);
    }

    public static class SnackBarBuilder {
        private static final MorphMap<String, Integer> colors = MorphMap.of("background_light",
            android.R.color.background_light, "background_dark", android.R.color.background_dark,
            "white", android.R.color.white, "black", android.R.color.black);
        private Context context;
        private String message;
        private View view;
        private View anchorView;
        private int messageMaxLines;
        private DURATION duration = DURATION.SHORT;
        private String actionDescription;
        private View.OnClickListener actionViewOnClickListener;
        private int messageColor, actionTextColor, backgroundTint;

        public void create() {
            Objects.requireNonNull(view, "No view was not set for SnackBar");
            Objects.requireNonNull(message, "Message was not set for SnackBar");

            context = context == null ? view.getContext() : context;
            final Snackbar snackbar = Snackbar.make(context, view, message, duration.get());

            if (actionDescription != null && actionViewOnClickListener == null) {
                snackbar.setAction(actionDescription, null);
            } else if (actionDescription != null) {
                snackbar.setAction(actionDescription, actionViewOnClickListener);
            }

            if (anchorView != null) {
                snackbar.setAnchorView(anchorView);
            }

            if (messageMaxLines != 0) {
                snackbar.setTextMaxLines(messageMaxLines);
            }

            if (messageColor != 0) {
                snackbar.setTextColor(ContextCompat.getColor(context, messageColor));
            }

            if (actionTextColor != 0) {
                snackbar.setActionTextColor(ContextCompat.getColor(context, actionTextColor));
            }

            if (backgroundTint != 0) {
                snackbar.setBackgroundTint(ContextCompat.getColor(context, backgroundTint));
            }

            snackbar.show();
        }

        public SnackBarBuilder setActionClickListener(View.OnClickListener listener) {
            this.actionViewOnClickListener = listener;
            return this;
        }

        public SnackBarBuilder setActionDescription(String description) {
            this.actionDescription = description;
            return this;
        }

        public SnackBarBuilder setActionTextColor(int color) {
            this.actionTextColor = color;
            return this;
        }

        public SnackBarBuilder setAnchorView(View anchor) {
            this.anchorView = anchor;
            return this;
        }

        public SnackBarBuilder setBackgroundTint(int tint) {
            this.backgroundTint = tint;
            return this;
        }

        public SnackBarBuilder setContext(Context context) {
            this.context = context;
            return this;
        }

        public SnackBarBuilder setDuration(DURATION duration) {
            this.duration = duration;
            return this;
        }

        public SnackBarBuilder setMessage(String message) {
            this.message = message;
            return this;
        }

        public SnackBarBuilder setMessageColor(int color) {
            this.messageColor = color;
            return this;
        }

        public SnackBarBuilder setMessageMaxLines(int messageMaxLines) {
            this.messageMaxLines = messageMaxLines;
            return this;
        }

        public SnackBarBuilder setView(View view) {
            this.view = view;
            return this;
        }

        public enum DURATION {
            SHORT(Snackbar.LENGTH_SHORT),
            LONG(Snackbar.LENGTH_LONG),
            INDEFINITE(Snackbar.LENGTH_INDEFINITE);

            private final int duration;

            DURATION(final int duration) {
                this.duration = duration;
            }

            public int get() {
                return this.duration;
            }
        }
    }
}
