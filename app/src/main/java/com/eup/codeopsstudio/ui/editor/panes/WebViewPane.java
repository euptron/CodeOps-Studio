/*************************************************************************
 * This file is part of CodeOps Studio.
 * CodeOps Studio - code anywhere anytime
 * https://github.com/etidoUP/CodeOps-Studio
 * Copyright (C) 2024 EUP
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
 * If you have more questions, feel free to message EUP if you have any
 * questions or need additional information. Email: etido.up@gmail.com
 *************************************************************************/
 
   package com.eup.codeopsstudio.ui.editor.panes;

import static com.eup.codeopsstudio.common.Constants.SharedPreferenceKeys;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.eup.codeopsstudio.common.AsyncTask;
import com.eup.codeopsstudio.common.Constants;
import com.eup.codeopsstudio.common.util.FileUtil;
import com.eup.codeopsstudio.common.util.PreferencesUtils;
import com.eup.codeopsstudio.databinding.LayoutPaneWebviewBinding;
import com.eup.codeopsstudio.logging.Logger;
import com.eup.codeopsstudio.pane.Pane;
import com.eup.codeopsstudio.res.R;
import com.eup.codeopsstudio.server.LiveServer;
import com.eup.codeopsstudio.util.BaseUtil;
import com.eup.codeopsstudio.util.Wizard;
import java.io.File;
import android.graphics.Bitmap;


public class WebViewPane extends Pane {

  private LayoutPaneWebviewBinding binding;
  private boolean showConsole = true;
  private boolean isDesktopMode = false;
  private boolean isZoomable = true;
  private LiveServer liveServer;
  private LiveServer consoleServer;
  private File mFile;
  private Logger logger;

  public WebViewPane(Context context, String title) {
    this(context, title, /* generate new uuid= */ true);
  }

  public WebViewPane(Context context, String title, boolean generateUUID) {
    super(context, title, generateUUID);
  }

  @Override
  public View onCreateView() {
    binding = LayoutPaneWebviewBinding.inflate(LayoutInflater.from(getContext()));
    logger = new Logger(Logger.LogClass.IDE);
    return binding.getRoot();
  }

