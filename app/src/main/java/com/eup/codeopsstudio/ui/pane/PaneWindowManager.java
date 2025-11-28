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

package com.eup.codeopsstudio.ui.pane;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewFlipper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.PopupMenu.OnMenuItemClickListener;
import androidx.appcompat.widget.TooltipCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import com.eup.codeopsstudio.R;
import com.eup.codeopsstudio.common.Constants;
import com.eup.codeopsstudio.common.ILog;
import com.eup.codeopsstudio.common.models.BooleanResult;
import com.eup.codeopsstudio.common.util.EncodeUtils;
import com.eup.codeopsstudio.common.util.PreferencesUtils;
import com.eup.codeopsstudio.databinding.PaneWindowBinding;
import com.eup.codeopsstudio.pane.Pane;
import com.eup.codeopsstudio.pane.PaneFactory;
import com.eup.codeopsstudio.ui.pane.contract.PaneContracts;
import com.eup.codeopsstudio.ui.pane.factory.PaneFactoryImpl;
import com.google.android.material.tabs.TabLayout;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * A concrete implementation of the {@link PaneWindow} interface that manages the lifecycle,
 * display, and interaction of {@link Pane} instances within a tab-based UI.
 *
 * <p>This class orchestrates the synchronization between a {@link TabLayout} and a {@link
 * ViewFlipper} content container. It is responsible for creating, adding, removing, and selecting
 * panes, as well as handling user interactions such as tab clicks, long-press context menus, and
 * persisting the state of open panes.
 *
 * <p>It provides robust handling for both default and custom tab layouts through the {@link
 * PaneWindowManager.CustomTabUIState} interface, ensuring that event listeners are correctly wired
 * for complex UI. State management is carefully controlled to prevent race conditions during bulk
 * operations like removing multiple tabs.
 *
 * @author Etido Peter
 * @see PaneWindow
 * @see Pane
 * @see TabLayout
 */
public class PaneWindowManager implements PaneWindow {

  public static final String TAG = "PaneWindowManager";

  private Context context;
  private Pane selectedPane;
  private TabLayout tabLayout;
  private PopupMenu mPopupMenu;
  private int selectedTabPosition;
  private ViewFlipper viewFlipper;
  private ViewFlipper paneContainer;
  private boolean showTabIcons = true;
  private final PaneFactory paneFactory;
  private FrameLayout emptyPaneContainer;
  private final EventWatcher eventWatcher;
  private CustomTabUIState customTabUIState;
  private boolean closeTabsRelativeToFirst = true;
  private final SharedPreferences sharedPreferences;
  private final List<Pane> panes = new ArrayList<>();

  public PaneWindowManager(
      @NonNull Context context,
      @NonNull PaneWindowBinding binding,
      @NonNull Fragment fragment,
      @NonNull EventWatcher watcher,
      @Nullable CustomTabUIState tabUIState) {
    Objects.requireNonNull(context, "Context cannot be null");
    Objects.requireNonNull(binding, "PaneWindowBinding cannot be null");
    Objects.requireNonNull(fragment, "Fragment cannot be null");
    Objects.requireNonNull(watcher, "PaneWindowWatcher cannot be " + "null");

    this.context = context;
    this.eventWatcher = watcher;
    this.tabLayout = binding.tablayout;
    this.emptyPaneContainer = binding.emptyViewContainer;
    this.viewFlipper = binding.viewFlipper;
    this.paneContainer = binding.paneContainer;
    this.selectedPane = null;
    this.selectedTabPosition = -1;
    this.customTabUIState = tabUIState;
    this.sharedPreferences = PreferencesUtils.getGlobalPreferences();
    this.tabLayout.addOnTabSelectedListener(this);
    this.paneFactory = new PaneFactoryImpl(fragment, this);
  }

