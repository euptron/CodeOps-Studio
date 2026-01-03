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

package com.eup.codeopsstudio.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.work.BackoffPolicy;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import com.eup.codeopsstudio.MainActivity;
import com.eup.codeopsstudio.R;
import com.eup.codeopsstudio.common.AsyncTask;
import com.eup.codeopsstudio.common.Constants;
import com.eup.codeopsstudio.common.ILog;
import com.eup.codeopsstudio.common.util.PreferencesUtils;
import com.eup.codeopsstudio.models.user.DeviceInfo;
import com.eup.codeopsstudio.service.fcm.FCMWorker;
import com.eup.codeopsstudio.util.Wizard;
import com.eup.codeopsstudio.util.versioning.VersionManager;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.bumptech.glide.Glide;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

/**
 * Firebase Cloud Messaging service handler that processes push notifications with advanced
 * targeting capabilities for CodeOps Studio. This service handles message reception, token
 * management, and notification display based on device, user, and application-specific criteria.
 *
 * <p>This service extends {@link FirebaseMessagingService} to receive and process FCM messages with
 * sophisticated filtering. Notifications are only shown if they match the user's device
 * characteristics based on targeting parameters.
 *
 * <p><b>Key features:</b>
 *
 * <ul>
 *   <li>Targeted notification delivery based on multiple criteria
 *   <li>Special handling for app update notifications with version checking
 *   <li>Device information collection for server-side targeting
 *   <li>Support for immediate tasks and long-running background tasks via WorkManager
 *   <li>Token management and persistence
 * </ul>
 *
 * <p><b>Supported Targeting Parameters:</b> (All parameters are optional)
 *
 * <ul>
 *   <li><code>target_country</code> - ISO country codes, comma-separated (e.g., "NG,GH,KE")
 *   <li><code>target_user_id</code> - User IDs, comma-separated (e.g., "user123,vip456")
 *   <li><code>target_android_version</code> - Version with operators (">=8", "<=10", "8-10")
 *   <li><code>target_app_version</code> - App version code with operators (">=100", "<=200",
 *       "100-200")
 *   <li><code>target_locale</code> - Language codes, comma-separated (e.g., "en,fr,es")
 * </ul>
 *
 * <p><b>Special Parameters:</b>
 *
 * <ul>
 *   <li><code>notification_type</code> - If set it is executed as a long running task e.g if set to
 *       "app_update", triggers special update handling
 * </ul>
 *
 * <p><b>App Update Notification Requirements:</b>
 *
 * <pre>
 * {
 *   "notification_type": "app_update",
 *   "version": "2.1.0",          // Display version
 *   "download_url": "...",       // APK download URL
 *   "changelog": "...",          // Optional: update description
 *   "force_update": "true",      // Optional: forces update
 *   "min_version": "1.1.0",         // Optional: minimum version code
 *   "image_url":  "..."           // Optional: Image url
 *   "download_size":  "10MB"      // Optional: Download size
 * }
 * </pre>
 *
 * <p><b>Minimal Working Example:</b>
 *
 * <pre>
 * {
 *   "data": {
 *     "title": "Notification Title",
 *     "body": "Notification body text",
 *     "target_country": "NG,ZA,GH",
 *     "target_android_version": ">=10"
 *   }
 * }
 * </pre>
 *
 * <pre>
 * {
 *   "data": {
 *     "title": "Notification Title",
 *     "body": "Notification body text",
 *     "target_android_version": ">=10"
 *   }
 * }
 * </pre>
 *
 * <pre>
 * {
 *   "data": {
 *     "title": "Notification Title",
 *     "body": "Notification body text",
 *   }
 * }
 * </pre>
 *
 * <pre>
 * {
 *   "data": {
 *     "title": "Notification Title",
 *     "body": "Notification body text",
 *     "image_url": "https://someurl/someimage.jpg"
 *   }
 * }
 * </pre>
 *
 * <p><b>Notification Flow:</b>
 *
 * <ol>
 *   <li>Message received from FCM
 *   <li>Check targeting parameters against device/user info
 *   <li>If <code>notification_type="app_update"</code>, handle update logic
 *   <li>If <code>long_task="true"</code>, schedule WorkManager job
 *   <li>Otherwise, show notification immediately
 * </ol>
 *
 * <p><b>Note:</b> Custom data parameters beyond the supported targeting fields are passed through
 * to the notification intent but not used for filtering logic.
 *
 * @author Etido Peter
 * @version 1.0
 * @since 2024
 * @see FirebaseMessagingService
 * @see FCMWorker
 * @see DeviceInfo
 * @see NotificationCompat
 * @see WorkManager
 */
