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

package com.eup.codeopsstudio.ui.editor;

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
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.eup.codeopsstudio.R;
import com.eup.codeopsstudio.common.AsyncTask;
import com.eup.codeopsstudio.common.Constants;
import com.eup.codeopsstudio.common.ILog;
import com.eup.codeopsstudio.common.models.ProjectEvent;
import com.eup.codeopsstudio.common.util.EncodeUtils;
import com.eup.codeopsstudio.common.util.PreferencesUtils;
import com.eup.codeopsstudio.databinding.FragmentBaseBinding;
import com.eup.codeopsstudio.domain.events.CurrentPaneEvent;
import com.eup.codeopsstudio.domain.events.EditorModificationEvent;
import com.eup.codeopsstudio.listeners.OnPaneTabSelectedListener;
import com.eup.codeopsstudio.models.ExtensionTable;
import com.eup.codeopsstudio.models.logger.Logger;
import com.eup.codeopsstudio.pane.Pane;
import com.eup.codeopsstudio.ui.editor.code.CodeEditorPane;
import com.eup.codeopsstudio.ui.editor.panes.EmptyPaneWindow;
import com.eup.codeopsstudio.ui.editor.panes.WebViewPane;
import com.eup.codeopsstudio.ui.editor.panes.WelcomePane;
import com.eup.codeopsstudio.ui.settings.PreferencesFragment;
import com.eup.codeopsstudio.ui.settings.SettingsPane;
import com.eup.codeopsstudio.util.Wizard;
import com.eup.codeopsstudio.util.manager.FileManager;
import com.eup.codeopsstudio.util.pane.PaneUtil;
import com.eup.codeopsstudio.viewmodel.MainViewModel;
import com.eup.codeopsstudio.viewmodel.SavedStateViewModel;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayout.Tab;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import io.github.rosemoe.sora.text.Content;

/**
 * Base fragment: A class that holds a functional editor Including its action sheets TES: Tabbed
 * editor system
 *
 * @author Etido Peter
 */
public class BaseFragment extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener, TabLayout.OnTabSelectedListener, OnPaneTabSelectedListener {

    public static final String TAG = BaseFragment.class.getSimpleName();
    public static final String LOG_TAG = "BaseInterface";
    private final MutableLiveData<List<Pair<Tab, Pane>>> panesLiveData =
        new MutableLiveData<>(new LinkedList<>());
    private final MutableLiveData<Pair<Integer, Pair<Tab, Pane>>> currentPaneLiveData =
        new MutableLiveData<>(null);
    protected MainViewModel mMainViewModel;
    private final OnBackPressedCallback mOnBackPressedCallback = new OnBackPressedCallback(false) {
        @Override
        public void handleOnBackPressed() {
            mMainViewModel.setBottomSheetState(BottomSheetBehavior.STATE_COLLAPSED);
        }
    };
    protected boolean canAutoSave = false;
    /**
     * TODO: Sync tab icon visibility with preference change
     */
    private boolean displayTabIcons = PreferencesUtils.canDisplayTabIcons();
    private BottomSheetBehavior<View> mBehavior;
    private Logger logger;
    private FragmentBaseBinding binding;
    private PopupMenu mPopupMenu;
    private SharedPreferences sharedPreferences;
    private Observer<List<Pair<Tab, Pane>>> panesObserver;
    private List<Pair<Tab, Pane>> openedPaneTabs = new ArrayList<>();
    private SavedStateViewModel mSavedStateViewModel;
    private boolean closeTabsRelativeToFirst, closeUnPinnedProjectPanes;

