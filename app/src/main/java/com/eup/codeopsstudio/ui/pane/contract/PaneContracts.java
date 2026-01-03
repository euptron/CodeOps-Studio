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
import java.util.Collections;

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
   * Converts a JSON string to a list of map objects.
   *
   * @param json the JSON string to convert, can be null or empty
   * @return the list-map representation of the JSON string, never null
   */
  public static List<Map<String, Object>> fromJson(@Nullable String json) {
    if (json == null || json.isEmpty()) {
      return new ArrayList<>();
    }

    return new Gson()
        .fromJson(json, new TypeToken<List<LinkedTreeMap<String, Object>>>() {}.getType());
  }

  /**
   * Gets the JSON representation of the persisted panes from the specified {@link
   * SharedPreferences}.
   *
   * @param sharedPreferences the shared preferences to read from, can be null
   * @return the JSON model of the persisted panes, never null
   */
  @NonNull
  public static String getPersistedPanesGsonModel(
      @Nullable final SharedPreferences sharedPreferences) {
    if (sharedPreferences == null) return "{}";

    final String encoded = sharedPreferences.getString(KEY_PERSISTED_PANES, EMPTY_JSON_ARRAY);
    byte[] bytes = EncodeUtils.base64Decode(encoded);
    return new String(bytes);
  }

  private static String encode(String string) {
    return EncodeUtils.base64Encode2String(string.getBytes());
  }

  /**
   * Checks if a {@link Pane} exists in the persisted pane storage.
   *
   * @param pane the pane to check for persistence, can be null
   * @param sharedPreferences the shared preferences to check in, can be null
   * @return true if the pane was persisted in {@link SharedPreferences}, false otherwise
   */
  private static boolean isPersisted(
      @Nullable Pane pane, @Nullable SharedPreferences sharedPreferences) {
    return pane != null
        && sharedPreferences != null
        && pane.getUUID() != null
        && getPersistedPaneTree(sharedPreferences).stream()
            .map(map -> map.get(Pane.KEY_UUID))
            .map(Object::toString)
            .anyMatch(pane.getUUID().toString()::equals);
  }

  /**
   * Converts the JSON model of persisted panes into list-map objects.
   *
   * @param sharedPreferences the shared preferences containing the persisted panes
   * @return a list of persisted pane data, never null
   */
  @NonNull
  private static List<Map<String, Object>> getPersistedPaneTree(
      SharedPreferences sharedPreferences) {
    return Optional.of(getPersistedPanesGsonModel(sharedPreferences))
        .map(PaneContracts::fromJson)
        .orElse(new LinkedList<>());
  }

  private static boolean commitRemoval(SharedPreferences prefs, List<String> uuidsToRemove) {
    if (uuidsToRemove == null || uuidsToRemove.isEmpty()) return false;

    List<Map<String, Object>> persistedPaneStates = getPersistedPaneTree(prefs);
    if (persistedPaneStates.isEmpty()) return false;

    boolean isModified = false;

    for (String uuidToRemove : uuidsToRemove) {
      boolean removed =
          persistedPaneStates.removeIf(
              map -> Objects.equals(uuidToRemove, String.valueOf(map.get(Pane.KEY_UUID))));
      isModified |= removed;
    }

    if (isModified) {
      SharedPreferences.Editor editor = prefs.edit();
      String encodedPanes = encode(new Gson().toJson(persistedPaneStates));
      editor.putString(KEY_PERSISTED_PANES, encodedPanes);
      return editor.commit();
    }

    return false;
  }

  /** A {@link PaneContract} to persist panes via {@link PaneWindow#persistPanes}. */
  public static final class PersistPanes extends PaneContract<List<Pane>, Boolean> {
    private static final String TAG = "PaneContracts#PersistPanes";

    private final SharedPreferences sharedPreferences;

    /**
     * Constructs a contract
     *
     * @param sharedPreferences the {@link SharedPreferences} to store pane information
     * @throws NullPointerException if @param sharedPreferences is null
     */
    public PersistPanes(SharedPreferences sharedPreferences) {
      this.sharedPreferences =
          Objects.requireNonNull(sharedPreferences, TAG + " SharedPreferences must not be null");
    }

    @Override
    public void publish(@NonNull List<Pane> panes, @NonNull Consumer<Boolean> output) {
      ILog.info(TAG, "Attempting to persist panes");

      AsyncTask.runOnUiThread(
          () -> {
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

            AsyncTask.runNonCancelable(
                () -> {
                  SharedPreferences.Editor editor = sharedPreferences.edit();
                  final String json = new Gson().toJson(listMap);
                  editor.putString(KEY_PERSISTED_PANES, encode(json));
                  return editor.commit();
                },
                (result, throwable) -> {
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
   * A {@link PaneContract} to remove a persisted pane via {@link
   * PaneWindow#removePersistedPane(Pane)}.
   */
  public static final class RemovePersistedPane extends PaneContract<Pane, String> {
    private static final String TAG = "PaneContracts#RemovePersistedPane";

    private final SharedPreferences sharedPreferences;

    /**
     * Constructs a contract
     *
     * @param sharedPreferences the {@link SharedPreferences} to remove pane information
     * @throws NullPointerException if @param sharedPreferences is null
     */
    public RemovePersistedPane(SharedPreferences sharedPreferences) {
      this.sharedPreferences =
          Objects.requireNonNull(sharedPreferences, TAG + " SharedPreferences must not be null!");
    }

    @Override
    public void publish(@NonNull Pane pane, @NonNull Consumer<String> output) {
      ILog.info(TAG, "Attempting to remove persisted pane: " + pane.getTID());

      if (pane.getUUID() == null) {
        output.accept("Pane has no UUID");
        return;
      }

      AsyncTask.runNonCancelable(
          () -> {
            List<String> singleId = Collections.singletonList(pane.getUUID().toString());
            return commitRemoval(sharedPreferences, singleId);
          },
          (removed, throwable) -> {
            if (throwable != null) {
              var msg = "Failed to remove persisted pane: " + pane.getTID();
              output.accept(msg + " , " + throwable.getMessage());
              ILog.error(TAG, msg, throwable);
            } else {
              if (Boolean.TRUE.equals(removed)) {
                output.accept("Successfully removed pane:" + pane.getTID());
              } else {
                output.accept(
                    "Failed to remove pane: " + pane.getTID() + " (Not found in storage)");
              }
            }
          });
    }
  }

  /**
   * A {@link PaneContract} to remove multiple persisted panes via {@link
   * PaneWindow#removePersistedPanes(List)}.
   */
  public static final class RemovePersistedPanes extends PaneContract<List<Pane>, String> {
    private static final String TAG = "PaneContracts#RemovePersistedPanes";

    private final SharedPreferences sharedPreferences;

    /**
     * Constructs a contract
     *
     * @param sharedPreferences the {@link SharedPreferences} to remove pane information
     * @throws NullPointerException if @param sharedPreferences is null
     */
    public RemovePersistedPanes(SharedPreferences sharedPreferences) {
      this.sharedPreferences =
          Objects.requireNonNull(sharedPreferences, TAG + " SharedPreferences must not be null!");
    }

    @Override
    public void publish(@NonNull List<Pane> panes, @NonNull Consumer<String> output) {
      AsyncTask.runNonCancelable(
          () -> {
            List<String> uuidsToRemove = getUUIDs(panes);
            if (uuidsToRemove.isEmpty()) return 0;

            boolean success = commitRemoval(sharedPreferences, uuidsToRemove);
            return success ? uuidsToRemove.size() : 0;
          },
          (count, throwable) -> {
            if (throwable != null) {
              String msg = "Failed to remove persisted panes: " + throwable.getMessage();
              ILog.error(TAG, msg, throwable);
              output.accept(msg);
            } else {
              if (count > 0) {
                output.accept("Successfully removed " + count + " panes");
              } else {
                output.accept("No panes were removed (none found in storage)");
              }
            }
          });
    }

    private List<String> getUUIDs(List<Pane> panes) {
      List<String> uuids = new ArrayList<>();
      for (Pane p : panes) {
        if (p != null && p.getUUID() != null) {
          uuids.add(p.getUUID().toString());
        }
      }
      return uuids;
    }
  }

  /** A {@link PaneContract} to restore persisted panes via {@link PaneWindow#restorePanes}. */
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
