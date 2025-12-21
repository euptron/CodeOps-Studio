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

package com.eup.codeopsstudio.ui.editor.actions;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.eup.codeopsstudio.R;
import com.eup.codeopsstudio.common.AsyncTask;
import com.eup.codeopsstudio.common.Constants;
import com.eup.codeopsstudio.common.ContextManager;
import com.eup.codeopsstudio.common.ILog;
import com.eup.codeopsstudio.common.util.PreferencesUtils;
import com.eup.codeopsstudio.databinding.FragmentBuildActionBinding;
import com.eup.codeopsstudio.domain.events.CurrentPaneEvent;
import com.eup.codeopsstudio.domain.events.EditorReadyEvent;
import com.eup.codeopsstudio.editor.langs.textmate.provider.JsonLanguageInfoProvider;
import com.eup.codeopsstudio.ui.editor.actions.adapters.BuildActionPagerAdapter;
import com.eup.codeopsstudio.ui.editor.actions.adapters.EditorShortcutAdapter;
import com.eup.codeopsstudio.ui.editor.actions.models.EditorAction;
import com.eup.codeopsstudio.ui.editor.actions.models.EditorShortcutWizard;
import com.eup.codeopsstudio.ui.editor.code.CodeEditorPane;
import com.eup.codeopsstudio.util.BaseUtil;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * @author Etido Peter
 */
