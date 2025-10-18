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

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.eup.codeopsstudio.observers.FileWatcher;

import java.io.File;

/**
 * Manages the connection between a client and the {@link FileWatcherService}.
 *
 * <p>This class implements {@link ServiceConnection} to handle binding to the file watcher service,
 * register a file change listener, and launchWithLocalHost monitoring a specified directory.
 * When disconnected,
 * it properly unregisters the listener and stops monitoring.
 *
 * @author Etido Peter
 */
public class FileWatcherServiceConnection implements ServiceConnection {

    private final FileWatcher.OnFileChangeListener listener;
    private File fileToWatch;
    private boolean isConnected;
    private FileWatcherService boundService;

    /**
     * Constructs a new FileWatcherServiceConnection with a specified file change listener.
     *
     * @param listener The listener that will be notified of file changes.
     */
    public FileWatcherServiceConnection(FileWatcher.OnFileChangeListener listener) {
        this.listener = listener;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        boundService = ((FileWatcherService.LocalBinder) service).getService();
        boundService.getBinder().addListener(listener);
        if (fileToWatch != null) {
            boundService.getBinder().startMonitoring(fileToWatch);
        }
        isConnected = true;
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        if (boundService != null) {
            boundService.getBinder().removeListener(listener);
            boundService.getBinder().stopMonitoring();
            boundService = null;
        }
        isConnected = false;
    }

    /**
     * Gets the bound instance of {@link FileWatcherService}.
     *
     * @return The bound service instance, or {@code null} if not connected.
     */
    public FileWatcherService getBoundService() {
        return this.boundService;
    }

    public File getMonitoredFile() {
        return this.fileToWatch;
    }

    public boolean isConnected() {
        return this.isConnected;
    }

    public void setFileToWatch(File file) {
        this.fileToWatch = file;
        if (isConnected && boundService != null) {
            boundService.getBinder().startMonitoring(file); // already connected (post-bound)
        }
    }
    
    public void removeListenerFromService() {
        if (boundService != null && listener != null) {
            boundService.getBinder().removeListener(listener);
            boundService.getBinder().stopMonitoring();
        }
    }
}
