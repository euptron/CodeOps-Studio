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
 
   package com.eup.codeopsstudio.common;

import com.eup.codeopsstudio.common.ContextManager;
import com.eup.codeopsstudio.common.util.FileUtil;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Constants class containing common constants used by CodeOps Studio. */
public final class Constants {
  
  public static final long AVG_WAIT_MILLS = 250;
  public static final Set<String> WEB_MARKUP_LANGUAGE =
      Collections.unmodifiableSet(new HashSet<>(Arrays.asList("html", "htm")));

  /**
   * @see DefaultFileEncodingDialogPreference#resetEncoding()
   */
  public static final String FALLBACK_FILE_ENCODING = Constants.CHARSET_UTF_8;

  public static final Set<String> PREVIEWABLE_EXTENSIONS =
      Collections.unmodifiableSet(
          new HashSet<>(
              Arrays.asList(
                  "jpg", "jpeg", "png", "bmp", "gif", "webp", "tiff", "ico", "psd", "raw", "svg",
                  "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "odt", "ods", "odp", "rtf",
                  "zip", "rar", "tar", "gz", "7z", "bz2", "xz", "pdf", "doc", "docx", "xls", "xlsx",
                  "ppt", "pptx", "odt", "ods", "odp", "rtf", "mp3", "wav", "ogg", "flac", "aac",
                  "wma", "m4a", "ac3", "avi", "mp4", "mov", "mkv", "wmv", "flv", "webm", "3gp",
                  "rmvb", "m4v")));

  public static boolean isMarkUp(final File file) {
    if (file == null) return false;
    final var ext = FileUtil.getFileExtension(file);
    if (ext != null & WEB_MARKUP_LANGUAGE.contains(ext)) {
      return true;
    }
    return false;
  }
 
  /**
   * Checks if a file is preview able
   *
   * @param file The file to check if it could be opened in the webview
   * @return True if @param file is preview able and false if it's not
   */
  public static boolean isPreviewAble(File file) {
    if (file == null) return false;
    final var ext = FileUtil.getFileExtension(file);
    if (ext != null & PREVIEWABLE_EXTENSIONS.contains(ext)) {
      return true;
    }
    return false;
  }

  public static final String EDITOR_TYPE_CODE = "CODE_EDITOR";
  public static final String EDITOR_TYPE_PAGE = "PAGE_EDITOR";
  public static final String EDITOR_FILE_PATH_KEY = "editor_file_path";
  // Textual constants
  public static final String BACK_SPACE = "\b";
  public static final String NEXT_LINE = "\n";
  public static final String SPACE = " ";
  public static final String TAB = "\t";
  // Shared preferences keys
  public static final String CHANGE_LOG_SHARED_PREF_KEY = "changelog_data";
  public static final String RECENT_PROJECTS_KEY = "recent_projects_key";
  // Adopted from org.mozilla.universalchardet
  // Character encodings
  // (These constants represent various character encodings supported by the system)
  // Unsupported character encodings
  public static final String CHARSET_ISO_2022_JP = "ISO-2022-JP";
  public static final String CHARSET_ISO_2022_CN = "ISO-2022-CN";
  public static final String CHARSET_ISO_2022_KR = "ISO-2022-KR";
  public static final String CHARSET_ISO_8859_5 = "ISO-8859-5";
  public static final String CHARSET_ISO_8859_7 = "ISO-8859-7";
  public static final String CHARSET_ISO_8859_8 = "ISO-8859-8";
  public static final String CHARSET_BIG5 = "BIG5";
  public static final String CHARSET_GB18030 = "GB18030";
  public static final String CHARSET_EUC_JP = "EUC-JP";
  public static final String CHARSET_EUC_KR = "EUC-KR";
  public static final String CHARSET_EUC_TW = "EUC-TW";
  public static final String CHARSET_SHIFT_JIS = "SHIFT_JIS";
  public static final String CHARSET_IBM855 = "IBM855";
  public static final String CHARSET_IBM866 = "IBM866";
  public static final String CHARSET_KOI8_R = "KOI8-R";
  public static final String CHARSET_MACCYRILLIC = "MACCYRILLIC";
  public static final String CHARSET_WINDOWS_1251 = "WINDOWS-1251";
  public static final String CHARSET_WINDOWS_1252 = "WINDOWS-1252";
  public static final String CHARSET_WINDOWS_1253 = "WINDOWS-1253";
  public static final String CHARSET_WINDOWS_1255 = "WINDOWS-1255";
  public static final String CHARSET_UTF_8 = "UTF-8";
  public static final String CHARSET_UTF_16BE = "UTF-16BE";
  public static final String CHARSET_UTF_16LE = "UTF-16LE";
  public static final String CHARSET_UTF_32BE = "UTF-32BE";
  public static final String CHARSET_UTF_32LE = "UTF-32LE";
  public static final String CHARSET_TIS620 = "TIS620";
  public static final String CHARSET_US_ASCCI = "US-ASCII";

