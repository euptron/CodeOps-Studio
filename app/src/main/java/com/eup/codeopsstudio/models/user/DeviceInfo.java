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

import android.content.Context;

import androidx.annotation.NonNull;

import com.eup.codeopsstudio.util.Wizard;

import java.util.Locale;
import java.util.Objects;

/**
 * Model representing the device info required for analytical purposes
 *
 * @author Etido Peter
 */
public class DeviceInfo {

    @NonNull
    private final String appVersionName;
    @NonNull
    private final String appPackageName;
    @NonNull
    private final String appVersionCode;
    private final int appVersionCodeInt;
    @NonNull
    private final String model;
    @NonNull
    private final String sdkVersion;
    @NonNull
    private final String buildID;
    @NonNull
    private final String release;
    @NonNull
    private final String board;
    @NonNull
    private final String brand;
    @NonNull
    private final String cpuArchitecture;
    @NonNull
    private final String country;
    @NonNull
    private final String localeLanguage;
    @NonNull
    private final String localeCountry;

    /**
     * Locale based on current device details.
     * <p>
     * This differs from the {@link User#getPreferredLocale()}
     */
    @NonNull
    private final Locale locale;

    public DeviceInfo(@NonNull Context context) {
        Objects.requireNonNull(context, "Context must not be null");

        this.appPackageName    = definite(context.getPackageName());
        this.appVersionCode    = definite(Wizard.getAppVersionCode(context));
        this.appVersionCodeInt = Wizard.getAppVersionCodeInteger(context);
        this.appVersionName    = definite(Wizard.getAppVersionName(context));
        this.model             = definite(Wizard.getDeviceBuildModel());
        this.sdkVersion        = definite(Wizard.getDeviceSDKVersion());
        this.buildID           = definite(Wizard.getDeviceBuildID());
        this.release           = definite(Wizard.getDeviceReleaseVersion());
        this.board             = definite(Wizard.getDeviceBoard());
        this.brand             = definite(Wizard.getDeviceManufacturer());
        this.cpuArchitecture   = definite(Wizard.getDeviceArchitecture());
        this.country           = definite(Wizard.getDeviceCountry(context));
        this.localeLanguage    = definite(Wizard.getDeviceLocaleLanguage());
        this.localeCountry     = definite(Wizard.getLocaleCountry(context));
        this.locale            = new Locale(localeLanguage);
    }

    @NonNull
    private String definite(String str) {
        return Wizard.validate(str);
    }

    @NonNull
    public String getAppPackageName() {
        return this.appPackageName;
    }

    @NonNull
    public String getAppVersionCode() {
        return this.appVersionCode;
    }

    public int getAppVersionCodeInt() {
        return this.appVersionCodeInt;
    }

    @NonNull
    public String getAppVersionName() {
        return this.appVersionName;
    }

    @NonNull
    public String getBoard() {
        return this.board;
    }

    @NonNull
    public String getBrand() {
        return this.brand;
    }

    @NonNull
    public String getBuildID() {
        return this.buildID;
    }

    @NonNull
    public String getCountry() {
        return this.country;
    }

    @NonNull
    public String getCpuArchitecture() {
        return this.cpuArchitecture;
    }

    @NonNull
    public Locale getLocale() {
        return this.locale;
    }

    @NonNull
    public String getLocaleCountry() {
        return this.localeCountry;
    }

    @NonNull
    public String getLocaleLanguage() {
        return this.localeLanguage;
    }

    @NonNull
    public String getModel() {
        return this.model;
    }

    @NonNull
    public String getRelease() {
        return this.release;
    }

    @NonNull
    public String getSdkVersion() {
        return this.sdkVersion;
    }
}
