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
 
   package com.eup.codeopsstudio.ui.explore;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import com.google.android.material.navigationrail.NavigationRailView;
import com.eup.codeopsstudio.databinding.FragmentPrimarySideBarBinding;
import com.eup.codeopsstudio.res.R;
import com.eup.codeopsstudio.viewmodel.MainViewModel;
import androidx.navigation.NavController;

/**
 * A Fragment subclass responsible for displaying the primary side bar in the Explore section of the
 * app. This fragment provides navigation options and handles user interaction with the navigation
 * rail aka primary side bar.
 *
 * @author EUP
 */
public class PrimarySideBarFragment extends Fragment {

  private FragmentPrimarySideBarBinding binding;
  private MainViewModel mMainViewModel;
  private NavController navController;
  private NavigationRailView navigationRail;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mMainViewModel =
        new ViewModelProvider(requireActivity() /*shared activity scope*/).get(MainViewModel.class);
  }

  @Nullable
  @Override
  public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    binding = FragmentPrimarySideBarBinding.inflate(inflater, container, false);
    return binding.getRoot();
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    navigationRail = binding.navigationRail;
    var host = com.eup.codeopsstudio.R.id.nav_host_primary_side_bar_fragment;
    navController = Navigation.findNavController(requireActivity(), host);

    navigationRail.setOnItemSelectedListener(
        item -> {
          if (navController != null) {
            return onNavDestinationSelected(item.getItemId());
          }
          return false;
        });
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    this.binding = null;
  }

  /**
   * Handles the selection of navigation items in the navigation rail aka Primary side bar.
   *
   * @param itemId The ID of the selected navigation item
   * @return true if the item should be selectable, false otherwise
   */
  private boolean onNavDestinationSelected(final int itemId) {
    if (itemId == R.id.action_file_explorer) {
      return navigateToFragment(R.id.nav_treeviewFragment);
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
