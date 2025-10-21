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
 * questions or need additional information. Email: etido.up@gmail.com
 */

package com.eup.codeopsstudio.ui.pane.contract;

import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.eup.codeopsstudio.common.AsyncTask;
import com.eup.codeopsstudio.common.Constants;
import com.eup.codeopsstudio.common.ILog;
import com.eup.codeopsstudio.common.util.EncodeUtils;
import com.eup.codeopsstudio.pane.Pane;
import com.eup.codeopsstudio.pane.PaneFactory;
import com.eup.codeopsstudio.ui.pane.PaneWindow;
import com.eup.codeopsstudio.ui.pane.factory.PaneFactoryImpl;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * A collection of contracts for managing panes within the {@code PaneWindow}
 *
 * @author Etido Peter
 * @see PaneWindow
 * @see PaneContract
 * @see PaneFactoryImpl
 */
public final class PaneContracts {

    private static final String KEY_PERSISTED_PANES =
        Constants.SharedPreferenceKeys.KEY_PERSISTED_PANES;
    private static final String EMPTY_JSON_ARRAY = "[]";

    private PaneContracts() {
        // No-Instance
    }

    /**
     * Converts a JSON string to a List-Map
     *
     * @return the List-Map representation of a JSON string
     */
    public static List<Map<String, Object>> fromJson(@Nullable String json) {
        if (json == null || json.isEmpty()) {
            return new ArrayList<>();
        }

        return new Gson().fromJson(json,
            new TypeToken<List<LinkedTreeMap<String, Object>>>() { }.getType());
    }

    /**
     * Gets the JSON representation of the persisted panes from the specified {@link
     * SharedPreferences}
     *
     * @return the JSON model of the persisted panes or an empty string if model was invalid
     */
    @NonNull
    public static String getPersistedPanesGsonModel(final SharedPreferences sharedPreferences) {
        final String encoded = sharedPreferences.getString(KEY_PERSISTED_PANES, EMPTY_JSON_ARRAY);
        byte[] bytes = EncodeUtils.base64Decode(encoded);
        return new String(bytes);
    }

    private static String encode(String string) {
        return EncodeUtils.base64Encode2String(string.getBytes());
    }

    /**
     * An {@link PaneContract} to {@link PaneWindow#persistPanes to persist panes}
     */
    public static final class PersistPanes extends PaneContract<List<Pane>, Boolean> {
        private static final String TAG = "PaneContracts#PersistPanes";
        /**
         * {@link SharedPreferences} to store pane information
         */
        private final SharedPreferences sharedPreferences;

        /**
         * Constructs a contract
         *
         * @param sharedPreferences the {@link SharedPreferences} to store pane information
         * @throws NullPointerException if @param sharedPreferences is null
         */
        public PersistPanes(SharedPreferences sharedPreferences) {
            this.sharedPreferences = Objects.requireNonNull(sharedPreferences,
                TAG + " SharedPreferences must not be null");
        }
        
        @Override
        public void publish(@NonNull List<Pane> panes, @NonNull Consumer<Boolean> output) {
            ILog.info(TAG, "Attempting to persist panes");
            
            AsyncTask.runOnUiThread(() -> {
                if (panes.isEmpty()) {
                   output.accept(false);
                   return;
                }
                
                List<Map<String, Object>> listMap = new LinkedList<>();
                
                for (Pane pane : panes) {
                    if (pane != null) {
                        pane.persist(); // persist synchronously
                        listMap.add(pane.getArguments());
                    }
                }
                
                AsyncTask.runNonCancelable(() -> {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    final String json = new Gson().toJson(listMap);
                    editor.putString(KEY_PERSISTED_PANES, encode(json));
                    return editor.commit();
                }, (result, throwable) -> {
                    if (throwable != null) {
                        output.accept(false);
                        ILog.debug(TAG, "Failed to persist panes due to unexpected error", throwable);
                    } else {
                        output.accept(result);
                        ILog.debug(TAG, String.format("Panes persisted = '%s'", result));
                    }
                });
            });
        }
    }

    /**
     * An {@link PaneContract} to {@link PaneWindow#removePersistedPane(Pane)}  to remove the
     * persisted
     * panes}
     */
    public static final class RemovePersistedPane extends PaneContract<Pane, String> {
        private static final String TAG = "PaneContracts#RemovePersistedPane";
        /**
         * {@link SharedPreferences} to store pane information
         */
        private final SharedPreferences sharedPreferences;

        /**
         * Constructs a contract
         *
         * @param sharedPreferences the {@link SharedPreferences} to remove pane information
         * @throws NullPointerException if @param sharedPreferences is null
         */
        public RemovePersistedPane(SharedPreferences sharedPreferences) {
            this.sharedPreferences = Objects.requireNonNull(sharedPreferences,
                TAG + " SharedPreferences must not be null!");
        }

        @Override
        public void publish(@NonNull Pane pane, @NonNull Consumer<String> output) {
            ILog.info(TAG, "Attempting to remove persisted pane: " + pane.getTID());

            AsyncTask.runNonCancelable(() -> {
                if (isPersisted(pane)) {
                    List<Map<String, Object>> persistedPanes = getPersistedPaneTree();
                    persistedPanes.removeIf(map -> Objects.equals(pane.getUUID(),
                        map.get(Pane.KEY_UUID)));
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(KEY_PERSISTED_PANES,
                        encode(new Gson().toJson(persistedPanes)));
                    return editor.commit();
                }
                return false;
            }, (removed, throwable) -> {
                if (throwable != null) {
                    var msg = "Failed to remove persisted panes due to unexpected error: "
                        + pane.getTID();
                    output.accept(msg + " , " + throwable.getMessage());
                    ILog.error(TAG, msg, throwable);
                } else {
                    if (Boolean.TRUE.equals(removed)) {
                        output.accept("Successfully removed pane:" + pane.getTID()
                            + " from persistent storage");
                    } else {
                        output.accept(
                            "Failed to remove pane: " + pane.getTID() + " from persistent storage, persisted = " + isPersisted(pane));
                    }
                }
            });
        }

        /**
         * Checks if a {@link Pane} exists in the persisted pane storage.
         *
         * @return true if @param pane was persisted in {@link SharedPreferences}
         */
        private boolean isPersisted(@Nullable Pane pane) {
            return pane != null && pane.getUUID() != null && getPersistedPaneTree().stream()
                                                                                   .map(map -> map.get(Pane.KEY_UUID))
                                                                                   .anyMatch(pane
                                                                                       .getUUID()
                                                                                       .toString()::equals);
        }

        /**
         * Converts the JSON model of persisted panes into List-Map objects
         *
         * <p>Never returns a null
         *
         * @return a list of persisted pane data, returns empty List-Map if JSON model is invalid
         */
        @NonNull
        private List<Map<String, Object>> getPersistedPaneTree() {
            return Optional.of(getPersistedPanesGsonModel(sharedPreferences))
                           .map(PaneContracts::fromJson).orElse(new LinkedList<>());
        }
    }

    /**
     * An {@link PaneContract} to {@link PaneWindow#restorePanes to restore the persisted
     * panes}
     */
    public static final class LoadPersistedPanes extends PaneContract<String, List<Pane>> {
        private final PaneFactory factory;

        public LoadPersistedPanes(@NonNull PaneFactory factory) {
            this.factory = factory;
        }

        @Override
        public void publish(@NonNull String json, @NonNull Consumer<List<Pane>> output) {
            output.accept(factory.loadPanes(json));
        }
    }
}