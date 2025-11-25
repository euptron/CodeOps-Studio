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

package com.eup.codeopsstudio.pane;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentTransaction;

import com.eup.codeopsstudio.common.ILog;

/**
 * Represents a pane that hosts a {@link androidx.fragment.app.Fragment} within a {@link
 * com.eup.codeopsstudio.pane.PaneLayout}. This class adeptly manages the fragment's lifecycle and
 * handles its display within the layout.
 *
 * @author Etido Peter
 * @version 1.3
 * @see com.eup.codeopsstudio.pane.PaneLayout
 * @see androidx.fragment.app.Fragment
 * @see com.eup.codeopsstudio.pane
 */
public abstract class FragmentPane extends Pane {

  public static final String TAG = FragmentPane.class.getSimpleName();
  private final int containerId;
  private Fragment fragment;
  private FragmentTransaction fragmentTransaction;

  protected FragmentPane(
      @NonNull Context context, @Nullable String title, @NonNull Fragment fragment) {
    this(context, title, true, fragment);
  }

  protected FragmentPane(
      @NonNull Context context,
      @Nullable String title,
      boolean generateUUID,
      @NonNull Fragment fragment) {
    super(context, title, generateUUID);
    this.fragment = fragment;
    // Generate a unique container ID for each instance of this class
    containerId = View.generateViewId();
  }

  @Override
  public View onCreateView() {
    var container = new FragmentContainerView(requireContext());
    container.setLayoutParams(PaneLayout.FILL_LAYOUT);
    container.setId(containerId);
    return container;
  }

  @Override
  public void onViewCreated(@NonNull View view) {
    super.onViewCreated(view);
    if (fragment != null) {
      final String name = fragment.getClass().getSimpleName();
      final var fragmentManager = requireActivity().getSupportFragmentManager();
      fragmentTransaction = fragmentManager.beginTransaction();
      fragmentTransaction.replace(containerId, fragment, name);
      fragmentTransaction.addToBackStack(null);
      fragmentTransaction.setPrimaryNavigationFragment(fragment);

      if (!fragmentManager.isStateSaved()) {
        fragmentTransaction.commit();
      } else {
        ILog.warning(
            TAG,
            "Activity state already saved. Forcing commit (allowStateLoss) to complete UI restoration.");
        fragmentTransaction.commitAllowingStateLoss();
      }
    }
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    fragment = null;
    fragmentTransaction = null;
  }

  @Override
  public void persist() {
    super.persist();
    ILog.debug(TAG, getTitle() + " persisted");
  }

  public int getContainerId() {
    return this.containerId;
  }

  public Fragment getFragment() {
    return fragment;
  }
}
