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

package com.eup.codeopsstudio.pane;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;

import org.json.JSONObject;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Creates and manages {@link Pane} instances from JSON or Map arguments. Provides type-safe
 * conversion and bulk restoration capabilities.
 *
 * @author Etido Peter
 * @see Pane
 * @since 1.0.3
 */
public interface PaneFactory {
    String TAG = PaneFactory.class.getSimpleName();

    /**
     * Creates a pane from JSON data.
     *
     * @throws IllegalArgumentException If JSON is malformed.
     */
    @NonNull
    Pane createPane(@NonNull JSONObject json);

    /**
     * Creates a pane from a {@link RandomAccessPane}
     *
     * @param randomAccessPane Non-null serialized pane
     */
    @NonNull
    Pane createPane(@NonNull RandomAccessPane randomAccessPane);

    /**
     * Creates a pane from a structured argument map.
     *
     * @param arguments Non-null map of key-value pairs.
     * @throws UnsupportedOperationException If an unsupported pane is passed in arguments.
     */
    @NonNull
    Pane createPane(@NonNull Map<String, Object> arguments) throws UnsupportedOperationException;

    /**
     * Returns an immutable view of default pane arguments.
     */
    @NonNull
    Map<String, Object> getArguments();

    @Nullable
    Context getContext();

    /**
     * Extracts a pane's UUID from JSON (null if missing/invalid).
     */
    @Nullable
    UUID getID(@NonNull JSONObject json);

    @Nullable
    LifecycleOwner getLifecycleOwner();

    /**
     * Loads panes from JSON-formatted data.
     *
     * @return Non-null list of panes (possibly empty).
     */
    @NonNull
    List<Pane> loadPanes(@NonNull String json);

    /**
     * Casts a pane to a specific type or throws.
     *
     * @throws ClassCastException If the pane is of the wrong type.
     */
    @NonNull
    default <T> T requirePane(@NonNull Pane pane, @NonNull Class<T> clazz) {
        T instance = getPane(pane, clazz);
        if (instance == null) {
            throw new ClassCastException(
                "Expected " + clazz.getSimpleName() + ", got " + pane.getClass().getSimpleName());
        }
        return instance;
    }

    /**
     * Safely casts a pane to a specific type.
     *
     * @return Null if the pane is not an instance of the target class.
     */
    @Nullable
    static <T> T getPane(@NonNull Pane pane, @NonNull Class<T> clazz) {
        return clazz.isInstance(pane) ? clazz.cast(pane) : null;
    }
}
