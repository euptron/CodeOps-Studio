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
 
   package com.eup.codeopsstudio.common.util;

import static com.eup.codeopsstudio.common.Constants.SharedPreferenceKeys;

import android.app.Activity;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;
import com.google.android.material.color.DynamicColors;
import com.eup.codeopsstudio.common.Constants;
import com.eup.codeopsstudio.common.ContextManager;
import com.eup.codeopsstudio.res.R;
import java.util.HashSet;
import java.util.Set;

public class PreferencesUtils {

  /**
   * Get the default SharedPreferences.
   *
   * @return The default SharedPreferences.
   */
  public static SharedPreferences getDefaultPreferences() {
    return PreferenceManager.getDefaultSharedPreferences(ContextManager.getApplicationContext());
  }

  /**
   * Get the SharedPreferences for the advertisement
   *
   * @return The advertisement SharedPreferences.
   */
  public static SharedPreferences getAdvertisementPreferences() {
    return ContextManager.getApplicationContext()
        .getSharedPreferences("advertisement_pref", Activity.MODE_PRIVATE);
  }

  /**
   * Get the default SharedPreferences for the file treeview.
   *
   * @return The SharedPreferences for the file treeview.
   */
  public static SharedPreferences getFileTreePrefs() {
    return ContextManager.getApplicationContext()
        .getSharedPreferences("file_tree", Activity.MODE_PRIVATE);
  }

  /**
   * Get the last opened project SharedPreferences .
   *
   * @return The SharedPreferences for last opened project.
   */
  public static SharedPreferences getLastOpenedProjectPreferences() {
    return ContextManager.getApplicationContext()
        .getSharedPreferences("last_opened_project", Activity.MODE_PRIVATE);
  }

  /**
   * Get the persistent pane SharedPreferences .
   *
   * @return The SharedPreferences for persisted panes.
   */
  public static SharedPreferences getPersistentPanesPreferences() {
    return ContextManager.getApplicationContext()
        .getSharedPreferences("persistent_panes", Activity.MODE_PRIVATE);
  }

  /**
   * Get the currently selected theme.
   *
   * @return The selected theme.
   */
  public static int getCurrentTheme() {
    var selectedTheme = getDefaultPreferences().getString(SharedPreferenceKeys.KEY_APP_THEME, "3");
    return getCurrentTheme(selectedTheme);
  }

  /**
   * Get a theme based on the user's choice.
   *
   * @param selectedTheme The selected theme value.
   * @return The corresponding theme.
   */
  public static int getCurrentTheme(String selectedTheme) {
    switch (selectedTheme) {
      case "2":
        return AppCompatDelegate.MODE_NIGHT_YES;
      case "1":
        return AppCompatDelegate.MODE_NIGHT_NO;
      default:
        if (SDKUtil.isAtLeast(SDKUtil.API.ANDROID_11)) {
          return AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
        } else {
          return AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY;
        }
    }
  }

  /**
   * Check if dynamic colors should be used based on the user's device API.
   *
   * @return true if dynamic colors should be used, otherwise false.
   */
  public static boolean useDynamicColors() {
    if (SDKUtil.isAtLeast(SDKUtil.API.ANDROID_12) && DynamicColors.isDynamicColorAvailable()) {
      return getDefaultPreferences().getBoolean(SharedPreferenceKeys.KEY_DYNAMIC_COLOURS, false);
    } else {
      return getDefaultPreferences().getBoolean(SharedPreferenceKeys.KEY_DYNAMIC_COLOURS, false);
    }
  }

  // =======================
  // Editor Preferences
  // =======================
  public static float getCodeEditorFontSize() {
    return getDefaultPreferences().getFloat(SharedPreferenceKeys.KEY_CODE_EDITOR_FONT_SIZE, 14);
  }

  /**
   * Get the code editor font size.
   *
   * @param fontSize The default font size.
   * @return The specified font size.
   */
  public static float getCodeEditorFontSize(float fontSize) {
    return getDefaultPreferences()
        .getFloat(SharedPreferenceKeys.KEY_CODE_EDITOR_FONT_SIZE, fontSize);
  }

  /**
   * Get the current editor font.
   *
   * @return The selected editor font.
   */
  public static int getCurrentEditorFont() {
    var selectedFont =
        getDefaultPreferences()
            .getString(SharedPreferenceKeys.KEY_CODE_EDITOR_FONT, "jetbrains_mono_regular");
    return getEditorFont(selectedFont);
  }