public class BuildActionFragment extends Fragment
    implements SharedPreferences.OnSharedPreferenceChangeListener {

  public static final String OFFSET_KEY = "offsetKey";
  public static final String TAG = "BuildActionFragment";

  private static final int MIN_HEADER_HEIGHT = 0;
  private static final long SHOW_MEMORY_OVERLAY_DELAY_MS = 3000L;
  private static final long HIDE_MEMORY_OVERLAY_DELAY_MS = 4000L;
  private static final long MEMORY_UPDATE_INTERVAL_MS = 200L;
  private static final int MEMORY_OVERLAY_VIEW_INDEX = 2;

  private int headerHeight;
  private String shortcutsJsonString;
  private FragmentBuildActionBinding binding;
  private EditorShortcutWizard shortcutWizard;
  private EditorShortcutAdapter shortcutAdapter;
  private Runnable memoryUpdateTask;

  private long pressStartTime = 0;
  private int previousDisplayedChild = 0;
  private Runnable toggleMemoryOverlayRunnable;
  private boolean isMemoryOverlayVisible = false;
  private boolean onEditorReadyEventCalled = false;

  public static BuildActionFragment newInstance() {
    return new BuildActionFragment();
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    shortcutsJsonString = loadShortcutsJson();
    shortcutAdapter = new EditorShortcutAdapter();
    headerHeight =
        ContextManager.getOriginalDimensionPixelSize(
            requireContext(), R.dimen.build_action_header_height);

    toggleMemoryOverlayRunnable = this::toggleMemoryOverlay;
  }

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    binding = FragmentBuildActionBinding.inflate(inflater, container, false);
    return binding.getRoot();
  }

  @Override
  public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    BaseUtil.applyImeInsets(binding.getRoot(), true);
    
    var llm = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
    binding.recyclerviewShortcuts.setLayoutManager(llm);
    binding.recyclerviewShortcuts.setHasFixedSize(true);
    binding.recyclerviewShortcuts.setAdapter(shortcutAdapter);

    var actionAdapter = new BuildActionPagerAdapter(getChildFragmentManager(), getLifecycle());
    actionAdapter.addFragment(OutPutFragment.newInstance());
    actionAdapter.addFragment(IdeLogsFragment.newInstance());
    binding.actionPager.setOffscreenPageLimit(1);
    binding.actionPager.setUserInputEnabled(false);
    binding.actionPager.setAdapter(actionAdapter);

    new TabLayoutMediator(binding.tabLayout, binding.actionPager, this::configTabs).attach();

    getParentFragmentManager()
        .setFragmentResultListener(
            OFFSET_KEY,
            getViewLifecycleOwner(),
            ((requestKey, result) -> applyHeaderOffset(result.getFloat("offset", 0f))));

    PreferencesUtils.getDefaultPreferences().registerOnSharedPreferenceChangeListener(this);
    refreshShortcuts();
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
  public void onDestroyView() {
    super.onDestroyView();
    PreferencesUtils.getDefaultPreferences().unregisterOnSharedPreferenceChangeListener(this);
    removePendingMemoryOverlayRunnables();
    this.binding = null;
  }

  @Override
  public void onSharedPreferenceChanged(SharedPreferences pref, @Nullable String key) {
    switch (Objects.requireNonNull(key)) {
      case Constants.SharedPreferenceKeys.KEY_CODE_EDITOR_TAB_SIZE,
          Constants.SharedPreferenceKeys.KEY_CODE_EDITOR_TAB_INDENT:
        refreshShortcuts();
        break;
    }
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onCurrentPaneChangeEvent(@NonNull CurrentPaneEvent event) {
    try {
      if (event.getPane() instanceof CodeEditorPane editorPane) {
        if (onEditorReadyEventCalled) {
          setupEditorShortcuts(editorPane);
        }
        binding.actionsHeader.setDisplayedChild(1);
      } else {
        binding.actionsHeader.setDisplayedChild(0);
      }
    } catch (Throwable e) {
      ILog.error(TAG, "Error handling current pane event " + e.getMessage(), e);
      binding.actionsHeader.setDisplayedChild(0); // Fail-safe
    }
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onEditorReadyEvent(@NonNull EditorReadyEvent event) {
    CodeEditorPane editorPane = event.getEditorPane();
    if (editorPane.isSelected()) {
      setupEditorShortcuts(editorPane);
    }
    onEditorReadyEventCalled = true;
  }

  private void setupEditorShortcuts(@NonNull CodeEditorPane editorPane) {
    if (binding == null || editorPane.getEditor() == null) return;

    try {
      shortcutAdapter.bindEditor(editorPane.getEditor());

      if (shortcutWizard == null) {
        shortcutWizard = new EditorShortcutWizard(editorPane.getEditor(), shortcutsJsonString);
      } else {
        shortcutWizard.setEditorContext(editorPane.getEditor());
      }

      refreshShortcuts();
    } catch (Throwable e) {
      ILog.error(TAG, "Critical error during setupEditorShortcuts: " + e.getMessage(), e);
      binding.actionsHeader.setDisplayedChild(0); // Fail-safe
    }
  }

  private void refreshShortcuts() {
    if (shortcutWizard == null) return;
    boolean useTabs = PreferencesUtils.useTabIndentation();
    int tabSize = PreferencesUtils.getCodeEditorTabSize();

    shortcutWizard.invalidateCache();
    List<EditorAction> actions = shortcutWizard.getActions();
    shortcutAdapter.submitList(EditorShortcutWizard.configureTabAction(actions, useTabs, tabSize));
  }

  private void configTabs(TabLayout.Tab tab, int position) {
    int[] tabTitles = {R.string.build_output, R.string.ide_logs};
    tab.setText(tabTitles[position]);

    if (position == 0) {
      setupTabLongPressGesture(tab.view);
    }
  }

  private String loadShortcutsJson() {
    try {
      InputStream is = requireContext().getAssets().open("editor/shortcuts.json");
      String result = JsonLanguageInfoProvider.readInputStream(is);
      return result != null ? result : "{}";
    } catch (IOException e) {
      ILog.error(TAG, "Failed to load shortcuts JSON file.", e);
      return "{}";
    }
  }

  private void applyHeaderOffset(float offset) {
    float progress = offset <= 0 ? 1 - Math.abs(offset) : 1 - offset;
    int newHeight = (int) (MIN_HEADER_HEIGHT + (headerHeight - MIN_HEADER_HEIGHT) * progress);
    setHeaderHeight(Math.max(MIN_HEADER_HEIGHT, newHeight));
  }

  private void setHeaderHeight(int heightPx) {
    if (binding == null || binding.actionsHeader == null) return;
    ViewGroup.LayoutParams params = binding.actionsHeader.getLayoutParams();

    if (params != null) {
      params.height = BaseUtil.dpToPx(heightPx);
      binding.actionsHeader.setLayoutParams(params);

      if (binding.actionsHeader.getHeight() != heightPx) {
        binding.actionsHeader.requestLayout();
      }
    }
  }

  private void removePendingMemoryOverlayRunnables() {
    AsyncTask.cancelRunLater(memoryUpdateTask);
    if (binding != null && binding.tabLayout != null) {
      binding.tabLayout.removeCallbacks(toggleMemoryOverlayRunnable);
    }
  }

  private void setupTabLongPressGesture(View tabView) {
    tabView.setOnTouchListener(
        (v, event) -> {
          switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
              pressStartTime = System.currentTimeMillis();
              long delay =
                  isMemoryOverlayVisible
                      ? HIDE_MEMORY_OVERLAY_DELAY_MS
                      : SHOW_MEMORY_OVERLAY_DELAY_MS;
              binding.tabLayout.postDelayed(toggleMemoryOverlayRunnable, delay);
              break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
              binding.tabLayout.removeCallbacks(toggleMemoryOverlayRunnable);
              handleLongPressResult(System.currentTimeMillis() - pressStartTime);
              break;
          }
          return false; // Don't consume event - allow normal tab selection behavior
        });
  }

  private void handleLongPressResult(long pressDuration) {
    long requiredDelay =
        isMemoryOverlayVisible ? HIDE_MEMORY_OVERLAY_DELAY_MS : SHOW_MEMORY_OVERLAY_DELAY_MS;
    if (pressDuration >= requiredDelay) {
      toggleMemoryOverlay();
    }
  }

  private void toggleMemoryOverlay() {
    if (binding == null) return;

    if (isMemoryOverlayVisible) {
      binding.actionsHeader.setDisplayedChild(previousDisplayedChild);
      isMemoryOverlayVisible = false;
    } else {
      previousDisplayedChild = binding.actionsHeader.getDisplayedChild();
      binding.actionsHeader.setDisplayedChild(MEMORY_OVERLAY_VIEW_INDEX);
      isMemoryOverlayVisible = true;
      setUpMemoryOverlay();
    }
  }

  private void setUpMemoryOverlay() {
    updateMemoryNow();
    memoryUpdateTask =
        () -> {
          updateMemoryNow();
          if (isMemoryOverlayVisible) {
            AsyncTask.runLaterOnUiThread(memoryUpdateTask, MEMORY_UPDATE_INTERVAL_MS);
          }
        };
    AsyncTask.runLaterOnUiThread(memoryUpdateTask, MEMORY_UPDATE_INTERVAL_MS);
  }

  private void updateMemoryNow() {
    if (binding != null && isMemoryOverlayVisible) {
      binding.memoryOverlay.updateMemoryInfo(Runtime.getRuntime());
    }
  }
}
