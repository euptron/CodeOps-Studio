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

package com.eup.codeopsstudio;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Environment;
import android.os.Process;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import com.blankj.utilcode.util.ThrowableUtils;
import com.eup.codeopsstudio.common.Constants;
import com.eup.codeopsstudio.common.ContextManager;
import com.eup.codeopsstudio.common.util.PreferencesUtils;
import com.eup.codeopsstudio.common.util.SDKUtil;
import com.eup.codeopsstudio.common.util.SDKUtil.API;
import com.eup.codeopsstudio.editor.ContextualCodeEditor;
import com.eup.codeopsstudio.util.Wizard;
import com.google.android.material.color.DynamicColors;
import com.google.android.material.color.DynamicColors.Precondition;
import com.google.android.material.color.DynamicColorsOptions;
import com.google.android.material.color.HarmonizedColors;
import com.google.android.material.color.HarmonizedColorsOptions;
import com.google.firebase.crashlytics.CustomKeysAndValues;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import java.io.File;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class IdeApplication extends Application implements Thread.UncaughtExceptionHandler {

  private static IdeApplication sInstance;
  public static Context applicationContext;
  private String newLine = "\n";
  private StringBuilder errorMessage = new StringBuilder();
  private FirebaseCrashlytics crashlytics;

  private static final String ARM = "armeabi-v7a";
  private static final String AARCH64 = "arm64-v8a";
  private static final String I686 = "x86";
  private static final String X86_64 = "x86_64";

  @Override
  public void onCreate() {
    super.onCreate();
    applicationContext = this;
    sInstance = this;
    ContextManager.initialize(applicationContext);
    // Initialize Firebase Crashlytics for crash reporting, it's disable in AndroidManifest
    crashlytics = FirebaseCrashlytics.getInstance();
    crashlytics.setCrashlyticsCollectionEnabled(userHasConsentedToCrashReporting());
    Thread.setDefaultUncaughtExceptionHandler(this);
    crashlytics.sendUnsentReports();
    // validateExpirationDate();
    changeTheme(PreferencesUtils.getCurrentTheme());
    applyDynamicColor();
    try {
      ContextualCodeEditor.loadConfigurations(this);
    } catch (Exception e) {
      crashlytics.recordException(e);
    }
  }

  @Override
  public void uncaughtException(Thread thread, Throwable throwable) {
    writeException(throwable);

    if (isAppInDebugMode()) {
      errorMessage.append(ThrowableUtils.getFullStackTrace(throwable));
    } else {
      errorMessage.append(throwable.getCause());
    }

    final var crashDate = Calendar.getInstance().getTime().toString();
    errorMessage.append(crashDate).append(Constants.NEXT_LINE.repeat(2));

    crashlytics.setUserId(Wizard.getUserID(getApplicationContext()));
    CustomKeysAndValues keysAndValues =
        new CustomKeysAndValues.Builder()
            .putString("Device Model", Wizard.getDeviceBuildModel())
            .putString("Device Sdk Version", Wizard.getDeviceSDKVersion())
            .putString("Device Manufacturer", Wizard.getDeviceManuFacturer())
            .putString("Device Release Version", Wizard.getDeviceReleaseVersion())
            .putString("Device Country", Wizard.getDeviceCountry(getApplicationContext()))
            .putString("App Package Name", Wizard.getAppPackageName(getApplicationContext()))
            .putString("App Version Name", Wizard.getAppVersionName(getApplicationContext()))
            .putString("App Version Code", Wizard.getAppVersionCode(getApplicationContext()))
            .putString("Error", errorMessage.toString())
            .putString("Crash Date", crashDate)
            .build();
    crashlytics.setCustomKeys(keysAndValues);

    crashlytics.log("Uncaught exception in thread: " + thread.getName());
    crashlytics.recordException(throwable);

    try {
      var restartIntent = new Intent(this, CrashActivity.class);
      restartIntent.putExtra("error", errorMessage.toString());
      restartIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
      startActivity(restartIntent);
      // Kills the process after a delay to allow Crashlytics to log reports completely
      scheduleProcessTermination();
    } catch (Throwable e) {
      crashlytics.recordException(e);
      e.printStackTrace();
    }
  }

  public static IdeApplication getInstance() {
    return sInstance;
  }

  public void changeTheme(int themeMode) {
    AppCompatDelegate.setDefaultNightMode(themeMode);
  }

  private void applyDynamicColor() {
    if (!SDKUtil.isAtLeast(API.ANDROID_12)) return;

    final Precondition precondition = (activity, theme) -> PreferencesUtils.useDynamicColors();
    DynamicColors.applyToActivitiesIfAvailable(
        this,
        new DynamicColorsOptions.Builder()
            .setPrecondition(precondition)
            .setOnAppliedCallback(
                activity -> {
                  if ((activity instanceof MainActivity)
                      && ((MainActivity) activity).isColorHarmonizationEnabled()) {
                    HarmonizedColors.applyToContextIfAvailable(
                        activity, HarmonizedColorsOptions.createMaterialDefaults());
                  }
                })
            .build());
  }

  public static boolean isAppInDebugMode() {
    return BuildConfig.DEBUG;
  }

  private void validateExpirationDate() {
    var currentDate = Calendar.getInstance();
    var fixedFutureDate = new GregorianCalendar(2024, Calendar.JUNE, 20);

    if (currentDate.after(fixedFutureDate)) {
      var msg =
          "This QA version of the app has expired. Please download the latest version from Google Play Store: "
              + Constants.GOOGLE_PLAY_APP_URL;
      throw new RuntimeException(msg);
    }
  }

  public void writeException(Throwable th) {
    final var logFilePath = Environment.getExternalStorageDirectory().getAbsolutePath();
    final var logFile = new File(logFilePath, "oxidelog.txt");
    final var log = ThrowableUtils.getFullStackTrace(th);
    // TODO: Write log to file to path
  }

  private boolean userHasConsentedToCrashReporting() {
    // TODO: logic to check if the user has given consent
    return true;
  }

  private void scheduleProcessTermination() {
    new Thread(
            () -> {
              try {
                Thread.sleep(2000); // 2-second delay
              } catch (InterruptedException ignore) {

              }
              // Terminate the process
              Process.killProcess(Process.myPid());
              System.exit(1);
            })
        .start();
  }

  public static boolean supportsArm32Bit() {
    return Arrays.asList(Build.SUPPORTED_ABIS).contains(ARM);
  }

  public static boolean supportsArm64Bit() {
    return Arrays.asList(Build.SUPPORTED_ABIS).contains(AARCH64);
  }

  public static boolean supportsX86_32Bit() {
    return Arrays.asList(Build.SUPPORTED_ABIS).contains(I686);
  }

  public static boolean supportsX86_64Bit() {
    return Arrays.asList(Build.SUPPORTED_ABIS).contains(X86_64);
  }

  public static boolean isSupportedArch() {
    return supportsArm32Bit() || supportsArm64Bit() || supportsX86_32Bit() || supportsX86_64Bit();
  }

  @NonNull
  public static String getArchitecture() {
    if (supportsArm32Bit()) return ARM;
    else if (supportsArm64Bit()) return AARCH64;
    else if (supportsX86_32Bit()) return I686;
    else if (supportsX86_64Bit()) return X86_64;
    else throw new UnsupportedOperationException("Device architecture not supported");
  }
}