  public PaneWindowManager(
      @NonNull Context context,
      @NonNull PaneWindowBinding binding,
      @NonNull FragmentActivity activity,
      @NonNull EventWatcher watcher,
      @Nullable CustomTabUIState tabUIState) {
    Objects.requireNonNull(context, "Context cannot be null");
    Objects.requireNonNull(binding, "PaneWindowBinding cannot be null");
    Objects.requireNonNull(activity, "Activity cannot be null");
    Objects.requireNonNull(watcher, "PaneWindowWatcher cannot be " + "null");

    this.context = context;
    this.eventWatcher = watcher;
    this.tabLayout = binding.tablayout;
    this.emptyPaneContainer = binding.emptyViewContainer;
    this.viewFlipper = binding.viewFlipper;
    this.paneContainer = binding.paneContainer;
    this.selectedPane = null;
    this.selectedTabPosition = -1;
    this.customTabUIState = tabUIState;
    this.sharedPreferences = PreferencesUtils.getGlobalPreferences();
    this.tabLayout.addOnTabSelectedListener(this);
    this.paneFactory = new PaneFactoryImpl(activity, this);
  }

  @Override
  public void onTabSelected(@NonNull TabLayout.Tab tab) {
    final int position = tab.getPosition();
    Pane pane = panes.get(position);
    if (pane != null) {
      paneContainer.setDisplayedChild(position);
      pane.onSelected();
      selectedPane = pane;
      selectedTabPosition = position;
      syncTabs();

      // Critical: Event notification must be deferred until TabLayout completes its
      // selection lifecycle. Without this synchronization, UI state becomes inconsistent
      // leading to unpredictable behavior in dependent components.
      //
      // Resolves: https://github.com/euptron/CodeOps-Studio/issues/12
      tabLayout.post(
          () -> {
            eventWatcher.onTabSelected(tab, selectedPane);
          });
    }
    invalidateMenuIfPossible();
  }

  @Override
  public void onTabUnselected(TabLayout.Tab tab) {
    Pane pane = panes.get(tab.getPosition());
    if (pane != null) {
      pane.onUnselected();
      syncTabs();
    }
  }

  @Override
  public void onTabReselected(@NonNull TabLayout.Tab tab) {
    Pane pane = panes.get(tab.getPosition());
    if (pane != null) {
      pane.onReselected();

      View anchor =
          (customTabUIState != null && tab.getCustomView() != null)
              ? tab.getCustomView()
              : tab.view;

      if (anchor == null) {
        ILog.warning(TAG, "Cannot show popup menu, anchor view is null.");
        return;
      }

      showPopupMenu(
          anchor,
          pane,
          menuItem -> {
            boolean handled = false;
            final int id = menuItem.getItemId();

            if (id == R.id.menu_close) {
              handled = close(pane);
            } else if (id == R.id.menu_close_others) {
              handled = closeOthers(pane);
            } else if (id == R.id.menu_close_all) {
              handled = closeAll();
            } else if (id == R.id.menu_close_right) {
              handled = closeToRightOf(pane, closeTabsRelativeToFirst);
            } else if (id == R.id.menu_close_left) {
              handled = closeToLeftOf(pane, closeTabsRelativeToFirst);
            } else if (id == R.id.menu_pin) {
              updatePinState(pane, !pane.isPinned());
              handled = true;
            }

            if (handled) syncTabs();

            validateState();
            return handled;
          });
    }
  }

  @Override
  public boolean add(@NonNull Pane pane) {
    return add(pane, panes.size(), panes.isEmpty());
  }

  @Override
  public boolean add(@NonNull Pane pane, int index) {
    return add(pane, index, panes.isEmpty());
  }

  @Override
  public boolean add(@NonNull Pane pane, boolean select) {
    return add(pane, panes.size(), select);
  }

  @Override
  public boolean add(@NonNull Pane pane, int index, boolean select) {
    if (index < 0 || index > panes.size()) {
      throw new IndexOutOfBoundsException(
          "Invalid Add Index: " + index + ", Size: " + panes.size());
    }

    if (contains(pane)) {
      selectTab(pane);
      ILog.debug(TAG, "Already added pane: " + pane.getTID());
      return false;
    }

    panes.add(index, pane);
    if (!pane.hasPerformedCreateView()) pane.createView();
    paneContainer.addView(pane.getView(), index);
    TabLayout.Tab newTab = createTab(pane);
    tabLayout.addTab(newTab, index, false);

    // Adjust selection if a tab was inserted before the current selection
    if (selectedTabPosition != -1 && index <= selectedTabPosition) {
      selectedTabPosition++;
    }

    final Pane paneToSelect;
    if (select) {
      paneToSelect = pane;
    } else if (panes.size() == 1) {
      paneToSelect = pane;
    } else {
      paneToSelect = null;
    }

    if (paneToSelect != null) {
      selectTab(paneToSelect);
    } else {
      updateTabUI(pane);
    }

    displayEmptyPaneIfRequired();
    return true;
  }

