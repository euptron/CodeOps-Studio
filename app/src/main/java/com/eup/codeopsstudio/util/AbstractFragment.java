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
 * questions or need additional information. Email: etido.up@gmail.com
 */

package com.eup.codeopsstudio.util;

import android.os.Bundle;
import android.os.SystemClock;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuHost;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.viewbinding.ViewBinding;

import com.eup.codeopsstudio.BuildConfig;
import com.eup.codeopsstudio.common.ILog;

public abstract class AbstractFragment extends Fragment implements MenuProvider {

    private final String LOG_TAG = getClass().getSimpleName();
    protected long fragmentReadyDuration;

    protected long fragmentCreateStartTime;
    private ViewTreeObserver.OnGlobalLayoutListener layoutListener;
    private boolean isViewLaidOut = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fragmentCreateStartTime = millsNow();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Check if view is already laid out (can happen in some cases)
        if (view.getWidth() > 0 && view.getHeight() > 0) {
            onViewLaidOut(view, savedInstanceState);
            return;
        }

        layoutListener = () -> {
            if (!isAdded() || isDetached()) {
                safelyRemoveLayoutListener(view);
                return;
            }

            if (view.getWidth() > 0 && view.getHeight() > 0) {
                safelyRemoveLayoutListener(view);
                onViewLaidOut(view, savedInstanceState);
            }
        };

        view.getViewTreeObserver().addOnGlobalLayoutListener(layoutListener);

        // Add a safety check in case the layout listener doesn't fire
        view.post(() -> {
            if (!isViewLaidOut && view.getWidth() > 0 && view.getHeight() > 0) {
                safelyRemoveLayoutListener(view);
                onViewLaidOut(view, savedInstanceState);
            }
        });
    }

    /**
     * Called after the view hierarchy associated with this fragment has been measured and laid out.
     *
     * <p>It's safe to get view dimensions (getWidth(), getHeight()) or perform other
     * layout-dependent
     * operations within this method.
     *
     * @param view The fragment's root view.
     */
    protected void onViewLaidOut(@NonNull View view, @Nullable Bundle savedInstanceState) {
        isViewLaidOut         = true;
        fragmentReadyDuration = SystemClock.uptimeMillis() - fragmentCreateStartTime;
        String msg = "Layout inflation took: " + fragmentReadyDuration + "ms";
        ILog.debug(LOG_TAG + "Startup", msg);
        if (enabledDebug()) {
            showSnackBar(msg);
        }
    }

    protected Boolean enabledDebug() {
        return BuildConfig.DEBUG;
    }

    public void showSnackBar(@NonNull String message) {
        if (getBinding() == null) return;

        var snackBarBuilder = showSnackBarInternal(message);

        if (snackBarBuilder != null) {
            snackBarBuilder.create();
        } else {
            ILog.debug(LOG_TAG, "BaseUtil.SnackBarBuilder is null");
        }
    }

    protected abstract ViewBinding getBinding();

    public BaseUtil.SnackBarBuilder showSnackBarInternal(@NonNull String message) {
        if (getBinding() == null) {
            ILog.warning(LOG_TAG, "binding is null");
            return null;
        }

        return BaseUtil.newSnackBarBuilder().setMessage(message).setView(getBinding().getRoot())
                       .setMessageMaxLines(6).setDuration(BaseUtil.SnackBarBuilder.DURATION.LONG);
    }

    private void safelyRemoveLayoutListener(View view) {
        if (layoutListener != null) {
            try {
                final ViewTreeObserver vto = view.getViewTreeObserver();
                if (vto.isAlive()) {
                    vto.removeOnGlobalLayoutListener(layoutListener);
                }
            } catch (Exception e) {
                ILog.warning(LOG_TAG, "Error removing layout listener", e);
            } finally {
                layoutListener = null;
            }
        }
    }

    @Override
    public void onDestroyView() {
        isViewLaidOut = false;
        safelyRemoveLayoutListener(getView());
        super.onDestroyView();
    }

    public static long millsNow() {
        return SystemClock.uptimeMillis();
    }

    /**
     * Called by the {@link MenuHost} right before the {@link Menu} is shown.
     * This should be called when the menu has been dynamically updated.
     *
     * @param menu the menu that is to be prepared
     * @see #onCreateMenu(Menu, MenuInflater)
     */
    @Override
    public void onPrepareMenu(@NonNull Menu menu) {
        // No-op
    }

    /**
     * Called by the {@link MenuHost} to allow the {@link MenuProvider}
     * to inflate {@link MenuItem}s into the menu.
     *
     * @param menu         the menu to inflate the new menu items into
     * @param menuInflater the inflater to be used to inflate the updated menu
     */
    @Override
    public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
        // No-op
    }

    /**
     * Called by the {@link MenuHost} when a {@link MenuItem} is selected from the menu.
     *
     * @param menuItem the menu item that was selected
     * @return {@code true} if the given menu item is handled by this menu provider,
     * {@code false} otherwise
     */
    @Override
    public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
        return false;
    }

    /**
     * Called by the {@link MenuHost} when the {@link Menu} is closed.
     *
     * @param menu the menu that was closed
     */
    @Override
    public void onMenuClosed(@NonNull Menu menu) {
        // No-op
    }

    public void showSnackBar(@NonNull String message, @NonNull String actionDescription,
        @NonNull View.OnClickListener actionListener) {
        var snackBarBuilder = showSnackBarInternal(message);

        if (snackBarBuilder != null) {
            snackBarBuilder.setActionDescription(actionDescription);
            snackBarBuilder.setActionClickListener(actionListener);
            snackBarBuilder.create();
        } else {
            ILog.debug(LOG_TAG, "BaseUtil.SnackBarBuilder is null");
        }
    }

    protected void invalidateMenu() {
        requireActivity().invalidateMenu();
    }
}