  // WARNING: Listed below are charsets which Java does not support.
  public static final String CHARSET_HZ_GB_2312 = "HZ-GB-2312"; // Simplified Chinese
  public static final String CHARSET_X_ISO_10646_UCS_4_3412 =
      "X-ISO-10646-UCS-4-3412"; // Malformed UTF-32
  public static final String CHARSET_X_ISO_10646_UCS_4_2143 =
      "X-ISO-10646-UCS-4-2143"; // Malformed UTF-32
  // List of supported character encodings
  public static final List<String> SUPPORTED_CHARSET_LIST =
      Arrays.asList(
          Constants.CHARSET_BIG5,
          Constants.CHARSET_EUC_JP,
          Constants.CHARSET_EUC_KR,
          Constants.CHARSET_EUC_TW,
          Constants.CHARSET_GB18030,
          Constants.CHARSET_IBM855,
          Constants.CHARSET_IBM866,
          Constants.CHARSET_ISO_2022_CN,
          Constants.CHARSET_ISO_2022_JP,
          Constants.CHARSET_ISO_2022_KR,
          Constants.CHARSET_ISO_8859_5,
          Constants.CHARSET_ISO_8859_7,
          Constants.CHARSET_ISO_8859_8,
          Constants.CHARSET_KOI8_R,
          Constants.CHARSET_MACCYRILLIC,
          Constants.CHARSET_SHIFT_JIS,
          Constants.CHARSET_TIS620,
          Constants.CHARSET_US_ASCCI,
          Constants.CHARSET_UTF_16BE,
          Constants.CHARSET_UTF_16LE,
          Constants.CHARSET_UTF_32BE,
          Constants.CHARSET_UTF_32LE,
          Constants.CHARSET_UTF_8,
          Constants.CHARSET_WINDOWS_1251,
          Constants.CHARSET_WINDOWS_1252,
          Constants.CHARSET_WINDOWS_1253,
          Constants.CHARSET_WINDOWS_1255);

  // URLS
  public static final String Telegram = "https://t.me/codeopsstudio";
  public static final String X = "https://x.com/codeopsstudio";
  public static final String DEFAULT_CHANGE_LOG_URL =
      "https://raw.githubusercontent.com/etidoUP/CodeOps-Studio/main/release-notes.json";
  public static final String WEBSITE_URL = "https://codeopsstudio.blogspot.com";
  public static final String DOCUMENTATION_URL =
      "https://codeopsstudio.blogspot.com/p/documentation.html";
  public static final String GOOGLE_PLAY_APP_URL =
      "https://play.google.com/store/apps/details?id=" + ContextManager.getPackageName();
  public static final String GITHUB_URL = "https://github.com/etidoUP/CodeOps-Studio";
  public static final String CHECK_UPDATE_URL = GITHUB_URL;
  public static final String CHECK_UPDATE_GPS_URL = "https://github.com/etidoUP/CodeOps-Studio/releases";
  public static final String PRIVACY_POLICY_URL = "https://codeopsstudio.blogspot.com/p/privacy-policy.html";
  public static final String TERMS_OF_SERVICE_URL = "https://codeopsstudio.blogspot.com/p/terms-of-service.html";

