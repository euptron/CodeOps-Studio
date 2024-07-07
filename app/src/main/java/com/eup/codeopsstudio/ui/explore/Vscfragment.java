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
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.eup.codeopsstudio.databinding.FragmentVscBinding;

public class Vscfragment extends Fragment {

  public static final String TAG = Vscfragment.class.getSimpleName();
  private FragmentVscBinding binding;

  public static Vscfragment newInstance() {
    return new Vscfragment();
  }

  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater, ViewGroup viewgroup, Bundle savedInstanceState) {
    binding = FragmentVscBinding.inflate(inflater, viewgroup, false);
    return binding.getRoot();
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    this.binding = null;
  }
}
