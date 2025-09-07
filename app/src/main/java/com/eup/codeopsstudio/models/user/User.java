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

package com.eup.codeopsstudio.models.user;

import android.os.Bundle;

import androidx.annotation.NonNull;

import com.eup.codeopsstudio.IdeApplication;
import com.eup.codeopsstudio.common.Constants;
import com.eup.codeopsstudio.domain.FormatDateUseCase;
import com.eup.codeopsstudio.util.Wizard;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.Locale;

public class User {

    private final String uniqueID;
    private final String firstLaunchTimeID;
    private final DeviceInfo deviceInfo;
    private final Locale preferredLocale;
    private final String preferredDateFormat;

    private User() {
        throw new UnsupportedOperationException("User must be created through the factory method.");
    }

    public User(String uniqueID, String firstLaunchTimeID, DeviceInfo deviceInfo,
        Locale preferredLocale, String preferredDateFormat) {
        this.uniqueID            = uniqueID;
        this.firstLaunchTimeID   = firstLaunchTimeID;
        this.deviceInfo          = deviceInfo;
        this.preferredLocale     = preferredLocale;
        this.preferredDateFormat = preferredDateFormat;
    }

    public static void registerSession() {
        var analytics = IdeApplication.getAnalytics();
        var context = IdeApplication.getGlobalContext();
        var deviceInfo = new DeviceInfo(context);
        var uniqueID = Wizard.getUserID(context);
        var bundle = new Bundle();
        var user = newInstance();

        analytics.setAnalyticsCollectionEnabled(true);
        analytics.logEvent(FirebaseAnalytics.Event.LOGIN, null);
        analytics.setUserId(uniqueID);

        bundle.putString("package_name", deviceInfo.getAppPackageName());
        bundle.putString("app_version_code", deviceInfo.getAppVersionCode());
        bundle.putString("app_version_name", deviceInfo.getAppVersionName());
        bundle.putString("device_model", deviceInfo.getModel());
        bundle.putString("device_board", deviceInfo.getBoard());
        bundle.putString("device_build_id", deviceInfo.getBuildID());
        bundle.putString("device_sdk_version", deviceInfo.getSdkVersion());
        bundle.putString("device_manufacturer", deviceInfo.getBrand());
        bundle.putString("device_release_version", deviceInfo.getRelease());
        bundle.putString("device_cpu_architecture", deviceInfo.getCpuArchitecture());
        bundle.putString("device_locale_country", deviceInfo.getLocaleCountry());
        bundle.putString("device_locale_language", deviceInfo.getLocaleLanguage());
        bundle.putString("device_country", deviceInfo.getCountry());
        bundle.putString("session_date", new FormatDateUseCase(user).format(Wizard.getTime()));
        analytics.logEvent("session_begin", bundle);
    }

    @NonNull
    public static User newInstance() {
        var context = IdeApplication.getGlobalContext();
        var info = new DeviceInfo(context);
        var uniqueID = Wizard.getUserID(context);
        var userPreferredLocale = new Locale(info.getLocaleLanguage(), info.getLocaleLanguage());
        return new User(uniqueID, Wizard.getInstallUserID(context), info, userPreferredLocale,
            Constants.DEFAULT_DATE_FORMAT);
    }

    @NonNull
    public static User newInstance(@NonNull String dateFormat) {
        var context = IdeApplication.getGlobalContext();
        var info = new DeviceInfo(context);
        var uniqueID = Wizard.getUserID(context);
        var userPreferredLocale = new Locale(info.getLocaleLanguage(), info.getLocaleLanguage());

        return new User(uniqueID, Wizard.getInstallUserID(context), info, userPreferredLocale,
            dateFormat);
    }

    public String getUniqueID() {
        return this.uniqueID;
    }

    public String getFirstLaunchTimeID() {
        return this.firstLaunchTimeID;
    }

    public DeviceInfo getDeviceInfo() {
        return this.deviceInfo;
    }

    /**
     * TODO: implement multi-language functionality.
     * <p> This locale should be initialized with the selected human language
     *
     * @return the preferred locale of the e.g new Locale("en", "NG")
     */
    public Locale getPreferredLocale() {
        return this.preferredLocale;
    }

    public String getPreferredDateFormat() {
        return this.preferredDateFormat;
    }

    @NonNull
    @Override
    public String toString() {
        return "User[uniqueID=" + uniqueID + ", firstLaunchTimeID=" + firstLaunchTimeID + ", "
            + "deviceInfo=" + deviceInfo + ", preferredLocale=" + preferredLocale + ", "
            + "preferredDateFormat=" + preferredDateFormat + "]";
    }
}
