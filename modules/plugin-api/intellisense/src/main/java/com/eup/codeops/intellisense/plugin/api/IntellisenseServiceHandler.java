package com.eup.codeops.intellisense.plugin.api;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import java.lang.ref.WeakReference;

public class IntellisenseServiceHandler extends Handler {

  public static final String TAG = "IntellisenseServiceHandler";

  private final WeakReference<IntellisenseService> mService;

  public IntellisenseServiceHandler(IntellisenseService instance) {
    super(Looper.getMainLooper());
    mService = new WeakReference<>(instance);
  }

  @Override
  public void handleMessage(Message msg) {
    super.handleMessage(msg);
    IntellisenseService service = mService.get();
    if (service == null) return;

    switch (msg.what) {
      case IntellisenseService.MSG_START_LSP:
        int port = msg.arg1;
        service.start(port, msg.replyTo);
        Log.i(TAG, "handleMessage called");
        break;
      case IntellisenseService.MSG_SHUTDOWN:
        service.stop();
        break;
      case IntellisenseService.MSG_PROCESS_REQUEST:
        service.handleProcessRequest(msg.obj != null ? msg.obj.toString() : "", msg.arg1, msg.arg2);
        break;
      default:
        Log.w(TAG, "Unknown message type: " + msg.what);
    }
  }
}
