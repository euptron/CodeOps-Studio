/*
 * This file is part of CodeOps Studio.
 * CodeOps Studio - Code anywhere anytime
 * https://github.com/euptron/CodeOps-Studio
 * Copyright (C) 2024-2026 Etido Peter
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

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import static android.content.Context.UI_MODE_SERVICE;

import android.Manifest;
import android.app.UiModeManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.webkit.MimeTypeMap;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import com.eup.codeopsstudio.BuildConfig;
import com.eup.codeopsstudio.R;
import com.eup.codeopsstudio.common.AsyncTask;
import com.eup.codeopsstudio.common.Constants;
import com.eup.codeopsstudio.common.ILog;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

/**
 * Main-Stream Utility class
 *
 * <p>The wizard of COS
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

  public static String getAppPackageName(Context context) {
    return validate(context.getPackageName());
  }

  public static String getAppVersionCode(Context context) {
    return validate(String.valueOf(getAppVersionCodeLong(context)));
  }

  @SuppressWarnings("deprecation")
  @SuppressLint("Deprecation")
  public static long getAppVersionCodeLong(Context context) {
    PackageManager packageManager = context.getPackageManager();
    PackageInfo packageInfo;
    try {
      packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        return packageInfo.getLongVersionCode();
      } else {
        return packageInfo.versionCode;
      }
    } catch (PackageManager.NameNotFoundException e) {
      ILog.error(LOG_TAG, "Failed to get application version code as long", e);
      return -1L;
    }
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

  /**
   * Retrieve the preferred ABI of the device. Some devices can support multiple ABIs and the first
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

  public static String getDeviceBoard() {
    return validate(Build.BOARD);
  }

  public static String getDeviceBuildModel() {
    return validate(Build.MODEL);
  }

  public static String getDeviceCountry(Context context) {
    return getLocaleCountry(context);
  }

  public static String getLocaleCountry(Context context) {
    String param = null;
    TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
    if (tm != null) {
      if (!isEmpty(tm.getSimCountryIso(), true)) {
        param = tm.getSimCountryIso();
      } else if (!isEmpty(tm.getNetworkCountryIso(), true)) {
        param = tm.getNetworkCountryIso();
      }
      if (!isEmpty(param, true) && param.length() == 2) {
        return validate(param.toUpperCase());
      }
      Configuration configuration = context.getResources().getConfiguration();
      Locale locale;
      locale = configuration.getLocales().get(0);
      return validate(locale.getCountry());
    }
    return validate(null);
  }

  @NonNull
  public static String getDeviceLocaleLanguage() {
    var param = Locale.getDefault().getLanguage();
    return validate(param);
  }

  public static String getDeviceManufacturer() {
    return validate(Build.MANUFACTURER);
  }

  public static String getDeviceReleaseVersion() {
    return validate(Build.VERSION.RELEASE);
  }

  public static String getDeviceSDKVersion() {
    return validate(String.valueOf(Build.VERSION.SDK_INT));
  }

  public static String getFilePathOrEmpty(File file) {
    if (file != null) {
      return validate(file.getAbsolutePath(), null);
    }
    return "";
  }

  public static String getMimeType(Context context, File file) {
    var extension = MimeTypeMap.getFileExtensionFromUrl(getUriForFile(context, file).getPath());
    var type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.toLowerCase());
    return (type != null) ? type : "*/*";
  }

  /**
   * @return mimeType of a file
   */
  public String getMimeType(Context context, Uri uri) {
    if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme())) {
      return context.getContentResolver().getType(uri);
    }
    var file = new File(uri.getPath());
    var extension = MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(file).toString());
    var type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.toLowerCase());
    return (type != null) ? type : "*/*";
  }

  public static long getTime() {
    return new Date().getTime();
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

  public static String getDeviceBuildID() {
    return validate(Build.ID);
  }

  @NonNull
  public static String ensureEmptyIfNull(String str) {
    return str == null ? "" : str;
  }

  @NonNull
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

  public static boolean isPackageInstalled(Context context, String packageName) {
    try {
      context.getPackageManager().getPackageInfo(packageName, 0);
      return true;
    } catch (PackageManager.NameNotFoundException e) {
      return false;
    }
  }

  public static void installApplication(Context context, File file) {
    installApplication(context, file, null);
  }

  public static void installApplication(Context context, File file, Runnable action) {
    final Uri uri = getUriForFile(context, file);
    installApplication(context, uri, action);
  }

  public static void installApplication(Context context, Uri uri, Runnable action) {
    // await installation
    BroadcastReceiver packageAddedReceiver =
        new BroadcastReceiver() {
          @Override
          public void onReceive(Context context, Intent intent) {
            String installedPackage = intent.getData().getSchemeSpecificPart();
            if (installedPackage.equals(context.getPackageName())) {
              // APK installed successfully :-)
              if (action != null) action.run();
              context.unregisterReceiver(this);
            }
          }
        };

    IntentFilter filter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
    filter.addDataScheme("package");
    context.registerReceiver(packageAddedReceiver, filter);

    // initiate installation
    try {
      Intent intent = new Intent(Intent.ACTION_VIEW);
      intent.setDataAndType(uri, "application/vnd.android.package-archive");
      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
      context.startActivity(intent);
    } catch (ActivityNotFoundException e) {
      ILog.error(LOG_TAG, "Error installing APK: " + uri.toString(), e);
      BaseUtil.toastShort(R.string.install_error);
    }
  }

  public static Uri getUriForFile(Context context, File file) {
    return FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", file);
  }

  /**
   * Returns whether the app is running on an automotive device.
   *
   * @return Whether the app is running on an automotive device.
   */
  public boolean isAutomotive() {
    return mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_AUTOMOTIVE);
  }

  public static boolean isEmpty(@NonNull char[] chars) {
    return chars.length == 0;
  }

  public static boolean isEmpty(Map<?, ?> map) {
    return map.size() <= 0;
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

  @NonNull
  public static List<String> listOf(String... args) {
    List<String> values = new ArrayList<>();
    Collections.addAll(values, args);
    return values;
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
      return resultFuture.get();
    } catch (Exception e) {
      ILog.error(LOG_TAG, "Error occurred during pretty printing: " + e.getMessage());
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
            ILog.error(LOG_TAG, "Error occurred when pretty printing json:" + e.getMessage());
            return null;
          }
        },
        callback);
    return jsonString;
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

  public static String toUpperCase(String inputString) {
    String result = "";
    for (int i = 0; i < inputString.length(); i++) {
      char currentChar = inputString.charAt(i);
      char currentCharToUpperCase = Character.toUpperCase(currentChar);
      result = result + currentCharToUpperCase;
    }
    return result;
  }

  public static void removeIntentExtras(Intent intent, String... keys) {
    for (String key : keys) {
      if (intent.hasExtra(key)) {
        intent.removeExtra(key);
      }
    }
  }

  public static Bundle toBundle(Intent intent) {
    Bundle bundle = new Bundle();
    if (intent != null && intent.getExtras() != null) {
      bundle = intent.getExtras();
    }
    return bundle;
  }

  public static Intent toIntent(Bundle bundle) {
    Intent intent = new Intent();
    if (bundle != null && !bundle.isEmpty()) {
      intent.putExtras(bundle);
    }
    return intent;
  }

  public static boolean toBoolean(String str) {
    return str != null && str.equalsIgnoreCase("true");
  }

  public static boolean allNotNull(Object... objs) {
    for (Object obj : objs) {
      if (obj == null) return false;
    }
    return true;
  }

  public static boolean allNotNullAndEmpty(String... strings) {
    for (String str : strings) {
      if (isEmpty(str, true)) return false;
    }
    return true;
  }

  public static boolean isEmpty(String str) {
    return isEmpty(str, false);
  }

  public static boolean isEmpty(String str, boolean trim) {
    return str == null || (trim ? str.trim().isEmpty() : str.isEmpty()) || str.equalsIgnoreCase("");
  }

  public static String formatToBulletList(String delimiter, String text) {
    if (isEmpty(text)) return "";

    String[] items = text.split(Pattern.quote(delimiter));

    StringBuilder bulletList = new StringBuilder();

    for (int i = 0; i < items.length; i++) {
      String item = items[i].trim();
      if (!isEmpty(item)) {
        bulletList.append("- ").append(item);
        if (i < items.length - 1) {
          bulletList.append("\n");
        }
      }
    }

    return bulletList.toString();
  }

  /// -- Storage Permission

  public static boolean isStoragePermissionGranted(Context context) {
    return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
        ? isStoragePermissionGrantedApi30(context)
        : isStoragePermissionGrantedApi19(context);
  }

  @RequiresApi(30)
  private static boolean isStoragePermissionGrantedApi30(Context context) {
    return (ActivityCompat.checkSelfPermission(
                context, Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
            == PackageManager.PERMISSION_GRANTED)
        || (ActivityCompat.checkSelfPermission(
                context, Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
            == PackageManager.PERMISSION_GRANTED)
        || Environment.isExternalStorageManager();
  }

  private static boolean isStoragePermissionGrantedApi19(Context context) {
    int readStatus =
        ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE);
    int writeStatus =
        ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);

    return readStatus == PackageManager.PERMISSION_GRANTED
        && writeStatus == PackageManager.PERMISSION_GRANTED;
  }

  @RequiresApi(api = Build.VERSION_CODES.R)
  public static void requestStoragePermissionApi30(
      Context context, ActivityResultLauncher<Intent> launcher) {
    try {
      Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
      Uri uri = Uri.fromParts("package", context.getPackageName(), null);
      intent.setData(uri);
      launcher.launch(intent);
    } catch (ActivityNotFoundException anfe) {
      ILog.error(
          LOG_TAG,
          "ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION not found, trying fallback",
          anfe);
      try {
        Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
        Uri uri = Uri.fromParts("package", context.getPackageName(), null);
        intent.setData(uri);
        launcher.launch(intent);
      } catch (Exception e) {
        ILog.error(LOG_TAG, "ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION not found", e);
        BaseUtil.toastLong(R.string.storage_permission_denied);
      }
    } catch (Exception e) {
      ILog.error(LOG_TAG, "Failed to request permission to grant access to all files", e);
      BaseUtil.toastLong(R.string.storage_permission_denied);
    }
  }

  public static void requestStoragePermissionApi19(ActivityResultLauncher<String[]> launcher) {
    final String[] permissions =
        new String[] {
          Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE
        };
    launcher.launch(permissions);
  }

  public static String getStoragePermissionName() {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
        ? Constants.MANAGE_EXTERNAL_STORAGE_PERMISSION
        : Manifest.permission.READ_EXTERNAL_STORAGE.concat(", ")
            + Manifest.permission.WRITE_EXTERNAL_STORAGE;
  }

  public static void launchDeviceSettingsActivity(Context context, String action) {
    String packageName = context.getPackageName();

    if (isEmpty(packageName)) {
      ILog.error(LOG_TAG, "Package name is NOE. Cannot launch settings.");
      return;
    }

    try {
      var intent = new Intent(action);
      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

      if (Settings.ACTION_APPLICATION_DETAILS_SETTINGS.equals(action)) {
        intent.setData(Uri.fromParts("package", packageName, null));
      } else {
        intent.putExtra(Settings.EXTRA_APP_PACKAGE, packageName);
      }

      context.startActivity(intent);
    } catch (ActivityNotFoundException e) {
      ILog.error(LOG_TAG, "Could not open " + action, e);
      if (!Settings.ACTION_APPLICATION_DETAILS_SETTINGS.equals(action)) {
        launchDeviceSettingsActivity(context, Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
      }
    }
  }

  /// -- Notification Permission
  public static boolean areNotificationsAllowed(Context context) {
    var nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    return nm.areNotificationsEnabled();
  }

  @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
  public static boolean isNotificationPermissionGranted(Context context) {
    int grantStatus =
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS);
    return grantStatus == PackageManager.PERMISSION_GRANTED;
  }
}