  @Override
  public boolean addAll(@NonNull Collection<? extends Pane> collection) {
    boolean changed = false;
    int startIndex = panes.size();
    int addedCount = 0;

    tabLayout.removeOnTabSelectedListener(this); // Optimize: remove listener once

    for (Pane pane : collection) {
      if (!contains(pane)) {
        int index = panes.size(); // Always add to the end in this loop
        panes.add(pane);
        if (!pane.hasPerformedCreateView()) pane.createView();
        paneContainer.addView(pane.getView(), index);
        TabLayout.Tab newTab = createTab(pane);
        tabLayout.addTab(newTab, index, false);
        changed = true;
        addedCount++;
      }
    }

    tabLayout.addOnTabSelectedListener(this); // Optmize: re-add listener

    if (changed) {
      // Select the first added tab if no tab was previously selected or if list was empty
      if (selectedTabPosition == -1 && addedCount > 0) {
        final int firstAddedIndex = startIndex;
        tabLayout.post(() -> selectTab(firstAddedIndex));
      } else {
        syncTabs();
        validateState();
      }
    }

    displayEmptyPaneIfRequired();
    // Return true if all items in the collection were actually added (didn't exist before)
    return addedCount == collection.size();
  }

  @Override
  public boolean canCloseTabsRelativeToFirst() {
    return closeTabsRelativeToFirst;
  }

  @Override
  public boolean close(@NonNull Pane pane) {
    return remove(pane);
  }

  @Override
  public boolean closeAll() {
    return closeAll(true);
  }

  @Override
  public boolean closeAll(boolean closeOnlyUnpinned) {
    return closeAll(closeOnlyUnpinned, null);
  }

  @Override
  public boolean closeAll(boolean closeOnlyUnpinned, BooleanResult<Pane> condition) {
    List<Pane> panesToRemove = new ArrayList<>();
    for (Pane pane : panes) {
      if (closeOnlyUnpinned) {
        if (!pane.isPinned()) {
          panesToRemove.add(pane);
        }
      } else {
        if (condition == null || condition.process(pane)) {
          panesToRemove.add(pane);
        }
      }
    }

    if (panesToRemove.isEmpty()) return false;
    return removeAll(panesToRemove);
  }

  @Override
  public boolean closeOthers(@NonNull Pane paneToKeep) {
    if (!panes.contains(paneToKeep)) return false; // Cannot keep a pane that doesn't exist

    List<Pane> panesToRemove = new ArrayList<>();
    for (Pane pane : panes) {
      if (!pane.equals(paneToKeep) && !pane.isPinned()) {
        panesToRemove.add(pane);
      }
    }

    if (panesToRemove.isEmpty()) return false;

    return removeAll(panesToRemove);
  }

  @Override
  public void closeTabsRelativeToFirst(boolean close) {
    this.closeTabsRelativeToFirst = close;
  }

  @Override
  public boolean closeToLeftOf(@NonNull Pane pane, boolean firstOnly) {
    int endIndex = getPaneIndex(pane);
    if (endIndex <= 0) return false; // Not found or is the first pane

    int startIndex = firstOnly ? endIndex - 1 : 0;
    removeRange(startIndex, endIndex);

    return true;
  }

  @Override
  public boolean closeToRightOf(@NonNull Pane pane, boolean firstOnly) {
    int startIndex = getPaneIndex(pane);
    if (startIndex < 0 || startIndex >= panes.size() - 1) {
      return false; // Not found or is the last pane
    }

    int rangeStart = startIndex + 1;
    int rangeEnd = firstOnly ? Math.min(rangeStart + 1, panes.size()) : panes.size();

    removeRange(rangeStart, rangeEnd);
    return true;
  }

  @Override
  public boolean contains(@NonNull Pane pane) {
    return panes.contains(Objects.requireNonNull(pane));
  }

  @Override
  public void addEmptyPaneWindow(@NonNull View view) {
    emptyPaneContainer.addView(view);
  }

