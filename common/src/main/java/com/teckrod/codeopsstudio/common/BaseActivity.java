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

package com.eup.codeopsstudio.common;

import android.Manifest;
import android.app.AppOpsManager;
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
import androidx.core.content.ContextCompat;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.eup.codeopsstudio.common.util.SDKUtil;
import com.eup.codeopsstudio.res.R;

public class BaseActivity extends AppCompatActivity {

  public static final String MANAGE_EXTERNAL_STORAGE_PERMISSION = "android:manage_external_storage";
  public static final String NOT_APPLICABLE = "N/A";
  private static final String KEY_REQUEST_STORAGE_PERMISSION_API_30 = "0xf2ee";
  private static final String KEY_REQUEST_STORAGE_PERMISSION_API_19 = "0xf11e";

  private ActivityResultLauncher<Intent> requestStoragePermissionLauncherApi30;
  private ActivityResultLauncher<String[]> requestStoragePermissionLauncherApi19;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    requestStoragePermissionLauncherApi30 =
        getActivityResultRegistry()
            .register(
                KEY_REQUEST_STORAGE_PERMISSION_API_30,
                this,
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                  @Override
                  public void onActivityResult(ActivityResult result) {
                    if (result != null) {
                      if (!isStoragePermissionGrantedApi30()) {
                        showStoragePermissionDeniedDialog(
                            () -> {
                              requestStoragePermissionApi30();
                            },
                            () -> {
                              finishAffinity();
                              System.exit(0);
                            });
                      }
                    }
                  }
                });

    requestStoragePermissionLauncherApi19 =
        getActivityResultRegistry()
            .register(
                KEY_REQUEST_STORAGE_PERMISSION_API_30,
                this,
                new ActivityResultContracts.RequestMultiplePermissions(),
                isGranted -> {
                  if (isGranted.containsValue(false)) {
                    showStoragePermissionDeniedDialog(
                        () -> {
                          requestStoragePermissionApi19();
                        },
                        () -> {
                          finishAffinity();
                          System.exit(0);
                        });
                  }
                });
  }

  public void ensureStoragePermissionGranted() {
    if (!isStoragePermissionGranted()) {
      requestStoragePermission();
    }
  }

  public boolean isStoragePermissionGranted() {
    return (SDKUtil.isAtLeast(SDKUtil.API.ANDROID_11))
        ? isStoragePermissionGrantedApi30()
        : isStoragePermissionGrantedApi19();
  }

  public static String getStoragePermissionName() {
    return (SDKUtil.isAtLeast(SDKUtil.API.ANDROID_11))
        ? MANAGE_EXTERNAL_STORAGE_PERMISSION
        : Manifest.permission.READ_EXTERNAL_STORAGE.concat(", ")
            + Manifest.permission.WRITE_EXTERNAL_STORAGE;
  }

  public void openPermissionSettings() {
    if (SDKUtil.isAtLeast(SDKUtil.API.ANDROID_11)) {
      requestStoragePermissionApi30();
    } else {
      startActivity(
          new Intent(
              Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
              Uri.fromParts("package", getPackageName(), null)));
    }
  }

  public void requestStoragePermission() {
    if (SDKUtil.isAtLeast(SDKUtil.API.ANDROID_11)) {
      requestStoragePermissionApi30();
    } else {
      requestStoragePermissionApi19();
    }
  }

  @RequiresApi(30)
  private boolean isStoragePermissionGrantedApi30() {
    return Environment.isExternalStorageManager();
  }

  @RequiresApi(19)
  private boolean isStoragePermissionGrantedApi19() {
    int readStatus =
        ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
    int writeStatus =
        ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

    return readStatus == PackageManager.PERMISSION_GRANTED
        && writeStatus == PackageManager.PERMISSION_GRANTED;
  }

  @RequiresApi(30)
  private void requestStoragePermissionApi30() {
    try {
      Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
      Uri uri = Uri.fromParts("package", getPackageName(), null);
      intent.setData(uri);
      requestStoragePermissionLauncherApi30.launch(intent);
    } catch (Exception e) {
      Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
      requestStoragePermissionLauncherApi30.launch(intent);
    }
  }

  @RequiresApi(19)
  private void requestStoragePermissionApi19() {
    final String[] permissions =
        new String[] {
          Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE
        };
    requestStoragePermissionLauncherApi19.launch(permissions);
  }

  private void showStoragePermissionDeniedDialog(Runnable positiveAction, Runnable negativeAction) {
    new MaterialAlertDialogBuilder(this)
        .setTitle(R.string.storage_permission_denied)
        .setMessage(getString(R.string.storage_permission_denial_prompt, R.string.app_name))
        .setPositiveButton(
            R.string.storage_permission_request_again,
            (d, which) -> {
              if (positiveAction != null) {
                positiveAction.run();
              }
            })
        .setNegativeButton(
            R.string.exit,
            (d, which) -> {
              if (negativeAction != null) {
                negativeAction.run();
              }
            })
        .setCancelable(false)
        .show();
  }
}
