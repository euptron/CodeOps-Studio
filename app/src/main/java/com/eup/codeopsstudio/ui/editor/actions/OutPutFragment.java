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

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.eup.codeopsstudio.logger.adapter.LogAdapter;
import com.eup.codeopsstudio.common.util.RecyclerViewOnScrollListener;
import com.eup.codeopsstudio.databinding.FragmentBuildOutputBinding;
import com.eup.codeopsstudio.logger.model.Log;
import com.eup.codeopsstudio.logger.Logger;
import com.eup.codeopsstudio.viewmodel.MainViewModel;

import java.util.ArrayList;

public class OutPutFragment extends Fragment {

    public static final String TAG = OutPutFragment.class.getSimpleName();
    public static final String LOG_TAG = "IDE Logs Fragment";
    private FragmentBuildOutputBinding binding;

    private Logger logger;
    private LogAdapter logAdapter;
    private MainViewModel model;
    private RecyclerViewOnScrollListener listener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        logger     = new Logger(Logger.LogClass.BUILD);
        logAdapter = new LogAdapter();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup viewgroup,
        Bundle savedInstanceState) {
        binding = FragmentBuildOutputBinding.inflate(inflater, viewgroup, false);
        binding.outViewFlipper.setDisplayedChild(1);
        binding.clearBuildLogsFab.setVisibility(View.GONE);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        model = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        logger.attach(getActivity());

        binding.buildOutputRecyclerview.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.buildOutputRecyclerview.setHasFixedSize(true);
        binding.buildOutputRecyclerview.setAdapter(logAdapter);

        listener = new RecyclerViewOnScrollListener() {
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

        model.getBUILDLogs().observe(getViewLifecycleOwner(), this::updateLayout);
        binding.clearBuildLogsFab.setOnClickListener(v -> clearLogs());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        this.binding = null;
    }

    private void clearLogs() {
        logger.clear();
        logAdapter.notifyDataSetChanged();
    }

    private void updateLayout(ArrayList<Log> logs) {
        if (logs == null) return;

        if (logs.isEmpty()) {
            binding.outViewFlipper.setDisplayedChild(1);
            binding.clearBuildLogsFab.setVisibility(View.GONE);
        } else {
            binding.outViewFlipper.setDisplayedChild(0);
            binding.clearBuildLogsFab.setVisibility(View.VISIBLE);
            logAdapter.submitList(logs);
            scrollToLastItem();
        }
    }

    private void scrollToLastItem() {
        int position = logAdapter.getItemCount();
        if (position > 0) {
            binding.buildOutputRecyclerview.scrollToPosition(position - 1);
        }
    }

    public static OutPutFragment newInstance() {
        return new OutPutFragment();
    }
}
