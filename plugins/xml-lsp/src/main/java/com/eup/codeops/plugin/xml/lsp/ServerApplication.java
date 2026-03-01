package com.eup.codeops.plugin.xml.lsp;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Process;
import android.util.Log;
import androidx.annotation.NonNull;
import com.eup.codeops.plugin.xml.lsp.logging.FileLogListener;
import com.eup.codeops.plugin.xml.lsp.logging.LogInterceptor;
import com.eup.codeops.plugin.xml.lsp.logging.LogManager;

public class ServerApplication extends Application
    implements Thread.UncaughtExceptionHandler {

  private static final String TAG = "ServerApplication";
  private static ServerApplication instance;
  private LogManager logManager;

  @Override
  public void onCreate() {
      Thread.setDefaultUncaughtExceptionHandler(this);
    super.onCreate();
    instance = this;

    logManager =
        new LogManager.Builder(this)
            .attachObserver(new FileLogListener(this, "xml-lsp_log.txt"))
            .applyFilter(LogInterceptor.Filter.byPID(Process.myPid()))
            .build();
    logManager.start();
  }

  @Override
  public void uncaughtException(@NonNull Thread thread, @NonNull Throwable throwable) {
      Log.e(TAG, "Crash in thread: " + thread.getName() + ", uncaught exception: " +Log.getStackTraceString(throwable));
  }

  @Override
  public void onTerminate() {
    if (logManager != null && logManager.isRunning()) {
      logManager.dispose();
    }
    super.onTerminate();
  }

  public static ServerApplication getInstance() {
    return instance;
  }

  public static Context getGlobalContext() {
    return instance.getApplicationContext();
  }
}
