/*
 * This file is part of CodeOps Studio.
 * CodeOps Studio - Code anywhere anytime
 * https://github.com/euptron/CodeOps-Studio
 * Copyright (C) 2024-2026 Etido Peter
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

package com.eup.codeopsstudio.ui.pane.factory;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LifecycleOwner;
import com.eup.codeopsstudio.common.ILog;

import com.eup.codeopsstudio.pane.EditorPane;
import com.eup.codeopsstudio.pane.Pane;
import com.eup.codeopsstudio.pane.PaneFactory;
import com.eup.codeopsstudio.pane.RandomAccessPane;
import com.eup.codeopsstudio.pane.TextPane;
import com.eup.codeopsstudio.ui.editor.code.CodeEditorPane;
import com.eup.codeopsstudio.ui.editor.panes.WebViewPane;
import com.eup.codeopsstudio.ui.editor.panes.WelcomePane;
import com.eup.codeopsstudio.ui.pane.PaneWindow;
import com.eup.codeopsstudio.ui.settings.PreferencesFragment;
import com.eup.codeopsstudio.ui.settings.SettingsPane;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Default implementation of {@link PaneFactory}.
 *
 * @author Etido Peter
 * @see PaneFactory
 */
public class PaneFactoryImpl implements PaneFactory {

  private static final Type GSON_TYPE_MAP =
      new TypeToken<LinkedTreeMap<String, Object>>() {}.getType();
  private static final Type GSON_TYPE_LIST_MAP =
      new TypeToken<List<LinkedTreeMap<String, Object>>>() {}.getType();
  private final Fragment fragment;
  private final FragmentActivity activity;
  private final Gson gson = new Gson();
  private final PaneWindow paneWindow;
  private final Context context;
  private final LifecycleOwner owner;

  public PaneFactoryImpl(@NonNull Fragment fragment, @NonNull PaneWindow paneWindow) {
    this.activity = null;
    this.fragment = fragment;
    this.paneWindow = paneWindow;
    this.context = fragment.requireContext();
    this.owner = fragment.getViewLifecycleOwner();
  }

  public PaneFactoryImpl(@NonNull FragmentActivity activity, @NonNull PaneWindow paneWindow) {
    this.fragment = null;
    this.activity = activity;
    this.paneWindow = paneWindow;
    this.context = activity;
    this.owner = activity;
  }

  @NonNull
  @Override
  public Pane createPane(@NonNull JSONObject json) {
    try {
      Type type = new TypeToken<LinkedTreeMap<String, Object>>() {}.getType();
      String jsonString = json.getJSONObject(Pane.KEY_ARGUMENTS).toString();
      LinkedTreeMap<String, Object> map = gson.fromJson(jsonString, type);
      return createPane(map == null ? new LinkedTreeMap<>() : map);
    } catch (JSONException e) {
      throw new IllegalArgumentException("Malformed pane JSON", e);
    }
  }

  @NonNull
  @Override
  public Pane createPane(@NonNull RandomAccessPane randomAccessPane) {
    Type type = new TypeToken<LinkedTreeMap<String, Object>>() {}.getType();
    LinkedTreeMap<String, Object> treeMap = gson.fromJson(randomAccessPane.getArguments(), type);
    return createPane(treeMap);
  }

  @NonNull
  @Override
  public Pane createPane(@NonNull Map<String, Object> arguments)
      throws UnsupportedOperationException {
    final boolean pinned = requireBoolean(Pane.KEY_PINNED, arguments);
    final boolean selected = requireBoolean(Pane.KEY_SELECTED, arguments);
    final String className = requireString(Pane.KEY_CLASS_NAME, arguments);
    final UUID uuid = UUID.fromString(requireString(Pane.KEY_UUID, arguments));

    Pane pane =
        switch (className) {
          case "TextPane" -> createTextPane(arguments);
          case "EditorPane" -> createEditorPane(arguments);
          case "WelcomePane" -> createWelcomePane(arguments);
          case "SettingsPane" -> createSettingsPane(arguments);
          case "WebViewPane" -> createWebViewPane(arguments);
          case "CodeEditorPane" -> createCodeEditorPane(arguments);
          default -> throw new UnsupportedOperationException(
              "Pane creation not implemented for: " + className);
        };

    pane.setPinned(pinned);
    pane.setSelected(selected);
    pane.setUUID(uuid);
    return pane;
  }

  @NonNull
  @Override
  public Map<String, Object> getArguments() {
    return null;
  }

