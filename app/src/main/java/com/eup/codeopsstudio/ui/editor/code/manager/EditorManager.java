/*************************************************************************
 * This file is part of CodeOps Studio.
 * CodeOps Studio - code anywhere anytime
 * https://github.com/etidoUP/CodeOps-Studio
 * Copyright (C) 2024 EUP
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
 * If you have more questions, feel free to message EUP if you have any
 * questions or need additional information. Email: etido.up@gmail.com
 *************************************************************************/
 
   package com.eup.codeopsstudio.ui.editor.code.manager;

import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.eup.codeopsstudio.common.AsyncTask;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

public class EditorManager {

  public enum EditorType {
    CODE_EDITOR,
    PAGE_EDITOR
  }

  public static class StartSelection {

    int column;
    int index;
    int line;

    public StartSelection(int column, int index, int line) {
      this.column = column;
      this.index = index;
      this.line = line;
    }

    public int getColumn() {
      return this.column;
    }

    public int getIndex() {
      return this.index;
    }

    public int getLine() {
      return this.line;
    }
  }

  public static class EndSelection {
    int column;
    int index;
    int line;

    public EndSelection(int column, int index, int line) {
      this.column = column;
      this.index = index;
      this.line = line;
    }

    public int getColumn() {
      return this.column;
    }

    public int getIndex() {
      return this.index;
    }

    public int getLine() {
      return this.line;
    }
  }

  public static class JsonBuilder {

    private HashMap<String, Object> jsonMap = new HashMap<>();
    private HashMap<String, Object> selectionMap = new HashMap<>();
    private HashMap<String, Object> startSelectionMap = new HashMap<>();
    private HashMap<String, Object> endSelectionMap = new HashMap<>();
    private HashMap<String, Object> selectedEditorMap = new HashMap<>();
    private ArrayList<HashMap<String, Object>> listMap = new ArrayList<>();

    public JsonBuilder addPageEditor(final String id) {
      createMap(jsonMap);
      jsonMap.put("type", EditorType.PAGE_EDITOR);
      jsonMap.put("id", id);
      return this;
    }

    public JsonBuilder addCodeEditor(
        String id, StartSelection startSelection, EndSelection endSelection) {
      createMap(jsonMap);
      jsonMap.put("type", EditorType.CODE_EDITOR);
      jsonMap.put("id", id);
      createMap(selectionMap);
      startSelectionMap.put("column", startSelection.getColumn());
      startSelectionMap.put("index", startSelection.getIndex());
      startSelectionMap.put("line", startSelection.getLine());
      endSelectionMap.put("column", endSelection.getColumn());
      endSelectionMap.put("index", endSelection.getIndex());
      endSelectionMap.put("line", endSelection.getLine());
      selectionMap.put("start", startSelectionMap);
      selectionMap.put("end", endSelectionMap);
      jsonMap.put("selection", selectionMap);
      listMap.add(jsonMap);
      jsonMap.put("allEditors", listMap);
      return this;
    }

    public JsonBuilder addSelectedEditor(String id, EditorType type) {
      selectedEditorMap = new HashMap<>();
      selectedEditorMap.put("type", type);
      selectedEditorMap.put("id", id);
      jsonMap.put("selectedEditor", selectedEditorMap);
      return this;
    }

    public String getLiveEditorsJson() {
      return prettyPrintJson(new Gson().toJson(jsonMap));
    }

    private String prettyPrintJsonAsync(String jsonString, AsyncTask.Callback<String> callback) {
      AsyncTask.runNonCancelable(
          () -> {
            try {
              return new GsonBuilder()
                  .setPrettyPrinting()
                  .create()
                  .toJson(JsonParser.parseString(jsonString));
            } catch (Exception e) {
              Log.e(
                  "EditorManager.JsonBuilder",
                  "Error occurred when pretty printing json:" + e.getMessage());
              return null;
            }
          },
          callback);

      return jsonString; // Or any appropriate value based on your requirements
    }

    // New public method
    public String prettyPrintJson(String jsonString) {
      // Create a CompletableFuture to capture the result
      CompletableFuture<String> resultFuture = new CompletableFuture<>();
      // Use the private method to perform the pretty printing
      prettyPrintJsonAsync(
          jsonString,
          new AsyncTask.Callback<String>() {
            @Override
            public void onComplete(String result) {
              resultFuture.complete(result);
            }
          });
      try {
        // Wait for the result and return it
        return resultFuture.get();
      } catch (Exception e) {
        Log.e(
            "EditorManager.JsonBuilder",
            "Error occurred during pretty printing: " + e.getMessage());
        return jsonString;
      }
    }

    private void createMap(HashMap<String, Object> hashMap) {
      hashMap.clear();
    }
  }
}
