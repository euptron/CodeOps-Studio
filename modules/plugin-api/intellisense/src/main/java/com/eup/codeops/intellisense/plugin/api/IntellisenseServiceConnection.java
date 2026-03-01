package com.eup.codeops.intellisense.plugin.api;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import java.util.Arrays;

public class IntellisenseServiceConnection implements ServiceConnection {

  public static final String TAG = "IntellisenceServiceConnection";

  private int pendingPort = -1;
  private volatile boolean isConnected;
  private Messenger messenger;
  private Messenger replyMessenger;

  private ConnectionCallback callback;
  private final Handler incomingHandler =
      new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
          switch (msg.what) {
            case IntellisenseService.MSG_SERVER_READY:
              int port = msg.arg1;
              Log.d(TAG, "Server ready on port: " + port);
              if (callback != null) callback.onConnected();
              break;
            default:
              Log.d(TAG, "Received message: " + msg.what);
          }
        }
      };

  public IntellisenseServiceConnection(ConnectionCallback callback) {
    this.callback = callback;
    this.replyMessenger = new Messenger(incomingHandler);
  }

  @Override
  public void onServiceConnected(ComponentName name, IBinder binder) {
    try {
      Log.d(TAG, "onServiceConnected called for: " + name);
      Log.d(TAG, "Binder class: " + binder.getClass().getName());
      Log.d(TAG, "Binder toString: " + binder.toString());

      if (!binder.pingBinder()) {
        Log.e(TAG, "Binder is not alive!");
        if (callback != null) callback.onError(new Exception("Binder not alive"));
        return;
      }
      messenger = new Messenger(binder);
      isConnected = true;

      if (pendingPort != -1) {
        Log.i(TAG, "Service connected, sending pending port: " + pendingPort);
        sendMessage(IntellisenseService.MSG_START_LSP, pendingPort, 0, null);
        pendingPort = -1;
      } else {
        Log.i(TAG, "Service connected, no pending port");
      }
    } catch (Exception e) {
      Log.e(TAG, "Failed to create Messenger from binder", e);
      // Try to extract more info
      if (binder != null) {
        Log.e(TAG, "Binder class: " + binder.getClass().getName());
        Log.e(TAG, "Binder interface: " + Arrays.toString(binder.getClass().getInterfaces()));
      }
      messenger = null;
      isConnected = false;
      if (callback != null) {
        callback.onError(e);
      }
    }
  }

  @Override
  public void onServiceDisconnected(ComponentName name) {
    Log.i(TAG, "Service disconnected");
    sendMessage(IntellisenseService.MSG_SHUTDOWN, 0, 0, null);
    messenger = null;
    isConnected = false;
    if (callback != null) {
      callback.onDisconnected();
    }
  }

  // -- FOR TESTING
  @Override
  public void onBindingDied(ComponentName name) {
    Log.w(TAG, "Binding died for service: " + name);
    messenger = null;
    isConnected = false;
  }

  @Override
  public void onNullBinding(ComponentName name) {
    Log.w(TAG, "Null binding for service: " + name);
    messenger = null;
    isConnected = false;
  }

  // --- TESTING END

  public boolean isConnected() {
    return this.isConnected;
  }

  public void start(int port) {
    Log.d(TAG, "start() called, isConnected=" + isConnected + ", port=" + port);
    if (isConnected) {
      sendMessage(IntellisenseService.MSG_START_LSP, port, 0, null);
    } else {
      pendingPort = port;
      Log.d(TAG, "Port stored as pending: " + port);
    }
  }

  public void stop() {
    sendMessage(IntellisenseService.MSG_SHUTDOWN, 0, 0, null);
  }

  public void processRequest(String requestType, int data, int requestId) {
    if (isConnected) {
      sendMessage(IntellisenseService.MSG_PROCESS_REQUEST, data, requestId, requestType);
    }
  }

  private void sendMessage(int what, int arg1, int arg2, Object obj) {
    if (messenger == null) {
      Log.e(TAG, "Messenger is null, cannot send message");
      return;
    }

    Message msg = Message.obtain(null, what);
    try {
      msg.arg1 = arg1;
      msg.arg2 = arg2;
      msg.obj = obj;
      if (what == IntellisenseService.MSG_START_LSP) {
        msg.replyTo = replyMessenger; // offer for 2 way connection
      }
      Log.d(TAG, "Sending message: " + what + " with port: " + arg1);
      messenger.send(msg);
    } catch (RemoteException e) {
      Log.e(TAG, "Failed to send message " + what, e);
      if (callback != null) {
        callback.onError(e);
      }
    } finally {
      msg.recycle();
    }
  }

  public interface ConnectionCallback {
    void onConnected();

    void onDisconnected();

    void onError(Exception e);
  }
}
