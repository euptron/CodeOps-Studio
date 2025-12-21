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
 * questions or need additional information. Email: etido.up@gmail.com
 */

package com.eup.codeopsstudio.ui.editor;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.eup.codeopsstudio.R;
import com.eup.codeopsstudio.common.Constants;
import com.eup.codeopsstudio.common.ILog;
import com.eup.codeopsstudio.common.models.ProjectEvent;
import com.eup.codeopsstudio.common.util.PathResolver;
import com.eup.codeopsstudio.common.util.PreferencesUtils;
import com.eup.codeopsstudio.databinding.FragmentBaseBinding;
import com.eup.codeopsstudio.domain.events.CurrentPaneEvent;
import com.eup.codeopsstudio.domain.events.EditorModificationEvent;
import com.eup.codeopsstudio.logger.Logger;
import com.eup.codeopsstudio.models.ExtensionTable;
import com.eup.codeopsstudio.pane.EditorPane;
import com.eup.codeopsstudio.pane.Pane;
import com.eup.codeopsstudio.pane.TextPane;
import com.eup.codeopsstudio.ui.editor.actions.BuildActionFragment;
import com.eup.codeopsstudio.ui.editor.code.CodeEditorPane;
import com.eup.codeopsstudio.ui.editor.panes.EmptyPaneWindow;
import com.eup.codeopsstudio.ui.editor.panes.WebViewPane;
import com.eup.codeopsstudio.ui.editor.panes.WelcomePane;
import com.eup.codeopsstudio.ui.pane.PaneWindow;
import com.eup.codeopsstudio.ui.pane.PaneWindowManager;
import com.eup.codeopsstudio.ui.pane.factory.PaneFactoryImpl;
import com.eup.codeopsstudio.ui.settings.PreferencesFragment;
import com.eup.codeopsstudio.ui.settings.SettingsPane;
import com.eup.codeopsstudio.util.Wizard;
import com.eup.codeopsstudio.util.manager.FileManager;
import com.eup.codeopsstudio.viewmodel.MainViewModel;
import com.eup.codeopsstudio.viewmodel.SavedStateViewModel;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.tabs.TabLayout;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Base fragment: A class that holds a functional editor Including its action sheets TES: Tabbed
 * editor system
 *
 * @author Etido Peter
 */
