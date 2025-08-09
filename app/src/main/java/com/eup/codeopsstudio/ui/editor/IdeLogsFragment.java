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

import com.eup.codeopsstudio.adapters.logger.LogAdapter;
import com.eup.codeopsstudio.common.util.RecyclerViewOnScrollListener;
import com.eup.codeopsstudio.databinding.FragmentIdeLogsBinding;
import com.eup.codeopsstudio.models.logger.Log;
import com.eup.codeopsstudio.models.logger.Logger;
import com.eup.codeopsstudio.viewmodel.MainViewModel;

import java.util.ArrayList;

public class IdeLogsFragment extends Fragment {

    public static final String TAG = IdeLogsFragment.class.getSimpleName();
    public static final String LOG_TAG = "IDE Logs Fragment";
    private FragmentIdeLogsBinding binding;
    private Logger logger;
    private LogAdapter logAdapter;
    private MainViewModel model;
    private RecyclerViewOnScrollListener listener;

    public static IdeLogsFragment newInstance() {
        return new IdeLogsFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        logAdapter = new LogAdapter();
        logger     = new Logger(Logger.LogClass.IDE);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup viewgroup,
                             Bundle savedInstanceState) {
        binding = FragmentIdeLogsBinding.inflate(inflater, viewgroup, false);
        binding.ideLogsViewFlipper.setDisplayedChild(1);
        binding.clearIdeLogsFab.setVisibility(View.GONE);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        model =
            new ViewModelProvider(requireActivity() /*shared activity scope*/).get(MainViewModel.class);
        logger.attach(requireActivity() /*shared activity scope*/);
        binding.ideLogRecyclerview.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.ideLogRecyclerview.setAdapter(logAdapter);
        binding.ideLogRecyclerview.setHasFixedSize(true);
        listener = new RecyclerViewOnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy < 0) {
                    binding.clearIdeLogsFab.extend();
                } else if (dy > 0) {
                    binding.clearIdeLogsFab.shrink();
                }
            }
        };
        binding.ideLogRecyclerview.addOnScrollListener(listener);

        model
            .getIDELogs()
            .observe(getViewLifecycleOwner(), this::updateLayout);
        binding.clearIdeLogsFab.setOnClickListener(v -> clearLogs());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        this.binding = null;
    }

    private void updateLayout(ArrayList<Log> logs) {
        if (logs == null) return;

        if (logs.isEmpty()) {
            binding.ideLogsViewFlipper.setDisplayedChild(1);
            binding.clearIdeLogsFab.setVisibility(View.GONE);
        } else {
            binding.ideLogsViewFlipper.setDisplayedChild(0);
            binding.clearIdeLogsFab.setVisibility(View.VISIBLE);
            logAdapter.submitList(logs);
            scrollToLastItem();
        }
    }

    private void scrollToLastItem() {
        int position = logAdapter.getItemCount();
        if (position > 0) {
            binding.ideLogRecyclerview.scrollToPosition(position - 1);
        }
    }

    private void clearLogs() {
        logger.clear();
        logAdapter.notifyDataSetChanged();
    }
}
