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

import androidx.annotation.NonNull;
import com.eup.codeopsstudio.common.io.comparator.AbstractComparator;
import com.eup.codeopsstudio.common.io.comparator.ReverseComparator;
import java.util.Comparator;
import java.util.List;
import org.apache.commons.io.IOCase;

/**
 * Compare the <strong>names</strong> of two {@link PluginItem}
 *
 * @author Etido Peter
 */
public class PluginItemComparator extends AbstractComparator<PluginItem> {

  public static final Comparator<PluginItem> SENSITIVE = newInstance(IOCase.SENSITIVE);
  public static final Comparator<PluginItem> SENSITIVE_REVERSE = new ReverseComparator<>(SENSITIVE);
  public static final Comparator<PluginItem> INSENSITIVE = newInstance(IOCase.INSENSITIVE);
  public static final Comparator<PluginItem> INSENSITIVE_REVERSE =
      new ReverseComparator<>(INSENSITIVE);
  public static final Comparator<PluginItem> SYSTEM = newInstance(IOCase.SYSTEM);
  public static final Comparator<PluginItem> SYSTEM_REVERSE = new ReverseComparator<>(SYSTEM);

  private final IOCase ioCase;

  public static PluginItemComparator newInstance(final IOCase ioCase) {
    return new PluginItemComparator(ioCase);
  }

  public PluginItemComparator(final IOCase ioCase) {
    this.ioCase = IOCase.value(ioCase, IOCase.SENSITIVE);
  }

  @Override
  public int compare(final PluginItem o1, final PluginItem o2) {
    return ioCase.checkCompareTo(o1.getName(), o2.getName());
  }

  @NonNull
  @Override
  public String toString() {
    return super.toString() + "[ioCase=" + ioCase + "]";
  }

  @Override
  public List<PluginItem> sort(final List<PluginItem> types) {
    return super.sort(types);
  }
}
