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

package com.eup.codeopsstudio.common.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;

import com.eup.codeopsstudio.common.Constants;
import com.eup.codeopsstudio.common.ContextManager;
import com.eup.codeopsstudio.common.ILog;
import com.eup.codeopsstudio.res.R;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @author Etido Peter
 */
public class PreferencesUtils {

  private static final String TAG = PreferencesUtils.class.getSimpleName();

  public static boolean isAppFirstLaunch() {
    SharedPreferences prefs = getGlobalPreferences();
    return prefs.getBoolean(Constants.KEY_APP_FIRST_LAUNCH, true);
  }

  public static void setAppFirstLaunchComplete() {
    SharedPreferences prefs = getGlobalPreferences();
    SharedPreferences.Editor editor = prefs.edit();
    editor.putBoolean(Constants.KEY_APP_FIRST_LAUNCH, false);
    editor.apply();
  }

  /**
   * Check if editor automatically saves files.
   *
   * @return true if files are automatically saved, otherwise false.
   */
  public static boolean autoSaveFiles() {
    return getDefaultPreferences()
        .getBoolean(Constants.SharedPreferenceKeys.KEY_CODE_EDITOR_AUTO_SAVE, false);
  }

  public static boolean canCloseRelativeToFirstDepth() {
    var depth =
        getDefaultPreferences()
            .getString(
                Constants.SharedPreferenceKeys.KEY_CODE_EDITOR_RELATIVE_CLOSE_DEPTH, "First");
    if (depth.equalsIgnoreCase("All")) {
      return false;
    } else if (depth.equalsIgnoreCase("First")) {
      return true;
    }
    return true; // default to first tab
  }

  public static boolean canCloseUnPinnedProjectPanes() {
    return getDefaultPreferences()
        .getBoolean(
            Constants.SharedPreferenceKeys.KEY_CODE_EDITOR_CLOSE_UNPINNED_PROJECT_PANES, true);
  }

  public static boolean canDisplayTabIcons() {
    return getDefaultPreferences()
        .getBoolean(Constants.SharedPreferenceKeys.KEY_DISPLAY_TAB_ICONS, true);
  }

  public static boolean canShareAnonymousStatistics() {
    return getDefaultPreferences()
        .getBoolean(Constants.SharedPreferenceKeys.KEY_SHARE_STATISTICS, true);
  }

  public static boolean canShowWelcomePanel() {
    return getDefaultPreferences()
        .getBoolean(Constants.SharedPreferenceKeys.KEY_SHOW_WELCOME_PANE, true);
  }

  public static void clearPreference(@NonNull SharedPreferences pref, String key) {
    Map<String, ?> cues = pref.getAll();
    cues.forEach(
        (k, v) -> {
          if (Objects.equals(key, k)) {
            emptyEditorValue(pref.edit(), k, v);
          }
        });
  }

  private static void emptyEditorValue(
      SharedPreferences.Editor editor, @NonNull String key, @NonNull Object value) {
    try {
      ILog.debug(TAG, String.format(" Key-Value: %s -> %s", key, value));
      if (value instanceof Boolean) {
        editor.putBoolean(key, false).apply();
      } else if (value instanceof Float) {
        editor.putFloat(key, 0.0f);
      } else if (value instanceof String) {
        editor.putString(key, "");
      } else if (value instanceof Integer) {
        editor.putInt(key, 0);
      } else if (value instanceof Long) {
        editor.putLong(key, 0L);
      } else if (value instanceof Set<?>) {
        editor.putStringSet(key, new HashSet<>());
      }
      ILog.debug(TAG, String.format(" Key-Value: %s -> %s", key, value));
      editor.apply();
    } catch (Exception e) {
      ILog.error(TAG, "Failed to clear preference value", e);
    }
  }