  /**
   * Get the editor font based on the selected font value entry.
   *
   * @param selectedFont The font value entry.
   * @return The corresponding editor font.
   */
  private static int getEditorFont(String selectedFont) {
    switch (selectedFont) {
      case "inconsolata_regular":
        return R.font.inconsolata_regular;
      case "sourcecodepro_regular":
        return R.font.sourcecodepro_regular;
      case "firacode_regular":
        return R.font.firacode_regular;
      case "jetbrains_mono_regular":
        return R.font.jetbrains_mono_regular;
      case "notosans_regular":
        return R.font.notosans_regular;
      default:
        return R.font.jetbrains_mono_regular;
    }
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
    return getDefaultPreferences().getInt(SharedPreferenceKeys.KEY_CODE_EDITOR_TAB_SIZE, size);
  }

  /**
   * Check if the selected np-painting flag is "inner".
   *
   * @return true if the selected np-painting flag is "inner", otherwise false.
   */
  public static boolean flagInner() {
    Set<String> selectedValues =
        getDefaultPreferences()
            .getStringSet(SharedPreferenceKeys.KEY_CODE_EDITOR_NP_PAINT_FLAGS, new HashSet<>());
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
            .getStringSet(SharedPreferenceKeys.KEY_CODE_EDITOR_NP_PAINT_FLAGS, new HashSet<>());
    return selectedValues.contains("2");
  }

  /**
   * Check if the selected np-painting flag is "trailing".
   *
   * @return true if the selected np-painting flag is "trailing", otherwise false.
   */
  public static boolean flagTrailing() {
    Set<String> selectedValues =
        getDefaultPreferences()
            .getStringSet(SharedPreferenceKeys.KEY_CODE_EDITOR_NP_PAINT_FLAGS, new HashSet<>());
    return selectedValues.contains("3");
  }

  /**
   * Check if the selected np-painting flag is "empty-line".
   *
   * @return true if the selected np-painting flag is "empty-line", otherwise false.
   */
  public static boolean flagEmptyLine() {
    Set<String> selectedValues =
        getDefaultPreferences()
            .getStringSet(SharedPreferenceKeys.KEY_CODE_EDITOR_NP_PAINT_FLAGS, new HashSet<>());
    return selectedValues.contains("4");
  }

  /**
   * Check if the selected np-painting flag is "line-breaks".
   *
   * @return true if the selected np-painting flag is "line-breaks", otherwise false.
   */
  public static boolean flagLineBreaks() {
    Set<String> selectedValues =
        getDefaultPreferences()
            .getStringSet(SharedPreferenceKeys.KEY_CODE_EDITOR_NP_PAINT_FLAGS, new HashSet<>());
    return selectedValues.contains("5");
  }

  /**
   * Get the selected line height for the code editor.
   *
   * @return The selected line height.
   */
  public static float getCurrentEditorLineHeight() {
    var selectedLineHeight =
        getDefaultPreferences().getString(SharedPreferenceKeys.KEY_CODE_EDITOR_LINE_HEIGHT, "3");
    return getEditorLineHeight(selectedLineHeight);
  }

  /**
   * Get the line height value based on the user's choice for the code editor.
   *
   * @param lineHeightEntry The user's line height choice.
   * @return The corresponding line height value.
   */
  private static float getEditorLineHeight(String lineHeightEntry) {
    switch (lineHeightEntry) {
      case "1":
        return 1;
      case "2":
        return 2;
      case "3":
        return 3;
      case "4":
        return 4;
      default:
        return 2;
    }
  }

  /**
   * Check if word wrap is enabled for the code editor.
   *
   * @return true if word wrap is enabled, otherwise false.
   */
  public static boolean useWordWrap() {
    return getDefaultPreferences()
        .getBoolean(SharedPreferenceKeys.KEY_CODE_EDITOR_WORD_WRAP, false);
  }

  /**
   * Check if tabs are used instead of spaces in the code editor.
   *
   * @return true if tabs are used, otherwise false.
   */
  public static boolean useTabIndentation() {
    return getDefaultPreferences()
        .getBoolean(SharedPreferenceKeys.KEY_CODE_EDITOR_TAB_INDENT, false);
  }

  /**
   * Check if the ICU library is used for word edge retrieval in the code editor.
   *
   * @return true if the ICU library is used, otherwise false.
   */
  public static boolean useICULibrary() {
    return getDefaultPreferences().getBoolean(SharedPreferenceKeys.KEY_CODE_EDITOR_ICU, false);
  }

  /**
   * Check if editor automatically saves files.
   *
   * @return true if files are automatically saved, otherwise false.
   */
  public static boolean autoSaveFiles() {
    return getDefaultPreferences()
        .getBoolean(SharedPreferenceKeys.KEY_CODE_EDITOR_AUTO_SAVE, false);
  }

