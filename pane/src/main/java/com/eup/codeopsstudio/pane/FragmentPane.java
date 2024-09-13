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
 
   package com.eup.codeopsstudio.pane;

import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainer;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentTransaction;

/**
 * Represents a pane that hosts a {@link androidx.fragment.app.Fragment} within a {@link
 * com.eup.codeopsstudio.pane.PaneLayout}. This class adeptly manages the fragment's lifecycle and
 * handles its display within the layout.
 *
 * @see com.eup.codeopsstudio.pane.PaneLayout
 * @see androidx.fragment.app.Fragment
 * @see com.eup.codeopsstudio.pane
 * @author EUP
 * @version 1.3
 */
public class FragmentPane extends Pane {

  private Fragment mFragment;
  private FragmentTransaction mTransaction;
  private final int mContainerId;

  public FragmentPane(Context context, String title) {
    this(context, title,  /* generate new uuid= */true);
  }

  public FragmentPane(Context context, String title, boolean generateUUID) {
    super(context, title, generateUUID);
    // Generate a unique container ID for each instance of this class
    mContainerId = View.generateViewId();
  }

  @Override
  public View onCreateView() {
    FragmentContainerView container = new FragmentContainerView(getContext());
        container.setLayoutParams(new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
    ));
    container.setId(mContainerId);
    return container;
  }

  @Override
  public void onViewCreated(@Nullable View view) {
    super.onViewCreated(view);
    if (mFragment != null) {
      mTransaction = requireActivity().getSupportFragmentManager().beginTransaction();
     final  String TAG = mFragment.getClass().getSimpleName();       
      mTransaction.replace(mContainerId, mFragment,TAG);
      mTransaction.addToBackStack(null);
      mTransaction.setPrimaryNavigationFragment(mFragment);
      mTransaction.commit();
    }
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    mFragment = null;
    mTransaction = null;
  }

  @Override
  public void persist() {
    super.persist();
    // sub classes should call Pane#addArguments(String,Object) to save persist fragment display
    // screen backstack
  }

  /**
   * Sets the Fragment to be hosted within this FragmentPane.
   *
   * @param fragment The Fragment instance to be set.
   */
  public void setFragment(Fragment fragment) {
    this.mFragment = fragment;
  }

  /**
   * Retrieves the Fragment currently hosted within this FragmentPane.
   *
   * @return The hosted Fragment instance.
   */
  public Fragment getFragment() {
    return mFragment;
  }

  public int getContainerId() {
    return this.mContainerId;
  }
}
