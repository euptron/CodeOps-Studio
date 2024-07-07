/*************************************************************************
 * This file is part of CodeOps Studio.
 * CodeOps Studio - code anywhere anytime
 * https://github.com/etidoUP/CodeOps-Studio
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
 
   package com.eup.codeopsstudio.util;

import android.util.Log;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.mozilla.universalchardet.UniversalDetector;
import java.util.stream.Collectors;

/**
 * A class to detect the encoding of a file
 *
 * @author EUP
 */
public class EncodingDetector {

  public static final String LOG_TAG = "EncodingDetector";
  private static int BUFFER_SIZE = 1024 * 4; // was *10

  public static Charset detectFileEncoding(String filePath) {
    return detectFileEncoding(new File(filePath));
  }

  public static Charset detectFileEncoding(File file) {
    byte[] buffer = new byte[BUFFER_SIZE];
    Charset foundCharSet = StandardCharsets.UTF_8; // default charset
    try (FileInputStream fis = new FileInputStream(file)) {
      UniversalDetector detector = new UniversalDetector();
      int bytesRead;
      while ((bytesRead = fis.read(buffer)) > 0 && !detector.isDone()) {
        detector.handleData(buffer, 0, bytesRead);
      }
      detector.dataEnd();
      String encoding = detector.getDetectedCharset();
      if (encoding != null) {
        foundCharSet = findEncoding(encoding.trim());
      }
      detector.reset(); // Reset the detector for reuse
    } catch (IOException e) {
      Log.e(LOG_TAG, "Error detecting file encoding", e);
    }
    return foundCharSet;
  }

  public static boolean isSupportedEncoding(Charset charset) {
    return getSupportedCharsets().contains(charset);
  }

  public static List<String> getSupportedEncodings() {
    return getSupportedCharsets().stream().map(Charset::name).collect(Collectors.toList());
  }

  public static Collection<Charset> getSupportedCharsets() {
    return Charset.availableCharsets().values().stream()
        .filter(Charset::isRegistered)
        .collect(Collectors.toList());
  }

  public static Charset getEncoding(String charsetDef) {
    var availableCharsets = Charset.availableCharsets();
    // Iterate through the keys and perform case-insensitive comparison
    for (Map.Entry<String, Charset> entry : availableCharsets.entrySet()) {
      if (entry.getKey().equalsIgnoreCase(charsetDef)) {
        Charset charset = entry.getValue();
        if (charset.isRegistered()) {
          Log.d(LOG_TAG, "Mapped encoding " + charsetDef + " to charset " + charset);
          return charset;
        }
      }
    }
    // If no matching charset is found, return the default charset (UTF-8)
    Log.d(LOG_TAG, "No matching encoding found for " + charsetDef + ". Using default charset.");
    return StandardCharsets.UTF_8;
  }

  public static Charset findEncoding(String charsetDef) {
    try {
      return Charset.forName(charsetDef);
    } catch (RuntimeException e) {
      Log.e(LOG_TAG, e.getMessage());
    }
    Log.d(LOG_TAG, "No matching encoding found for " + charsetDef + ". Using default charset.");
    return StandardCharsets.UTF_8;
  }
}
