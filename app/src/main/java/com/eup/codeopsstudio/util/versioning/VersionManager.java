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

package com.eup.codeopsstudio.util.versioning;

import com.eup.codeopsstudio.IdeApplication;
import com.eup.codeopsstudio.util.Wizard;

/**
 * Handles versioning
 *
 * @author Etido Peter
 */
public final class VersionManager {

  private static final VersionComparator COMPARATOR = new VersionComparator();

  private VersionManager() {
    // Default
  }

  public static boolean isUpdateAvailable(String latestVersion) {
    return isNewer(latestVersion);
  }

  public static boolean isForceUpdateRequired(String minSupportedVersion) {
    return isNewer(minSupportedVersion);
  }

  private static boolean isNewer(String serverVersion) {
    if (Wizard.isEmpty(serverVersion, true)) return false;
    String currentVersion = Wizard.getAppVersionName(IdeApplication.getGlobalContext());
    return COMPARATOR.compare(serverVersion, currentVersion) > 0;
  }
}
