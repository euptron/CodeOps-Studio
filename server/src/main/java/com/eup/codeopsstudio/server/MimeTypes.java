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

package com.eup.codeopsstudio.server;

import android.webkit.MimeTypeMap;
import androidx.annotation.NonNull;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for determining MIME types from files or extensions. Provides a comprehensive
 * static map for common web development formats and falls back to Android's system MimeTypeMap.
 *
 * @author Etido Peter
 */
public class MimeTypes {

  private static final String DEFAULT_MIME_TYPE = "application/octet-stream";
  private static final Map<String, String> MIME_MAP = new HashMap<>();

  static {
    // --- Web Technologies ---
    MIME_MAP.put("html", "text/html");
    MIME_MAP.put("htm", "text/html");
    MIME_MAP.put("css", "text/css");
    MIME_MAP.put("js", "application/javascript");
    MIME_MAP.put("mjs", "application/javascript");
    MIME_MAP.put("json", "application/json");
    MIME_MAP.put("xml", "application/xml");
    MIME_MAP.put("map", "application/json"); // Source maps
    MIME_MAP.put("ts", "application/x-typescript");
    MIME_MAP.put("jsx", "text/jsx");
    MIME_MAP.put("tsx", "text/tsx");
    MIME_MAP.put("wasm", "application/wasm");

    // --- Images ---
    MIME_MAP.put("png", "image/png");
    MIME_MAP.put("jpg", "image/jpeg");
    MIME_MAP.put("jpeg", "image/jpeg");
    MIME_MAP.put("gif", "image/gif");
    MIME_MAP.put("svg", "image/svg+xml");
    MIME_MAP.put("webp", "image/webp");
    MIME_MAP.put("ico", "image/x-icon");
    MIME_MAP.put("bmp", "image/bmp");
    MIME_MAP.put("tiff", "image/tiff");

    // --- Fonts ---
    MIME_MAP.put("ttf", "font/ttf");
    MIME_MAP.put("otf", "font/otf");
    MIME_MAP.put("woff", "font/woff");
    MIME_MAP.put("woff2", "font/woff2");
    MIME_MAP.put("eot", "application/vnd.ms-fontobject");

    // --- Audio/Video ---
    MIME_MAP.put("mp3", "audio/mpeg");
    MIME_MAP.put("wav", "audio/wav");
    MIME_MAP.put("ogg", "audio/ogg");
    MIME_MAP.put("mp4", "video/mp4");
    MIME_MAP.put("webm", "video/webm");
    MIME_MAP.put("avi", "video/x-msvideo");
    MIME_MAP.put("mov", "video/quicktime");
    MIME_MAP.put("mkv", "video/x-matroska");

    // --- Documents/Archives ---
    MIME_MAP.put("pdf", "application/pdf");
    MIME_MAP.put("zip", "application/zip");
    MIME_MAP.put("rar", "application/x-rar-compressed");
    MIME_MAP.put("tar", "application/x-tar");
    MIME_MAP.put("gz", "application/gzip");
    MIME_MAP.put("7z", "application/x-7z-compressed");
    MIME_MAP.put("md", "text/markdown");
    MIME_MAP.put("txt", "text/plain");
    MIME_MAP.put("csv", "text/csv");
    MIME_MAP.put("rtf", "application/rtf");
  }

  private MimeTypes() {
    // @HIDE
  }

  @NonNull
  public static String getMimeType(@NonNull File file) {
    return getMimeType(file.getName());
  }

  /**
   * Determines the MIME type for a given filename or extension.
   *
   * @param fileName The filename (e.g., "style.css") or extension.
   * @return The MIME type string, or "application/octet-stream" if unknown.
   */
  @NonNull
  public static String getMimeType(@NonNull String fileName) {
    int lastDot = fileName.lastIndexOf('.');

    if (lastDot >= 0 && lastDot < fileName.length() - 1) {
      String extension = fileName.substring(lastDot + 1).toLowerCase();

      if (MIME_MAP.containsKey(extension)) {
        return MIME_MAP.get(extension);
      }

      String systemMime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
      if (systemMime != null) {
        return systemMime;
      }
    }

    return DEFAULT_MIME_TYPE;
  }
}
