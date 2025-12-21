/*
 * This file is part of CodeOps Studio.
 * CodeOps Studio - Code anywhere anytime
 * https://github.com/euptron/CodeOps-Studio
 * Copyright (C) 2024-2025 Etido Peter
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/
 *
 * If you have more questions, feel free to message Etido Peter if you have any
 * questions or need additional information. Email: euptron@gmail.com
 */

package com.eup.codeopsstudio.ui.editor.panes;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;

import androidx.lifecycle.ViewModelProvider;
import com.eup.codeopsstudio.R;
import com.eup.codeopsstudio.common.AsyncTask;
import com.eup.codeopsstudio.common.Constants;
import com.eup.codeopsstudio.common.util.FileUtil;
import com.eup.codeopsstudio.databinding.LayoutPaneWebviewBinding;
import com.eup.codeopsstudio.logger.Logger;
import com.eup.codeopsstudio.pane.Pane;
import com.eup.codeopsstudio.server.LiveServer;
import com.eup.codeopsstudio.util.BaseUtil;
import com.eup.codeopsstudio.util.Wizard;

import com.eup.codeopsstudio.viewmodel.MainViewModel;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * TODO: Implement {@link #goBack()} and {@link #goForward()}
 *
 * @author Etido Peter
 */
public class WebViewPane extends Pane {

  public static final String LOG_TAG = WebViewPane.class.getSimpleName();
  public static final String KEY_PREVIEW_FILE_PATH = "preview_file_path";
  public static final String KEY_IS_ZOOMABLE = "isZoomAble";
  public static final String KEY_DESKTOP_MODE = "isDeskTopMode";
  private static final String USER_AGENT =
      "Mozilla/5.0 (Windows NT 10) AppleWebKit/537.36 "
          + "(KHTML, like Gecko) Chrome/60.0.3112.78 Safari/537.36";

  private Logger logger;
  private File fileToPreview;
  private int lastProgress = 0;
  private LiveServer liveServer;
  private LiveServer consoleServer;
  private boolean isZoomable = true;
  private boolean showConsole = true;
  private MainViewModel mainViewModel;
  private boolean isDesktopMode = false;
  private LayoutPaneWebviewBinding binding;

  public WebViewPane(Context context, String title) {
    this(context, title, /* generate new uuid= */ true);
  }

  public WebViewPane(Context context, String title, boolean generateUUID) {
    super(context, title, generateUUID);
  }

  @Override
  public View onCreateView() {
    binding = LayoutPaneWebviewBinding.inflate(LayoutInflater.from(getContext()));
    mainViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
    logger = new Logger(Logger.LogClass.IDE);
    liveServer = new LiveServer(requireContext());
    consoleServer = new LiveServer(requireContext());
    return binding.getRoot();
  }

  @Override
  public void onViewCreated(@NonNull View view) {
    super.onViewCreated(view);
    logger.attach(requireActivity());

    WebSettings webSettings = binding.webview.getSettings();
    webSettings.setMediaPlaybackRequiresUserGesture(true);
    webSettings.setBuiltInZoomControls(true);
    // Hide the +/- zoom controls
    webSettings.setDisplayZoomControls(false);
    webSettings.setAllowFileAccess(true);
    webSettings.setAllowContentAccess(true);
    // auto zoom webview content relative to a screen size
    webSettings.setLoadWithOverviewMode(true);
    webSettings.setUseWideViewPort(true);
    webSettings.setSupportMultipleWindows(true);
    webSettings.setJavaScriptEnabled(true);
    webSettings.setDomStorageEnabled(true);
    webSettings.setDefaultTextEncodingName(StandardCharsets.UTF_8.displayName());
    webSettings.setLoadsImagesAutomatically(true);
    webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
    webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
    webSettings.setAllowUniversalAccessFromFileURLs(true);
    webSettings.setAllowFileAccessFromFileURLs(true);
    // for better CSS/JS support
    webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);
    setZoomable(true);
    enableDeskTopMode(false);

    // web view -since v1.0.2
    binding.webview.setFocusable(true);
    binding.webview.setFocusableInTouchMode(true);

    binding.webview.setWebChromeClient(
        new WebChromeClient() {
          @Override
          public void onProgressChanged(WebView view, int progress) {
            if (progress > lastProgress) {
              mainViewModel.updateMainProgress(false, progress, false);
              lastProgress = progress;
            }
            if (view.getTitle() != null && Objects.equals(view.getTitle(), "about:blank")) {
              setTitle(view.getTitle());
            }
          }

          @Override
          public void onReceivedTitle(WebView view, String pageTitle) {
            super.onReceivedTitle(view, pageTitle);

            if (!Wizard.isEmpty(pageTitle)) {
              setTitle(pageTitle);
            }
          }
        });

    binding.webview.setWebViewClient(
        new WebViewClient() {
          @Override
          public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            view.loadUrl(request.getUrl().toString());
            return true;
          }

          @Override
          public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            lastProgress = 0; // reset
            mainViewModel.updateMainProgress(false, 0, false);
          }

          @Override
          public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            lastProgress = 100;
            mainViewModel.updateMainProgress(false, 100, true);

            if (showConsole && consoleServer.getUrl() != null) {
              String msg = getString(R.string.msg_console_welcome, getString(R.string.app_name));
              String initializeConsole =
                  """
                        eruda.init({
                            defaults: {
                        displaySize: 50,
                        transparency: 1,
                        theme: 'Atom One Dark'
                        }\

                        });""";
              if (consoleServer.getUrl() != null) {
                view.evaluateJavascript(
                    "javascript:(function () { var script = document.createElement"
                        + "('script'); script.src=\""
                        + consoleServer.getUrl()
                        + "\"; document.body.appendChild(script);"
                        + "script.onload = function () { "
                        + initializeConsole
                        + "let console = eruda.get('console');"
                        + "console.log('"
                        + msg
                        + "');"
                        + "\n"
                        + "} })();",
                    null);
              }
            }
          }
        });
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    if (liveServer != null) {
      liveServer.stop();
    }

    if (consoleServer != null) {
      consoleServer.stop();
    }

    if (binding.webview != null) {
      binding.webview.destroy();
    }

    liveServer = null;
    consoleServer = null;
    binding = null;
  }

  @Override
  public void onSelected() {
    super.onSelected();
    if (binding == null) {
      return;
    }

    if (fileToPreview != null && liveServer != null) {
      try {
        liveServer.setSingleFileMode(fileToPreview);
      } catch (IllegalArgumentException e) {
        logger.e(LOG_TAG, "Error setting file mode: " + e.getMessage());
      }
    }

    if (fileToPreview == null) {
      fileToPreview = liveServer.getSourceFile();
    }

    if (fileToPreview != null
        && Constants.WEB_MARKUP_LANGUAGE.stream().anyMatch(fileToPreview.getName()::endsWith)) {
      try {
        consoleServer.setSingleFileMode(FileUtil.Path.ERUDA_CONSOLE);
      } catch (IllegalArgumentException e) {
        logger.e(LOG_TAG, e.getMessage());
      }
      consoleServer.launchWithLocalHost();
    }

    AsyncTask.runNonCancelable(
        () -> {
          if (!liveServer.isAlive()) {
            liveServer.launch();
          }
          return liveServer.getUrl();
        },
        (result, throwable) -> {
          if (throwable == null) {
            if (result != null) {
              binding.webview.loadUrl(result);
            } else {
              logger.e(LOG_TAG, "Failed to load live server");
            }
          } else {
            BaseUtil.toastShort(throwable.getMessage());
            logger.e(LOG_TAG, throwable.getMessage());
          }
        });
  }

  @Override
  public void onUnselected() {
    super.onUnselected();
    if (liveServer != null) {
      liveServer.stop();
    }
    if (consoleServer != null) {
      consoleServer.stop();
    }
  }

  @Override
  public void persist() {
    super.persist();
    addArguments(KEY_PREVIEW_FILE_PATH, Wizard.getFilePathOrEmpty(fileToPreview));
    addArguments(KEY_IS_ZOOMABLE, isZoomable);
    addArguments(KEY_DESKTOP_MODE, isDesktopMode);
  }

  /**
   * Loads file preset for selected
   *
   * @param file the file
   */
  public void loadFile(File file) {
    fileToPreview = file;
    if (liveServer == null) return;

    try {
      liveServer.setSingleFileMode(file);
    } catch (IllegalArgumentException e) {
      logger.e(LOG_TAG, e.getMessage());
    }
  }

  public void enableDeskTopMode(boolean enabled) {
    if (getView() == null) return;

    final WebSettings webSettings = binding.webview.getSettings();
    isDesktopMode = enabled;

    if (enabled) {
      webSettings.setUserAgentString(USER_AGENT);
      webSettings.setUseWideViewPort(true);
      webSettings.setLoadWithOverviewMode(true);
      webSettings.setSupportZoom(true);
      webSettings.setBuiltInZoomControls(true);
    } else {
      // revert to default user agent when ua is null or empty
      webSettings.setUserAgentString(null);
      webSettings.setUseWideViewPort(false);
      webSettings.setLoadWithOverviewMode(false);
      webSettings.setSupportZoom(false);
      webSettings.setBuiltInZoomControls(false);
    }
  }

  public File getFile() {
    return this.fileToPreview;
  }

  public WebView getWebView() {
    return binding.webview;
  }

  public void goBack() {
    if (binding != null) {
      if (canUndo()) binding.webview.goBack();
    }
  }

  public boolean canUndo() {
    return binding.webview.canGoBack();
  }

  public void goForward() {
    if (binding != null) {
      if (canRedo()) binding.webview.goForward();
    }
  }

  public boolean canRedo() {
    return binding.webview.canGoForward();
  }

  public boolean isDeskTopMode() {
    return isDesktopMode;
  }

  public boolean isZoomable() {
    return isZoomable;
  }

  public void setZoomable(boolean enabled) {
    if (!hasPerformedCreateView()) return;
    isZoomable = enabled;
    binding.webview.getSettings().setSupportZoom(enabled);
  }

  public void openInDeviceBrowser() {
    var url = binding.webview.getUrl();
    BaseUtil.openUrlOutsideActivity(url);
  }

  public void refresh() {
    binding.webview.reload();
  }
}
