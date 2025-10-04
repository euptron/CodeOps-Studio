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

package com.eup.codeopsstudio.git;

import androidx.annotation.NonNull;

import com.eup.codeopsstudio.IdeApplication;
import com.eup.codeopsstudio.R;
import com.eup.codeopsstudio.git.listeners.CloneListener;

import org.eclipse.jgit.lib.BatchingProgressMonitor;

import java.util.Locale;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicBoolean;

public class BatchProgressMonitor extends BatchingProgressMonitor {

    private static final String COMPLETE;
    private static final String COMPLETED_IN;
    private static final String SECONDS;

    static {
        COMPLETE     = IdeApplication.getInstance().getString(R.string.complete);
        COMPLETED_IN = IdeApplication.getInstance().getString(R.string.completed_in);
        SECONDS      = IdeApplication.getInstance().getString(R.string.seconds);
    }

    private final String url;
    private final CloneListener listener;
    private final AtomicBoolean cancelled = new AtomicBoolean(false);
    private long startTime;
    private int totalTasks;
    private int currentTask;
    private long lastUpdateTime;
    private long lastWorkCount = 0;
    /**
     * Transfer rate in KB/s
     */
    private double transferRate;

    public BatchProgressMonitor(@NonNull CloneListener listener, @NonNull String url) {
        this.url      = url;
        this.listener = listener;
    }

    @Override
    public void start(int totalTasks) {
        super.start(totalTasks);
        this.totalTasks  = totalTasks;
        this.currentTask = 0;
        this.startTime   = System.currentTimeMillis();

        var msg = "Starting operation: " + totalTasks + " task(s) enqueued";
        listener.onUpdateMessage(msg);
        listener.onProgress(0);
    }

    @Override
    public void beginTask(String title, int totalWork) {
        super.beginTask(title, totalWork);
        this.currentTask++;
        this.lastWorkCount  = 0;
        this.lastUpdateTime = System.currentTimeMillis();

        var msg = format("Processing task %d/%d: %s", currentTask, totalTasks, title);
        listener.onUpdateMessage(msg);
        listener.onProgress(calculateOverallProgress(0));
    }

    @Override
    public boolean isCancelled() {
        return cancelled.get();
    }

    @Override
    protected void onUpdate(String taskName, int workCurr) {
        checkCancelled();
        publishUpdate(taskName, workCurr);
    }

    @Override
    protected void onEndTask(String taskName, int workCurr) {
        checkCancelled();
        publishEndTask(taskName, workCurr);
    }

    @Override
    protected void onUpdate(String taskName, int workCurr, int workTotal, int percentDone) {
        checkCancelled();
        publishUpdate(taskName, workCurr, workTotal, percentDone);
    }

    @Override
    protected void onEndTask(String taskName, int workCurr, int workTotal, int percentDone) {
        checkCancelled();
        publishEndTask(taskName, workCurr, workTotal, percentDone);
    }

    private void publishEndTask(String taskName, int workCurr) {
        publishEndTask(taskName, workCurr, UNKNOWN, UNKNOWN);
    }

    private void publishEndTask(String taskName, int workCurr, int workTotal, int percentDone) {
        boolean isTotalWorkKnown = (workTotal != UNKNOWN) && (percentDone != UNKNOWN);

        long endTime = System.currentTimeMillis();
        double seconds = (endTime - startTime) / 1000.0;

        String msg;
        if (isTotalWorkKnown) {
            msg = format("[%s] %s: (%d/%d) %d%% %s %.1f %s", url, taskName, workCurr, workTotal,
                percentDone, COMPLETED_IN, seconds, SECONDS);
        } else {
            msg = format("[%s] %s: %d %s %.1f %s", url, taskName, workCurr, COMPLETED_IN, seconds
                , SECONDS);
        }
        listener.onUpdateMessage(msg);
        listener.onProgress(calculateOverallProgress(100));
    }

    private void publishUpdate(String taskName, int workCurr) {
        publishUpdate(taskName, workCurr, UNKNOWN, UNKNOWN);
    }

    private void publishUpdate(String taskName, int workCurr, int workTotal, int percentDone) {
        boolean isTotalWorkKnown = (workTotal != UNKNOWN) && (percentDone != UNKNOWN);

        long currentTime = System.currentTimeMillis();
        long timeDiff = currentTime - lastUpdateTime;
        long workDiff = workCurr - lastWorkCount;

        if (timeDiff > 0 && workDiff > 0) {
            double kbTransferred = workDiff / 1024.0;
            double seconds = timeDiff / 1000.0;
            transferRate = kbTransferred / seconds;
        }

        String msg;
        if (isTotalWorkKnown) {
            msg = format("[%s] %s: (%d/%d) %d%% %s (%.1f KB/s)", url, taskName, workCurr,
                workTotal, percentDone, COMPLETE, transferRate);
            listener.onProgress(calculateOverallProgress(percentDone));
        } else {
            msg = format("[%s] %s: %d %s (%.1f KB/s)", url, taskName, workCurr, COMPLETE,
                transferRate);
        }

        listener.onUpdateMessage(msg);
        lastUpdateTime = currentTime;
        lastWorkCount  = workCurr;
    }

    private void checkCancelled() {
        if (cancelled.get()) {
            throw new CancellationException("Task cancelled");
        }
    }

    @NonNull
    private String format(String str, Object... args) {
        return String.format(Locale.ENGLISH, str, args);
    }

    private int calculateOverallProgress(int currentTaskPercent) {
        if (totalTasks <= 0) return 0;

        double taskProgress = Math.min(currentTaskPercent, 100) / 100.0;
        double overallProgress = ((currentTask - 1) + taskProgress) / totalTasks;
        return (int) Math.min(100, overallProgress * 100);
    }

    public void cancel() {
        cancelled.compareAndSet(false, true);
    }
}
