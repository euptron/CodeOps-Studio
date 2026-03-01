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

package com.eup.codeopsstudio.plugin;

import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import java.util.Objects;

public class PluginItem {

  private final String id;
  private final String name;
  private final String author;
  private final Drawable icon;
  private final String description;
  private final String downloadUrl;

  public PluginItem(String id, String name, String author, Drawable icon, String description) {
    this(id, name, author, icon, description, null);
  }

  public PluginItem(
      String id,
      String name,
      String author,
      Drawable icon,
      String description,
      String downloadUrl) {
    this.id = id;
    this.name = name;
    this.author = author;
    this.icon = icon;
    this.description = description;
    this.downloadUrl = downloadUrl;
  }

  public String getId() {
    return this.id;
  }

  public String getName() {
    return this.name;
  }

  public String getAuthor() {
    return this.author;
  }

  public Drawable getIcon() {
    return this.icon;
  }

  public String getDescription() {
    return this.description;
  }
  
  public String getDownloadUrl() {
    return this.downloadUrl;
  }
  
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    PluginItem that = (PluginItem) o;
    return id.equals(that.id)
        && Objects.equals(name, that.name)
        && Objects.equals(author, that.author)
        && Objects.equals(description, that.description);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, description);
  }

  public static final DiffUtil.ItemCallback<PluginItem> DIFF_CALLBACK =
      new DiffUtil.ItemCallback<PluginItem>() {
        @Override
        public boolean areItemsTheSame(@NonNull PluginItem oldItem, @NonNull PluginItem newItem) {
          return oldItem.id.equals(newItem.id);
        }

        @Override
        public boolean areContentsTheSame(
            @NonNull PluginItem oldItem, @NonNull PluginItem newItem) {
          return oldItem.equals(newItem);
        }
      };
}