  @Override
  public void destroy() {
    ILog.info(TAG, "Destroying PaneWindowManager and releasing all resources.");

    if (tabLayout != null) {
      tabLayout.removeOnTabSelectedListener(this);
    }

    for (Pane pane : new ArrayList<>(panes)) {
      if (pane != null) {
        pane.destroy();
      }
    }

    panes.clear();

    if (paneContainer != null) {
      paneContainer.removeAllViews();
    }

    if (emptyPaneContainer != null) {
      emptyPaneContainer.removeAllViews();
    }

    if (mPopupMenu != null) {
      mPopupMenu.setOnDismissListener(null);
    }

    this.mPopupMenu = null;
    this.context = null;
    this.tabLayout = null;
    this.paneContainer = null;
    this.viewFlipper = null;
    this.emptyPaneContainer = null;
    this.selectedPane = null;
  }

  @Override
  @NonNull
  public List<Pane> getPanes() {
    return Collections.unmodifiableList(panes);
  }

  @NonNull
  @Override
  public List<String> getOpenedPanesClassNames() {
    Set<String> classNames = new HashSet<>();
    for (Pane pane : panes) {
      classNames.add(pane.getClass().getName());
    }
    return new ArrayList<>(classNames);
  }

  @Override
  public void persistPanes() {
    persistPanes((Class<? extends Pane>[]) null);
  }

  @Override
  public void persistPanes(Class<? extends Pane>... paneTypesToFilter) {
    List<Pane> filteredPanes =
        getPanes().stream()
            .filter(
                p -> {
                  if (paneTypesToFilter == null) return true;
                  for (Class<? extends Pane> type : paneTypesToFilter) {
                    if (type.isInstance(p)) return false;
                  }
                  return true;
                })
            .collect(Collectors.toList());

    new PaneContracts.PersistPanes(sharedPreferences)
        .publish(
            filteredPanes,
            isSaved -> {
              if (Boolean.TRUE.equals(isSaved)) {
                ILog.info(TAG, " Successfully persisted panes");
              } else {
                ILog.info(TAG, "Failed to persist panes");
              }
            });
  }

  @Override
  public Pane remove(int index) {
    if (index < 0 || index >= panes.size()) {
      ILog.warning(TAG, "Invalid remove index: " + index + ", Size: " + panes.size());
      return null;
    }
    Pane pane = panes.get(index);
    return remove(pane, index) ? pane : null;
  }

  @Override
  public boolean remove(@NonNull Pane pane) {
    int index = getPaneIndex(pane);
    if (index == -1) return false;
    return remove(pane, index);
  }

  @Override
  public boolean remove(@NonNull Pane paneToRemove, int index) {
    try {
      if (index < 0 || index >= panes.size()) {
        ILog.warning(
            TAG, "Invalid remove index inside performRemove: " + index + ", Size: " + panes.size());
        return false;
      }

      if (!Objects.equals(paneToRemove, panes.get(index))) return false;
      if (paneToRemove.isPinned()) return false;
      removePersistedPane(paneToRemove);
      panes.remove(index);
      paneContainer.removeViewAt(index);
      tabLayout.removeTabAt(index);
      paneToRemove.destroy();
      
      if (panes.size() <= 0) {
        // No tabs left
        selectedPane = null;
        selectedTabPosition = -1;
        tabLayout.post(
            () -> {
              eventWatcher.onTabSelected(null, null);
            });
      }
      return true;
    } catch (Exception e) {
      ILog.error(TAG, "Failed to remove tab at index " + index + ": " + e.getMessage(), e);
      return false;
    } finally {
      validateState();
      displayEmptyPaneIfRequired();
      invalidateMenuIfPossible();
    }
  }

  @Override
  public boolean removeAll(@NonNull List<Pane> paneList) {
    List<Pane> panesToRemove = new ArrayList<>();
    for (Pane pane : paneList) {
      if (!pane.isPinned() && panes.contains(pane)) {
        panesToRemove.add(pane);
      }
    }
    if (panesToRemove.isEmpty()) return false;
    return batchRemove(panesToRemove, false, 0, panes.size());
  }

  @Override
  public void removePersistedPane(@NonNull Pane pane) {
    new PaneContracts.RemovePersistedPane(sharedPreferences)
        .publish(pane, result -> ILog.info(TAG, result));
  }
    
