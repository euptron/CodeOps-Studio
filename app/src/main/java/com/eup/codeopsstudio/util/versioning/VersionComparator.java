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

import com.eup.codeopsstudio.common.io.comparator.AbstractComparator;
import java.util.Comparator;

/**
 * Compares application version strings using semantic rules.
 *
 * @author Etido Peter
 */
public final class VersionComparator extends AbstractComparator<String> {

  @Override
  public int compare(String v1, String v2) {
    return compareVersions(v1, v2);
  }

  private int compareVersions(String v1, String v2) {
    v1 = normalize(v1);
    v2 = normalize(v2);

    String[] p1 = v1.split("-", 2);
    String[] p2 = v2.split("-", 2);

    // compare numeric core (1.2.3)
    int core = compareCore(p1[0], p2[0]);
    if (core != 0) return core;

    // compare labels (alpha < beta < rc < qa < stable)
    String l1 = (p1.length > 1) ? p1[1] : "";
    String l2 = (p2.length > 1) ? p2[1] : "";
    return compareLabel(l1, l2);
  }

  private String normalize(String v) {
    return v.trim().toLowerCase().replace(" ", "-");
  }

  private int compareCore(String c1, String c2) {
    String[] a1 = c1.split("\\.");
    String[] a2 = c2.split("\\.");

    int max = Math.max(a1.length, a2.length);

    for (int i = 0; i < max; i++) {
      int x = (i < a1.length) ? parseInt(a1[i]) : 0;
      int y = (i < a2.length) ? parseInt(a2[i]) : 0;

      if (x != y) return x - y;
    }
    return 0;
  }

  private int parseInt(String s) {
    try {
      return Integer.parseInt(s);
    } catch (Exception e) {
      return 0;
    }
  }

  private int compareLabel(String l1, String l2) {
    ReleaseType r1 = ReleaseType.fromLabel(l1);
    ReleaseType r2 = ReleaseType.fromLabel(l2);
    return r1.getPriority() - r2.getPriority();
  }
}
