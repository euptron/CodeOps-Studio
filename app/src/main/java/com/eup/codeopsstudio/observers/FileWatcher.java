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

package com.eup.codeopsstudio.observers;

import android.os.Build;
import android.os.FileObserver;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import com.eup.codeopsstudio.common.AsyncTask;
import java.io.File;

/**
 * FileObserver subclass to monitor file changes
 *
 * @author Etido Peter
 */
public class FileWatcher extends FileObserver {

  /**
   * <table>
   * <body>
   *  <tr>
   *    <th>Event</th>
   *    <th>Description</th>
   *  </tr>
   *  <tr>
   *    <td>FileObserver.CREATE</td>
   *    <td>A new file or subdirectory was created under the monitored directory</td>
   *  </tr>
   *  <tr>
   *    <td>FileObserver.DELETE</td>
   *    <td>A file was deleted from the monitored directory</td>
   *  </tr>
   *  <tr>
   *    <td>FileObserver.DELETE_SELF</td>
   *    <td>The monitored file or directory was deleted; monitoring effectively stops</td>
   *  </tr>
   *  <tr>
   *    <td>FileObserver.MOVE_SELF</td>
   *    <td>The monitored file or directory was moved; monitoring continues</td>
   *  </tr>
   *  <tr>
   *    <td>FileObserver.MOVED_FROM</td>
   *    <td>A file or subdirectory was moved from the monitored directory</td>
   *  </tr>
   *   <tr>
   *    <td>FileObserver.MOVED_TO</td>
   *    <td>A file or subdirectory was moved to the monitored directory</td>
   *  </tr>
   *  <tr>
   *    <td>FileObserver.MODIFY</td>
   *    <td>Data was written to a file</td>
   *  </tr>
   *  </body>
   */
  private static final int NOTIFY_EVENTS =
      FileObserver.CREATE
          | FileObserver.DELETE
          | FileObserver.DELETE_SELF
          | FileObserver.MOVE_SELF
          | FileObserver.MOVED_FROM
          | FileObserver.MOVED_TO
          | FileObserver.MODIFY;

  private final OnFileChangeListener listener;
  private final Runnable eventRunnable;

  private volatile int lastEvent;
  private volatile String lastPath;

  /**
   * Constructor for FileWatcher
   *
   * <p>Although deprecated it is available on devices below Android 10
   *
   * @param path the file or directory to monitor
   * @param listener the event callback for file changes
   */
  public FileWatcher(String path, OnFileChangeListener listener) {
    super(path, NOTIFY_EVENTS);
    this.listener = listener;
    this.eventRunnable = this::handleEvent;
  }

  /**
   * Constructor for FileWatcher
   *
   * @param file the file or directory to monitor
   * @param listener the event callback for file changes
   */
  @RequiresApi(api = Build.VERSION_CODES.Q)
  public FileWatcher(File file, OnFileChangeListener listener) {
    super(file, NOTIFY_EVENTS);
    this.listener = listener;
    this.eventRunnable = this::handleEvent;
  }

  @Override
  public void onEvent(int event, @Nullable String path) {
    if ((event & NOTIFY_EVENTS) == 0) return;

    lastEvent = event;
    lastPath = path;

    AsyncTask.runOnUiThread(eventRunnable);
  }

  private void handleEvent() {
    if (listener != null && lastEvent != 0) {
      listener.onFileChanged(lastEvent, lastPath);
    }
    lastEvent = 0;
    lastPath = null;
  }

  public interface OnFileChangeListener {
    void onFileChanged(int event, String path);
  }
}