  @Override
  public void removePersistedPanes(@NonNull List<Pane> panes) {
    new PaneContracts.RemovePersistedPanes(sharedPreferences)
        .publish(panes, result -> ILog.info(TAG, result));
  }

  @Override
  public void removeRange(int fromIndex, int toIndex) {
    if (fromIndex < 0 || toIndex > panes.size() || fromIndex >= toIndex) {
      ILog.warning(
          TAG, "Invalid removeRange: " + fromIndex + " to " + toIndex + ", Size: " + panes.size());
      return;
    }

    List<Pane> panesToRemove = new ArrayList<>();
    // backwards iteration is safer since indices shift doesn't affect ops
    for (int i = Math.min(toIndex, panes.size() - 1); i >= fromIndex; i--) {
      Pane p = panes.get(i);
      if (!p.isPinned()) {
        panesToRemove.add(p);
      }
    }

    if (!panesToRemove.isEmpty()) {
      batchRemove(panesToRemove, /* retain= */ false, fromIndex, toIndex);
    }
  }

  @Override
  public void restorePanes(@NonNull Consumer<List<Pane>> onPanesLoaded) {
    new PaneContracts.LoadPersistedPanes(paneFactory)
        .publish(getPersistedPaneJson(), onPanesLoaded);
  }

  @Override
  public boolean retainAll(@NonNull List<Pane> panesToRetain) {
    boolean potentiallyModified =
        panes.stream().anyMatch(p -> !panesToRetain.contains(p) && !p.isPinned());

    if (!potentiallyModified) {
      return false; // Nothing to remove in the entire list
    }

    return batchRemove(panesToRetain, true, 0, panes.size());
  }

  @Override
  public void selectTab(@NonNull Pane pane) {
    int existingIndex = getPaneIndex(pane);
    if (existingIndex != -1) selectTab(existingIndex);
  }

  @Override
  public void selectTab(int index) {
    if (index < 0 || index >= tabLayout.getTabCount() || index >= panes.size()) {
      ILog.warning(
          TAG,
          "Cannot select invalid index: "
              + index
              + ", TabCount: "
              + tabLayout.getTabCount()
              + ", PaneCount: "
              + panes.size());

      // Try selecting the last valid tab if index is out of bounds?
      if (tabLayout.getTabCount() > 0) {
        index = tabLayout.getTabCount() - 1;
      } else {
        ILog.info(TAG, "No tabs left to select");
        selectedPane = null;
        selectedTabPosition = -1;
        syncTabs();
        displayEmptyPaneIfRequired();
        validateState();
        return;
      }
    }

    TabLayout.Tab tab = tabLayout.getTabAt(index);
    if (tab == null) {
      ILog.warning(TAG, "Cannot select null tab at index: " + index);
      return;
    }

    if (!tab.isSelected()) {
      tabLayout.selectTab(tab);
      ILog.warning(TAG, "attempting to select tab at : " + index);
    }

    // Scroll to the selected tab
    tabLayout.setScrollPosition(index, 0f, true);
  }

