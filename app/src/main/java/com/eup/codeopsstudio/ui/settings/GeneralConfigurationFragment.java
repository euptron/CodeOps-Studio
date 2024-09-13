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
 
   package com.eup.codeopsstudio.ui.settings;

import static com.eup.codeopsstudio.common.Constants.SharedPreferenceKeys;
import android.os.Bundle;
import androidx.preference.Preference;
import com.eup.codeopsstudio.IdeApplication;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;
import com.eup.codeopsstudio.res.R;
import com.eup.codeopsstudio.common.util.PreferencesUtils;
import com.eup.codeopsstudio.common.util.SDKUtil;
import com.eup.codeopsstudio.common.util.SDKUtil.API;

public class GeneralConfigurationFragment extends PreferenceFragmentCompat {

    public static final String TAG = GeneralConfigurationFragment.class.getSimpleName();
  @Override
  public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
    setPreferencesFromResource(R.xml.general_configuration_preferences, rootKey);
    Preference preferenceTheme = findPreference(SharedPreferenceKeys.KEY_APP_THEME);
    SwitchPreferenceCompat switchPreferenceCompat =
        findPreference(SharedPreferenceKeys.KEY_DYNAMIC_COLOURS);

    if (!SDKUtil.isAtLeast(API.ANDROID_12)) {
      switchPreferenceCompat.setEnabled(false);
      switchPreferenceCompat.setSummary(R.string.msg_unsupported_sdk_dynamic_colors);
    }

    preferenceTheme.setOnPreferenceChangeListener(
        (preference, newValue) -> {
          if (newValue instanceof String) {
            int newTheme = PreferencesUtils.getCurrentTheme((String) newValue);
            IdeApplication.getInstance().changeTheme(newTheme);
            return true;
          }
          return false;
        });
  }
}
