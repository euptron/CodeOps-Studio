package com.eup.codeopsstudio.git;

import org.eclipse.jgit.lib.BatchingProgressMonitor;

public class BatchProgressMonitor extends BatchingProgressMonitor {

  private String url;
  private CloneListener listener;

  public BatchProgressMonitor(CloneListener listener, String url) {
    this.url = url;
    this.listener = listener;
  }

  @Override
  protected void onUpdate(String taskName, int workCurr) {
    String msg = String.format("[%s] %s %d", url, taskName, workCurr);
    listener.onUpdateMessage(msg);
  }

  @Override
  protected void onEndTask(String taskName, int workCurr) {
    String msg = String.format("[%s] %s %d", url, taskName, workCurr);
    listener.onUpdateMessage(msg);
  }

  @Override
  protected void onUpdate(String taskName, int workCurr, int workTotal, int percentDone) {
    String msg =
        String.format("[%s] %s (%d/%d) %d", url, taskName, workCurr, workTotal, percentDone);
    listener.onUpdateMessage(msg);
    listener.onProgress(percentDone);
  }

  @Override
  protected void onEndTask(String taskName, int workCurr, int workTotal, int percentDone) {
    String msg =
        String.format("[%s] %s (%d/%d) %d", url, taskName, workCurr, workTotal, percentDone);
    listener.onUpdateMessage(msg);
  }
}
