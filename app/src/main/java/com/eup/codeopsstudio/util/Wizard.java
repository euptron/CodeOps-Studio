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
 
package com.eup.codeopsstudio.util;

import static android.content.Context.UI_MODE_SERVICE;
import static com.eup.codeopsstudio.common.models.Document.MimeType.*;
import static com.eup.codeopsstudio.common.util.SDKUtil.API;

import android.app.UiModeManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import com.eup.codeopsstudio.BuildConfig;
import com.eup.codeopsstudio.common.AsyncTask;
import com.eup.codeopsstudio.common.util.SDKUtil;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import java.io.File;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Main-Stream Utility class
 *
 * <p>The wizard of OZ /*
 *
 * @author EUP
 */
public class Wizard {

  protected Context mContext;
  private Analytics mAnalytics;
  public static final String LOG_TAG = Wizard.class.getSimpleName();

  public Wizard(Context context) {
    mContext = context;
    mAnalytics = new Analytics(context);
  }
  
  public void uploadAnynomousAnalytics(){
     uploadAnynomousAnalytics(true);
  }
  public void uploadAnynomousAnalytics(boolean enabled) {
    if (enabled) {
      mAnalytics.enableAnalytics(true);
      // Set user properties
      mAnalytics.setUserProperty("package_name", getAppPackageName(mContext));
      mAnalytics.setUserProperty("app_version_code", getAppVersionCode(mContext));
      mAnalytics.setUserProperty("app_version_name", getAppVersionName(mContext));
      // Set system properties
      mAnalytics.setUserProperty("device_model", getDeviceBuildModel());
      mAnalytics.setUserProperty("device_sdk_version", getDeviceSDKVersion());
      mAnalytics.setUserProperty("device_build_id", getDeviceBuildID());
      mAnalytics.setUserProperty("device_release", getDeviceReleaseVersion());
      mAnalytics.setUserProperty("device_board", getDeviceBoard());
      mAnalytics.setUserProperty("device_brand", getDeviceManuFacturer());
      mAnalytics.setUserProperty("device_cpu_arch", getDeviceArchitecture());
      mAnalytics.setUserProperty("device_locale_country", getLocaleCountry(mContext));
      mAnalytics.setUserProperty("device_locale", getDeviceLocaleLanguage());
      mAnalytics.setUserProperty("device_country", getDeviceCountry(mContext));
      mAnalytics.setUserProperty("user_id", getUserID(mContext));
    } else {
      mAnalytics.enableAnalytics(false);
    }
  }

  private static String uniqueID = null;
  private static final String PREF_UNIQUE_ID = "PREF_UNIQUE_ID";

  public static synchronized String getUserID(Context context) {
    if (uniqueID == null) {
      SharedPreferences sharedPrefs =
          context.getSharedPreferences(PREF_UNIQUE_ID, Context.MODE_PRIVATE);
      uniqueID = sharedPrefs.getString(PREF_UNIQUE_ID, null);
      if (uniqueID == null) {
        uniqueID = "user" + UUID.randomUUID().toString() + ":" + getDeviceBuildID();
        Editor editor = sharedPrefs.edit();
        editor.putString(PREF_UNIQUE_ID, uniqueID);
        editor.apply();
      }
    }
    return uniqueID;
  }

  public static long getTime() {
    return new Date().getTime();
  }

