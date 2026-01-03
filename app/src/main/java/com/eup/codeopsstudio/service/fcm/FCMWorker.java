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
package com.eup.codeopsstudio.service.fcm;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.eup.codeopsstudio.common.Constants;
import com.eup.codeopsstudio.common.ILog;
import com.eup.codeopsstudio.common.util.PreferencesUtils;
import com.eup.codeopsstudio.server.provider.IPProvider;
import com.eup.codeopsstudio.util.Wizard;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.function.Function;
import org.json.JSONObject;

/**
 * Worker class for performing background version checks triggered by FCM notifications.
 *
 * <p>This worker is expected to run periodically to ensure the latest version of CodeOps Studio is
 * installed
 *
 * @author Etido Peter
 */
public class FCMWorker extends Worker {

  private static final String TAG = "FCMWorker";

  public FCMWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
    super(context, workerParams);
  }

  @NonNull
  @Override
  public Result doWork() {
    ILog.debug(TAG, "FCMWorker started.");
    Map<String, Object> data = getInputData().getKeyValueMap();
    String notificationType = (String) data.get(Constants.FCM_NOTIFICATION_TYPE);

    Function<Map<String, Object>, Result> taskHandler =
        switch (notificationType) {
          case Constants.NOTIFICATION_TYPE_APP_UPDATE -> this::performAppUpdateCheck;
          default -> throw new IllegalArgumentException(
              "Unknown notification type: " + notificationType);
        };

    try {
      return taskHandler.apply(data);
    } catch (Exception e) {
      ILog.error(TAG, "Error executing task: " + notificationType, e);
      return Result.failure();
    }
  }

  private Result performAppUpdateCheck(Map<String, Object> data) {
    try {
      Context ctx = getApplicationContext();
      String latestVersion = fetchLatestVersion();
      String currentVersion = Wizard.getAppVersionName(ctx);

      if (Wizard.isEmpty(latestVersion)) {
        if (new IPProvider(ctx).isConnected()) {
          return Result.retry();
        } else {
          return Result.failure();
        }
      }

      if (!currentVersion.equals(latestVersion)) {
        saveUpdateCheckTime();
        ILog.debug(TAG, "New version available: " + latestVersion);
        return Result.success();
      } else {
        ILog.debug(TAG, "App is up to date.");
        return Result.success();
      }
    } catch (Exception e) {
      ILog.error(TAG, "Error in long running task", e);
      return Result.failure();
    }
  }

  private String fetchLatestVersion() {
    HttpURLConnection connection = null;
    BufferedReader reader = null;

    try {
      var url = new URL(Constants.DEFAULT_CHANGE_LOG_URL);
      connection = (HttpURLConnection) url.openConnection();
      connection.setRequestMethod("GET");
      connection.setConnectTimeout(15000);
      connection.setReadTimeout(15000);
      connection.setRequestProperty("Accept", "application/json");
      connection.setRequestProperty("User-Agent", "CodeOps-Studio-App");

      int responseCode = connection.getResponseCode();

      if (responseCode == HttpURLConnection.HTTP_OK) {
        InputStream inputStream = connection.getInputStream();
        reader = new BufferedReader(new InputStreamReader(inputStream));
        var response = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
          response.append(line);
        }

        var jsonResponse = new JSONObject(response.toString());
        return jsonResponse.optString("latest_version", "1.0.0");
      } else {
        ILog.warning(TAG, "HTTP error code: " + responseCode);
        throw new RuntimeException("HTTP error: " + responseCode);
      }
    } catch (Exception e) {
      ILog.error(TAG, "Error fetching latest version", e);
    } finally {
      if (connection != null) {
        connection.disconnect();
      }
      if (reader != null) {
        try {
          reader.close();
        } catch (Exception e) {
          ILog.error(TAG, "Error closing reader", e);
        }
      }
    }

    ILog.warning(TAG, "Could not fetch latest version, falling back to current");
    return Wizard.getAppVersionName(getApplicationContext()); // fallback
  }

  private void saveUpdateCheckTime() {
    SharedPreferences prefs = PreferencesUtils.getAppUpdatePreferences();
    prefs.edit().putLong(Constants.PREF_UPDATE_CHECK_TIME, System.currentTimeMillis()).apply();
  }
}