public class BackgroundMessagingService extends FirebaseMessagingService {

  private static final String TAG = "BackgroundMessagingService";
  private static final String PREF_NAME = "fcm_targeting_prefs";
  private static final String KEY_FCM_TOKEN = "fcm_token";

  @Override
  public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
    ILog.debug(TAG, "From: " + remoteMessage.getFrom());

    if (!shouldShowNotification(remoteMessage)) {
      ILog.debug(TAG, "Notification filtered - not targeted to this user");
      return;
    }

    handleNotifications(remoteMessage);
  }

  @Override
  public void onNewToken(@NonNull String token) {
    super.onNewToken(token);
    ILog.debug(TAG, "Refreshed token: " + token);

    SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
    prefs.edit().putString(KEY_FCM_TOKEN, token).apply();

    sendRegistrationToServer(token);
  }

  private void handleNotifications(RemoteMessage remoteMessage) {
    if (remoteMessage == null) return;
    Map<String, String> data = remoteMessage.getData();
    if (data == null) return;
    Context ctx = getApplicationContext();

    boolean isUpdateNotification = false;
    String notificationType = data.get(Constants.FCM_NOTIFICATION_TYPE);

    if (!data.isEmpty()) {
      if (Constants.NOTIFICATION_TYPE_APP_UPDATE.equals(notificationType)) {
        isUpdateNotification = true;
      }

      boolean needsLongRunningTask =
          data.containsKey(Constants.FCM_NOTIFICATION_TYPE)
              && !Wizard.isEmpty(data.get(Constants.FCM_NOTIFICATION_TYPE));
      if (needsLongRunningTask) {
        scheduleJob(data);
      } else {
        handleNow(data);
      }
    }

    if (isUpdateNotification && !isUpdateRequired(data)) {
      return;
    }

    String imageUrl = null;
    if (remoteMessage.getNotification() != null) {
      Uri imageUri = remoteMessage.getNotification().getImageUrl();
      if (imageUri != null) {
        imageUrl = imageUri.toString();
      }
    }

    if (Wizard.isEmpty(imageUrl) && !Wizard.isEmpty(data)) {
      if (data.containsKey(Constants.FCM_IMAGE_URL)) {
        imageUrl = data.get(Constants.FCM_IMAGE_URL);
      }
    }

    String version =
        isUpdateNotification
            ? data.get(Constants.KEY_UPDATE_VERSION)
            : Wizard.getAppVersionName(ctx);
    int notificationID = isUpdateNotification ? Constants.NOTIFICATION_ID_APP_UPDATE : timeMask();
    int pendingIntentID = isUpdateNotification ? Constants.NOTIFICATION_ID_APP_UPDATE : timeMask();
    String channelId =
        isUpdateNotification
            ? Constants.CHANNEL_ID_APP_UPDATES
            : getString(R.string.cloud_messaging_notification_channel_id);
    String channelName = isUpdateNotification ? "App Updates" : "General Notifications";
    var channelImportance =
        isUpdateNotification
            ? NotificationManager.IMPORTANCE_HIGH
            : NotificationManager.IMPORTANCE_DEFAULT;
    String notificationTitle =
        isUpdateNotification
            ? getString(R.string.new_update_available)
            : getNotificationTitle(remoteMessage);
    String notificationMessage =
        isUpdateNotification
            ? getString(R.string.version_ready, version)
            : getNotificationBody(remoteMessage);

    Intent intent = new Intent(this, MainActivity.class);
    boolean forceUpdate = false;

    if (isUpdateNotification) {
        String minUpdateVersion = data.get(Constants.KEY_MIN_VERSION);
      String downloadUrl = data.get(Constants.KEY_DOWNLOAD_URL);
      String changelog = data.get(Constants.KEY_CHANGELOG);
      String downloadSize = data.get(Constants.PREF_UPDATE_DOWNLOAD_SIZE);
      forceUpdate = "true".equals(data.get(Constants.KEY_FORCE_UPDATE));

      SharedPreferences prefs = PreferencesUtils.getAppUpdatePreferences();

      prefs
          .edit()
          .putString(Constants.PREF_UPDATE_VERSION, version)
          .putString(Constants.PREF_UPDATE_DOWNLOAD_URL, downloadUrl)
          .putString(Constants.PREF_UPDATE_CHANGELOG, changelog)
          .putString(Constants.PREF_UPDATE_DOWNLOAD_SIZE, downloadSize)
          .putString(Constants.PREF_UPDATE_FORCED, String.valueOf(forceUpdate));
      if (!Wizard.isEmpty(minUpdateVersion)) {
        prefs.edit().putString(Constants.PREF_UPDATE_MIN_VERSION, minUpdateVersion);
      }
      prefs.edit().commit();

      intent.putExtra(Constants.FCM_NOTIFICATION_TYPE, Constants.NOTIFICATION_TYPE_APP_UPDATE);
      intent.putExtra(Constants.KEY_UPDATE_VERSION, version);
      intent.putExtra(Constants.KEY_DOWNLOAD_URL, downloadUrl);
      intent.putExtra(Constants.KEY_UPDATE_DOWNLOAD_SIZE, downloadSize);
      intent.putExtra(Constants.KEY_FORCE_UPDATE, String.valueOf(forceUpdate));
      intent.putExtra(Constants.KEY_CHANGELOG, changelog);
      if (!Wizard.isEmpty(minUpdateVersion)) {
        intent.putExtra(Constants.KEY_MIN_VERSION, minUpdateVersion);
      }
    } else {
      for (Map.Entry<String, String> entry : data.entrySet()) {
        intent.putExtra(entry.getKey(), entry.getValue());
      }

      if (data.containsKey(Constants.FCM_NOTIFICATION_TYPE)) {
        intent.putExtra(
            Constants.FCM_NOTIFICATION_TYPE, data.get(Constants.NOTIFICATION_TYPE_DEFAULT));
      }
    }

    intent.addFlags(
        Intent.FLAG_ACTIVITY_NEW_TASK
            | Intent.FLAG_ACTIVITY_SINGLE_TOP
            | Intent.FLAG_ACTIVITY_CLEAR_TOP);

    PendingIntent pendingIntent =
        PendingIntent.getActivity(
            this,
            pendingIntentID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
    NotificationCompat.Builder notificationBuilder =
        new NotificationCompat.Builder(this, channelId)
            .setSmallIcon(com.eup.codeopsstudio.res.R.drawable.ic_stat_name)
            .setContentTitle(notificationTitle)
            .setContentText(notificationMessage)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent);

    if (isUpdateNotification) {
      notificationBuilder.setPriority(NotificationCompat.PRIORITY_HIGH);
      if (forceUpdate) {
        notificationBuilder.setOngoing(true);
      }
    }

    NotificationManager notificationManager =
        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

    if (notificationManager == null) return;

    var channel = new NotificationChannel(channelId, channelName, channelImportance);
    if (!isUpdateNotification) {
      channel.enableVibration(true);
      channel.setVibrationPattern(new long[] {100, 200, 300});
    }

    notificationManager.createNotificationChannel(channel);

    final String final_image_url = imageUrl;

    if (Wizard.isEmpty(final_image_url)) {
      displayNotification(notificationBuilder, notificationManager, notificationID);
    } else {
      ILog.debug(TAG, "Attempting to load notification image: " + imageUrl);
      AsyncTask.runNonCancelable(
          () -> {
            return Glide.with(ctx).asBitmap().load(final_image_url).submit().get();
          },
          (bitmap, throwable) -> {
            if (throwable != null) {
              ILog.error(TAG, "Failed to load notification image (exception).", throwable);
              displayNotification(notificationBuilder, notificationManager, notificationID);
              return;
            }

            if (bitmap == null) {
              ILog.error(TAG, "Failed to load notification image (bitmap null).");
              displayNotification(notificationBuilder, notificationManager, notificationID);
              return;
            }

            NotificationCompat.Builder builderWithImage =
                notificationBuilder
                    .setLargeIcon(bitmap)
                    .setStyle(
                        new NotificationCompat.BigPictureStyle()
                            .bigPicture(bitmap)
                            .bigLargeIcon((Bitmap) null));
            displayNotification(builderWithImage, notificationManager, notificationID);
          });
    }
  }

  private void displayNotification(
      NotificationCompat.Builder notificationBuilder,
      NotificationManager notificationManager,
      int notificationID) {
    try {
      notificationManager.notify(notificationID, notificationBuilder.build());
    } catch (SecurityException e) {
      ILog.error(TAG, "Permission denied for posting notification", e);
    }
  }

  private void handleNow(@NonNull Map<String, String> dataPayload) {
    ILog.debug(TAG, "Short lived task is done");
  }

  private void scheduleJob(@NonNull Map<String, String> dataPayload) {
    ILog.debug(TAG, "Long running task needs to be scheduled.");
    Data.Builder dataBuilder = new Data.Builder();
    for (Map.Entry<String, String> entry : dataPayload.entrySet()) {
      dataBuilder.putString(entry.getKey(), entry.getValue());
    }

    Constraints constraints =
        new Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build();

    OneTimeWorkRequest request =
        new OneTimeWorkRequest.Builder(FCMWorker.class)
            .setInputData(dataBuilder.build())
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                OneTimeWorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS)
            .build();
    WorkManager.getInstance(getApplicationContext()).enqueue(request);
  }

  private boolean isUpdateRequired(Map<String, String> data) {
    if (data == null || data.isEmpty()) return false;
    String minVersion = data.get(Constants.KEY_MIN_VERSION);
    return VersionManager.isForceUpdateRequired(minVersion);
  }

  private String getNotificationTitle(RemoteMessage remoteMessage) {
    if (remoteMessage == null) return getString(R.string.fcm_title_fallback);
    Map<String, String> data = remoteMessage.getData();
    if (remoteMessage.getNotification() != null) {
      return remoteMessage.getNotification().getTitle();
    } else if (data != null && !data.isEmpty()) {
      return data.getOrDefault("title", getString(R.string.fcm_title_fallback));
    }
    return getString(R.string.fcm_title_fallback);
  }

  private String getNotificationBody(RemoteMessage remoteMessage) {
    if (remoteMessage == null) return getString(R.string.fcm_body_fallback);
    Map<String, String> data = remoteMessage.getData();
    if (remoteMessage.getNotification() != null) {
      return remoteMessage.getNotification().getBody();
    } else if (data != null && !data.isEmpty()) {
      return data.getOrDefault("body", getString(R.string.fcm_body_fallback));
    }
    return getString(R.string.fcm_body_fallback);
  }

  private boolean shouldShowNotification(RemoteMessage remoteMessage) {
    if (remoteMessage == null) return false;
    Map<String, String> data = remoteMessage.getData();
    if (data == null) return false;

    DeviceInfo deviceInfo = new DeviceInfo(this);
    String targetCountry = data.get(Constants.FCM_TARGET_COUNTRY);
    String targetAndroidVersion = data.get(Constants.FCM_TARGET_ANDROID_VERSION);
    String targetUserId = data.get(Constants.FCM_TARGET_USER_ID);
    String targetAppVersion = data.get(Constants.FCM_TARGET_APP_VERSION);
    String targetLocale = data.get(Constants.FCM_TARGET_LOCALE);

    String currentUserId = Wizard.getUserID(this);
    String currentCountry = deviceInfo.getCountry();
    String currentAndroidVersion = Build.VERSION.RELEASE;
    long currentAppVersion = deviceInfo.getAppVersionCodeLong();
    String currentLocale = deviceInfo.getLocaleLanguage();

    // If NO targeting params, show to everyone
    if (targetCountry == null
        && targetUserId == null
        && targetAndroidVersion == null
        && targetAppVersion == null
        && targetLocale == null) {
      return true;
    }

    if (targetCountry != null && !matchesCountry(targetCountry, currentCountry)) {
      return false;
    }

    if (targetUserId != null && !matchesUserId(targetUserId, currentUserId)) {
      return false;
    }

    if (targetAndroidVersion != null
        && !matchesAndroidVersion(targetAndroidVersion, currentAndroidVersion)) {
      return false;
    }

    if (targetAppVersion != null && !matchesAppVersion(targetAppVersion, currentAppVersion)) {
      return false;
    }

    if (targetLocale != null && !matchesLocale(targetLocale, currentLocale)) {
      return false;
    }

    return true;
  }

  private boolean matchesCountry(String targetCountry, String currentCountry) {
    // Support multiple countries: "NG,GH,KE,ZA"
    if (targetCountry.contains(",")) {
      String[] countries = targetCountry.split(",");
      for (String country : countries) {
        if (country.trim().equalsIgnoreCase(currentCountry)) {
          return true;
        }
      }
      return false;
    }
    return targetCountry.equalsIgnoreCase(currentCountry);
  }

  private boolean matchesUserId(String targetUserId, String currentUserId) {
    if (Wizard.isEmpty(targetUserId) || Wizard.isEmpty(currentUserId)) {
      return false;
    }

    // Support multiple user IDs: "user123,user456,vip789"
    if (targetUserId.contains(",")) {
      String[] userIds = targetUserId.split(",");
      for (String userId : userIds) {
        if (userId.trim().equals(currentUserId)) {
          return true;
        }
      }
      return false;
    }
    return targetUserId.equals(currentUserId);
  }

  private boolean matchesAndroidVersion(String targetVersion, String currentVersion) {
    int currentMajor;

    try {
      String[] parts = currentVersion.split("\\.");
      currentMajor = Integer.parseInt(parts[0]);
    } catch (Exception e) {
      currentMajor = Build.VERSION.SDK_INT;
    }

    try {
      if (targetVersion.startsWith(">=")) {
        int minVersion = Integer.parseInt(targetVersion.substring(2));
        return currentMajor >= minVersion;
      } else if (targetVersion.startsWith("<=")) {
        int maxVersion = Integer.parseInt(targetVersion.substring(2));
        return currentMajor <= maxVersion;
      } else if (targetVersion.contains("-")) {
        String[] range = targetVersion.split("-");
        int min = Integer.parseInt(range[0].trim());
        int max = Integer.parseInt(range[1].trim());
        return currentMajor >= min && currentMajor <= max;
      } else {
        return targetVersion.equals(currentVersion);
      }
    } catch (Exception e) {
      ILog.error(TAG, "Error occured while matching Android versions", e);
      return true; // fallback to show notification
    }
  }

  private boolean matchesAppVersion(String targetVersion, long currentVersion) {
    try {
      if (targetVersion.startsWith(">=")) {
        long minVersion = Long.parseLong(targetVersion.substring(2));
        return currentVersion >= minVersion;
      } else if (targetVersion.startsWith("<=")) {
        long maxVersion = Long.parseLong(targetVersion.substring(2));
        return currentVersion <= maxVersion;
      } else if (targetVersion.contains("-")) {
        String[] range = targetVersion.split("-");
        long min = Long.parseLong(range[0].trim());
        long max = Long.parseLong(range[1].trim());
        return currentVersion >= min && currentVersion <= max;
      } else {
        // Exact version match
        long target = Long.parseLong(targetVersion);
        return currentVersion == target;
      }
    } catch (Exception e) {
      ILog.error(TAG, "Error while matching app versions", e);
      return true;
    }
  }

  private boolean matchesLocale(String targetLocale, String currentLocale) {
    if (targetLocale.contains(",")) {
      String[] locales = targetLocale.split(",");
      for (String locale : locales) {
        if (locale.trim().equalsIgnoreCase(currentLocale)) {
          return true;
        }
      }
      return false;
    }
    return targetLocale.equalsIgnoreCase(currentLocale);
  }

  private void sendRegistrationToServer(String token) {
    // TODO: Implement server registration
    ILog.debug(TAG, "Token ready for server registration: " + token);
  }

  private int timeMask() {
    return (int) (System.currentTimeMillis() % Integer.MAX_VALUE);
  }
}
