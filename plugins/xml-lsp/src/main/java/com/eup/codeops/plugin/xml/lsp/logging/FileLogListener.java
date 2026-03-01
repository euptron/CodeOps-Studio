package com.eup.codeops.plugin.xml.lsp.logging;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * An ILog.LogListener that writes all received log messages to a file on a background thread. This
 * is designed to be fast and non-blocking, making it safe to use for debugging freezes.
 *
 * @author Etido Peter
 */
public class FileLogListener implements ILogObserver {

  private final BlockingQueue<String> logQueue = new LinkedBlockingQueue<>();
  private final ExecutorService executor = Executors.newSingleThreadExecutor();
  private final File logFile;
  private volatile boolean isRunning = true;

  /**
   * Initializes the logger. It will overwrite the specified file on each new app session.
   *
   * @param context App context to resolve file paths.
   * @param fileName The name of the log file to create (e.g., "debug_log.txt").
   */
  public FileLogListener(Context context, String fileName) {
    // File storageDir = /* context.getExternalFilesDir(null)*/ new
    File storageDir =
        new File(
            Environment.getExternalStorageDirectory(), "Documents/" + context.getPackageName());
    if (storageDir != null && !storageDir.exists()) {
      storageDir.mkdirs();
    }

    this.logFile = new File(storageDir, fileName);
    executor.submit(this::processLogQueue);
    Log.d("FileLogListener", "Log file initialized at: " + logFile.getAbsolutePath());
  }

  @Override
  public void onLog(String formattedMessage) {
    if (isRunning) {
      logQueue.offer(formattedMessage);
    }
  }

  private void processLogQueue() {
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, /* append= */ true))) {
      writer.write("--- Log Session Started ---\n\n");
      writer.flush();

      while (isRunning || !logQueue.isEmpty()) {
        String logMessage = logQueue.take();

        if ("SHUTDOWN_LOGGER".equals(logMessage)) {
          break;
        }

        writer.write(logMessage + "\n");

        // To ensure logs are written before a crash/freeze, we flush more often.
        // For debugging freezes, performance is less critical than getting the data.
        writer.flush();
      }
    } catch (IOException e) {
      Log.e("FileLogListener", "Error writing to log file", e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt(); // Preserve the interrupted status
      Log.e("FileLogListener", "Log processing thread was interrupted.", e);
    }
  }

  public void stop() {
    if (isRunning) {
      isRunning = false;
      logQueue.offer("SHUTDOWN_LOGGER");
      executor.shutdown();
    }
  }
}
