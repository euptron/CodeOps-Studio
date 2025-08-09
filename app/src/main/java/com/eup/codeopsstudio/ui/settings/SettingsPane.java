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

package com.eup.codeopsstudio.ui.settings;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.eup.codeopsstudio.models.logger.Logger;
import com.eup.codeopsstudio.pane.FragmentPane;
import com.eup.codeopsstudio.res.R;
import com.eup.codeopsstudio.util.BaseUtil;
import com.eup.codeopsstudio.viewmodel.MainViewModel;

import java.util.Objects;

public class SettingsPane extends FragmentPane implements PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

    public static final String TAG = SettingsPane.class.getSimpleName();
    private final Logger logger;
    private LifecycleOwner cycleOwner;
    private MainViewModel mMainViewModel;

    public SettingsPane(Context context, String title, Fragment fragment) {
        this(context, title, true, fragment);
    }

    public SettingsPane(@NonNull Context context, @Nullable String title, boolean generateUUID,
        @NonNull Fragment fragment) {
        super(context, title, generateUUID, fragment);
        logger = new Logger(Logger.LogClass.IDE);
    }

    @Override
    public void onViewCreated(@NonNull View view) {
        super.onViewCreated(view);
        mMainViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        logger.attach(requireActivity());

        if (cycleOwner != null) {
            requireActivity()
                .getOnBackPressedDispatcher()
                .addCallback(cycleOwner, new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        /**
                         * Fixes fragment back stack handling (variant: OXIDE: - v0.0.1: Primary
                         * fragment
                         * was incorrectly removed. - v0.0.2: Fixed by preserving primary
                         * fragment during
                         * back stack removal.
                         */
                        int stackCount = requireActivity()
                            .getSupportFragmentManager()
                            .getBackStackEntryCount();
                        if (stackCount > 0 && !isPrimaryNavigation()) {
                            requireActivity()
                                .getSupportFragmentManager()
                                .popBackStack();
                        } else {
                            BaseUtil.toastShort(R.string.alrt_cannot_go_back);
                        }
                    }
                });
        } else {
            logger.e(TAG, "LifecycleOwner not attached. Back press handling disabled.");
        }
    }

    private boolean isPrimaryNavigation() {
        Fragment currentFragment = getFragment();
        Fragment primaryFragment = requireActivity()
            .getSupportFragmentManager()
            .getPrimaryNavigationFragment();

        return Objects.equals(currentFragment, primaryFragment);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        cycleOwner = null;
    }

    @Override
    public boolean onPreferenceStartFragment(PreferenceFragmentCompat caller, Preference pref) {
        final Bundle args = pref.getExtras();
        final Fragment fragment = requireActivity()
            .getSupportFragmentManager()
            .getFragmentFactory()
            .instantiate(requireActivity().getClassLoader(), pref.getFragment());
        fragment.setArguments(args);
        final String FRAGMENT_TAG = fragment
            .getClass()
            .getSimpleName();
        requireActivity()
            .getSupportFragmentManager()
            .beginTransaction()
            .replace(getContainerId(), fragment, FRAGMENT_TAG)
            .addToBackStack(null)
            .commit();
        return true;
    }

    /**
     * Attaches {@code LifecycleOwner} to this class
     *
     * @param cycleOwner The lifecycle owner
     *                   <p>This must always be called before {@link #createView()}
     */
    public void attach(LifecycleOwner cycleOwner) {
        this.cycleOwner = cycleOwner;
    }
}
