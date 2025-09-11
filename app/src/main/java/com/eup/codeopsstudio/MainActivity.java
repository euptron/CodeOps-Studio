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
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.splashscreen.SplashScreen;

import com.eup.codeopsstudio.common.ILog;
import com.eup.codeopsstudio.databinding.ActivityMainBinding;
import com.eup.codeopsstudio.observers.ContextualLifecycleObserver;
import com.eup.codeopsstudio.res.R;
import com.eup.codeopsstudio.util.BaseUtil;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/**
 * Main UI host
 *
 * @author Etido Peter
 */
public class MainActivity extends AppCompatActivity {

    public static final String MANAGE_EXTERNAL_STORAGE_PERMISSION =
        "android" + ":manage_external_storage";
    public static final String TAG = MainActivity.class.getSimpleName();
    private static final String KEY_REQUEST_STORAGE_PERMISSION_API_30 = "0xf2ee";
    private static final String KEY_REQUEST_STORAGE_PERMISSION_API_19 = "0xf11e";
    private static final String KEY_REQUEST_NOTIFICATION_PERMISSION_API_33 = "0xf23e";

    private ContextualLifecycleObserver lifecycleObserver;
    private ActivityResultLauncher<Intent> requestStoragePermissionLauncherApi30;
    private ActivityResultLauncher<String[]> requestStoragePermissionLauncherApi19;
    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    private ActivityResultLauncher<String> requestNotificationPermissionLauncherApi33;

    public static String getStoragePermissionName() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.R ? MANAGE_EXTERNAL_STORAGE_PERMISSION
            : Manifest.permission.READ_EXTERNAL_STORAGE.concat(", ")
                + Manifest.permission.WRITE_EXTERNAL_STORAGE;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        lifecycleObserver = new ContextualLifecycleObserver(this, getActivityResultRegistry(),
            this);
        getLifecycle().addObserver(lifecycleObserver);

        requestStoragePermissionLauncherApi30 =
            getActivityResultRegistry().register(KEY_REQUEST_STORAGE_PERMISSION_API_30, this,
                new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    if (!isStoragePermissionGrantedApi30()) {
                        showStoragePermissionDeniedDialog(this::requestStoragePermissionApi30,
                            () -> {
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
                showStoragePermissionDeniedDialog(this::requestStoragePermissionApi19, () -> {
                    finishAffinity();
                    System.exit(0);
                });
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotificationPermissionLauncherApi33 =
                getActivityResultRegistry().register(KEY_REQUEST_NOTIFICATION_PERMISSION_API_33,
                    this, new ActivityResultContracts.RequestPermission(), isGranted -> {
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
        }

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

    private void showStoragePermissionDeniedDialog(Runnable positiveAction,
        Runnable negativeAction) {
        new MaterialAlertDialogBuilder(this)
            .setTitle(R.string.storage_permission_denied)
            .setMessage(getString(R.string.storage_permission_denial_prompt,
                getString(R.string.app_name)))
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
            .setPositiveButton(R.string.ok_turn_on,
                (d, which) -> launchDeviceSettingsActivity(Settings.ACTION_APP_NOTIFICATION_SETTINGS))
            .setNegativeButton(R.string.cancel, null)
            .setCancelable(false)
            .show();
    }

    private void launchDeviceSettingsActivity(String section) {
        String packageName = getPackageName();
        ILog.debug(TAG, "Package Name for settings: " + packageName);
        if (packageName == null || packageName.isEmpty()) {
            ILog.error(TAG, "Package name is null or empty. Cannot launch settings.");
            BaseUtil.toastLong("Error: Could not determine package name.");
            return;
        }

        try {
            startActivity(new Intent(section, Uri.fromParts("package", getPackageName(), null)));
        } catch (ActivityNotFoundException e) {
            var msg = "Could not open " + section;
            ILog.error(TAG, msg);
            BaseUtil.toastLong("Error: " + msg);

            if (!section.equals(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)) {
                launchDeviceSettingsActivity();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    private void showNotificationPermissionRationale() {
        new MaterialAlertDialogBuilder(this)
            .setTitle(R.string.msg_grant_notification_permission)
            .setMessage(R.string.msg_request_notification_rationale)
            .setPositiveButton(R.string.ok, (d, which) -> requestNotificationPermission())
            .setNegativeButton(R.string.cancel, null)
            .setCancelable(false)
            .show();
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
        return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) ? isStoragePermissionGrantedApi30()
            : isStoragePermissionGrantedApi19();
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

    private boolean isStoragePermissionGrantedApi19() {
        int readStatus = ContextCompat.checkSelfPermission(this,
            Manifest.permission.READ_EXTERNAL_STORAGE);
        int writeStatus = ContextCompat.checkSelfPermission(this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE);

        return readStatus == PackageManager.PERMISSION_GRANTED
            && writeStatus == PackageManager.PERMISSION_GRANTED;
    }

    public void requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            requestStoragePermissionApi30();
        } else {
            requestStoragePermissionApi19();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    private void requestStoragePermissionApi30() {
        try {
            Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
            Uri uri = Uri.fromParts("package", getPackageName(), null);
            intent.setData(uri);
            requestStoragePermissionLauncherApi30.launch(intent);
        } catch (ActivityNotFoundException ignored) {
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

    private void requestStoragePermissionApi19() {
        final String[] permissions = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE
        };
        requestStoragePermissionLauncherApi19.launch(permissions);
    }

    public void openStoragePermissionSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            requestStoragePermissionApi30();
        } else {
            launchDeviceSettingsActivity();
        }
    }

    private void launchDeviceSettingsActivity() {
        launchDeviceSettingsActivity(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
    }

    public void ensureNotificationPermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (isNotificationPermissionGranted()) {
                showNotificationSettingsRationaleIfAllowed();
            } else {
                requestNotificationPermission();
            }
        } else {
            showNotificationSettingsRationaleIfAllowed();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    private boolean isNotificationPermissionGranted() {
        int grantStatus = ContextCompat.checkSelfPermission(this,
            Manifest.permission.POST_NOTIFICATIONS);
        return grantStatus == PackageManager.PERMISSION_GRANTED;
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    private void requestNotificationPermission() {
        try {
            requestNotificationPermissionLauncherApi33.launch(Manifest.permission.POST_NOTIFICATIONS);
        } catch (ActivityNotFoundException e) {
            BaseUtil.toastLong(R.string.msg_no_handle_activity_found);
        }
    }

    private void showNotificationSettingsRationaleIfAllowed() {
        if (areNotificationsAllowed()) {
            ILog.debug(TAG, "Notifications allowed");
        } else {
            showNotificationSettingsRationale();
        }
    }

    private boolean areNotificationsAllowed() {
        NotificationManager notificationManager =
            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        return notificationManager.areNotificationsEnabled();
    }
}
