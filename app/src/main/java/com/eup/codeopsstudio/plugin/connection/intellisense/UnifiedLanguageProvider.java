package com.eup.codeopsstudio.plugin.connection.intellisense;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import com.eup.codeops.common.plugin.api.sandbox.Plugin;
import com.eup.codeops.intellisense.plugin.api.IntellisenseServiceConnection;
import com.eup.codeopsstudio.IdeApplication;
import com.eup.codeopsstudio.common.AsyncTask;
import com.eup.codeopsstudio.common.ILog;
import com.eup.codeopsstudio.editor.ContextualCodeEditor;
import com.eup.codeopsstudio.logger.Logger;
import com.eup.codeopsstudio.plugin.PluginItem;
import com.eup.codeopsstudio.plugin.PluginScanner;
import com.eup.codeopsstudio.plugin.connection.lsp.LSPConnectionProvider;
import io.github.rosemoe.sora.lang.Language;
import io.github.rosemoe.sora.langs.textmate.TextMateLanguage;
import io.github.rosemoe.sora.lsp.client.languageserver.requestmanager.DefaultRequestManager;
import io.github.rosemoe.sora.lsp.client.languageserver.requestmanager.RequestManager;
import io.github.rosemoe.sora.lsp.client.languageserver.serverdefinition.CustomLanguageServerDefinition;
import io.github.rosemoe.sora.lsp.client.languageserver.serverdefinition.LanguageServerDefinition;
import io.github.rosemoe.sora.lsp.client.languageserver.wrapper.EventHandler;
import io.github.rosemoe.sora.lsp.editor.LspEditor;
import io.github.rosemoe.sora.lsp.editor.LspEditorManager;
import io.github.rosemoe.sora.lsp.operations.signature.SignatureHelpProvider;
import io.github.rosemoe.sora.lsp.utils.URIUtils;
import java.io.File;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;
import org.eclipse.lsp4j.DidChangeWorkspaceFoldersParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.WorkspaceFolder;
import org.eclipse.lsp4j.WorkspaceFoldersChangeEvent;
import org.eclipse.lsp4j.services.LanguageServer;

public class UnifiedLanguageProvider {

  public static final String TAG = "UnifiedLanguageProvider";

  private Logger logger;
  private LspEditor lspEditor;
  private String languageExtension;
  private File workspaceFolder;
  private final PluginScanner pluginScanner;
  private final ContextualCodeEditor codeEditor;
  private boolean supportsULPFormatting;
  private final ExecutorService executorService = Executors.newSingleThreadExecutor();

  public UnifiedLanguageProvider(
      @NonNull Logger logger,
      @NonNull ContextualCodeEditor codeEditor,
      @NonNull String languageExtension) {
    this(logger, codeEditor, languageExtension, null);
  }

  /**
   * languageextension must be .ext not ext If workspace folder is null its sssumed that we want to
   * serve lsp for s single file so we assign the editor.getFile#parentFile if avialble to the
   * workspace folder.
   */
  public UnifiedLanguageProvider(
      @NonNull Logger logger,
      @NonNull ContextualCodeEditor codeEditor,
      @NonNull String languageExtension,
      @Nullable File workspaceFolder) {
    this.logger = logger;
    this.codeEditor = codeEditor;
    this.languageExtension = languageExtension;
    this.workspaceFolder = workspaceFolder;
    this.pluginScanner = IdeApplication.getPluginScanner();
  }

  public void start() {
    executorService.execute(this::launchLanguageIntellisense);
  }

  public void stop() {
    pluginScanner.detachAllPlugins();
    //if (!codeEditor.isReleased()) codeEditor.release();
    LspEditorManager.closeAllManager();
    executorService.shutdownNow();
  }

  private void launchLanguageIntellisense() {
    AsyncTask.runOnUiThread(
        () -> {
          logger.i(TAG, "Scanning for intellisense plugin...");
          codeEditor.setEditable(false);
        });

    pluginScanner.scan();
    List<Plugin<?>> plugins = pluginScanner.getPluginsForExtension(languageExtension);

    for (Plugin<?> plugin : plugins) {
      if (plugin != null) {
          ILog.info(TAG, "Found Plugin " + plugin.getId());
        if (pluginScanner.isRegistered(plugin.getId())) {
          connectToPlugin(plugin.getId(), generateRandomPort());
          break;
        }
      }
    }
  }

