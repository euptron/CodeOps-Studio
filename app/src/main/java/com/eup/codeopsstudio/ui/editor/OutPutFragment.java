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

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.eup.codeopsstudio.databinding.FragmentBuildOutputBinding;
import com.eup.codeopsstudio.viewmodel.MainViewModel;
import com.eup.codeopsstudio.common.util.RecyclerViewOnScrollListener;
import com.eup.codeopsstudio.logging.Logger;
import com.eup.codeopsstudio.logging.LogAdapter;
import androidx.lifecycle.ViewModelProvider;
import com.eup.codeopsstudio.res.R;
import androidx.annotation.Nullable;

public class OutPutFragment extends Fragment {

  public static final String TAG = OutPutFragment.class.getSimpleName();
  public static final String LOG_TAG = "IDE Logs Fragment";
  private FragmentBuildOutputBinding binding;

  private Logger logger;
  private LogAdapter logAdapter;
  private MainViewModel model;
  private RecyclerViewOnScrollListener listener;

  public static OutPutFragment newInstance() {
    return new OutPutFragment();
  }
  
  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    logger = new Logger(Logger.LogClass.BUILD);
    logAdapter = new LogAdapter();
  }
  
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater, ViewGroup viewgroup, Bundle savedInstanceState) {
    binding = FragmentBuildOutputBinding.inflate(inflater, viewgroup, false);
    //...
    binding.outViewFlipper.setDisplayedChild(1);
    binding.clearBuildLogsFab.setVisibility(View.GONE);
    return binding.getRoot();
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    model = new ViewModelProvider(requireActivity()/*shared activity scope*/).get(MainViewModel.class);
    logger.attach(requireActivity()/*shared activity scope*/);
    binding.buildOutputRecyclerview.setLayoutManager(new LinearLayoutManager(requireContext()));
    binding.buildOutputRecyclerview.setAdapter(logAdapter);
    binding.buildOutputRecyclerview.setHasFixedSize(true);
    listener =
        new RecyclerViewOnScrollListener() {
          @Override
          public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            if (dy < 0) {
              binding.clearBuildLogsFab.extend();
            } else if (dy > 0) {
              binding.clearBuildLogsFab.shrink();
            }
          }
        };

    binding.buildOutputRecyclerview.addOnScrollListener(listener);

    model
        .getBUILDLogs()
        .observe(
            getViewLifecycleOwner(),
            data -> {
              if (data.isEmpty()) {
                binding.outViewFlipper.setDisplayedChild(1);
                binding.clearBuildLogsFab.setVisibility(View.GONE);
              } else {
                binding.outViewFlipper.setDisplayedChild(0);
                binding.clearBuildLogsFab.setVisibility(View.VISIBLE);
                logAdapter.submitList(data);
                scrollToLastItem();
              }
            });
    binding.clearBuildLogsFab.setOnClickListener(v -> clearLogs());
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    this.binding = null;
  }

  private void clearLogs() {
    if (logger != null) {
      logger.clear();
      logAdapter.notifyDataSetChanged();
    }
  }

  private void scrollToLastItem() {
    int position = logAdapter.getItemCount();
    if (position > 0) {
      binding.buildOutputRecyclerview.scrollToPosition(position - 1);
    }
  }
}
