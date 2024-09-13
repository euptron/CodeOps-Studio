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
 
   package com.eup.codeopsstudio.models;

import com.eup.codeopsstudio.res.R;
import com.eup.codeopsstudio.common.util.PreferencesUtils;

public class ExtensionTable {
  /**
   * File icon extensions
   *
   * @since 0.0.1
   */
  public static final int getExtensionIcon(String fileName) {
    String extension = fileName.substring(fileName.lastIndexOf(".") + 1);
    switch (extension) {
      case "c":
        return R.drawable.ic_lang_c;
      case "mhtml":
        return R.drawable.ic_lang_html5;
      case "woff":
        return R.drawable.ic_format_font;
      case "ttf":
        return R.drawable.ic_format_font;
      case "js":
        return R.drawable.ic_lang_javascript;
      case "h":
        return R.drawable.ic_lang_c;
      case "html":
        return R.drawable.ic_lang_html5;
      case "xlsx":
        return R.drawable.ic_file_excel;
      case "cpp":
        return R.drawable.ic_lang_cpp;
      case "css":
        return R.drawable.ic_lang_css3;
      case "git":
        return R.drawable.ic_git;
      case "go":
        return R.drawable.ic_lang_go;
      case "gradle":
        return R.drawable.ic_gradle;
      case "java":
        return R.drawable.ic_lang_java;
      case "php":
        return R.drawable.ic_lang_php;
      case "py":
        return R.drawable.ic_lang_python;
      case "kts":
        return R.drawable.ic_lang_kotlin;
      case "kt":
        return R.drawable.ic_lang_kotlin;
      case "lua":
        return R.drawable.ic_lang_lua;
      case "github":
        return R.drawable.ic_github;
      case "jsx":
        return R.drawable.ic_react;
      case "xml":
        return R.drawable.ic_lang_xml;
      case "json":
        return R.drawable.ic_code_json;
      case "bat":
        return R.drawable.ic_bash;
      case "net":
        return R.drawable.ic_dot_net;
      case "sh":
        return R.drawable.ic_powershell;
      case "cs":
        return R.drawable.ic_lang_csharp;
      case "ts":
        return R.drawable.ic_lang_typescript;
      case "gitignore":
        return R.drawable.ic_git;
        // Outlined iocn support...btw whats the use \(- -)/
      case "md":
        if (useOutlinedIcons()) {
          return R.drawable.ic_lang_markdown_outline;
        } else {
          return R.drawable.ic_lang_markdown;
        }
      case "so":
        if (useOutlinedIcons()) {
          return R.drawable.ic_file_cog_outline;
        } else {
          return R.drawable.ic_file_cog;
        }
      case "zip":
        if (useOutlinedIcons()) {
          return R.drawable.ic_folder_zip_outline;
        } else {
          return R.drawable.ic_folder_zip;
        }
      case "jar":
        if (useOutlinedIcons()) {
          return R.drawable.ic_file_outline;
        } else {
          return R.drawable.ic_file;
        }
      case "jpg":
        if (useOutlinedIcons()) {
          return R.drawable.ic_file_image_outline;
        } else {
          return R.drawable.ic_file_image;
        }
      case "jpeg":
        if (useOutlinedIcons()) {
          return R.drawable.ic_file_image_outline;
        } else {
          return R.drawable.ic_file_image;
        }
      case "svg":
        if (useOutlinedIcons()) {
          return R.drawable.ic_file_image_outline;
        } else {
          return R.drawable.ic_file_image;
        }
      case "keystore":
        if (useOutlinedIcons()) {
          return R.drawable.ic_file_key_outline;
        } else {
          return R.drawable.ic_file_key;
        }
      case "jks":
        if (useOutlinedIcons()) {
          return R.drawable.ic_file_key_outline;
        } else {
          return R.drawable.ic_file_key;
        }
      case "png":
        if (useOutlinedIcons()) {
          return R.drawable.ic_file_image_outline;
        } else {
          return R.drawable.ic_file_image;
        }
      case "doc":
        if (useOutlinedIcons()) {
          return R.drawable.ic_file_document_outline;
        } else {
          return R.drawable.ic_file_document;
        }
      case "properties":
        if (useOutlinedIcons()) {
          return R.drawable.ic_file_cog_outline;
        } else {
          return R.drawable.ic_file_cog;
        }
        // Audio Visual
      case "avi":
        if (useOutlinedIcons()) {
          return R.drawable.ic_file_video_outline;
        } else {
          return R.drawable.ic_file_video;
        }
      case "mp4":
        if (useOutlinedIcons()) {
          return R.drawable.ic_file_video_outline;
        } else {
          return R.drawable.ic_file_video;
        }
      case "mkv":
        if (useOutlinedIcons()) {
          return R.drawable.ic_file_video_outline;
        } else {
          return R.drawable.ic_file_video;
        }
      case "mp3":
        if (useOutlinedIcons()) {
          return R.drawable.ic_file_music_outline;
        } else {
          return R.drawable.ic_file_music;
        }
      case "amr":
        if (useOutlinedIcons()) {
          return R.drawable.ic_file_music_outline;
        } else {
          return R.drawable.ic_file_music;
        }
      case "pdf":
        if (useOutlinedIcons()) {
          return R.drawable.ic_file_pdf_outline;
        } else {
          return R.drawable.ic_file_pdf;
        }
      case "m4a":
        if (useOutlinedIcons()) {
          return R.drawable.ic_file_music_outline;
        } else {
          return R.drawable.ic_file_music;
        }
      case "sql":
        if (useOutlinedIcons()) {
          return R.drawable.ic_database_outline;
        } else {
          return R.drawable.ic_database;
        }
      case "srt":
        if (useOutlinedIcons()) {
          return R.drawable.ic_script_text_outline;
        } else {
          return R.drawable.ic_script_text;
        }
      case "ssa":
        if (useOutlinedIcons()) {
          return R.drawable.ic_script_text_outline;
        } else {
          return R.drawable.ic_script_text;
        }
      case "smi":
        if (useOutlinedIcons()) {
          return R.drawable.ic_script_text_outline;
        } else {
          return R.drawable.ic_script_text;
        }
      case "vtt":
        if (useOutlinedIcons()) {
          return R.drawable.ic_script_text_outline;
        } else {
          return R.drawable.ic_script_text;
        }
      case "ttml":
        if (useOutlinedIcons()) {
          return R.drawable.ic_script_text_outline;
        } else {
          return R.drawable.ic_script_text;
        }
      case "dfxp":
        if (useOutlinedIcons()) {
          return R.drawable.ic_script_text_outline;
        } else {
          return R.drawable.ic_script_text;
        }
      case "txt":
        if (useOutlinedIcons()) {
          return R.drawable.ic_script_text_outline;
        } else {
          return R.drawable.ic_script_text;
        }
      default:
        if (useOutlinedIcons()) {
          return R.drawable.ic_file_outline;
        } else {
          return R.drawable.ic_file;
        }
    }
  }

  private static boolean useOutlinedIcons() {
    return PreferencesUtils.useOutLinedIcons();
  }
}
