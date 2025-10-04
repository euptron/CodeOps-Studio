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

package com.eup.codeopsstudio.ui.editor.actions;

import androidx.annotation.NonNull;

import com.eup.codeopsstudio.common.Constants;
import com.eup.codeopsstudio.editor.ContextualCodeEditor;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A language-aware editor shortcuts wizard
 *
 * @author Etido Peter
 */
public class EditorShortcutWizard {

    private static final String DEFAULT_SEQUENCE_KEY = "default";
    private final Map<String, List<EditorAction>> allSequences;
    private final Map<String, List<EditorAction>> cachedLanguageSequences;
    private ContextualCodeEditor codeEditor;

    public EditorShortcutWizard(ContextualCodeEditor codeEditor, String jsonSequence) {
        this.codeEditor         = codeEditor;
        this.allSequences       = parseAllSequences(jsonSequence);
        cachedLanguageSequences = new ConcurrentHashMap<>();
    }

    @NonNull
    private Map<String, List<EditorAction>> parseAllSequences(String json) {
        if (json == null || json.isEmpty()) return Collections.emptyMap();

        Gson gson = new Gson();
        Type type = new TypeToken<Map<String, List<EditorAction>>>() { }.getType();
        Map<String, List<EditorAction>> parsedMap = gson.fromJson(json, type);
        return parsedMap != null ? parsedMap : Collections.emptyMap();
    }

    public static List<EditorAction> configureTabAction(@NonNull List<EditorAction> actions,
        boolean useTabs, int numberOfTabs) {
        for (EditorAction action : actions) {
            if ("TAB".equals(action.getName())) {
                var indentation =
                    useTabs ? Constants.TAB.repeat(numberOfTabs) : " ".repeat(numberOfTabs);
                action.setValue(indentation);
                break;
            }
        }
        return actions;
    }

    public List<EditorAction> getActions() {
        String currentLanguage = codeEditor.getLanguageExtension();
        return cachedLanguageSequences.computeIfAbsent(currentLanguage,
            this::buildAndConfigureSequence);
    }

    /**
     * Builds the definitive list of actions by selecting the correct language sequence
     * (or the default) and then dynamically configuring any actions that depend on editor state.
     *
     * @param language The language for which to build the sequence (e.g., "java", "python").
     * @return A new, configured list of EditorActions ready for use.
     */
    @NonNull
    private List<EditorAction> buildAndConfigureSequence(String language) {
        List<EditorAction> baseSequence = allSequences.get(language);
        if (baseSequence == null) {
            baseSequence = allSequences.getOrDefault(DEFAULT_SEQUENCE_KEY, Collections.emptyList());
        }

        Objects.requireNonNull(baseSequence);
        List<EditorAction> configuredSequence = new ArrayList<>(baseSequence.size());
        for (EditorAction action : baseSequence) {
            configuredSequence.add(new EditorAction(action.getName(), action.getValue()));
        }
        return configuredSequence;
    }

    public void setEditorContext(ContextualCodeEditor editor) {
        this.codeEditor = editor;
        invalidateCache();
    }

    /**
     * Clears the cache of compiled language sequences. This is called if editor
     * preferences (like tab size or indentation style) are changed by the user,
     * to ensure the TAB key and other dynamic actions are correctly configured on the next request.
     */
    public void invalidateCache() {
        cachedLanguageSequences.clear();
    }
}