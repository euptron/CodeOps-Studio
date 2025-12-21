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

package com.eup.codeopsstudio.common;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides a centralized list of Material Design 3 (M3) color attribute keys used throughout
 * CodeOps Studio. This utility class exposes all relevant Material color attribute names as string
 * constants and offers a builder-based method for retrieving them as a consolidated list.
 *
 * <p>The values returned by {@link #buildList()} are intended to be resolved dynamically against
 * {@code com.google.android.material.R.attr} when extracting runtime theme colors, exporting
 * themes, or performing color introspection. This separation allows CodeOps Studio to dynamically
 * access Material color attributes without hard-coding attribute IDs.
 *
 * <p><strong>Usage examples:</strong>
 *
 * <pre>{@code
 * List<String> keys = MaterialColorKeys.buildList();
 * for (String key : keys) {
 *     // Resolve attr ID via reflection and fetch its color
 * }
 * }</pre>
 *
 * @author Etido Peter
 */
public class MaterialColorKeys {

  private static final String COLOR_ON_PRIMARY = "colorOnPrimary";
  private static final String COLOR_PRIMARY_INVERSE = "colorPrimaryInverse";
  private static final String COLOR_PRIMARY_CONTAINER = "colorPrimaryContainer";
  private static final String COLOR_ON_PRIMARY_CONTAINER = "colorOnPrimaryContainer";
  private static final String COLOR_PRIMARY_FIXED = "colorPrimaryFixed";
  private static final String COLOR_PRIMARY_FIXED_DIM = "colorPrimaryFixedDim";
  private static final String COLOR_ON_PRIMARY_FIXED = "colorOnPrimaryFixed";
  private static final String COLOR_ON_PRIMARY_FIXED_VARIANT = "colorOnPrimaryFixedVariant";
  private static final String COLOR_SECONDARY = "colorSecondary";
  private static final String COLOR_SECONDARY_CONTAINER = "colorSecondaryContainer";
  private static final String COLOR_ON_SECONDARY_CONTAINER = "colorOnSecondaryContainer";
  private static final String COLOR_SECONDARY_FIXED = "colorSecondaryFixed";
  private static final String COLOR_SECONDARY_FIXED_DIM = "colorSecondaryFixedDim";
  private static final String COLOR_ON_SECONDARY_FIXED = "colorOnSecondaryFixed";
  private static final String COLOR_ON_SECONDARY_FIXED_VARIANT = "colorOnSecondaryFixedVariant";
  private static final String COLOR_TERTIARY = "colorTertiary";
  private static final String COLOR_ON_TERTIARY = "colorOnTertiary";
  private static final String COLOR_TERTIARY_CONTAINER = "colorTertiaryContainer";
  private static final String COLOR_ON_TERTIARY_CONTAINER = "colorOnTertiaryContainer";
  private static final String COLOR_TERTIARY_FIXED = "colorTertiaryFixed";
  private static final String COLOR_TERTIARY_FIXED_DIM = "colorTertiaryFixedDim";
  private static final String COLOR_ON_TERTIARY_FIXED = "colorOnTertiaryFixed";
  private static final String COLOR_ON_TERTIARY_FIXED_VARIANT = "colorOnTertiaryFixedVariant";
  private static final String COLOR_ON_BACKGROUND = "colorOnBackground";
  private static final String COLOR_SURFACE = "colorSurface";
  private static final String COLOR_ON_SURFACE = "colorOnSurface";
  private static final String COLOR_SURFACE_VARIANT = "colorSurfaceVariant";
  private static final String COLOR_ON_SURFACE_VARIANT = "colorOnSurfaceVariant";
  private static final String COLOR_SURFACE_INVERSE = "colorSurfaceInverse";
  private static final String COLOR_ON_SURFACE_INVERSE = "colorOnSurfaceInverse";
  private static final String COLOR_SURFACE_BRIGHT = "colorSurfaceBright";
  private static final String COLOR_SURFACE_DIM = "colorSurfaceDim";
  private static final String COLOR_SURFACE_CONTAINER = "colorSurfaceContainer";
  private static final String COLOR_SURFACE_CONTAINER_HIGH = "colorSurfaceContainerHigh";
  private static final String COLOR_SURFACE_CONTAINER_HIGHEST = "colorSurfaceContainerHighest";
  private static final String COLOR_SURFACE_CONTAINER_LOW = "colorSurfaceContainerLow";
  private static final String COLOR_SURFACE_CONTAINER_LOWEST = "colorSurfaceContainerLowest";
  private static final String COLOR_OUTLINE = "colorOutline";
  private static final String COLOR_OUTLINE_VARIANT = "colorOutlineVariant";
  private static final String COLOR_ON_ERROR = "colorOnError";
  private static final String COLOR_ERROR_CONTAINER = "colorErrorContainer";
  private static final String COLOR_ON_ERROR_CONTAINER = "colorOnErrorContainer";
  private static final String COLOR_PRIMARY_SURFACE = "colorPrimarySurface";
  private static final String COLOR_ON_PRIMARY_SURFACE = "colorOnPrimarySurface";
  private static final String COLOR_CONTAINER = "colorContainer";
  private static final String COLOR_CONTAINER_CHECKED = "colorContainerChecked";
  private static final String COLOR_CONTAINER_UNCHECKED = "colorContainerUnchecked";
  private static final String COLOR_ON_CONTAINER = "colorOnContainer";
  private static final String COLOR_ON_CONTAINER_CHECKED = "colorOnContainerChecked";
  private static final String COLOR_ON_CONTAINER_UNCHECKED = "colorOnContainerUnchecked";
  private static final String SCRIM_BACKGROUND = "scrimBackground";

  public static List<String> buildList() {
    return new Builder()
        .add(COLOR_ON_PRIMARY)
        .add(COLOR_PRIMARY_INVERSE)
        .add(COLOR_PRIMARY_CONTAINER)
        .add(COLOR_ON_PRIMARY_CONTAINER)
        .add(COLOR_PRIMARY_FIXED)
        .add(COLOR_PRIMARY_FIXED_DIM)
        .add(COLOR_ON_PRIMARY_FIXED)
        .add(COLOR_ON_PRIMARY_FIXED_VARIANT)
        .add(COLOR_SECONDARY)
        .add(COLOR_SECONDARY_CONTAINER)
        .add(COLOR_ON_SECONDARY_CONTAINER)
        .add(COLOR_SECONDARY_FIXED)
        .add(COLOR_SECONDARY_FIXED_DIM)
        .add(COLOR_ON_SECONDARY_FIXED)
        .add(COLOR_ON_SECONDARY_FIXED_VARIANT)
        .add(COLOR_TERTIARY)
        .add(COLOR_ON_TERTIARY)
        .add(COLOR_TERTIARY_CONTAINER)
        .add(COLOR_ON_TERTIARY_CONTAINER)
        .add(COLOR_TERTIARY_FIXED)
        .add(COLOR_TERTIARY_FIXED_DIM)
        .add(COLOR_ON_TERTIARY_FIXED)
        .add(COLOR_ON_TERTIARY_FIXED_VARIANT)
        .add(COLOR_ON_BACKGROUND)
        .add(COLOR_SURFACE)
        .add(COLOR_ON_SURFACE)
        .add(COLOR_SURFACE_VARIANT)
        .add(COLOR_ON_SURFACE_VARIANT)
        .add(COLOR_SURFACE_INVERSE)
        .add(COLOR_ON_SURFACE_INVERSE)
        .add(COLOR_SURFACE_BRIGHT)
        .add(COLOR_SURFACE_DIM)
        .add(COLOR_SURFACE_CONTAINER)
        .add(COLOR_SURFACE_CONTAINER_HIGH)
        .add(COLOR_SURFACE_CONTAINER_HIGHEST)
        .add(COLOR_SURFACE_CONTAINER_LOW)
        .add(COLOR_SURFACE_CONTAINER_LOWEST)
        .add(COLOR_OUTLINE)
        .add(COLOR_OUTLINE_VARIANT)
        .add(COLOR_ON_ERROR)
        .add(COLOR_ERROR_CONTAINER)
        .add(COLOR_ON_ERROR_CONTAINER)
        .add(COLOR_PRIMARY_SURFACE)
        .add(COLOR_ON_PRIMARY_SURFACE)
        .add(COLOR_CONTAINER)
        .add(COLOR_CONTAINER_CHECKED)
        .add(COLOR_CONTAINER_UNCHECKED)
        .add(COLOR_ON_CONTAINER)
        .add(COLOR_ON_CONTAINER_CHECKED)
        .add(COLOR_ON_CONTAINER_UNCHECKED)
        .add(SCRIM_BACKGROUND)
        .build();
  }

  private static class Builder {
    private final List<String> list = new ArrayList<>();

    Builder add(String key) {
      list.add(key);
      return this;
    }

    List<String> build() {
      return list;
    }
  }
}
