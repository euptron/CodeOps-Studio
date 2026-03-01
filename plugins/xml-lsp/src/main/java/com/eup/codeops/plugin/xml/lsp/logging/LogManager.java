package com.eup.codeops.plugin.xml.lsp.logging;

import android.content.Context;
import androidx.annotation.NonNull;

public class LogManager {

  private final LogInterceptor interceptor;

  private LogManager(Builder builder) {
    this.interceptor = builder.interceptor;
  }

  public void start() {
    interceptor.start();
  }

  public void stop() {
    interceptor.stop();
  }

  public void clearObserver() {
    interceptor.clearObserver();
  }

  public void detachObserver(@NonNull ILogObserver observer) {
    interceptor.detachObserver(observer);
  }

  public boolean isRunning() {
    return interceptor.isRunning();
  }

  public void dispose() {
    interceptor.dispose();
  }

  public static class Builder {

    protected final LogInterceptor interceptor;

    public Builder(Context context) {
      this.interceptor = LogInterceptor.get(context);
    }

    public Builder applyFilter(LogInterceptor.Filter filter) {
      interceptor.setFilter(filter);
      return this;
    }

    public Builder attachObserver(@NonNull ILogObserver observer) {
      interceptor.attachObserver(observer);
      return this;
    }

    public LogManager build() {
      return new LogManager(this);
    }
  }
}
