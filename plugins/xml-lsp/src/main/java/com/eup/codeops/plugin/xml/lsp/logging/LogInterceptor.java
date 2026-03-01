package com.eup.codeops.plugin.xml.lsp.logging;

import android.content.Context;
import android.os.Process;
import android.util.Log;
import androidx.annotation.NonNull;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LogInterceptor {

  private static final String TAG = "LogInterceptor";

  private Filter filter;
  private final Context context;
  private java.lang.Process logcatProcess;
  private static LogInterceptor instance;
  private volatile boolean isRunning = false;
  private volatile boolean shouldStop = false;
  private final Set<ILogObserver> observers = ConcurrentHashMap.newKeySet();
  private final ExecutorService executorService = Executors.newSingleThreadExecutor();

  private LogInterceptor(Context context) {
    this.context = context.getApplicationContext();
  }

  public static synchronized LogInterceptor get(Context context) {
    if (instance == null) instance = new LogInterceptor(context);
    return instance;
  }

  public void attachObserver(@NonNull ILogObserver observer) {
    observers.add(Objects.requireNonNull(observer));
  }

  public void detachObserver(@NonNull ILogObserver observer) {
    observers.remove(Objects.requireNonNull(observer));
  }

  public void clearObserver() {
    observers.clear();
  }

  public void setFilter(Filter filter) {
    this.filter = filter;
  }

  public void start() {
    if (isRunning) {
      notifyObservers(TAG + " is already running");
      return;
    }
    executorService.execute(this::run);
  }

  public void stop() {
    shouldStop = true;
    isRunning = false;

    if (logcatProcess != null) {
      try {
        logcatProcess.destroy();
      } catch (Exception e) {
        Log.e(TAG, "Error destroying logcat process: " + e.getMessage());
      }
      logcatProcess = null;
    }

    Log.i(TAG, "Log interceptor stopped");
  }

  public boolean isRunning() {
    return isRunning;
  }

  public void dispose() {
    stop();
    executorService.shutdown();
    instance = null;
  }

  private void run() {
    notifyObservers(TAG + "=========STARTED=========");
    try {
      isRunning = true;
      shouldStop = false;

      clear();

      Filter commandFilter = filter == null ? Filter.get() : filter;

      logcatProcess = Runtime.getRuntime().exec(commandFilter.buildCommand());
      read();
    } catch (Exception e) {
      notifyObservers("Failed to start log interception, " + e.getMessage());
    } finally {
      stop();
      notifyObservers(TAG + "==========STOPPED==========");
    }
  }

  private void read() throws IOException {
    InputStream inputStream = logcatProcess.getInputStream();
    InputStreamReader streamReader = new InputStreamReader(inputStream);
    char[] buffer = new char[8192];
    StringBuilder lineBuffer = new StringBuilder();

    while (!shouldStop) {
      int bytesRead = streamReader.read(buffer, 0, buffer.length);
      if (bytesRead == -1) break;

      // Process the characters
      for (int i = 0; i < bytesRead; i++) {
        char c = buffer[i];
        lineBuffer.append(c);

        if (c == '\n') {
          notifyObservers(lineBuffer.toString());
          lineBuffer.setLength(0);
        }
      }

      try {
        Thread.sleep(10);
      } catch (InterruptedException ignored) {
        Thread.currentThread().interrupt();
        break;
      }
    }
    streamReader.close();
  }

  private void clear() {
    try {
      Runtime.getRuntime().exec("logcat -c");
    } catch (IOException e) {
      Log.w(TAG, "Could not clear logcat buffer: " + e.getMessage());
    }
  }

  private void notifyObservers(String query) {
    try {
      for (ILogObserver observer : observers) {
        observer.onLog(query);
      }
    } catch (Throwable th) {
      Log.e(TAG, "Failed to notify LogObservers: " + th.getMessage(), th);
    }
  }

  public static class Filter {

    private int pid = -1;
    private String packageName;
    private String[] customFilter;

    public static Filter get() {
      return new Filter(-1, null, null);
    }

    public static Filter byPID(int pid) {
      return new Filter(pid, null, null);
    }

    public static Filter byPackageName(String pkgName) {
      return new Filter(-1, pkgName, null);
    }

    public static Filter byCustomFilter(String[] customFilter) {
      return new Filter(-1, null, customFilter);
    }

    private Filter(int pid, String packageName, String[] customFilter) {
      this.pid = pid;
      this.packageName = packageName;
      this.customFilter = customFilter;
    }

    public static Filter fromContext(Context context) {
      return new Filter(-1, context.getPackageName(), null);
    }

    public String[] buildCommand() {
      List<String> command = new ArrayList<>();
      command.add("logcat");
      command.add("-v");
      command.add("threadtime");

      if (pid != -1) {
        command.add("--pid=" + pid);
      } else if (packageName != null) {
        command.add(packageName + ":V");
        command.add("*:S");
      } else if (customFilter != null && customFilter.length > 0) {
        command.addAll(Arrays.asList(customFilter));
      } else {
        command.add("*:V");
      }

      return command.toArray(new String[0]);
    }
  }
}
