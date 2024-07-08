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
 
   package com.eup.codeopsstudio;

import static com.eup.codeopsstudio.common.Constants.SharedPreferenceKeys;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.InsetDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.core.util.Pair;
import androidx.core.view.GravityCompat;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import com.blankj.utilcode.util.KeyboardUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.eup.codeopsstudio.common.AsyncTask;
import com.eup.codeopsstudio.common.Constants;
import com.eup.codeopsstudio.common.models.BaseEvent;
import com.eup.codeopsstudio.common.util.Archive;
import com.eup.codeopsstudio.common.util.FileUtil;
import com.eup.codeopsstudio.common.util.PreferencesUtils;
import com.eup.codeopsstudio.common.util.SDKUtil;
import com.eup.codeopsstudio.common.util.SDKUtil.API;
import com.eup.codeopsstudio.databinding.FragmentMainBinding;
import com.eup.codeopsstudio.events.CurrentPaneEvent;
import com.eup.codeopsstudio.listeners.FileActionListener;
import com.eup.codeopsstudio.logging.Logger;
import com.eup.codeopsstudio.observers.ContextualLifecycleObserver;
import com.eup.codeopsstudio.pane.Pane;
import com.eup.codeopsstudio.res.R;
import com.eup.codeopsstudio.res.databinding.LayoutDialogTextInputBinding;
import com.eup.codeopsstudio.ui.AllowChildInterceptDrawerLayout;
import com.eup.codeopsstudio.ui.editor.code.CodeEditorPane;
import com.eup.codeopsstudio.ui.editor.panes.WebViewPane;
import com.eup.codeopsstudio.events.EditorModificationEvent;
import com.eup.codeopsstudio.util.Wizard;
import com.eup.codeopsstudio.util.BaseUtil;
import com.eup.codeopsstudio.viewmodel.MainViewModel;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import androidx.core.app.ActivityCompat;

/**
 * Foundational ui
 *
 * <p>TODO:
 * <li>Copy main fraent of old westidop
 *
 * @see androidx.fragment.app.Fragment
 * @author EUP
 */
