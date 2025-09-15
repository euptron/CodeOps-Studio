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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.eup.codeopsstudio.R;
import com.eup.codeopsstudio.adapters.BuildActionPagerAdapter;
import com.eup.codeopsstudio.common.Constants;
import com.eup.codeopsstudio.common.ILog;
import com.eup.codeopsstudio.common.util.PreferencesUtils;
import com.eup.codeopsstudio.databinding.FragmentBuildActionBinding;
import com.eup.codeopsstudio.domain.events.CurrentPaneEvent;
import com.eup.codeopsstudio.editor.ContextualCodeEditor;
import com.eup.codeopsstudio.editor.langs.textmate.provider.JsonLanguageInfoProvider;
import com.eup.codeopsstudio.pane.Pane;
import com.eup.codeopsstudio.ui.editor.actions.EditorAction;
import com.eup.codeopsstudio.ui.editor.actions.EditorShortcutAdapter;
import com.eup.codeopsstudio.ui.editor.actions.EditorShortcutWizard;
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

public class BuildActionFragment extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String OFFSET_KEY = "offsetKey";
    private final String TAG = "BuildActionFragment";
    private FragmentBuildActionBinding binding;
    private EditorShortcutAdapter shortcutAdapter;
    private int numberOfTabs;
    private boolean useTabs;
    private EditorShortcutWizard shortcutWizard;
    private String shortcutsJsonString;

    public static BuildActionFragment newInstance() {
        return new BuildActionFragment();
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
        PreferencesUtils
            .getDefaultPreferences()
            .registerOnSharedPreferenceChangeListener(this);
        shortcutAdapter = new EditorShortcutAdapter();
        var adapter = new BuildActionPagerAdapter(getChildFragmentManager(), getLifecycle());
        loadShortcutsJson();
        adapter.addFragment(OutPutFragment.newInstance());
        adapter.addFragment(IdeLogsFragment.newInstance());
        // mAdapter.addFragment(DiagnosticsFragment.newInstance());
        // TODO: Support terminal fragment
        binding.actionPager.setOffscreenPageLimit(1);
        binding.actionPager.setUserInputEnabled(false);
        binding.actionPager.setAdapter(adapter);

        new TabLayoutMediator(binding.tabLayout, binding.actionPager, (tab, position) -> {
            if (position == 0) {
                tab.setText(R.string.build_output);
            } else if (position == 1) {
                tab.setText(R.string.ide_logs);
            }
            // else if (position == 2) {
            // tab.setText(R.string.diagnostics);
            // }
        }).attach();

        getParentFragmentManager().setFragmentResultListener(OFFSET_KEY, getViewLifecycleOwner(),
            ((requestKey, result) -> setOffset(result.getFloat("offset", 0f))));

        binding.recyclerviewShortcuts.setLayoutManager(new LinearLayoutManager(getContext(),
            LinearLayoutManager.HORIZONTAL, false));
        binding.recyclerviewShortcuts.setHasFixedSize(true);
        binding.recyclerviewShortcuts.setAdapter(shortcutAdapter);
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
        PreferencesUtils
            .getDefaultPreferences()
            .unregisterOnSharedPreferenceChangeListener(this);
        this.binding = null;
    }

    private void setOffset(float offset) {
        if (offset >= 0.50f) {
            float invertedOffset = 0.5f - offset;
            setRowOffset(((invertedOffset + 0.5f) * 2f));
        } else {
            if (binding.rowLayout.getHeight() != BaseUtil.dp(30)) {
                setRowOffset(1f);
            }
        }
    }

    private void setRowOffset(float offset) {
        binding.rowLayout.getLayoutParams().height = Math.round(BaseUtil.dp(38) * offset);
        binding.rowLayout.requestLayout();
    }

    private void loadShortcutsJson() {
        if (shortcutsJsonString == null) {
            try {
                InputStream is = requireContext()
                    .getAssets()
                    .open("editor/shortcuts.json");
                shortcutsJsonString = JsonLanguageInfoProvider.readInputStream(is);
            } catch (IOException e) {
                ILog.error(TAG, "Failed to load shortcuts JSON file.", e);
                shortcutsJsonString = "{}";// empty json
            }
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences pref, @Nullable String key) {
        switch (Objects.requireNonNull(key)) {
            case Constants.SharedPreferenceKeys.KEY_CODE_EDITOR_TAB_SIZE:
                numberOfTabs = PreferencesUtils.getCodeEditorTabSize();
                refreshShortcuts();
                break;
            case Constants.SharedPreferenceKeys.KEY_CODE_EDITOR_TAB_INDENT:
                useTabs = PreferencesUtils.useTabIndentation();
                refreshShortcuts();
                break;
        }
    }

    private void refreshShortcuts() {
        if (shortcutWizard == null) {
            ILog.debug(TAG, "ShortcutWizard is null");
            return;
        }
        shortcutWizard.invalidateCache();
        List<EditorAction> baseActions = shortcutWizard.getActions();
        List<EditorAction> configuredActions =
            EditorShortcutWizard.configureTabAction(baseActions, useTabs, numberOfTabs);
        shortcutAdapter.submitList(configuredActions);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCurrentPaneChangeEvent(@NonNull CurrentPaneEvent event) {
        int position = event.index;
        Pane currentPane = event.pane;

        if (position != -1 || currentPane != null) {
            if (currentPane instanceof CodeEditorPane editorPane) {
                ContextualCodeEditor editor = editorPane.getEditor();
                shortcutAdapter.bindEditor(editor);
                if (shortcutWizard == null) {
                    shortcutWizard = new EditorShortcutWizard(editor, shortcutsJsonString);
                } else {
                    shortcutWizard.setEditorContext(editor);
                }
                refreshShortcuts();
                binding.rowLayout.setDisplayedChild(1);
            } else {
                binding.rowLayout.setDisplayedChild(0);
            }
        }
    }
}
