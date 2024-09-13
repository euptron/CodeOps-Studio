/*************************************************************************
 * This file is part of CodeOps Studio.
 * CodeOps Studio - code anywhere anytime
 * https://github.com/euptron/CodeOps-Studio
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
 
   package com.eup.codeopsstudio.common;

import android.content.Context;
import androidx.annotation.NonNull;

/**
 * Utility to retrive the application context from anywhere
 *
 * @author EUP
 */
public class ContextManager {

  private static Context mContext;

  public static void initialize(@NonNull Context context) {
    mContext = context.getApplicationContext();
  }

  public static Context getApplicationContext() {
    if (mContext == null) {
      throw new IllegalStateException("initialize() hasn't been called.");
    }
    return mContext;
  }

  /**
   * Gets string value from an integer
   *
   * @param i The integer containing a value
   * @return The string value of the integer
   */
  public static String getStringRes(int i) {
    return mContext.getString(i);
  }

  public static String getPackageName() {
    return mContext.getPackageName();
  }
}
