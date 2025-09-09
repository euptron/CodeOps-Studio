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

import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import com.eup.codeopsstudio.IdeApplication;
import com.eup.codeopsstudio.common.Constants;
import com.eup.codeopsstudio.common.ILog;
import com.eup.codeopsstudio.common.util.PreferencesUtils;
import com.eup.codeopsstudio.res.R;
import com.google.android.material.transition.MaterialSharedAxis;

public class GeneralConfigurationFragment extends PreferenceFragmentCompat {

    public static final String TAG = GeneralConfigurationFragment.class.getSimpleName();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setEnterTransition(new MaterialSharedAxis(MaterialSharedAxis.X, false));
        setExitTransition(new MaterialSharedAxis(MaterialSharedAxis.X, true));
    }

    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
        setPreferencesFromResource(R.xml.general_configuration_preferences, rootKey);
        Preference themePreference = findPreference(Constants.SharedPreferenceKeys.KEY_APP_THEME);
        SwitchPreferenceCompat switchPreference =
            findPreference(Constants.SharedPreferenceKeys.KEY_DYNAMIC_COLOURS);

        if (themePreference == null || switchPreference == null) {
            ILog.debug(TAG, "themePreference or switchPreference == null");
            return;
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            switchPreference.setEnabled(false);
            switchPreference.setSummary(R.string.msg_unsupported_sdk_dynamic_colors);
        }

        themePreference.setOnPreferenceChangeListener((preference, newValue) -> {
            if (newValue instanceof String val) {
                int newTheme = PreferencesUtils.getCurrentTheme(val);
                IdeApplication.changeTheme(newTheme);
                return true;
            }
            return false;
        });
    }
}