  /**
   * Check if the bread crumb navigation panel is displayed.
   *
   * @return true if the navigation panel is displayed, otherwise false.
   */
  public static boolean displayNavigationPanel() {
    return getDefaultPreferences()
        .getBoolean(Constants.SharedPreferenceKeys.KEY_CODE_EDITOR_NAV_PANEL, true);
  }

  /**
   * Check if auto complete is enabled.
   *
   * @return true if auto complete is enabled, otherwise false.
   */
  public static boolean enableAutoComplete() {
    return getDefaultPreferences()
        .getBoolean(Constants.SharedPreferenceKeys.KEY_CODE_EDITOR_AUTO_COMPLETE, false);
  }

  /**
   * Check if auto-complete window animation is enabled.
   *
   * @return true if auto-complete window animation is enabled, otherwise false.
   */
  public static boolean enableAutoCompleteWindowAnimation() {
    return getDefaultPreferences()
        .getBoolean(Constants.SharedPreferenceKeys.KEY_CODE_EDITOR_ANIMATE_AUTO_COMP_WINDOW, false);
  }

  /**
   * Check if bracket auto-closing is enabled.
   *
   * @return true if bracket auto-closing is enabled, otherwise false.
   */
  public static boolean enableBracketAutoClosing() {
    return getDefaultPreferences()
        .getBoolean(Constants.SharedPreferenceKeys.KEY_CODE_EDITOR_AUTO_CLOSE_BRACKET, false);
  }

  /**
   * Check if bracket highlighting is enabled.
   *
   * @return true if bracket highlighting is enabled, otherwise false.
   */
  public static boolean enableBracketHighlight() {
    return getDefaultPreferences()
        .getBoolean(Constants.SharedPreferenceKeys.KEY_CODE_EDITOR_HIGHLIGHT_BRACKET, true);
  }

  /**
   * Check if deleting empty lines is enabled.
   *
   * @return true if deleting empty lines is enabled, otherwise false.
   */
  public static boolean enableDeleteEmptyLine() {
    return getDefaultPreferences()
        .getBoolean(Constants.SharedPreferenceKeys.KEY_CODE_EDITOR_DELETE_EMPTY_LINE, false);
  }

  /**
   * Check if deleting tabs is enabled.
   *
   * @return true if deleting tabs is enabled, otherwise false.
   */
  public static boolean enableDeleteTab() {
    return getDefaultPreferences()
        .getBoolean(Constants.SharedPreferenceKeys.KEY_CODE_EDITOR_DELETE_TAB, false);
  }

  /**
   * Check if hardware acceleration is enabled.
   *
   * @return true if hardware acceleration is enabled, otherwise false.
   */
  public static boolean enableHardWareAcceleration() {
    return getDefaultPreferences()
        .getBoolean(Constants.SharedPreferenceKeys.KEY_CODE_EDITOR_HARDWARE_ACCELERATION, false);
  }

  /**
   * Check if line numbers are enabled.
   *
   * @return true if line numbers are enabled, otherwise false.
   */
  public static boolean enableLineNumbers() {
    return getDefaultPreferences()
        .getBoolean(Constants.SharedPreferenceKeys.KEY_CODE_EDITOR_LINE_NUMBERS, true);
  }

  /**
   * Check if the magnifier is enabled.
   *
   * @return true if the magnifier is enabled, otherwise false.
   */
  public static boolean enableMagnifier() {
    return getDefaultPreferences()
        .getBoolean(Constants.SharedPreferenceKeys.KEY_CODE_EDITOR_MAGNIFIER, true);
  }

  /**
   * Check if the scroll bar is enabled.
   *
   * @return true if the scroll bar is enabled, otherwise false.
   */
  public static boolean enableScrollBar() {
    return getDefaultPreferences()
        .getBoolean(Constants.SharedPreferenceKeys.KEY_CODE_EDITOR_SCROLL_BAR, false);
  }

  /**
   * Check if sticky scroll is enabled.
   *
   * @return true if sticky scroll is enabled, otherwise false.
   */
  public static boolean enableStickyScroll() {
    return getDefaultPreferences()
        .getBoolean(Constants.SharedPreferenceKeys.KEY_CODE_EDITOR_STICKY_SCROLL, true);
  }