  /**
   * Check if the line number in the code editor is pinned to the screen on gesture events.
   *
   * @return true if the line number is pinned, otherwise false.
   */
  public static boolean pinLineNumber() {
    return getDefaultPreferences()
        .getBoolean(SharedPreferenceKeys.KEY_CODE_EDITOR_PIN_LINE_NUM, true);
  }

  /**
   * Check if the symbol input panel is displayed.
   *
   * @return true if the symbol input panel is displayed, otherwise false.
   */
  public static boolean displaySIPanel() {
    return getDefaultPreferences().getBoolean(SharedPreferenceKeys.KEY_CODE_EDITOR_SI_PANEL, false);
  }

  /**
   * Check if the function panel is displayed.
   *
   * @return true if the function panel is displayed, otherwise false.
   */
  public static boolean displayFunctionPanel() {
    return getDefaultPreferences()
        .getBoolean(SharedPreferenceKeys.KEY_CODE_EDITOR_FUN_PANEL, false);
  }

  /**
   * Check if the bread crumb navigation panel is displayed.
   *
   * @return true if the navigation panel is displayed, otherwise false.
   */
  public static boolean displayNavigationPanel() {
    return getDefaultPreferences().getBoolean(SharedPreferenceKeys.KEY_CODE_EDITOR_NAV_PANEL, true);
  }

  /**
   * Check if the magnifier is enabled.
   *
   * @return true if the magnifier is enabled, otherwise false.
   */
  public static boolean enableMagnifier() {
    return getDefaultPreferences().getBoolean(SharedPreferenceKeys.KEY_CODE_EDITOR_MAGNIFIER, true);
  }

  /**
   * Check if sticky scroll is enabled.
   *
   * @return true if sticky scroll is enabled, otherwise false.
   */
  public static boolean enableStickyScroll() {
    return getDefaultPreferences()
        .getBoolean(SharedPreferenceKeys.KEY_CODE_EDITOR_STICKY_SCROLL, true);
  }

  /**
   * Check if bracket auto-closing is enabled.
   *
   * @return true if bracket auto-closing is enabled, otherwise false.
   */
  public static boolean enableBracketAutoClosing() {
    return getDefaultPreferences()
        .getBoolean(SharedPreferenceKeys.KEY_CODE_EDITOR_AUTO_CLOSE_BRACKET, false);
  }

  /**
   * Check if the scroll bar is enabled.
   *
   * @return true if the scroll bar is enabled, otherwise false.
   */
  public static boolean enableScrollBar() {
    return getDefaultPreferences()
        .getBoolean(SharedPreferenceKeys.KEY_CODE_EDITOR_SCROLL_BAR, false);
  }

  /**
   * Check if hardware acceleration is enabled.
   *
   * @return true if hardware acceleration is enabled, otherwise false.
   */
  public static boolean enableHardWareAcceleration() {
    return getDefaultPreferences()
        .getBoolean(SharedPreferenceKeys.KEY_CODE_EDITOR_HARDWARE_ACCLERATION, false);
  }

  /**
   * Check if line numbers are enabled.
   *
   * @return true if line numbers are enabled, otherwise false.
   */
  public static boolean enableLineNumbers() {
    return getDefaultPreferences()
        .getBoolean(SharedPreferenceKeys.KEY_CODE_EDITOR_LINE_NUMBERS, true);
  }

  /**
   * Check if deleting empty lines is enabled.
   *
   * @return true if deleting empty lines is enabled, otherwise false.
   */
  public static boolean enableDeleteEmptyLine() {
    return getDefaultPreferences()
        .getBoolean(SharedPreferenceKeys.KEY_CODE_EDITOR_DELETE_EMPTY_LINE, false);
  }

  /**
   * Check if deleting tabs is enabled.
   *
   * @return true if deleting tabs is enabled, otherwise false.
   */
  public static boolean enableDeleteTab() {
    return getDefaultPreferences()
        .getBoolean(SharedPreferenceKeys.KEY_CODE_EDITOR_DELETE_TAB, false);
  }

  /**
   * Check if auto-complete window animation is enabled.
   *
   * @return true if auto-complete window animation is enabled, otherwise false.
   */
  public static boolean enableAutoCompleteWindowAnimation() {
    return getDefaultPreferences()
        .getBoolean(SharedPreferenceKeys.KEY_CODE_EDITOR_ANIMATE_AUTO_COMP_WINDOW, false);
  }

  /**
   * Check if bracket highlighting is enabled.
   *
   * @return true if bracket highlighting is enabled, otherwise false.
   */
  public static boolean enableBracketHighlight() {
    return getDefaultPreferences()
        .getBoolean(SharedPreferenceKeys.KEY_CODE_EDITOR_HIGHLIGHT_BRACKET, true);
  }

