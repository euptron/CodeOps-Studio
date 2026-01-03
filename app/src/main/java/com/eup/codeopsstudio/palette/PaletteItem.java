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

package com.eup.codeopsstudio.palette;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import java.util.Objects;

/**
 * A data model representing a single row in the command palette.
 *
 * <p>A {@code PaletteItem} can represent either a section header or an executable command. It
 * encapsulates display data (title, subtitle, tags, shortcuts) and the functional logic ({@link
 * Runnable}) to be executed upon selection.
 *
 * <p>This class implements custom {@code equals()} and {@code hashCode()} logic to support
 * efficient diffing by the {@link PaletteAdapter}.
 *
 * @author Etido Peter
 */
public class PaletteItem {

  public static final int TYPE_HEADER = 0;
  public static final int TYPE_COMMAND = 1;

  private final String id;
  private int type;
  private String title;
  private String subtitle;
  private String tag;
  private String shortcut;
  private Runnable action;

  public PaletteItem(String title) {
    this.type = TYPE_HEADER;
    this.title = title;
    this.id = "header_" + title;
  }

  public PaletteItem(String id, String title, Runnable action) {
    this(id, title, null, action);
  }

  public PaletteItem(String id, String title, String subtitle, Runnable action) {
    this(id, title, subtitle, null, action);
  }

  public PaletteItem(String id, String title, String subtitle, String tag, Runnable action) {
    this(id, title, subtitle, tag, null, action);
  }

  public PaletteItem(
      String id, String title, String subtitle, String tag, String shortcut, Runnable action) {
    this.id = id;
    this.type = TYPE_COMMAND;
    this.title = title;
    this.subtitle = subtitle;
    this.tag = tag;
    this.shortcut = shortcut;
    this.action = action;
  }

  public String getTitle() {
    return title;
  }

  public String getSubtitle() {
    return subtitle;
  }

  public String getTag() {
    return tag;
  }

  public String getShortcut() {
    return shortcut;
  }

  public Runnable getAction() {
    return action;
  }

  public int getType() {
    return type;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    PaletteItem that = (PaletteItem) o;
    return id.equals(that.id)
        && Objects.equals(title, that.title)
        && Objects.equals(subtitle, that.subtitle);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, title, subtitle);
  }

  public static final DiffUtil.ItemCallback<PaletteItem> DIFF_CALLBACK =
      new DiffUtil.ItemCallback<PaletteItem>() {
        @Override
        public boolean areItemsTheSame(@NonNull PaletteItem oldItem, @NonNull PaletteItem newItem) {
          return oldItem.id.equals(newItem.id);
        }

        @Override
        public boolean areContentsTheSame(
            @NonNull PaletteItem oldItem, @NonNull PaletteItem newItem) {
          return oldItem.equals(newItem);
        }
      };
}
