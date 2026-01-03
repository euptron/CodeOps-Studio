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

import com.eup.codeopsstudio.util.Wizard;

/**
 * Defines the release types used by the app when comparing versions.
 *
 * <p>Each release type represents a stage in the app’s lifecycle, from early testing to stable
 * public release.
 *
 * <p>Defines ordering: ALPHA < BETA < RC < PR < STABLE
 *
 * @author Etido Peter
 */
public enum ReleaseType {

  /**
   * A version of the app that's in early development. It's usually not stable and might have
   * several bugs.
   */
  ALPHA(1, "alpha"),

  /**
   * A more polished version compared to alpha, released to a limited group of users for testing and
   * feedback. Bugs are still expected
   */
  BETA(2, "beta"),

  /**
   * A near-final version that's considered stable and ready for release unless any critical issues
   * are found.
   *
   * <p>release-candidate
   */
  RELEASE_CANDIDATE(3, "rc"),

  /**
   * A version of the app that's released before the stable version.
   *
   * <p>pre-release
   */
  PRE_RELEASE(4, "pr"),

  /**
   * The official version of the app that's intended for the general public. It has undergone
   * testing and bug fixing.
   */
  STABLE(5, "stable");

  private final int priority;
  private final String key;

  ReleaseType(int priority, String key) {
    this.priority = priority;
    this.key = key;
  }

  public int getPriority() {
    return priority;
  }

  public String getKey() {
    return key;
  }

  @Override
  public String toString() {
    return key;
  }

  public static ReleaseType fromName(String name) {
    if (Wizard.isEmpty(name)) {
      return ALPHA;
    }

    for (ReleaseType type : values()) {
      if (type.name().equalsIgnoreCase(name)) {
        return type;
      }
    }
    return ALPHA;
  }

  public static ReleaseType fromLabel(String label) {
    if (Wizard.isEmpty(label)) {
      return STABLE;
    }

    String lower = label.toLowerCase();

    for (ReleaseType type : values()) {
      if (lower.startsWith(type.key)) {
        return type;
      }
    }
    return STABLE;
  }
}
