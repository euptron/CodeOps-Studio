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

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import com.eup.codeopsstudio.R;
import com.eup.codeopsstudio.common.Constants;
import com.eup.codeopsstudio.common.ILog;
import com.eup.codeopsstudio.observers.FileWatcher;
import com.eup.codeopsstudio.observers.FileWatcher.OnFileChangeListener;
import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * A foreground service that monitors file changes in a specified directory.
 *
 * <p>Uses {@link FileWatcher} to observe file events and notifies registered {@link
 * OnFileChangeListener} instances.
 *
 * <p>To ensure the service stops when the app is removed from recent tasks, {@code stopWithTask} is
 * set to {@code true}.
 *
 * <p>Manifest declaration:
 *
 * <pre>{@code
 * <service
 *     android:name=".service.FileWatcherService"
 *     android:enabled="true"
 *     android:stopWithTask="true"
 *     android:exported="false" />
 * }</pre>
 *
 * @author Etido Peter
 */
public class FileWatcherService extends Service {

  public static final String TAG = "FileMonitorService";

  private final IBinder binder = new LocalBinder();

  public FileWatcherService() {
    // Default
  }

  @Override
  public void onCreate() {
    super.onCreate();
    // ensure immediate start
    performStartForeground();
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    // restart if killed.
    performStartForeground();
    return START_STICKY;
  }

  @Nullable
  @Override
  public IBinder onBind(Intent intent) {
    return binder;
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    getBinder().stopMonitoring();
    getBinder().release();
    performStopService();
  }

  public LocalBinder getBinder() {
    return (LocalBinder) this.binder;
  }

  /**
   * Stops the service completely.
   *
   * <p>This method first removes the service from the foreground state and then stops it.
   */
  private void performStopService() {
    performStopForeground();
    stopSelf();
  }

  /**
   * Removes the service from the foreground state, which allows it to be killed if the system needs
   * memory.
   *
   * <p>This method does not stop the service; it only removes the persistent foreground
   * notification.
   */
  private void performStopForeground() {
    stopForeground(STOP_FOREGROUND_REMOVE);
  }

  /**
   * Puts the service into the foreground state with a persistent notification.
   *
   * <p><b>Note:</b> This method does not launchWithLocalHost the service. To launchWithLocalHost
   * the service, call {@link #startForegroundService(Intent)} on Android 8.0 (API 26) and above,
   * {@link #startService(Intent)} is not allowed for long-running background work and will fail
   * unless the service enters foreground immediately.
   */
  private void performStartForeground() {
    createNotificationChannel();
    Notification notification = buildNotification();

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      startForeground(
          Constants.APP_NOTIFICATION_ID,
          notification,
          ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC);
    } else {
      startForeground(Constants.APP_NOTIFICATION_ID, notification);
    }
  }

  private void createNotificationChannel() {
    final String channelId = Constants.FILE_WATCHER_NOTIFICATION_CHANNEL_ID;
    final CharSequence channelName = getString(R.string.file_watcher_notification_channel_name);
    String description = getString(R.string.file_watcher_notification_channel_description);
    final int importance = NotificationManager.IMPORTANCE_LOW;

    NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
    channel.setDescription(description);
    NotificationManager notificationManager = getSystemService(NotificationManager.class);
    if (notificationManager != null) {
      notificationManager.createNotificationChannel(channel);
    }
  }

  private Notification buildNotification() {
    return new NotificationCompat.Builder(this, Constants.FILE_WATCHER_NOTIFICATION_CHANNEL_ID)
        .setContentTitle(getString(R.string.app_name))
        .setSmallIcon(R.drawable.ic_folder_sync_outline)
        .setContentText(getString(R.string.file_watcher_desc))
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .setOngoing(true)
        .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
        .build();
  }

  public static class LocalBinder extends Binder implements FileWatcher.OnFileChangeListener {

    private FileWatcher fileWatcher;
    private boolean isMonitoring = false;
    private final List<WeakReference<OnFileChangeListener>> listeners = new ArrayList<>();

    public LocalBinder() {
      // Default
    }

    @Override
    public void onFileChanged(int event, String path) {
      listeners.removeIf(ref -> ref.get() == null);

      for (WeakReference<OnFileChangeListener> ref : listeners) {
        OnFileChangeListener listener = ref.get();
        if (listener != null) {
          listener.onFileChanged(event, path);
        }
      }
    }

    public void addListener(OnFileChangeListener listener) {
      if (listener != null) {
        listeners.removeIf(ref -> ref.get() == null);
        listeners.add(new WeakReference<>(listener));
      }
    }

    public void removeListener(OnFileChangeListener listener) {
      listeners.removeIf(ref -> ref.get() == listener || ref.get() == null);
    }

    public void release() {
      listeners.clear();
    }

    public void startMonitoring(File file) {
      if (!isMonitoring && file != null) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
          fileWatcher = new FileWatcher(file, LocalBinder.this);
        } else {
          fileWatcher = new FileWatcher(file.getAbsolutePath(), LocalBinder.this);
        }

        fileWatcher.startWatching();
        isMonitoring = true;
        ILog.debug(TAG, "Monitoring started");
      }
    }

    public void stopMonitoring() {
      if (fileWatcher != null) {
        fileWatcher.stopWatching();
        fileWatcher = null;
        isMonitoring = false;
        ILog.debug(TAG, "Monitoring stopped");
      }
    }
  }
}
