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

package com.eup.codeopsstudio.common;

import android.icu.text.SimpleDateFormat;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Also known as Interface-Log, this class provides a static utility interface for logging
 * application events using the standard Android
 * {@link android.util.Log} framework.
 *
 * <p>This class enhances the default logging by:
 *
 * <ul>
 *   <li>Offering standard logging methods (verbose, debug, info, warning, error).
 *   <li>Providing the ability to register {@link LogListener} instances to receive formatted log
 *       messages, which include a timestamp, log level, tag, and the message content.
 *   <li>Ensuring thread-safe addition, removal, and notification of listeners.
 * </ul>
 *
 * <p>Usage involves calling the static methods directly, e.g., {@code Logger.info("MyTag", "User
 * logged in");}. Listeners can be added via {@link #addLogListener(LogListener)} to forward logs to
 * other destinations like files or UI components.
 *
 * @author EUP
 * @since 1.0.3 beta
 */
public final class ILog {

    public static final String TAG = "CodeOps-Studio:Logger";
    private static final SimpleDateFormat DATE_FORMAT;
    private static final List<LogListener> logListeners = new CopyOnWriteArrayList<>();
    private static boolean isInDebugMode = false;

    static {
        DATE_FORMAT = new SimpleDateFormat(Constants.DEBUG_LOGS_DATE_FORMAT, Locale.getDefault());
    }

    private ILog() {
        throw new UnsupportedOperationException(
            "This is a utility class and cannot be " + "instantiated");
    }

    public static void mode(boolean isInDebugMode) {
        ILog.isInDebugMode = isInDebugMode;
    }

    public static void verbose(@NonNull String tag, @NonNull String msg) {
        verbose(tag, msg, null);
    }

    public static void verbose(@NonNull String tag, @NonNull String msg,
        @Nullable Throwable throwable) {
        logInternal(Log.VERBOSE, tag, msg, throwable);
    }

    private static void logInternal(int priority, @NonNull String tag, @NonNull String message,
        @Nullable Throwable throwable) {
        if (!isInDebugMode) return;

        String timestamp;
        synchronized (DATE_FORMAT) {
            timestamp = DATE_FORMAT.format(new Date());
        }

        String level = logAndGetPriority(priority, tag, message, throwable);
        if (!logListeners.isEmpty()) {
            String messageForListeners = String.format(Locale.ROOT, "[%s] [%s/%s] %s%s",
                timestamp, level, tag, message, (
                throwable == null ? "" : "\n" + Log.getStackTraceString(throwable)));
            try {
                for (LogListener listener : logListeners) {
                    listener.onLog(messageForListeners);
                }
            } catch (Exception e) {
                // Catch exceptions from logListeners to prevent one bad listener
                // from crashing the app or stopping other listeners.
                Log.e(TAG, "Failure executing LogListener: " + e.getMessage(), e);
            }
        }
    }

    @NonNull
    private static String logAndGetPriority(int priority, @NonNull String tag,
        @NonNull String message, @Nullable Throwable throwable) {
        return switch (priority) {
            case Log.VERBOSE -> {
                if (throwable == null) {
                    Log.v(tag, message);
                } else {
                    Log.v(tag, message, throwable);
                }
                yield "VERBOSE";
            }
            case Log.DEBUG -> {
                if (throwable == null) {
                    Log.d(tag, message);
                } else {
                    Log.d(tag, message, throwable);
                }
                yield "DEBUG";
            }
            case Log.INFO -> {
                if (throwable == null) {
                    Log.i(tag, message);
                } else {
                    Log.i(tag, message, throwable);
                }
                yield "INFO";
            }
            case Log.WARN -> {
                if (throwable == null) {
                    Log.w(tag, message);
                } else {
                    Log.w(tag, message, throwable);
                }
                yield "WARNING";
            }
            case Log.ERROR -> {
                if (throwable == null) {
                    Log.e(tag, message);
                } else {
                    Log.e(tag, message, throwable);
                }
                yield "ERROR";
            }
            default -> {
                Log.w(tag, "Attempted to log with unknown priority " + priority + ": "
                    + message, throwable);
                yield "UNKNOWN";
            }
        };
    }

    public static void debug(@NonNull String tag, @NonNull String msg) {
        debug(tag, msg, null);
    }

    public static void debug(@NonNull String tag, @NonNull String msg,
        @Nullable Throwable throwable) {
        logInternal(Log.DEBUG, tag, msg, throwable);
    }

    public static void info(@NonNull String tag, @NonNull String msg) {
        info(tag, msg, null);
    }

    public static void info(@NonNull String tag, @NonNull String msg,
        @Nullable Throwable throwable) {
        logInternal(Log.INFO, tag, msg, throwable);
    }

    public static void warning(@NonNull String tag, @NonNull String msg) {
        warning(tag, msg, null);
    }

    public static void warning(@NonNull String tag, @NonNull String msg,
        @Nullable Throwable throwable) {
        logInternal(Log.WARN, tag, msg, throwable);
    }

    public static void error(@NonNull String tag, @NonNull String msg) {
        error(tag, msg, null);
    }

    public static void error(@NonNull String tag, @NonNull String msg,
        @Nullable Throwable throwable) {
        logInternal(Log.ERROR, tag, msg, throwable);
    }

    public static void addLogListener(@NonNull LogListener listener) {
        if (!logListeners.contains(Objects.requireNonNull(listener))) {
            logListeners.add(listener);
        }
    }

    public static void removeLogListener(@NonNull LogListener listener) {
        logListeners.remove(Objects.requireNonNull(listener));
    }

    public static void clearLogListeners() {
        logListeners.clear();
    }

    public static int getLogListenersCount() {
        return logListeners.size();
    }

    public interface LogListener {
        /**
         * Called when the ILog generates a log entry.
         *
         * @param formattedMessage The complete log message
         */
        void onLog(@NonNull String formattedMessage);
    }
}