  /**
   * Check if auto complete is enabled.
   *
   * @return true if auto complete is enabled, otherwise false.
   */
  public static boolean enableAutoComplete() {
    return getDefaultPreferences()
        .getBoolean(SharedPreferenceKeys.KEY_CODE_EDITOR_AUTO_COMPLETE, false);
  }

  public static int getCursorBlinkPeriod() {
    return getDefaultPreferences()
        .getInt(SharedPreferenceKeys.KEY_CODE_EDITOR_CURSOR_BLINK_PERIOD, 500);
  }

  /**
   * Get the cursor blink period.
   *
   * @return the cursor blink period.
   */
  public static int getCursorBlinkPeriod(int defaultBlinkPeriod) {
    return getDefaultPreferences()
        .getInt(SharedPreferenceKeys.KEY_CODE_EDITOR_CURSOR_BLINK_PERIOD, defaultBlinkPeriod);
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
        .getString(SharedPreferenceKeys.KEY_CODE_EDITOR_DEFAULT_FILE_ENCODING, encoding);
  }

  /**
   * Set the default file encoding
   *
   * @param encoding The fallback encoding in case there is a failure while getting the actual
   */
  public static void setDefaultFileEncoding(String encoding) {
    getDefaultPreferences()
        .edit()
        .putString(SharedPreferenceKeys.KEY_CODE_EDITOR_DEFAULT_FILE_ENCODING, encoding)
        .apply();
  }
  
  public static boolean canCloseUnPinnedProjectPanes() {
    return getDefaultPreferences().getBoolean(SharedPreferenceKeys.KEY_CODE_EDITOR_CLOSE_UNPINNED_PROJECT_PANES, true);
  }

  public static void setCloseUnPinnedProjectPanes(boolean closeUnPinned) {
    getDefaultPreferences()
        .edit()
        .putBoolean(SharedPreferenceKeys.KEY_CODE_EDITOR_CLOSE_UNPINNED_PROJECT_PANES, closeUnPinned)
        .apply();
  }
  // =======================
  // Other Preferences
  // =======================

  /**
   * Check if the user wants to use the Google JSON formatter.
   *
   * @return true if the user wants to use the Google JSON formatter, otherwise false.
   */
  public static boolean useGoogleJsonFormatter() {
    return getDefaultPreferences()
        .getBoolean(SharedPreferenceKeys.KEY_GOOGLE_JSON_FORMATTER, false);
  }

  /**
   * @return true if the user wants to open the last project, otherwise false.
   */
  public static boolean openLastOpenedProject() {
    return getDefaultPreferences()
        .getBoolean(SharedPreferenceKeys.KEY_OPEN_LAST_OPENED_PROJECT, false);
  }

  /**
   * Check if the user wants to use outlined icons.
   *
   * @return true if the user wants to use outlined icons, otherwise false.
   */
  public static boolean useOutLinedIcons() {
    return getDefaultPreferences().getBoolean(SharedPreferenceKeys.KEY_OUTLINE_ICONS, true);
  }

  /**
   * Get an item from a specified resource array at a given index.
   *
   * @param resource The resource array.
   * @param index The index of the item to retrieve.
   * @return The item at the specified index from the resource array.
   */
  public static String getItem(int resource, int index) {
    CharSequence[] choices =
        ContextManager.getApplicationContext().getResources().getStringArray(resource);
    return choices[index].toString();
  }

  public static boolean canCloseRelativeToFirstDepth() {
    var depth =
        getDefaultPreferences()
            .getString(SharedPreferenceKeys.KEY_CODE_EDITOR_RELATIVE_CLOSE_DEPTH, "First");
    if (depth.equalsIgnoreCase("All")) {
      return false;
    } else if (depth.equalsIgnoreCase("First")) {
      return true;
    }
    return true; // default to first tab
  }

  public static boolean canShowWelcomePanel() {
    return getDefaultPreferences().getBoolean(SharedPreferenceKeys.KEY_SHOW_WELCOME_PANE, true);
  }

  public static void setCanShowWelcomePane(boolean canShow) {
    getDefaultPreferences()
        .edit()
        .putBoolean(SharedPreferenceKeys.KEY_SHOW_WELCOME_PANE, canShow)
        .apply();
  }

  public static boolean canShareAnynomousStatistics() {
    return getDefaultPreferences().getBoolean(SharedPreferenceKeys.KEY_SHARE_STATISTICS, true);
  }

  public static void enableShareAnynomousStatistics(boolean enabled) {
    getDefaultPreferences()
        .edit()
        .putBoolean(SharedPreferenceKeys.KEY_SHARE_STATISTICS, enabled)
        .apply();
  }
  
  public static boolean clearPerference(SharedPreferences pref, String key) {
    return pref.edit().putString(key, "").commit();
  }
}
