/*************************************************************************
 * This file is part of CodeOps Studio.
 * CodeOps Studio - code anywhere anytime
 * https://github.com/euptron/CodeOps-Studio
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

package com.eup.codeopsstudio.ui.editor;

import static com.eup.codeopsstudio.common.Constants.SharedPreferenceKeys;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.TooltipCompat;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import com.blankj.utilcode.util.EncodeUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayout.Tab;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;
import com.eup.codeopsstudio.MainFragment;
import com.eup.codeopsstudio.common.AsyncTask;
import com.eup.codeopsstudio.common.Constants;
import com.eup.codeopsstudio.common.models.BaseEvent;
import com.eup.codeopsstudio.common.models.ProjectEvent;
import com.eup.codeopsstudio.common.util.PathResolver;
import com.eup.codeopsstudio.common.util.PreferencesUtils;
import com.eup.codeopsstudio.databinding.FragmentBaseBinding;
import com.eup.codeopsstudio.editor.ContextualCodeEditor;
import com.eup.codeopsstudio.events.CurrentPaneEvent;
import com.eup.codeopsstudio.events.EditorModificationEvent;
import com.eup.codeopsstudio.listeners.onPaneTabSelectedListener;
import com.eup.codeopsstudio.logging.Logger;
import com.eup.codeopsstudio.models.ExtensionTable;
import com.eup.codeopsstudio.pane.EditorPane;
import com.eup.codeopsstudio.pane.FragmentPane;
import com.eup.codeopsstudio.pane.Pane;
import com.eup.codeopsstudio.pane.Rap;
import com.eup.codeopsstudio.pane.TextPane;
import com.eup.codeopsstudio.res.R;
import com.eup.codeopsstudio.ui.editor.code.CodeEditorPane;
import com.eup.codeopsstudio.ui.editor.panes.EmptyPaneWindow;
import com.eup.codeopsstudio.ui.editor.panes.WebViewPane;
import com.eup.codeopsstudio.ui.editor.panes.WelcomePane;
import com.eup.codeopsstudio.ui.settings.PreferencesFragment;
import com.eup.codeopsstudio.ui.settings.SettingsPane;
import com.eup.codeopsstudio.util.Wizard;
import com.eup.codeopsstudio.viewmodel.MainViewModel;
import io.github.rosemoe.sora.text.Content;
import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import androidx.lifecycle.Observer;
import java.util.concurrent.CompletableFuture;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Base fragment: A class that holds a functional editor Including its action sheets TES: Tabbed
 * editor system
 *
 * @author EUP
 */
