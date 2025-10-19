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

package com.eup.codeopsstudio;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.graphics.drawable.InsetDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.util.Pair;
import androidx.core.view.GravityCompat;
import androidx.core.view.MenuHost;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;

import com.eup.codeopsstudio.common.Constants;
import com.eup.codeopsstudio.common.ILog;
import com.eup.codeopsstudio.common.archive.ZIPArchive;
import com.eup.codeopsstudio.common.models.MetaDocument;
import com.eup.codeopsstudio.common.util.FileUtil;
import com.eup.codeopsstudio.common.util.PreferencesUtils;
import com.eup.codeopsstudio.databinding.FragmentMainBinding;
import com.eup.codeopsstudio.databinding.LayoutDialogTextInputBinding;
import com.eup.codeopsstudio.domain.events.CurrentPaneEvent;
import com.eup.codeopsstudio.domain.events.EditorModificationEvent;
import com.eup.codeopsstudio.models.logger.Logger;
import com.eup.codeopsstudio.models.user.User;
import com.eup.codeopsstudio.observers.ContextualLifecycleObserver;
import com.eup.codeopsstudio.pane.Pane;
import com.eup.codeopsstudio.ui.AllowChildInterceptDrawerLayout;
import com.eup.codeopsstudio.ui.editor.code.CodeEditorPane;
import com.eup.codeopsstudio.ui.editor.panes.WebViewPane;
import com.eup.codeopsstudio.util.BaseUtil;
import com.eup.codeopsstudio.util.Wizard;
import com.eup.codeopsstudio.viewmodel.FileViewModel;
import com.eup.codeopsstudio.viewmodel.MainViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.j2objc.annotations.UsedByReflection;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.IOException;

/**
 * The main UI fragment responsible for managing the core interface of CodeOps Studio.
 *
 * <p>This fragment serves as the foundation for the IDE experience, handling:
 *
 * <ul>
 *   <li>Toolbar and navigation drawer setup
 *   <li>File and project management operations
 *   <li>Editor pane management (code editors, web views)
 *   <li>Back navigation handling
 *   <li>Preference change listening
 *   <li>Menu management and action handling
 * </ul>
 *
 * <p>Key responsibilities include:
 *
 * <ul>
 *   <li>Coordinating between {@link MainViewModel} and UI components
 *   <li>Managing {@link CodeEditorPane} and {@link WebViewPane} instances
 *   <li>Handling file operations through {@link FileViewModel}
 *   <li>Maintaining navigation state and drawer interactions
 *   <li>Facilitating plugin management and updates
 * </ul>
 *
 * <p>This fragment implements several important interfaces:
 *
 * <ul>
 *   <li>{@link SharedPreferences.OnSharedPreferenceChangeListener} for settings changes
 *   <li>{@link MenuProvider} for toolbar menu management
 * </ul>
 *
 * <p>Lifecycle considerations:
 *
 * <ul>
 *   <li>Registers with {@link EventBus} for editor events during onStart()
 *   <li>Manages {@link ContextualLifecycleObserver} for activity result handling
 *   <li>Maintains proper {@link Lifecycle} state awareness for UI components
 * </ul>
 *
 * @author Etido Peter
 * @see MainViewModel
 * @see FileViewModel
 * @see CodeEditorPane
 * @see WebViewPane
 */
public class MainFragment extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener, MenuProvider {

    public static final String TAG = MainFragment.class.getSimpleName();
    private static final String LOG_TAG = "CoreUI";
    private static final int MENU_ICON_MARGIN = 8;
    private FragmentMainBinding binding;
    private CoordinatorLayout mainLayout;
    private View rootView;
    private Logger logger;
    private MainViewModel mainViewModel;
    private ContextualLifecycleObserver lifeCycleObserver;
    private Pair<Integer, Pane> currentPanePair = Pair.create(-1, null);
    private OnBackPressedCallback onBackPressedCallback;
    private FileViewModel fileViewModel;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private ILog.LogListener logListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        logger = new Logger(Logger.LogClass.IDE);

