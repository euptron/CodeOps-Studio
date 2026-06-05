/*
 * This file is part of CodeOps Studio.
 * CodeOps Studio - Code anywhere anytime
 * https://github.com/euptron/CodeOps-Studio
 * Copyright (C) 2024-2026 Etido Peter
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

package com.eup.codeopsstudio.ui.explore;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.eup.codeopsstudio.IdeApplication;
import com.eup.codeopsstudio.R;
import com.eup.codeopsstudio.common.util.TextWatcherAdapter;
import com.eup.codeopsstudio.databinding.FragmentPluginBinding;
import com.eup.codeopsstudio.plugin.PluginAdapter;
import com.eup.codeopsstudio.plugin.PluginItem;
import com.eup.codeopsstudio.plugin.PluginItemComparator;
import com.eup.codeopsstudio.plugin.PluginScanner;
import com.eup.codeopsstudio.util.UninstallDialogFragment;
import com.eup.codeopsstudio.util.Wizard;
import com.google.android.material.chip.ChipGroup;
import io.github.rosemoe.sora.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PluginFragment extends Fragment {

  public static final String TAG = PluginFragment.class.getSimpleName();

  private PluginAdapter adapter;
  private PluginScanner pluginScanner;
  private FragmentPluginBinding binding;

  public static PluginFragment newInstance() {
    return new PluginFragment();
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    pluginScanner = IdeApplication.getPluginScanner();
  }

  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater, ViewGroup viewgroup, Bundle savedInstanceState) {
    binding = FragmentPluginBinding.inflate(inflater, viewgroup, false);
    return binding.getRoot();
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    configRecyclerView();

    binding.pluginFilterGroup.setOnCheckedStateChangeListener(
        new ChipGroup.OnCheckedStateChangeListener() {
          @Override
          public void onCheckedChanged(
              @NonNull ChipGroup group, @NonNull List<Integer> checkedIds) {
            if (checkedIds.isEmpty()) {
              return;
            }

            int selectedId = checkedIds.get(0);

            if (selectedId == R.id.chip_installed) {
              performSearch("");
            } else if (selectedId == R.id.chip_marketplace) {
              // TODO Handle
            }
          }
        });

    binding.swipeRefreshLayout.setOnRefreshListener(
        new SwipeRefreshLayout.OnRefreshListener() {
          @Override
          public void onRefresh() {
            performSearch("");
          }
        });
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    this.binding = null;
  }

  private void configRecyclerView() {
    configRecyclerView(true);
  }

  private void configRecyclerView(boolean killAnimations) {
    binding.pluginRecyclerview.setLayoutManager(new LinearLayoutManager(getContext()));
    adapter = new PluginAdapter(this);
    binding.pluginRecyclerview.setAdapter(adapter);
    adapter.setOnItemClickListener(this::launchPluginDetails);

    if (killAnimations) {
      binding.pluginRecyclerview.setItemAnimator(null);
    }

    indicateProgress(true);
    configSearch();
  }

  private void configSearch() {
    var etSearch = binding.searchTil.getEditText();

    etSearch.addTextChangedListener(
        new TextWatcherAdapter() {
          @Override
          public void onTextChanged(CharSequence input, int start, int before, int count) {
            performSearch(input.toString());
          }
        });

    if (etSearch.getText().length() == 0) {
      performSearch("");
    } else {
      performSearch(etSearch.getText().toString());
    }
  }

  private void performSearch(String query) {
    adapter.submitList(
        search(query),
        () -> {
          binding.pluginRecyclerview.scrollToPosition(0);
        });
  }

  public List<PluginItem> search(String query) {
    List<PluginItem> items = pluginScanner.scan();
    indicateProgress(false);
    binding.swipeRefreshLayout.setRefreshing(false);

    if (Wizard.isEmpty(query)) {
      return ((PluginItemComparator) PluginItemComparator.INSENSITIVE).sort(items);
    }

    String cleanQuery = query.toLowerCase().trim();
    List<PluginItem> results = new ArrayList<>();

    for (PluginItem item : items) {
      if (item.name.toLowerCase().contains(cleanQuery)) {
        results.add(item);
      }
    }

    return ((PluginItemComparator) PluginItemComparator.INSENSITIVE).sort(results);
  }

  private void indicateProgress(boolean show) {
    binding.progressIndicator.setVisibility(show ? View.VISIBLE : View.GONE);
  }

  private void launchPluginDetails(PluginItem item) {
    // TODO: implement this
  }
}