  /**
   * Check if the selected np-painting flag is "empty-line".
   *
   * @return true if the selected np-painting flag is "empty-line", otherwise false.
   */
  public static boolean flagEmptyLine() {
    Set<String> selectedValues =
        getDefaultPreferences()
            .getStringSet(
                Constants.SharedPreferenceKeys.KEY_CODE_EDITOR_NP_PAINT_FLAGS, new HashSet<>());
    return selectedValues.contains("4");
  }

  /**
   * Check if the selected np-painting flag is "inner".
   *
   * @return true if the selected np-painting flag is "inner", otherwise false.
   */
  public static boolean flagInner() {
    Set<String> selectedValues =
        getDefaultPreferences()
            .getStringSet(
                Constants.SharedPreferenceKeys.KEY_CODE_EDITOR_NP_PAINT_FLAGS, new HashSet<>());
    return selectedValues.contains("1");
  }

  /**
   * Check if the selected np-painting flag is "leading".
   *
   * @return true if the selected np-painting flag is "leading", otherwise false.
   */
  public static boolean flagLeading() {
    Set<String> selectedValues =
        getDefaultPreferences()
            .getStringSet(
                Constants.SharedPreferenceKeys.KEY_CODE_EDITOR_NP_PAINT_FLAGS, new HashSet<>());
    return selectedValues.contains("2");
  }

  /**
   * Check if the selected np-painting flag is "line-breaks".
   *
   * @return true if the selected np-painting flag is "line-breaks", otherwise false.
   */
  public static boolean flagLineBreaks() {
    Set<String> selectedValues =
        getDefaultPreferences()
            .getStringSet(
                Constants.SharedPreferenceKeys.KEY_CODE_EDITOR_NP_PAINT_FLAGS, new HashSet<>());
    return selectedValues.contains("5");
  }

  /**
   * Check if the selected np-painting flag is "trailing".
   *
   * @return true if the selected np-painting flag is "trailing", otherwise false.
   */
  public static boolean flagTrailing() {
    Set<String> selectedValues =
        getDefaultPreferences()
            .getStringSet(
                Constants.SharedPreferenceKeys.KEY_CODE_EDITOR_NP_PAINT_FLAGS, new HashSet<>());
    return selectedValues.contains("3");
  }

  // =======================
  // Editor Preferences
  // =======================
  public static float getCodeEditorFontSize() {
    return getDefaultPreferences()
        .getFloat(Constants.SharedPreferenceKeys.KEY_CODE_EDITOR_FONT_SIZE, 14);
  }

  /**
   * Get the code editor font size.
   *
   * @param fontSize The default font size.
   * @return The specified font size.
   */
  public static float getCodeEditorFontSize(float fontSize) {
    return getDefaultPreferences()
        .getFloat(Constants.SharedPreferenceKeys.KEY_CODE_EDITOR_FONT_SIZE, fontSize);
  }

  /**
   * Get the selected tab size for the code editor.
   *
   * @return The selected tab size.
   */
  public static int getCodeEditorTabSize() {
    return getCodeEditorTabSize(4);
  }

  public static int getCodeEditorTabSize(int size) {
    return getDefaultPreferences()
        .getInt(Constants.SharedPreferenceKeys.KEY_CODE_EDITOR_TAB_SIZE, size);
  }

  /**
   * Get the user selected buffer size
   *
   * <p>No set method to prevent concurrent modification, buffer size is updated on restart
   *
   * @return the buffer size in kilobytes
   */
  public static int getCurrentBufferSize() {
    var selectedBufferSize =
        getDefaultPreferences().getString(Constants.SharedPreferenceKeys.KEY_BUFFER_SIZE, "5");
    return Integer.parseInt(selectedBufferSize) * 1024;
  }

