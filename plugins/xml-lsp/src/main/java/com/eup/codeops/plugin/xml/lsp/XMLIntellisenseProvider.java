package com.eup.codeops.plugin.xml.lsp;

import android.os.Messenger;
import android.util.Log;
import androidx.annotation.NonNull;
import com.eup.codeops.intellisense.plugin.api.IntellisenseService;
import org.eclipse.lemminx.XMLLanguageServer;
import org.eclipse.lemminx.customservice.XMLLanguageClientAPI;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.services.LanguageServer;

public class XMLIntellisenseProvider extends IntellisenseService<XMLLanguageClientAPI> {

  public static final String TAG = "XMLIntellisenseProvider";

  // In XMLIntellisenseProvider:
  @Override
  public void start(int port, Messenger replyTo) {
    Log.d(TAG, "XMLIntellisenseProvider.start() called");
    super.start(port, replyTo);
  }

  @Override
  public LanguageServer attachLanguageServer() {
    return new XMLLanguageServer();
  }

  @Override
  public void handShake(LanguageServer languageServer, Launcher<XMLLanguageClientAPI> launcher) {
    Log.i(TAG, "Handshake success");
    try {
      if (languageServer instanceof XMLLanguageServer server) {
        server.setClient(launcher.getRemoteProxy());
      }
    } catch (Exception e) {
      Log.e(TAG, "Handshake failed", e);
    }
  }

  @NonNull
  @Override
  public String id() {
    return "xml";
  }

  @Override
  public Class<XMLLanguageClientAPI> getClientInterface() {
    return XMLLanguageClientAPI.class;
  }

  @Override
  public void handleProcessRequest(String requestType, int data, int requestId) {
    // TODO: Handle specific requests
  }
}