  @Override
  public void showPopupMenu(
      @NonNull View anchor, @NonNull Pane pane, @NonNull OnMenuItemClickListener listener) {
    // Dismiss any existing popup first to prevent overlaps
    if (mPopupMenu != null) {
      mPopupMenu.getMenu();
      try {
        mPopupMenu.dismiss();
      } catch (Exception e) {
        /* Ignore dismissal errors */
      }
      mPopupMenu = null;
    }

    mPopupMenu = new PopupMenu(context, anchor, Gravity.NO_GRAVITY);
    mPopupMenu.inflate(R.menu.pane_tab_menu);

    Menu menu = mPopupMenu.getMenu();
    MenuItem closeItem = menu.findItem(R.id.menu_close);
    MenuItem pinItem = menu.findItem(R.id.menu_pin);
    MenuItem closeOthersItem = menu.findItem(R.id.menu_close_others);
    MenuItem closeRightItem = menu.findItem(R.id.menu_close_right);
    MenuItem closeLeftItem = menu.findItem(R.id.menu_close_left);

    if (closeItem != null) {
      closeItem.setVisible(!pane.isPinned());
    }

    if (pinItem != null) {
      pinItem.setTitle(pane.isPinned() ? R.string.pane_unpin : R.string.pane_pin);
    }

    // Disable options that don't make sense based on tab count or position
    int currentSize = panes.size();
    int currentIndex = getPaneIndex(pane);
    boolean canCloseOthers = panes.stream().anyMatch(p -> !p.equals(pane) && !p.isPinned());
    boolean canCloseRight =
        currentIndex < currentSize - 1
            && panes.stream().skip(currentIndex + 1).anyMatch(p -> !p.isPinned());
    boolean canCloseLeft =
        currentIndex > 0 && panes.stream().limit(currentIndex).anyMatch(p -> !p.isPinned());

    if (closeOthersItem != null) closeOthersItem.setEnabled(canCloseOthers);
    if (closeRightItem != null) closeRightItem.setEnabled(canCloseRight);
    if (closeLeftItem != null) closeLeftItem.setEnabled(canCloseLeft);

    mPopupMenu.setOnMenuItemClickListener(listener);
    mPopupMenu.setOnDismissListener(menuPopup -> mPopupMenu = null);
    mPopupMenu.show();
  }

  @Override
  public void showTabIcons(boolean show) {
    this.showTabIcons = show;
    syncTabs();
  }

  @Override
  public void syncTab(@NonNull Pane pane) {
    updateTabUI(pane);
  }

  @Override
  public void syncTabs() {
    if (tabLayout.getTabCount() != panes.size()) {
      ILog.warning(
          TAG, "SyncTabs Mismatch: panes=" + panes.size() + ", tabs=" + tabLayout.getTabCount());
      return;
    }

    for (int i = 0; i < panes.size(); i++) {
      // ensure safe index bounds
      if (i < tabLayout.getTabCount()) {
        Pane pane = panes.get(i);
        TabLayout.Tab tab = tabLayout.getTabAt(i);
        if (tab != null) {
          updateSpecificTabUI(tab, pane);
        }
      } else {
        ILog.error(
            TAG,
            "SyncTabs Error: Pane index "
                + i
                + " out of bounds for Tab Count "
                + tabLayout.getTabCount());
      }
    }

    if (panes.isEmpty()) {
      displayEmptyPaneIfRequired();
    }

    invalidateMenuIfPossible();
  }

  @Override
  public void updatePinState(@NonNull Pane pane, boolean pinned) {
    if (contains(pane)) {
      pane.setPinned(pinned);
      syncTab(pane);
      invalidateMenuIfPossible();
    }
  }
  
  @Nullable
  @Override
  public Pane getSelectedPane() {
    return selectedPane;
  }
  
  private void updateSpecificTabUI(@NonNull TabLayout.Tab tab, @NonNull Pane pane) {
    String title = eventWatcher.requireTabTitle(pane);

    if (customTabUIState != null && tab.getCustomView() != null) {
      View customView = tab.getCustomView();
      TextView tabText = customTabUIState.getTabTitleTextView(customView);
      ImageView tabIcon = customTabUIState.getTabIconImageView(customView);
      ImageButton closeButton = customTabUIState.getTabCloseImageButton(customView);

      if (tabText != null) tabText.setText(title);

      if (tabIcon != null) {
        if (showTabIcons) {
          tabIcon.setVisibility(View.VISIBLE);
          try {
            tabIcon.setImageResource(customTabUIState.getTabIconResId(pane));
          } catch (Exception e) {
            ILog.error(TAG, "Failed to get/set tab icon resource: " + e.getMessage());
            tabIcon.setVisibility(View.GONE);
          }
        } else {
          tabIcon.setVisibility(View.GONE);
        }
      }

      if (closeButton != null) {
        closeButton.setVisibility(View.VISIBLE);

        if (pane.isPinned()) {
          closeButton.setImageResource(R.drawable.ic_pin_outline);
          TooltipCompat.setTooltipText(closeButton, context.getString(R.string.pane_unpin));
        } else {
          closeButton.setImageResource(R.drawable.ic_close);
          TooltipCompat.setTooltipText(closeButton, context.getString(R.string.pane_close));
        }
      }
    } else {
      // Default tab view
      tab.setText(title);
    }

    tabLayout.requestLayout();
  }
  
