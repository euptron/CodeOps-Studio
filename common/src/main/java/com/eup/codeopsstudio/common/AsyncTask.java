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

package com.eup.codeopsstudio.common;

import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Asynchronous task executor optimized for Android. This class provides a comprehensive set of
 * utilities for executing asynchronous tasks, handling callbacks, and managing UI thread
 * interactions.
 *
 * <p>The AsyncTask class leverages CompletableFuture and a custom MainThreadExecutor to ensure
 * efficient and non-blocking execution of tasks while maintaining smooth interaction with the user
 * interface.
 *
 * <p>It includes support for executing callable tasks with or without error handling, scheduling
 * tasks to run later, canceling scheduled tasks, and executing runnable asynchronously on the main
 * UI thread.
 *
 * <p>This class encapsulates best practices for Android asynchronous programming and is designed to
 * enhance the performance and responsiveness of Android applications.
 *
 * @author Etido Peter
 * @version 1.1
 * @since 2024-04-22
 */
public class AsyncTask {

    public static final String LOG_TAG = AsyncTask.class.getSimpleName();
    private static final int CPU_PROCESSORS_COUNT = Runtime.getRuntime().availableProcessors();
    public static final Executor executor = Executors.newFixedThreadPool(CPU_PROCESSORS_COUNT);

    /**
     * Cancels a scheduled runnable by removing it from the UI thread's message queue.
     *
     * @param action The runnable to cancel.
     */
    public static void cancelRunLater(Runnable action) {
        if (action == null) return;
        MainThreadExecutor.getInstance().cancelExecute(action);
    }

    /**
     * Executes a callable task asynchronously and invokes the provided callback upon completion.
     * Supports posting callback after the specified delay.
     *
     * @param callable      The callable task to execute.
     * @param callback      The callback to invoke upon task completion.
     * @param delayDuration The delay (in seconds) before executing the task. If zero or negative,
     *                      executes immediately.
     * @param <R>           The type of result returned by the task.
     */
    @RequiresApi(api = Build.VERSION_CODES.S)
    public static <R> void execute(Callable<R> callable, CallbackWithError<R> callback,
        long delayDuration) {
        execute(callable, callback, delayDuration, TimeUnit.SECONDS);
    }

    /**
     * Executes a callable task asynchronously and invokes the provided callback upon completion.
     * Supports posting callback after the specified delay.
     *
     * <p>Example usage:
     *
     * <pre>{@code
     * AsyncTask.execute(myCallable, myCallback, 1000, TimeUnit);
     * }</pre>
     *
     * @param callable   The callable task to execute.
     * @param callback   The callback to invoke upon task completion.
     * @param delayMills The delay before executing the task. If zero or negative, executes
     *                   immediately.
     * @param timeUnit   the time unit type for delay period
     * @param <R>        The type of result returned by the task.
     */
    @RequiresApi(api = Build.VERSION_CODES.S)
    public static <R> void execute(Callable<R> callable, @NonNull CallbackWithError<R> callback,
        long delayMills, TimeUnit timeUnit) {
        runProvideError(callable).whenCompleteAsync(callback::onComplete,
            CompletableFuture.delayedExecutor(delayMills, timeUnit));
    }

    /**
     * Executes a task and invokes the callback on resolution thread
     *
     * <p>Upon observation when i invoked the callback directly on the main thread, it takes few
     * milliseconds depending on the task before invoking the callback, that this makes callback
     * handling more flexible. <strong> Remember to handle UI elements on the main UI
     * thread</strong>
     *
     * @see #runNonCancelable(Callable, CallbackWithError) for main thread callback invocation
     */
    public static <R> void execute(Callable<R> callable,
        @NonNull CallbackWithError<R> callbackWithError) {
        runProvideError(callable).whenComplete(callbackWithError::onComplete);
    }

