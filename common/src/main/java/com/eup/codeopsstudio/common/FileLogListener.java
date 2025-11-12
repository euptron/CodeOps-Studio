package com.eup.codeopsstudio.common;

import android.content.Context;
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
 */
public class FileLogListener implements ILog.LogListener {

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
    // This path is easily accessible via a device file manager:
    // Android/data/your.package.name/files/
    File storageDir = context.getExternalFilesDir(null);
    if (storageDir != null && !storageDir.exists()) {
      storageDir.mkdirs();
    }
    this.logFile = new File(storageDir, fileName);

    // Start the background thread that will process the log queue.
    executor.submit(this::processLogQueue);

    Log.d("FileLogListener", "Log file initialized at: " + logFile.getAbsolutePath());
  }

  /**
   * This method is called by the ILog system. It's very fast because it only adds the message to a
   * queue and returns immediately.
   */
  @Override
  public void onLog(String formattedMessage) {
    if (isRunning) {
      logQueue.offer(formattedMessage);
    }
  }

  /**
   * This method runs on a dedicated background thread. It waits for messages from the queue and
   * writes them to the file.
   */
  private void processLogQueue() {
    // Using try-with-resources to ensure the writer is always closed.
    // The 'false' in FileWriter means the file will be overwritten on each app start.
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, false))) {
      writer.write("--- Log Session Started ---\n\n");
      writer.flush();

      while (isRunning || !logQueue.isEmpty()) {
        // take() will wait patiently if the queue is empty, consuming no CPU.
        String logMessage = logQueue.take();

        // A special message to tell the thread to shut down gracefully.
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

  /** Stops the logger gracefully, ensuring any pending logs are written. */
  public void stop() {
    if (isRunning) {
      isRunning = false;
      // This special message will unblock the queue and terminate the loop.
      logQueue.offer("SHUTDOWN_LOGGER");
      executor.shutdown();
    }
  }
}
