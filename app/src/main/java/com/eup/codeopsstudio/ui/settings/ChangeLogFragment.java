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

package com.eup.codeopsstudio.ui.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.eup.codeopsstudio.R;
import com.eup.codeopsstudio.common.AsyncTask;
import com.eup.codeopsstudio.common.Constants;
import com.eup.codeopsstudio.common.ILog;
import com.eup.codeopsstudio.databinding.FragmentChangeLogBinding;
import com.eup.codeopsstudio.ui.settings.api.ChangelogAdapter;
import com.eup.codeopsstudio.ui.settings.api.ChangelogItem;
import com.google.android.material.transition.MaterialSharedAxis;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Etido Peter
 */
public class ChangeLogFragment extends Fragment {

    public static final String TAG = ChangeLogFragment.class.getSimpleName();
    private static final String SHARED_PREF_KEY = Constants.CHANGE_LOG_SHARED_PREF_KEY;
    private FragmentChangeLogBinding binding;
    private SharedPreferences sharedPreferences;
    private updateListener listener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setEnterTransition(new MaterialSharedAxis(MaterialSharedAxis.X, false));
        setExitTransition(new MaterialSharedAxis(MaterialSharedAxis.X, true));
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup viewgroup,
        Bundle savedInstanceState) {
        binding = FragmentChangeLogBinding.inflate(inflater, viewgroup, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // initialize
        sharedPreferences = requireContext().getSharedPreferences(SHARED_PREF_KEY,
            Context.MODE_PRIVATE);

        listener = new updateListener() {
            @Override
            public void onDataLoaded(List<ChangelogItem> changelogList) {
                if (getView() != null) {
                    if (changelogList != null && !changelogList.isEmpty()) {
                        checkIfLoaded(true);
                        Collections.reverse(changelogList);
                        ChangelogAdapter adapter = new ChangelogAdapter(changelogList);
                        binding.changelogList.setLayoutManager(new LinearLayoutManager(getContext()));
                        binding.changelogList.setAdapter(adapter);
                    }
                }
            }

            @Override
            public void onDataFailedToLoad(Exception error) {
                if (getView() != null) {
                    showOfflineAlert(error.getMessage());
                }
            }
        };

        AsyncTask.runNonCancelable(this::loadSavedLogs, (cachedData, throwable) -> {
            if (cachedData != null) {
                if (cachedData.isEmpty()) {
                    if (hasInternetConnection()) {
                        showSyncingAlert();
                        fetchLogs();
                    } else {
                        showOfflineAlert(getString(R.string.offline_summary));
                    }
                } else {
                    if (hasInternetConnection()) {
                        showSyncingAlert();
                        fetchLogs();
                    } else {
                        // load saved logs
                        listener.onDataLoaded(cachedData);
                    }
                }
            }
            if (throwable != null) listener.onDataFailedToLoad(((Exception) throwable));
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        this.binding = null;
    }

    private boolean hasInternetConnection() {
        ConnectivityManager connectivityManager =
            (ConnectivityManager) requireContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    public static ChangeLogFragment newInstance() {
        return new ChangeLogFragment();
    }

    private void checkIfLoaded(boolean isLoaded) {
        if (isLoaded) {
            binding.changelogList.setVisibility(View.VISIBLE);
            binding.alertHeader.setVisibility(View.GONE);
            if (binding.alertHeader.getVisibility() == View.VISIBLE) {
                binding.alertHeader.setVisibility(View.GONE); // Hide the offline layout
            }
            if (binding.alertHeader.getVisibility() == View.VISIBLE) {
                binding.retryButton.setVisibility(View.GONE); // hide retry button on data loaded
            }
        } else {
            binding.changelogList.setVisibility(View.GONE);
            binding.alertHeader.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Make a request
     */
    private void fetchLogs() {
        RequestQueue requestQueue = Volley.newRequestQueue(requireContext());
        // request check for outdated data
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET,
            Constants.DEFAULT_CHANGE_LOG_URL, null, response -> AsyncTask.runNonCancelable(() -> {
            var parsedLogs = parseLogs(response);
            saveToSharedPreferences(parsedLogs);
            return parsedLogs;
        }, (parsedItems, throwable) -> {
            if (parsedItems != null) {
                listener.onDataLoaded(parsedItems);
            }
            if (throwable != null) listener.onDataFailedToLoad(((Exception) throwable));
        }), error -> requireActivity().runOnUiThread(() -> listener.onDataFailedToLoad(error)));
        requestQueue.add(request);
    }

    /**
     * Loads saved app change logs
     *
     * @return the changes as list
     */
    private List<ChangelogItem> loadSavedLogs() throws JSONException {
        String jsonData = sharedPreferences.getString(SHARED_PREF_KEY, null);
        if (jsonData != null) {
            List<ChangelogItem> changelogList = new ArrayList<>();
            JSONArray jsonArray = new JSONArray(jsonData);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String versionName = jsonObject.getString("versionName");
                String description = jsonObject.getString("description");
                long releaseDate = jsonObject.getLong("releaseDate");
                ChangelogItem.ReleaseType releaseType =
                    ChangelogItem.ReleaseType.get(jsonObject.getString("releaseType"));
                boolean hasVersionName = jsonObject.getBoolean("hasVersionName");
                boolean supportsHtml = jsonObject.getBoolean("supportsHtml");
                ChangelogItem item = new ChangelogItem(versionName, description, releaseDate,
                    hasVersionName, releaseType);
                item.setSupportsHtml(supportsHtml);
                changelogList.add(item);
            }
            return changelogList;
        }
        return Collections.emptyList();
    }

    /**
     * Parses a json object from a successful JO request
     *
     * @param response the json object
     * @return a list of ChangelogItem
     */
    @NonNull
    private List<ChangelogItem> parseLogs(@NonNull JSONObject response) {
        List<ChangelogItem> changelogList = new ArrayList<>();
        JSONArray changelogArray = response.optJSONArray("changelog");
        if (changelogArray != null) {
            for (int i = 0; i < changelogArray.length(); i++) {
                JSONObject changelogObject = changelogArray.optJSONObject(i);
                if (changelogObject != null) {
                    String versionName = changelogObject.optString("versionName", "No Title");
                    String description = changelogObject.optString("description", "No Description");
                    long releaseDate = changelogObject.optLong("releaseDate", 0);
                    ChangelogItem.ReleaseType releaseType =
                        ChangelogItem.ReleaseType.get(changelogObject.optString("releaseType", ""));
                    boolean hasVersionName = changelogObject.optBoolean("hasVersionName", false);
                    boolean supportsHtml = changelogObject.optBoolean("supportsHtml", false);
                    ChangelogItem item = new ChangelogItem(versionName, description, releaseDate,
                        hasVersionName, releaseType);
                    item.setSupportsHtml(supportsHtml);
                    changelogList.add(item);
                }
            }
        }
        return changelogList;
    }

    /**
     * Saves a list of app change logs to shared-preferences
     *
     * @param changelogList the log list to save
     * @throws JSONException if error JSON error occurs
     */
    private void saveToSharedPreferences(
        @NonNull List<ChangelogItem> changelogList) throws JSONException {
        JSONArray jsonArray = new JSONArray();
        for (ChangelogItem item : changelogList) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("versionName", item.getVersionName());
            jsonObject.put("description", item.getDescription());
            jsonObject.put("releaseDate", item.getReleaseDate());
            jsonObject.put("releaseType", item.getReleaseType());
            jsonObject.put("hasVersionName", item.hasVersionName());
            jsonObject.put("supportsHtml", item.getSupportsHtml());
            jsonArray.put(jsonObject);
        }
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SHARED_PREF_KEY, jsonArray.toString());
        editor.apply();
    }

    private void showOfflineAlert(String errorMessage) {
        binding.getRoot().post(() -> {
            binding.alertIcon.setImageResource(R.drawable.ic_signal_off);
            binding.alertTitle.setText(R.string.failed_sync_change_log_title);
            binding.alertMessage.setText(getString(R.string.failed_sync_change_log_summ));
            binding.retryButton.setVisibility(View.VISIBLE);
            binding.retryButton.setOnClickListener(v -> fetchLogs());
            checkIfLoaded(false);
        });
        ILog.error(TAG, errorMessage);
    }

    private void showSyncingAlert() {
        binding.alertIcon.setImageResource(R.drawable.ic_cached);
        binding.alertTitle.setText(R.string.sync_change_log_title);
        binding.alertMessage.setText(R.string.sync_change_log_summ);
        binding.retryButton.setVisibility(View.GONE); // Hide retry button
    }

    public interface updateListener {
        void onDataFailedToLoad(Exception error);

        void onDataLoaded(List<ChangelogItem> changelogList);
    }
}