  public void displayEmptyPaneIfRequired() {
    final boolean isEmpty = getPanes().isEmpty();
    tabLayout.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    viewFlipper.setDisplayedChild(isEmpty ? 1 : 0);
  }

  public String getPersistedPaneJson() {
    var bytes =
        EncodeUtils.base64Decode(
            sharedPreferences.getString(Constants.SharedPreferenceKeys.KEY_PERSISTED_PANES, ""));
    return new String(bytes);
  }

  private void invalidateMenuIfPossible() {
    tabLayout.post(
        () -> {
          if (context instanceof FragmentActivity activity) {
            activity.invalidateMenu();
          }
        });
  }

  /**
   * Optimized internal batch removal operating within a specified range.
   *
   * @param panesToModify List of panes to either remove or keep (based on retain flag).
   * @param retain If true, panes *not* in panesToModify within the range are removed. If false,
   *     panes *in* panesToModify within the range are removed.
   * @param fromIndex The starting index (inclusive) of the range to consider.
   * @param toIndex The ending index (exclusive) of the range to consider.
   * @return true if the list was modified.
   */
  private boolean batchRemove(
      List<Pane> panesToModify, boolean retain, int fromIndex, int toIndex) {
    if (fromIndex < 0 || toIndex > panes.size() || fromIndex > toIndex) {
      ILog.error(
          TAG,
          "Invalid batch remove range: "
              + fromIndex
              + " to "
              + toIndex
              + ", Size: "
              + panes.size());
      return false;
    }

    boolean modified = false;

    try {
      List<Integer> indicesToRemove = new ArrayList<>();
      List<Pane> panesBeingRemoved = new ArrayList<>();

      for (int i = toIndex - 1; i >= fromIndex; i--) {
        if (i >= panes.size()) continue;

        Pane currentPane = panes.get(i);
        boolean shouldRemove;

        if (retain) {
          shouldRemove = !panesToModify.contains(currentPane) && !currentPane.isPinned();
        } else {
          // Ensure we don't try to remove pinned panes even if they are in the set
          shouldRemove = panesToModify.contains(currentPane) && !currentPane.isPinned();
        }

        if (shouldRemove) {
          indicesToRemove.add(i);
          panesBeingRemoved.add(currentPane);
          modified = true;
        }
      }

      if (!modified) {
        return false;
      }
            removePersistedPanes(panesBeingRemoved);

      // Sort indices in descending order to ensure correct removal from list/views
      // (Important because we collected them in descending order of checking,
      // but the actual removal needs highest index first)
      indicesToRemove.sort(Comparator.reverseOrder());

      for (int index : indicesToRemove) {
        if (index >= 0 && index < panes.size()) {
          panes.remove(index);

          if (index < paneContainer.getChildCount()) {
            paneContainer.removeViewAt(index);
          } else {
            ILog.warning(
                TAG,
                "Batch remove inconsistency: View index "
                    + index
                    + " invalid for container count "
                    + paneContainer.getChildCount());
          }

          if (index < tabLayout.getTabCount()) {
            tabLayout.removeTabAt(index);
          } else {
            ILog.warning(
                TAG,
                "Batch remove inconsistency: Tab index "
                    + index
                    + " invalid for tab count "
                    + tabLayout.getTabCount());
          }
        } else {
          ILog.warning(
              TAG,
              "Batch remove inconsistency: Invalid index "
                  + index
                  + " during removal phase. Size: "
                  + panes.size());
        }
      }
      
      for (Pane p : panesBeingRemoved) {
        p.destroy();
      }

      // Update selection
      int newSize = panes.size();
      int newSelectionIndex = -1;
      if (newSize > 0) {
        if (selectedTabPosition == -1 || !panes.contains(selectedPane)) {
          newSelectionIndex = 0;
        } else {
          newSelectionIndex = panes.indexOf(selectedPane);
          if (newSelectionIndex == -1) newSelectionIndex = 0;
        }
        selectedTabPosition = newSelectionIndex;
      } else {
        selectedPane = null;
        selectedTabPosition = -1;
        tabLayout.post(
            () -> {
              eventWatcher.onTabSelected(null, null);
            });
      }

      if (newSelectionIndex != -1) {
        final int finalSelectionIndex = newSelectionIndex;
        selectTab(finalSelectionIndex);
      } else {
        syncTabs();
      }
      return true;
    } catch (Exception e) {
      var msg =
          String.format("Batch remove failed in range [from=%s, to=%s]: ", fromIndex, toIndex);
      ILog.error(TAG, msg + e.getMessage(), e);
      return modified;
    } finally {
      displayEmptyPaneIfRequired();
      invalidateMenuIfPossible();
      validateState();
    }
  }

