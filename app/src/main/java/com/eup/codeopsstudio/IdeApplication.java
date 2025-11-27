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

package com.eup.codeopsstudio;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.os.Process;
import androidx.annotation.NonNull;
import com.eup.codeopsstudio.common.Constants;
import com.eup.codeopsstudio.common.ContextManager;
import com.eup.codeopsstudio.common.FileLogListener;
import com.eup.codeopsstudio.common.ILog;
import com.eup.codeopsstudio.common.SystemArchitecture;
import com.eup.codeopsstudio.common.util.PreferencesUtils;
import com.eup.codeopsstudio.editor.ContextualCodeEditor;
import com.eup.codeopsstudio.ui.debug.CrashActivity;
import com.eup.codeopsstudio.util.ThrowableUtils;
import com.eup.codeopsstudio.util.Wizard;
import com.eup.codeopsstudio.util.manager.ThemeManager;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crashlytics.CustomKeysAndValues;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class IdeApplication extends Application implements Thread.UncaughtExceptionHandler {

  public static final String TAG = IdeApplication.class.getSimpleName();

  private static IdeApplication instance;
  private static final long SLEEP_DURATION = 2000; // milliseconds

  private ThemeManager themeManager;
  private FirebaseCrashlytics crashlytics;
  private FileLogListener fileLogListener;
  private final StringBuilder errorMessage = new StringBuilder();

  @Override
  public void onCreate() {
    ILog.mode(isAppInDebugMode());
    super.onCreate();
    instance = this;
    ContextManager.initialize(getGlobalContext());

    fileLogListener = new FileLogListener(this, "freeze_log.txt");
    ILog.addLogListener(fileLogListener);

    themeManager = new ThemeManager(this);
    themeManager.applyTheme();
    themeManager.applyDynamicColors();
    crashlytics = FirebaseCrashlytics.getInstance();
    crashlytics.setCrashlyticsCollectionEnabled(userHasConsentedToDataSharing());
    FirebaseAnalytics.getInstance(this)
        .setAnalyticsCollectionEnabled(userHasConsentedToDataSharing());

    Thread.setDefaultUncaughtExceptionHandler(this);
    crashlytics.sendUnsentReports();

    validateExpirationDate();
    loadEditorConfigurations();
  }

  public static boolean isAppInDebugMode() {
    return BuildConfig.DEBUG;
  }

  private void validateExpirationDate() {
    var currentDate = Calendar.getInstance();
    var fixedFutureDate =
        new GregorianCalendar(
            Constants.EXPIRATION_YEAR, Constants.EXPIRATION_MONTH, Constants.EXPIRATION_DAY);

    if (currentDate.after(fixedFutureDate)) {
      var msg =
          "This version of CodeOps Studio is outdated. Please download the latest "
              + "version from Git Hub: "
              + Constants.GITHUB_URL;
      throw new RuntimeException(msg);
    }
  }

  private boolean userHasConsentedToDataSharing() {
    return PreferencesUtils.canShareAnonymousStatistics();
  }

  private void loadEditorConfigurations() {
    try {
      ContextualCodeEditor.loadConfigurations(IdeApplication.this);
      ILog.info(TAG, "Code editor configurations loaded successfully");
    } catch (Exception e) {
      ILog.error(TAG, "Error loading code editor configurations", e);
    }
  }

  /**
   * Returns the global Application context of the current process. This generally should only be
   * used if you need a Context whose lifecycle is tied to the lifetime of the current process
   * rather than the current component context like a an Activity lifecycle.
   *
   * <p>Use when:
   *
   * <ul>
   *   <li>Accessing application-wide resources or services.
   *   <li>Registering/unregistering components with application lifecycle.
   * </ul>
   *
   * <p>Avoid when:
   *
   * <ul>
   *   <li>Creating UI components or views tied to activity/fragment lifecycles.
   *   <li>Registering/unregistering components with activity/fragment lifecycles.
   * </ul>
   */
  public static Context getGlobalContext() {
    return instance.getApplicationContext();
  }

  @Override
  public void uncaughtException(Thread thread, @NonNull Throwable throwable) {
    try {
      final var crashDate = Calendar.getInstance().getTime().toString();
      errorMessage.append(ThrowableUtils.getFullStackTrace(throwable));
      errorMessage.append(crashDate).append(Constants.NEXT_LINE.repeat(2));

      crashlytics.setUserId(Wizard.getUserID(getGlobalContext()));
      CustomKeysAndValues keysAndValues =
          new CustomKeysAndValues.Builder()
              .putString("Device " + "Model", Wizard.getDeviceBuildModel())
              .putString("Device Sdk Version", Wizard.getDeviceSDKVersion())
              .putString("Device Manufacturer", Wizard.getDeviceManufacturer())
              .putString("Device Release Version", Wizard.getDeviceReleaseVersion())
              .putString("Device CPU Architecture", getArchitecture())
              .putString("Device Country", Wizard.getDeviceCountry(getGlobalContext()))
              .putString("App Package Name", Wizard.getAppPackageName(getGlobalContext()))
              .putString("App Version " + "Name", Wizard.getAppVersionName(getGlobalContext()))
              .putString("App " + "Version Code", Wizard.getAppVersionCode(getGlobalContext()))
              .putString("Error", errorMessage.toString())
              .putString("Crash Date", crashDate)
              .build();
      crashlytics.setCustomKeys(keysAndValues);

      crashlytics.log("Uncaught exception in thread: " + thread.getName());
      crashlytics.recordException(throwable);

      var restartIntent = new Intent(this, CrashActivity.class);
      restartIntent.putExtra("error", errorMessage.toString());
      restartIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
      startActivity(restartIntent);
      // allow crashlytics log reports completely
      scheduleProcessTermination();
    } catch (Exception e) {
      ILog.error(TAG, "Failed to restart", e);
    }
  }

  @Override
  public void onTerminate() {
    // invocation not guaranteed
    if (fileLogListener != null) {
      fileLogListener.stop();
    }
    super.onTerminate();
  }

  @NonNull
  public static String getArchitecture() {
    return SystemArchitecture.getArchitecture();
  }

  private void scheduleProcessTermination() {
    new Thread(
            () -> {
              try {
                Thread.sleep(SLEEP_DURATION);
              } catch (InterruptedException exception) {
                // InterruptedException ignored
              }
              Process.killProcess(Process.myPid());
              System.exit(1);
            })
        .start();
  }

  @NonNull
  public static FirebaseAnalytics getAnalytics() {
    return FirebaseAnalytics.getInstance(getGlobalContext());
  }

  public static ConnectivityManager getConnectivityManager() {
    return (ConnectivityManager) getGlobalSystemService(Context.CONNECTIVITY_SERVICE);
  }

  public static Object getGlobalSystemService(String name) {
    return getGlobalContext().getSystemService(name);
  }

  public static Configuration getGlobalConfiguration() {
    return getGlobalResources().getConfiguration();
  }

  public static Resources getGlobalResources() {
    return getGlobalContext().getResources();
  }

  public static IdeApplication getInstance() {
    return instance;
  }

  public ThemeManager getThemeManager() {
    return themeManager;
  }
}
