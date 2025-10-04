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

package com.eup.codeopsstudio.models;

import androidx.annotation.NonNull;

import com.eup.codeopsstudio.R;
import com.eup.codeopsstudio.common.util.PreferencesUtils;

/**
 * Manages associations between file extensions and their corresponding icons.
 * This class serves as a lookup table to determine the appropriate icon for a given file extension.
 * <p>
 * Note: Future enhancements might involve moving this logic to a JSON-based plugin for increased
 * modularity.
 *
 * @author Etido Peter
 * @since 0.0.1
 */
public class ExtensionTable {
    private ExtensionTable() {
        // Default
    }

    /**
     * Retrieves the icon resource ID for a given file name based on its extension.
     *
     * @param fileName The name of the file to check.
     * @return The resource ID for the icon corresponding to the file's extension,
     * or a default icon if the extension is not recognized.
     */
    public static int getExtensionIcon(@NonNull String fileName) {
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1);
        switch (extension) {
            case "c", "h":
                return R.drawable.ic_lang_c;
            case "mhtml", "html":
                return R.drawable.ic_lang_html5;
            case "woff", "ttf":
                return R.drawable.ic_format_font;
            case "js":
                return R.drawable.ic_lang_javascript;
            case "xlsx":
                return R.drawable.ic_file_excel;
            case "cpp":
                return R.drawable.ic_lang_cpp;
            case "css":
                return R.drawable.ic_lang_css3;
            case "git", "gitignore":
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
            case "kts", "kt":
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
            //---Outlined icons
            case "md":
                if (useOutlinedIcons()) {
                    return R.drawable.ic_lang_markdown_outline;
                }
                return R.drawable.ic_lang_markdown;
            case "properties", "so":
                if (useOutlinedIcons()) {
                    return R.drawable.ic_file_cog_outline;
                }
                return R.drawable.ic_file_cog;
            case "zip", "jar":
                if (useOutlinedIcons()) {
                    return R.drawable.ic_folder_zip_outline;
                }
                return R.drawable.ic_folder_zip;
            case "png", "svg", "jpg", "jpeg":
                if (useOutlinedIcons()) {
                    return R.drawable.ic_file_image_outline;
                }
                return R.drawable.ic_file_image;
            case "jks", "keystore":
                if (useOutlinedIcons()) {
                    return R.drawable.ic_file_key_outline;
                }
                return R.drawable.ic_file_key;
            case "doc":
                if (useOutlinedIcons()) {
                    return R.drawable.ic_file_document_outline;
                }
                return R.drawable.ic_file_document;
            case "sql":
                if (useOutlinedIcons()) {
                    return R.drawable.ic_database_outline;
                }
                return R.drawable.ic_database;
            case "dfxp", "txt", "ttml", "vtt", "smi", "ssa", "srt":
                if (useOutlinedIcons()) {
                    return R.drawable.ic_script_text_outline;
                }
                return R.drawable.ic_script_text;
            //---Audio Visual
            case "mkv", "mp4", "avi":
                if (useOutlinedIcons()) {
                    return R.drawable.ic_file_video_outline;
                }
                return R.drawable.ic_file_video;
            case "pdf":
                if (useOutlinedIcons()) {
                    return R.drawable.ic_file_pdf_outline;
                }
                return R.drawable.ic_file_pdf;
            case "m4a", "amr", "mp3":
                if (useOutlinedIcons()) {
                    return R.drawable.ic_file_music_outline;
                }
                return R.drawable.ic_file_music;
            default:
                if (useOutlinedIcons()) {
                    return R.drawable.ic_file_outline;
                }
                return R.drawable.ic_file;
        }
    }

    private static boolean useOutlinedIcons() {
        return PreferencesUtils.useOutLinedIcons();
    }
}