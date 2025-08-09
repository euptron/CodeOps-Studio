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
package com.eup.codeopsstudio.util;

import static android.content.Context.UI_MODE_SERVICE;
import static com.eup.codeopsstudio.common.util.SDKUtil.API;

import android.app.UiModeManager;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.webkit.MimeTypeMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import com.eup.codeopsstudio.BuildConfig;
import com.eup.codeopsstudio.common.AsyncTask;
import com.eup.codeopsstudio.common.ILog;
import com.eup.codeopsstudio.common.util.SDKUtil;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Main-Stream Utility class
 *
 * <p>The wizard of OZ /*
 *
 * @author Etido Peter
 */
public class Wizard {
    public static final String LOG_TAG = Wizard.class.getSimpleName();
    private static final String PREF_UNIQUE_ID = "PREF_UNIQUE_ID";
    private static String uniqueID = null;
    protected Context mContext;

    public Wizard(Context context) {
        mContext = context;
    }

    public static synchronized String getUserID(Context context) {
        return getInstallUserID(context);
    }

    /**
     * Gets the unique id given to devices at first launch time
     *
     * @return the pesudo ID
     */
    public static synchronized String getInstallUserID(Context context) {
        if (uniqueID == null) {
            SharedPreferences sharedPrefs = context.getSharedPreferences(PREF_UNIQUE_ID,
                Context.MODE_PRIVATE);
            uniqueID = sharedPrefs.getString(PREF_UNIQUE_ID, null);
            if (uniqueID == null) {
                uniqueID = "user" + UUID
                    .randomUUID()
                    .toString() + ":" + getDeviceBuildID();
                Editor editor = sharedPrefs.edit();
                editor.putString(PREF_UNIQUE_ID, uniqueID);
                editor.apply();
            }
        }
        return uniqueID;
    }

    public static String getDeviceBuildID() {
        return validate(Build.ID);
    }

    @NonNull
    public static String validate(String str) {
        return validate(str, "unavailable");
    }

    public static String validate(String str, String fallback) {
        if (str == null || str
            .trim()
            .isEmpty() || str.equalsIgnoreCase("")) {
            return fallback == null ? "" : fallback;
        } else {
            return str;
        }
    }

    public static long getTime() {
        return new Date().getTime();
    }

    public static String getDeviceCountry(Context context) {
        return getLocaleCountry(context);
    }

