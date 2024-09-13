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
 
package com.eup.codeopsstudio.editor;

import static android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import static com.eup.codeopsstudio.common.Constants.SharedPreferenceKeys;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;
import com.blankj.utilcode.util.ToastUtils;
import com.eup.codeopsstudio.common.AsyncTask;
import com.eup.codeopsstudio.common.Constants;
import com.eup.codeopsstudio.common.util.PreferencesUtils;
import com.eup.codeopsstudio.editor.event.IndexingEvent;
import com.eup.codeopsstudio.editor.langs.widget.component.ContextualEditorAutoCompletion;
import com.eup.codeopsstudio.editor.langs.widget.component.ContextualEditorCompletionAdapter;
import com.eup.codeopsstudio.editor.langs.widget.component.ContextualEditorTextActionWindow;
import com.eup.codeopsstudio.res.R;
import io.github.rosemoe.sora.lang.EmptyLanguage;
import io.github.rosemoe.sora.langs.textmate.TextMateColorScheme;
import io.github.rosemoe.sora.langs.textmate.TextMateLanguage;
import io.github.rosemoe.sora.langs.textmate.registry.FileProviderRegistry;
import io.github.rosemoe.sora.langs.textmate.registry.GrammarRegistry;
import io.github.rosemoe.sora.langs.textmate.registry.ThemeRegistry;
import io.github.rosemoe.sora.langs.textmate.registry.model.ThemeModel;
import io.github.rosemoe.sora.langs.textmate.registry.provider.AssetsFileResolver;
import io.github.rosemoe.sora.widget.CodeEditor;
import io.github.rosemoe.sora.widget.SymbolPairMatch;
import io.github.rosemoe.sora.widget.component.EditorAutoCompletion;
import io.github.rosemoe.sora.widget.component.EditorTextActionWindow;
import io.github.rosemoe.sora.widget.component.Magnifier;
import java.io.File;
import org.eclipse.tm4e.core.registry.IThemeSource;

public class ContextualCodeEditor extends CodeEditor implements OnSharedPreferenceChangeListener {

  private boolean isIndexing = false;
  private ContextualEditorAutoCompletion editorAutoCompletion;
  private static final String ASSETS_LANGUAGE_GRAMMAR_PATH = "editor/textmate/languages.json";
  private Context context;
  private File mFile;

  public ContextualCodeEditor(Context context) {
    this(context, null);
  }