  // Inner class for shared preference keys
  public static class SharedPreferenceKeys {
    public static final String KEY_RECENT_PROJECTS = "recent_projects";
    public static final String KEY_LAST_OPENED_PROJECT = "last_opened_project";
    public static final String KEY_PERSISTED_PANES = "persisted_cues";
    /*
     * Editor Preferences
     * <p> Aliases
     * NAV = navigation
     * SI = symbol input
     * FUN = function
     * NUM = number
     * NP = non printable
     */
    public static final String KEY_CODE_EDITOR_FONT_SIZE = "pref_code_editor_font_size";
    public static final String KEY_CODE_EDITOR_FONT = "pref_code_editor_font";
    public static final String KEY_SHOW_WELCOME_PANE = "pref_show_welcome_pane";
    public static final String KEY_CODE_EDITOR_NP_PAINT_FLAGS = "pref_code_editor_npc";
    public static final String KEY_CODE_EDITOR_LINE_HEIGHT = "pref_code_editor_line_height";
    public static final String KEY_CODE_EDITOR_TAB_SIZE = "pref_code_editor_tab_size";
    public static final String KEY_CODE_EDITOR_FONT_LIAGTURES = "pref_code_editor_font_liagtures";
    public static final String KEY_CODE_EDITOR_WORD_WRAP = "pref_code_editor_word_wrap";
    public static final String KEY_CODE_EDITOR_TAB_INDENT = "pref_code_editor_tab_indent";
    public static final String KEY_CODE_EDITOR_ICU = "pref_code_editor_icu";
    public static final String KEY_CODE_EDITOR_AUTO_SAVE = "pref_code_editor_auto_save";
    public static final String KEY_CODE_EDITOR_RELATIVE_CLOSE_DEPTH =
        "pref_code_editor_relative_close_depth";
    public static final String KEY_CODE_EDITOR_PIN_LINE_NUM = "pref_code_editor_pin_line_numbers";
    public static final String KEY_CODE_EDITOR_SI_PANEL = "pref_code_editor_symbol_panel";
    public static final String KEY_CODE_EDITOR_FUN_PANEL = "pref_code_editor_fun_panel";
    public static final String KEY_CODE_EDITOR_NAV_PANEL = "pref_code_editor_nav_panel";
    public static final String KEY_CODE_EDITOR_MAGNIFIER = "pref_code_editor_use_magnifier";
    public static final String KEY_CODE_EDITOR_STICKY_SCROLL = "pref_code_editor_sticky_scroll";
    public static final String KEY_CODE_EDITOR_AUTO_CLOSE_BRACKET =
        "pref_code_editor_auto_close_bracket";
    public static final String KEY_CODE_EDITOR_SCROLL_BAR = "pref_code_editor_scroll_bar";
    public static final String KEY_CODE_EDITOR_HARDWARE_ACCLERATION =
        "pref_code_editor_hardware_accleration";
    public static final String KEY_CODE_EDITOR_LINE_NUMBERS = "pref_code_editor_line_numbers";
    public static final String KEY_CODE_EDITOR_DELETE_EMPTY_LINE =
        "pref_code_editor_delete_empty_line_bck_key_event";
    public static final String KEY_CODE_EDITOR_DELETE_TAB =
        "pref_code_editor_delete_tab_bck_key_event";
    public static final String KEY_CODE_EDITOR_ANIMATE_AUTO_COMP_WINDOW =
        "pref_code_editor_animate_auto_complt_window";
    public static final String KEY_CODE_EDITOR_HIGHLIGHT_BRACKET =
        "pref_code_editor_highlight_brckt";
    public static final String KEY_CODE_EDITOR_AUTO_COMPLETE = "pref_code_editor_auto_complete";
    public static final String KEY_CODE_EDITOR_CURSOR_BLINK_PERIOD =
        "pref_code_editor_cursor_blnk_period";
    public static final String KEY_OPEN_LAST_OPENED_PROJECT = "pref_open_last_project";
    public static final String KEY_CODE_EDITOR_CLOSE_UNPINNED_PROJECT_PANES = "pref_code_editor_close_unpinned_project_panes";
    /**
     * The SharedPreference associate for the default file encoding.
     *
     * <p>In CodeOps Studio a file is opened suing its own encoding as detected by the {@link
     * EncodingDetector} if the detector fails it default to UTF-8. Making this useful for editing
     * files as it is and saving files universally using a singular charset.
     */
    public static final String KEY_CODE_EDITOR_DEFAULT_FILE_ENCODING =
        "pref_editor_default_file_encoding";

    // General Configuration Preferences
    public static final String KEY_APP_THEME = "pref_app_theme";
    public static final String KEY_DYNAMIC_COLOURS = "pref_dynamic_colours";
    // Privacy Preferences
    public static final String KEY_SHARE_STATISTICS = "pref_share_stats";
    public static final String KEY_PACKAGE_NAME = "pref_pkg_name";
    public static final String KEY_VERSION_CODE = "pref_app_version_code";
    public static final String KEY_VERSION_NAME = "pref_app_version_name";
    public static final String KEY_DEVICE_MODEL = "pref_device_model";
    public static final String KEY_SDK_VERSION = "pref_sdk_version";
    public static final String KEY_BUILD_ID = "pref_build_id";
    public static final String KEY_RELEASE = "pref_release";
    public static final String KEY_DEVICE_BOARD = "pref_board";
    public static final String KEY_DEVICE_BRAND = "pref_brand";
    public static final String KEY_CPU_ARCH = "pref_cpu_arch";
    public static final String KEY_DEVICE_COUNTRY = "pref_country";
    public static final String KEY_LOCALE = "pref_locale";
    // Other Preferences
    public static final String KEY_OUTLINE_ICONS = "pref_useoutlined_icons";
    public static final String KEY_GOOGLE_JSON_FORMATTER = "pref_google_jsonformatter";
    public static final String KEY_RECENT_FOLDER = "recent_folder_path";
    
    /** Key for storing the project save path in SharedPreferences. */
    public static final String KEY_PROJECT_SAVE_PATH = "projects_save_path";
  }
}
