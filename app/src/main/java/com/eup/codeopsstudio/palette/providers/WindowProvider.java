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
 * questions or need additional information. Email: etido.up@gmail.com
 */

package com.eup.codeopsstudio.palette.providers;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.EditText;

import androidx.annotation.NonNull;

import com.eup.codeopsstudio.MainActivity;
import com.eup.codeopsstudio.R;
import com.eup.codeopsstudio.common.ILog;
import com.eup.codeopsstudio.common.models.BooleanResult;
import com.eup.codeopsstudio.palette.PaletteItem;
import com.eup.codeopsstudio.pane.Pane;
import com.eup.codeopsstudio.util.BaseUtil;
import com.eup.codeopsstudio.util.Wizard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class WindowProvider implements CommandProvider {

    private static final String TAG = "WindowProvider";
    private final Activity activity;
    private final EditText searchInput;

    public static final String EXTRA_SESSION_ID = "extra_session_id";

    public WindowProvider(Activity activity, EditText searchInput) {
        this.activity    = activity;
        this.searchInput = searchInput;
    }

    @Override
    public String getTrigger() {
        return ">window";
    }

    @Override
    public List<PaletteItem> getItems(String rawQuery, String contentQuery) {
        List<PaletteItem> results = new ArrayList<>();

        results.add(new PaletteItem("new_window", "New Window", "", "", "Ctrl + Shift + N",
            () -> newWindow(activity)));
        results.add(new PaletteItem("switch_window", "Switch Window", () -> {
            searchInput.setHint("Select a window to switch to");
            performLoadWindows(session -> switchWindows(activity, session), results);
        }));
        results.add(new PaletteItem("close_window", "Close Window", () -> {
            searchInput.setHint("Select a window to close");
            performLoadWindows(session -> closeWindowById(activity, session.getTaskId()), results);
        }));
        results.add(new PaletteItem("close_this_window", "Close This Window",
            () -> closeThisWindow(activity)));
        results.add(new PaletteItem("close_other_window", "Close Other Windows",
            () -> closeOtherWindows(activity)));
        return Collections.emptyList();
    }

    @NonNull
    @Override
    public String toString() {
        int size = Registry.size();
        return size + " windows" + ((size != 1) ? "s" : "");
    }

    private void performLoadWindows(Consumer<Session> sessionConsumer, List<PaletteItem> results) {
        if (activity instanceof MainActivity from) {
            String sessionId = from.getSessionId(); // current window id
            if (Wizard.isEmpty(sessionId)) return;
            results.addAll(loadWindowSessions(sessionId, sessionConsumer));
        }
    }

    private List<PaletteItem> loadWindowSessions(String sessionId,
        Consumer<Session> sessionConsumer) {
        final List<PaletteItem> items = new ArrayList<>();
        final List<Session> windowsSessions = Registry.getSessions();

        if (windowsSessions.isEmpty()) {
            items.add(new PaletteItem("empty_windows", "No other windows open", null));
            return items;
        }

        for (int i = 0; i < windowsSessions.size(); i++) {
            var windowSession = windowsSessions.get(i);
            boolean isCurrent = windowSession.sessionId.equals(sessionId);

            var title = activity.getString(R.string.app_name);
            var subtitle = windowSession.activePane.getTitle();
            var tag = "#" + windowSession.getWindowCount();
            var shortcut = isCurrent ? "Current" : "";

            items.add(new PaletteItem(sessionId, title, subtitle, tag, shortcut,
                () -> sessionConsumer.accept(windowSession)));
        }
        return items;
    }

    public void switchWindows(Activity from, Session to) {
        var am = (ActivityManager) from.getSystemService(Context.ACTIVITY_SERVICE);
        try {
            for (var task : am.getAppTasks()) {
                if (getTaskId(task.getTaskInfo()) == to.getTaskId()) {
                    task.moveToFront();
                    break;
                }
            }
        } catch (Exception e) {
            ILog.debug(TAG, "Failed to switch window:" + e.getMessage(), e);
        }
    }

    /**
     * Opens another instance of the IDE window
     *
     * @param from the current base activity
     */
    public static void newWindow(Activity from) {
        String newSessionId = Registry.generateSessionId();

        var intent = new Intent(from, MainActivity.class);
        intent.putExtra(EXTRA_SESSION_ID, newSessionId);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S_V2) {
            ActivityOptions options = ActivityOptions.makeBasic();
            from.startActivity(intent, options.toBundle());
        } else {
            from.startActivity(intent);
        }
        BaseUtil.toastShort("Opening window#" + Registry.size());
    }

    /**
     * Closes the current app window
     *
     * @param activity the current app instance activity
     */
    public static void closeThisWindow(Activity activity) {
        activity.finishAndRemoveTask();
    }

    public static void closeOtherWindows(Activity from) {
        int currentTaskId = from.getTaskId();
        closeWindowsInternal(from, task -> getTaskId(task.getTaskInfo()) != currentTaskId, false);
    }

    public static void closeWindowById(Activity from, int taskId) {
        closeWindowsInternal(from, task -> getTaskId(task.getTaskInfo()) == taskId, true);
    }

    private static void closeWindowsInternal(Activity from,
        BooleanResult<ActivityManager.AppTask> condition, boolean closeSingle) {
        var am = (ActivityManager) from.getSystemService(Context.ACTIVITY_SERVICE);
        try {
            for (var task : am.getAppTasks()) {
                if (condition.process(task)) {
                    task.finishAndRemoveTask();
                    if (closeSingle) break;
                }
            }
        } catch (Exception e) {
            ILog.debug(TAG, "Failed to close window:" + e.getMessage(), e);
        }
    }

    @SuppressWarnings("deprecation")
    private static int getTaskId(ActivityManager.RecentTaskInfo info) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return info.taskId;
        } else {
            return info.id;
        }
    }

    public static final class Registry {
        private Registry() {
            // Default
        }

        private static final AtomicInteger COUNTER = new AtomicInteger(0);
        private static final ConcurrentHashMap<String, Session> SESSIONS =
            new ConcurrentHashMap<>();

        public static Session register(String sessionId, Pane selected) {
            int count = COUNTER.incrementAndGet();
            var session = new Session(sessionId, count, selected);
            SESSIONS.put(sessionId, session);
            return session;
        }

        public static void unregister(String sessionId) {
            var session = SESSIONS.remove(sessionId);
            if (session != null) session.isActive = false;
        }

        public static void setTaskId(String sessionId, int taskId) {
            var session = SESSIONS.get(sessionId);
            if (session != null) session.setTaskId(taskId);
        }

        public static Session get(String sessionId) {
            return SESSIONS.get(sessionId);
        }

        /**
         * The size of all active windows.
         *
         * @return the total number of windows
         * @see Registry#size()
         */
        public static int size() {
            return SESSIONS.size();
        }

        public static List<Session> getSessions() {
            var sessions = new ArrayList<>(SESSIONS.values());
            sessions.sort(Comparator.comparingInt(a -> a.windowCount));
            return sessions;
        }

        public static String generateSessionId() {
            return "w_" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        }
    }

    public static final class Session {
        final Pane activePane;
        final int windowCount;
        final String sessionId;
        final long creationTime;
        int taskId = -1;
        boolean isActive = true;

        public Session(String sessionId, int windowCount, Pane pane) {
            this(sessionId, windowCount, System.currentTimeMillis(), pane);
        }

        public Session(String sessionId, int windowCount, long creationTime, Pane pane) {
            this.sessionId    = sessionId;
            this.windowCount  = windowCount;
            this.creationTime = creationTime;
            this.activePane   = pane;
        }

        public void setTaskId(int taskId) {
            this.taskId = taskId;
        }

        public int getTaskId() {
            return this.taskId;
        }

        /**
         * Return the session count window count
         *
         * @return the session window count
         * @see Registry#size()
         */
        public int getWindowCount() {
            return windowCount;
        }
    }
}
