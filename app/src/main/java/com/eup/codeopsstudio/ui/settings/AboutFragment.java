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
 
   package com.eup.codeopsstudio.ui.settings;

import android.content.Intent;
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity;
import static com.eup.codeopsstudio.ui.settings.api.AboutAdapter.CHECK_UPDATE;
import static com.eup.codeopsstudio.ui.settings.api.AboutAdapter.DOCUMENTATION;
import static com.eup.codeopsstudio.ui.settings.api.AboutAdapter.SOCIALS;
import static com.eup.codeopsstudio.ui.settings.api.AboutAdapter.VISIT_WEBSITE;
import static com.eup.codeopsstudio.ui.settings.api.AboutAdapter.OPEN_SOURCE_LICENCES;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.eup.codeopsstudio.common.Constants;
import com.eup.codeopsstudio.databinding.FragmentAboutBinding;
import com.eup.codeopsstudio.res.R;
import com.eup.codeopsstudio.ui.settings.api.AboutAdapter;
import com.eup.codeopsstudio.util.BaseUtil;

public class AboutFragment extends Fragment {
  public static final String TAG = AboutFragment.class.getSimpleName();
  private FragmentAboutBinding binding;

  public static AboutFragment newInstance() {
    return new AboutFragment();
  }

  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater, ViewGroup viewgroup, Bundle savedInstanceState) {
    binding = FragmentAboutBinding.inflate(inflater, viewgroup, false);
    return binding.getRoot();
  }
  
   @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    var adapter = new AboutAdapter(requireActivity());
    binding.recyclerview.setAdapter(adapter);

    adapter.setOnItemClickListener(
        (items, pos) -> {
          // TODO: Show animation screen based @ app version...sim to android API ver
          if (pos == VISIT_WEBSITE) {
            BaseUtil.openUrlOutsideActivity(Constants.WEBSITE_URL);
          } else if (pos == SOCIALS) {
            // TODO: Show social handler dialog
            CharSequence[] choices = getResources().getStringArray(R.array.social_handles);
            CharSequence[] choicesIndex =
                getResources().getStringArray(R.array.social_handles_index);
            new MaterialAlertDialogBuilder(getContext())
                .setItems(
                    choices,
                    (dialog, which) -> {
                      if (which == 0) {
                        BaseUtil.openUrlOutsideActivity(Constants.Telegram);
                      } else if (which == 1) {
                        BaseUtil.openUrlOutsideActivity(Constants.X);
                      }
                      dialog.dismiss();
                    })
                .setCancelable(true)
                .show();
          } else if (pos == OPEN_SOURCE_LICENCES) {
            // When the user selects an option to see the licenses:
            startActivity(new Intent(requireContext(), OssLicensesMenuActivity.class));
            OssLicensesMenuActivity.setActivityTitle(getString(R.string.open_source_licences));
          } else if (pos == CHECK_UPDATE) {
            BaseUtil.openUrlOutsideActivity(Constants.CHECK_UPDATE_URL);
          } else if (pos == DOCUMENTATION) {
            BaseUtil.openUrlOutsideActivity(Constants.DOCUMENTATION_URL);
          }
        });
        
  }
  
  @Override
  public void onDestroyView() {
    super.onDestroyView();
    this.binding = null;
  }
}
