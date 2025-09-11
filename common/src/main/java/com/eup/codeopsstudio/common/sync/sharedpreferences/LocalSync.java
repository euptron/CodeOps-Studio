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

package com.eup.codeopsstudio.common.sync.sharedpreferences;

import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.eup.codeopsstudio.common.ILog;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Etiido Peter
 */
public class LocalSync implements Sync {

    public static LocalSync IO(){
        return new LocalSync();
    }

    public LocalSync() {
        // Default
    }

    @NonNull
    @Override
    public Map<String, ?> backup(@NonNull SharedPreferences sharedPreferences) {
        var preferencesCache = new HashMap<String, Object>();
        var preferences = sharedPreferences.getAll();

        preferences.forEach((key, value) -> {
            ILog.debug("Preferences ", String.format("%s -> %s", key, value));
            preferencesCache.put(key, value);
        });
        return Collections.unmodifiableMap(preferencesCache);
    }

    @Nullable
    @Override
    public SharedPreferences restore(@NonNull SharedPreferences prefs,
        @NonNull Map<String, ?> cues) {
        cues.forEach((key, value) -> writeToEditor(prefs.edit(), key, value));
        return prefs;
    }

    private void writeToEditor(SharedPreferences.Editor editor, @NonNull String key,
        @NonNull Object value) {
        ILog.debug("Preferences ", String.format(" Restored: %s -> %s", key, value));

        if (value instanceof Boolean o) {
            editor
                .putBoolean(key, o)
                .apply();
        } else if (value instanceof Float o) {
            editor.putFloat(key, o);
        } else if (value instanceof String o) {
            editor.putString(key, o);
        } else if (value instanceof Integer o) {
            editor.putInt(key, o);
        } else if (value instanceof Long o) {
            editor.putLong(key, o);
        } else if (value instanceof Set<?> o) {
            Set<String> set = new HashSet<>();
            for (Object object : o) {
                if (object instanceof String s) {
                    set.add(s);
                    editor.putStringSet(key, set);
                }
            }
        }

        editor.apply();
    }
}