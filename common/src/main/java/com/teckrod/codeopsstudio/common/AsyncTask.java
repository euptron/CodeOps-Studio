/*************************************************************************
 * This file is part of CodeOps Studio.
 * CodeOps Studio - code anywhere anytime
 * https://github.com/euptron/CodeOps-Studio
 * Copyright (C) 2024 EUP
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
 * If you have more questions, feel free to message EUP if you have any
 * questions or need additional information. Email: etido.up@gmail.com
 *************************************************************************/
 
   package com.eup.codeopsstudio.common;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

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
 * tasks to run later, canceling scheduled tasks, and executing runnables asynchronously on the main
 * UI thread.
 *
 * <p>This class encapsulates best practices for Android asynchronous programming and is designed to
 * enhance the performance and responsiveness of Android applications.
 *
 * @author EUP
 * @version 1.0
 * @since 2024-04-22
 */
public class AsyncTask {

  private static final int CPU_PROCESSORS_COUNT = Runtime.getRuntime().availableProcessors();
  private static final Executor executor = Executors.newFixedThreadPool(CPU_PROCESSORS_COUNT);
  public static final String LOG_TAG = AsyncTask.class.getSimpleName();

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

  /**
   * Executes a callable task asynchronously and invokes the provided callback upon completion.
   * Supports delayed execution after the specified delay.
   *
   * <p>Example usage:
   *
   * <pre>{@code
   * AsyncTask.runNonCancelable(myCallable, myCallback, 1000);
   * }</pre>
   *
   * @param callable The callable task to execute.
   * @param callback The callback to invoke upon task completion.
   * @param delayMills The delay (in milliseconds) before executing the task. If zero or negative,
   *     executes immediately.
   * @param <R> The type of result returned by the task.
   */
  public static <R> void runNonCancelable(
      Callable<R> callable, CallbackWithError<R> callback, long delayMills) {
    CompletableFuture.supplyAsync(
            () -> {
              try {
                return callable.call();
              } catch (Throwable throwable) {
                throw new CompletionException(throwable);
              }
            })
        .whenComplete(
            (result, throwable) -> {
              MainThreadExecutor.getInstance()
                  .executeAfterDelay(
                      () -> {
                        callback.onComplete(result, throwable);
                      },
                      delayMills);
            });
  }

  /**
   * Executes a callable task asynchronously and invokes the provided callback upon completion.
   * Handles task completion without error.
   *
   * <p>This method is suitable for executing tasks that are expected to complete without errors. If
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
   * @param <R> The type of result returned by the task.
   */
  public static <R> void runNonCancelable(Callable<R> callable, Callback<R> callback) {
    run(callable)
        .whenComplete(
            (result, throwable) -> {
              MainThreadExecutor.getInstance()
                  .execute(
                      () -> {
                        callback.onComplete(result);
                      });
            });
  }

  /**
   * Executes a callable task asynchronously and invokes the provided callback upon completion.
   * Supports delayed execution after the specified delay.
   *
   * <p>This method handles task completion with error.
   *
   * @param callable The callable task to execute.
   * @param callbackWithError The callback to invoke upon task completion (with error).
   * @param <R> The type of result returned by the task.
   */
  public static <R> void runNonCancelable(
      Callable<R> callable, CallbackWithError<R> callbackWithError) {
    runProvideError(callable)
        .whenComplete(
            (result, throwable) -> {
              MainThreadExecutor.getInstance()
                  .execute(
                      () -> {
                        callbackWithError.onComplete(result, throwable);
                      });
            });
  }
 
  /*
   *. Dont use this causes error
   */
  public static void runNonCancelable(Runnable runnable) {
   CompletableFuture.runAsync(runnable);
  }

  /**
   * Executes a callable task asynchronously and returns a CompletableFuture. Returns null to
   * indicate error if an exception occurs during execution.
   *
   * @param callable The callable task to execute.
   * @param <R> The type of result returned by the task.
   * @return A CompletableFuture representing the result of the task.
   */
  public static <R> CompletableFuture<R> run(Callable<R> callable) {
    // Execute the task asynchronously and handle exceptions
    return CompletableFuture.supplyAsync(
        () -> {
          try {
            return callable.call();
          } catch (Throwable throwable) {
            return null; // return null to indicate error
          }
        });
  }

