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
 
   package com.eup.codeopsstudio.common.util;

import android.os.Build;

public class SDKUtil {

  public enum API {
    ANDROID_5,
    ANDROID_6,
    ANDROID_7,
    ANDROID_8,
    ANDROID_9,
    ANDROID_10,
    ANDROID_11,
    ANDROID_12
  }

  public static boolean isAtLeast(API api) {
    if (api == API.ANDROID_5) {
      return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    } else if (api == API.ANDROID_6) {
      return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    } else if (api == API.ANDROID_7) {
      return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N;
    } else if (api == API.ANDROID_8) {
      return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
    } else if (api == API.ANDROID_9) {
      return Build.VERSION.SDK_INT >= Build.VERSION_CODES.P;
    } else if (api == API.ANDROID_10) {
      return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q;
    } else if (api == API.ANDROID_11) {
      return Build.VERSION.SDK_INT >= Build.VERSION_CODES.R;
    } else if (api == API.ANDROID_12) {
      return Build.VERSION.SDK_INT >= Build.VERSION_CODES.S;
    }
    return false; // default
  }

  public static boolean isGreaterThan(API api) {
    if (api == API.ANDROID_5) {
      return Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP;
    } else if (api == API.ANDROID_6) {
      return Build.VERSION.SDK_INT > Build.VERSION_CODES.M;
    } else if (api == API.ANDROID_7) {
      return Build.VERSION.SDK_INT > Build.VERSION_CODES.N;
    } else if (api == API.ANDROID_8) {
      return Build.VERSION.SDK_INT > Build.VERSION_CODES.O;
    } else if (api == API.ANDROID_9) {
      return Build.VERSION.SDK_INT > Build.VERSION_CODES.P;
    } else if (api == API.ANDROID_10) {
      return Build.VERSION.SDK_INT > Build.VERSION_CODES.Q;
    } else if (api == API.ANDROID_11) {
      return Build.VERSION.SDK_INT > Build.VERSION_CODES.R;
    } else if (api == API.ANDROID_12) {
      return Build.VERSION.SDK_INT > Build.VERSION_CODES.S;
    }
    return false; // default
  }

  public static boolean is(API api) {
    if (api == API.ANDROID_5) {
      return Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP;
    } else if (api == API.ANDROID_6) {
      return Build.VERSION.SDK_INT == Build.VERSION_CODES.M;
    } else if (api == API.ANDROID_7) {
      return Build.VERSION.SDK_INT == Build.VERSION_CODES.N;
    } else if (api == API.ANDROID_8) {
      return Build.VERSION.SDK_INT == Build.VERSION_CODES.O;
    } else if (api == API.ANDROID_9) {
      return Build.VERSION.SDK_INT == Build.VERSION_CODES.P;
    } else if (api == API.ANDROID_10) {
      return Build.VERSION.SDK_INT == Build.VERSION_CODES.Q;
    } else if (api == API.ANDROID_11) {
      return Build.VERSION.SDK_INT == Build.VERSION_CODES.R;
    } else if (api == API.ANDROID_12) {
      return Build.VERSION.SDK_INT == Build.VERSION_CODES.S;
    }
    return false; // default
  }
}
