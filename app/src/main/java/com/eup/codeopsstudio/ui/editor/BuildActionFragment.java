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

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.eup.codeopsstudio.adapters.BuildActionPagerAdapter;
import com.eup.codeopsstudio.common.util.PreferencesUtils;
import com.eup.codeopsstudio.databinding.FragmentBuildActionBinding;
import com.eup.codeopsstudio.events.CurrentPaneEvent;
import com.eup.codeopsstudio.pane.Pane;
import com.eup.codeopsstudio.res.R;
import com.eup.codeopsstudio.ui.editor.code.CodeEditorPane;
import com.eup.codeopsstudio.util.BaseUtil;
import com.eup.codeopsstudio.viewmodel.MainViewModel;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class BuildActionFragment extends Fragment
    implements SharedPreferences.OnSharedPreferenceChangeListener {

  public static final String OFFSET_KEY = "offsetKey";
  private FragmentBuildActionBinding binding;
  private BuildActionPagerAdapter adapter;
  private MainViewModel mMainViewModel;

  public static BuildActionFragment newInstance() {
    return new BuildActionFragment();
  }

  @Nullable
  @Override
  public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    binding = FragmentBuildActionBinding.inflate(inflater, container, false);
    return binding.getRoot();
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    mMainViewModel =
        new ViewModelProvider(requireActivity() /*shared activity scope*/).get(MainViewModel.class);
    PreferencesUtils.getDefaultPreferences().registerOnSharedPreferenceChangeListener(this);
    adapter = new BuildActionPagerAdapter(getChildFragmentManager(), getLifecycle());

    adapter.addFragment(OutPutFragment.newInstance());
    adapter.addFragment(IdeLogsFragment.newInstance());
    // adapter.addFragment(DiagnosticsFragment.newInstance());
    // TODO: Support terminal fragment
    binding.actionPager.setOffscreenPageLimit(1);
    binding.actionPager.setUserInputEnabled(false);
    binding.actionPager.setAdapter(adapter);

    new TabLayoutMediator(
            binding.tabLayout,
            binding.actionPager,
            new TabLayoutMediator.TabConfigurationStrategy() {
              @Override
              public void onConfigureTab(TabLayout.Tab tab, int position) {
                if (position == 0) {
                  tab.setText(R.string.build_output);
                } else if (position == 1) {
                  tab.setText(R.string.ide_logs);
                }
                // else if (position == 2) {
                // tab.setText(R.string.diagnostics);
                // }
              }
            })
        .attach();

    getParentFragmentManager()
        .setFragmentResultListener(
            OFFSET_KEY,
            getViewLifecycleOwner(),
            ((requestKey, result) -> {
              setOffset(result.getFloat("offset", 0f));
            }));
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    this.binding = null;
    PreferencesUtils.getDefaultPreferences().unregisterOnSharedPreferenceChangeListener(this);
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
  public void onSharedPreferenceChanged(SharedPreferences pref, String key) {
    switch (key) {
      case SharedPreferenceKeys.KEY_CODE_EDITOR_TAB_SIZE:
        binding.recyclerviewShortcuts.updateTabSize(PreferencesUtils.getCodeEditorTabSize());
        break;
      case SharedPreferenceKeys.KEY_CODE_EDITOR_TAB_INDENT:
        binding.recyclerviewShortcuts.useTabIndentation(PreferencesUtils.useTabIndentation());
        break;
    }
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onCurrentPaneChangeEvent(CurrentPaneEvent event) {
    int position = event.getIndex();
    Pane currentPane = event.getPane();

    if (position != -1 || currentPane != null) {
      if (currentPane instanceof CodeEditorPane) {
        CodeEditorPane editorPane = (CodeEditorPane) currentPane;
        binding.recyclerviewShortcuts.bindEditor(editorPane.getEditor());
        binding.rowLayout.setDisplayedChild(1);
      } else {
        binding.rowLayout.setDisplayedChild(0);
      }
    }
  }

  private void setOffset(float offset) {
    if (binding.rowLayout == null) {
      return;
    }

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
}
