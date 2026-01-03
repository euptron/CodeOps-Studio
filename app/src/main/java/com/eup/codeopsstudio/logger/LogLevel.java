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

package com.eup.codeopsstudio.logger;

import com.eup.codeopsstudio.IdeApplication;
import com.eup.codeopsstudio.R;

/**
 * @author Etido Peter
 */
public enum LogLevel {
  WARN(IdeApplication.getInstance().getString(R.string.warning).toUpperCase()),
  INFO(IdeApplication.getInstance().getString(R.string.info).toUpperCase()),
  DEBUG(IdeApplication.getInstance().getString(R.string.debug).toUpperCase()),
  ERROR(IdeApplication.getInstance().getString(R.string.error).toUpperCase());

  public final String level;

  LogLevel(String level) {
    this.level = "[" + level + "]";
  }

  public static String getLevel(LogLevel level) {
    for (LogLevel value : values()) {
      if (value.level.equals(level.level)) {
        return value.level;
      }
    }
    return "INVALID-LOG-LEVEL";
  }
}
