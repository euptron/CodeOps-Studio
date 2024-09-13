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

import androidx.preference.Preference;
import com.eup.codeopsstudio.res.R;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceFragmentCompat;
import com.google.android.material.transition.MaterialSharedAxis;
import com.eup.codeopsstudio.util.Wizard;

public class PrivacyFragment extends PreferenceFragmentCompat {

  public static final String TAG = PrivacyFragment.class.getSimpleName();

  public static PrivacyFragment newInstance() {
    return new PrivacyFragment();
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setEnterTransition(new MaterialSharedAxis(MaterialSharedAxis.X, false));
    setExitTransition(new MaterialSharedAxis(MaterialSharedAxis.X, true));
  }

  @Override
  public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
    setPreferencesFromResource(R.xml.privacy_preferences, rootKey);
    // app
    Preference pkg_name = findPreference("pref_pkg_name");
    Preference app_version_code = findPreference("pref_app_version_code");
    Preference app_version_name = findPreference("pref_app_version_name");
    // system
    Preference device_model = findPreference("pref_device_model");
    Preference device_sdk_version = findPreference("pref_sdk_version");
    Preference device_build_id = findPreference("pref_build_id");
    Preference device_release = findPreference("pref_release");
    Preference device_board = findPreference("pref_board");
    Preference device_brand = findPreference("pref_brand");
    Preference device_cpu_arch = findPreference("pref_cpu_arch");
    Preference device_country = findPreference("pref_country");
    Preference device_locale = findPreference("pref_locale");

    // application summary
    pkg_name.setSummary(Wizard.getAppPackageName(requireContext()));
    app_version_code.setSummary(Wizard.getAppVersionCode(requireContext()));
    app_version_name.setSummary(Wizard.getAppVersionName(requireContext()));
    // system summary
    device_model.setSummary(Wizard.getDeviceBuildModel());
    device_sdk_version.setSummary(Wizard.getDeviceSDKVersion());
    device_build_id.setSummary(Wizard.getDeviceBuildID());
    device_release.setSummary(Wizard.getDeviceReleaseVersion());
    device_board.setSummary(Wizard.getDeviceBoard());
    device_brand.setSummary(Wizard.getDeviceManuFacturer());
    device_cpu_arch.setSummary(Wizard.getDeviceArchitecture());
    device_country.setSummary(Wizard.getDeviceCountry(requireContext()));
    device_locale.setSummary(
        Wizard.getDeviceLocaleLanguage() + Wizard.getLocaleCountry(requireContext()));
  }
}
