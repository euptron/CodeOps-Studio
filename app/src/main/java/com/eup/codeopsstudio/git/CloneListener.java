package com.eup.codeopsstudio.git;

import java.io.File;

public interface CloneListener {

  void onCloneSuccess(File file);

  void onCloneFailed(String e);

  void onUpdateMessage(String message);

  void onProgress(int progress);
}
