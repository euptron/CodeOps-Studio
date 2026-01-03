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

package com.eup.codeopsstudio.ui.fcm;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.app.NotificationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.eup.codeopsstudio.R;
import com.eup.codeopsstudio.common.AsyncTask;
import com.eup.codeopsstudio.common.Constants;
import com.eup.codeopsstudio.common.ILog;
import com.eup.codeopsstudio.common.util.PreferencesUtils;
import com.eup.codeopsstudio.databinding.BottomSheetUpdateBinding;
import com.eup.codeopsstudio.util.BaseUtil;
import com.eup.codeopsstudio.util.Wizard;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import java.io.File;

/**
 * In-App update UI
 *
 * @author Etido Peter
 */
public class UpdateBottomSheet extends BottomSheetDialogFragment {

  public static final String TAG = "UpdateBottomSheet";

  private boolean installedApp = false;

  private String version;
  private long downloadId;
  private String changelog;
  private String minVersion;
  private String downloadUrl;
  private boolean forceUpdate;
  private String downloadSize;
  private DownloadManager downloadManager;
  private BottomSheetUpdateBinding binding;
  private BroadcastReceiver downloadCompleteReceiver;
  private ActivityResultLauncher<Intent> installPermissionLauncher;

  public static UpdateBottomSheet newInstance(
      String minVersion,
      String version,
      String changelog,
      String downloadUrl,
      boolean forceUpdate,
      String downloadSize) {
    UpdateBottomSheet fragment = new UpdateBottomSheet();
    Bundle args = new Bundle();
    args.putString("version", version);
    args.putString("changelog", changelog);
    args.putString("min_version", minVersion);
    args.putString("download_url", downloadUrl);
    args.putBoolean("force_update", forceUpdate);
    args.putString("download_size", downloadSize);
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if (getArguments() != null) {
      version = getArguments().getString("version");
      minVersion = getArguments().getString("min_version");
      downloadUrl = getArguments().getString("download_url");
      downloadSize = getArguments().getString("download_size");
      forceUpdate = getArguments().getBoolean("force_update", false);
      changelog = getArguments().getString("changelog", getString(R.string.default_changelog));
    }

    ILog.debug(TAG, "Update Check: Version=" + version + ", URL=" + downloadUrl);

    if (forceUpdate) {
      setCancelable(false);
    }

    installPermissionLauncher =
        registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
              if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (requireContext().getPackageManager().canRequestPackageInstalls()) {
                  installUpdate();
                } else {
                  BaseUtil.toastShort("Permission denied. Update cannot be installed.");
                }
              }
            });
  }

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    binding = BottomSheetUpdateBinding.inflate(inflater, container, false);
    return binding.getRoot();
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    setupContent();
    setupListeners();
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    try {
      if (downloadCompleteReceiver != null) {
        requireContext().unregisterReceiver(downloadCompleteReceiver);
      }
    } catch (IllegalArgumentException e) {
      // Receiver was not registered, ignore
    }
  }

  @Override
  public void onDismiss(@NonNull DialogInterface dialog) {
    super.onDismiss(dialog);
    if (!installedApp) {
      remindMe();
    }

    if (!forceUpdate && downloadId == 0) {
      clearAllUpdateData(false);
    }
  }

  private void setupContent() {
    if (Wizard.allNotNull(version, changelog)) {
      String displayLog = Wizard.formatToBulletList(",", changelog);
      String versionText = String.format(getString(R.string.version_format), version);

      if (!Wizard.isEmpty(downloadSize)) {
        versionText += " – " + downloadSize;
      }

      binding.versionText.setText(versionText);
      binding.changelogText.setText(displayLog);
    }

    if (forceUpdate) {
      binding.laterBtn.setVisibility(View.GONE);
    }
  }

  private void setupListeners() {
    binding.updateBtn.setOnClickListener(v -> startDownload());
    binding.laterBtn.setOnClickListener(v -> dismiss());
  }

  private void remindMe() {
    SharedPreferences prefs = PreferencesUtils.getAppUpdatePreferences();
    if (!Wizard.allNotNullAndEmpty(downloadUrl, version)) {
      return;
    }

    long lastRemindTime = prefs.getLong(Constants.PREF_LAST_REMIND_TIME, 0);
    long currentTime = System.currentTimeMillis();

    if (currentTime - lastRemindTime < Constants.REMIND_INTERVAL_MS) {
      return; // Too soon, don't remind
    }

    SharedPreferences.Editor editor =
        prefs
            .edit()
            .putString(Constants.PREF_UPDATE_VERSION, version)
            .putString(Constants.PREF_UPDATE_DOWNLOAD_URL, downloadUrl)
            .putString(Constants.PREF_UPDATE_CHANGELOG, changelog)
            .putLong(Constants.PREF_LAST_REMIND_TIME, currentTime)
            .putString(Constants.PREF_UPDATE_FORCED, String.valueOf(forceUpdate))
            .putString(Constants.PREF_UPDATE_DOWNLOAD_SIZE, downloadSize);

    if (!Wizard.isEmpty(minVersion)) {
      editor.putString(Constants.PREF_UPDATE_MIN_VERSION, minVersion);
    }

    editor.apply();
  }

  private void startDownload() {
    if (downloadUrl == null || downloadUrl.isEmpty()) {
      BaseUtil.toastShort(R.string.invalid_download_url);
      return;
    }

    binding.updateBtn.setEnabled(false);
    binding.updateBtn.setText(getString(R.string.downloading));
    binding.progressIndicator.setVisibility(View.VISIBLE);

    downloadManager = (DownloadManager) requireContext().getSystemService(Context.DOWNLOAD_SERVICE);

    String fileName = Constants.APP_FILE_PREFIX + version + Constants.APK_FILE_EXTENSION;

    DownloadManager.Request request =
        new DownloadManager.Request(Uri.parse(downloadUrl))
            .setTitle(getString(R.string.app_name) + " Update")
            .setDescription(getString(R.string.downloading_version, version))
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);

    request.setRequiresCharging(false);
    downloadId = downloadManager.enqueue(request);

    // Register receiver for download completion
    downloadCompleteReceiver =
        new BroadcastReceiver() {
          @Override
          public void onReceive(Context context, Intent intent) {
            long receivedId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            if (receivedId == downloadId) {
              requireActivity()
                  .runOnUiThread(
                      () -> {
                        binding.progressIndicator.setProgressCompat(100, true);
                        binding.updateBtn.setText(getString(R.string.install));
                        binding.updateBtn.setEnabled(true);
                        binding.updateBtn.setOnClickListener(v -> installUpdate());
                      });

              requireContext().unregisterReceiver(downloadCompleteReceiver);
            }
          }
        };

    requireContext()
        .registerReceiver(
            downloadCompleteReceiver,
            new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
            Context.RECEIVER_NOT_EXPORTED);
    monitorDownloadProgress();
  }

  private void monitorDownloadProgress() {
    AsyncTask.runOnBackgroundThread(
        () -> {
          boolean downloading = true;

          while (downloading) {
            if (!isAdded() || getActivity() == null) {
              break;
            }

            DownloadManager.Query query = new DownloadManager.Query();
            query.setFilterById(downloadId);

            try (android.database.Cursor cursor = downloadManager.query(query)) {
              if (cursor.moveToFirst()) {
                int statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
                int status = cursor.getInt(statusIndex);

                if (status == DownloadManager.STATUS_SUCCESSFUL) {
                  downloading = false;

                  AsyncTask.runOnUiThread(
                      () -> {
                        if (isAdded()) {
                          binding.progressIndicator.setProgressCompat(100, true);
                          binding.updateBtn.setText(getString(R.string.install));
                          binding.updateBtn.setEnabled(true);
                          binding.updateBtn.setOnClickListener(v -> installUpdate());
                        }
                      });

                } else if (status == DownloadManager.STATUS_RUNNING) {
                  int bytesDownloadedIndex =
                      cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR);
                  int bytesTotalIndex =
                      cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES);

                  int bytesDownloaded = cursor.getInt(bytesDownloadedIndex);
                  int bytesTotal = cursor.getInt(bytesTotalIndex);

                  if (bytesTotal > 0) {
                    int progress = (int) ((bytesDownloaded * 100L) / bytesTotal);

                    AsyncTask.runOnUiThread(
                        () -> {
                          if (isAdded()) {
                            binding.progressIndicator.setProgressCompat(progress, true);
                          }
                        });
                  }

                } else if (status == DownloadManager.STATUS_FAILED) {
                  downloading = false;

                  AsyncTask.runOnUiThread(
                      () -> {
                        if (isAdded()) {
                          BaseUtil.toastShort(R.string.download_failed);
                          binding.updateBtn.setText(getString(R.string.retry_download));
                          binding.updateBtn.setEnabled(true);
                          binding.updateBtn.setOnClickListener(v -> startDownload());
                        }
                      });
                }
              }
            } catch (Exception e) {
              ILog.error(TAG, "Error monitoring download", e);
              break;
            }

            // Sleep to prevent CPU spinning
            try {
              Thread.sleep(500);
            } catch (InterruptedException e) {
              Thread.currentThread().interrupt();
              break;
            }
          }
        });
  }

  private void installUpdate() {
    if (!isAdded() || getContext() == null) return;

    Uri uri = downloadManager.getUriForDownloadedFile(downloadId);

    if (uri == null) {
      BaseUtil.toastShort("Update file not found");
      return;
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      if (!requireContext().getPackageManager().canRequestPackageInstalls()) {
        BaseUtil.toastLong("Please allow permission to install update");

        try {
          Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
          intent.setData(Uri.parse("package:" + requireContext().getPackageName()));
          installPermissionLauncher.launch(intent);
        } catch (Exception e) {
          ILog.error(TAG, "Error opening settings", e);
          BaseUtil.toastShort("Could not open settings");
        }
        dismiss();
        return;
      }
    }

    Wizard.installApplication(
        requireContext(),
        uri,
        () -> {
          installedApp = true;
          clearAllUpdateData(true);
          dismiss();
        });
  }

  private void clearAllUpdateData(boolean andPref) {
    Context context = requireContext();

    if (andPref) {
      SharedPreferences prefs = PreferencesUtils.getAppUpdatePreferences();

      prefs
          .edit()
          .remove(Constants.PREF_UPDATE_VERSION)
          .remove(Constants.PREF_UPDATE_DOWNLOAD_URL)
          .remove(Constants.PREF_UPDATE_DOWNLOAD_SIZE)
          .remove(Constants.PREF_UPDATE_CHANGELOG)
          .remove(Constants.PREF_UPDATE_FORCED)
          .remove(Constants.PREF_UPDATE_CHECK_TIME)
          .remove(Constants.PREF_UPDATE_MIN_VERSION)
          .remove(Constants.PREF_LAST_REMIND_TIME)
          .apply();
    }

    Intent intent = requireActivity().getIntent();
    if (intent != null) {
      Wizard.removeIntentExtras(
          intent,
          Constants.FCM_NOTIFICATION_TYPE,
          Constants.KEY_UPDATE_VERSION,
          Constants.KEY_DOWNLOAD_URL,
          Constants.KEY_FORCE_UPDATE,
          Constants.KEY_CHANGELOG,
          Constants.KEY_UPDATE_DOWNLOAD_SIZE,
          Constants.KEY_MIN_VERSION);
    }

    clearDownloadedUpdateFiles();
    NotificationManager notificationManager =
        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    notificationManager.cancel(Constants.NOTIFICATION_ID_APP_UPDATE);
    ILog.debug(TAG, "Cleared all update data after installation");
  }

  private void clearDownloadedUpdateFiles() {
    try {
      File downloadsDir =
          Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
      if (downloadsDir.exists() && downloadsDir.isDirectory()) {
        File[] files =
            downloadsDir.listFiles(
                (dir, name) ->
                    name.startsWith(Constants.APP_FILE_PREFIX)
                        && name.endsWith(Constants.APK_FILE_EXTENSION));

        if (files != null) {
          for (File file : files) {
            if (file.delete()) {
              ILog.debug(TAG, "Deleted update file: " + file.getName());
            }
          }
        }
      }
    } catch (Exception e) {
      ILog.error(TAG, "Error clearing downloaded update files", e);
    }
  }
}