    public static String getLocaleCountry(Context context) {
    String param = null;
    TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

    if (tm != null) {
      if (!isEmpty(tm.getSimCountryIso())) {
        param = tm.getSimCountryIso();
      } else if (!isEmpty(tm.getNetworkCountryIso())) {
        param = tm.getNetworkCountryIso();
      }

      if (!isEmpty(param) && param.length() == 2) {
        return validate(param.toUpperCase());
      }

      Configuration configuration = context.getResources().getConfiguration();
      Locale locale;
      if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
        locale = configuration.getLocales().get(0);
      } else {
        locale = configuration.locale;
      }
      return validate(locale.getCountry());
    }
    return validate(null);
  }

  public static String getDeviceCountry(Context context) {
    return getLocaleCountry(context);
  }
    
  public static String getDeviceBuildModel() {
    return validate(Build.MODEL);
  }

  public static String getDeviceSDKVersion() {
    return validate(Build.VERSION.SDK);
  }

  public static String getDeviceBuildID() {
    return validate(Build.ID);
  }

  public static String getDeviceReleaseVersion() {
    return validate(Build.VERSION.RELEASE);
  }

  public static String getDeviceBoard() {
    return validate(Build.BOARD);
  }

  public static String getDeviceManuFacturer() {
    return validate(Build.MANUFACTURER);
  }

  /**
   * Retrieve the preferred ABI of the device. Some devices can support multiple ABIs and the first
   * one returned in the preferred one.
   *
   * <p>Supressed deprecation warning because that code path is only used below Lollipop.
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

  public static String getDeviceLocaleLanguage() {
    var param = Locale.getDefault().getLanguage();
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

  public static boolean isEmpty(String str) {
    if (str == null || str.trim().isEmpty() || str.equalsIgnoreCase("")) return true;
    return false;
  }

  public static String validate(String str) {
    return validate(str, "unavailable");
  }

  public static String validate(String str, String fallback) {
    if (str == null || str.trim().isEmpty() || str.equalsIgnoreCase("")) {
      return fallback == null ? "" : fallback;
    } else {
      return str;
    }
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
      e.printStackTrace();
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
      e.printStackTrace();
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
      e.printStackTrace();
      return validate(null);
    }
  }

  /** The Analytics class is responsible for handling Firebase Analytics operations. */
  public class Analytics {
    /** The FirebaseAnalytics instance used to record events and user properties. */
    private FirebaseAnalytics mFirebaseAnalytics;

    /**
     * Initializes the Analytics object with the FirebaseAnalytics instance. Call this method in the
     * onCreate() of your activity or application context.
     *
     * @param context The activity or application context.
     */
    public Analytics(Context context) {
      mFirebaseAnalytics = FirebaseAnalytics.getInstance(context);
    }
    
    public void enableAnalytics(boolean enable) {
      mFirebaseAnalytics.setAnalyticsCollectionEnabled(enable);
    }
    
    /**
     * Sets a user property to a given value.
     *
     * @param propertyKey The key of the user property.
     * @param propertyName The value of the user property.
     */
    public void setUserProperty(String propertyKey, String propertyName) {
      mFirebaseAnalytics.setUserProperty(propertyKey, propertyName);
    }

    /**
     * Records a Firebase Analytics event with a specific name and parameters.
     *
     * @param event The event to be logged.
     * @param bundle A Bundle containing event parameters.
     */
    public void recordEvent(FirebaseAnalytics.Event event, Bundle bundle) {
      mFirebaseAnalytics.logEvent(event.toString(), bundle);
    }

    /**
     * Records a Firebase Analytics event with a custom name and parameters.
     *
     * @param eventName The name of the event to be logged.
     * @param params A Bundle containing event parameters.
     */
    public void recordEvent(String eventName, Bundle params) {
      mFirebaseAnalytics.logEvent(eventName, params);
    }
  }

  public static String prettyPrintJson(String jsonString) {
    CompletableFuture<String> resultFuture = new CompletableFuture<>();
    prettyPrintJsonAsync(
        jsonString,
        (result) -> {
          if (result != null) {
            resultFuture.complete(result);
          }
        });
    try {
      // Wait for the result and return it
      return resultFuture.get();
    } catch (Exception e) {
      Log.e(
          "EditorManager.JsonBuilder", "Error occurred during pretty printing: " + e.getMessage());
      return jsonString;
    }
  }

  private static String prettyPrintJsonAsync(
      String jsonString, AsyncTask.Callback<String> callback) {
    AsyncTask.runNonCancelable(
        () -> {
          try {
            return new GsonBuilder()
                .setPrettyPrinting()
                .create()
                .toJson(JsonParser.parseString(jsonString));
          } catch (Exception e) {
            Log.e(
                "EditorManager.JsonBuilder",
                "Error occurred when pretty printing json:" + e.getMessage());
            return null;
          }
        },
        callback);

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
    @Nullable
    UiModeManager uiModeManager =
        (UiModeManager) context.getApplicationContext().getSystemService(UI_MODE_SERVICE);
    return uiModeManager != null
        && uiModeManager.getCurrentModeType() == Configuration.UI_MODE_TYPE_TELEVISION;
  }

  /**
   * Returns whether the app is running on an automotive device.
   *
   * @param context Any context.
   * @return Whether the app is running on an automotive device.
   */
  public boolean isAutomotive() {
    if (SDKUtil.isAtLeast(API.ANDROID_6)) {
      return mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_AUTOMOTIVE);
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
    @Nullable
    UiModeManager uiModeManager =
        (UiModeManager) mContext.getApplicationContext().getSystemService(UI_MODE_SERVICE);
    return uiModeManager != null
        && uiModeManager.getCurrentModeType() == Configuration.UI_MODE_TYPE_TELEVISION;
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
    return o1 == null ? o2 == null : o1.equals(o2);
  }

  /**
   * Tests whether an {@code items} array contains an object equal to {@code item}, according to
   * {@link Object#equals(Object)}.
   *
   * <p>If {@code item} is null then true is returned if and only if {@code items} contains null.
   *
   * @param items The array of items to search.
   * @param item The item to search for.
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
    
    public static void installApplication(Context context, File file) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uriFromFile(context, file), "application/vnd.android.package-archive");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static Uri uriFromFile(Context context, File file) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", file);
        } else {
            return Uri.fromFile(file);
        }
    }
}