  @Override
  public void onViewCreated(View view) {
    super.onViewCreated(view);
    logger.attach(requireActivity());
    
    liveServer = new LiveServer(getContext());
    consoleServer = new LiveServer(getContext());

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
    webSettings.setDefaultTextEncodingName("UTF-8");
    webSettings.setLoadsImagesAutomatically(true);
    webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
    webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
    setZoomable(true);
    enableDeskTopMode(false);
    
    binding.progressbar.setMax(100);
    binding.progressbar.setProgress(1);
    binding.progressbar.setVisibility(View.GONE);
    
    binding.webview.setWebViewClient(
        new WebViewClient() {
          @Override
          public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            binding.progressbar.setVisibility(View.VISIBLE);
          }
          
          @Override
          public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            view.loadUrl(request.getUrl().toString());
            return true;
          }

          @Override
          public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            binding.progressbar.setVisibility(View.GONE);
            if (showConsole) {
              String msg = getString(R.string.msg_console_welcome);
              String initalizeConsole =
                  "eruda.init({"
                      + "\n"
                      + "    defaults: {"
                      + "\n"
                      + "displaySize: 50,"
                      + "\n"
                      + "transparency: 1,"
                      + "\n"
                      + "theme: 'Atom One Dark'"
                      + "\n"
                      + "}"
                      + "\n"
                      + "});";
              if (consoleServer.getUrl() != null) {
                view.evaluateJavascript(
                    "javascript:(function () { var script = document.createElement('script'); script.src=\""
                        + consoleServer.getUrl()
                        + "\"; document.body.appendChild(script);"
                        + "script.onload = function () { "
                        + initalizeConsole
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

    binding.webview.setWebChromeClient(
        new WebChromeClient() {
          @Override
          public void onProgressChanged(WebView view, int progress) {
            if (binding == null) return;
            binding.progressbar.setProgressCompat(progress, true);
            if (view.getTitle()!= null && view.getTitle() == "about:blank") {
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
  }

  /** Called before the pane is destroyed */
  @Override
  public void onDestroy() {
    super.onDestroy();
    if (liveServer != null) {
      liveServer.stop();
    }
    if (consoleServer != null) {
      consoleServer.stop();
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
    if (mFile == null && liveServer != null && liveServer.getFile() != null) {
      mFile = liveServer.getFile();
    }
    if (mFile != null) {
      if (Constants.WEB_MARKUP_LANGUAGE.stream().anyMatch(mFile.getName()::endsWith)) {
        consoleServer.setFile(FileUtil.Path.ERUDA_CONSOLE);
        consoleServer.start();
      }
    }
    AsyncTask.runLaterOnUiThread(
        () -> {
          // load url after server has started
          liveServer.start(
              (successful, throwable) -> {
                if (successful) {
                  binding.webview.loadUrl(liveServer.getUrl());
                }
                if (throwable != null) {
                  // TODO: Handle live server error
                  throwable.printStackTrace();
                  BaseUtil.showToast(throwable.getMessage(), BaseUtil.LENGTH_SHORT);
                }
              });
        },
        Constants.AVG_WAIT_MILLS); // workaround for overhead ~ 600ms
  }

  @Override
  public void persist() {
    super.persist();
    addArguments("preview_file_path", Wizard.getFilePathOrEmpty(mFile));
    addArguments("isZoomAble", isZoomable);
    addArguments("isDeskTopMode", isDesktopMode);
  }

  /**
   * Loads file preset for selected
   *
   * @param file the file
   */
  public void loadFile(File file) {
    if (liveServer != null) {
      mFile = file;
      liveServer.setFile(file);
      if (liveServer.server != null && liveServer.server.isAlive()) {
        binding.webview.loadUrl(liveServer.getUrl());
      }
    }
  }

  public void refresh() {
    binding.webview.reload();
  }

  public void loadHtmlSnippet(String snippet) {
    binding.webview.loadData(snippet, "text/html", "UTF-8");
  }

  public WebView getWebView() {
    return binding.webview;
  }

  public void closeFindResult() {
    binding.webview.clearMatches();
  }

  public void findText(String text) {
    binding.webview.findAllAsync(text);
  }

  public void findNext(boolean forward) {
    binding.webview.findNext(forward);
  }

  public void setFindListener(WebView.FindListener findListener) {
    binding.webview.setFindListener(findListener);
  }

  public boolean canUndo() {
    return binding.webview.canGoBack();
  }

  public void goBack() {
    if (binding != null) {
      if (canUndo()) binding.webview.goBack();
    }
  }

  public boolean canRedo() {
    return binding.webview.canGoForward();
  }

  public void goForward() {
    if (binding != null) {
      if (canRedo()) binding.webview.goForward();
    }
  }

  public boolean isDeskTopMode() {
    return isDesktopMode;
  }

  public boolean isZoomable() {
    return isZoomable;
  }

  public boolean hasshowConsole() {
    return showConsole;
  }

  public void showConsole(boolean enabled) {
    showConsole = enabled;
  }

  public void setZoomable(boolean enabled) {
    if (!hasPerformedCreateView()) return;
    isZoomable = enabled;
    binding.webview.getSettings().setSupportZoom(enabled);
  }

  public void enableDeskTopMode(boolean enabled) {
    if (getView() == null) return;
    WebSettings webSettings = binding.webview.getSettings();
    isDesktopMode = enabled;
    if (enabled) {
      webSettings.setUserAgentString(
          "Mozilla/5.0 (Windows NT 10) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.78 Safari/537.36");
      webSettings.setUseWideViewPort(true);
      webSettings.setLoadWithOverviewMode(true);
    } else {
      // revert to default user agent when ua is null or empty
      webSettings.setUserAgentString(null);
      webSettings.setUseWideViewPort(false);
      webSettings.setLoadWithOverviewMode(false);
    }
  }

  public void openInDeviceBrowser() {
    var url = binding.webview.getUrl();
    BaseUtil.openUrlOutsideActivity(url);
  }

  public File getFile() {
    return this.mFile;
  }
}
