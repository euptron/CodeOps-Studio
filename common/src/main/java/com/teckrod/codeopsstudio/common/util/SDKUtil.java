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
    ANDROID_4(Build.VERSION_CODES.KITKAT),   
    ANDROID_5(Build.VERSION_CODES.LOLLIPOP),
    ANDROID_6(Build.VERSION_CODES.M),
    ANDROID_7(Build.VERSION_CODES.N),
    ANDROID_8(Build.VERSION_CODES.O),
    ANDROID_9(Build.VERSION_CODES.P),
    ANDROID_10(Build.VERSION_CODES.Q),
    ANDROID_11(Build.VERSION_CODES.R),
    ANDROID_12(Build.VERSION_CODES.S),
    ANDROID_13(Build.VERSION_CODES.TIRAMISU),
    ANDROID_14(Build.VERSION_CODES.UPSIDE_DOWN_CAKE);
   // ANDROID_15(Build.VERSION_CODES.VANILLA_ICE_CREAM);

    private final int versionCode;

    API(int versionCode) {
      this.versionCode = versionCode;
    }

    public int getVersionCode() {
      return versionCode;
    }
  }

  public static boolean isAtLeast(API api) {
    return Build.VERSION.SDK_INT >= api.getVersionCode();
  }

  public static boolean isGreaterThan(API api) {
    return Build.VERSION.SDK_INT > api.getVersionCode();
  }

  public static boolean is(API api) {
    return Build.VERSION.SDK_INT == api.getVersionCode();
  }
}
