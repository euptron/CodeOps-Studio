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
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.eup.codeopsstudio.R;
import com.eup.codeopsstudio.common.Constants;
import com.eup.codeopsstudio.common.ILog;
import com.eup.codeopsstudio.common.util.PreferencesUtils;
import com.eup.codeopsstudio.databinding.FragmentBuildActionBinding;
import com.eup.codeopsstudio.domain.events.CurrentPaneEvent;
import com.eup.codeopsstudio.editor.ContextualCodeEditor;
import com.eup.codeopsstudio.editor.langs.textmate.provider.JsonLanguageInfoProvider;
import com.eup.codeopsstudio.pane.Pane;
import com.eup.codeopsstudio.ui.editor.actions.models.EditorAction;
import com.eup.codeopsstudio.ui.editor.actions.models.EditorShortcutWizard;
import com.eup.codeopsstudio.ui.editor.actions.adapters.EditorShortcutAdapter;
import com.eup.codeopsstudio.ui.editor.actions.adapters.BuildActionPagerAdapter;
import com.eup.codeopsstudio.ui.editor.code.CodeEditorPane;
import com.eup.codeopsstudio.util.BaseUtil;
import com.google.android.material.tabs.TabLayoutMediator;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;

/**
 * @author Etido Peter
 */
public class BuildActionFragment extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String OFFSET_KEY = "offsetKey";
    public static final String TAG = "BuildActionFragment";
    
    private int collapsedHeightPx = 0;
    private int expandedHeightPx = 0;
    private String shortcutsJsonString;
    private FragmentBuildActionBinding binding;
    private EditorShortcutWizard shortcutWizard;
    private EditorShortcutAdapter shortcutAdapter;
    
    public static BuildActionFragment newInstance() {
        return new BuildActionFragment();
    }
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        shortcutsJsonString = loadShortcutsJson();
        shortcutAdapter = new EditorShortcutAdapter();
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState) {
        binding = FragmentBuildActionBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
    
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
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

        new TabLayoutMediator(binding.tabLayout, binding.actionPager, (tab, position) -> {
            if (position == 0) {
                tab.setText(R.string.build_output);
            } else if (position == 1) {
                tab.setText(R.string.ide_logs);
            }
        }).attach();
        
        binding.rowLayout.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        binding.rowLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        measureRowHeights();
                    }
                });
                
        getParentFragmentManager().setFragmentResultListener(OFFSET_KEY, getViewLifecycleOwner(),
            ((requestKey, result) -> setHeightRatio(result.getFloat("offset", 0f))));
        
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
        ILog.debug(TAG, String.format("CurrentPaneEven received - Pos: %d, Pane: %s, Type: %s",
            event.getIndex(),
            event.getPane() != null ? event.getPane().getTitle() : "null",
            event.getPane() != null ? event.getPane().getClass().getSimpleName() : "null"));

        if (event.getPane() instanceof CodeEditorPane editorPane) {
            ILog.debug(TAG, "Setting up shortcuts for CodeEditorPane");
            
            shortcutAdapter.bindEditor(editor);
            
            if (shortcutWizard == null) {
                shortcutWizard = new EditorShortcutWizard(editorPane.getEditor(), shortcutsJsonString);
            } else {
                shortcutWizard.setEditorContext(editorPane.getEditor());
            }
            
            refreshShortcuts();
            binding.rowLayout.setDisplayedChild(1);
            // Expand shortcuts fully
            setHeightRatio(1f);
        } else {
            binding.rowLayout.setDisplayedChild(0);
            ILog.debug(TAG, "Not a CodeEditorPane, hiding shortcuts");
        }
        
        binding.rowLayout.post(this::measureRowHeights);
    }

    private void measureRowHeights() {
        binding.rowLayout.post(() -> {
            View collapsedView = binding.rowLayout.getChildAt(0);
            View expandedView = binding.rowLayout.getChildAt(1);
            
            collapsedView.measure(
                View.MeasureSpec.makeMeasureSpec(binding.rowLayout.getWidth(), View.MeasureSpec.EXACTLY),
                View.MeasureSpec.UNSPECIFIED
            );
            
            // dynamic content
            expandedView.measure(
                View.MeasureSpec.makeMeasureSpec(binding.rowLayout.getWidth(), View.MeasureSpec.AT_MOST),
                View.MeasureSpec.UNSPECIFIED
            );
    
            collapsedHeightPx = collapsedView.getMeasuredHeight();
            expandedHeightPx = Math.max(expandedView.getMeasuredHeight(), collapsedHeightPx);
            
            ILog.debug(TAG, "Measured heights -> collapsed=" + collapsedHeightPx + "px, expanded=" + expandedHeightPx + "px");
            applyRowHeight(collapsedHeightPx);
        });
    }

    private void setHeightRatio(float ratio) {
        if (collapsedHeightPx == 0 || expandedHeightPx == 0) return;
        float clamped = Math.max(0f, Math.min(1f, ratio));
        int height = (int) (collapsedHeightPx + (expandedHeightPx - collapsedHeightPx) * clamped);
        applyRowHeight(height);
    }

    private void applyRowHeight(int heightPx) {
        if (binding == null || binding.rowLayout == null) return;
        
        binding.rowLayout.post(() -> {
            ViewGroup.LayoutParams params = binding.rowLayout.getLayoutParams();
            if (params != null && params.height != heightPx) {
                params.height = heightPx;
                binding.rowLayout.setLayoutParams(params);
                binding.rowLayout.requestLayout();
            }
        });
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

    private void refreshShortcuts() {
        if (shortcutWizard == null) return;
        
        boolean useTabs = PreferencesUtils.useTabIndentation();
        int numberOfTabs = PreferencesUtils.getCodeEditorTabSize();
        
        shortcutWizard.invalidateCache();
        
        List<EditorAction> baseActions = shortcutWizard.getActions();
        List<EditorAction> configuredActions = EditorShortcutWizard.configureTabAction(baseActions, useTabs, numberOfTabs);
        shortcutAdapter.submitList(configuredActions);
    }
}