public class BaseFragment extends Fragment
    implements SharedPreferences.OnSharedPreferenceChangeListener,
        TabLayout.OnTabSelectedListener,
        onPaneTabSelectedListener {

  public static final String TAG = BaseFragment.class.getSimpleName();
  public static final String LOG_TAG = "BaseUI";
  private BottomSheetBehavior<View> mBehavior;
  protected MainViewModel mMainViewModel;
  private Logger logger;
  private FragmentBaseBinding binding;
  private List<CodeEditorPane> addedPanes = new ArrayList<>(); // tracking list
  private ContextualCodeEditor editor;
  protected boolean canAutoSave = false;
  private PopupMenu mPopupMenu;
  // TODO: Replace showTabIcons with {@link PreferencesUtils.canShowTabIcons()}
  private boolean showTabIcons = true;
  private final OnBackPressedCallback mOnBackPressedCallback =
      new OnBackPressedCallback(false) {
        @Override
        public void handleOnBackPressed() {
          mMainViewModel.setBottomSheetState(BottomSheetBehavior.STATE_COLLAPSED);
        }
      };
  private SharedPreferences sharedPreferences;
  private MutableLiveData<List<Pair<Tab, Pane>>> panesLiveData =
      new MutableLiveData<>(new LinkedList<>());
  private MutableLiveData<Pair<Integer, Pair<Tab, Pane>>> currentPaneLiveData =
      new MutableLiveData<>(null);

  private Observer<List<Pair<Tab, Pane>>> panesObserver;
  private Observer<Pair<Integer, Pair<Tab, Pane>>> currentPaneObserver;

  public static BaseFragment newInstance() {
    return new BaseFragment();
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    logger = new Logger(Logger.LogClass.IDE);
  }

  @Override
  public void onSaveInstanceState(@NonNull Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putInt("bottom_sheet_state", mBehavior.getState());
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
    PreferencesUtils.getDefaultPreferences().registerOnSharedPreferenceChangeListener(this);
    sharedPreferences = PreferencesUtils.getPersistentPanesPreferences();
    mMainViewModel =
        new ViewModelProvider(requireActivity() /*shared activity scope*/).get(MainViewModel.class);

    mBehavior = BottomSheetBehavior.from(binding.actionsSheet);
    mBehavior.setGestureInsetBottomIgnored(true);
    mBehavior.addBottomSheetCallback(
        new BottomSheetBehavior.BottomSheetCallback() {
          @Override
          public void onStateChanged(@NonNull View p1, int state) {
            mMainViewModel.setBottomSheetState(state);
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

    if (savedInstanceState != null) {
      restoreViewState(savedInstanceState);
    }

    logger.attach(requireActivity() /*shared activity scope*/);
    binding.tablayout.addOnTabSelectedListener(this);

    configureObservers();
    createEmptyPaneWindow();
    restorePersistedPanes();
    addWelcomePane(/* pinned= */ true);
  }

  @Override
  public void onSharedPreferenceChanged(SharedPreferences pref, String key) {
    switch (key) {
      case SharedPreferenceKeys.KEY_CODE_EDITOR_AUTO_SAVE:
        canAutoSave = PreferencesUtils.autoSaveFiles();
        break;
    }
  }

  /**
   * Callback method to handle the selection of a tab in the TabLayout. Retrieves the associated
   * Pane for the selected tab.
   *
   * @param tab The selected tab.
   */
  @Override
  public void onTabSelected(@NonNull Tab tab) {
    final int position = tab.getPosition();
    // Now display the already added pane view {@see #addPane(Pane,boolean)} at this associate
    // position
    binding.paneContainer.setDisplayedChild(position);
    var pair = getPair(tab);
    // mark selected and also for persistency
    pair.second.onSelected();
    // post current pane
    EventBus.getDefault().post(new CurrentPaneEvent(position, pair.second));
    currentPaneLiveData.setValue(Pair.create(position, pair));

    var editorPane = getSelectedCodeEditorPane(pair.second);
    var welcomePane = getSelectedWelcomePane(pair.second);

    if (welcomePane != null) {
      // hideActionSheet(true);
    } else if (editorPane != null) {
      // hideActionSheet(false);
      editor = editorPane.getEditor();
    }
    updateTabs();
    invalidateMainMenus();
  }

  @Override
  public void onTabUnselected(Tab tab) {
    var pair = getPair(tab);
    pair.second.onUnselected();
    updateTabs();
  }

  @Override
  public void onTabReselected(Tab tab) {
    var position = tab.getPosition();
    var pair = getPaneTab(position);
    // mark selected and also for persistency
    pair.second.onReselected();
    var pane = pair.second;

    showPopupMenu(
        tab.getCustomView(),
        pane,
        menuItem -> {
          final var id = menuItem.getItemId();
          final var title = menuItem.getTitle();

          if (title.equals(getString(R.string.pane_close))) {
            close(tab);
          } else if (title.equals(getString(R.string.pane_close_others))) {
            closeOthers(tab);
          } else if (title.equals(getString(R.string.pane_close_all))) {
            closeAll();
          } else if (title.equals(getString(R.string.pane_close_right))) {
            closeToRightOf(tab, PreferencesUtils.canCloseRelativeToFirstDepth());
          } else if (title.equals(getString(R.string.pane_close_left))) {
            closeToLeftOf(tab, PreferencesUtils.canCloseRelativeToFirstDepth());
          } else if (title.equals(getString(R.string.pane_pin))
              || title.equals(getString(R.string.pane_unpin))) {
            pinPaneTab(tab, !pane.isPinned());
            menuItem.setTitle(pane.isPinned() ? R.string.pane_unpin : R.string.pane_pin);
          }
          invalidateMainMenus();
          return true;
        });
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    // release resources initalized onCreateView and onViewCreated
    mMainViewModel.getBottomSheetExpanded().removeObservers(getViewLifecycleOwner());
    mMainViewModel.getBottomSheetState().removeObservers(getViewLifecycleOwner());
    mMainViewModel.addSettingsPane().removeObservers(getViewLifecycleOwner());
    panesLiveData.removeObserver(panesObserver);
    PreferencesUtils.getDefaultPreferences().unregisterOnSharedPreferenceChangeListener(this);
    this.binding = null;
  }

  @Override
  public void onDestroy() {
    var iterator = getPaneTabs().iterator();
    if (iterator.hasNext()) {
      do {
        var pair = iterator.next();
        if (pair != null) {
          // destroy panes to avoid memory leaks
          pair.second.onDestroy();
        }
      } while (iterator.hasNext());
    }
    super.onDestroy();
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

  @Override
  public void onPause() {
    super.onPause();
    persistPanes();
  }

  /**
   * Close the selected pane relative to a tab
   *
   * @param tab The relative tab of the pane to close
   */
  @Override
  public void close(Tab tab) {
    for (Pair<Tab, Pane> pair : getPaneTabs()) {
      if (pair.first == tab && !pair.second.isPinned()) {
        removeCodeEditorPaneFromHistory(getSelectedCodeEditorPane(pair.second));
        binding.tablayout.removeTab(tab);
        binding.paneContainer.removeView(pair.second.getView());
        pair.second.onDestroy();
        removePaneTab(pair);
        break;
      }
    }
    binding.tablayout.requestLayout();
    invalidateMainMenus();
  }

  @Override
  public void closeOthers(Tab tabToKeep) {
    int max = getPaneTabSize();
    int keep = tabToKeep.getPosition();
    for (int i = max - 1; i >= 0; i--) {
      var pair = getPaneTab(i);
      if (i != keep && !pair.second.isPinned()) {
        removeCodeEditorPaneFromHistory(getSelectedCodeEditorPane(pair.second));
        binding.tablayout.removeTab(binding.tablayout.getTabAt(i));
        binding.paneContainer.removeView(binding.paneContainer.getChildAt(i));
        pair.second.onDestroy();
        removePaneTab(pair);
      }
    }
    binding.tablayout.requestLayout();
  }

  @Override
  public void closeAll() {
    var iterator = getPaneTabs().iterator();
    while (iterator.hasNext()) {
      var pair = iterator.next();
      if (!pair.second.isPinned()) {
        removeCodeEditorPaneFromHistory(getSelectedCodeEditorPane(pair.second));
        iterator.remove();
        binding.tablayout.removeTab(pair.first);
        binding.paneContainer.removeView(pair.second.getView());
        pair.second.onDestroy();
        removePaneTab(pair);
      }
    }
    binding.tablayout.requestLayout();
  }

  @Override
  public void closeToRightOf(Tab tab, boolean first) {
    int selectedIndex = tab.getPosition();
    int maxPosition = getPaneTabSize();
    if (first) {
      if (selectedIndex >= 0 && selectedIndex < maxPosition - 1) {
        for (int i = selectedIndex + 1; i < maxPosition; i++) {
          var pair = getPaneTab(i);
          if (!pair.second.isPinned()) {
            close(pair.first);
            break;
          }
        }
      }
    } else {
      int tabIndex = findTabIndex(tab);
      if (tabIndex >= 0 && tabIndex < maxPosition - 1) {
        for (int i = maxPosition - 1; i > tabIndex; i--) {
          close(getPaneTab(i).first);
        }
      }
    }
  }

  @Override
  public void closeToLeftOf(Tab tab, boolean first) {
    int selectedIndex = tab.getPosition();
    if (first) {
      if (selectedIndex > 0) {
        for (int i = selectedIndex - 1; i >= 0; i--) {
          Pair<Tab, Pane> pair = getPaneTab(i);
          if (!pair.second.isPinned()) {
            close(pair.first);
            break;
          }
        }
      }
    } else {
      if (selectedIndex >= 0) {
        for (int i = selectedIndex - 1; i >= 0; i--) {
          close(getPaneTab(i).first);
        }
      }
    }
  }

  @Override
  public void pinPaneTab(Tab tab, boolean isPinned) {
    for (Pair<Tab, Pane> pair : getPaneTabs()) {
      if (pair.first == tab) {
        pair.second.setPinned(isPinned);
        updateTab(pair.first, pair.second);
        break;
      }
    }
  }

  private void forceCloseAll(boolean closeUnPinned) {
    if (closeUnPinned) {
      closeAll();
    } else {
      hardCloseAll();
    }
  }

  private void hardCloseAll() {
    var iterator = getPaneTabs().iterator();
    while (iterator.hasNext()) {
      var pair = iterator.next();
      if (!(pair.second instanceof WelcomePane)) {
        removeCodeEditorPaneFromHistory(getSelectedCodeEditorPane(pair.second));
        iterator.remove();
        binding.tablayout.removeTab(pair.first);
        binding.paneContainer.removeView(pair.second.getView());
        pair.second.onDestroy();
        removePaneTab(pair);
      }
    }
    binding.tablayout.requestLayout();
  }

  /** Updates all the pane tabs. */
  public void updateTabs() {
    updateTabs(false);
  }

  /**
   * Updates the pane tabs.
   *
   * @param single If true, updates the first pane tabs; if false, updates all pane tab.
   */
  public void updateTabs(boolean single) {
    for (int i = 0; i < getPaneTabSize(); i++) {
      var pair = getPaneTab(i);
      var tab = pair.first;
      var pane = pair.second;

      if (tab != null && pane != null) {
        updateTab(tab, pane);
        if ((tab != null && tab.getCustomView() != null)) {
          var editor = getSelectedCodeEditorPane(pane);
          TextView tabText = tab.getCustomView().findViewById(R.id.tab_text);
          String title = null;

          if (editor != null) {
            File current = editor.getFile();
            String text = current != null ? getUniqueTabTitle(current) : "INVALID-TAB";
            if (editor.isModified()) {
              text = "*" + text;
            }
            title = text;
          } else {
            title = pane.getTitle();
          }

          if (title != null) {
            tabText.setText(title);
          }
        }
        if (single) break;
      }
    }
  }

  private void updateTab(Tab tab, Pane pane) {
    if (tab != null && tab.getCustomView() != null) {
      ImageButton actionButton = tab.getCustomView().findViewById(R.id.pane_action_button);

      if (pane.isPinned()) {
        actionButton.setImageResource(R.drawable.ic_pin_outline);
        actionButton.setVisibility(View.VISIBLE);
        TooltipCompat.setTooltipText(actionButton, getString(R.string.pane_unpin));
      } else {
        actionButton.setImageResource(R.drawable.ic_window_close);
        actionButton.setVisibility(View.VISIBLE);
        TooltipCompat.setTooltipText(actionButton, getString(R.string.close));
      }
    }
  }

  /**
   * Add panes into the pane tabbed editor system
   *
   * @param pane The pane to add
   * @param select Whether to select the added pane or not
   */
  private void addPane(Pane pane, boolean select) {
    if (pane == null) return;

    if (containsPane(pane)) {
      logger.d(LOG_TAG, "Pane already opened. Pane: " + pane.getTitle() + " !");
      selectPaneInTabLayout(pane);
      return;
    }

    pane.createView();
    binding.paneContainer.addView(pane.getView());
    var tab = createTab(pane);
    /**
     * Add the tab to the parent tablayout. #addTab(tab,false) is invoked with false so we do not
     * select the initial tab since it has not been added to the "panesLiveData". This caused
     * #getPair(Tab) to return null @see #onTabSelected(Tab)
     */
    binding.tablayout.addTab(tab, false);

    // Add the pane and its corresponding tab to the list of editors
    // var pair = new Pair<>(tab, pane);
    var pair = Pair.create(tab, pane);
    addPaneTab(pair);

    final int position = panesLiveData.getValue().size();
    // logger.d("Opening pane at index: " + position + " pane: " + pane.getTitle());
    if (select) {
      selectPaneInTabLayout(pane);
    }

    currentPaneLiveData.setValue(Pair.create(position, pair));
    updateTabs();
  }

  /**
   * Selects the tab corresponding to the provided pane in the TabLayout and scrolls to it. This
   * method iterates through the list of pane tabs directly, which is more efficient than using
   * indexOf() to find the position of the pane.
   *
   * @param pane The pane to select in the TabLayout.
   */
  public void selectPaneInTabLayout(Pane pane) {
    var pair = getPair(pane);
    if (pair != null) {
      if (pair.second.equals(pane)) {
        var tab = pair.first;
        if (tab != null) {
          if (!tab.isSelected()) {
           // select tab in parent
           binding.tablayout.selectTab(tab);
           mMainViewModel.setDrawerState(false);
          } else {
           /**
            * v0.0.1: Drawer didn't close when @see #addSettings was invoked with the settings pane-tab selected.
            * Fixed in v0.0.2: Drawer now closes when the settings pane-tab is selected.
            */
           mMainViewModel.setDrawerState(false);
          }
        }
      }
    }
  }

  /**
   * Creates a new Pane tab
   *
   * <p>TODO:
   * <li>Replace showTabIcons with {@link PreferencesUtils.canShowTabIcons()}
   */
  private Tab createTab(Pane pane) {
    var tab = binding.tablayout.newTab();

    tab.setCustomView(R.layout.pane_tab_item);

    if (tab.getCustomView() != null) {
      ImageView tabIcon = tab.getCustomView().findViewById(R.id.tab_icon);
      TextView tabText = tab.getCustomView().findViewById(R.id.tab_text);
      ImageButton actionButton = tab.getCustomView().findViewById(R.id.pane_action_button);

      var editor = getSelectedCodeEditorPane(pane);
      var webview = getSelectedWebViewPane(pane);
      var welcome = getSelectedWelcomePane(pane);
      var settings = getSelectedSettingsPane(pane);

      if (showTabIcons) {
        if (editor != null) {
          var file = editor.getFile();
          tabIcon.setImageResource(ExtensionTable.getExtensionIcon(file.getName()));
        } else if (webview != null) {
          tabIcon.setImageResource(R.drawable.ic_access_point);
        } else if (welcome != null) {
          tabIcon.setImageResource(R.drawable.ic_westudio);
        } else if (settings != null) {
          tabIcon.setImageResource(R.drawable.ic_cog_outline);
        }
        if (tabIcon.getVisibility() == View.INVISIBLE || tabIcon.getVisibility() == View.GONE) {
          tabIcon.setVisibility(View.VISIBLE);
        }
      } else {
        if (tabIcon.getVisibility() == View.VISIBLE) {
          tabIcon.setVisibility(View.GONE);
        }
      }
      tabText.setText(pane.getTitle());
      actionButton.setOnClickListener(
          v -> {
            if (pane.isPinned()) {
              pinPaneTab(tab, !pane.isPinned());
              return;
            }
            close(tab);
          });
    }
    return tab;
  }

  public void performUIUpdate() {
    panesObserver =
        paneTabs -> {
          updateUI(paneTabs);
        };
    panesLiveData.observe(getViewLifecycleOwner(), panesObserver);
  }

  public void updateUI(List<Pair<Tab, Pane>> paneTabs) {
    if (paneTabs.isEmpty()) {
      // hide tabs as they are not needed
      binding.tablayout.setVisibility(View.GONE);
      currentPaneLiveData.setValue(Pair.create(-1, null));
      showEmptyPaneWindow(true);
    } else {
      binding.tablayout.setVisibility(View.VISIBLE);
      showEmptyPaneWindow(false);
    }
  }

  private String getUniqueTabTitle(@NonNull File currentFile) {
    if (currentFile == null) return null;

    int sameFileNameCount = 0;
    PathResolver<File> builder = new PathResolver<>("", "/");

    for (Pair<Tab, Pane> pair : Objects.requireNonNull(getPaneTabs())) {
      if (pair != null) {
        var editor = getSelectedCodeEditorPane(pair.second);

        if (editor != null) {
          File openFile = editor.getFile();
          if (openFile.getName().equals(currentFile.getName())) {
            sameFileNameCount++;
          }
          builder.addPath(openFile, openFile.getPath());
        }
      }
    }

    if (sameFileNameCount > 1) {
      return builder.getShortPath(currentFile);
    } else {
      return currentFile.getName();
    }
  }

  private int findTabIndex(Tab tab) {
    if (tab == null) {
      logger.e(LOG_TAG, "Cannot find index of a null tab.");
      return -1;
    }
    for (int i = 0; i < getPaneTabSize(); i++) {
      if (getPaneTab(i).first == tab) {
        return i;
      }
    }
    return -1; // Tab not found
  }

  /**
   * Method to show the popup menu.
   *
   * @param context Context the popup menu is running in, through which it can access the current
   *     theme, resources, etc.
   * @param anchor Anchor view for this popup. The popup will appear below the anchor if there is
   *     room, or above it if there is not.
   * @param pane Pane assoctaied to a tab for the popup menu anchor.
   * @param listener Listener to handle menu item clicks.
   */
  private void showPopupMenu(
      @NonNull View anchor, Pane pane, PopupMenu.OnMenuItemClickListener listener) {
    if (mPopupMenu == null) {
      mPopupMenu = new PopupMenu(requireContext(), anchor, Gravity.NO_GRAVITY);
      // add menus
      mPopupMenu.getMenu().add(R.string.pane_close);
      mPopupMenu.getMenu().add(R.string.pane_close_others);
      mPopupMenu.getMenu().add(R.string.pane_close_all);
      mPopupMenu.getMenu().add(R.string.pane_close_right);
      mPopupMenu.getMenu().add(R.string.pane_close_left);
      mPopupMenu.getMenu().add(pane.isPinned() ? R.string.pane_unpin : R.string.pane_pin);
      mPopupMenu.setOnMenuItemClickListener(listener);
      mPopupMenu.setOnDismissListener(menu -> mPopupMenu = null);
      mPopupMenu.show();
    }
  }

  public Pair<Tab, Pane> getPair(Tab tab) {
    Pair<Tab, Pane> found = null;

    if (tab == null) {
      logger.e(LOG_TAG, "Cannot find pair for a null tab!");
      return found;
    }

    for (Pair<Tab, Pane> pair : getPaneTabs()) {
      if (pair.first.equals(tab)) {
        found = pair;
        break;
      }
    }
    return found;
  }

  public Pair<Tab, Pane> getPair(Pane pane) {
    Pair<Tab, Pane> found = null;

    if (pane == null) {
      logger.e(LOG_TAG, "Cannot find pair for a null pane!");
      return found;
    }

    for (Pair<Tab, Pane> pair : getPaneTabs()) {
      if (pair.second.equals(pane)) {
        found = pair;
        break;
      }
    }
    return found;
  }

  /**
   * Adds a pane and tab to the editor
   *
   * @param pair The pane-tab pair to opened in the editor
   */
  private void addPaneTab(final Pair<Tab, Pane> pair) {
    final List<Pair<Tab, Pane>> panes = panesLiveData.getValue();
    Objects.requireNonNull(panes).add(new Pair<>(pair.first, pair.second));
    panesLiveData.setValue(panes);
  }

  /**
   * Gets the current Pane and Tab opened in the editor
   *
   * @return The opened Pane and Tab pair
   */
  public Pair<Tab, Pane> getPaneTab(final int position) {
    return Objects.requireNonNull(panesLiveData.getValue()).get(position);
  }

  /**
   * Gets the current size of all opened Pane and TabLayout.Tab
   *
   * @return The size of opened Pane Tabs Pair of the editor
   */
  public int getPaneTabSize() {
    return Objects.requireNonNull(panesLiveData.getValue().size());
  }

  /**
   * Gets a {@code List} of the opened Pane and TabLayout.Tab pair of the editor
   *
   * @return The {@code List} {@code Pair} of opened Pane and TabLayout.Tab
   */
  @NonNull
  public List<Pair<Tab, Pane>> getPaneTabs() {
    return panesLiveData.getValue() == null ? new ArrayList<>() : panesLiveData.getValue();
  }

  /**
   * Check if no Pane and TabLayout.Tab pair exist in the editor
   *
   * @return true if no Pane and TabLayout.Tab pair exist and false if there exists
   */
  public boolean isPaneTabsEmpty() {
    return panesLiveData.getValue() == null || panesLiveData.getValue().isEmpty();
  }

  public void persistPanes() {
    List<Pair<Tab, Pane>> paneTabs = panesLiveData.getValue();
    final List<Pane> paneList = new LinkedList<>();
    paneTabs.forEach(pair -> paneList.add(pair.second));

    savePanesAsync(paneList)
        .thenAccept(
            isSaved -> {
              AsyncTask.runOnUiThread(
                  () -> {
                    if (isSaved) {
                      logger.d(LOG_TAG, "Panes persisted");
                    } else {
                      logger.d(LOG_TAG, "Failed to persist panes properly");
                    }
                  });
            })
        .exceptionally(
            ex -> {
              logger.e(LOG_TAG, "Exception during panes persistence" + ex.getLocalizedMessage());
              return null;
            });
  }

  /**
   * Converts a list of pane to a JSON representation of a PaneState model
   *
   * <p>Panes must first create their view before invoking this function
   *
   * @param cues The list of pane cues to be converted
   * @return Returns true if the pane cues were successfully written to persistent storage.
   */
  private CompletableFuture<Boolean> savePanesAsync(List<Pane> cues) {
    return CompletableFuture.supplyAsync(
        () -> {
          if (cues == null || cues.isEmpty()) {
            return false;
          }

          var editor = sharedPreferences.edit();
          var treeMapList = new LinkedList<LinkedTreeMap<String, Object>>();

          for (Pane pane : cues) {
            var treeMap = new LinkedTreeMap<String, Object>();
            if (pane != null) {
              pane.persist();
              for (var entry : pane.getArguments().entrySet()) {
                treeMap.put(entry.getKey(), entry.getValue());
              }
              treeMapList.add(treeMap);
            }
          }

          String jsonString = new Gson().toJson(treeMapList);
          editor.putString(
              SharedPreferenceKeys.KEY_PERSISTED_PANES,
              EncodeUtils.base64Encode2String(jsonString.getBytes()));
          return editor.commit();
        });
  }

  public String getJson() {
    var bytes =
        EncodeUtils.base64Decode(
            sharedPreferences.getString(SharedPreferenceKeys.KEY_PERSISTED_PANES, ""));
    return new String(bytes);
  }

  private List<LinkedTreeMap<String, Object>> getPersistedPaneTree() {
    Gson gson = new Gson();
    var type = new TypeToken<List<LinkedTreeMap<String, Object>>>() {}.getType();
    var json = getJson();
    if (json != null) {
      List<LinkedTreeMap<String, Object>> linkedTreeMapList = gson.fromJson(json, type);
      if (linkedTreeMapList != null || linkedTreeMapList.isEmpty()) return linkedTreeMapList;
    }
    return new LinkedList<LinkedTreeMap<String, Object>>();
  }

  public boolean isPersisted(Pane pane) {
    var isPersisted = false;
    var linkedTreeMapList = getPersistedPaneTree();
    for (LinkedTreeMap<String, Object> treeMap : linkedTreeMapList) {
      if (UUID.fromString(treeMap.get("uuid").toString()).equals(pane.getUUID())) {
        isPersisted = true;
        break;
      }
    }
    return isPersisted;
  }

  private void removePaneTab(@NonNull Pair<TabLayout.Tab, Pane> pair) {
    Tab tab = pair.first;
    Pane pane = pair.second;

    final List<Pair<TabLayout.Tab, Pane>> panes = panesLiveData.getValue();
    Objects.requireNonNull(panes).remove(pair);
    panesLiveData.setValue(panes);

    if (pane == null) return;

    removePersistedPane(pane);
  }

  private void removePersistedPane(@NonNull Pane pane) {
    AsyncTask.runNonCancelable(
        () -> {
          if (isPersisted(pane)) {
            var linkedTreeMapList = getPersistedPaneTree();
            linkedTreeMapList.removeIf(
                map -> map.get("uuid").toString().equals(pane.getUUID().toString()));
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(
                SharedPreferenceKeys.KEY_PERSISTED_PANES,
                EncodeUtils.base64Encode2String(new Gson().toJson(linkedTreeMapList).getBytes()));
            return editor.commit();
          }
          return false;
        },
        (result, throwable) -> {
          if (result) {
            AsyncTask.runOnUiThread(
                () -> {
                  logger.i(
                      LOG_TAG,
                      "Successfully removed " + pane.getTitle() + " from persisted storage");
                });
          } else {
            logger.i(LOG_TAG, "Pane : " + pane + "was not persisted");
          }

          if (throwable != null) {
            Throwable cause = throwable.getCause();
            AsyncTask.runOnUiThread(
                () -> {
                  logger.e(
                      LOG_TAG,
                      "Failed to remove"
                          + pane
                          + cause.getMessage()
                          + " "
                          + throwable.getMessage());
                });
          }
        });
  }

  /**
   * Clears all persisted panes.
   *
   * <p>Usage: When closing all project invoke this
   */
  public void removePersistedPanes() {
    PreferencesUtils.clearPerference(sharedPreferences, SharedPreferenceKeys.KEY_PERSISTED_PANES);
  }

  public void restorePersistedPanes() {
    restorePersistedPanes(loadPanes());
  }

  private void restorePersistedPanes(List<Pane> paneList) {
    if (paneList != null || !paneList.isEmpty()) {
      for (Pane pane : paneList) {
        if (pane != null) {
          var textPane = getSelectedTextPane(pane);
          var editorPane = getSelectedEditorPane(pane);
          var webViewPane = getSelectedWebViewPane(pane);
          var settingsPane = getSelectedSettingsPane(pane);
          var codeEditorPane = getSelectedCodeEditorPane(pane);

          addPane(pane, false);

          // select the last selected persisted pane
          if (pane.isSelected()) {
            selectPaneInTabLayout(pane);
          }

          if (textPane != null) {
            var txt = textPane.getArguments().get("content").toString();
            textPane.setText(txt);
          } else if (editorPane != null) {
            var code = editorPane.getArguments().get("editor_text").toString();
            editorPane.setText(code);
          } else if (webViewPane != null) {
            boolean isDeskTopMode = (boolean) webViewPane.getArguments().get("isDeskTopMode");
            boolean isZoomable = (boolean) webViewPane.getArguments().get("isZoomAble");
            var path = webViewPane.getArguments().get("preview_file_path").toString();
            webViewPane.loadFile(new File(path));
            webViewPane.setZoomable(isZoomable);
            webViewPane.enableDeskTopMode(isDeskTopMode);
          } else if (codeEditorPane != null) {
            AsyncTask.runNonCancelable(
                () -> {
                  return codeEditorPane.getArguments().get("editor_content").toString();
                },
                (result) -> {
                  // null or empty persisted content default to the read editoe file
                  if (!Wizard.isEmpty(result)) {
                    codeEditorPane.getEditor().setText(result);
                    // mark persisted content as modified
                    codeEditorPane.setModified(true);

                    int left_column =
                        (int) (double) codeEditorPane.getArguments().get("left_column");
                    int left_line = (int) (double) codeEditorPane.getArguments().get("left_line");

                    Content text = codeEditorPane.getEditor().getText();
                    if (left_line < text.getLineCount()
                        && left_column < text.getColumnCount(left_line)) {
                      // set cursor position
                      codeEditorPane.getEditor().getCursor().set(left_line, left_column);
                    }
                  }
                });
          }
          updateTabs();
        }
      }
    }
  }

  /**
   * @return A list of loaded panes from persisted storage
   */
  public LinkedList<Pane> loadPanes() {
    Gson gson = new Gson();
    LinkedList<Pane> loadedPanes = new LinkedList<>();
    Type type = new TypeToken<List<LinkedTreeMap<String, Object>>>() {}.getType();

    String json = getJson();
    if (json != null) {
      List<LinkedTreeMap<String, Object>> linkedTreeMapList = gson.fromJson(json, type);

      if (linkedTreeMapList != null && !linkedTreeMapList.isEmpty()) {
        for (LinkedTreeMap<String, Object> treeMap : linkedTreeMapList) {
          String identifier = treeMap.get("uuid").toString();
          String jsonArguments = new Gson().toJson(treeMap);

          loadedPanes.add(createPane(new Rap(jsonArguments, identifier)));
        }
      }
      return loadedPanes;
    }
    return new LinkedList<Pane>();
  }

  /**
   * Creates a pane subclass from its pane state
   *
   * @param paneState The pane state to create a new {@code Pane} sub-class from
   * @return The created pane
   */
  private Pane createPane(Rap rap) {
    return createPane(requireContext(), rap);
  }

  public Pane createPane(Context context, Rap rap) {
    Pane pane = null;
    try {
      Gson gson = new Gson();
      Type type = new TypeToken<LinkedTreeMap<String, Object>>() {}.getType();
      LinkedTreeMap<String, Object> treeMap = gson.fromJson(rap.ARGUMENTS, type);

      String title = treeMap.get("title").toString();
      String clazz = treeMap.get("class_name").toString();
      boolean pinned = (boolean) treeMap.get("pinned");
      boolean selected = (boolean) treeMap.get("selected");
      String identity = treeMap.get("uuid").toString();
      UUID uuid = UUID.fromString(identity);

      if (clazz.equals(TextPane.class.getSimpleName())) {
        TextPane textPane = new TextPane(context, title, false);
        textPane.addArguments("content", treeMap.get("content").toString());
        pane = textPane;
      } else if (clazz.equals(EditorPane.class.getSimpleName())) {
        EditorPane editorPane = new EditorPane(context, title, false);
        editorPane.addArguments("editor_text", treeMap.get("editor_text").toString());
        pane = editorPane;
      } else if (clazz.equals(SettingsPane.class.getSimpleName())) {
        SettingsPane sp = new SettingsPane(context, title, false);
        // invoke before Pane#createView()
        sp.attach(getViewLifecycleOwner());
        sp.setFragment(PreferencesFragment.newInstance());
        pane = sp;
      } else if (clazz.equals(WelcomePane.class.getSimpleName())) {
        pane = new WelcomePane(context, title, false);
      } else if (clazz.equals(WebViewPane.class.getSimpleName())) {
        WebViewPane webViewPane = new WebViewPane(context, title, false);
        webViewPane.addArguments("preview_file_path", treeMap.get("preview_file_path").toString());
        webViewPane.addArguments("isZoomAble", (boolean) treeMap.get("isZoomAble"));
        webViewPane.addArguments("isDeskTopMode", (boolean) treeMap.get("isDeskTopMode"));
        pane = webViewPane;
      } else if (clazz.equals(CodeEditorPane.class.getSimpleName())) {
        CodeEditorPane codeEditorPane = new CodeEditorPane(context, title, false);
        codeEditorPane.setFile(new File(treeMap.get("file_path").toString()));
        codeEditorPane.addArguments("left_column", treeMap.get("left_column"));
        codeEditorPane.addArguments("left_line", treeMap.get("left_line"));
        codeEditorPane.addArguments("editor_content", treeMap.get("editor_content").toString());
        pane = codeEditorPane;
      } else if (clazz.equals(FragmentPane.class.getSimpleName())) {
        // last use case
        pane = new FragmentPane(context, title, false);
      }
      pane.setPinned(pinned);
      pane.setSelected(selected);
      pane.setUUID(uuid);
      return pane;
    } catch (RuntimeException e) {
      ToastUtils.showLong(e.getLocalizedMessage());
    }
    return null;
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onProjectChangeEvent(ProjectEvent event) {
    if (event.getFile() == null) {
      if (PreferencesUtils.canCloseUnPinnedProjectPanes()) {
        forceCloseAll(true);
      } else {
        forceCloseAll(false);
      }
    }
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onEditorModificationEvent(EditorModificationEvent event) {
    updateTabs(); // update all tabs
  }

  private void restoreViewState(@NonNull Bundle state) {
    int behaviorState = state.getInt("bottom_sheet_state", BottomSheetBehavior.STATE_COLLAPSED);
    mMainViewModel.setBottomSheetState(behaviorState);
    Bundle floatOffset = new Bundle();
    floatOffset.putFloat("offset", behaviorState == BottomSheetBehavior.STATE_EXPANDED ? 1 : 0f);
    getChildFragmentManager().setFragmentResult(BuildActionFragment.OFFSET_KEY, floatOffset);
  }

  /**
   * Opens a file asynchronously. public openable from main activity
   *
   * @param file The file to be opened.
   */
  public void openFileInPane(@NonNull File file) {
    if (Constants.isPreviewAble(file)) {
      addWebViewPane(file);
    } else {
      addCodeEditorPane(file);
    }
  }

  private void removeCodeEditorPaneFromHistory(CodeEditorPane currentEditor) {
    if (currentEditor != null) {
      addedPanes.removeIf(any -> currentEditor.getFilePath().equals(any.getFilePath()));
    }
  }

  /**
   * Hide the bottom sheet for logs
   *
   * @param enabled True if the sheets needs to be hidden
   */
  private void hideActionSheet(boolean enabled) {
    if (enabled) {
      binding.actionsSheet.setVisibility(View.GONE);
    } else {
      binding.actionsSheet.setVisibility(View.VISIBLE);
    }
  }

  /**
   * Opens a preview able file in a webview panes Adds the webview pane into the pane TES
   *
   * @param file The preview able file
   */
  public WebViewPane addWebViewPane(File file) {
    return addWebViewPane(file, false, true);
  }

  public WebViewPane addWebViewPane(File file, boolean isDeskTopMode, boolean isZoomable) {
    WebViewPane webViewPane = getPane(WebViewPane.class);
    if (webViewPane == null) {
      var path = file.getAbsolutePath();
      webViewPane =
          new WebViewPane(
              requireContext(), getString(R.string.webview_pane_title) + " | " + file.getName());
      addPane(webViewPane, false);
    }
    webViewPane.loadFile(file);
    webViewPane.setZoomable(isZoomable);
    webViewPane.enableDeskTopMode(isDeskTopMode);
    selectPaneInTabLayout(webViewPane);
    return webViewPane;
  }
  
  public CodeEditorPane addCodeEditorPane(@NonNull File file) {
    var tabName = file.getName();
    CodeEditorPane codeEditor = null;
    // Check if the code editor pane has already been added
    for (CodeEditorPane editorPane : addedPanes) {
      if (editorPane.getFile().equals(file)) {
        codeEditor = editorPane;
        break;
      }
    }
    // The associated code editor pane is not found, create a new one.
    if (codeEditor == null) {
      codeEditor = new CodeEditorPane(requireContext(), tabName);
      codeEditor.setFile(file);
      addPane(codeEditor, select);
      addedPanes.add(codeEditor); // Track the added editor pane
    } else {
      // Select pane tab since file is already opened in code editor pane
      selectPaneInTabLayout(codeEditor);
    }
    updateTabs();
    return codeEditor;
  }

  public SettingsPane addSettingsPane() {
    return addSettingsPane(true);
  }

  public SettingsPane addSettingsPane(boolean select) {
    SettingsPane sp = getPane(SettingsPane.class);
    if (sp == null) {
      sp = new SettingsPane(getContext(), getString(R.string.settings));
      sp.attach(getViewLifecycleOwner());
      sp.setFragment(PreferencesFragment.newInstance());
      addPane(sp, false);
    }
    if (select) {
      selectPaneInTabLayout(sp);
    }
    return sp;
  }

  public WelcomePane addWelcomePane(boolean setPinned) {
    WelcomePane welcomePane = getPane(WelcomePane.class);
    if (welcomePane == null) {
      welcomePane = new WelcomePane(requireContext(), getString(R.string.welcome));
      welcomePane.setPinned(setPinned);
      if (PreferencesUtils.canShowWelcomePanel()) {
        addPane(welcomePane, true);
      }
    }
    return welcomePane;
  }

  /**
   * Flip between the empty pane view and the view flipper for other added pane
   *
   * @param isEmpty True if no panes are added
   */
  private void showEmptyPaneWindow(boolean isEmpty) {
    binding.viewFlipper.setDisplayedChild(isEmpty ? 1 : 0);
  }

  private void createEmptyPaneWindow() {
    var windowPane = new EmptyPaneWindow(mMainViewModel, this, getContext(), "Empty Pane");
    binding.emptyViewContainer.addView(windowPane.createView());
  }

  /**
   * @return A live-data list of opened pane and it's associated tab
   */
  public LiveData<List<Pair<Tab, Pane>>> getOpenedPaneTabs() {
    return this.panesLiveData;
  }

  private void configureObservers() {
    // Observe changes for the build action panel
    mMainViewModel
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

    mMainViewModel.observeEditorFileOpening(
        getViewLifecycleOwner(),
        file -> {
          if (file != null && file.exists()) {
            var fileName = file.getName();
            // Sanity checks
            if (fileName.endsWith(".apk")) {
              // TODO: Install apk
            } else if (fileName.endsWith(".zip")) {
              // TODO: Request zip import into folder afterwards open it and close drawer
            } else if (fileName.endsWith(".pdf")) {
              // TODO: With plugin support open file in pdf plugin
            } else {
              openFileInPane(file);
            }
          }
        });
    performUIUpdate();
    mMainViewModel
        .getWebViewPaneFile()
        .observe(
            getViewLifecycleOwner(),
            file -> {
              if (file != null) addWebViewPane(file);
            });
    mMainViewModel
        .getBottomSheetExpanded()
        .observe(
            getViewLifecycleOwner(),
            expanded -> {
              if (expanded) {
                mBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
              } else {
                mBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
              }
            });
    mMainViewModel
        .addSettingsPane()
        .observe(
            getViewLifecycleOwner(),
            canAdd -> {
              if (canAdd) {
                addSettingsPane();
              }
            });
  }

  /**
   * @return FragmentBaseBinding
   */
  public FragmentBaseBinding getBinding() {
    return this.binding;
  }

  /**
   * Get the selected code editor pane
   *
   * @param pane The selected pane
   * @return The selected code editor pane
   */
  protected CodeEditorPane getSelectedCodeEditorPane(Pane pane) {
    return (pane instanceof CodeEditorPane) ? (CodeEditorPane) pane : null;
  }

  /**
   * Get the selected web view pane
   *
   * @param pane The selected pane
   * @return The selected web view pane
   */
  protected WebViewPane getSelectedWebViewPane(Pane pane) {
    return (pane instanceof WebViewPane) ? (WebViewPane) pane : null;
  }

  /**
   * Get the selected welcome pane
   *
   * @param pane The selected pane
   * @return The selected welcome pane
   */
  protected WelcomePane getSelectedWelcomePane(Pane pane) {
    return (pane instanceof WelcomePane) ? (WelcomePane) pane : null;
  }

  /**
   * Get the selected settings pane
   *
   * @param pane The selected pane
   * @return The selected settings pane
   */
  protected SettingsPane getSelectedSettingsPane(Pane pane) {
    return (pane instanceof SettingsPane) ? (SettingsPane) pane : null;
  }

  /**
   * Get the selected text pane
   *
   * @param pane The selected pane
   * @return The selected text pane
   */
  protected TextPane getSelectedTextPane(Pane pane) {
    return (pane instanceof TextPane) ? (TextPane) pane : null;
  }

  /**
   * Get the selected editor pane
   *
   * @param pane The selected pane
   * @return The selected editor pane
   */
  protected EditorPane getSelectedEditorPane(Pane pane) {
    return (pane instanceof EditorPane) ? (EditorPane) pane : null;
  }

  public <T> T getPane(Class<T> paneClass) {
    var paneTab = getPaneTabs();
    if (paneTab != null) {
      for (Pair<Tab, Pane> pair : paneTab) {
        if (pair.second.getClass().getName().equals(paneClass.getName())) {
          return paneClass.cast(pair.second);
        }
      }
    }
    return null;
  }

  public boolean containsPane(Class<?> pane) {
    var paneTab = getPaneTabs();
    if (paneTab != null) {
      for (Pair<Tab, Pane> temp : paneTab) {
        if (temp.second.getClass().getName().equals(pane.getName())) {
          return true;
        }
      }
    }
    return false;
  }

  public boolean containsPane(Pane pane) {
    var paneTab = getPaneTabs();
    if (paneTab != null) {
      for (Pair<Tab, Pane> temp : paneTab) {
        if (temp.second.equals(pane)) {
          return true;
        }
      }
    }
    return false;
  }

  private void invalidateMainMenus() {
    // In the child fragment
    MainFragment mainFragment =
        (MainFragment)
            requireActivity().getSupportFragmentManager().findFragmentByTag(MainFragment.TAG);
    if (mainFragment != null) {
      mainFragment.invalidateMenu();
    }
  }
}
