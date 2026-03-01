package com.eup.codeops.intellisense.plugin.api;

import android.os.Messenger;

public interface LanguageServiceProvider {
  void start(int port, Messenger replyTo);

  void stop();
}