  /**
   * Get the current editor font.
   *
   * @return The selected editor font.
   */
  public static int getCurrentEditorFont() {
    var selectedFont =
        getDefaultPreferences()
            .getString(
                Constants.SharedPreferenceKeys.KEY_CODE_EDITOR_FONT, "jetbrains_mono_regular");
    return getEditorFont(selectedFont);
  }

  /**
   * Get the editor font based on the selected font value entry.
   *
   * @param selectedFont The font value entry.
   * @return The corresponding editor font.
   */
  private static int getEditorFont(@NonNull String selectedFont) {
    return switch (selectedFont) {
      case "inconsolata_regular" -> R.font.inconsolata_regular;
      case "sourcecodepro_regular" -> R.font.sourcecodepro_regular;
      case "firacode_regular" -> R.font.firacode_regular;
      case "notosans_regular" -> R.font.notosans_regular;
      default -> R.font.jetbrains_mono_regular;
    };
  }

  /**
   * Get the selected line height for the code editor.
   *
   * @return The selected line height.
   */
  public static float getCurrentEditorLineHeight() {
    var selectedLineHeight =
        getDefaultPreferences()
            .getString(Constants.SharedPreferenceKeys.KEY_CODE_EDITOR_LINE_HEIGHT, "2");
    return getEditorLineHeight(selectedLineHeight);
  }

  /**
   * Get the line height value based on the user's choice for the code editor.
   *
   * @param lineHeightEntry The user's line height choice.
   * @return The corresponding line height value.
   */
  private static float getEditorLineHeight(String lineHeightEntry) {
    return switch (lineHeightEntry) {
      case "1" -> 1;
      case "3" -> 3;
      case "4" -> 4;
      default -> 2;
    };
  }

  /**
   * Get the currently selected theme.
   *
   * @return The selected theme.
   */
  public static int getCurrentTheme() {
    var selectedTheme =
        getDefaultPreferences().getString(Constants.SharedPreferenceKeys.KEY_APP_THEME, "3");
    return getCurrentTheme(selectedTheme);
  }

  /**
   * Get the default SharedPreferences.
   *
   * @return The default SharedPreferences.
   */
  public static SharedPreferences getDefaultPreferences() {
    return PreferenceManager.getDefaultSharedPreferences(ContextManager.getApplicationContext());
  }

