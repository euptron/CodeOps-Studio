package com.eup.codeops.intellisense.plugin.api;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import java.io.Closeable;
import java.lang.ref.WeakReference;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageServer;

public abstract class IntellisenseService<ClientAPI extends LanguageClient> extends Service
    implements LanguageServiceProvider {

  // Messenger message types
  public static final int MSG_START_LSP = 1;
  public static final int MSG_SHUTDOWN = 2;
  public static final int MSG_PROCESS_REQUEST = 3;
  public static final int MSG_SERVER_READY = 4;

  private static final int NOTIFICATION_ID = 1001;
  private static final String TAG = "IntellisenseService";
  private static final String CHANNEL_ID = "IntellisenseService";
  private final ExecutorService executor = Executors.newSingleThreadExecutor();

  private Messenger messenger;
  private boolean isRunning = false;
  private LanguageServer languageServer;

  @Override
  public void onCreate() {
    super.onCreate();
    messenger = new Messenger(new IntellisenseServiceHandler(this));
    performStartForeground();
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    performStartForeground();
    return START_STICKY;
  }

  @Override
  public IBinder onBind(Intent intent) {
    if (messenger == null) {
      Log.e(TAG, "Messanger is null @onBind");
    }
    return messenger.getBinder();
  }

  @Override
  public void start(int port, Messenger replyTo) {
    Log.i(TAG, "IntellisenseProvider.start() called, port:" + port);
    if (isRunning) {
      Log.i(TAG, "IntellisenseProvider.start() already called/running");
      //      shutdown();
      // old way not work
      // return;
    }
    isRunning = true;
    executor.execute(() -> startLspServer(port, replyTo));
  }

  @Override
  public void stop() {
    isRunning = false;
    executor.shutdownNow();
    if (languageServer != null) {
      try {
        languageServer.shutdown();
      } catch (Exception e) {
        Log.e(TAG, "Error shutting down language server", e);
      }
    }
    Log.i(TAG, "Intellisense Service stopped");
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    performStopService();
  }

  public boolean isRunning() {
    return this.isRunning;
  }

  /**
   * Override to specify language id
   *
   * @return the language id
   */
  public String id() {
    return ""; // fallback
  }

  public abstract LanguageServer attachLanguageServer();

  public abstract Class<ClientAPI> getClientInterface();

  public abstract void handleProcessRequest(String requestType, int data, int requestId);

  public abstract void handShake(LanguageServer languageServer, Launcher<ClientAPI> client);

  private void performStartForeground() {
    createNotificationChannel(NotificationManager.IMPORTANCE_LOW);

    Notification notification =
        new NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Intellisense plugin")
            .setContentText(id().toUpperCase(Locale.US) + "Intellisense provider running")
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .setOngoing(true)
            .setSilent(true)
            .build();

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC);
    } else {
      startForeground(NOTIFICATION_ID, notification);
    }
  }

  private void performStopService() {
    Log.i(TAG, "Stopping Intellisense Service");
    stop();
    stopForeground(STOP_FOREGROUND_REMOVE);
    stopSelf();
  }

  private void createNotificationChannel(int importance) {
    var channel = new NotificationChannel(CHANNEL_ID, "Intellisense Service", importance);

    channel.setSound(null, null);
    channel.enableVibration(false);
    channel.setDescription("Keeps Intellisense server running");

    NotificationManager manager = getSystemService(NotificationManager.class);

    if (manager != null) {
      manager.createNotificationChannel(channel);
    }
  }

  private void startLspServer(int port, Messenger replyTo) {
    Log.i(TAG, "IntellisenseProvider.startLspServer() called, port:" + port);
    ServerSocket serverSocket = null;
    Socket clientSocket = null;

    try {
      sendMessage(replyTo, port);
      serverSocket = new ServerSocket(port);
      clientSocket = serverSocket.accept();
      Log.d(TAG, "Client connected on port " + clientSocket.getLocalPort());

      languageServer = attachLanguageServer();
      Class<ClientAPI> clientInterface = getClientInterface();
      Launcher<ClientAPI> launcher =
          Launcher.createLauncher(
              languageServer,
              clientInterface,
              clientSocket.getInputStream(),
              clientSocket.getOutputStream());

      handShake(languageServer, launcher);
      launcher.startListening().get(Long.MAX_VALUE, TimeUnit.SECONDS);
    } catch (Throwable t) {
      Log.e(TAG, "Language Server error", t);
    } finally {
      closeQuietly(clientSocket);
      closeQuietly(serverSocket);
      isRunning = false;
    }
  }

  private void closeQuietly(Closeable closeable) {
    if (closeable != null) {
      try {
        closeable.close();
      } catch (Exception e) {
        Log.e(TAG, "IntellisenseService close action error", e);
      }
    }
  }

  private void sendMessage(Messenger replyTo, int port) {
    if (replyTo == null) return;
    Message reply = Message.obtain(null, MSG_SERVER_READY);

    try {
      reply.arg1 = port;
      replyTo.send(reply);
    } catch (RemoteException e) {
      Log.e(TAG, "Failed to send message", e);
    } finally {
      //  reply.recycle();
    }
  }
}
