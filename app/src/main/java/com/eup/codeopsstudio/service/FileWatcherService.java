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

import com.eup.codeopsstudio.common.Constants;
import com.eup.codeopsstudio.common.ILog;
import com.eup.codeopsstudio.common.util.SDKUtil;
import com.eup.codeopsstudio.common.util.SDKUtil.API;
import com.eup.codeopsstudio.observers.FileWatcher;
import com.eup.codeopsstudio.observers.FileWatcher.OnFileChangeListener;
import com.eup.codeopsstudio.res.R;

import java.io.File;
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
public class FileWatcherService extends Service implements FileWatcher.OnFileChangeListener {

    public static final String TAG = "FileMonitorService";
    private final List<OnFileChangeListener> listeners = new ArrayList<>();
    private final IBinder binder = new LocalBinder();
    private FileWatcher fileWatcher;
    private boolean isMonitoring = false;

    public FileWatcherService() {
        // Default
    }

    @Override
    public void onCreate() {
        super.onCreate();
        performStartForeground();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        performStartForeground();
        return START_STICKY; // restart if killed.
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopWatching();
        performStopService();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    private void stopWatching() {
        if (fileWatcher != null) {
            fileWatcher.stopWatching();
            fileWatcher = null;
        }
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
     * Removes the service from the foreground state, which allows it to be killed if the system
     * needs
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
     * <p><b>Note:</b> This method does not launchWithLocalHost the service. To
     * launchWithLocalHost the service, call {@link
     * #startService(Intent)}.
     */
    private void performStartForeground() {
        createNotificationChannel();
        Notification notification = buildNotification();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(Constants.APP_NOTIFICATION_ID, notification,
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
            .build();
    }

    @Override
    public void onFileChanged(int event, String path) {
        for (OnFileChangeListener listener : listeners) {
            listener.onFileChanged(event, path);
        }
    }

    public LocalBinder getBinder() {
        return (LocalBinder) this.binder;
    }

    /**
     * Binder class for clients to interact with the FileWatcherService.
     *
     * <p>It provides methods to add or remove file change listeners and to
     * launchWithLocalHost/stop monitoring a
     * specified directory.
     */
    public class LocalBinder extends Binder {
        public void addListener(OnFileChangeListener listener) {
            listeners.add(listener);
        }

        public void removeListener(OnFileChangeListener listener) {
            listeners.remove(listener);
        }

        public void startMonitoring(File file) {
            if (!isMonitoring && file != null) {
                if (SDKUtil.isAtLeast(API.ANDROID_10)) {
                    fileWatcher = new FileWatcher(file, FileWatcherService.this);
                } else {
                    fileWatcher = new FileWatcher(file.getAbsolutePath(), FileWatcherService.this);
                }

                fileWatcher.startWatching();
                isMonitoring = true;
                ILog.debug(TAG, "Monitoring started");
            }
        }

        public void stopMonitoring() {
            if (fileWatcher != null) {
                fileWatcher.stopWatching();
                fileWatcher  = null;
                isMonitoring = false;
                ILog.debug(TAG, "Monitoring stopped");
            }
        }

        public FileWatcherService getService() {
            return FileWatcherService.this;
        }
    }
}