  public ContextualCodeEditor(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public ContextualCodeEditor(Context context, AttributeSet attrs, int defStyleAttr) {
    this(context, attrs, defStyleAttr, 0);
  }

  public ContextualCodeEditor(
      Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
    initialize(context, attrs, defStyleAttr, defStyleRes);
  }

  /**
   * Prepare editor
   *
   * <p>Initialize variants
   *
   * @see io.github.rosemoe.sora.widget.component.EditorTextActionWindow for editor text compose
   *     panel
   * @see EditorAutoCompletion to set custom EditorAutoCompletion layout Call {@code
   *     #replaceComponent(oldclass, newclass)} to make built-in changes without modularizing
   *     Sora-Code Editor
   */
  private void initialize(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    this.context = context;
    PreferencesUtils.getDefaultPreferences().registerOnSharedPreferenceChangeListener(this);
    setInputType(defaultInputType(true, true, true, true));
    editorAutoCompletion = new ContextualEditorAutoCompletion(this);
    editorAutoCompletion.setAdapter(new ContextualEditorCompletionAdapter());
    // editorAutoCompletion.applyColorScheme();
    replaceComponent(EditorAutoCompletion.class, editorAutoCompletion);
    replaceComponent(EditorTextActionWindow.class, new ContextualEditorTextActionWindow(this));
    updateEditorTypeFace();
    updateEditorTextSize();
    updateEditorTabSize();
    updateEditorStickyScroll();
    updateEditorHardWareAcceleration();
    updateEditorScrollBar();
    updateEditorMagnifier();
    updateEditorWordWrap();
    updateEditorLineNumber();
    updateEditorAutoCompletePanelAnimation();
    updateEditorDeleteEmptyLineFast();
    updateEditorDeleteTabs();
    updateEditorHighlightBracketPair();
    updateEditorLineSpacing();
    updateEditorCursorBlinkPeriod();
    updateEditorNonPrintablePaintingFlags();
  }

  /**
   * Sets the position of the cursor in the editor precisely
   *
   * @see CodeEditor#setSelectionAround(int, int);
   * @param line zero-based line.
   * @param column zero-based column.
   */
  @Override
  public void setSelectionAround(int line, int column) {
    int numberOfLines = getLineCount();
    if (line < numberOfLines) {
      int columnCount = getText().getColumnCount(line);
      if (column > columnCount) {
        column = columnCount;
      }
      setSelection(line, column);
    } else {
      int truncLine = 0;

      if (numberOfLines == 0) {
        truncLine = numberOfLines;
      } else {
        truncLine = numberOfLines - 1;
      }
      setSelection(truncLine, getText().getColumnCount(truncLine));
    }
  }

  @Override
  public synchronized void release() {
    super.release();
    PreferencesUtils.getDefaultPreferences().unregisterOnSharedPreferenceChangeListener(this);
  }

  @Override
  public void onSharedPreferenceChanged(SharedPreferences pref, String key) {
    switch (key) {
      case SharedPreferenceKeys.KEY_CODE_EDITOR_FONT:
        updateEditorTypeFace();
        break;
      case SharedPreferenceKeys.KEY_CODE_EDITOR_FONT_SIZE:
        updateEditorTextSize();
        break;
      case SharedPreferenceKeys.KEY_CODE_EDITOR_TAB_SIZE:
        updateEditorTabSize();
        break;
      case SharedPreferenceKeys.KEY_CODE_EDITOR_STICKY_SCROLL:
        updateEditorStickyScroll();
        break;
      case SharedPreferenceKeys.KEY_CODE_EDITOR_HARDWARE_ACCLERATION:
        updateEditorHardWareAcceleration();
        break;
      case SharedPreferenceKeys.KEY_CODE_EDITOR_SCROLL_BAR:
        updateEditorScrollBar();
        break;
      case SharedPreferenceKeys.KEY_CODE_EDITOR_MAGNIFIER:
        updateEditorMagnifier();
        break;
      case SharedPreferenceKeys.KEY_CODE_EDITOR_WORD_WRAP:
        updateEditorWordWrap();
        break;
      case SharedPreferenceKeys.KEY_CODE_EDITOR_LINE_NUMBERS:
        updateEditorLineNumber();
        break;
      case SharedPreferenceKeys.KEY_CODE_EDITOR_ANIMATE_AUTO_COMP_WINDOW:
        updateEditorAutoCompletePanelAnimation();
        break;
      case SharedPreferenceKeys.KEY_CODE_EDITOR_DELETE_EMPTY_LINE:
        updateEditorDeleteEmptyLineFast();
        break;
      case SharedPreferenceKeys.KEY_CODE_EDITOR_DELETE_TAB:
        updateEditorDeleteTabs();
        break;
      case SharedPreferenceKeys.KEY_CODE_EDITOR_HIGHLIGHT_BRACKET:
        updateEditorHighlightBracketPair();
        break;
      case SharedPreferenceKeys.KEY_CODE_EDITOR_LINE_HEIGHT:
        updateEditorLineSpacing();
        break;
      case SharedPreferenceKeys.KEY_CODE_EDITOR_CURSOR_BLINK_PERIOD:
        updateEditorCursorBlinkPeriod();
        break;
      case SharedPreferenceKeys.KEY_CODE_EDITOR_NP_PAINT_FLAGS:
        updateEditorNonPrintablePaintingFlags();
        break;
    }
  }

  private void updateEditorTypeFace() {
    var typeface = getContext().getResources().getFont(PreferencesUtils.getCurrentEditorFont());
    setTypefaceText(typeface);
    setTypefaceLineNumber(typeface);
  }

  private void updateEditorTextSize() {
    setTextSize(PreferencesUtils.getCodeEditorFontSize());
  }

  private void updateEditorStickyScroll() {
    var enabled = PreferencesUtils.enableStickyScroll();
    getProps().stickyScroll = enabled;
    setStickyScroll(enabled);
    setStickyScrollMaxLines(4);
  }

  private void updateEditorHardWareAcceleration() {
    setHardwareAcceleratedDrawAllowed(PreferencesUtils.enableHardWareAcceleration());
  }

  private void updateEditorScrollBar() {
    setScrollBarEnabled(PreferencesUtils.enableScrollBar());
  }

  private void updateEditorTabSize() {
    setTabWidth(PreferencesUtils.getCodeEditorTabSize());
  }

  private void updateEditorMagnifier() {
    enableMagnifier(PreferencesUtils.enableMagnifier());
  }

  private void updateEditorWordWrap() {
    setWordwrap(PreferencesUtils.useWordWrap());
  }

  private void updateEditorLineNumber() {
    setLineNumberEnabled(PreferencesUtils.enableLineNumbers());
  }

  private void updateEditorAutoCompletePanelAnimation() {
    animateAutoCompletionPanel(PreferencesUtils.enableAutoCompleteWindowAnimation());
  }

  private void updateEditorDeleteEmptyLineFast() {
    deleteEmptyLineFast(PreferencesUtils.enableDeleteEmptyLine());
  }

  private void updateEditorDeleteTabs() {
    deleteTabs(PreferencesUtils.enableDeleteTab());
  }

  private void updateEditorHighlightBracketPair() {
    setHighlightBracketPair(PreferencesUtils.enableBracketHighlight());
  }

  private void updateEditorLineSpacing() {
    setLineSpacing(PreferencesUtils.getCurrentEditorLineHeight(), 1.1f);
  }

  private void updateEditorCursorBlinkPeriod() {
    setCursorBlinkPeriod(PreferencesUtils.getCursorBlinkPeriod());
  }

  private void updateEditorNonPrintablePaintingFlags() {
    var flags =
        applyNonPrintableFlags(
            PreferencesUtils.flagLeading(),
            PreferencesUtils.flagInner(),
            PreferencesUtils.flagTrailing(),
            PreferencesUtils.flagEmptyLine(),
            PreferencesUtils.flagLineBreaks(),
            true,
            false);
    setNonPrintablePaintingFlags(flags);
  }

  public void gotoEnd() {
    setSelection(
        getText().getLineCount() - 1, getText().getColumnCount(getText().getLineCount() - 1));
  }

  public void enableMagnifier(boolean enabled) {
    getComponent(Magnifier.class).setEnabled(enabled);
  }

  public void useICU(boolean enabled) {
    getProps().useICULibToSelectWords = enabled;
  }

  /*
   * Applies a set of non-printable painting flags.
   * This should set the flags dynamically if the flags are enabled from the preferences
   * the flags would be added otherwise #flag would return 0 at that particular flag to disable it
   */
  public int applyNonPrintableFlags(
      boolean FLAG_LEADING,
      boolean FLAG_INNER,
      boolean FLAG_TRAILING,
      boolean FLAG_EMPTY_LINE,
      boolean FLAG_LINE_SEPARATOR,
      boolean FLAG_IN_SELECTION,
      boolean FLAG_TAB_SAME_AS_SPACE) {
    int flags =
        (FLAG_LEADING ? ContextualCodeEditor.FLAG_DRAW_WHITESPACE_LEADING : 0)
            | (FLAG_INNER ? ContextualCodeEditor.FLAG_DRAW_WHITESPACE_INNER : 0)
            | (FLAG_TRAILING ? ContextualCodeEditor.FLAG_DRAW_WHITESPACE_TRAILING : 0)
            | (FLAG_EMPTY_LINE ? ContextualCodeEditor.FLAG_DRAW_WHITESPACE_FOR_EMPTY_LINE : 0)
            | (FLAG_LINE_SEPARATOR ? ContextualCodeEditor.FLAG_DRAW_LINE_SEPARATOR : 0)
            | (FLAG_IN_SELECTION ? ContextualCodeEditor.FLAG_DRAW_WHITESPACE_IN_SELECTION : 0)
            | (FLAG_TAB_SAME_AS_SPACE ? ContextualCodeEditor.FLAG_DRAW_TAB_SAME_AS_SPACE : 0);
    return flags;
  }

  public void navigatePreviousSearch() {
    try {
      getSearcher().gotoPrevious();
    } catch (IllegalStateException e) {
      ToastUtils.showShort(e.getLocalizedMessage());
    }
  }

  public void navigateNextSearch() {
    try {
      getSearcher().gotoNext();
    } catch (IllegalStateException e) {
      ToastUtils.showShort(e.getLocalizedMessage());
    }
  }

  public void replaceSearch(String result) {
    try {
      getSearcher().replaceThis(result);
    } catch (IllegalStateException e) {
      ToastUtils.showShort(e.getLocalizedMessage());
    }
  }

  public void replaceAllSearch(String result) {
    try {
      getSearcher().replaceAll(result);
    } catch (IllegalStateException e) {
      ToastUtils.showShort(e.getLocalizedMessage());
    }
  }

  /**
   * Replace all matched position. Note that after invoking this, a blocking {@link ProgressDialog}
   * is shown until the action is done (either succeeded or failed). The given callback will be
   * executed on success.
   *
   * @param replacement The text for replacement
   * @param onReplacementComplete Callback when action is succeeded
   * @throws IllegalStateException if no search is in progress
   */
  public void replaceAllSearch(String replacement, final Runnable onReplacementComplete) {
    try {
      getSearcher().replaceAll(replacement, onReplacementComplete);
    } catch (IllegalStateException e) {
      ToastUtils.showShort(e.getLocalizedMessage());
    }
  }

  public void setStickyScroll(boolean enabled) {
    getProps().stickyScroll = enabled;
  }

  public void setStickyScrollMaxLines(int maxLines) {
    getProps().stickyScrollMaxLines = maxLines;
  }

  public void animateAutoCompletionPanel(boolean enabled) {
    getComponent(EditorAutoCompletion.class).setEnabledAnimation(enabled);
  }

  public void useICULibrary(boolean enabled) {
    getProps().useICULibToSelectWords = enabled;
  }

  public String getSelectedText() {
    return getSelectedText(true);
  }

  public void deleteEmptyLineFast(boolean deleteEmptyLinesFast) {
    getProps().deleteEmptyLineFast = deleteEmptyLinesFast;
  }

  public void deleteTabs(boolean deleteTabs) {
    getProps().deleteMultiSpaces = deleteTabs ? -1 : 1;
  }

  public String getSelectedText(boolean hasBrackets) {
    var cursor = getCursor();
    if (cursor.isSelected()) {
      if (hasBrackets) {
        return "("
            + (cursor.getRight() - cursor.getLeft())
            + Constants.SPACE
            + context.getString(R.string.selected)
            + ")";
      } else
        return (cursor.getRight() - cursor.getLeft())
            + Constants.SPACE
            + context.getString(R.string.selected);
    }
    return null;
  }

  /** Get the left line the cursor line */
  public int getCursorLinePosition() {
    return 1 + getCursor().getLeftLine();
  }

  /** Get the left cursor column */
  public int getCursorColumnPosition() {
    return getCursor().getLeftColumn();
  }

  /**
   * Get left cursor index
   *
   * @param index The cummulative possible cursor previous positions
   */
  public int getCursorIndex() {
    return getCursor().getLeft();
  }

  public String getMatchingSearchResult() {
    return getMatchingSearchResult(true);
  }

  public String getMatchingSearchResult(boolean hasBrackets) {
    var text = "";
    var searcher = getSearcher();
    if (searcher.hasQuery()) {
      int idx = searcher.getCurrentMatchedPositionIndex();
      int count = searcher.getMatchedPositionCount();

      String matchText =
          (count == 0)
              ? context.getString(R.string.no_search_match)
              : ((count == 1)
                  ? 1 + context.getResources().getQuantityString(R.plurals.search_matches, 1)
                  : count
                      + context.getResources().getQuantityString(R.plurals.search_matches, count));

      if (idx == -1) {
        if (hasBrackets) {
          text = "(" + matchText + ")";
        } else text = matchText;
      } else {
        if (hasBrackets) {
          text =
              "(" + (idx + 1) + context.getString(R.string.of) + Constants.SPACE + matchText + ")";
        } else text = (idx + 1) + context.getString(R.string.of) + Constants.SPACE + matchText;
      }
    }
    return text;
  }

  public boolean isUIDarkMode() {
    return isUIDarkMode(this.context);
  }

  public boolean isUIDarkMode(Context context) {
    if ((context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
        == Configuration.UI_MODE_NIGHT_YES) {
      return true;
    } else {
      return false;
    }
  }

  public static void loadConfigurations(Context context) throws Exception {
    FileProviderRegistry.getInstance().addFileProvider(new AssetsFileResolver(context.getAssets()));

    loadDefaultLanguages();

    String[] themes = new String[] {"darcula", "quietlight"};
    ThemeRegistry themeRegistry = ThemeRegistry.getInstance();
        
    for (String name : themes) {
      var path = "editor/scheme/" + name + ".json";
      themeRegistry.loadTheme(
          new ThemeModel(
              IThemeSource.fromInputStream(
                  FileProviderRegistry.getInstance().tryGetInputStream(path), path, null),
              name));
    }
  }

  /** Use #loadConfigurations(Context) for optimality */
  @Deprecated
  public void loadDefaultTheme() throws Exception {
    // Load default editor themes
    FileProviderRegistry.getInstance().addFileProvider(new AssetsFileResolver(context.getAssets()));
    String[] themes = new String[] {"darcula", "quietlight"};
    ThemeRegistry themeRegistry = ThemeRegistry.getInstance();
    for (String name : themes) {
      var path = "editor/scheme/" + name + ".json";
      themeRegistry.loadTheme(
          new ThemeModel(
              IThemeSource.fromInputStream(
                  FileProviderRegistry.getInstance().tryGetInputStream(path), path, null),
              name));
    }
    // TODO: Handle multiple light themes by using selected entry here
    themeRegistry.setTheme(isUIDarkMode() ? "darcula" : "quietlight");
  }

  /*
   * Call this method when ever you set a new theme
   */
  public void ensureTextmateTheme() throws Exception {
    var editorColorScheme = getColorScheme();
    editorColorScheme = TextMateColorScheme.create(ThemeRegistry.getInstance());
    setColorScheme(editorColorScheme);
    // incase of crash below is suspect
    getComponent(EditorAutoCompletion.class).applyColorScheme();
  }

  /**
   * Updates the previous editor theme with a new one
   *
   * @param themeName the name of theme to be used for update
   */
  public void updateTextMateTheme(String themeName) throws Exception {
    ensureTextmateTheme();
    ThemeRegistry.getInstance().setTheme(themeName);
    resetColorScheme();
  }

  public void resetColorScheme() {
    setColorScheme(getColorScheme());
  }

  /** Updates and sets an editor language */
  public void setEditorLanguage(
      String langScope, boolean autoCompleteEnabled, boolean isAutoCompleteSymbols)
      throws Exception {
    var lang = getEditorLanguage();
    TextMateLanguage language = null;
    if (lang != null) {
      if (lang instanceof EmptyLanguage) {
        language = createTextMateLanguage(langScope, autoCompleteEnabled, isAutoCompleteSymbols);
      } else if (lang instanceof TextMateLanguage) {
        ensureTextmateTheme();
        language = (TextMateLanguage) lang;
        language.updateLanguage(langScope);
      } else if (!(lang instanceof TextMateLanguage)) {
        language = createTextMateLanguage(langScope, autoCompleteEnabled, isAutoCompleteSymbols);
      }
      setEditorLanguage(language);
      resetColorScheme();
    } else {
      throw new RuntimeException("Failed to load editor language");
    }
  }

  private TextMateLanguage createTextMateLanguage(
      String langScope, boolean autoCompleteEnabled, boolean isAutoCompleteSymbols) {
    var tml = TextMateLanguage.create(langScope, autoCompleteEnabled);
    if (isAutoCompleteSymbols) {
      // SymbolPairs
      tml.getSymbolPairs().putPair("{", new SymbolPairMatch.SymbolPair("{", "}"));
      tml.getSymbolPairs().putPair("(", new SymbolPairMatch.SymbolPair("(", ")"));
      tml.getSymbolPairs().putPair("[", new SymbolPairMatch.SymbolPair("[", "]"));
      tml.getSymbolPairs().putPair("\"", new SymbolPairMatch.SymbolPair("\"", "\""));
      tml.getSymbolPairs().putPair("„", new SymbolPairMatch.SymbolPair("„", "„"));
      tml.getSymbolPairs().putPair("“", new SymbolPairMatch.SymbolPair("“", "”"));
      tml.getSymbolPairs().putPair("«", new SymbolPairMatch.SymbolPair("“", "»"));
      tml.getSymbolPairs().putPair("\'", new SymbolPairMatch.SymbolPair("\'", "\'"));
      tml.getSymbolPairs().putPair("‚", new SymbolPairMatch.SymbolPair("‚", "‚"));
      tml.getSymbolPairs().putPair("‘", new SymbolPairMatch.SymbolPair("‘", "’"));
      tml.getSymbolPairs().putPair("‹", new SymbolPairMatch.SymbolPair("‹", "›"));
      tml.getSymbolPairs().putPair("`", new SymbolPairMatch.SymbolPair("`", "`"));
    }
    return tml;
  }

  public static void loadDefaultLanguages() {
    loadDefaultLanguages(ASSETS_LANGUAGE_GRAMMAR_PATH);
  }

  public static void loadDefaultLanguages(String defaultGrammarPath) {
    GrammarRegistry.getInstance().loadGrammars(defaultGrammarPath);
  }

  public void convertSelectionToLowerCase() {
    final var cursor = getCursor();
    final var props = getProps();
        
    if (cursor.isSelected()) {
      int left = cursor.getLeft();
      int right = cursor.getRight();
      int length = right - left;
      
      if (length > 0) {
        final var line = cursor.left().line;
        setIndexing(true);
        AsyncTask.runNonCancelable(
            () -> toLowerCase(getText().substring(left, right)),
            (result) -> {
              setIndexing(false);
              if (result != null) {
                commitText(result);
                setSelectionRegion(line, 0, line, getText().getColumnCount(line)); // reselect line
              }
            });
      }
    } else {
      Toast.makeText(
              getContext(),
              getContext().getString(R.string.editor_select_convert_text_first),
              Toast.LENGTH_SHORT)
          .show();
    }
  }

  public void convertSelectionToUpperCase() {
    final var cursor = getCursor();
    final var props = getProps();
    
    if (cursor.isSelected()) {
      int left = cursor.getLeft();
      int right = cursor.getRight();
      int length = right - left;
      
      if (length > 0) {
        final var line = cursor.left().line;
        setIndexing(true);
        AsyncTask.runNonCancelable(
            () ->  toUpperCase(getText().substring(left, right)),
            (result) -> {
              setIndexing(false);
              if (result != null) {
                commitText(result);
                setSelectionRegion(line, 0, line, getText().getColumnCount(line)); // reselect line
              }
            });
      }
    } else {
      Toast.makeText(
              getContext(),
              getContext().getString(R.string.editor_select_convert_text_first),
              Toast.LENGTH_SHORT)
          .show();
    }
  }

  public void replaceCurrLine() {
    deleteLineText();
    pasteText();
  }

  public void deleteLine() {
    final var cursor = getCursor();
    deleteLine(cursor.isSelected());
  }

  public void deleteLine(boolean isSelected) {
    final var cursor = getCursor();
    if (isSelected) {
      deleteLineText();
      return;
    }

    final var left = cursor.left();
    final var line = left.line;

    if (line + 1 == getLineCount()) {
      setSelectionRegion(line, 0, line, getText().getColumnCount(line));
    } else {
      setSelectionRegion(line, 0, line + 1, 0);
    }

    deleteLineText();
  }

  private void deleteLineText() {
    final var cursor = getCursor();
    if (cursor.isSelected()) {
      deleteText();
      notifyIMEExternalCursorChange();
    } else {
      deleteLine();
    }
  }
  
  public interface ProgressListener {
     void onProgress(int progress);
  }
  
  private String toUpperCase(String input) {
    return toUpperCase(input, null);
  }
  
  private String toUpperCase(String input, ProgressListener listener) {
    var result = new StringBuilder();
    
    for (int i = 0; i < input.length(); i++) {
      var current = input.charAt(i);
      result.append(Character.toUpperCase(current));
      
      if (listener != null) {
         // Update progress
         int progress = (i + 1) * 100 / input.length();
         listener.onProgress(progress);
      }
    }
    
    return result.toString();
  }
  
  private String toLowerCase(String input) {
    return toLowerCase(input, null);
  }
  
  private String toLowerCase(String input, ProgressListener listener) {
    var result = new StringBuilder();
    
    for (int i = 0; i < input.length(); i++) {
      var current = input.charAt(i);
      result.append(Character.toLowerCase(current));
      
      if (listener != null) {
         // Update progress
         int progress = (i + 1) * 100 / input.length();
         listener.onProgress(progress);
      }
    }
    
    return result.toString();
  }

  /**
   * editor input type + no suggestions flag
   *
   * @return The default editor input type
   */
  private int defaultInputType(
      boolean TYPE_CLASS_TEXT,
      boolean TYPE_TEXT_FLAG_MULTI_LINE,
      boolean TYPE_TEXT_FLAG_NO_SUGGESTIONS,
      boolean TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
    int flags = 0;
    if (TYPE_CLASS_TEXT) {
      flags |= EditorInfo.TYPE_CLASS_TEXT;
    }
    if (TYPE_TEXT_FLAG_MULTI_LINE) {
      flags |= EditorInfo.TYPE_TEXT_FLAG_MULTI_LINE;
    }
    if (TYPE_TEXT_FLAG_NO_SUGGESTIONS) {
      flags |= EditorInfo.TYPE_TEXT_FLAG_NO_SUGGESTIONS;
    }
    if (TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
      flags |= EditorInfo.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD;
    }
    return flags;
  }

  /**
   * editor input type + no suggestions flag
   *
   * @return The default editor input type
   */
  private int createEditorInputType() {
    return EditorInfo.TYPE_CLASS_TEXT
        | EditorInfo.TYPE_TEXT_FLAG_MULTI_LINE
        | EditorInfo.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD;
  }

  /**
   * Called when the editor is loading a function
   *
   * @param state The current state of the function to load
   */
  public void setIndexing(boolean state) {
    isIndexing = state;
    dispatchEvent(new IndexingEvent(this, state));
  }

  /**
   * @see CodeEditorPane TODO: In {@code CodeEditorPane} show loading progress if return val is true
   * @return if The editor is indexing
   */
  public boolean isIndexing() {
    return isIndexing;
  }

  public File getFile() {
    return this.mFile;
  }

  public void setFile(File mFile) {
    this.mFile = mFile;
  }

  public String getFilePath() {
    return mFile.getAbsolutePath();
  }

  public void refreshEditorLanguageSyntax(String langSope, boolean enableAutoComplete) {
    try {
      setEditorLanguage(langSope, enableAutoComplete, PreferencesUtils.enableBracketAutoClosing());

    } catch (Exception e) {
      ToastUtils.showShort(e.getLocalizedMessage());
    }
  }
}
