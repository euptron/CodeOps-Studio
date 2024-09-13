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
 
   package com.eup.codeopsstudio.ui.settings;

import static com.eup.codeopsstudio.common.Constants.SharedPreferenceKeys;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import com.blankj.utilcode.util.ToastUtils;
import com.eup.codeopsstudio.common.Constants;
import com.eup.codeopsstudio.logging.Logger;
import com.eup.codeopsstudio.pane.FragmentPane;
import com.eup.codeopsstudio.res.R;
import com.eup.codeopsstudio.viewmodel.MainViewModel;

public class SettingsPane extends FragmentPane
    implements PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

  private LifecycleOwner cycleOwner;
  private MainViewModel mMainViewModel;
  private Logger logger;

  public SettingsPane(Context context, String title) {
    this(context, title, true);
  }

  public SettingsPane(Context context, String title, boolean generateUUID) {
    super(context, title, generateUUID);
    logger = new Logger(Logger.LogClass.IDE);
  }

  @Override
  public void onViewCreated(View view) {
    super.onViewCreated(view);
    mMainViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
    logger.attach(requireActivity());
    
    // Intercept back button press
    requireActivity()
        .getOnBackPressedDispatcher()
        .addCallback(
            cycleOwner,
            new OnBackPressedCallback(true) {
              @Override
              public void handleOnBackPressed() {
                /**
                 * v0.0.1: Failed to remove only other fragment in the back stack except primary
                 * Fixed in FOSS variant OXIDE v0.0.2: Removes only other fragment in the back stack apart from the primary 
                 */
                int stackCount = requireActivity().getSupportFragmentManager().getBackStackEntryCount();
                if (stackCount > 0 && !isPrimaryNavigation()) {
                  // If there are other fragments in the back stack, allow the default back behavior
                  requireActivity().getSupportFragmentManager().popBackStack();
                } else {
                  ToastUtils.showShort(getString(R.string.alrt_cannot_go_back));
                }
              }
            });
  }
  
  private boolean isPrimaryNavigation() {
    Fragment primaryNavigationFragment = requireActivity().getSupportFragmentManager().getPrimaryNavigationFragment();
    return getFragment().equals(primaryNavigationFragment);
  }
  
  @Override
  public boolean onPreferenceStartFragment(PreferenceFragmentCompat caller, Preference pref) {
    // Instantiate the new Fragment
    final var args = pref.getExtras();
    final Fragment fragment =
        requireActivity()
            .getSupportFragmentManager()
            .getFragmentFactory()
            .instantiate(requireActivity().getClassLoader(), pref.getFragment());
    fragment.setArguments(args);
    // fragment.setTargetFragment(caller, 0);
    final String TAG = fragment.getClass().getSimpleName();
    requireActivity()
        .getSupportFragmentManager()
        .beginTransaction()
        .replace(getContainerId(), fragment, TAG)
        .addToBackStack(null)
        .commit();
    return true;
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    cycleOwner = null;
  }
    
  /**
   * Attaches {@code LifecycleOwner} to this class
   *
   * @param cycleOwner The lifecycle owner
   *     <p>This must always be called before {@link createView}
   */
  public void attach(LifecycleOwner cycleOwner) {
    this.cycleOwner = cycleOwner;
  }
}