  private TabLayout.Tab createTab(Pane pane) {
    TabLayout.Tab tab = tabLayout.newTab();
    tab.setTag(pane);

    if (customTabUIState != null && tab.getCustomView() == null) {
      tab.setCustomView(customTabUIState.getTabLayoutResId());
      View customView = tab.getCustomView();

      if (customView != null) {
        ImageButton closeButton = customTabUIState.getTabCloseImageButton(customView);
        updateSpecificTabUI(tab, pane);

        if (closeButton != null) {
          closeButton.setOnClickListener(
              v -> {
                if (pane.isPinned()) {
                  updatePinState(pane, false);
                } else {
                  close(pane);
                }
              });
        }
      } else {
        ILog.info(TAG, "Custom tab view inflation failed for: " + pane.getTitle());
        // Fallback to default text tab
        tab.setText(eventWatcher.requireTabTitle(pane));
      }
    } else {
      // Default tab view
      tab.setText(eventWatcher.requireTabTitle(pane));
    }
    return tab;
  }

  private int getPaneIndex(Pane pane) {
    return panes.indexOf(pane);
  }

  private void updateTabUI(@NonNull Pane pane) {
    if (!contains(pane)) return;

    int index = getPaneIndex(pane);

    if (index < 0 || index >= tabLayout.getTabCount()) {
      ILog.verbose(
          TAG,
          "updateTabUI: Invalid index for pane: "
              + index
              + ", TabCount: "
              + tabLayout.getTabCount()
              + ". This might happen temporarily during removals");
      return;
    }

    TabLayout.Tab tab = tabLayout.getTabAt(index);
    if (tab != null) updateSpecificTabUI(tab, pane);
  }

  private void validateState() {
    int paneCount = panes.size();
    int tabCount = tabLayout.getTabCount();
    int viewCount = paneContainer.getChildCount();

    boolean mismatch = false;
    StringBuilder errorMsg = new StringBuilder("State inconsistency detected! ");

    if (tabCount != paneCount) {
      errorMsg.append("Tabs: ").append(tabCount).append(" != Panes: ").append(paneCount);
      mismatch = true;
    }
    if (viewCount != paneCount) {
      if (mismatch) errorMsg.append(" | ");
      errorMsg.append("Views: ").append(viewCount).append(" != Panes: ").append(paneCount);
      mismatch = true;
    }

    // Also check selected position validity
    if (selectedTabPosition != -1
        && (selectedTabPosition >= paneCount
            || selectedTabPosition >= tabCount
            || selectedTabPosition >= viewCount)) {
      if (mismatch) errorMsg.append(" | ");
      errorMsg.append("Selected Index: ").append(selectedTabPosition).append(" is out of bounds.");
      mismatch = true;
    }

    // Check selected pane consistency
    if (selectedTabPosition != -1
        && (selectedPane == null || getPaneIndex(selectedPane) != selectedTabPosition)) {
      if (mismatch) errorMsg.append(" | ");
      errorMsg
          .append("Selected Pane instance does not match selected index ")
          .append(selectedTabPosition);
      mismatch = true;
    }

    if (mismatch) {
      ILog.verbose(TAG, errorMsg.toString());
      // TODO: Add more detailed logging, like dumping pane list titles vs tab titles
    }
  }

  public interface EventWatcher {

    void onTabSelected(@Nullable TabLayout.Tab tab, @Nullable Pane pane);

    String requireTabTitle(@NonNull Pane pane);
  }

  public interface CustomTabUIState {
    ImageButton getTabCloseImageButton(@NonNull View view);

    ImageView getTabIconImageView(@NonNull View view);

    int getTabIconResId(@NonNull Pane pane);

    int getTabLayoutResId();

    TextView getTabTitleTextView(@NonNull View view);
  }
}
