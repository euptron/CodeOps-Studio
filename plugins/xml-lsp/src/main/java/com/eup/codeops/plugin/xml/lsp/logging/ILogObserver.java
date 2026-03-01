package com.eup.codeops.plugin.xml.lsp.logging;

import androidx.annotation.Nullable;

public interface ILogObserver {
  void onLog(@Nullable String query);
}
