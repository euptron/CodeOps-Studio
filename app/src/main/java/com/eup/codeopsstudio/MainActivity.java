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

import android.Manifest;
import android.app.NotificationManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.splashscreen.SplashScreen;

import com.eup.codeopsstudio.common.ILog;
import com.eup.codeopsstudio.common.util.SDKUtil;
import com.eup.codeopsstudio.common.util.SDKUtil.API;
import com.eup.codeopsstudio.databinding.ActivityMainBinding;
import com.eup.codeopsstudio.observers.ContextualLifecycleObserver;
import com.eup.codeopsstudio.res.R;
import com.eup.codeopsstudio.util.BaseUtil;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.analytics.FirebaseAnalytics;

/**
 * Main UI host
 *
 * @author Etido Peter
 */
public class MainActivity extends AppCompatActivity {

    public static final String MANAGE_EXTERNAL_STORAGE_PERMISSION =
        "android" + ":manage_external_storage";
    public static final String NOT_APPLICABLE = "N/A";
    public static final String TAG = MainActivity.class.getSimpleName();
    private static final String KEY_REQUEST_STORAGE_PERMISSION_API_30 = "0xf2ee";
    private static final String KEY_REQUEST_STORAGE_PERMISSION_API_19 = "0xf11e";
    private static final String KEY_REQUEST_NOTIFICATION_PERMISSION_API_30 = "0xf23e";

    private ActivityMainBinding binding;
    private ContextualLifecycleObserver lifecycleObserver;
    private ActivityResultLauncher<Intent> requestStoragePermissionLauncherApi30;
    private ActivityResultLauncher<String[]> requestStoragePermissionLauncherApi19;
    private ActivityResultLauncher<String> requestNotificationPermissionLauncherApi33;

    private FirebaseAnalytics mFirebaseAnalytics;

