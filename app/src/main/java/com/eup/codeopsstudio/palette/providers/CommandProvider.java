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

package com.eup.codeopsstudio.palette.providers;

import com.eup.codeopsstudio.palette.PaletteItem;
import com.eup.codeopsstudio.util.BaseUtil;
import java.util.List;

/**
 * Defines the contract for a logic module that provides items to the command palette.
 *
 * <p>Implementations of this interface are responsible for handling specific prefixes (triggers)
 * and filtering their internal data sets based on user queries.
 *
 * @author Etido Peter
 */
public interface CommandProvider {
  /**
   * The string that triggers this provider. e.g., ">" for actions, "@syntax" for highlighting, ""
   * for files.
   */
  String getTrigger();

  /**
   * Returns the items matching the query.
   *
   * @param rawQuery The full query typed by user (e.g., "@syntax java")
   * @param contentQuery The query WITHOUT the trigger (e.g., "java")
   */
  List<PaletteItem> getItems(String rawQuery, String contentQuery);

  default void toast(String msg) {
    BaseUtil.toastShort(msg);
  }
}
