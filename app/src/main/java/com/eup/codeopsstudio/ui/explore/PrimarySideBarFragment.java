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

package com.eup.codeopsstudio.ui.explore;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import com.eup.codeopsstudio.BuildConfig;
import com.eup.codeopsstudio.R;
import com.eup.codeopsstudio.databinding.FragmentPrimarySideBarBinding;
import com.eup.codeopsstudio.util.BaseUtil;
import com.eup.codeopsstudio.util.ThemeExporter;
import com.eup.codeopsstudio.viewmodel.MainViewModel;

/**
 * A Fragment subclass responsible for displaying the primary side bar in the Explore section of the
 * app. This fragment provides navigation options and handles user interaction with the navigation
 * rail aka primary side bar.
 *
 * @author Etido Peter
 */
public class PrimarySideBarFragment extends Fragment {

  private FragmentPrimarySideBarBinding binding;
  private MainViewModel mMainViewModel;
  private NavController navController;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mMainViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
  }

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    binding = FragmentPrimarySideBarBinding.inflate(inflater, container, false);
    return binding.getRoot();
  }

  @Override
  public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    applySystemInsets(binding.fragmentContainer);
    int host = R.id.nav_host_primary_side_bar_fragment;
    navController = Navigation.findNavController(requireActivity(), host);
    binding.navigationRail.setOnItemSelectedListener(this::onNavDestinationSelected);

    binding
        .navigationRail
        .getHeaderView()
        .setOnClickListener(
            headerView -> {
              if (BuildConfig.DEBUG) {
                ThemeExporter.exportThemeToXML(requireActivity());
              }
            });
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    this.binding = null;
  }

  private void applySystemInsets(@NonNull View view) {
    int systemBars = WindowInsetsCompat.Type.systemBars();
    BaseUtil.applyWindowInsetToPadding(view, false, true, false, true, systemBars);
  }

  /**
   * Handles the selection of navigation items in the navigation rail aka Primary side bar.
   *
   * @param itemId The ID of the selected navigation item
   * @return true if the item should be selectable, false otherwise
   */
  private boolean onNavDestinationSelected(MenuItem item) {
    if (navController == null || item == null) return false;
    final int itemId = item.getItemId();

    if (itemId == R.id.action_file_explorer) {
      return navigateToFragment(com.eup.codeopsstudio.R.id.nav_treeviewFragment);
    } else if (itemId == R.id.action_plugin) {
      return navigateToFragment(com.eup.codeopsstudio.R.id.nav_pluginFragment);
    } else if (itemId == R.id.action_settings) {
      mMainViewModel.addSettingsPane(true);
      return false;
    } else if (itemId == R.id.action_close_app) {
      requireActivity().finishAffinity();
      return false;
    }
    return false;
  }

  /**
   * Navigates to menu associated with a fragment
   *
   * @return true Since fragments must be made selectable
   */
  private boolean navigateToFragment(int fragmentId) {
    if (navController.getCurrentDestination() != null
        && navController.getCurrentDestination().getId() != fragmentId) {
      navController.navigate(fragmentId); // Assuming the fragment is not displayed we add it
    }
    return true;
  }
}