        mainViewModel     = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        fileViewModel     = new ViewModelProvider(requireActivity()).get(FileViewModel.class);
        lifeCycleObserver = new ContextualLifecycleObserver(requireContext(),
            requireActivity().getActivityResultRegistry(), requireActivity());
        getLifecycle().addObserver(lifeCycleObserver);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
        @Nullable Bundle savedInstanceState) {
        binding  = FragmentMainBinding.inflate(inflater, container, false);
        rootView = binding.getRoot();
        ((AppCompatActivity) requireActivity()).setSupportActionBar(binding.toolbar);
        binding.toolbar.setNavigationIcon(R.drawable.ic_menu);
        return rootView;
    }

    @Override
    @MainThread
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        AppCompatActivity activity = (AppCompatActivity) requireActivity();

        logger.attach(requireActivity());
        logListener = formattedMessage -> {
            requireActivity().runOnUiThread(() -> {
                logger.postLog(formattedMessage);
            });
        };
        
        requireActivity().addMenuProvider(this, getViewLifecycleOwner(), Lifecycle.State.RESUMED);

        ((MainActivity) requireActivity()).ensureStoragePermissionGranted();
        if (((MainActivity) requireActivity()).isStoragePermissionGranted()) checkPlugins();
        ((MainActivity) requireActivity()).ensureNotificationPermissionGranted();

        mainViewModel.getToolbarTitle().observe(getViewLifecycleOwner(), binding.toolbar::setTitle);
        mainViewModel.getToolbarSubTitle()
                     .observe(getViewLifecycleOwner(), binding.toolbar::setSubtitle);
        mainViewModel.observeSetTreeViewFragmentFile(getViewLifecycleOwner(),
            file -> invalidateMenu());
        mainViewModel.observeEditorFileOpening(getViewLifecycleOwner(), file -> invalidateMenu());

        FirebaseApp.initializeApp(requireContext());

        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                ILog.warning(TAG, "Fetching FCM registration token failed", task.getException());
                return;
            }

            // Get new FCM registration token
            String token = task.getResult();

            // Log and toast
            String msg = "Instance ID: " + token;
            ILog.debug(TAG, msg);
            logger.i(TAG, msg);
            BaseUtil.toastShort(msg);
        });

        setUpDrawer();

        onBackPressedCallback = new OnBackPressedCallback(/* enabled= */ false) {
            @Override
            public void handleOnBackPressed() {
                var webViewPane = getSelectedWebViewPane();
                if (webViewPane != null && webViewPane.getWebView().canGoBack()) {
                    webViewPane.getWebView().goBack();
                    return;
                }
                if (rootView instanceof AllowChildInterceptDrawerLayout) {
                    if (mainViewModel.isDrawerOpen()) {
                        mainViewModel.requestCloseDrawer();
                    } else {
                        mainViewModel.requestExit();
                    }
                }
            }
        };
        requireActivity().getOnBackPressedDispatcher()
                         .addCallback(getViewLifecycleOwner(), onBackPressedCallback);

        BaseUtil.registerSoftInputChangedListener(getActivity(), __ -> invalidateMenu());

        if (PreferencesUtils.canShareAnonymousStatistics()) User.registerSession();

        if (savedInstanceState != null) restoreViewState(savedInstanceState);
        restoreLastProject();

        mainViewModel.getShouldUpdateMenu().observe(getViewLifecycleOwner(), shouldUpdate -> {
            if (shouldUpdate != null && shouldUpdate) {
                invalidateMenu();
                mainViewModel.setShouldUpdateMenu(false); // reset
            }
        });

        fileViewModel.monitorMessages(getViewLifecycleOwner(), observer -> {
            if (observer != null) {
                logger.e(LOG_TAG, observer.second);
            }
        });

        fileViewModel.observePickedFiles(getViewLifecycleOwner(), file -> {
            if (file != null) {
                if (Wizard.getMimeType(requireContext(), file)
                          .equals(MetaDocument.MimeType.ZIP.toString()) || file.getName()
                                                                               .endsWith(".zip")) {
                    mainViewModel.setZipFile(file);
                } else {
                    openFileInPane(file);
                }
            }
        });

        fileViewModel.observePickedFolders(getViewLifecycleOwner(), file -> {
            if (file != null) {
                mainViewModel.setTreeViewFragmentTreeDir(file);
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        if (rootView instanceof AllowChildInterceptDrawerLayout) {
            outState.putBoolean("start_drawer_state",
                ((AllowChildInterceptDrawerLayout) rootView).isDrawerOpen(GravityCompat.START));
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }
    
    @Override
    public void onResume() {
        super.onResume();
        ILog.addLogListener(logListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        ILog.removeLogListener(logListener);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        // mainViewModel.getDrawerState().removeObservers(getViewLifecycleOwner());
        mainViewModel.getToolbarTitle().removeObservers(getViewLifecycleOwner());
        mainViewModel.getToolbarSubTitle().removeObservers(getViewLifecycleOwner());
        ILog.removeLogListener(logListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        onBackPressedCallback.setEnabled(false);
    }

    /**
     * Invalidates the {@link android.view.Menu} to ensure that what is displayed matches the
     * current
     * internal state of the menu.
     *
     * <p>This should be called whenever the state of the menu is changed, such as items being
     * removed
     * or disabled based on some user event.
     *
     * @see {@link MenuHost}
     */
    public void invalidateMenu() {
        requireActivity().invalidateMenu();
    }

    private void setUpDrawer() {
        if (rootView instanceof AllowChildInterceptDrawerLayout drawerLayout) {

            mainLayout = (CoordinatorLayout) binding.mainLayout;
            mainViewModel.setDrawerInstance(true);

            mainViewModel.getDrawerState().observe(getViewLifecycleOwner(), event -> {
                Boolean shouldOpenDrawer = event.getContentIfNotHandled();
                
                if (Boolean.TRUE.equals(shouldOpenDrawer)) {
                    drawerLayout.openDrawer(binding.navView);
                } else {
                    drawerLayout.closeDrawer(binding.navView);
                }
            });
            
            binding.toolbar.setNavigationOnClickListener(v -> {
                if (drawerLayout.isDrawerOpen(binding.navView)) {
                    mainViewModel.requestCloseDrawer();
                } else if (!drawerLayout.isDrawerOpen(binding.navView)) {
                    mainViewModel.requestOpenDrawer();
                }
            });

            drawerLayout.addDrawerListener(new AllowChildInterceptDrawerLayout.SimpleDrawerListener() {
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
            // Device with large screens do not use the AllowChildInterceptDrawerLayout
            mainViewModel.setDrawerInstance(false);
            binding.toolbar.setNavigationIcon(null);
        }
    }

    private void restoreViewState(@NonNull Bundle state) {
        if (rootView instanceof AllowChildInterceptDrawerLayout) {          
            boolean shouldOpenDrawer = state.getBoolean("start_drawer_state", false);
            
            if (shouldOpenDrawer) {
                mainViewModel.requestOpenDrawer();
            } else {
                mainViewModel.requestCloseDrawer(); 
            }
        }
    }

    private WebViewPane getSelectedWebViewPane() {
        if (currentPanePair == null) return null;
        Pane current = currentPanePair.second;
        return (current instanceof WebViewPane) ? (WebViewPane) current : null;
    }

    /**
     * Open a file in base fragment which is added to the pane system
     *
     * @param file the file to be opened
     */
    public void openFileInPane(File file) {
        // BaseFragment performs sanity check for invalid files
        mainViewModel.openEditorFile(file);
        invalidateMenu();
    }

    private void checkPlugins() {
        logger.d(LOG_TAG, getString(R.string.msg_checking_plugins));
        if (FileUtil.Path.ERUDA_CONSOLE.exists()) return;

        logger.w(LOG_TAG, getString(R.string.msg_js_plugin_absent));
        logger.i(LOG_TAG, getString(R.string.msg_installing_js_console_plugins));
        installErudaConsole();
    }

    private void installErudaConsole() {
        try {
            logger.i(LOG_TAG, getString(R.string.msg_installing_js_console_plugins));
            int bufferSize = PreferencesUtils.getCurrentBufferSize();
            String asset = "plugins/eruda.min.zip";

            File destDir = FileUtil.Path.PLUGINS_FOLDER;
            var archive = ZIPArchive.fromAssets(requireContext(), asset, destDir, bufferSize);
            archive.unzip();
        } catch (IOException e) {
            logger.e(LOG_TAG, "Plugin installation failed: " + e.getMessage());
        }
    }

    private void restoreLastProject() {
        if (!PreferencesUtils.openLastOpenedProject()) return;

        try {
            String lastProjectPath = PreferencesUtils.getLastOpenedProjectPreferences()
                                                     .getString(Constants.SharedPreferenceKeys.KEY_LAST_OPENED_PROJECT, "");
            if (!Wizard.isEmpty(lastProjectPath)) {
                var projectFile = new File(lastProjectPath);
                if (projectFile.exists() && projectFile.isDirectory()) {
                    mainViewModel.setTreeViewFragmentTreeDir(projectFile);
                }
            }
        } catch (Throwable e) {
            // corrupted thus clear
            PreferencesUtils.clearPreference(PreferencesUtils.getLastOpenedProjectPreferences(),
                Constants.SharedPreferenceKeys.KEY_LAST_OPENED_PROJECT);
            logger.e(LOG_TAG, "Failed to reopen last opened project: " + e);
        }
    }

    @Override
    public void onPrepareMenu(@NonNull Menu menu) {
        onPrepareToolbarOptionsMenus(menu);
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.main_menu, menu);
        if (menu instanceof MenuBuilder menuBuilder) {
            menuBuilder.setOptionalIconsVisible(true);

            for (MenuItem item : menuBuilder.getVisibleItems()) {
                int iconMarginPx = BaseUtil.dp(MENU_ICON_MARGIN);
                if (item.getIcon() != null) {
                    item.setIcon(new InsetDrawable(item.getIcon(), iconMarginPx, 0, iconMarginPx,
                        0));
                }
            }
        }
    }

    @Override
    public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
        final boolean handled = onToolbarOptionsMenuItemSelected(menuItem);
        if (handled) invalidateMenu();
        return handled;
    }

    private boolean onToolbarOptionsMenuItemSelected(MenuItem item) {
        final int id = item.getItemId();
        final CodeEditorPane editorPane = getSelectedCodeEditorPane();
        final WebViewPane webViewPane = getSelectedWebViewPane();

        if (editorPane != null && editorPane.getEditor() != null) {
            return handleCodeEditorActions(item, id, editorPane);
        } else if (webViewPane != null) {
            return handleWebViewActions(item, id, webViewPane);
        }
        return false;
    }
    
    private boolean handleCodeEditorActions(MenuItem item, int id, CodeEditorPane editorPane) {
        if (id == R.id.menu_run) {
            editorPane.saveEditor();
            mainViewModel.setWebViewPaneFile(editorPane.getFile());
            return true;
        } else if (id == R.id.menu_undo) {
            editorPane.undo();
            return true;
        } else if (id == R.id.menu_redo) {
            editorPane.redo();
            return true;
        } else if (id == R.id.menu_save_file) {
            editorPane.saveEditor();
            return true;
        } else if (id == R.id.menu_save_as) {
            editorPane.saveAs();
            return true;
        } else if (id == R.id.menu_reload_file) {
            editorPane.reloadFile();
            return true;
        } else if (id == R.id.menu_reload_with_charset) {
            editorPane.showCharsetSelectionDialog();
            return true;
        } else if (id == R.id.menu_file_statistics) {
            editorPane.showStatistics();
            return true;
        } else if (id == R.id.menu_findFile) {
            editorPane.getSearchManager().openSearchPanel(true);
            return true;
        } else if (id == R.id.menu_jump_to_line) {
            editorPane.doJumpToLine();
            return true;
        } else if (id == R.id.menu_read_only_mode) {
            final var newState = !item.isChecked();
            editorPane.makeReadOnly(newState);
            item.setChecked(newState);
            return true;
        } else if (id == R.id.menu_copy_line) {
            editorPane.getEditor().copyText();
            return true;
        } else if (id == R.id.menu_delete_line) {
            editorPane.getEditor().deleteLine();
            return true;
        } else if (id == R.id.menu_replace_line) {
            editorPane.getEditor().replaceCurrLine();
            return true;
        } else if (id == R.id.menu_duplicate_line) {
            editorPane.getEditor().duplicateLine();
            return true;
        } else if (id == R.id.menu_convert_to_lowercase) {
            editorPane.getEditor().convertSelectionToLowerCase();
            return true;
        } else if (id == R.id.menu_convert_to_uppercase) {
            editorPane.getEditor().convertSelectionToUpperCase();
            return true;
        } else if (id == R.id.menu_reset_color_schemes) {
            editorPane.refreshEditorLanguageSyntax();
            return true;
        } else if (id == R.id.menu_cut_line) {
            editorPane.getEditor().cutLine();
            return true;
        }
        return false;
    }
    
    private boolean handleWebViewActions(MenuItem item, int id, WebViewPane webViewPane) {
        final WebView webView = webViewPane.getWebView();
        final boolean newCheckedState = !item.isChecked();

        if (id == R.id.menu_undo) {
            if (webView.canGoBack()) {
                webView.goBack();
            } else {
                BaseUtil.toastShort(R.string.alrt_cannot_go_back);
            }
            return true;
        } else if (id == R.id.menu_redo) {
            if (webView.canGoForward()) {
                webView.goForward();
            } else {
                BaseUtil.toastShort(R.string.alrt_cannot_go_forward);
            }
            return true;
        } else if (id == R.id.menu_zoom) {
            webViewPane.setZoomable(newCheckedState);
            item.setChecked(newCheckedState);
            return true;
        } else if (id == R.id.menu_desktop_mode) {
            webViewPane.enableDeskTopMode(newCheckedState);
            item.setChecked(newCheckedState);
            return true;
        } else if (id == R.id.menu_refresh) {
            webViewPane.refresh();
            return true;
        } else if (id == R.id.menu_open_in_browser) {
            webViewPane.openInDeviceBrowser();
            return true;
        } else if (id == R.id.menu_copy_url) {
            final String url = webView.getOriginalUrl();
            if (!Wizard.isEmpty(url)) {
                BaseUtil.copyToClipBoard(url, true);
            }
            return true;
        }
        return false;
    }
    
    private void onPrepareToolbarOptionsMenus(Menu menu) {
        CodeEditorPane editorPane = getSelectedCodeEditorPane();
        WebViewPane webViewPane = getSelectedWebViewPane();

        if (editorPane != null) {
            menu.setGroupVisible(R.id.group_file_operations, true);
            menu.setGroupVisible(R.id.group_editor_actions, true);
            menu.setGroupVisible(R.id.group_unredo, true);
            
            if (editorPane.isReadOnlyMode()) {
                menu.setGroupEnabled(R.id.group_unredo, false);
                menu.setGroupEnabled(R.id.group_content_edit, false);
                menu.setGroupEnabled(R.id.group_file_operations, false);
            } else {
                menu.setGroupEnabled(R.id.group_unredo, true);
                menu.setGroupVisible(R.id.group_content_edit, true);
                menu.setGroupEnabled(R.id.group_file_operations, true);
                menu.findItem(R.id.menu_undo).setEnabled(editorPane.canUndo());
                menu.findItem(R.id.menu_redo).setEnabled(editorPane.canRedo());
            }
            menu.findItem(R.id.menu_run).setVisible(Constants.isMarkUp(editorPane.getFile()));
            menu.findItem(R.id.menu_save_file).setEnabled(editorPane.isModified());
            menu.findItem(R.id.menu_read_only_mode).setChecked(editorPane.isReadOnlyMode());
            // menu.findItem(R.id.?).setEnabled(!BaseUtil.isSoftInputVisible(this));
        } else if (webViewPane != null) {
            menu.setGroupVisible(R.id.group_file_operations, false);
            menu.setGroupVisible(R.id.group_content_edit, false);
            menu.setGroupVisible(R.id.group_editor_actions, false);
            menu.setGroupVisible(R.id.group_unredo, true);
            
            menu.findItem(R.id.menu_liveserver).setVisible(true);
            menu.findItem(R.id.menu_zoom).setChecked(webViewPane.isZoomable());
            menu.findItem(R.id.menu_desktop_mode).setChecked(webViewPane.isDeskTopMode());
            menu.findItem(R.id.menu_redo).setEnabled(webViewPane.canRedo());
            menu.findItem(R.id.menu_undo).setEnabled(webViewPane.canUndo());
        } else {
            // No active editor or web view - hide all editor-specific groups
            menu.setGroupVisible(R.id.group_file_operations, false);
            menu.setGroupVisible(R.id.group_content_edit, false);
            menu.setGroupVisible(R.id.group_editor_actions, false);
            menu.setGroupVisible(R.id.group_unredo, false);
            menu.findItem(R.id.menu_liveserver).setVisible(false);
        }
    }

    private CodeEditorPane getSelectedCodeEditorPane() {
        if (currentPanePair == null) return null;
        Pane current = currentPanePair.second;
        return (current instanceof CodeEditorPane) ? (CodeEditorPane) current : null;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences pref, @Nullable String key) {
        if (key != null) {
            if (key.equals(Constants.SharedPreferenceKeys.KEY_SHARE_STATISTICS)) {
                if (PreferencesUtils.canShareAnonymousStatistics()) User.registerSession();
            }
        }
    }

    public void closeApp() {
        onBackPressedCallback.setEnabled(true);
    }

    public void createFileFromManager() {
        var dialogBinding =
            LayoutDialogTextInputBinding.inflate(LayoutInflater.from(requireContext()));
        var builder = new MaterialAlertDialogBuilder(requireContext());

        builder.setTitle(R.string.new_file);
        builder.setView(dialogBinding.getRoot());
        dialogBinding.tilName.setHint(getString(R.string.prompt_file_name));

        builder.setPositiveButton(getString(R.string.next), (dialog, which) -> {
            String prepName = null;
            if (dialogBinding.tilName.getEditText() != null) {
                prepName = dialogBinding.tilName.getEditText().getText().toString();
            }
            if (prepName != null && prepName.isEmpty()) {
                if (lifeCycleObserver != null) {
                    lifeCycleObserver.createFile(getString(R.string.untitled));
                }
            }
        });

        builder.setNegativeButton(getString(R.string.cancel), null);
        builder.show();
    }

    public static MainFragment newInstance() {
        return new MainFragment();
    }

    public static MainFragment newInstance(@NonNull Bundle arg) {
        final var fragment = new MainFragment();
        fragment.setArguments(arg);
        return fragment;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCurrentPaneChangeEvent(@NonNull CurrentPaneEvent event) {
        currentPanePair = Pair.create(event.getIndex(), event.getPane());
        invalidateMenu();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEditorModificationEvent(EditorModificationEvent event) {
        invalidateMenu();
    }

    public void openFileFromManager() {
        if (lifeCycleObserver != null) lifeCycleObserver.pickFile();
    }

    public void openFolderFromManager() {
        if (lifeCycleObserver != null) lifeCycleObserver.pickFolder();
    }

    @UsedByReflection
    public void openFolderInTreeViewFragment(File dir) {
        // BaseFragment performs sanity check for invalid files
        mainViewModel.setTreeViewFragmentTreeDir(dir);
        invalidateMenu();
    }

    @UsedByReflection
    public void openZipFileFromManager() {
        if (lifeCycleObserver != null) lifeCycleObserver.pickZipFile();
    }
}