  @Override
  public Context getContext() {
    return context;
  }

  @Nullable
  @Override
  public UUID getID(@NonNull JSONObject json) {
    try {
      return json.has("uuid") && !json.isNull("uuid")
          ? UUID.fromString(json.getString("uuid"))
          : null;
    } catch (JSONException e) {
      throw new IllegalArgumentException("Invalid UUID format in JSON", e);
    }
  }

  @Override
  public LifecycleOwner getLifecycleOwner() {
    return owner;
  }

  @NonNull
  @Override
  public List<Pane> loadPanes(@NonNull String json) {
    LinkedList<Pane> loadedPanes = new LinkedList<>();
    List<LinkedTreeMap<String, Object>> listMap = gson.fromJson(json, GSON_TYPE_LIST_MAP);

    if (listMap != null && !listMap.isEmpty()) {
      for (LinkedTreeMap<String, Object> map : listMap) {
        if (map != null) loadedPanes.add(createPane(map));
      }
    }
    return loadedPanes;
  }

  @NonNull
  @Override
  public <T> T requirePane(@NonNull Pane pane, @NonNull Class<T> clazz) {
    return PaneFactory.super.requirePane(pane, clazz);
  }

  /**
   * Returns a {@code Boolean} value from the {@link Map}.
   *
   * @param key the key assigned to the {@link Boolean} value
   * @param map the map holding the key-value pair
   * @return the boolean value paired with the map key or false if the assigned key does not exist.
   * @see #requireInt(String, Map)
   * @see #requireString(String, Map)
   */
  public static boolean requireBoolean(@NonNull String key, @NonNull Map<String, ?> map) {
    Object object = map.get(key);
    return object != null && (boolean) object;
  }

  /**
   * Returns a {@code String} value from the {@link Map}.
   *
   * @param key the key assigned to the {@link String} value
   * @param map the map holding the key-value pair
   * @return the a string value paired with the map key or an empty string if the assigned key does
   *     not exist.
   * @see #requireInt(String, Map)
   * @see #requireBoolean(String, Map)
   */
  public static String requireString(@NonNull String key, @NonNull Map<String, ?> map) {
    Object object = map.get(key);
    return object != null ? (String) object : "";
  }

  @NonNull
  private Pane createTextPane(@NonNull Map<String, Object> data) {
    final String CONTENT_KEY = Pane.PaneConstants.TEXT_PANE_ARGUMENT_KEY;
    final String title = requireString(Pane.KEY_TITLE, data);
    final String content = requireString(CONTENT_KEY, data);

    var textPane = new TextPane(context, title, false);
    textPane.addArguments(CONTENT_KEY, content);
    return textPane;
  }

  @NonNull
  private Pane createEditorPane(@NonNull Map<String, Object> data) {
    final String CONTENT_KEY = Pane.PaneConstants.EDITOR_PANE_ARGUMENT_KEY;
    final String title = requireString(Pane.KEY_TITLE, data);
    final String content = requireString(CONTENT_KEY, data);

    var editorPane = new EditorPane(context, title, false);
    editorPane.addArguments(CONTENT_KEY, content);
    return editorPane;
  }

  @NonNull
  private Pane createSettingsPane(@NonNull Map<String, Object> data) {
    final String title = requireString(Pane.KEY_TITLE, data);
    final Fragment prefFragment = PreferencesFragment.newInstance();

    var settingsPane = new SettingsPane(context, title, false, prefFragment);
    // invoke before Pane#createView()
    settingsPane.attach(owner);
    return settingsPane;
  }

  @NonNull
  private Pane createWebViewPane(@NonNull Map<String, Object> data) {
    final String title = requireString(Pane.KEY_TITLE, data);
    final boolean zoomable = requireBoolean(WebViewPane.KEY_IS_ZOOMABLE, data);
    final boolean desktopMode = requireBoolean(WebViewPane.KEY_DESKTOP_MODE, data);
    final String previewFilePath = requireString(WebViewPane.KEY_PREVIEW_FILE_PATH, data);

    var webViewPane = new WebViewPane(context, title, false);
    webViewPane.addArguments(WebViewPane.KEY_PREVIEW_FILE_PATH, previewFilePath);
    webViewPane.addArguments(WebViewPane.KEY_IS_ZOOMABLE, zoomable);
    webViewPane.addArguments(WebViewPane.KEY_DESKTOP_MODE, desktopMode);
    return webViewPane;
  }