  /**
   * Get a theme based on the user's choice.
   *
   * @param selectedTheme The selected theme value.
   * @return The corresponding theme.
   */
  public static int getCurrentTheme(@NonNull String selectedTheme) {
    // 1 => Light
    // 2 => Dark
    // 3 => Auto
    switch (selectedTheme) {
      case "1":
        return AppCompatDelegate.MODE_NIGHT_NO;
      case "2":
        return AppCompatDelegate.MODE_NIGHT_YES;
      default:
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
          return AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
        } else {
          return AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY;
        }
    }
  }

  public static int getCursorBlinkPeriod() {
    return getDefaultPreferences()
        .getInt(Constants.SharedPreferenceKeys.KEY_CODE_EDITOR_CURSOR_BLINK_PERIOD, 500);
  }

  /**
   * Get the cursor blink period.
   *
   * @return the cursor blink period.
   */
  public static int getCursorBlinkPeriod(int defaultBlinkPeriod) {
    return getDefaultPreferences()
        .getInt(
            Constants.SharedPreferenceKeys.KEY_CODE_EDITOR_CURSOR_BLINK_PERIOD, defaultBlinkPeriod);
  }

  public static String getDefaultFileEncoding() {
    return getDefaultFileEncoding(Constants.FALLBACK_FILE_ENCODING);
  }

  /**
   * Get the default file encoding
   *
   * @param encoding The fallback encoding in case there is a failure while getting the actual
   * @return A string based file encoding
   */
  public static String getDefaultFileEncoding(String encoding) {
    return getDefaultPreferences()
        .getString(Constants.SharedPreferenceKeys.KEY_CODE_EDITOR_DEFAULT_FILE_ENCODING, encoding);
  }

  /**
   * Get the persistent pane SharedPreferences .
   *
   * @return The SharedPreferences for persisted panes.
   */
  public static SharedPreferences getGlobalPreferences() {
    return ContextManager.getApplicationContext()
        .getSharedPreferences("persistent_panes", Context.MODE_PRIVATE);
  }

  public static SharedPreferences getAppUpdatePreferences() {
    return ContextManager.getApplicationContext()
        .getSharedPreferences(Constants.PREF_APP_UPDATES, Context.MODE_PRIVATE);
  }

  // =======================
  // Other Preferences
  // =======================

  /**
   * Get the last opened project SharedPreferences .
   *
   * @return The SharedPreferences for last opened project.
   */
  public static SharedPreferences getLastOpenedProjectPreferences() {
    return ContextManager.getApplicationContext()
        .getSharedPreferences("last_opened_project", Context.MODE_PRIVATE);
  }

  /**
   * @return true if the user wants to open the last project, otherwise false.
   */
  public static boolean openLastOpenedProject() {
    return getDefaultPreferences()
        .getBoolean(Constants.SharedPreferenceKeys.KEY_OPEN_LAST_OPENED_PROJECT, false);
  }

  /**
   * Check if the line number in the code editor is pinned to the screen on gesture events.
   *
   * @return true if the line number is pinned, otherwise false.
   */
  public static boolean pinLineNumber() {
    return getDefaultPreferences()
        .getBoolean(Constants.SharedPreferenceKeys.KEY_CODE_EDITOR_PIN_LINE_NUM, true);
  }

  public static void setCanShowWelcomePane(boolean canShow) {
    getDefaultPreferences()
        .edit()
        .putBoolean(Constants.SharedPreferenceKeys.KEY_SHOW_WELCOME_PANE, canShow)
        .apply();
  }

  /**
   * Check if dynamic colors should be used based on the user's device API.
   *
   * @return true if dynamic colors should be used, otherwise false.
   */
  public static boolean useDynamicColors() {
    return getDefaultPreferences()
        .getBoolean(Constants.SharedPreferenceKeys.KEY_DYNAMIC_COLOURS, false);
  }

  /**
   * Check if the font ligatures is enabled
   *
   * @return true if the font ligatures is used, otherwise false.
   */
  public static boolean useFontLigatures() {
    return getDefaultPreferences()
        .getBoolean(Constants.SharedPreferenceKeys.KEY_CODE_EDITOR_FONT_LIAGTURES, false);
  }

  /**
   * Check if the ICU library is used for word edge retrieval in the code editor.
   *
   * @return true if the ICU library is used, otherwise false.
   */
  public static boolean useICULibrary() {
    return getDefaultPreferences()
        .getBoolean(Constants.SharedPreferenceKeys.KEY_CODE_EDITOR_ICU, false);
  }

  /**
   * Check if the user wants to use outlined icons.
   *
   * @return true if the user wants to use outlined icons, otherwise false.
   */
  public static boolean useOutLinedIcons() {
    return getDefaultPreferences()
        .getBoolean(Constants.SharedPreferenceKeys.KEY_OUTLINE_ICONS, true);
  }

  /**
   * Check if tabs are used instead of spaces in the code editor.
   *
   * @return true if tabs are used, otherwise false.
   */
  public static boolean useTabIndentation() {
    return getDefaultPreferences()
        .getBoolean(Constants.SharedPreferenceKeys.KEY_CODE_EDITOR_TAB_INDENT, false);
  }

  /**
   * Check if word wrap is enabled for the code editor.
   *
   * @return true if word wrap is enabled, otherwise false.
   */
  public static boolean useWordWrap() {
    return getDefaultPreferences()
        .getBoolean(Constants.SharedPreferenceKeys.KEY_CODE_EDITOR_WORD_WRAP, false);
  }
}
