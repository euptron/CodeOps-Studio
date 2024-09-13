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
 
   package com.eup.codeopsstudio.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import java.util.ArrayList;
import java.util.List;

public class BuildActionPagerAdapter extends FragmentStateAdapter {
  private List<Fragment> fragmentList = new ArrayList<>();

  public BuildActionPagerAdapter(
      @NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
    super(fragmentManager, lifecycle);
  }

  @Override
  public int getItemCount() {
    return fragmentList.size();
  }

  @Override
  public Fragment createFragment(int position) {
    return fragmentList.get(position);
  }

  public void addFragment(Fragment fragment) {
    fragmentList.add(fragment);
  }
}
