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
 * questions or need additional information. Email: euptron@gmail.com
 */

package com.eup.codeopsstudio.palette.providers;

import android.content.Context;
import com.eup.codeopsstudio.palette.PaletteItem;
import java.util.ArrayList;
import java.util.List;

/**
 * A command provider responsible for file navigation.
 *
 * <p>This provider is triggered by an empty string (default mode) and searches through the
 * project's file structure. It typically utilizes a {@link
 * com.eup.codeopsstudio.palette.utils.CommandTrie} for high-performance prefix searching across
 * large file sets.
 *
 * @author Etido Peter
 */
public class FileProvider implements CommandProvider {
  private final Context context;
  private final List<PaletteItem> allFiles = new ArrayList<>();

  public FileProvider(Context context) {
    this.context = context;
    seedFiles();
  }

  @Override
  public String getTrigger() {
    return "";
  }

  @Override
  public List<PaletteItem> getItems(String rawQuery, String contentQuery) {
    List<PaletteItem> results = new ArrayList<>();
    results.add(new PaletteItem("Files"));

    String search = contentQuery.toLowerCase();
    for (PaletteItem item : allFiles) {
      if (item.getTitle().toLowerCase().contains(search)) {
        results.add(item);
      }
    }
    return results;
  }

  private void seedFiles() {
    // Exact copy of your dummy files
    allFiles.add(
        new PaletteItem(
            "f1", "MainActivity.java", "app/src/main", "File", () -> toast("Open Main")));
    allFiles.add(
        new PaletteItem("f2", "colors.xml", "res/values", "File", () -> toast("Open Colors")));

    allFiles.add(
        new PaletteItem("f3", "styles.xml", "res/values", "File", () -> toast("Open Styles")));

    allFiles.add(
        new PaletteItem(
            "f4", "AndroidManifest.xml", "app/src/main", "File", () -> toast("Open Manifest")));
    allFiles.add(
        new PaletteItem("f5", "build.gradle", "app", "File", () -> toast("Open Build Gradle")));

    allFiles.add(
        new PaletteItem("f6", "strings.xml", "res/values", "File", () -> toast("Open Strings")));

    allFiles.add(
        new PaletteItem(
            "f7", "activity_main.xml", "res/layout", "File", () -> toast("Open Layout")));
    allFiles.add(
        new PaletteItem(
            "f8", "MyApplication.java", "app/src/main", "File", () -> toast("Open Application")));
    allFiles.add(
        new PaletteItem("f9", "dimens.xml", "res/values", "File", () -> toast("Open Dimens")));

    allFiles.add(
        new PaletteItem(
            "f10", "gradle.properties", "project", "File", () -> toast("Open Gradle Properties")));
    allFiles.add(
        new PaletteItem(
            "f11", "settings.gradle", "project", "File", () -> toast("Open Settings Gradle")));
    allFiles.add(
        new PaletteItem(
            "f12", "proguard-rules.pro", "app", "File", () -> toast("Open Proguard Rules")));
  }
}