    public static String getLocaleCountry(Context context) {
        String param = null;
        TelephonyManager tm =
            (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (tm != null) {
            if (!isEmpty(tm.getSimCountryIso())) {
                param = tm.getSimCountryIso();
            } else if (!isEmpty(tm.getNetworkCountryIso())) {
                param = tm.getNetworkCountryIso();
            }
            if (!isEmpty(param) && param.length() == 2) {
                return validate(param.toUpperCase());
            }
            Configuration configuration = context
                .getResources()
                .getConfiguration();
            Locale locale;
            if (SDKUtil.isAtLeast(SDKUtil.API.ANDROID_7)) {
                locale = configuration
                    .getLocales()
                    .get(0);
            } else {
                locale = configuration.locale;
            }
            return validate(locale.getCountry());
        }
        return validate(null);
    }

    public static boolean isEmpty(String str) {
        return str == null || str
            .trim()
            .isEmpty() || str.equalsIgnoreCase("");
    }

    public static String getDeviceBuildModel() {
        return validate(Build.MODEL);
    }

    public static String getDeviceSDKVersion() {
        return validate(Build.VERSION.SDK);
    }

    public static String getDeviceReleaseVersion() {
        return validate(Build.VERSION.RELEASE);
    }

    public static String getDeviceBoard() {
        return validate(Build.BOARD);
    }

    public static String getDeviceManufacturer() {
        return validate(Build.MANUFACTURER);
    }

    /**
     * Retrieve the preferred ABI of the device. Some devices can support multiple ABIs and the
     * first
     * one returned in the preferred one.
     *
     * <p>Suppressed deprecation warning because that code path is only used below Lollipop.
     *
     * @return The preferred ABI of the device
     */
    @SuppressWarnings("deprecation")
    public static String getDeviceArchitecture() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return validate(Build.SUPPORTED_ABIS[0]);
        }
        return validate(Build.CPU_ABI);
    }

    @NonNull
    public static String getDeviceLocaleLanguage() {
        var param = Locale
            .getDefault()
            .getLanguage();
        return validate(param);
    }

    public static String toUpperCase(String inputString) {
        String result = "";
        for (int i = 0; i < inputString.length(); i++) {
            char currentChar = inputString.charAt(i);
            char currentCharToUpperCase = Character.toUpperCase(currentChar);
            result = result + currentCharToUpperCase;
        }
        return result;
    }

    public static String toLowerCase(String inputString) {
        String result = "";
        for (int i = 0; i < inputString.length(); i++) {
            char currentChar = inputString.charAt(i);
            char currentCharToLowerCase = Character.toLowerCase(currentChar);
            result = result + currentCharToLowerCase;
        }
        return result;
    }

    public static boolean isEmpty(@NonNull char[] chars) {
        return chars.length == 0;
    }

    public static String getFilePathOrEmpty(File file) {
        if (file != null) {
            return validate(file.getAbsolutePath(), null);
        }
        return "";
    }

    public static String getAppVersionName(Context context) {
        PackageManager packageManager = context.getPackageManager();
        PackageInfo packageInfo;
        try {
            packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            return validate(packageInfo.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            ILog.error(LOG_TAG, "Failed to get application version name", e);
            return validate(null);
        }
    }

    public static String getAppVersionCode(Context context) {
        return validate(String.valueOf(getAppVersionCodeInteger(context)));
    }

    public static int getAppVersionCodeInteger(Context context) {
        PackageManager packageManager = context.getPackageManager();
        PackageInfo packageInfo;
        try {
            packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            ILog.error(LOG_TAG, "Failed to get application version code as integer", e);
            return -1;
        }
    }

    public static String getAppPackageName(Context context) {
        return validate(context.getPackageName());
    }

    public static String getAppName(Context context) {
        PackageManager packageManager = context.getPackageManager();
        PackageInfo packageInfo;
        try {
            packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            var param = (String) packageInfo.applicationInfo.loadLabel(packageManager);
            return validate(param);
        } catch (PackageManager.NameNotFoundException e) {
            ILog.error(LOG_TAG, "Failed to get application name", e);
            return validate(null);
        }
    }

    public static String prettyPrintJson(String jsonString) {
        CompletableFuture<String> resultFuture = new CompletableFuture<>();
        prettyPrintJsonAsync(jsonString, (result) -> {
            if (result != null) {
                resultFuture.complete(result);
            }
        });
        try {
            // Wait for the result and return it
            return resultFuture.get();
        } catch (Exception e) {
            ILog.error("EditorManager.JsonBuilder",
                "Error occurred during pretty printing: " + e.getMessage());
            return jsonString;
        }
    }

    private static String prettyPrintJsonAsync(String jsonString,
        AsyncTask.Callback<String> callback) {
        AsyncTask.runNonCancelable(() -> {
            try {
                return new GsonBuilder()
                    .setPrettyPrinting()
                    .create()
                    .toJson(JsonParser.parseString(jsonString));
            } catch (Exception e) {
                ILog.error("EditorManager.JsonBuilder",
                    "Error occurred when pretty printing json:" + e.getMessage());
                return null;
            }
        }, callback);
        return jsonString;
    }

    /**
     * Returns whether the app is running on a TV device.
     *
     * @param context Any context.
     * @return Whether the app is running on a TV device.
     */
    public static boolean isTv(Context context) {
        // See https://developer.android.com/training/tv/start/hardware.html#runtime-check.
        @Nullable UiModeManager uiModeManager = (UiModeManager) context
            .getApplicationContext()
            .getSystemService(UI_MODE_SERVICE);
        return uiModeManager != null
            && uiModeManager.getCurrentModeType() == Configuration.UI_MODE_TYPE_TELEVISION;
    }

    /**
     * Tests whether an {@code items} array contains an object equal to {@code item}, according to
     * {@link Object#equals(Object)}.
     *
     * <p>If {@code item} is null then true is returned if and only if {@code items} contains null.
     *
     * @param items The array of items to search.
     * @param item  The item to search for.
     * @return True if the array contains an object equal to the item being searched for.
     */
    public static boolean contains(@Nullable Object[] items, @Nullable Object item) {
        for (Object arrayItem : items) {
            if (areEqual(arrayItem, item)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Tests two objects for {@link Object#equals(Object)} equality, handling the case where one or
     * both may be null.
     *
     * @param o1 The first object.
     * @param o2 The second object.
     * @return {@code o1 == null ? o2 == null : o1.equals(o2)}.
     */
    public static boolean areEqual(@Nullable Object o1, @Nullable Object o2) {
        return Objects.equals(o1, o2);
    }

    public static void installApplication(Context context, File file) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(getUriForFile(context, file),
            "application/vnd.android" + ".package-archive");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            ILog.error(LOG_TAG, "Failed to install application: " + file.getName()
                + " as no: App Install Manager Exists", e);
        }
    }

    public static Uri getUriForFile(Context context, File file) {
        if (SDKUtil.isAtLeast(SDKUtil.API.ANDROID_7)) {
            return FileProvider.getUriForFile(context,
                BuildConfig.APPLICATION_ID + ".provider", file);
        } else {
            return Uri.fromFile(file);
        }
    }

    public static String getMimeType(Context context, File file) {
        var extension = MimeTypeMap.getFileExtensionFromUrl(getUriForFile(context, file).getPath());
        var type = MimeTypeMap
            .getSingleton()
            .getMimeTypeFromExtension(extension.toLowerCase());
        return (type != null) ? type : "*/*";
    }

    @NonNull
    public static List<String> listOf(String... args) {
        List<String> values = new ArrayList<>();
        Collections.addAll(values, args);
        return values;
    }

    /**
     * Returns whether the app is running on an automotive device.
     *
     * @param context Any context.
     * @return Whether the app is running on an automotive device.
     */
    public boolean isAutomotive() {
        if (SDKUtil.isAtLeast(API.ANDROID_6)) {
            return mContext
                .getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_AUTOMOTIVE);
        }
        return false;
    }

    /**
     * Returns whether the app is running on a TV device.
     *
     * @param context Any context.
     * @return Whether the app is running on a TV device.
     */
    public boolean isTv() {
        // See https://developer.android.com/training/tv/start/hardware.html#runtime-check.
        @Nullable UiModeManager uiModeManager = (UiModeManager) mContext
            .getApplicationContext()
            .getSystemService(UI_MODE_SERVICE);
        return uiModeManager != null
            && uiModeManager.getCurrentModeType() == Configuration.UI_MODE_TYPE_TELEVISION;
    }

    /**
     * @return mimeType of a file
     */
    public String getMimeType(Context context, Uri uri) {
        if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme())) {
            return context
                .getContentResolver()
                .getType(uri);
        }
        var file = new File(uri.getPath());
        var extension = MimeTypeMap.getFileExtensionFromUrl(Uri
            .fromFile(file)
            .toString());
        var type = MimeTypeMap
            .getSingleton()
            .getMimeTypeFromExtension(extension.toLowerCase());
        return (type != null) ? type : "*/*";
    }
}
