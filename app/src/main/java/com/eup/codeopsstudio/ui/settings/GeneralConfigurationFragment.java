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

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import com.eup.codeopsstudio.common.Constants;
import com.eup.codeopsstudio.common.ILog;
import com.eup.codeopsstudio.res.R;
import com.eup.codeopsstudio.util.manager.ThemeManager;
import com.google.android.material.color.DynamicColors;
import com.google.android.material.transition.MaterialSharedAxis;

public class GeneralConfigurationFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

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
        SwitchPreferenceCompat switchPreference = findPreference(ThemeManager.KEY_DYNAMIC_COLORS);

        if (switchPreference == null) {
            ILog.debug(TAG, "Dynamic switch preference is null");
        } else {
            if (!DynamicColors.isDynamicColorAvailable()) {
                switchPreference.setEnabled(false);
                switchPreference.setSummary(R.string.msg_unsupported_sdk_dynamic_colors);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences pref = getPreferenceManager().getSharedPreferences();

        if (pref != null) {
            pref.registerOnSharedPreferenceChangeListener(this);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        SharedPreferences pref = getPreferenceManager().getSharedPreferences();

        if (pref != null) {
            pref.unregisterOnSharedPreferenceChangeListener(this);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
        @Nullable String key) {
        if (key != null) {
            switch (key) {
                case ThemeManager.KEY_THEME:
                    // fall-through
                case ThemeManager.KEY_DYNAMIC_COLORS:
                    ThemeManager.applyTheme(requireActivity().getApplication());
                    requireActivity().recreate();
                    break;
                case Constants.SharedPreferenceKeys.KEY_SHOW_WELCOME_PANE:
                    boolean checked = sharedPreferences.getBoolean(key, true);
                    syncSwitch(findPreference(Constants.SharedPreferenceKeys.KEY_SHOW_WELCOME_PANE), checked);
                    break;
            }
        }
    }

    private void syncSwitch(SwitchPreferenceCompat switchPreference, boolean checked) {
        if (switchPreference == null) {
            ILog.debug(TAG, "Switch preference is null");
        } else {
            switchPreference.setChecked(checked);
        }
    }
}
