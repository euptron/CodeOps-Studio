package com.eup.codeopsstudio.service.fcm;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.eup.codeopsstudio.common.ILog;

import java.util.Map;

public class FCMWorker extends Worker {
    private static final String TAG = "FCMWorker";

    public FCMWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        ILog.debug(TAG, "Starting long running task from FCM.");
        Map<String, Object> data = getInputData().getKeyValueMap();
        ILog.debug(TAG, "Worker received data: " + data);

        // Perform your long-running operations here.
        // For example, network requests, database operations, complex calculations.

        try {
            // Simulate work
            Thread.sleep(150); // Example: 15 seconds of work
            ILog.debug(TAG, "Long running task finished.");
            return Result.success();
        } catch (InterruptedException e) {
            ILog.error(TAG, "Long running task interrupted", e);
            Thread.currentThread().interrupt(); // Restore interrupted status
            return Result.failure();
        } catch (Exception e) {
            ILog.error(TAG, "Error in long running task", e);
            return Result.failure();
        }
    }
}