public class BaseFragment extends Fragment
    implements SharedPreferences.OnSharedPreferenceChangeListener,
        PaneWindowManager.EventWatcher,
        PaneWindowManager.CustomTabUIState {

  public static final String TAG = BaseFragment.class.getSimpleName();
  public static final String LOG_TAG = "BaseInterface";
  private Logger logger;
  private PaneWindow paneWindow;
  private MainViewModel mainViewModel;
  private final OnBackPressedCallback mOnBackPressedCallback =
      new OnBackPressedCallback(false) {
        @Override
        public void handleOnBackPressed() {
          mainViewModel.setBottomSheetState(BottomSheetBehavior.STATE_COLLAPSED);
        }
      };
  private File lastOpenedProject;
  private FragmentBaseBinding binding;
  private boolean closeUnPinnedProjectPanes;
  private SharedPreferences sharedPreferences;
  private BottomSheetBehavior<View> mBehavior;
  private SavedStateViewModel stateViewModel;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    logger = new Logger(Logger.LogClass.IDE);
  }

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    binding = FragmentBaseBinding.inflate(inflater, container, false);
    return binding.getRoot();
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    sharedPreferences = PreferencesUtils.getGlobalPreferences();
    mainViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
    stateViewModel = new ViewModelProvider(requireActivity()).get(SavedStateViewModel.class);
    logger.attach(requireActivity());

    paneWindow = new PaneWindowManager(requireContext(), binding.paneWindow, this, this, this);
    paneWindow.closeTabsRelativeToFirst(PreferencesUtils.canCloseRelativeToFirstDepth());
    closeUnPinnedProjectPanes = PreferencesUtils.canCloseUnPinnedProjectPanes();
    paneWindow.addEmptyPaneWindow(createEmptyPaneView());

    setupBottomSheet();
    configureObservers();
    createWelcomePane();

    paneWindow.restorePanes(this::onPanesReadyForRestoration);
  }

  @Override
  public ImageButton getTabCloseImageButton(@NonNull View view) {
    return view.findViewById(R.id.pane_action_button);
  }

  @Override
  public ImageView getTabIconImageView(@NonNull View view) {
    return view.findViewById(R.id.tab_icon);
  }

  @Override
  public int getTabIconResId(@NonNull Pane pane) {
    int iconRes;

    if (pane instanceof CodeEditorPane editorPane) {
      var file = editorPane.getFile();
      iconRes = ExtensionTable.getExtensionIcon(file.getName());
    } else if (pane instanceof WebViewPane) {
      iconRes = R.drawable.ic_access_point;
    } else if (pane instanceof WelcomePane) {
      iconRes = R.drawable.ic_codeopsstudio;
    } else if (pane instanceof SettingsPane) {
      iconRes = R.drawable.ic_cog_outline;
    } else {
      iconRes = 0;
    }

    return iconRes;
  }

  @Override
  public int getTabLayoutResId() {
    return R.layout.pane_tab_item;
  }

  @Override
  public TextView getTabTitleTextView(@NonNull View view) {
    return view.findViewById(R.id.tab_text);
  }

  @Override
  public void onSharedPreferenceChanged(SharedPreferences pref, @Nullable String key) {
    if (key == null) return;

    switch (key) {
      case Constants.SharedPreferenceKeys.KEY_CODE_EDITOR_RELATIVE_CLOSE_DEPTH:
        paneWindow.closeTabsRelativeToFirst(PreferencesUtils.canCloseRelativeToFirstDepth());
        break;
      case Constants.SharedPreferenceKeys.KEY_CODE_EDITOR_CLOSE_UNPINNED_PROJECT_PANES:
        closeUnPinnedProjectPanes = PreferencesUtils.canCloseUnPinnedProjectPanes();
        break;
      case Constants.SharedPreferenceKeys.KEY_DISPLAY_TAB_ICONS:
        paneWindow.showTabIcons(PreferencesUtils.canDisplayTabIcons());
        break;
      default:
        break;
    }
  }

  @Override
  public void onTabSelected(@Nullable TabLayout.Tab tab, @Nullable Pane pane) {
    if (tab != null && pane != null) {
      // Normal case
      final int position = tab.getPosition();
      ILog.debug(
          TAG, "Sending CurrentPaneEvent - Position: " + position + ", Pane: " + pane.getTitle());
      var event = new CurrentPaneEvent(Pair.create(position, pane));
      EventBus.getDefault().post(event);
    } else {
      // This is the "empty" event from Fix 2
      ILog.debug(TAG, "Sending CurrentPaneEvent - No pane selected (all tabs closed)");
      var event = new CurrentPaneEvent(Pair.create(-1, (Pane) null));
      EventBus.getDefault().post(event);
    }
  }

  @Override
  public String requireTabTitle(@NonNull Pane pane) {
    if (pane instanceof CodeEditorPane editor) {
      File file = editor.getFile();
      String name = (file != null) ? getUniqueName(file) : "INVALID-TAB";
      return editor.isModified() ? "*" + name : name;
    }
    return pane.getTitle();
  }

  @Override
  public void onStart() {
    super.onStart();
    if (!EventBus.getDefault().isRegistered(this)) {
      EventBus.getDefault().register(this);
    }
  }

  @Override
  public void onResume() {
    super.onResume();
    PreferencesUtils.getDefaultPreferences().registerOnSharedPreferenceChangeListener(this);
    sharedPreferences.registerOnSharedPreferenceChangeListener(this);
  }

  @Override
  public void onSaveInstanceState(@NonNull Bundle outState) {
    super.onSaveInstanceState(outState);
    stateViewModel.saveActionSheetState(mBehavior.getState());
  }

  @Override
  public void onPause() {
    super.onPause();
    if (paneWindow != null) paneWindow.persistPanes(WelcomePane.class);
    PreferencesUtils.getDefaultPreferences().unregisterOnSharedPreferenceChangeListener(this);
    sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    mainViewModel.getBottomSheetExpanded().removeObservers(getViewLifecycleOwner());
    mainViewModel.getBottomSheetState().removeObservers(getViewLifecycleOwner());
    mainViewModel.addSettingsPane().removeObservers(getViewLifecycleOwner());
    this.binding = null;
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    if (EventBus.getDefault().isRegistered(this)) {
      EventBus.getDefault().unregister(this);
    }
    if (paneWindow != null) paneWindow.destroy();
  }

  private void setupBottomSheet() {
    mBehavior = BottomSheetBehavior.from(binding.actionsSheet);
    mBehavior.setGestureInsetBottomIgnored(true);
    mBehavior.addBottomSheetCallback(
        new BottomSheetBehavior.BottomSheetCallback() {
          @Override
          public void onStateChanged(@NonNull View p1, int state) {
            mainViewModel.setBottomSheetState(state);
          }

          @Override
          public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            if (isAdded()) {
              var bundle = new Bundle();
              bundle.putFloat("offset", slideOffset);
              getChildFragmentManager().setFragmentResult(BuildActionFragment.OFFSET_KEY, bundle);
            }
          }
        });
    mBehavior.setHalfExpandedRatio(0.3f);
    mBehavior.setFitToContents(false);

    stateViewModel
        .getActionSheetState()
        .observe(
            getViewLifecycleOwner(),
            state -> {
              int sheetBehaviour = (state != null) ? state : BottomSheetBehavior.STATE_COLLAPSED;
              restoreViewState(sheetBehaviour);
            });
  }

  private View createEmptyPaneView() {
    var windowPane =
        new EmptyPaneWindow(mainViewModel, getViewLifecycleOwner(), requireContext(), "Empty Pane");
    return windowPane.createView();
  }

  private void onPanesReadyForRestoration(List<Pane> loadedPanes) {
    // SAFETY CHECK: If the app was stopped while loading panes, stop here.
    if (!isAdded() || isStateSaved()) {
      ILog.warning(TAG, "Skipping pane restoration: Fragment is not added or state is saved.");
      return;
    }

    if (loadedPanes == null || loadedPanes.isEmpty()) {
      return;
    }

    Pane paneToSelect = null;

    for (Pane pane : loadedPanes) {
      if (pane != null && !isPaneDuplicate(pane)) {
        paneWindow.add(pane, false);
        if (pane.isSelected()) {
          paneToSelect = pane;
        }
      }
    }

    for (Pane pane : paneWindow.getPanes()) {
      restorePaneState(pane);
    }

    if (paneToSelect != null) {
      ILog.info(TAG, "Selecting: " + paneToSelect.getTitle());
      paneWindow.selectTab(paneToSelect);
    } else if (!paneWindow.getPanes().isEmpty()) {
      ILog.info(TAG, "Falling back to first tab");
      paneWindow.selectTab(0); // fallback to first tab
    }

    paneWindow.syncTabs();
  }

  private boolean isPaneDuplicate(Pane newPane) {
    // Check for duplicates
    if (newPane instanceof WelcomePane) {
      return paneWindow.findPane(WelcomePane.class) != null;
    }

    if (newPane instanceof SettingsPane) {
      return paneWindow.findPane(SettingsPane.class) != null;
    }

    if (newPane instanceof WebViewPane) {
      return paneWindow.findPane(WebViewPane.class) != null;
    }

    if (newPane instanceof CodeEditorPane newEditorPane) {
      String newFilePath = newEditorPane.getFilePath();
      if (newFilePath != null) {
        CodeEditorPane existingPane =
            paneWindow.findPane(
                CodeEditorPane.class, pane -> newFilePath.equals(pane.getFilePath()));
        return existingPane != null;
      }
    }

    return false;
  }

  private void restorePaneState(Pane pane) {
    if (pane == null) return;

    if (pane.getArguments() == null) {
      ILog.warning(TAG, "Cannot restore Pane State for: " + pane.getTitle() + "arguments are null");
      return;
    } else {
      ILog.info(TAG, "Attempting to restore Pane State for: " + pane.getTitle());
    }

    if (pane instanceof TextPane tp) {
      restoreTextPane(tp);
    } else if (pane instanceof EditorPane ep) {
      restoreEditorPane(ep);
    } else if (pane instanceof SettingsPane sp) {
      restoreSettingsPane(sp);
    } else if (pane instanceof WebViewPane wvp) {
      restoreWebViewPane(wvp);
    } else if (pane instanceof CodeEditorPane cep) {
      restoreCodeEditorPane(cep);
    }
  }

  private void restoreCodeEditorPane(@NonNull CodeEditorPane pane) {
    // CodeEditorPane handles restoration
    if (pane.hasPersistedEditorChanges()) {
      ILog.debug(
          TAG,
          "CodeEditorPane has persisted changes - will restoration content on selection "
              + pane.getTitle());
    } else {
      ILog.debug(
          TAG,
          "CodeEditorPane has no persisted changes - will load file on selection: "
              + pane.getTitle());
    }
  }

  private void restoreWebViewPane(@NonNull WebViewPane pane) {
    final Map<String, Object> arguments = pane.getArguments();
    final boolean zoomable = PaneFactoryImpl.requireBoolean(WebViewPane.KEY_IS_ZOOMABLE, arguments);
    final boolean desktopMode =
        PaneFactoryImpl.requireBoolean(WebViewPane.KEY_DESKTOP_MODE, arguments);
    final String previewFilePath =
        PaneFactoryImpl.requireString(WebViewPane.KEY_PREVIEW_FILE_PATH, arguments);

    pane.setZoomable(zoomable);
    pane.enableDeskTopMode(desktopMode);
    pane.loadFile(new File(previewFilePath));
  }

  private void restoreSettingsPane(@NonNull SettingsPane pane) {
    // TODO: Implement and Handle this
  }

  private void restoreTextPane(@NonNull TextPane pane) {
    final Map<String, Object> arguments = pane.getArguments();
    final String CONTENT_KEY = Pane.PaneConstants.TEXT_PANE_ARGUMENT_KEY;
    final String content = PaneFactoryImpl.requireString(CONTENT_KEY, arguments);

    pane.setText(content);
  }

  private void restoreEditorPane(@NonNull EditorPane pane) {
    final Map<String, Object> arguments = pane.getArguments();
    final String CONTENT_KEY = Pane.PaneConstants.EDITOR_PANE_ARGUMENT_KEY;
    final String content = PaneFactoryImpl.requireString(CONTENT_KEY, arguments);

    pane.setText(content);
  }

  private void configureObservers() {
    mainViewModel
        .getBottomSheetState()
        .observe(
            getViewLifecycleOwner(),
            state -> {
              if (state == BottomSheetBehavior.STATE_DRAGGING
                  || state == BottomSheetBehavior.STATE_SETTLING) {
                return;
              }
              mBehavior.setState(state);
              mOnBackPressedCallback.setEnabled(state == BottomSheetBehavior.STATE_EXPANDED);
            });

    mainViewModel.observeEditorFileOpening(
        getViewLifecycleOwner(),
        file -> {
          if (file != null && file.exists()) {
            var fileName = file.getName();
            // Sanity checks
            if (fileName.endsWith(".apk")) {
              Wizard.installApplication(requireContext(), file);
            } else {
              openFileInPane(file);
            }
          }
        });

    mainViewModel
        .getWebViewPaneFile()
        .observe(
            getViewLifecycleOwner(),
            file -> {
              if (file != null) addWebViewPane(file);
            });

    mainViewModel
        .getBottomSheetExpanded()
        .observe(
            getViewLifecycleOwner(),
            expanded -> {
              if (Boolean.TRUE.equals(expanded)) {
                mBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
              } else {
                mBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
              }
            });

    mainViewModel
        .addSettingsPane()
        .observe(
            getViewLifecycleOwner(),
            canAdd -> {
              if (canAdd) {
                addSettingsPane(true);
              }
            });
  }

  public void createWelcomePane() {
    WelcomePane welcomePane = paneWindow.findPane(WelcomePane.class);

    if (welcomePane == null) {
      welcomePane = new WelcomePane(requireContext(), getString(R.string.welcome));
      welcomePane.setPinned(true);
      if (PreferencesUtils.canShowWelcomePanel()) {
        paneWindow.add(welcomePane, 0, false);
      }
    } else {
      paneWindow.selectTab(welcomePane);
    }
  }

  public void addSettingsPane(boolean select) {
    SettingsPane pane = paneWindow.findPane(SettingsPane.class);

    if (pane == null) {
      String title = getString(R.string.settings);
      pane = new SettingsPane(getContext(), title, PreferencesFragment.newInstance());
      pane.attach(getViewLifecycleOwner());
      mainViewModel.requestCloseDrawer();
      paneWindow.add(pane, select);
    } else {
      mainViewModel.requestCloseDrawer();
      paneWindow.selectTab(pane); // already exists
    }
  }

  public void openFileInPane(@NonNull File file) {
    if (Constants.isPreviewAble(file)) {
      addWebViewPane(file);
      return;
    }

    if (FileManager.isOpenableFile(file)) {
      addCodeEditorPane(file);
    }
  }

  public void addCodeEditorPane(@NonNull File file) {
    ILog.debug(TAG, "addCodeEditorPane called - file: " + file.getName());

    CodeEditorPane editorPane =
        paneWindow.findPane(
            CodeEditorPane.class,
            pane -> {
              String path = pane.getFilePath();
              boolean found = path != null && path.equals(file.getAbsolutePath()); // already opened
              ILog.debug(TAG, "Pane search - path: " + path + ", found: " + found);
              return found;
            });

    Pane currentPane = paneWindow.getSelectedPane(); // You might need to add this method
    ILog.debug(
        TAG, "Current selected pane: " + (currentPane != null ? currentPane.getTitle() : "null"));
    if (editorPane == null) {
      String title = file.getName();
      editorPane = new CodeEditorPane(requireContext(), title);
      editorPane.setFile(file);
      mainViewModel.requestCloseDrawer();
      paneWindow.add(editorPane, /* select= */ true);
    } else {
      ILog.debug(TAG, "Creating new CodeEditorPane");
      mainViewModel.requestCloseDrawer();
      paneWindow.selectTab(editorPane);
    }

    paneWindow.syncTabs();
    // Verify selection worked
    Pane newCurrentPane = paneWindow.getSelectedPane();
    ILog.debug(
        TAG, "New selected pane: " + (newCurrentPane != null ? newCurrentPane.getTitle() : "null"));
  }

  public void addWebViewPane(@NonNull File file) {
    WebViewPane pane = paneWindow.findPane(WebViewPane.class);
    Runnable action = null;

    if (pane == null) {
      String title = getString(R.string.webview_pane_title) + " | " + file.getName();
      final WebViewPane finalPane = new WebViewPane(requireContext(), title);
      action =
          new Runnable() {
            @Override
            public void run() {
              mainViewModel.requestCloseDrawer();
              paneWindow.add(finalPane, false);
            }
          };
      pane = finalPane;
    }

    pane.loadFile(file);
    pane.setZoomable(true);
    pane.enableDeskTopMode(false);
    if (action != null) action.run();

    mainViewModel.requestCloseDrawer();
    paneWindow.selectTab(pane);
  }

  private void restoreViewState(int behaviorState) {
    boolean isExpanded = behaviorState == BottomSheetBehavior.STATE_EXPANDED;
    mainViewModel.setBottomSheetState(behaviorState);

    Bundle floatOffset = new Bundle();
    floatOffset.putFloat("offset", isExpanded ? 1f : 0f);
    getChildFragmentManager().setFragmentResult(BuildActionFragment.OFFSET_KEY, floatOffset);
  }

  public String getUniqueName(@NonNull File currentFile) {
    int sameFileNameCount = 0;
    PathResolver<File> builder = new PathResolver<>("", "/");

    for (var pane : Objects.requireNonNull(paneWindow.getPanes())) {
      if (pane instanceof CodeEditorPane editor) {
        File openFile = editor.getFile();
        if (openFile.getName().equals(currentFile.getName())) {
          sameFileNameCount++;
        }
        builder.addPath(openFile, openFile.getPath());
      }
    }

    if (sameFileNameCount > 1) {
      return builder.getShortPath(currentFile);
    } else {
      return currentFile.getName();
    }
  }

  public static BaseFragment newInstance() {
    return new BaseFragment();
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onEditorModificationEvent(EditorModificationEvent event) {
    paneWindow.syncTabs();
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onProjectChangeEvent(ProjectEvent event) {
    final var currentProject = event.getFile();

    if (currentProject == null) {
      lastOpenedProject = null;
      return;
    }

    if (lastOpenedProject != null && lastOpenedProject.equals(currentProject)) {
      ILog.debug(TAG, "Project already open");
      return;
    }

    // Only close tabs if we're switching from one project to another
    if (lastOpenedProject != null && paneWindow != null) {
      paneWindow.closeAll(closeUnPinnedProjectPanes, pane -> !(pane instanceof WelcomePane));
    }

    lastOpenedProject = currentProject;
  }
}
