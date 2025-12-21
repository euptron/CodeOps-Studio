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

package com.eup.codeopsstudio.util;

import android.content.Context;
import android.graphics.Color;
import android.os.Environment;
import com.eup.codeopsstudio.common.Constants;
import com.eup.codeopsstudio.common.ILog;
import com.eup.codeopsstudio.common.MaterialColorKeys;
import com.google.android.material.color.MaterialColors;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Utility class responsible for extracting Material Design 3 (M3) theme colors at runtime and
 * exporting them into a structured XML resource file. This enables CodeOps Studio to introspect the
 * active theme applied to the application and generate a portable snapshot of all relevant Material
 * color attributes.
 *
 * <p>The exporter resolves each color attribute name provided by {@link
 * com.eup.codeopsstudio.common.MaterialColorKeys} by dynamically mapping it to the corresponding
 * field in {@code com.google.android.material.R.attr}. Each resolved attribute is then retrieved
 * using {@link MaterialColors#getColor(Context, int, Object)} and written to a standard Android
 * color XML definition.
 *
 * <p>The resulting XML file is saved to the application's external files directory under the
 * filename {@code captured_colors.xml}. This file can be used for debugging, theme inspection,
 * style generation, or external theme editing.
 *
 * <p><strong>Usage example:</strong>
 *
 * <pre>{@code
 * ThemeExporter.exportThemeToXML(context);
 * }</pre>
 *
 * <p>This class contains Android-specific file and theme operations and is therefore intended to be
 * used only within the application module.
 *
 * @author Etido Peter
 */
public class ThemeExporter {

  public static final String TAG = "ThemeExporter";

  public static void exportThemeToXML(Context context) {
    StringBuilder xmlBuilder = new StringBuilder();
    xmlBuilder.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
    xmlBuilder.append("<resources>\n");

    for (String attrName : MaterialColorKeys.buildList()) {
      try {
        int attrId = getMaterialAttrId(attrName);
        if (attrId != 0) {
          int color = MaterialColors.getColor(context, attrId, attrName);
          appendColor(xmlBuilder, attrName, color);
        } else {
          ILog.error(TAG, "Failed to resolve attribute ID for: " + attrName);
        }
      } catch (Exception e) {
        ILog.error(TAG, "Error processing attribute '" + attrName + "': " + e.getMessage(), e);
      }
    }

    xmlBuilder.append("</resources>");
    saveToFile(context, "captured_colors.xml", xmlBuilder.toString());
  }

  private static int getMaterialAttrId(String name) {
    try {
      Class<?> attrClass = com.google.android.material.R.attr.class;
      return (int) attrClass.getField(name).get(null);
    } catch (Exception e) {
      ILog.error(TAG, e.getMessage(), e);
      return 0; // fallback
    }
  }

  private static void appendColor(StringBuilder builder, String colorName, int color) {
    builder.append(
        String.format("    <color name=\"" + colorName + "\">%s</color>\n", colorToHex(color)));
  }

  private static String colorToHex(int color) {
    return String.format("#%08X", color);
  }

  private static void saveToFile(Context context, String fileName, String content) {
    try {
      //  File file = new File(context.getExternalFilesDir(null), fileName);
      var storageDir =
          new File(Environment.getExternalStorageDirectory(), Constants.DEBUG_LOGGING_PATH_CUE);
      File file = new File(storageDir, fileName);
      FileWriter writer = new FileWriter(file);
      writer.write(content);
      writer.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static int getMaterialColor(Context ctx, String name) {
    return MaterialColors.getColor(ctx, getMaterialAttrId(name), Color.TRANSPARENT);
  }
}
