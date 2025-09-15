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

package com.eup.codeopsstudio.editor;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.text.InputType;
import android.util.AttributeSet;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.eup.codeopsstudio.common.AsyncTask;
import com.eup.codeopsstudio.common.Constants;
import com.eup.codeopsstudio.common.ILog;
import com.eup.codeopsstudio.common.util.PreferencesUtils;
import com.eup.codeopsstudio.editor.event.IndexingEvent;
import com.eup.codeopsstudio.editor.langs.widget.component.ContextualEditorAutoCompletion;
import com.eup.codeopsstudio.editor.langs.widget.component.ContextualEditorCompletionAdapter;
import com.eup.codeopsstudio.editor.langs.widget.component.ContextualEditorTextActionWindow;

import org.eclipse.tm4e.core.registry.IThemeSource;

import java.io.File;
import java.util.Objects;

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
import io.github.rosemoe.sora.widget.schemes.EditorColorScheme;

public class ContextualCodeEditor extends CodeEditor implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String TAG = "ContextualCodeEditor";
    private static final String ASSETS_LANGUAGE_GRAMMAR_PATH = "editor/textmate/languages.json";
    private boolean isIndexing = false;
    private Context context;
    private File mFile;
    private String languageExtension;

    public ContextualCodeEditor(Context context) {
        this(context, null);
    }

    public ContextualCodeEditor(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ContextualCodeEditor(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ContextualCodeEditor(Context context, AttributeSet attrs, int defStyleAttr,
        int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initialize(context);
    }

    /**
     * Prepare editor
     *
     * <p>Initialize variants
     *
     * @see io.github.rosemoe.sora.widget.component.EditorTextActionWindow for editor text compose
     * panel
     * @see EditorAutoCompletion to set custom EditorAutoCompletion layout Call {@code
     * #replaceComponent(oldclass, newclass)} to make built-in changes without modularizing
     * Sora-Code Editor
     */
    private void initialize(Context context) {
        ContextualEditorAutoCompletion editorAutoCompletion;
        this.context = context;
        PreferencesUtils
            .getDefaultPreferences()
            .registerOnSharedPreferenceChangeListener(this);
        setInputType(defaultInputType(true, true, true, true));
        editorAutoCompletion = new ContextualEditorAutoCompletion(this);
        editorAutoCompletion.setAdapter(new ContextualEditorCompletionAdapter());
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
        updateEditorFontLiagtures();
        updateEditorPinLineNumber();
    }

    private void updateEditorPinLineNumber() {
        var pinLineNumber = PreferencesUtils.pinLineNumber();
        setPinLineNumber(pinLineNumber);
    }

    private void updateEditorFontLiagtures() {
        var fontligatureEnabled = PreferencesUtils.useFontLigatures();
        setLigatureEnabled(fontligatureEnabled);
    }

    private void updateEditorTypeFace() {
        var typeface = getContext()
            .getResources()
            .getFont(PreferencesUtils.getCurrentEditorFont());
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

    public void setStickyScroll(boolean enabled) {
        getProps().stickyScroll = enabled;
    }

    public void setStickyScrollMaxLines(int maxLines) {
        getProps().stickyScrollMaxLines = maxLines;
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

    public void enableMagnifier(boolean enabled) {
        getComponent(Magnifier.class).setEnabled(enabled);
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

    public void animateAutoCompletionPanel(boolean enabled) {
        getComponent(EditorAutoCompletion.class).setEnabledAnimation(enabled);
    }

    private void updateEditorDeleteEmptyLineFast() {
        deleteEmptyLineFast(PreferencesUtils.enableDeleteEmptyLine());
    }

    public void deleteEmptyLineFast(boolean deleteEmptyLinesFast) {
        getProps().deleteEmptyLineFast = deleteEmptyLinesFast;
    }

    private void updateEditorDeleteTabs() {
        deleteTabs(PreferencesUtils.enableDeleteTab());
    }

    public void deleteTabs(boolean deleteTabs) {
        getProps().deleteMultiSpaces = deleteTabs ? -1 : 1;
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
        var flags = applyNonPrintableFlags(PreferencesUtils.flagLeading(),
            PreferencesUtils.flagInner(), PreferencesUtils.flagTrailing(),
            PreferencesUtils.flagEmptyLine(), PreferencesUtils.flagLineBreaks(), true, false);
        setNonPrintablePaintingFlags(flags);
    }

    /*
     * Applies a set of non-printable painting flags.
     * This should set the flags dynamically if the flags are enabled from the preferences
     * the flags would be added otherwise #flag would return 0 at that particular flag to disable it
     */
    public int applyNonPrintableFlags(boolean leading, boolean inner, boolean trailing,
        boolean emptyLine, boolean lineSeparator, boolean inSelection, boolean tabSameAsSpace) {
        return (leading ? CodeEditor.FLAG_DRAW_WHITESPACE_LEADING : 0) | (inner
            ? CodeEditor.FLAG_DRAW_WHITESPACE_INNER : 0) | (trailing
            ? CodeEditor.FLAG_DRAW_WHITESPACE_TRAILING : 0) | (emptyLine
            ? CodeEditor.FLAG_DRAW_WHITESPACE_FOR_EMPTY_LINE : 0) | (lineSeparator
            ? CodeEditor.FLAG_DRAW_LINE_SEPARATOR : 0) | (inSelection
            ? CodeEditor.FLAG_DRAW_WHITESPACE_IN_SELECTION : 0) | (tabSameAsSpace
            ? CodeEditor.FLAG_DRAW_TAB_SAME_AS_SPACE : 0);
    }

    /**
     * editor input type + no suggestions flag
     *
     * @return The default editor input type
     */
    private int defaultInputType(boolean typeClassText, boolean typeTextFlagMultiLine,
        boolean typeTextFlagNoSuggestions, boolean typeTextVariationVisiblePassword) {
        int flags = 0;
        if (typeClassText) {
            flags |= InputType.TYPE_CLASS_TEXT;
        }
        if (typeTextFlagMultiLine) {
            flags |= InputType.TYPE_TEXT_FLAG_MULTI_LINE;
        }
        if (typeTextFlagNoSuggestions) {
            flags |= InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS;
        }
        if (typeTextVariationVisiblePassword) {
            flags |= InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD;
        }
        return flags;
    }

    public static void loadConfigurations(@NonNull Context context) throws Exception {
        FileProviderRegistry
            .getInstance()
            .addFileProvider(new AssetsFileResolver(context.getAssets()));

        loadDefaultLanguages();

        String[] themes = new String[]{"darcula", "quietlight"};
        ThemeRegistry themeRegistry = ThemeRegistry.getInstance();

        for (String name : themes) {
            var path = "editor/scheme/" + name + ".json";
            var is = FileProviderRegistry
                .getInstance()
                .tryGetInputStream(path);
            if (is != null) {
                themeRegistry.loadTheme(new ThemeModel(IThemeSource.fromInputStream(is, path,
                    null), name));
            } else {
                ILog.warning(TAG, "Failed to load configs, provider inputstream is null");
            }
        }
    }

    public static void loadDefaultLanguages() {
        loadDefaultLanguages(ASSETS_LANGUAGE_GRAMMAR_PATH);
    }

    public static void loadDefaultLanguages(String defaultGrammarPath) {
        GrammarRegistry
            .getInstance()
            .loadGrammars(defaultGrammarPath);
    }

    public String getLanguageExtension() {
        return languageExtension;
    }

    public boolean isUIDarkMode() {
        return isUIDarkMode(this.context);
    }

    public boolean isUIDarkMode(Context context) {
        return (context
            .getResources()
            .getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
            == Configuration.UI_MODE_NIGHT_YES;
    }

    /**
     * Sets the position of the cursor in the editor precisely
     *
     * @param line   zero-based line.
     * @param column zero-based column.
     * @see CodeEditor#setSelectionAround(int, int);
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
            int truncLine;

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
        PreferencesUtils
            .getDefaultPreferences()
            .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences pref, @Nullable String key) {
        Objects.requireNonNull(key);
        switch (key) {
            case Constants.SharedPreferenceKeys.KEY_CODE_EDITOR_FONT:
                updateEditorTypeFace();
                break;
            case Constants.SharedPreferenceKeys.KEY_CODE_EDITOR_FONT_SIZE:
                updateEditorTextSize();
                break;
            case Constants.SharedPreferenceKeys.KEY_CODE_EDITOR_TAB_SIZE:
                updateEditorTabSize();
                break;
            case Constants.SharedPreferenceKeys.KEY_CODE_EDITOR_STICKY_SCROLL:
                updateEditorStickyScroll();
                break;
            case Constants.SharedPreferenceKeys.KEY_CODE_EDITOR_HARDWARE_ACCELERATION:
                updateEditorHardWareAcceleration();
                break;
            case Constants.SharedPreferenceKeys.KEY_CODE_EDITOR_SCROLL_BAR:
                updateEditorScrollBar();
                break;
            case Constants.SharedPreferenceKeys.KEY_CODE_EDITOR_MAGNIFIER:
                updateEditorMagnifier();
                break;
            case Constants.SharedPreferenceKeys.KEY_CODE_EDITOR_WORD_WRAP:
                updateEditorWordWrap();
                break;
            case Constants.SharedPreferenceKeys.KEY_CODE_EDITOR_LINE_NUMBERS:
                updateEditorLineNumber();
                break;
            case Constants.SharedPreferenceKeys.KEY_CODE_EDITOR_ANIMATE_AUTO_COMP_WINDOW:
                updateEditorAutoCompletePanelAnimation();
                break;
            case Constants.SharedPreferenceKeys.KEY_CODE_EDITOR_DELETE_EMPTY_LINE:
                updateEditorDeleteEmptyLineFast();
                break;
            case Constants.SharedPreferenceKeys.KEY_CODE_EDITOR_DELETE_TAB:
                updateEditorDeleteTabs();
                break;
            case Constants.SharedPreferenceKeys.KEY_CODE_EDITOR_HIGHLIGHT_BRACKET:
                updateEditorHighlightBracketPair();
                break;
            case Constants.SharedPreferenceKeys.KEY_CODE_EDITOR_LINE_HEIGHT:
                updateEditorLineSpacing();
                break;
            case Constants.SharedPreferenceKeys.KEY_CODE_EDITOR_CURSOR_BLINK_PERIOD:
                updateEditorCursorBlinkPeriod();
                break;
            case Constants.SharedPreferenceKeys.KEY_CODE_EDITOR_NP_PAINT_FLAGS:
                updateEditorNonPrintablePaintingFlags();
                break;
            case Constants.SharedPreferenceKeys.KEY_CODE_EDITOR_FONT_LIAGTURES:
                updateEditorFontLiagtures();
                break;
            case Constants.SharedPreferenceKeys.KEY_CODE_EDITOR_PIN_LINE_NUM:
                updateEditorPinLineNumber();
                break;
            case Constants.SharedPreferenceKeys.KEY_CODE_EDITOR_ICU:
                useICULibrary(PreferencesUtils.useICULibrary());
                break;
            default: // Nothing
        }
    }

    public void gotoEnd() {
        setSelection(
            getText().getLineCount() - 1, getText().getColumnCount(getText().getLineCount() - 1));
    }

    public void navigatePreviousSearch() {
        try {
            getSearcher().gotoPrevious();
        } catch (IllegalStateException e) {
            toast(e.getLocalizedMessage());
        }
    }

    private void toast(String message) {
        Toast
            .makeText(getContext(), message, Toast.LENGTH_SHORT)
            .show();
    }

    public void navigateNextSearch() {
        try {
            getSearcher().gotoNext();
        } catch (IllegalStateException e) {
            toast(e.getLocalizedMessage());
        }
    }

    public void replaceSearch(String result) {
        try {
            getSearcher().replaceThis(result);
        } catch (IllegalStateException e) {
            toast(e.getLocalizedMessage());
        }
    }

    public void replaceAllSearch(String result) {
        try {
            getSearcher().replaceAll(result);
        } catch (IllegalStateException e) {
            toast(e.getLocalizedMessage());
        }
    }

    /**
     * Replace all matched position. Note that after invoking this, a blocking
     * {@link ProgressDialog}
     * is shown until the action is done (either succeeded or failed). The given callback will be
     * executed on success.
     *
     * @param replacement           The text for replacement
     * @param onReplacementComplete Callback when action is succeeded
     * @throws IllegalStateException if no search is in progress
     */
    public void replaceAllSearch(String replacement, final Runnable onReplacementComplete) {
        try {
            getSearcher().replaceAll(replacement, onReplacementComplete);
        } catch (IllegalStateException e) {
            toast(e.getLocalizedMessage());
        }
    }

    public void useICULibrary(boolean enabled) {
        getProps().useICULibToSelectWords = enabled;
    }

    public String getSelectedText() {
        return getSelectedText(true);
    }

    public String getSelectedText(boolean hasBrackets) {
        var cursor = getCursor();
        if (cursor.isSelected()) {
            if (hasBrackets) {
                return "(" + (cursor.getRight() - cursor.getLeft()) + Constants.SPACE
                    + context.getString(R.string.editor_selected) + ")";
            } else {
                return (cursor.getRight() - cursor.getLeft()) + Constants.SPACE
                    + context.getString(R.string.editor_selected);
            }
        }
        return null;
    }

    /**
     * Get the left line the cursor line
     */
    public int getCursorLinePosition() {
        return 1 + getCursor().getLeftLine();
    }

    /**
     * Get the left cursor column
     */
    public int getCursorColumnPosition() {
        return getCursor().getLeftColumn();
    }

    /**
     * Get left cursor index
     *
     * @return the index of cumulative possible cursor previous positions
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
        if (!searcher.hasQuery()) return text;

        int idx = searcher.getCurrentMatchedPositionIndex();
        int count = searcher.getMatchedPositionCount();

        String matchText;
        if (count == 0) {
            matchText = context.getString(R.string.editor_no_search_match);
        } else {
            matchText = (count == 1) ? 1 + context
                .getResources()
                .getQuantityString(R.plurals.editor_search_matches, 1) : count + context
                .getResources()
                .getQuantityString(R.plurals.editor_search_matches, count);
        }

        if (idx == -1) {
            if (hasBrackets) {
                text = "(" + matchText + ")";
            } else {
                text = matchText;
            }
        } else {
            if (hasBrackets) {
                text = "(" + (idx + 1) + Constants.SEPARATOR + matchText + ")";
            } else {
                text = (idx + 1) + Constants.SEPARATOR + matchText;
            }
        }
        return text;
    }

    /**
     * Updates the previous editor theme with a new one
     *
     * @param themeName the name of theme to be used for update
     */
    public void updateTextMateTheme(String themeName) throws Exception {
        ensureTextmateTheme();
        ThemeRegistry
            .getInstance()
            .setTheme(themeName);
        resetColorScheme();
    }

    /*
     * Call this method when ever you set a new theme
     */
    public void ensureTextmateTheme() throws Exception {
        EditorColorScheme editorColorScheme = getColorScheme();
        if (!(editorColorScheme instanceof TextMateColorScheme)) {
            editorColorScheme = TextMateColorScheme.create(ThemeRegistry.getInstance());
            setColorScheme(editorColorScheme);
            // in case of crash below is suspect
            getComponent(EditorAutoCompletion.class).applyColorScheme();
        }
    }

    public void resetColorScheme() {
        setColorScheme(getColorScheme());
    }

    public void convertSelectionToLowerCase() {
        final var cursor = getCursor();

        if (cursor.isSelected()) {
            int left = cursor.getLeft();
            int right = cursor.getRight();
            int length = right - left;

            if (length > 0) {
                final var line = cursor.left().line;
                setIndexing(true);
                AsyncTask
                    .runProvideError(() -> getText().substring(left, right))
                    .thenApply(this::toLowerCase)
                    .thenAccept(result -> this.post(() -> {
                        setIndexing(false);
                        if (result != null) {
                            commitText(result);
                            setSelectionRegion(line, 0, line, getText().getColumnCount(line)); //
                            // reselect line
                        } else {
                            toast(R.string.editor_unable_to_format);
                        }
                    }));
            }
        } else {
            toast(R.string.editor_select_convert_text_first);
        }
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

    private void toast(@StringRes int message) {
        Toast
            .makeText(getContext(), message, Toast.LENGTH_SHORT)
            .show();
    }

    public void convertSelectionToUpperCase() {
        final var cursor = getCursor();

        if (cursor.isSelected()) {
            int left = cursor.getLeft();
            int right = cursor.getRight();
            int length = right - left;

            if (length > 0) {
                final var line = cursor.left().line;
                setIndexing(true);
                AsyncTask
                    .runProvideError(() -> getText().substring(left, right))
                    .thenApply(this::toUpperCase)
                    .thenAccept(result -> this.post(() -> {
                        setIndexing(false);
                        if (result != null) {
                            commitText(result);
                            setSelectionRegion(line, 0, line, getText().getColumnCount(line)); //
                            // reselect line
                        } else {
                            toast(R.string.editor_unable_to_format);
                        }
                    }));
            }
        } else {
            toast(R.string.editor_select_convert_text_first);
        }
    }

    @NonNull
    private String toUpperCase(String input) {
        return toUpperCase(input, null);
    }

    @NonNull
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

    public boolean isIndexing() {
        return isIndexing;
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

    public File getFile() {
        return this.mFile;
    }

    public void setFile(File mFile) {
        this.mFile = mFile;
    }

    public String getFilePath() {
        return mFile.getAbsolutePath();
    }

    public void refreshEditorLanguageSyntax(String languageExtension, String langScope,
        boolean autoCompleteWindowEnabled, boolean enableBracketAutoClosing) {
        try {
            this.languageExtension = languageExtension;
            setEditorLanguage(languageExtension, langScope, autoCompleteWindowEnabled,
                enableBracketAutoClosing, true);
        } catch (Exception e) {
            toast(e.getLocalizedMessage());
        }
    }

    /**
     * Updates and sets an editor language
     */
    public void setEditorLanguage(String languageExtension, String langScope,
        boolean autoCompleteWindowEnabled, boolean isAutoCompleteSymbols,
        boolean isRefreshing) throws Exception {
        this.languageExtension = languageExtension;
        var lang = getEditorLanguage();
        TextMateLanguage language;
        if (isRefreshing) {
            ensureTextmateTheme();
            language = (TextMateLanguage) lang;
            language.updateLanguage(langScope);
        } else {
            language = createTextMateLanguage(langScope, autoCompleteWindowEnabled,
                isAutoCompleteSymbols);
        }

        setEditorLanguage(language);
        resetColorScheme();
    }

    private TextMateLanguage createTextMateLanguage(String langScope,
        boolean autoCompleteWindowEnabled, boolean isAutoCompleteSymbols) {
        var tml = TextMateLanguage.create(langScope, autoCompleteWindowEnabled);
        if (isAutoCompleteSymbols) {
            // SymbolPairs
            tml
                .getSymbolPairs()
                .putPair("{", new SymbolPairMatch.SymbolPair("{", "}"));
            tml
                .getSymbolPairs()
                .putPair("(", new SymbolPairMatch.SymbolPair("(", ")"));
            tml
                .getSymbolPairs()
                .putPair("[", new SymbolPairMatch.SymbolPair("[", "]"));
            tml
                .getSymbolPairs()
                .putPair("\"", new SymbolPairMatch.SymbolPair("\"", "\""));
            tml
                .getSymbolPairs()
                .putPair("„", new SymbolPairMatch.SymbolPair("„", "„"));
            tml
                .getSymbolPairs()
                .putPair("“", new SymbolPairMatch.SymbolPair("“", "”"));
            tml
                .getSymbolPairs()
                .putPair("«", new SymbolPairMatch.SymbolPair("“", "»"));
            tml
                .getSymbolPairs()
                .putPair("'", new SymbolPairMatch.SymbolPair("'", "'"));
            tml
                .getSymbolPairs()
                .putPair("‚", new SymbolPairMatch.SymbolPair("‚", "‚"));
            tml
                .getSymbolPairs()
                .putPair("‘", new SymbolPairMatch.SymbolPair("‘", "’"));
            tml
                .getSymbolPairs()
                .putPair("‹", new SymbolPairMatch.SymbolPair("‹", "›"));
            tml
                .getSymbolPairs()
                .putPair("`", new SymbolPairMatch.SymbolPair("`", "`"));
        }
        return tml;
    }

    public interface ProgressListener {
        void onProgress(int progress);
    }
}