  /**
   * Executes a callable task asynchronously and returns a CompletableFuture. Throws a
   * CompletionException if an exception occurs during execution.
   *
   * @param callable The callable task to execute.
   * @param <R> The type of result returned by the task.
   * @return A CompletableFuture representing the result of the task.
   */
  public static <R> CompletableFuture<R> runProvideError(Callable<R> callable) {
    // Execute the task asynchronously and handle exceptions with a CompletionException
    return CompletableFuture.supplyAsync(
        () -> {
          try {
            return callable.call();
          } catch (Throwable throwable) {
            throw new CompletionException(throwable);
          }
        });
  }

  /**
   * Executes a sequence of tasks asynchronously and handles their completion. Supports chaining
   * tasks and executing callbacks on the main UI thread.
   *
   * @param taskToRun The first task to execute.
   * @param taskToRunCallback The callback for the first task.
   * @param finalTaskToRun The final task to execute.
   * @param finalTaskToRunCallback The callback for the final task.
   * @param <R> The type of result returned by the tasks.
   */
  public static <R> void runNonCancelable(
      Callable<R> taskToRun,
      CallbackWithError<R> taskToRunCallback,
      Callable<R> finalTaskToRun,
      CallbackWithError<R> finalTaskToRunCallback) {
    // Execute the first task asynchronously and handle completion
    CompletableFuture.supplyAsync(
            () -> {
              try {
                return taskToRun.call();
              } catch (Throwable throwable) {
                throw new CompletionException(throwable);
              }
            })
        .whenComplete(
            (result, throwable) -> {
              // Execute the first callback on the main UI thread
              MainThreadExecutor.getInstance()
                  .execute(
                      () -> {
                        taskToRunCallback.onComplete(result, throwable);
                      });
            })
        .thenApplyAsync(
            result -> {
              // Execute the final task asynchronously and handle completion
              try {
                return finalTaskToRun.call();
              } catch (Throwable throwable) {
                throw new CompletionException(throwable);
              }
            },
            MainThreadExecutor.getInstance())
        .whenCompleteAsync(
            (result, throwable) -> {
              // Execute the final callback on the main UI thread
              finalTaskToRunCallback.onComplete(result, throwable);
            },
            MainThreadExecutor.getInstance());
  }

  /**
   * Executes a runnable task asynchronously and returns a CompletableFuture.
   *
   * @param runnable The runnable task to execute.
   * @return A CompletableFuture representing the result of the task.
   */
  public static CompletableFuture<Void> run(Runnable runnable) {
    // Execute the runnable task asynchronously on the main UI thread
    return run(runnable, MainThreadExecutor.getInstance());
  }

  /**
   * Executes a runnable task asynchronously with the specified executor and returns a
   * CompletableFuture.
   *
   * @param runnable The runnable task to execute.
   * @param executor The executor to use for executing the task.
   * @return A CompletableFuture representing the result of the task.
   */
  public static CompletableFuture<Void> run(Runnable runnable, Executor executor) {
    // Execute the runnable task asynchronously with the specified executor
    return CompletableFuture.runAsync(runnable, executor);
  }
  