  @NonNull
  private Pane createWelcomePane(@NonNull Map<String, Object> data) {
    final String title = requireString(Pane.KEY_TITLE, data);
    return new WelcomePane(context, title, false);
  }
  
  @NonNull
  private Pane createCodeEditorPane(@NonNull Map<String, Object> data) {
    final String title = requireString(Pane.KEY_TITLE, data);
    final int leftColumn = requireInt(CodeEditorPane.KEY_LEFT_COLUMN, data);
    final int leftLine = requireInt(CodeEditorPane.KEY_LEFT_LINE, data);
    final String filePath = requireString(CodeEditorPane.KEY_FILE_PATH, data);
    final String fileExtension = requireString(CodeEditorPane.KEY_FILE_EXTENSION, data);
    final String editorContent = requireString(CodeEditorPane.KEY_EDITOR_CONTENT, data);
    final boolean wasDirty = requireBoolean(CodeEditorPane.KEY_WAS_DIRTY, data);
    final long fileMTime = requireLong(CodeEditorPane.KEY_FILE_MTIME, data);
    final long fileSize = requireLong(CodeEditorPane.KEY_FILE_SIZE, data);
    
    var codeEditorPane = new CodeEditorPane(context, title, false);
    codeEditorPane.setFile(new File(filePath));
    codeEditorPane.addArguments(CodeEditorPane.KEY_LEFT_COLUMN, leftColumn);
    codeEditorPane.addArguments(CodeEditorPane.KEY_LEFT_LINE, leftLine);
    codeEditorPane.addArguments(CodeEditorPane.KEY_FILE_PATH, filePath);
    codeEditorPane.addArguments(CodeEditorPane.KEY_FILE_EXTENSION, fileExtension);
    codeEditorPane.addArguments(CodeEditorPane.KEY_EDITOR_CONTENT, editorContent);
    codeEditorPane.addArguments(CodeEditorPane.KEY_WAS_DIRTY, wasDirty);
    codeEditorPane.addArguments(CodeEditorPane.KEY_FILE_MTIME, fileMTime);
    codeEditorPane.addArguments(CodeEditorPane.KEY_FILE_SIZE, fileSize);
    return codeEditorPane;
  }

  /**
   * Returns an {@code Integer} from the {@link Map}.
   *
   * @param key the key assigned to the {@link Integer} value
   * @param map the map holding the key-value pair
   * @return the integer value paired with the map key or 0 if the assigned key does not exist.
   * @throws IllegalArgumentException if the value is not an integer
   * @see #requireString(String, Map)
   * @see #requireBoolean(String, Map)
   * @see #requireLong(String, Map)
   */
  public static int requireInt(@NonNull String key, @NonNull Map<String, ?> map) {
    Object object = map.get(key);

    if (object instanceof Integer o) {
      return o;
    } else if (object instanceof Double d) {
      return d.intValue();
    } else if (object instanceof Number n) {
      return n.intValue();
    } else if (object instanceof String s) {
      try {
        return Integer.parseInt(s.trim());
      } catch (NumberFormatException e) {
        ILog.debug(TAG, "Failed to parse int from string: " + s);
        return 0;
      }
    } else {
      ILog.debug(
          TAG,
          "Expected numeric or string type for int, but got: "
              + (object != null ? object.getClass().getName() : "null"));
      return 0; // fallback for recovery
    }
  }

  /**
   * Returns a {@code Long} from the {@link Map}.
   *
   * @param key the key assigned to the {@link Long} value
   * @param map the map holding the key-value pair
   * @return the long value paired with the map key or 0L if the assigned key does not exist.
   * @throws IllegalArgumentException if the value is not a long-compatible numeric type
   * @see #requireInt(String, Map)
   * @see #requireString(String, Map)
   * @see #requireBoolean(String, Map)
   */
  public static long requireLong(@NonNull String key, @NonNull Map<String, ?> map) {
    Object object = map.get(key);

    if (object instanceof Long o) {
      return o;
    } else if (object instanceof Integer i) {
      return i.longValue();
    } else if (object instanceof Double d) {
      return d.longValue();
    } else if (object instanceof Number n) {
      return n.longValue();
    } else if (object instanceof String s) {
      try {
        return Long.parseLong(s.trim());
      } catch (NumberFormatException e) {
        ILog.debug(TAG, "Failed to parse long from string: " + s);
        return 0L;
      }
    } else {
      ILog.debug(
          TAG,
          "Expected numeric or string type for long, but got: "
              + (object != null ? object.getClass().getName() : "null"));
      return 0L; // fallback for recovery
    }
  }
}