    public static String getStoragePermissionName() {
        return (SDKUtil.isAtLeast(API.ANDROID_11)) ? MANAGE_EXTERNAL_STORAGE_PERMISSION
            : Manifest.permission.READ_EXTERNAL_STORAGE.concat(", ")
                + Manifest.permission.WRITE_EXTERNAL_STORAGE;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        lifecycleObserver = new ContextualLifecycleObserver(this, getActivityResultRegistry(),
            this);
        getLifecycle().addObserver(lifecycleObserver);
        // Obtain the FirebaseAnalytics instance.
        // The SDK can now launchWithLocalHost automatically logging some events and user
        // properties; you don't
        // have to add any additional code to enable this logging.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        requestStoragePermissionLauncherApi30 =
            getActivityResultRegistry().register(KEY_REQUEST_STORAGE_PERMISSION_API_30, this,
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result != null) {
                    if (!isStoragePermissionGrantedApi30()) {
                        showStoragePermissionDeniedDialog(() -> {
                            requestStoragePermissionApi30();
                        }, () -> {
                            finishAffinity();
                            System.exit(0);
                        });
                    }
                }
            }
        });

        requestStoragePermissionLauncherApi19 =
            getActivityResultRegistry().register(KEY_REQUEST_STORAGE_PERMISSION_API_19, this,
                new ActivityResultContracts.RequestMultiplePermissions(), isGranted -> {
            if (isGranted.containsValue(false)) {
                showStoragePermissionDeniedDialog(() -> {
                    requestStoragePermissionApi19();
                }, () -> {
                    finishAffinity();
                    System.exit(0);
                });
            }
        });

        requestNotificationPermissionLauncherApi33 =
            getActivityResultRegistry().register(KEY_REQUEST_NOTIFICATION_PERMISSION_API_30, this
                , new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (Boolean.TRUE.equals(isGranted)) {
                BaseUtil.toastLong(R.string.msg_notification_permission_granted);
            } else {
                if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                    showNotificationPermissionRationale();
                } else {
                    showNotificationSettingsRationale();
                }
            }
        });
        showMainFragment();
    }

    private void showMainFragment() {
        if (getSupportFragmentManager().findFragmentByTag(MainFragment.TAG) == null) {
            getSupportFragmentManager()
                .beginTransaction()
                .add(com.eup.codeopsstudio.R.id.fragment_container, MainFragment.newInstance(),
                    MainFragment.TAG)
                .commit();
        }
    }

    @RequiresApi(30)
    private boolean isStoragePermissionGrantedApi30() {
        return
            (ActivityCompat.checkSelfPermission(this,
                Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                == PackageManager.PERMISSION_GRANTED) || (
                ActivityCompat.checkSelfPermission(this,
                    Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                    == PackageManager.PERMISSION_GRANTED) || Environment.isExternalStorageManager();
    }

    @RequiresApi(30)
    private void requestStoragePermissionApi30() {
        try {
            Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
            Uri uri = Uri.fromParts("package", getPackageName(), null);
            intent.setData(uri);
            requestStoragePermissionLauncherApi30.launch(intent);
        } catch (ActivityNotFoundException anfe) {
            try {
                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                requestStoragePermissionLauncherApi30.launch(intent);
            } catch (Exception e) {
                BaseUtil.toastLong(R.string.storage_permission_denied);
            }
        } catch (Exception e) {
            ILog.error(TAG, "Failed to request permission to grant access to all files", e);
            BaseUtil.toastLong(R.string.storage_permission_denied);
        }
    }

    @RequiresApi(19)
    private void requestStoragePermissionApi19() {
        final String[] permissions = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE
        };
        requestStoragePermissionLauncherApi19.launch(permissions);
    }

    private void showStoragePermissionDeniedDialog(Runnable positiveAction,
        Runnable negativeAction) {
        new MaterialAlertDialogBuilder(this)
            .setTitle(R.string.storage_permission_denied)
            .setMessage(getString(R.string.storage_permission_denial_prompt, R.string.app_name))
            .setPositiveButton(R.string.storage_permission_request_again, (d, which) -> {
                if (positiveAction != null) {
                    positiveAction.run();
                }
            })
            .setNegativeButton(R.string.exit, (d, which) -> {
                if (negativeAction != null) {
                    negativeAction.run();
                }
            })
            .setCancelable(false)
            .show();
    }

    private void showNotificationSettingsRationale() {
        new MaterialAlertDialogBuilder(this)
            .setTitle(R.string.msg_grant_notification_permission)
            .setMessage(R.string.msg_request_notification_rationale)
            .setPositiveButton(R.string.ok_turn_on, (d, which) -> {
                launchDeviceSettingsActivity(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
            })
            .setNegativeButton(R.string.cancel, null)
            .setCancelable(false)
            .show();
    }

    private void launchDeviceSettingsActivity(String section) {
        startActivity(new Intent(section, Uri.fromParts("package", getPackageName(), null)));
    }

    private void showNotificationPermissionRationale() {
        new MaterialAlertDialogBuilder(this)
            .setTitle(R.string.msg_grant_notification_permission)
            .setMessage(R.string.msg_request_notification_rationale)
            .setPositiveButton(R.string.ok, (d, which) -> {
                requestNotificationPermissionLauncherApi33.launch(Manifest.permission.POST_NOTIFICATIONS);
            })
            .setNegativeButton(R.string.cancel, null)
            .setCancelable(false)
            .show();
    }

    /**
     * Returns {@code true} if color harmonization is enabled.
     */
    public boolean isColorHarmonizationEnabled() {
        return false;
    }

    public ContextualLifecycleObserver getLifecycleObserver() {
        return this.lifecycleObserver;
    }

    public void ensureStoragePermissionGranted() {
        if (!isStoragePermissionGranted()) {
            requestStoragePermission();
        }
    }

    public boolean isStoragePermissionGranted() {
        return (SDKUtil.isAtLeast(API.ANDROID_11)) ? isStoragePermissionGrantedApi30()
            : isStoragePermissionGrantedApi19();
    }

    @RequiresApi(19)
    private boolean isStoragePermissionGrantedApi19() {
        int readStatus = ContextCompat.checkSelfPermission(this,
            Manifest.permission.READ_EXTERNAL_STORAGE);
        int writeStatus = ContextCompat.checkSelfPermission(this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE);

        return readStatus == PackageManager.PERMISSION_GRANTED
            && writeStatus == PackageManager.PERMISSION_GRANTED;
    }

    public void requestStoragePermission() {
        if (SDKUtil.isAtLeast(API.ANDROID_11)) {
            requestStoragePermissionApi30();
        } else {
            requestStoragePermissionApi19();
        }
    }

    public void openPermissionSettings() {
        if (SDKUtil.isAtLeast(API.ANDROID_11)) {
            requestStoragePermissionApi30();
        } else {
            launchDeviceSettingsActivity();
        }
    }

    private void launchDeviceSettingsActivity() {
        launchDeviceSettingsActivity(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
    }

    public void ensureNotificationPermissionGranted() {
        if (SDKUtil.isAtLeast(API.ANDROID_13)) {
            if (!isNotificationPermissionGranted()) {
                requestNotificationPermission();
            } else {
                if (!areNotificationsAllowed()) {
                    showNotificationSettingsRationale();
                }
            }
        } else {
            if (!areNotificationsAllowed()) {
                showNotificationSettingsRationale();
            }
        }
    }

    @RequiresApi(33)
    private boolean isNotificationPermissionGranted() {
        int grantStatus = ContextCompat.checkSelfPermission(this,
            Manifest.permission.POST_NOTIFICATIONS);
        return grantStatus == PackageManager.PERMISSION_GRANTED;
    }

    private boolean areNotificationsAllowed() {
        NotificationManager notificationManager =
            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (SDKUtil.isAtLeast(API.ANDROID_7)) {
            return notificationManager.areNotificationsEnabled();
        } else {
            return true;
        }
    }

    @RequiresApi(33)
    private void requestNotificationPermission() {
        if (SDKUtil.isAtLeast(API.ANDROID_13)) {
            try {
                requestNotificationPermissionLauncherApi33.launch(Manifest.permission.POST_NOTIFICATIONS);
            } catch (ActivityNotFoundException e) {
                BaseUtil.toastLong(R.string.msg_no_handle_activity_found);
            }
        }
    }
}