    /**
     * Executes a callable task asynchronously and returns a CompletableFuture. Throws a
     * CompletionException if an exception occurs during execution.
     *
     * @param callable The callable task to execute.
     * @param <R>      The type of result returned by the task.
     * @return A CompletableFuture representing the result of the task.
     */
    public static <R> CompletableFuture<R> runProvideError(Callable<R> callable) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return callable.call();
            } catch (Throwable throwable) {
                throw new CompletionException(throwable);
            }
        });
    }

    /**
     * Posts the runnable into the UI thread to be run later after the specified amount of time
     * elapses.
     *
     * <p>The time-base is {@link android.os.SystemClock#uptimeMillis}.
     *
     * @param runnable The Runnable that will be executed.
     * @param delay    The delay (in milliseconds) until the Runnable will be executed.
     */
    public static void runLaterOnUiThread(Runnable runnable, long delay) {
        MainThreadExecutor.getInstance().executeAfterDelay(runnable, delay);
    }

    /**
     * Executes a callable task asynchronously and invokes the provided callback on the main UI
     * thread
     * upon completion. Supports delayed execution after the specified delay.
     *
     * <p>This method handles task completion with error.
     *
     * @param callable          The callable task to execute.
     * @param callbackWithError The callback to invoke upon task completion (with error).
     * @param <R>               The type of result returned by the task.
     */
    public static <R> void runNonCancelable(Callable<R> callable,
        CallbackWithError<R> callbackWithError) {
        runProvideError(callable).whenCompleteAsync(callbackWithError::onComplete,
            MainThreadExecutor.getInstance());
    }

    /**
     * Executes a callable task asynchronously and invokes the provided callback on the main UI
     * thread
     * upon completion. Handles task completion without error.
     *
     * <p>This method is suitable for executing tasks that are expected to complete without
     * errors. If
     * the task encounters an exception, it will be logged, but no further action will be taken.
     *
     * <p>Example usage:
     *
     * <pre>{@code
     * AsyncTask.execute(myCallable, myCallback);
     * }</pre>
     *
     * @param callable The callable task to execute.
     * @param callback The callback to invoke upon task completion.
     * @param <R>      The type of result returned by the task.
     */
    public static <R> void runNonCancelable(Callable<R> callable, Callback<R> callback) {
        run(callable).whenCompleteAsync((result, throwable) -> callback.onComplete(result),
            MainThreadExecutor.getInstance());
    }

    /**
     * Executes a callable task asynchronously and returns a CompletableFuture. Returns null to
     * indicate error if an exception occurs during execution.
     *
     * @param callable The callable task to execute.
     * @param <R>      The type of result returned by the task.
     * @return A CompletableFuture representing the result of the task if successful or null in case
     * of an error
     */
    public static <R> CompletableFuture<R> run(Callable<R> callable) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return callable.call();
            } catch (Throwable throwable) {
                return null;
            }
        });
    }

    /**
     * Execute task on background thread
     *
     * @param runnable The runnable to execute
     * @since CodeOps Studio version 1.0.3
     */
    public static void runOnBackgroundThread(Runnable runnable) {
        CompletableFuture.runAsync(runnable);
    }

    /**
     * Posts the runnable into the UI thread to be run later.
     *
     * @param runnable The code to run
     */
    public static void runOnUiThread(Runnable runnable) {
        MainThreadExecutor.getInstance().execute(runnable);
    }

    /**
     * Interface for handling task completion without error.
     *
     * @param <R> The type of result returned by the task.
     */
    public interface Callback<R> {
        void onComplete(R result);
    }

    /**
     * Interface for handling task completion with error.
     *
     * @param <R> The type of result returned by the task.
     */
    public interface CallbackWithError<R> {
        void onComplete(R result, Throwable throwable);
    }

    private static class MainThreadExecutor implements Executor {
        private static MainThreadExecutor mainInstance = null;
        private final Handler mainHandler;

        private MainThreadExecutor() {
            mainHandler = createAsync(Looper.getMainLooper());
        }

        /**
         * Create a new Handler whose posted messages and runnable are not subject to
         * synchronization
         * barriers such as display vsync.
         *
         * <p>Messages sent to an async handler are guaranteed to be ordered with respect to one
         * another, but not necessarily with respect to messages from other Handlers.
         *
         * @param looper the Looper that the new Handler should be bound to
         * @return a new async Handler instance
         */
        @NonNull
        private static Handler createAsync(Looper looper) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                return Handler.createAsync(looper);
            }

            return new Handler(looper);
        }

        /**
         * Executes the given action {@code action} on the main UI thread immediately or posts it
         * to the
         * UI thread's message queue if called from a background thread.
         *
         * @param action The runnable task to execute.
         */
        @Override
        public void execute(Runnable action) {
            if (isMainThread()) {
                action.run();
            } else {
                mainHandler.post(action);
            }
        }

        /**
         * Remove any pending posts of runnable that are in the message queue.
         *
         * @param runnable The runnable to remove
         */
        public void cancelExecute(Runnable runnable) {
            mainHandler.removeCallbacks(runnable);
        }

        /**
         * Checks that currently running on the main thread.
         *
         * @throws IllegalStateException if the current thread is not the main thread.
         */
        public static void checkMainThread() {
            if (!isMainThread()) {
                throw new IllegalStateException(
                    "Not running on main thread when it is required " + "to.");
            }
        }

        /**
         * Return whether the current thread is the main thread.
         *
         * @return true if we are on the main thread, false otherwise
         */
        public static boolean isMainThread() {
            return Thread.currentThread() == Looper.getMainLooper().getThread();
        }

        /**
         * Posts the runnable into the UI thread to be run later after the specified amount of time
         * elapses.
         *
         * <p>The time-base is {@link android.os.SystemClock#uptimeMillis}.
         *
         * @param runnable   The Runnable that will be executed.
         * @param delayMills The delay (in milliseconds) until the Runnable will be executed.
         */
        public void executeAfterDelay(final Runnable runnable, long delayMills) {
            mainHandler.postDelayed(runnable, delayMills);
        }

        /**
         * Returns an instance of the main thread executor.
         *
         * @return The singleton MainThreadExecutor.
         */
        public static MainThreadExecutor getInstance() {
            if (mainInstance == null) {
                mainInstance = new MainThreadExecutor();
            }
            return mainInstance;
        }
    }
}
