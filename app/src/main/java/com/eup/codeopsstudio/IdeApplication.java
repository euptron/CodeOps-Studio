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
import android.os.Build;
import android.os.Process;

import androidx.annotation.NonNull;

import com.eup.codeopsstudio.common.AsyncTask;
import com.eup.codeopsstudio.common.Constants;
import com.eup.codeopsstudio.common.ContextManager;
import com.eup.codeopsstudio.common.ILog;
import com.eup.codeopsstudio.common.util.PreferencesUtils;
import com.eup.codeopsstudio.editor.ContextualCodeEditor;
import com.eup.codeopsstudio.util.ThrowableUtils;
import com.eup.codeopsstudio.util.Wizard;
import com.eup.codeopsstudio.util.manager.ThemeManager;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crashlytics.CustomKeysAndValues;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class IdeApplication extends Application implements Thread.UncaughtExceptionHandler {

    public static final String TAG = IdeApplication.class.getSimpleName();
    private static final long SLEEP_DURATION = 2000; // milliseconds
    private static IdeApplication instance;
    private final StringBuilder errorMessage = new StringBuilder();
    private final ExecutorService backgroundExecutor = Executors.newSingleThreadExecutor();
    private FirebaseCrashlytics crashlytics;
    private CompletableFuture<Void> editorConfigFuture;
    private ThemeManager themeManager;

    public static IdeApplication getInstance() {
        return instance;
    }

    public static Configuration getGlobalConfiguration() {
        return getGlobalResources().getConfiguration();
    }

    public static Resources getGlobalResources() {
        return getGlobalContext().getResources();
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

    public static ConnectivityManager getConnectivityManager() {
        return (ConnectivityManager) getGlobalSystemService(Context.CONNECTIVITY_SERVICE);
    }

    public static Object getGlobalSystemService(String name) {
        return getGlobalContext().getSystemService(name);
    }

    @NonNull
    public static FirebaseAnalytics getAnalytics() {
        return FirebaseAnalytics.getInstance(getGlobalContext());
    }

    public ThemeManager getThemeManager() {
        return themeManager;
    }

    public CompletableFuture<Void> getEditorConfigFuture() {
        return editorConfigFuture;
    }

    @Override
    public void onCreate() {
        ILog.mode(isAppInDebugMode());
        super.onCreate();
        instance = this;
        ContextManager.initialize(getGlobalContext());

        themeManager = new ThemeManager(this);
        themeManager.applyTheme();
        themeManager.applyDynamicColors();
        crashlytics = FirebaseCrashlytics.getInstance();
        crashlytics.setCrashlyticsCollectionEnabled(userHasConsentedToDataSharing());
        FirebaseAnalytics
            .getInstance(this)
            .setAnalyticsCollectionEnabled(userHasConsentedToDataSharing());

        Thread.setDefaultUncaughtExceptionHandler(this);
        crashlytics.sendUnsentReports();
        validateExpirationDate();
        editorConfigFuture = initializeEditorConfigurationsInBackground();
    }

    public static boolean isAppInDebugMode() {
        return BuildConfig.DEBUG;
    }

    private void validateExpirationDate() {
        var currentDate = Calendar.getInstance();
        var fixedFutureDate = new GregorianCalendar(Constants.EXPIRATION_YEAR,
            Constants.EXPIRATION_MONTH, Constants.EXPIRATION_DAY);

        if (currentDate.after(fixedFutureDate)) {
            var msg = "This version of CodeOps Studio is outdated. Please download the latest "
                + "version from Git Hub: " + Constants.GITHUB_URL;
            throw new RuntimeException(msg);
        }
    }

    private boolean userHasConsentedToDataSharing() {
        return PreferencesUtils.canShareAnonymousStatistics();
    }

    @NonNull
    private CompletableFuture<Void> initializeEditorConfigurationsInBackground() {
        return CompletableFuture
            .runAsync(() -> {
                try {
                    ContextualCodeEditor.loadConfigurations(IdeApplication.this);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }, backgroundExecutor)
            .whenComplete((result, throwable) -> AsyncTask.runOnUiThread(() -> {
                if (throwable != null) {
                    crashlytics.recordException(throwable);
                    ILog.error(TAG, "Failed to load code editor configurations in background",
                        throwable);
                } else {
                    ILog.info(TAG, "Code editor configurations loaded successfully.");
                }
            }));
    }

    @Override
    public void uncaughtException(Thread thread, @NonNull Throwable throwable) {
        errorMessage.append(ThrowableUtils.getFullStackTrace(throwable));
        final var crashDate = Calendar
            .getInstance()
            .getTime()
            .toString();
        errorMessage
            .append(crashDate)
            .append(Constants.NEXT_LINE.repeat(2));

        crashlytics.setUserId(Wizard.getUserID(getGlobalContext()));
        CustomKeysAndValues keysAndValues = new CustomKeysAndValues.Builder()
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

        try {
            var restartIntent = new Intent(this, CrashActivity.class);
            restartIntent.putExtra("error", errorMessage.toString());
            restartIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(restartIntent);
            // allow crashlytics log reports completely
            scheduleProcessTermination();
        } catch (Exception e) {
            crashlytics.recordException(e);
            ILog.error(TAG, "Failed to restart", e);
        }
    }

    @NonNull
    public static String getArchitecture() {
        return SystemArchitecture.getArchitecture();
    }

    private void scheduleProcessTermination() {
        new Thread(() -> {
            try {
                Thread.sleep(SLEEP_DURATION);
            } catch (InterruptedException exception) {
                //InterruptedException ignored
            }
            Process.killProcess(Process.myPid());
            System.exit(1);
        }).start();
    }

    public static class SystemArchitecture {
        public static final String DEVICE_ARCHITECTURE_NOT_SUPPORTED = "Device Not Supported";

        private static final String ARM = "armeabi-v7a";
        private static final String AARCH64 = "arm64-v8a";
        private static final String I686 = "x86";
        private static final String X86_64 = "x86_64";

        @NonNull
        public static String getArchitecture() {
            if (isSupportedArch()) {
                if (supportsArm32Bit()) {
                    return ARM;
                } else if (supportsArm64Bit()) {
                    return AARCH64;
                } else if (supportsX86_32Bit()) {
                    return I686;
                } else if (supportsX86_64Bit()) {
                    return X86_64;
                }
            }
            return DEVICE_ARCHITECTURE_NOT_SUPPORTED;
        }

        public static boolean isSupportedArch() {
            return supportsArm32Bit() || supportsArm64Bit() || supportsX86_32Bit()
                || supportsX86_64Bit();
        }

        public static boolean supportsArm32Bit() {
            return Arrays
                .asList(Build.SUPPORTED_ABIS)
                .contains(ARM);
        }

        public static boolean supportsArm64Bit() {
            return Arrays
                .asList(Build.SUPPORTED_ABIS)
                .contains(AARCH64);
        }

        public static boolean supportsX86_32Bit() {
            return Arrays
                .asList(Build.SUPPORTED_ABIS)
                .contains(I686);
        }

        public static boolean supportsX86_64Bit() {
            return Arrays
                .asList(Build.SUPPORTED_ABIS)
                .contains(X86_64);
        }
    }
}
