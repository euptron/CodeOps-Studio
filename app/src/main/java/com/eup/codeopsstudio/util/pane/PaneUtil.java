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

package com.eup.codeopsstudio.util.pane;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;

import com.eup.codeopsstudio.common.util.PathResolver;
import com.eup.codeopsstudio.pane.EditorPane;
import com.eup.codeopsstudio.pane.Pane;
import com.eup.codeopsstudio.pane.RandomAccessPane;
import com.eup.codeopsstudio.pane.TextPane;
import com.eup.codeopsstudio.ui.editor.code.CodeEditorPane;
import com.eup.codeopsstudio.ui.editor.panes.WebViewPane;
import com.eup.codeopsstudio.ui.editor.panes.WelcomePane;
import com.eup.codeopsstudio.ui.settings.PreferencesFragment;
import com.eup.codeopsstudio.ui.settings.SettingsPane;
import com.eup.codeopsstudio.util.BaseUtil;
import com.google.android.material.tabs.TabLayout.Tab;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class PaneUtil {

    public static void addPaneTab(final Pair<Tab, Pane> pair,
        MutableLiveData<List<Pair<Tab, Pane>>> panesLiveData) {
        final List<Pair<Tab, Pane>> panes = panesLiveData.getValue();
        Objects.requireNonNull(panes).add(new Pair<>(pair.first, pair.second));
        panesLiveData.setValue(panes);
    }

    public static boolean containsPane(List<Pair<Tab, Pane>> paneTabs, Pane pane) {
        if (paneTabs != null) {
            for (Pair<Tab, Pane> temp : paneTabs) {
                if (temp.second.equals(pane)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean containsPane(List<Pair<Tab, Pane>> paneTabs, Class<?> pane) {
        if (paneTabs != null) {
            for (Pair<Tab, Pane> temp : paneTabs) {
                if (temp.second.getClass().getName().equals(pane.getName())) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void destroyPanes(List<Pair<Tab, Pane>> paneTabs) {
        var iterator = paneTabs.iterator();
        if (iterator.hasNext()) {
            do {
                var pair = iterator.next();
                if (pair != null) pair.second.destroy();
            } while (iterator.hasNext());
        }
    }

    public static int findTabIndex(List<Pair<Tab, Pane>> paneTabs, Tab tab) {
        if (paneTabs == null || tab == null) return -1;

        for (int i = 0; i < paneTabs.size(); i++) {
            if (paneTabs.get(0).first == tab) return i;
        }
        return -1; // Tab not found
    }

    public static Pair<Tab, Pane> getPair(List<Pair<Tab, Pane>> paneTabs, Tab tab) {
        Pair<Tab, Pane> found = null;

        if (tab == null) return found;

        for (Pair<Tab, Pane> pair : paneTabs) {
            if (pair.first.equals(tab)) {
                found = pair;
                break;
            }
        }
        return found;
    }

    public static Pair<Tab, Pane> getPair(List<Pair<Tab, Pane>> paneTabs, Pane pane) {
        Pair<Tab, Pane> found = null;

        if (pane == null) return found;

        for (Pair<Tab, Pane> pair : paneTabs) {
            if (pair.second.equals(pane)) {
                found = pair;
                break;
            }
        }
        return found;
    }

    /**
     * Gets the current Pane and Tab opened in the editor
     *
     * @return The opened Pane and Tab pair
     */
    public static Pair<Tab, Pane> getPaneTab(MutableLiveData<List<Pair<Tab, Pane>>> panesLiveData,
        final int position) {
        return Objects.requireNonNull(panesLiveData.getValue()).get(position);
    }

    /**
     * Gets the current size of all opened Pane and TabLayout.Tab
     *
     * @return The size of opened Pane Tabs Pair of the editor
     */
    public static int getPaneTabSize(MutableLiveData<List<Pair<Tab, Pane>>> panesLiveData) {
        return Objects.requireNonNull(panesLiveData.getValue().size());
    }

    /**
     * Gets a {@code List} of the opened Pane and TabLayout.Tab pair of the editor
     *
     * @return The {@code List} {@code Pair} of opened Pane and TabLayout.Tab
     */
    @NonNull
    public static List<Pair<Tab, Pane>> getPaneTabs(
        MutableLiveData<List<Pair<Tab, Pane>>> panesLiveData) {
        return panesLiveData.getValue() == null ? new ArrayList<>() : panesLiveData.getValue();
    }

    public static String getUniqueTabTitle(@NonNull List<Pane> panes, @NonNull File currentFile) {
        int sameFileNameCount = 0;
        PathResolver<File> builder = new PathResolver<>("", "/");

        for (var pane : Objects.requireNonNull(panes)) {
            if (pane instanceof CodeEditorPane editor) {
                File openFile = editor.getFile();
                if (openFile.getName().equals(currentFile.getName())) {
                    sameFileNameCount++;
                }
                builder.addPath(openFile, openFile.getPath());
            }
        }

        if (sameFileNameCount > 1) {
            return builder.getShortPath(currentFile);
        } else {
            return currentFile.getName();
        }
    }

    /**
     * Check if no Pane and TabLayout.Tab pair exist in the editor
     *
     * @return true if no Pane and TabLayout.Tab pair exist and false if there exists
     */
    public static boolean isPaneTabsEmpty(MutableLiveData<List<Pair<Tab, Pane>>> panesLiveData) {
        return panesLiveData.getValue() == null || panesLiveData.getValue().isEmpty();
    }

    /**
     * @return A list of loaded panes from persisted json
     */
    public static LinkedList<Pane> loadPanes(String json, Context context, LifecycleOwner owner) {
        if (json == null || context == null || owner == null) return new LinkedList<Pane>();

        Gson gson = new Gson();
        LinkedList<Pane> loadedPanes = new LinkedList<>();
        Type type = new TypeToken<List<LinkedTreeMap<String, Object>>>() { }.getType();

        List<LinkedTreeMap<String, Object>> linkedTreeMapList = gson.fromJson(json, type);

        if (linkedTreeMapList != null && !linkedTreeMapList.isEmpty()) {
            for (LinkedTreeMap<String, Object> treeMap : linkedTreeMapList) {
                String identifier = treeMap.get("uuid").toString();
                String jsonArguments = new Gson().toJson(treeMap);

                loadedPanes.add(createPane(context, owner, new RandomAccessPane(jsonArguments,
                    identifier)));
            }
        }
        return loadedPanes;
    }

    /**
     * @author Etido Peter
     */
    public static Pane createPane(Context context, LifecycleOwner owner,
        RandomAccessPane randomAccessPane) {
        Pane pane = null;
        try {
            Gson gson = new Gson();
            Type type = new TypeToken<LinkedTreeMap<String, Object>>() { }.getType();
            LinkedTreeMap<String, Object> treeMap = gson.fromJson(randomAccessPane.getArguments()
                , type);

            String title = treeMap.get(Pane.KEY_TITLE).toString();
            String clazz = treeMap.get(Pane.KEY_CLASS_NAME).toString();
            boolean pinned = (boolean) treeMap.get(Pane.KEY_PINNED);
            boolean selected = (boolean) treeMap.get(Pane.KEY_SELECTED);
            String identity = treeMap.get(Pane.KEY_UUID).toString();
            UUID uuid = UUID.fromString(identity);

            if (clazz.equals(TextPane.class.getSimpleName())) {
                TextPane textPane = new TextPane(context, title, false);
                textPane.addArguments(Pane.PaneConstants.TEXT_PANE_ARGUMENT_KEY, treeMap
                    .get(Pane.PaneConstants.TEXT_PANE_ARGUMENT_KEY).toString());
                pane = textPane;
            } else if (clazz.equals(EditorPane.class.getSimpleName())) {
                EditorPane editorPane = new EditorPane(context, title, false);
                editorPane.addArguments(Pane.PaneConstants.EDITOR_PANE_ARGUMENT_KEY, treeMap
                    .get(Pane.PaneConstants.EDITOR_PANE_ARGUMENT_KEY).toString());
                pane = editorPane;
            } else if (clazz.equals(SettingsPane.class.getSimpleName())) {
                SettingsPane sp = new SettingsPane(context, title, false,
                    PreferencesFragment.newInstance());
                // invoke before Pane#createView()
                sp.attach(owner);
                pane = sp;
            } else if (clazz.equals(WelcomePane.class.getSimpleName())) {
                pane = new WelcomePane(context, title, false);
            } else if (clazz.equals(WebViewPane.class.getSimpleName())) {
                WebViewPane webViewPane = new WebViewPane(context, title, false);
                webViewPane.addArguments("preview_file_path", treeMap.get("preview_file_path")
                                                                     .toString());
                webViewPane.addArguments("isZoomAble", (boolean) treeMap.get("isZoomAble"));
                webViewPane.addArguments("isDeskTopMode", (boolean) treeMap.get("isDeskTopMode"));
                pane = webViewPane;
            } else if (clazz.equals(CodeEditorPane.class.getSimpleName())) {
                CodeEditorPane codeEditorPane = new CodeEditorPane(context, title, false);
                codeEditorPane.setFile(new File(treeMap.get("file_path").toString()));
                codeEditorPane.addArguments("left_column", treeMap.get("left_column"));
                codeEditorPane.addArguments("left_line", treeMap.get("left_line"));
                codeEditorPane.addArguments("editor_content", treeMap.get("editor_content")
                                                                     .toString());
                pane = codeEditorPane;
            }
            pane.setPinned(pinned);
            pane.setSelected(selected);
            pane.setUUID(uuid);
            return pane;
        } catch (RuntimeException e) {
            BaseUtil.toastShort(e.getLocalizedMessage());
            throw e;
        }
        //return null;
    }

    public static CodeEditorPane requireCodeEditorPane(Pane pane) {
        return (pane instanceof CodeEditorPane) ? (CodeEditorPane) pane : null;
    }

    public static EditorPane requireEditorPane(Pane pane) {
        return (pane instanceof EditorPane) ? (EditorPane) pane : null;
    }

    public static SettingsPane requireSettingsPane(Pane pane) {
        return (pane instanceof SettingsPane) ? (SettingsPane) pane : null;
    }

    public static TextPane requireTextPane(Pane pane) {
        return (pane instanceof TextPane) ? (TextPane) pane : null;
    }

    public static WebViewPane requireWebViewPane(Pane pane) {
        return (pane instanceof WebViewPane) ? (WebViewPane) pane : null;
    }

    public static WelcomePane requireWelcomePane(Pane pane) {
        return (pane instanceof WelcomePane) ? (WelcomePane) pane : null;
    }
}
