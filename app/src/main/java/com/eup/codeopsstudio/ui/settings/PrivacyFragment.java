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

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.eup.codeopsstudio.common.Constants.SharedPreferenceKeys;
import com.eup.codeopsstudio.models.user.DeviceInfo;
import com.eup.codeopsstudio.res.R;
import com.google.android.material.transition.MaterialSharedAxis;

/**
 * @author Etido Peter
 */
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
        var deviceInfo = new DeviceInfo(requireContext());
        // app
        Preference prefPkgName = findPreference(SharedPreferenceKeys.KEY_PACKAGE_NAME);
        Preference prefAppVersionCode = findPreference(SharedPreferenceKeys.KEY_VERSION_CODE);
        Preference prefAppVersionName = findPreference(SharedPreferenceKeys.KEY_VERSION_NAME);
        // system
        Preference prefDeviceModel = findPreference(SharedPreferenceKeys.KEY_DEVICE_MODEL);
        Preference prefSdkVersion = findPreference(SharedPreferenceKeys.KEY_SDK_VERSION);
        Preference prefBuildId = findPreference(SharedPreferenceKeys.KEY_BUILD_ID);
        Preference prefRelease = findPreference(SharedPreferenceKeys.KEY_RELEASE);
        Preference prefBoard = findPreference(SharedPreferenceKeys.KEY_DEVICE_BOARD);
        Preference prefBrand = findPreference(SharedPreferenceKeys.KEY_DEVICE_BRAND);
        Preference prefCpuArch = findPreference(SharedPreferenceKeys.KEY_CPU_ARCH);
        Preference prefCountry = findPreference(SharedPreferenceKeys.KEY_DEVICE_COUNTRY);
        Preference prefLocale = findPreference(SharedPreferenceKeys.KEY_LOCALE);

        assert prefPkgName != null;
        assert prefAppVersionCode != null;
        assert prefAppVersionName != null;
        assert prefDeviceModel != null;
        assert prefSdkVersion != null;
        assert prefBuildId != null;
        assert prefRelease != null;
        assert prefBoard != null;
        assert prefBrand != null;
        assert prefCpuArch != null;
        assert prefCountry != null;
        assert prefLocale != null;

        // application summary
        prefPkgName.setSummary(deviceInfo.getAppPackageName());
        prefAppVersionCode.setSummary(deviceInfo.getAppVersionCode());
        prefAppVersionName.setSummary(deviceInfo.getAppVersionName());
        // system summary
        prefDeviceModel.setSummary(deviceInfo.getModel());
        prefSdkVersion.setSummary(deviceInfo.getSdkVersion());
        prefBuildId.setSummary(deviceInfo.getBuildID());
        prefRelease.setSummary(deviceInfo.getRelease());
        prefBoard.setSummary(deviceInfo.getBoard());
        prefBrand.setSummary(deviceInfo.getBrand());
        prefCpuArch.setSummary(deviceInfo.getCpuArchitecture());
        prefCountry.setSummary(deviceInfo.getCountry());
        prefLocale.setSummary(deviceInfo.getLocaleLanguage() + deviceInfo.getLocaleCountry());

        enableCopying(prefPkgName, prefAppVersionName, prefAppVersionCode, prefAppVersionName,
            prefDeviceModel, prefSdkVersion, prefBuildId, prefRelease, prefBoard, prefBrand,
            prefCpuArch, prefCountry, prefLocale);
    }

    private void enableCopying(@NonNull Preference... preferences) {
        for (var pref : preferences) {
            if (pref == null) continue;
            pref.setCopyingEnabled(true);
        }
    }
}
