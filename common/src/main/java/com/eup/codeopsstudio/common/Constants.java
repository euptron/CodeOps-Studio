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

import com.eup.codeopsstudio.common.util.FileUtil;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A class containing common constants used by CodeOps Studio.
 *
 * @author Etido Peter
 */
public final class Constants {

    public static final String DEFAULT_DATE_FORMAT = "EEE, dd-MMM-yyyy HH:mm:ss";
    public static final String IDE_LOGS_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.S";
    public static final String DEBUG_LOGS_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
    public static final String API_RESPONSE_DATE_FORMAT = "EEE, dd-MMM-yyyy HH:mm:ss Z";
    public static final String DATABASE_TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String USER_DISPLAY_DATE_TIME_FORMAT = "dd-MM-yyyy HH:mm:ss";
    public static final String USER_DISPLAY_TIME_DATE_FORMAT = "HH:mm:ss dd-MM-yyyy";
    public static final String ISO_8601_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss Z";
    public static final Set<String> WEB_MARKUP_LANGUAGE = Set.of("html", "htm");
    public static final Set<String> MODIFIABLE_EXTERNAL_STORAGE_IDS = Set.of("primary", "home");
    public static final Set<String> VIDEO_EXTENSIONS = Set.of("avi", "mp4", "mov", "mkv", "wmv",
        "flv", "webm", "3gp", "rmvb", "m4v");
    public static final Set<String> AUDIO_EXTENSIONS = Set.of("mp3", "wav", "ogg", "flac", "aac",
        "wma", "m4a", "ac3");
    public static final Set<String> IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "bmp", "gif",
        "webp", "tiff", "ico", "psd", "raw", "svg");
    public static final Set<String> DOCUMENT_EXTENSIONS = Set.of("pdf", "doc", "docx", "xls",
        "xlsx", "ppt", "pptx", "odt", "ods", "odp", "rtf");
    public static final Set<String> ARCHIVE_EXTENSIONS = Set.of("zip", "rar", "tar", "gz", "7z",
        "bz2", "xz");
    public static final Set<String> PREVIEWABLE_EXTENSIONS = Collections.unmodifiableSet(Stream
        .of(VIDEO_EXTENSIONS, AUDIO_EXTENSIONS, IMAGE_EXTENSIONS)
        .flatMap(Collection::stream)
        .collect(Collectors.toSet()));

    public static final Set<String> NON_PREVIEWABLE_EXTENSIONS = Collections.unmodifiableSet(Stream
        .of(DOCUMENT_EXTENSIONS, ARCHIVE_EXTENSIONS)
        .flatMap(Collection::stream)
        .collect(Collectors.toSet()));

    public static final int EXPIRATION_YEAR = 2026;
    public static final int EXPIRATION_MONTH = Calendar.MARCH;
    public static final int EXPIRATION_DAY = 4;
    public static final Charset DEFAULT_CHAR_SET = StandardCharsets.UTF_8;
    public static final long AVG_WAIT_MILLS = 250; // MillSeconds
    public static final long TOGGLE_TREENODE_ANIM_TIME = 500; // MillSeconds
    public static final String PREFERRED_TIME_ZONE = "UTC";
    /**
     * Used by the  <strong>DefaultFileEncodingDialogPreference#resetEncoding()</strong>
     */
    public static final String FALLBACK_FILE_ENCODING = "UTF-8";
    public static final String APP_PACKAGE_NAME =
        com.eup.codeopsstudio.common.ContextManager.getPackageName();
    public static final String EDITOR_FILE_PATH_KEY = "editor_file_path";
    // Textual constants
    public static final String BACK_SPACE = "\b";
    public static final String NEXT_LINE = "\n";
    public static final String SPACE = " ";
    public static final String TAB = "\t";
    // Shared preferences keys
    public static final String CHANGE_LOG_SHARED_PREF_KEY = "changelog_data";
    public static final String RECENT_PROJECTS_KEY = "recent_projects_key";
    // URLS
    public static final String TELEGRAM = "https://t.me/codeopsstudio";
    public static final String X = "https://x.com/codeopsstudio";
    public static final String WEBSITE_URL = "https://codeopsstudio.blogspot.com";
    public static final String FACEBOOK_URL =
        "https://www.facebook.com/profile" + ".php?id=61563542822201";
    public static final String DOCUMENTATION_URL =
        "https://codeopsstudio.blogspot" + ".com/p/documentation.html";
    public static final String GOOGLE_PLAY_APP_URL =
        "https://play.google" + ".com/store/apps/details?id="
            + com.eup.codeopsstudio.common.ContextManager.getPackageName();

    public static final String DEFAULT_CHANGE_LOG_URL =
        "https://raw.githubusercontent" + ".com/euptron/CodeOps-Studio/main/release-notes.json";
    public static final String GITHUB_BASE_URL = "https://github.com";
    public static final String REPO_ADMIN_USER_NAME = "euptron";
    public static final String REPO_NAME = "CodeOps-Studio";
    public static final String GITHUB_URL = GITHUB_BASE_URL
        .concat("/")
        .concat(REPO_ADMIN_USER_NAME)
        .concat(REPO_NAME);
    public static final String CHECK_UPDATE_GITHUB_URL = GITHUB_URL.concat("/releases");
    public static final String PRIVACY_POLICY_URL =
        "https://codeopsstudio.blogspot" + ".com/p/privacy-policy.html";
    public static final String TERMS_OF_SERVICE_URL =
        "https://codeopsstudio.blogspot" + ".com/p/terms-of-service.html";
    /**
     * Unique Notification ID used to dispatch notifications
     */
    public static final int APP_NOTIFICATION_ID = 1101;
    /**
     * App notification channel ID used by the com.eup.codeopsstudio.service.FileWatcherService
     */
    public static final String FILE_WATCHER_NOTIFICATION_CHANNEL_ID =
        "file_watcher_notification_channel";
    //---- Project Templates
    public static final String KEY_PROJECT_TEMPLATE_NAME = "name";
    public static final String KEY_PROJECT_TEMPLATE_TYPE = "projectType";
    public static final String KEY_PROJECT_TEMPLATE_AUTHOR = "author";
    public static final String KEY_PROJECT_TEMPLATE_VERSION_CODE = "versionCode";
    public static final String KEY_PROJECT_TEMPLATE_VERSION_NAME = "versionName";
    public static final String KEY_PROJECT_TEMPLATE_CREATION_DATE = "creation_date";
    public static final String KEY_PROJECT_TEMPLATE_DOC_URL = "doc_url";
    public static final String KEY_PROJECT_TEMPLATE_DESCRIPTION = "description";
    public static final String PROJECT_TEMPLATES_SUB_DIR_PATH =
        File.separator + ".cos" + File.separator + "template";
    public static final String PROJECT_TEMPLATE_MODEL_JSON_FILE_PATH =
        PROJECT_TEMPLATES_SUB_DIR_PATH + File.separator + "info" + ".json";

    private Constants() {
        // hide
    }

    //---- Others
    public static boolean isMarkUp(final File file) {
        if (file == null) return false;
        final String ext = FileUtil.getFileExtension(file);
        return WEB_MARKUP_LANGUAGE.contains(ext);
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
        return PREVIEWABLE_EXTENSIONS.contains(ext);
    }

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
        public static final String KEY_CODE_EDITOR_FONT_LIAGTURES =
            "pref_code_editor_font_liagtures";
        public static final String KEY_CODE_EDITOR_WORD_WRAP = "pref_code_editor_word_wrap";
        public static final String KEY_CODE_EDITOR_TAB_INDENT = "pref_code_editor_tab_indent";
        public static final String KEY_CODE_EDITOR_ICU = "pref_code_editor_icu";
        public static final String KEY_CODE_EDITOR_AUTO_SAVE = "pref_code_editor_auto_save";
        public static final String KEY_CODE_EDITOR_RELATIVE_CLOSE_DEPTH =
            "pref_code_editor_relative_close_depth";
        public static final String KEY_CODE_EDITOR_PIN_LINE_NUM =
            "pref_code_editor_pin_line_numbers";
        public static final String KEY_CODE_EDITOR_SI_PANEL = "pref_code_editor_symbol_panel";
        public static final String KEY_CODE_EDITOR_FUN_PANEL = "pref_code_editor_fun_panel";
        public static final String KEY_CODE_EDITOR_NAV_PANEL = "pref_code_editor_nav_panel";
        public static final String KEY_CODE_EDITOR_MAGNIFIER = "pref_code_editor_use_magnifier";
        public static final String KEY_CODE_EDITOR_STICKY_SCROLL = "pref_code_editor_sticky_scroll";
        public static final String KEY_CODE_EDITOR_AUTO_CLOSE_BRACKET =
            "pref_code_editor_auto_close_bracket";
        public static final String KEY_CODE_EDITOR_SCROLL_BAR = "pref_code_editor_scroll_bar";
        public static final String KEY_CODE_EDITOR_HARDWARE_ACCELERATION =
            "pref_code_editor_hardware_acceleration";
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
        public static final String KEY_CODE_EDITOR_CLOSE_UNPINNED_PROJECT_PANES =
            "pref_code_editor_close_unpinned_project_panes";
        public static final String KEY_CODE_EDITOR_DEFAULT_FILE_ENCODING =
            "pref_editor_default_file_encoding";
        // General Configuration Preferences
        public static final String KEY_APP_THEME = "pref_app_theme";
        public static final String KEY_DYNAMIC_COLOURS = "pref_dynamic_colours";
        public static final String KEY_BUFFER_SIZE = "pref_general_config_buffer_size";
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
        /**
         * Key for storing the project save path in SharedPreferences.
         */
        public static final String KEY_PROJECT_SAVE_PATH = "projects_save_path";

        private SharedPreferenceKeys() {
            throw new UnsupportedOperationException(
                "This is a utility class and cannot be " + "instantiated");
        }
    }
}