    public static BaseFragment newInstance() {
        return new BaseFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        logger = new Logger(Logger.LogClass.IDE);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
        @Nullable Bundle savedInstanceState) {
        binding = FragmentBaseBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sharedPreferences    = PreferencesUtils.getGlobalPreferences();
        mMainViewModel       = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        mSavedStateViewModel =
            new ViewModelProvider(requireActivity()).get(SavedStateViewModel.class);
        logger.attach(requireActivity() /*shared activity scope*/);
        binding.tablayout.addOnTabSelectedListener(this);

        closeTabsRelativeToFirst  = PreferencesUtils.canCloseRelativeToFirstDepth();
        closeUnPinnedProjectPanes = PreferencesUtils.canCloseUnPinnedProjectPanes();

        mBehavior = BottomSheetBehavior.from(binding.actionsSheet);
        mBehavior.setGestureInsetBottomIgnored(true);
        mBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View p1, int state) {
                mMainViewModel.setBottomSheetState(state);
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                if (isAdded()) {
                    var bundle = new Bundle();
                    bundle.putFloat("offset", slideOffset);
                    getChildFragmentManager().setFragmentResult(BuildActionFragment.OFFSET_KEY,
                        bundle);
                }
            }
        });
        mBehavior.setHalfExpandedRatio(0.3f);
        mBehavior.setFitToContents(false);

        mSavedStateViewModel
            .getActionSheetState()
            .observe(getViewLifecycleOwner(), savedState -> {
                int sheet_behaviour =
                    (savedState != null) ? savedState : BottomSheetBehavior.STATE_COLLAPSED;
                restoreViewState(sheet_behaviour);
            });

        configureObservers();
        createEmptyPaneWindow();
        restorePersistedPanes();
        addWelcomePane(/* pinned= */ true);
        invalidateMainMenus();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!EventBus
            .getDefault()
            .isRegistered(this)) {
            EventBus
                .getDefault()
                .register(this);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        PreferencesUtils
            .getDefaultPreferences()
            .registerOnSharedPreferenceChangeListener(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mSavedStateViewModel.saveActionSheetState(mBehavior.getState());
    }

    @Override
    public void onPause() {
        super.onPause();
        persistPanes();
        PreferencesUtils
            .getDefaultPreferences()
            .unregisterOnSharedPreferenceChangeListener(this);
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (EventBus
            .getDefault()
            .isRegistered(this)) {
            EventBus
                .getDefault()
                .unregister(this);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // release resources initalized onCreateView and onViewCreated
        mMainViewModel
            .getBottomSheetExpanded()
            .removeObservers(getViewLifecycleOwner());
        mMainViewModel
            .getBottomSheetState()
            .removeObservers(getViewLifecycleOwner());
        mMainViewModel
            .addSettingsPane()
            .removeObservers(getViewLifecycleOwner());
        panesLiveData.removeObserver(panesObserver);
        this.binding = null;
    }

    @Override
    public void onDestroy() {
        PaneUtil.destroyPanes(PaneUtil.getPaneTabs(panesLiveData));
        super.onDestroy();
    }

    public void persistPanes() {
        List<Pair<Tab, Pane>> paneTabs = panesLiveData.getValue();
        if (paneTabs == null) {
            ILog.warning(TAG, "Could not persist panes, livedata is null");
            return;
        }

        final List<Pane> paneList = new LinkedList<>();
        paneTabs.forEach(pair -> paneList.add(pair.second));

        savePanesAsync(paneList)
            .thenAccept(isSaved -> requireActivity().runOnUiThread(() -> {
                if (!isSaved) {
                    logger.w(LOG_TAG, "Failed to persist panes properly");
                }
            }))
            .exceptionally(ex -> {
                requireActivity().runOnUiThread(() -> logger.e(LOG_TAG,
                    "Exception during panes persistence : " + ex.getLocalizedMessage()));
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
        return CompletableFuture.supplyAsync(() -> {
            if (cues == null || cues.isEmpty()) return false;

            var editor = sharedPreferences.edit();
            var treeMapList = new LinkedList<LinkedTreeMap<String, Object>>();

            for (Pane pane : cues) {
                var treeMap = new LinkedTreeMap<String, Object>();
                if (pane != null) {
                    pane.persist();
                    treeMap.putAll(pane.getArguments());
                    treeMapList.add(treeMap);
                }
            }

            String jsonString = new Gson().toJson(treeMapList);
            editor.putString(Constants.SharedPreferenceKeys.KEY_PERSISTED_PANES,
                EncodeUtils.base64Encode2String(jsonString.getBytes()));
            return editor.commit();
        });
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences pref, @Nullable String key) {
        if (key == null) return;

        switch (key) {
            case Constants.SharedPreferenceKeys.KEY_CODE_EDITOR_AUTO_SAVE:
                canAutoSave = PreferencesUtils.autoSaveFiles();
                break;
            case Constants.SharedPreferenceKeys.KEY_CODE_EDITOR_RELATIVE_CLOSE_DEPTH:
                closeTabsRelativeToFirst = PreferencesUtils.canCloseRelativeToFirstDepth();
                break;
            case Constants.SharedPreferenceKeys.KEY_CODE_EDITOR_CLOSE_UNPINNED_PROJECT_PANES:
                closeUnPinnedProjectPanes = PreferencesUtils.canCloseUnPinnedProjectPanes();
                break;
            case Constants.SharedPreferenceKeys.KEY_DISPLAY_TAB_ICONS:
                displayTabIcons = PreferencesUtils.canDisplayTabIcons();
                break;
        }
    }

    @Override
    public void onTabSelected(@NonNull Tab tab) {
        final int position = tab.getPosition();
        binding.paneContainer.setDisplayedChild(position);
        Pair<Tab, Pane> pair = PaneUtil.getPair(PaneUtil.getPaneTabs(panesLiveData), tab);

        if (pair == null || pair.second == null) {
            logger.e(LOG_TAG, "Selected tab has no associated pane!");
            return;
        }

        pair.second.onSelected(); // mark also for persistency
        // post current pane
        EventBus
            .getDefault()
            .post(new CurrentPaneEvent(position, pair.second));
        currentPaneLiveData.setValue(Pair.create(position, pair));

        updateTabs();
        invalidateMainMenus();
    }

    @Override
    public void onTabUnselected(Tab tab) {
        if (tab == null) {
            logger.e(LOG_TAG, "Cannot find pair for a null tab!");
            return;
        }
        Pair<Tab, Pane> pair = PaneUtil.getPair(PaneUtil.getPaneTabs(panesLiveData), tab);
        if (pair == null || pair.second == null) {
            logger.e(LOG_TAG, "Selected tab has no associated pane!");
            return;
        }
        pair.second.onUnselected();
        updateTabs();
    }

    @Override
    public void onTabReselected(@NonNull Tab tab) {
        int position = tab.getPosition();
        Pair<Tab, Pane> pair = PaneUtil.getPaneTab(panesLiveData, position);
        if (pair == null || pair.second == null) {
            logger.e(LOG_TAG, "Selected tab has no associated pane!");
            return;
        }
        pair.second.onReselected(); // mark for persistency
        Pane pane = pair.second;

        showPopupMenu(tab.getCustomView(), pane, menuItem -> {
            final int id = menuItem.getItemId();
            final var title = menuItem.getTitle();

            if (title.equals(getString(R.string.pane_close))) {
                close(tab);
            } else if (title.equals(getString(R.string.pane_close_others))) {
                closeOthers(tab);
            } else if (title.equals(getString(R.string.pane_close_all))) {
                closeAll();
            } else if (title.equals(getString(R.string.pane_close_right))) {
                closeToRightOf(tab, closeTabsRelativeToFirst);
            } else if (title.equals(getString(R.string.pane_close_left))) {
                closeToLeftOf(tab, closeTabsRelativeToFirst);
            } else if (title.equals(getString(R.string.pane_pin))
                || title.equals(getString(R.string.pane_unpin))) {
                pinPaneTab(tab, !pane.isPinned());
                menuItem.setTitle(pane.isPinned() ? R.string.pane_unpin : R.string.pane_pin);
            }
            invalidateMainMenus();
            return true;
        });
    }

    public void updateTabs() {
        updateTabs(false);
    }

    /**
     * Updates the pane tabs.
     *
     * @param single If true, updates the first pane tabs; if false, updates all pane tab.
     */
    public void updateTabs(boolean single) {
        for (int i = 0; i < PaneUtil.getPaneTabSize(panesLiveData); i++) {
            var pair = PaneUtil.getPaneTab(panesLiveData, i);
            var tab = pair.first;
            var pane = pair.second;

            if (tab != null && pane != null) {
                updateTab(tab, pane);
                if ((tab != null && tab.getCustomView() != null)) {
                    var editor = PaneUtil.requireCodeEditorPane(pane);
                    TextView tabText = tab
                        .getCustomView()
                        .findViewById(R.id.tab_text);
                    String title = null;

                    if (editor != null) {
                        File current = editor.getFile();
                        String name =
                            PaneUtil.getUniqueTabTitle(PaneUtil.getPaneTabs(panesLiveData),
                                current);
                        String text = current != null ? name : "INVALID-TAB";
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
            ImageButton actionButton = tab
                .getCustomView()
                .findViewById(R.id.pane_action_button);

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

    private void invalidateMainMenus() {
        mMainViewModel.setShouldUpdateMenu(true);
    }

    @Override
    public void close(Tab tab) {
        for (Pair<Tab, Pane> pair : PaneUtil.getPaneTabs(panesLiveData)) {
            if (pair.first == tab && !pair.second.isPinned()) {
                binding.tablayout.removeTab(tab);
                binding.paneContainer.removeView(pair.second.getView());
                pair.second.onUnselected();
                pair.second.destroy();
                removePaneTab(pair);
                break;
            }
        }
        binding.tablayout.requestLayout();
        invalidateMainMenus();
    }

    @Override
    public void closeOthers(Tab tabToKeep) {
        int max = PaneUtil.getPaneTabSize(panesLiveData);
        int keep = tabToKeep.getPosition();
        for (int i = max - 1; i >= 0; i--) {
            var pair = PaneUtil.getPaneTab(panesLiveData, i);
            if (i != keep && !pair.second.isPinned()) {
                binding.tablayout.removeTab(binding.tablayout.getTabAt(i));
                binding.paneContainer.removeView(binding.paneContainer.getChildAt(i));
                pair.second.destroy();
                removePaneTab(pair);
            }
        }
        binding.tablayout.requestLayout();
    }

    @Override
    public void closeAll() {
        var iterator = PaneUtil
            .getPaneTabs(panesLiveData)
            .iterator();
        while (iterator.hasNext()) {
            var pair = iterator.next();
            if (!pair.second.isPinned()) {
                iterator.remove();
                binding.tablayout.removeTab(pair.first);
                binding.paneContainer.removeView(pair.second.getView());
                pair.second.destroy();
                removePaneTab(pair);
            }
        }
        binding.tablayout.requestLayout();
    }

    @Override
    public void closeToRightOf(Tab tab, boolean first) {
        int selectedIndex = tab.getPosition();
        int maxPosition = PaneUtil.getPaneTabSize(panesLiveData);
        if (first) {
            if (selectedIndex >= 0 && selectedIndex < maxPosition - 1) {
                for (int i = selectedIndex + 1; i < maxPosition; i++) {
                    var pair = PaneUtil.getPaneTab(panesLiveData, i);
                    if (!pair.second.isPinned()) {
                        close(pair.first);
                        break;
                    }
                }
            }
        } else {
            int tabIndex = PaneUtil.findTabIndex(PaneUtil.getPaneTabs(panesLiveData), tab);
            if (tabIndex >= 0 && tabIndex < maxPosition - 1) {
                for (int i = maxPosition - 1; i > tabIndex; i--) {
                    close(PaneUtil.getPaneTab(panesLiveData, i).first);
                }
            } else {
                logger.e(TAG, "Cannot find index of a null tab.");
            }
        }
    }

    @Override
    public void closeToLeftOf(Tab tab, boolean first) {
        int selectedIndex = tab.getPosition();
        if (first) {
            if (selectedIndex > 0) {
                for (int i = selectedIndex - 1; i >= 0; i--) {
                    Pair<Tab, Pane> pair = PaneUtil.getPaneTab(panesLiveData, i);
                    if (!pair.second.isPinned()) {
                        close(pair.first);
                        break;
                    }
                }
            }
        } else {
            if (selectedIndex >= 0) {
                for (int i = selectedIndex - 1; i >= 0; i--) {
                    close(PaneUtil.getPaneTab(panesLiveData, i).first);
                }
            }
        }
    }

    @Override
    public void pinPaneTab(Tab tab, boolean isPinned) {
        for (Pair<Tab, Pane> pair : PaneUtil.getPaneTabs(panesLiveData)) {
            if (pair.first == tab) {
                pair.second.setPinned(isPinned);
                updateTab(pair.first, pair.second);
                break;
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onProjectChangeEvent(ProjectEvent event) {
        if (event.getFile() == null) {
            forceCloseAll(closeUnPinnedProjectPanes);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEditorModificationEvent(EditorModificationEvent event) {
        updateTabs(); // update all tabs
    }

    private void forceCloseAll(boolean closeUnPinned) {
        if (closeUnPinned) {
            closeAll();
        } else {
            hardCloseAll();
        }
    }

    private void hardCloseAll() {
        var iterator = PaneUtil
            .getPaneTabs(panesLiveData)
            .iterator();
        while (iterator.hasNext()) {
            var pair = iterator.next();
            if (!(pair.second instanceof WelcomePane)) {
                iterator.remove();
                binding.tablayout.removeTab(pair.first);
                binding.paneContainer.removeView(pair.second.getView());
                pair.second.destroy();
                removePaneTab(pair);
            }
        }
        binding.tablayout.requestLayout();
    }

    private void addPane(Pane pane, boolean select) {
        if (pane == null) return;

        if (PaneUtil.containsPane(PaneUtil.getPaneTabs(panesLiveData), pane)) {
            selectPaneInTabLayout(pane);
            return;
        }

        pane.createView();
        binding.paneContainer.addView(pane.getView());
        var tab = createTab(pane);
        // tab hasn't been added to the panesLiveData
        // false so tab !selected to prevent #getPair(Tab) from returning null @see
        // #onTabSelected(Tab)
        binding.tablayout.addTab(tab, false);
        var pair = Pair.create(tab, pane);
        PaneUtil.addPaneTab(pair, panesLiveData);

        if (select) selectPaneInTabLayout(pane); // select tab
        currentPaneLiveData.setValue(Pair.create(panesLiveData
            .getValue()
            .size(), pair));
        updateTabs();
    }

    public void selectPaneInTabLayout(Pane pane) {
        if (pane == null) {
            logger.e(LOG_TAG, "Cannot find pair for a null pane!");
            return;
        }

        var pair = PaneUtil.getPair(PaneUtil.getPaneTabs(panesLiveData), pane);
        if (pair != null) {
            if (pair.second.equals(pane)) {
                var tab = pair.first;
                if (tab != null) {
                    if (!tab.isSelected()) {
                        binding.tablayout.selectTab(tab);
                        mMainViewModel.setDrawerState(false);
                    } else {
                        // In variant WeStudio v0.0.1: Drawer didn't close properly Fixed in v0.0.2
                        mMainViewModel.setDrawerState(false);
                    }
                }
            }
        }
    }

    /**
     * Creates a new Pane tab
     */
    @NonNull
    private Tab createTab(Pane pane) {
        var tab = binding.tablayout.newTab();

        tab.setCustomView(R.layout.pane_tab_item);

        if (tab.getCustomView() != null) {
            ImageView tabIcon = tab
                .getCustomView()
                .findViewById(R.id.tab_icon);
            TextView tabText = tab
                .getCustomView()
                .findViewById(R.id.tab_text);
            ImageButton actionButton = tab
                .getCustomView()
                .findViewById(R.id.pane_action_button);

            var editor = PaneUtil.requireCodeEditorPane(pane);
            var webview = PaneUtil.requireWebViewPane(pane);
            var welcome = PaneUtil.requireWelcomePane(pane);
            var settings = PaneUtil.requireSettingsPane(pane);

            if (displayTabIcons) {
                if (editor != null) {
                    var file = editor.getFile();
                    tabIcon.setImageResource(ExtensionTable.getExtensionIcon(file.getName()));
                } else if (webview != null) {
                    tabIcon.setImageResource(R.drawable.ic_access_point);
                } else if (welcome != null) {
                    tabIcon.setImageResource(R.drawable.ic_codeopsstudio);
                } else if (settings != null) {
                    tabIcon.setImageResource(R.drawable.ic_cog_outline);
                }

                if (tabIcon.getVisibility() == View.INVISIBLE
                    || tabIcon.getVisibility() == View.GONE) {
                    tabIcon.setVisibility(View.VISIBLE);
                }
            } else {
                if (tabIcon.getVisibility() == View.VISIBLE) {
                    tabIcon.setVisibility(View.GONE);
                }
            }

            tabText.setText(pane.getTitle());
            actionButton.setOnClickListener(v -> {
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
        panesObserver = paneTabs -> {
            updateUI(paneTabs);
        };

        panesLiveData.observe(getViewLifecycleOwner(), panesObserver);
    }

    public void updateUI(List<Pair<Tab, Pane>> paneTabs) {
        openedPaneTabs = paneTabs;
        if (paneTabs.isEmpty()) {
            binding.tablayout.setVisibility(View.GONE);
            currentPaneLiveData.setValue(Pair.create(-1, null));
            showEmptyPaneWindow(true);
        } else {
            binding.tablayout.setVisibility(View.VISIBLE);
            showEmptyPaneWindow(false);
        }
    }

    /**
     * Method to show the popup menu.
     *
     * @param anchor   Anchor view for this popup. The popup will appear below the anchor if
     *                 there is
     *                 room, or above it if there is not.
     * @param pane     Pane assoctaied to a tab for the popup menu anchor.
     * @param listener Listener to handle menu item clicks.
     */
    private void showPopupMenu(@NonNull View anchor, Pane pane,
        PopupMenu.OnMenuItemClickListener listener) {
        if (mPopupMenu == null) {
            mPopupMenu = new PopupMenu(requireContext(), anchor, Gravity.NO_GRAVITY);

            if (!pane.isPinned()) {
                mPopupMenu
                    .getMenu()
                    .add(R.string.pane_close);
            }
            mPopupMenu
                .getMenu()
                .add(R.string.pane_close_others);
            mPopupMenu
                .getMenu()
                .add(R.string.pane_close_all);
            mPopupMenu
                .getMenu()
                .add(R.string.pane_close_right);
            mPopupMenu
                .getMenu()
                .add(R.string.pane_close_left);
            mPopupMenu
                .getMenu()
                .add(pane.isPinned() ? R.string.pane_unpin : R.string.pane_pin);
            mPopupMenu.setOnMenuItemClickListener(listener);
            mPopupMenu.setOnDismissListener(menu -> mPopupMenu = null);
            mPopupMenu.show();
        }
    }

    public String getJson() {
        var bytes =
            EncodeUtils.base64Decode(sharedPreferences.getString(Constants.SharedPreferenceKeys.KEY_PERSISTED_PANES, ""));
        return new String(bytes);
    }

    private List<LinkedTreeMap<String, Object>> getPersistedPaneTree() {
        Gson gson = new Gson();
        var type = new TypeToken<List<LinkedTreeMap<String, Object>>>() { }.getType();
        var json = getJson();
        if (json != null) {
            List<LinkedTreeMap<String, Object>> linkedTreeMapList = gson.fromJson(json, type);
            if (linkedTreeMapList != null || linkedTreeMapList.isEmpty()) return linkedTreeMapList;
        }
        return new LinkedList<LinkedTreeMap<String, Object>>();
    }

    private boolean isPersisted(Pane pane) {
        var isPersisted = false;
        var linkedTreeMapList = getPersistedPaneTree();
        for (LinkedTreeMap<String, Object> treeMap : linkedTreeMapList) {
            if (UUID
                .fromString(treeMap
                    .get("uuid")
                    .toString())
                .equals(pane.getUUID())) {
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
        Objects
            .requireNonNull(panes)
            .remove(pair);
        panesLiveData.setValue(panes);

        if (pane == null) return;

        removePersistedPane(pane);
    }

    private void removePersistedPane(@NonNull Pane pane) {
        AsyncTask.runNonCancelable(() -> {
            if (isPersisted(pane)) {
                var persistedPanes = getPersistedPaneTree();
                persistedPanes.removeIf(map -> map
                    .get("uuid")
                    .toString()
                    .equals(pane
                        .getUUID()
                        .toString()));
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(Constants.SharedPreferenceKeys.KEY_PERSISTED_PANES,
                    EncodeUtils.base64Encode2String(new Gson()
                    .toJson(persistedPanes)
                    .getBytes()));
                return editor.commit();
            }
            return false;
        }, (result, throwable) -> {
            if (result) {
                logger.i(LOG_TAG,
                    "Successfully removed " + pane.getTitle() + " from persisted storage");
            } else {
                logger.i(LOG_TAG, "Pane : " + pane + "was not persisted");
            }

            if (throwable != null) {
                Throwable cause = throwable.getCause();
                logger.e(LOG_TAG,
                    "Failed to remove" + pane + cause.getMessage() + " " + throwable.getMessage());
            }
        });
    }

    // Clears all persisted panes
    public void removePersistedPanes() {
        PreferencesUtils.clearPreference(sharedPreferences,
            Constants.SharedPreferenceKeys.KEY_PERSISTED_PANES);
    }

    public void restorePersistedPanes() {
        restorePersistedPanes(PaneUtil.loadPanes(getJson(), getContext(), getViewLifecycleOwner()));
    }

    private void restorePersistedPanes(List<Pane> paneList) {
        if (paneList != null && !paneList.isEmpty()) {
            for (Pane pane : paneList) {
                if (pane != null) {
                    var textPane = PaneUtil.requireTextPane(pane);
                    var editorPane = PaneUtil.requireEditorPane(pane);
                    var webViewPane = PaneUtil.requireWebViewPane(pane);
                    var settingsPane = PaneUtil.requireSettingsPane(pane);
                    var codeEditorPane = PaneUtil.requireCodeEditorPane(pane);

                    addPane(pane, false);

                    if (pane.isSelected()) selectPaneInTabLayout(pane);

                    if (textPane != null) {
                        var txt = textPane
                            .getArguments()
                            .get(Pane.PaneConstants.TEXT_PANE_ARGUMENT_KEY)
                            .toString();
                        textPane.setText(txt);
                    } else if (editorPane != null) {
                        var code = editorPane
                            .getArguments()
                            .get(Pane.PaneConstants.EDITOR_PANE_ARGUMENT_KEY)
                            .toString();
                        editorPane.setText(code);
                    } else if (webViewPane != null) {
                        boolean isDeskTopMode = (boolean) webViewPane
                            .getArguments()
                            .get("isDeskTopMode");
                        boolean isZoomable = (boolean) webViewPane
                            .getArguments()
                            .get("isZoomAble");
                        var path = webViewPane
                            .getArguments()
                            .get("preview_file_path")
                            .toString();
                        webViewPane.loadFile(new File(path));
                        webViewPane.setZoomable(isZoomable);
                        webViewPane.enableDeskTopMode(isDeskTopMode);
                    } else if (codeEditorPane != null) {
                        AsyncTask.runNonCancelable(() -> {
                            return codeEditorPane
                                .getArguments()
                                .get("editor_content")
                                .toString();
                        }, (result) -> {
                            if (!Wizard.isEmpty(result)) {
                                String fileExtension = codeEditorPane
                                    .getArguments()
                                    .get("file_extension")
                                    .toString();
                                codeEditorPane
                                    .getEditor()
                                    .setLanguageExtension(fileExtension);
                                codeEditorPane
                                    .getEditor()
                                    .setText(result);
                                codeEditorPane.setModified(true); // flag modified

                                Content text = codeEditorPane
                                    .getEditor()
                                    .getText();
                                int column = (int) (double) codeEditorPane
                                    .getArguments()
                                    .get("left_column");
                                int line = (int) (double) codeEditorPane
                                    .getArguments()
                                    .get("left_line");
                                int currLine = text.getLineCount();
                                int currColumn = text.getColumnCount(line);

                                if (line < currLine && column < currColumn) {
                                    codeEditorPane
                                        .getEditor()
                                        .getCursor()
                                        .set(line, column); // cursor position
                                }
                            }
                        });
                    }
                    updateTabs();
                }
            }
        }
    }

    private void restoreViewState(int behaviorState) {
        mMainViewModel.setBottomSheetState(behaviorState);
        Bundle floatOffset = new Bundle();
        floatOffset.putFloat("offset",
            behaviorState == BottomSheetBehavior.STATE_EXPANDED ? 1 : 0f);
        getChildFragmentManager().setFragmentResult(BuildActionFragment.OFFSET_KEY, floatOffset);
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
            webViewPane = new WebViewPane(requireContext(),
                getString(R.string.webview_pane_title) + " | " + file.getName());
            addPane(webViewPane, false);
        }
        webViewPane.loadFile(file);
        webViewPane.setZoomable(isZoomable);
        webViewPane.enableDeskTopMode(isDeskTopMode);
        selectPaneInTabLayout(webViewPane);
        return webViewPane;
    }

    public CodeEditorPane addCodeEditorPane(@NonNull File file) {
        return addCodeEditorPane(file, true);
    }

    public CodeEditorPane addCodeEditorPane(@NonNull File file, boolean select) {
        var tabName = file.getName();
        CodeEditorPane codeEditor = null;
        String filePath = file.getAbsolutePath();

        // Check if the code editor pane has previously been opened
        for (var pair : openedPaneTabs) {
            Pane openedPane = pair.second;
            if (openedPane != null) {
                CodeEditorPane openedEditorPane = PaneUtil.requireCodeEditorPane(openedPane);
                if (openedEditorPane != null && openedEditorPane.getFile() != null) {
                    if (openedEditorPane
                        .getFile()
                        .getAbsolutePath()
                        .equals(filePath)) {
                        codeEditor = openedEditorPane;
                        break;
                    }
                }
            }
        }

        // The associated code editor pane was not found.
        if (codeEditor == null) {
            codeEditor = new CodeEditorPane(requireContext(), tabName);
            codeEditor.setFile(file);
            addPane(codeEditor, select);
        } else {
            selectPaneInTabLayout(codeEditor); // found
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
            sp = new SettingsPane(getContext(), getString(R.string.settings),
                PreferencesFragment.newInstance());
            sp.attach(getViewLifecycleOwner());
            addPane(sp, false);
        }
        if (select) selectPaneInTabLayout(sp);
        return sp;
    }

    public WelcomePane addWelcomePane(boolean setPinned) {
        WelcomePane welcomePane = getPane(WelcomePane.class);
        if (welcomePane == null) {
            welcomePane = new WelcomePane(requireContext(), getString(R.string.welcome));
            welcomePane.setPinned(setPinned);
            if (PreferencesUtils.canShowWelcomePanel()) addPane(welcomePane, true);
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
        mMainViewModel
            .getBottomSheetState()
            .observe(getViewLifecycleOwner(), state -> {
                if (state == BottomSheetBehavior.STATE_DRAGGING
                    || state == BottomSheetBehavior.STATE_SETTLING) {
                    return;
                }
                mBehavior.setState(state);
                mOnBackPressedCallback.setEnabled(state == BottomSheetBehavior.STATE_EXPANDED);
            });

        mMainViewModel.observeEditorFileOpening(getViewLifecycleOwner(), file -> {
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

        performUIUpdate();
        mMainViewModel
            .getWebViewPaneFile()
            .observe(getViewLifecycleOwner(), file -> {
                if (file != null) addWebViewPane(file);
            });
        mMainViewModel
            .getBottomSheetExpanded()
            .observe(getViewLifecycleOwner(), expanded -> {
                if (expanded) {
                    mBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                } else {
                    mBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            });
        mMainViewModel
            .addSettingsPane()
            .observe(getViewLifecycleOwner(), canAdd -> {
                if (canAdd) {
                    addSettingsPane();
                }
            });
    }

    private <T> T getPane(Class<T> paneClass) {
        var paneTab = PaneUtil.getPaneTabs(panesLiveData);
        if (paneTab != null) {
            for (Pair<Tab, Pane> pair : paneTab) {
                if (pair.second
                    .getClass()
                    .getName()
                    .equals(paneClass.getName())) {
                    return paneClass.cast(pair.second);
                }
            }
        }
        return null;
    }
}
