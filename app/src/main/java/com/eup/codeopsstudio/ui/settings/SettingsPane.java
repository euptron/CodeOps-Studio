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

package com.eup.codeopsstudio.ui.settings;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.LifecycleOwner;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.eup.codeopsstudio.R;
import com.eup.codeopsstudio.logger.Logger;
import com.eup.codeopsstudio.pane.FragmentPane;
import com.eup.codeopsstudio.util.BaseUtil;

import java.util.Objects;

/**
 * @author Etido Peter
 */
public class SettingsPane extends FragmentPane
    implements PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

  public static final String TAG = SettingsPane.class.getSimpleName();
  private final Logger logger;
  private LifecycleOwner cycleOwner;

  public SettingsPane(Context context, String title, Fragment fragment) {
    this(context, title, true, fragment);
  }

  public SettingsPane(
      @NonNull Context context,
      @Nullable String title,
      boolean generateUUID,
      @NonNull Fragment fragment) {
    super(context, title, generateUUID, fragment);
    logger = new Logger(Logger.LogClass.IDE);
  }

  @Override
  public boolean onPreferenceStartFragment(
      @NonNull PreferenceFragmentCompat caller, @NonNull Preference pref) {
    final Bundle args = pref.getExtras();
    final Fragment fragment;
    if (pref.getFragment() != null) {
      fragment =
          requireActivity()
              .getSupportFragmentManager()
              .getFragmentFactory()
              .instantiate(requireActivity().getClassLoader(), pref.getFragment());
      fragment.setArguments(args);

      final String FRAGMENT_TAG = fragment.getClass().getSimpleName();
      requireActivity()
          .getSupportFragmentManager()
          .beginTransaction()
          .replace(getContainerId(), fragment, FRAGMENT_TAG)
          .addToBackStack(null)
          .commit();
      return true;
    }
    return false;
  }

  @Override
  public void onViewCreated(@NonNull View view) {
    super.onViewCreated(view);
    logger.attach(requireActivity());

    if (cycleOwner == null) {
      logger.e(TAG, "LifecycleOwner not attached. Back press handling disabled.");
    } else {
      var onBackPressedCallback =
          new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
              if (!isSelected()) return;
              FragmentManager fm = requireActivity().getSupportFragmentManager();
              int stackCount = fm.getBackStackEntryCount();

              if (stackCount > 0 && !isPrimaryNavigation()) {
                fm.popBackStack();
              } else {
                BaseUtil.toastShort(R.string.alrt_cannot_go_back);
              }
            }
          };
      requireActivity().getOnBackPressedDispatcher().addCallback(cycleOwner, onBackPressedCallback);
    }
  }

  private boolean isPrimaryNavigation() {
    Fragment currentFragment = getFragment();
    FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
    Fragment primaryFragment = fragmentManager.getPrimaryNavigationFragment();
    return Objects.equals(currentFragment, primaryFragment);
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    cycleOwner = null;
  }

  /**
   * Attaches {@code LifecycleOwner} to this class
   *
   * @param cycleOwner The lifecycle owner
   *     <p>This must always be called before {@link #createView()}
   */
  public void attach(LifecycleOwner cycleOwner) {
    this.cycleOwner = cycleOwner;
  }
}
