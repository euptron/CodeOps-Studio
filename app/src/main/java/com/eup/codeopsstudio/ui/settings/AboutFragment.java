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

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.eup.codeopsstudio.R;
import com.eup.codeopsstudio.common.Constants;
import com.eup.codeopsstudio.databinding.FragmentAboutBinding;
import com.eup.codeopsstudio.ui.settings.api.AboutAdapter;
import com.eup.codeopsstudio.util.BaseUtil;
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.transition.MaterialSharedAxis;

/**
 * Activity component responsible for the about screen.
 *
 * @author Etido Peter
 */
public class AboutFragment extends Fragment {
    public static final String TAG = AboutFragment.class.getSimpleName();
    private FragmentAboutBinding binding;

    public static AboutFragment newInstance() {
        return new AboutFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setEnterTransition(new MaterialSharedAxis(MaterialSharedAxis.X, false));
        setExitTransition(new MaterialSharedAxis(MaterialSharedAxis.X, true));
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup viewgroup,
        Bundle savedInstanceState) {
        binding = FragmentAboutBinding.inflate(inflater, viewgroup, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        var adapter = new AboutAdapter(requireContext());

        binding.recyclerview.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerview.setHasFixedSize(true);
        binding.recyclerview.setAdapter(adapter);

        adapter.setOnItemClickListener((items, pos) -> {
            if (pos == AboutAdapter.VISIT_WEBSITE) {
                BaseUtil.openUrlOutsideActivity(Constants.WEBSITE_URL);
            } else if (pos == AboutAdapter.SOCIALS) {
                CharSequence[] choices = getResources().getStringArray(R.array.social_handles);

                new MaterialAlertDialogBuilder(requireContext())
                    .setItems(choices, (dialog, which) -> {
                        if (which == 0) {
                            BaseUtil.openUrlOutsideActivity(Constants.TELEGRAM);
                        } else if (which == 1) {
                            BaseUtil.openUrlOutsideActivity(Constants.X);
                        } else if (which == 2) {
                            BaseUtil.openUrlOutsideActivity(Constants.FACEBOOK_URL);
                        }
                        dialog.dismiss();
                    })
                    .setCancelable(true)
                    .show();
            } else if (pos == AboutAdapter.OPEN_SOURCE_LICENCES) {
                OssLicensesMenuActivity.setActivityTitle(getString(R.string.open_source_licences));
                startActivity(new Intent(requireContext(), OssLicensesMenuActivity.class));
            } else if (pos == AboutAdapter.CHECK_UPDATE) {
                BaseUtil.openUrlOutsideActivity(Constants.CHECK_UPDATE_GITHUB_URL);
            } else if (pos == AboutAdapter.DOCUMENTATION) {
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