  private void connectToPlugin(String pluginId, int port) {
    try {
      File currentFile = codeEditor.getFile();
      if (currentFile == null) return;

      workspaceFolder = (workspaceFolder == null) ? currentFile.getParentFile() : workspaceFolder;
      if (workspaceFolder == null) return;

      final String projectPath = workspaceFolder.getAbsolutePath();
      final String fileUri = URIUtils.fileToURI(currentFile).toString();
      final String projectUri = URIUtils.fileToURI(workspaceFolder).toString();

      ILog.info(TAG, "Current File Uri:" + fileUri);
      ILog.info(TAG, "Project path: " + projectPath);
      ILog.info(TAG, "Current File path: " + currentFile.getAbsolutePath());

      var callback =
          new IntellisenseServiceConnection.ConnectionCallback() {
            @Override
            public void onConnected() {
              AsyncTask.runOnUiThread(() -> logger.i(TAG, pluginId + " service connected"));
            }

            @Override
            public void onDisconnected() {
              AsyncTask.runOnUiThread(() -> logger.i(TAG, pluginId + " service disconnected"));
            }

            @Override
            public void onError(Exception e) {
              AsyncTask.runOnUiThread(() -> logger.e(TAG, "Error connecting to " + pluginId, e));
            }
          };

      boolean attached = pluginScanner.attachPlugin(pluginId, port, callback);

      if (!attached) {
        AsyncTask.runOnUiThread(() -> logger.w(TAG, "Failed to attach " + pluginId + " plugin"));
        return;
      }

      try {
        final var lock = new Object();

        AsyncTask.runOnUiThread(
            () -> {
              var serverDefinition = createLanguageServerDefinition(languageExtension, port);
              LspEditorManager manager = LspEditorManager.getOrCreateEditorManager(projectPath);
              lspEditor = manager.createEditor(fileUri, serverDefinition);

              lspEditor.setWrapperLanguage(configWrapperLanguage());
              lspEditor.setEditor(codeEditor);
              synchronized (lock) {
                lock.notify();
              }
            });

        synchronized (lock) {
          lock.wait();
        }

        lspEditor.connectWithTimeout();
        configLanguageServer(projectUri, port);
      } catch (InterruptedException e) {
        ILog.error(TAG, "Plugin connection Interrupted...", e);
      } catch (TimeoutException te) {
        ILog.error(TAG, "Plugin connection time out...", te);
      }
    } catch (Exception e) {
      AsyncTask.runOnUiThread(() -> logger.e(TAG, "Plugin connection failed " + e.getMessage(), e));
    }
  }

  private void configLanguageServer(String projectUri, int port) {
    try {
      // remove unsupported server capabilities  to prevent the NPE crash.
      var rm = (DefaultRequestManager) lspEditor.getRequestManager();

      if (rm != null) {
        ServerCapabilities caps = rm.getServerCapabilities();

        if (caps != null) {
          if (caps.getSignatureHelpProvider() == null) {
            lspEditor.getProviderManager().removeProvider(SignatureHelpProvider.class);
            ILog.info(TAG, "SignatureHelpProvider removed because the server does not support it.");
          }
        }
      }

      List<WorkspaceFolder> added = new ArrayList<>();
      List<WorkspaceFolder> removed = new ArrayList<>();

      added.add(new WorkspaceFolder(projectUri, workspaceFolder.getName()));

      var changeEvent = new WorkspaceFoldersChangeEvent(added, removed);
      RequestManager requestManager = lspEditor.getRequestManager();
      requestManager.didChangeWorkspaceFolders(new DidChangeWorkspaceFoldersParams(changeEvent));
      AsyncTask.runOnUiThread(
          () -> {
            codeEditor.setEditable(true);
            logger.i(TAG, "Plugin Initialized at port: " + port);
          });
    } catch (Exception e) {
      AsyncTask.runOnUiThread(
          () -> {
            codeEditor.setEditable(true);
            logger.e(TAG, "Error configuring language server: " + e.getMessage(), e);
          });
    }
  }

  private Language configWrapperLanguage() {
    if (codeEditor.getEditorLanguage() instanceof TextMateLanguage tml) {
      tml.setAutoCompleteEnabled(false); // false to use  ULP completions
      return tml;
    }
    return codeEditor.getEditorLanguage();
  }

  private int generateRandomPort() {
    try {
      ServerSocket socket = new ServerSocket(0);
      int port = socket.getLocalPort();
      socket.close();
      return port;
    } catch (Exception e) {
      ILog.error(TAG, "Error generating random port: " + e.getMessage(), e);
      return 0;
    }
  }

  private LanguageServerDefinition createLanguageServerDefinition(String fileExtension, int port) {
    return new CustomLanguageServerDefinition(
        fileExtension, workingDir -> LSPConnectionProvider.getInstanceNoTimeOut(() -> port)) {
      @Override
      public EventHandler.EventListener getEventListener() {
        return new EventListener();
      }
    };
  }

  class EventListener implements EventHandler.EventListener {
    @Override
    public void initialize(@Nullable LanguageServer server, @NonNull InitializeResult result) {
      EventHandler.EventListener.super.initialize(server, result);
      supportsULPFormatting = result.getCapabilities().getDocumentFormattingProvider() != null;
    }
  }
}