  /**
   * Posts the runnable into the UI thread to be run later after the specified amount of time
   * elapses.
   *
   * <p>The time-base is {@link android.os.SystemClock#uptimeMillis}.
   *
   * @param runnable The Runnable that will be executed.
   * @param delay The delay (in milliseconds) until the Runnable will be executed.
   */
  public static void runLaterOnUiThread(Runnable runnable, long delay) {
    MainThreadExecutor.getInstance().executeAfterDelay(runnable, delay);
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
   * Cancels a scheduled runnable by removing it from the UI thread's message queue.
   *
   * @param runnable The runnable to cancel.
   */
  public static void cancelRunLater(Runnable runnable) {
    // Cancel the execution of the specified runnable on the main UI thread
    MainThreadExecutor.getInstance().cancelExecute(runnable);
  }

  /**
   * Executes a callable task asynchronously and invokes the provided callback upon completion.
   * Handles task completion without error.
   *
   * @param callable The callable task to execute.
   * @param callback The callback to invoke upon task completion.
   * @param <R> The type of result returned by the task.
   */
  public static <R> void execute(Callable<R> callable, Callback<R> callback) {
    // Execute the task asynchronously using the executor
    executor.execute(
        () -> {
          try {
            // Call the callable task and get the result
            final R result = callable.call();
            // Execute the callback on the main UI thread
            MainThreadExecutor.getInstance()
                .execute(
                    () -> {
                      callback.onComplete(result);
                    });
          } catch (Throwable th) {
            // Log any exceptions that occur during execution
            Log.e(LOG_TAG, "Callable task was not able to finish", th);
          }
        });
  }
  
  /**
   * Executes a runnable task asynchronously.
   *
   * @param runnable The runnable task to execute.
   */
  public static CompletableFuture<Void> execute(Runnable async) {
     return CompletableFuture.runAsync(async);
  }
  
  /**
   * Executes a callable task asynchronously and invokes the provided callback upon completion.
   * Handles task completion with error.
   *
   * @param callable The callable task to execute.
   * @param callback The callback to invoke upon task completion (with error).
   * @param <R> The type of result returned by the task.
   */
  public static <R> void executeProvideError(Callable<R> callable, CallbackWithError<R> callback) {
    // Execute the task asynchronously using the executor
    executor.execute(
        () -> {
          Throwable error = null;
          R result = null;
          try {
            // Call the callable task and get the result
            result = callable.call();
          } catch (Throwable th) {
            // Log any exceptions that occur during execution and capture the error
            Log.e(LOG_TAG, "Callable task was not able to finish", th);
            error = th;
          }
          // Final variables to capture the result and error for the callback
          final R resultCopied = result;
          final Throwable errorCopied = error;

          // Execute the callback on the main UI thread
          MainThreadExecutor.getInstance()
              .execute(
                  () -> {
                    callback.onComplete(resultCopied, errorCopied);
                  });
        });
  }

  private static class MainThreadExecutor implements Executor {

    private static final Handler HANDLER = new Handler(Looper.getMainLooper());
    private static MainThreadExecutor SINGLETON_INSTANCE = null;

    private MainThreadExecutor() {
      // No-op
    }

    public static MainThreadExecutor getInstance() {
      if (SINGLETON_INSTANCE == null) {
        SINGLETON_INSTANCE = new MainThreadExecutor();
      }
      return SINGLETON_INSTANCE;
    }

    /**
     * Return whether the thread is the main thread.
     *
     * @return {@code true}: yes<br>
     *     {@code false}: no
     */
    public static boolean isMainThread() {
      return Looper.myLooper() == Looper.getMainLooper();
    }

    /**
     * Executes the given action {@code runnable} on the main UI thread immediately or posts it to the UI
     * thread's message queue if called from a background thread.
     *
     * @param runnable The runnable task to execute.
     */
    @Override
    public void execute(Runnable runnable) {
      // Run the task on the UI thread if already on the main thread, otherwise post it
      if (isMainThread()) {
        runnable.run();
      } else {
        HANDLER.post(runnable);
      }
    }

    /**
     * Posts the runnable into the UI thread to be run later after the specified amount of time
     * elapses.
     *
     * <p>The time-base is {@link android.os.SystemClock#uptimeMillis}.
     *
     * @param runnable The Runnable that will be executed.
     * @param delayMills The delay (in milliseconds) until the Runnable will be executed.
     */
    public void executeAfterDelay(final Runnable runnable, long delayMills) {
      HANDLER.postDelayed(runnable, delayMills);
    }

    /**
     * Remove any pending posts of runnable that are in the message queue.
     *
     * @param runnable The runnable to remove
     */
    public void cancelExecute(Runnable runnable) {
      HANDLER.removeCallbacks(runnable);
    }
  }
}
