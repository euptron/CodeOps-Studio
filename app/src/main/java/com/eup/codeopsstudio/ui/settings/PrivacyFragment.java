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
import com.eup.codeopsstudio.models.user.DeviceInfo;
import com.eup.codeopsstudio.res.R;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceFragmentCompat;
import com.google.android.material.transition.MaterialSharedAxis;
import com.eup.codeopsstudio.util.Wizard;

public class PrivacyFragment extends PreferenceFragmentCompat {

  public static final String TAG = PrivacyFragment.class.getSimpleName();

  private DeviceInfo deviceInfo;

  public static PrivacyFragment newInstance() {
    return new PrivacyFragment();
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    deviceInfo = new DeviceInfo(getContext());

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
    pkg_name.setSummary(deviceInfo.getAppPackageName());
    app_version_code.setSummary(deviceInfo.getAppVersionCode());
    app_version_name.setSummary(deviceInfo.getAppVersionName());
    // system summary
    device_model.setSummary(deviceInfo.getModel());
    device_sdk_version.setSummary(deviceInfo.getSdkVersion());
    device_build_id.setSummary(deviceInfo.getBuildID());
    device_release.setSummary(deviceInfo.getRelease());
    device_board.setSummary(deviceInfo.getBoard());
    device_brand.setSummary(deviceInfo.getBrand());
    device_cpu_arch.setSummary(deviceInfo.getCpuArchitecture());
    device_country.setSummary(deviceInfo.getCountry());
    device_locale.setSummary(
        deviceInfo.getLocale().getLanguage() + deviceInfo.getLocale().getCountry());
  }
}