public class MainFragment extends Fragment
    implements FileActionListener,
        SharedPreferences.OnSharedPreferenceChangeListener,
        MenuProvider {

  public static final String TAG = MainFragment.class.getSimpleName();
  private FragmentMainBinding binding;
  private CoordinatorLayout mainLayout;
  private View mRoot;
  private Logger logger;
  private ActivityResultLauncher<String[]> mPermissionLauncher;
  private final ActivityResultContracts.RequestMultiplePermissions mPermissionsContract =
      new ActivityResultContracts.RequestMultiplePermissions();
  private MainViewModel mMainViewModel;
  private static final String LOG_TAG = "CoreUI";
  private Wizard wizard;
  private ContextualLifecycleObserver mLifecycleObserver;
  private static final int MENU_ICON_MARGIN = 8;
  private Pair<Integer, Pane> currentPanePair = Pair.create(-1, null);
  private OnBackPressedCallback onBackPressedCallback;
  /**
   * Debug only
   *
   * @return A new MainFragment
   */
  @VisibleForTesting
  public static MainFragment newInstance() {
    return new MainFragment();
  }

  public static MainFragment newInstance(@NonNull Bundle arg) {
    final var fragment = new MainFragment();
    fragment.setArguments(arg);
    return fragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mMainViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
    wizard = new Wizard(requireContext());
    logger = new Logger(Logger.LogClass.IDE);
  }

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    binding = FragmentMainBinding.inflate(inflater, container, false);
    mRoot = binding.getRoot();
    // Set toolbar as support action bar
    ((AppCompatActivity) requireActivity()).setSupportActionBar(binding.toolbar);
    binding.toolbar.setNavigationIcon(R.drawable.ic_menu);
    return mRoot;
  }

  @Override
  @MainThread
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    requireActivity().addMenuProvider(this, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
    mLifecycleObserver =
        new ContextualLifecycleObserver(
            requireContext(), requireActivity().getActivityResultRegistry());
    mLifecycleObserver.setFileActionListener(this);
    getLifecycle().addObserver(mLifecycleObserver);
    logger.attach(requireActivity());
    onBackPressedCallback =
        new OnBackPressedCallback(/* enabled= */ false) {
          @Override
          public void handleOnBackPressed() {
            var webViewPane = getSelectedWebViewPane();
            if (webViewPane != null && webViewPane.getWebView().canGoBack()) {
              webViewPane.getWebView().goBack();
              return;
            }
            if (mRoot instanceof AllowChildInterceptDrawerLayout) {
              //noinspection ConstantConditions
              if (mMainViewModel.getDrawerState().getValue()) {
                mMainViewModel.setDrawerState(false);
              } else {
                // TODO: showExitDialog
              }
            }
          }
        };
    requireActivity().getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);
    KeyboardUtils.registerSoftInputChangedListener(requireActivity(), __ -> invalidateMenu());

    if (savedInstanceState != null) {
      restoreViewState(savedInstanceState);
    }

    if (PreferencesUtils.canShareAnynomousStatistics()) {
          wizard.uploadAnynomousAnalytics(true);
    } else {
          wizard.uploadAnynomousAnalytics(false);
    }

    if (PreferencesUtils.openLastOpenedProject()) {
      var filePath =
          PreferencesUtils.getLastOpenedProjectPreferences()
              .getString(SharedPreferenceKeys.KEY_LAST_OPENED_PROJECT, "");

      if (!Wizard.isEmpty(filePath)) {
        var projectFile = new File(filePath);
        if (projectFile != null && projectFile.exists()) {
          mMainViewModel.setTreeViewFragmentTreeDir(projectFile);
        } else if (projectFile != null && !projectFile.exists()) {
          // corrupted thus clear
          PreferencesUtils.clearPerference(
              PreferencesUtils.getLastOpenedProjectPreferences(),
              SharedPreferenceKeys.KEY_LAST_OPENED_PROJECT);
          logger.e(
              LOG_TAG,
              "Failed to reopen last opened project because it no longer exists in that directory");
        }
      }
    }

    mPermissionLauncher =
        registerForActivityResult(
            mPermissionsContract,
            isGranted -> {
              if (isGranted.containsValue(false)) {
                new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.storage_permission_denied)
                    .setMessage(R.string.storage_permission_denial_prompt)
                    .setPositiveButton(
                        R.string.storage_permission_request_again,
                        (d, which) -> {
                          requestStoragePermission(true);
                        })
                    .setNegativeButton(
                        R.string.exit,
                        (d, which) -> {
                          requireActivity().finishAffinity();
                          System.exit(0);
                        })
                    .setCancelable(false)
                    .show();
              } else {
                checkPlugins();
              }
            });
    checkStoragePermission();

    if (mRoot instanceof AllowChildInterceptDrawerLayout) {
      var drawerLayout = (AllowChildInterceptDrawerLayout) mRoot;

      mainLayout = (CoordinatorLayout) binding.mainLayout;
      mMainViewModel.setDrawerInstance(true);

      binding.toolbar.setNavigationOnClickListener(
          v -> {
            if (mRoot instanceof AllowChildInterceptDrawerLayout) {
              if (drawerLayout.isDrawerOpen(binding.navView)) {
                mMainViewModel.setDrawerState(false);
              } else if (!drawerLayout.isDrawerOpen(binding.navView)) {
                mMainViewModel.setDrawerState(true);
              }
            }
          });

      drawerLayout.addDrawerListener(
          new AllowChildInterceptDrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
              // float translationX = drawerView.getWidth() * slideOffset * 0.3f;
              float translation = drawerView.getWidth() * slideOffset;
              mainLayout.setTranslationX(translation);
            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
              onBackPressedCallback.setEnabled(true);
            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
              onBackPressedCallback.setEnabled(false);
            }
          });
    } else {
      /**
       * Device with large screens do not use the {@code AllowChildInterceptDrawerLayout}
       *
       * @see EmtpyPaneWindow
       */
      mMainViewModel.setDrawerInstance(false);
      binding.toolbar.setNavigationIcon(null);
    }
    mMainViewModel.getToolbarTitle().observe(getViewLifecycleOwner(), binding.toolbar::setTitle);
    mMainViewModel
        .getToolbarSubTitle()
        .observe(getViewLifecycleOwner(), binding.toolbar::setSubtitle);

    if (mRoot instanceof AllowChildInterceptDrawerLayout) {
      var drawerLayout = (AllowChildInterceptDrawerLayout) mRoot;
      mMainViewModel
          .getDrawerState()
          .observe(
              getViewLifecycleOwner(),
              isOpen -> {
                if (isOpen) {
                  drawerLayout.openDrawer(binding.navView);
                } else {
                  drawerLayout.closeDrawer(binding.navView);
                }
              });
    }
    mMainViewModel
        .canRequestStoragePermission()
        .observe(getViewLifecycleOwner(), this::requestStoragePermission);
  }

  @Override
  public void onSaveInstanceState(@NonNull Bundle outState) {
    if (mRoot instanceof AllowChildInterceptDrawerLayout) {
      outState.putBoolean(
          "start_drawer_state",
          ((AllowChildInterceptDrawerLayout) mRoot).isDrawerOpen(GravityCompat.START));
    }
    super.onSaveInstanceState(outState);
  }

  /**
   * Restore view state via bundle
   *
   * @param state The bundle
   */
  private void restoreViewState(@NonNull Bundle state) {
    if (mRoot instanceof AllowChildInterceptDrawerLayout) {
      boolean drawerState = state.getBoolean("start_drawer_state", false);
      mMainViewModel.setDrawerState(drawerState);
    }
  }

  @Override
  public void onPrepareMenu(@NonNull Menu menu) {
    onPrepareToolbarOptionsMenus(menu);
  }

  @Override
  public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
    menuInflater.inflate(R.menu.main_menu, menu);
    // There is no public API to make icons show on menus.
    // TODO: Always check {@link MenuBuilder} is still available or supported
    if (menu instanceof MenuBuilder) {
      MenuBuilder menuBuilder = (MenuBuilder) menu;
      //noinspection RestrictedApi
      menuBuilder.setOptionalIconsVisible(true);
      //noinspection RestrictedApi
      for (MenuItem item : menuBuilder.getVisibleItems()) {
        int iconMarginPx =
            (int)
                TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    MENU_ICON_MARGIN,
                    getResources().getDisplayMetrics());

        if (item.getIcon() != null) {
          if (SDKUtil.isGreaterThan(API.ANDROID_5)) {
            item.setIcon(new InsetDrawable(item.getIcon(), iconMarginPx, 0, iconMarginPx, 0));
          } else {
            item.setIcon(
                new InsetDrawable(item.getIcon(), iconMarginPx, 0, iconMarginPx, 0) {
                  @Override
                  public int getIntrinsicWidth() {
                    return getIntrinsicHeight() + iconMarginPx + iconMarginPx;
                  }
                });
          }
        }
      }
    }
  }

  @Override
  public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
    return onToolbarOptionsMenuItemSelected(menuItem);
  }

  /**
   * Invalidates the {@link android.view.Menu} to ensure that what is displayed matches the current
   * internal state of the menu.
   *
   * <p>This should be called whenever the state of the menu is changed, such as items being removed
   * or disabled based on some user event.
   *
   * @see {@link MenuHost}
   */
  public void invalidateMenu() {
    requireActivity().invalidateMenu();
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    binding = null;
    // mMainViewModel.getDrawerState().removeObservers(getViewLifecycleOwner());
    mMainViewModel.getToolbarTitle().removeObservers(getViewLifecycleOwner());
    mMainViewModel.getToolbarSubTitle().removeObservers(getViewLifecycleOwner());
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    onBackPressedCallback.setEnabled(false);
  }

  @Override
  public void onStart() {
    super.onStart();
    if (!EventBus.getDefault().isRegistered(this)) {
      EventBus.getDefault().register(this);
    }
  }

  @Override
  public void onStop() {
    super.onStop();
    if (EventBus.getDefault().isRegistered(this)) {
      EventBus.getDefault().unregister(this);
    }
  }

  // File Action Listener
  @Override
  public void onFolderPicked(@NonNull File file) {
    mMainViewModel.setTreeViewFragmentTreeDir(file);
  }

  @Override
  public void onFilePicked(@NonNull File file) {
    openFileInPane(file);
  }

  @Override
  public void onFilePicked(@NonNull Uri uri) {
    // No-op
  }

  @Override
  public void onCreateFile(@NonNull File file) {
    openFileInPane(file);
  }

  @Override
  public void onActionFailed(@Nullable String message) {
    logger.e(LOG_TAG, message);
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onEditorModificationEvent(EditorModificationEvent event) {
    invalidateMenu();
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onCurrentPaneChangeEvent(@NonNull CurrentPaneEvent event) {
    currentPanePair = Pair.create(event.getIndex(), event.getPane());
  }

  @Override
  public void onSharedPreferenceChanged(SharedPreferences pref, String key) {
    switch (key) {
      case SharedPreferenceKeys.KEY_SHARE_STATISTICS:
        if (PreferencesUtils.canShareAnynomousStatistics()) {
          wizard.uploadAnynomousAnalytics(true);
        } else {
          wizard.uploadAnynomousAnalytics(false);
        }
        break;
    }
  }

  public void closeApp() {
    onBackPressedCallback.setEnabled(true);
  }

  private void onPrepareToolbarOptionsMenus(Menu menu) {
    CodeEditorPane editorPane = getSelectedCodeEditorPane();
    WebViewPane webViewPane = getSelectedWebViewPane();

    if (editorPane != null) {
      menu.setGroupVisible(R.id.group_content_edit, true);
      // code editor search panel
      menu.setGroupVisible(R.id.group_editor_actions, true);
      menu.setGroupVisible(R.id.group_unredo, true);

      if (editorPane.isReadOnlyMode()) {
        menu.setGroupEnabled(R.id.group_unredo, false);
        menu.setGroupEnabled(R.id.group_content_edit, false);
      } else {
        menu.setGroupEnabled(R.id.group_unredo, true);
        menu.setGroupEnabled(R.id.group_content_edit, true);
      }
      menu.findItem(R.id.menu_run).setVisible(Constants.isMarkUp(editorPane.getFile()));
      menu.findItem(R.id.menu_save_file).setEnabled(editorPane.isModified());
      menu.findItem(R.id.menu_read_only_mode).setChecked(editorPane.isReadOnlyMode());
      // menu.findItem(R.id.?).setEnabled(!KeyboardUtils.isSoftInputVisible(this));
    } else if (webViewPane != null) {
      menu.setGroupVisible(R.id.group_content_edit, false);
      // code editor search panel
      menu.setGroupVisible(R.id.group_editor_actions, false);
      menu.setGroupVisible(R.id.group_unredo, true);

      menu.findItem(R.id.menu_liveserver).setVisible(true);
      menu.findItem(R.id.menu_zoom).setChecked(webViewPane.isZoomable());
      menu.findItem(R.id.menu_desktop_mode).setChecked(webViewPane.isDeskTopMode());
      menu.findItem(R.id.menu_redo).setEnabled(webViewPane.canRedo());
      menu.findItem(R.id.menu_undo).setEnabled(webViewPane.canUndo());
    }
  }

  private boolean onToolbarOptionsMenuItemSelected(MenuItem item) {
    var id = item.getItemId();
    CodeEditorPane editorPane = getSelectedCodeEditorPane();
    WebViewPane webViewPane = getSelectedWebViewPane();

    if (editorPane != null && editorPane.getEditor() != null) {
      if (id == R.id.menu_run) {
        // save editor content ahead of preview
        editorPane.saveEditor();
        mMainViewModel.setWebViewPaneFile(editorPane.getFile());
      }
      else if (id == R.id.menu_undo) editorPane.undo();
      else if (id == R.id.menu_redo) editorPane.redo();
      else if (id == R.id.menu_save_file) editorPane.saveEditor();
      else if (id == R.id.menu_findFile) editorPane.openSearchPanel(true);
      else if (id == R.id.menu_jump_to_line) editorPane.doJumpToLine();
      else if (id == R.id.menu_read_only_mode) {
        editorPane.makeReadOnly(!item.isChecked());
        item.setChecked(!item.isChecked());
      } else if (id == R.id.menu_copy_line) {
        editorPane.getEditor().copyText();
      } else if (id == R.id.menu_cut_line) {
        editorPane.getEditor().cutLine();
      } else if (id == R.id.menu_delete_line) {
        editorPane.getEditor().deleteLine();
      } else if (id == R.id.menu_replace_line) {
        editorPane.getEditor().replaceCurrLine();
      } else if (id == R.id.menu_duplicate_line) {
        editorPane.getEditor().duplicateLine();
      } else if (id == R.id.menu_convert_to_lowercase) {
        editorPane.getEditor().convertSelectionToLowerCase();
      } else if (id == R.id.menu_convert_to_uppercase) {
        editorPane.getEditor().convertSelectionToUpperCase();
      } else if (id == R.id.menu_reset_color_schemes) {
        editorPane.refreshEditorLanguageSyntax();
      }
    } else if (webViewPane != null) {
      WebView webView = webViewPane.getWebView();
      if (id == R.id.menu_undo) {
        if (webView.canGoBack()) webView.goBack();
        else ToastUtils.showShort(getString(R.string.alrt_cannot_go_back));
      } else if (id == R.id.menu_redo) {
        if (webView.canGoForward()) webView.goForward();
        else ToastUtils.showShort(getString(R.string.alrt_cannot_go_forward));
      } else if (id == R.id.menu_zoom) {
        webViewPane.setZoomable(!item.isChecked());
        item.setChecked(!item.isChecked());
      } else if (id == R.id.menu_desktop_mode) {
        webViewPane.enableDeskTopMode(!item.isChecked());
        item.setChecked(!item.isChecked());
      } else if (id == R.id.menu_refresh) webViewPane.refresh();
      else if (id == R.id.menu_open_in_browser) webViewPane.openInDeviceBrowser();
      else if (id == R.id.menu_copy_url) {
        var url = webViewPane.getWebView().getOriginalUrl();
        if (!Wizard.isEmpty(url)) BaseUtil.copyToClipBoard(url, true);
      }
    }
    invalidateMenu();
    return true;
  }

  /**
   * Get the selected code editor pane from the {@code MainViewModel}
   *
   * @return The selected code editor pane
   */
  private CodeEditorPane getSelectedCodeEditorPane() {
    var pair = currentPanePair;
    if (pair != null) {
      Pane current = pair.second;
      if (current != null && current instanceof CodeEditorPane) {
        return (CodeEditorPane) current;
      }
    }
    return null;
  }

  /**
   * Get the selected web view pane from the {@code MainViewModel}
   *
   * @return The selected web view pane
   */
  private WebViewPane getSelectedWebViewPane() {
    var pair = currentPanePair;
    if (pair != null) {
      Pane current = pair.second;
      if (current != null && current instanceof WebViewPane) {
        return (WebViewPane) current;
      }
    }
    return null;
  }

  /**
   * Open a file in base fragment which is added to the pane system
   *
   * @param file the file to be opened
   */
  public void openFileInPane(File file) {
    // BaseFragment performs sanity check for invalid files
    mMainViewModel.openEditorFile(file);
    invalidateMenu();
  }

  /**
   * Open a directory in TreeViewFragment which is added to the pane system
   *
   * @param dir the directory to be opened
   */
  public void openFolderInTreeViewFragment(File dir) {
    // BaseFragment performs sanity check for invalid files
    mMainViewModel.setTreeViewFragmentTreeDir(dir);
    invalidateMenu();
  }

  /*
   * Open file manager to select folder
   */
  public void openFolderFromManager() {
    if (mLifecycleObserver != null) mLifecycleObserver.pickFolder();
  }

  /*
   * Open file manager to select file
   */
  public void openFileFromManager() {
    if (mLifecycleObserver != null) mLifecycleObserver.pickFile();
  }

  /*
   * Open file manager to create file
   */
  public void createFileFromManager() {
    var dialogBinding = LayoutDialogTextInputBinding.inflate(LayoutInflater.from(requireContext()));
    var builder = new MaterialAlertDialogBuilder(requireContext());

    builder.setTitle(R.string.new_file);
    builder.setView(dialogBinding.getRoot());
    dialogBinding.tilName.setHint(getString(R.string.prompt_file_name));

    builder.setPositiveButton(
        getString(R.string.next),
        (dialog, which) -> {
          var prepName = dialogBinding.tilName.getEditText().getText().toString();
          if (prepName == null || prepName.isEmpty()) {
            if (mLifecycleObserver != null)
              mLifecycleObserver.createFile(getString(R.string.untitled));
          } else {
            if (mLifecycleObserver != null) mLifecycleObserver.createFile(prepName);
          }
        });

    builder.setNegativeButton(getString(R.string.cancel), null);
    builder.show();
  }

  private void requestStoragePermission(boolean enabled) {
    if (enabled) {
      if (SDKUtil.isAtLeast(API.ANDROID_11)) {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
        Uri uri = Uri.fromParts("package", Wizard.getAppPackageName(requireContext()), null);
        intent.setData(uri);
        startActivity(intent);
        mMainViewModel.setCanRequestStoragePermissionState(false);
      } else {
        mMainViewModel.setCanRequestStoragePermissionState(false);
        mPermissionLauncher.launch(
            new String[] {
              Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.MANAGE_EXTERNAL_STORAGE
            });
      }
    }
  }

  @SuppressLint("NewApi")
  public static boolean isStoragePermissionGranted(Context context) {
    if (SDKUtil.isAtLeast(API.ANDROID_11)) {
      return Environment.isExternalStorageManager();
    } else {
      return ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
              == PackageManager.PERMISSION_GRANTED
          && ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
              == PackageManager.PERMISSION_GRANTED;
    }
  }

  private void checkPlugins() {
    logger.d(LOG_TAG, getString(R.string.msg_checking_plugins));
    if (!FileUtil.Path.ERUDA_CONSOLE.exists()) {
      logger.w(LOG_TAG, getString(R.string.msg_js_plugin_absent));
      try {
        logger.i(LOG_TAG, getString(R.string.msg_installing_js_console_plugins));
        Archive.unzipFromAssets(
            requireContext(),
            "plugins/eruda.min.zip",
            FileUtil.Path.PLUGINS_FOLDER.getAbsolutePath());
      } catch (IOException e) {
        logger.e(LOG_TAG, e.getMessage());
      }
    }
  }

  /* Check storage permission state */
  private void checkStoragePermission() {
    if (!isStoragePermissionGranted(requireContext())) {
      requestStoragePermission(true);
    } else {
      checkPlugins();
    }
  }

  /**
   * @return ActivityMainBinding
   */
  public FragmentMainBinding getBinding() {
    return this.binding;
  }
}
