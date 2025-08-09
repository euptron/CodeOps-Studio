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

package com.eup.codeopsstudio.editor.langs.textmate.provider;

import com.eup.codeopsstudio.common.ILog;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides language-scope mapping functionality for the code editor.
 *
 * @author Etido Peter
 */
public class JsonLanguageInfoProvider implements LanguageInfoProvider {

    public static final String TAG = "JsonLanguageInfoProvider";
    private final Map<String, String> scopeMap;

    public JsonLanguageInfoProvider(InputStream inputStream) {
        scopeMap = new HashMap<>();
        scopeMap.putAll(loadConfig(inputStream));
    }

    /**
     * Loads language-scope data from a provided InputStream (docs/assets, etc).
     *
     * @param inputStream InputStream containing JSON data (if not from docs/assets).
     * @return The loaded language-scope data.
     */
    private Map<String, String> loadConfig(InputStream inputStream) {
        if (scopeMap != null && !scopeMap.isEmpty()) {
            return scopeMap;// already cached
        }

        try {
            return new Gson().fromJson(readInputStream(inputStream), new TypeToken<Map<String,
                String>>() { }.getType());
        } catch (IOException e) {
            ILog.error(TAG, "Failed to load JSON configuration", e);
        }
        return Collections.emptyMap();
    }

    public String readInputStream(InputStream inputStream) throws IOException {
        StringBuilder data = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String dataRow;
        while ((dataRow = reader.readLine()) != null) {
            data
                .append(dataRow)
                .append("\n");
        }
        reader.close();
        return data.toString();
    }

    @Override
    public String getScope(String extensionEntry) {
        return scopeMap.get(extensionEntry);
    }

    @Override
    public String getLanguageExtension(String scopeEntry) {
        for (Map.Entry<String, String> entry : scopeMap.entrySet()) {
            if (entry
                .getValue()
                .equals(scopeEntry)) {
                return entry.getKey();
            }
        }
        return null;
    }
}
