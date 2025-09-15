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

package com.eup.codeopsstudio.ui.editor.code;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.Editable;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;
import androidx.core.util.Pair;

import com.eup.codeopsstudio.IdeApplication;
import com.eup.codeopsstudio.common.AsyncTask;
import com.eup.codeopsstudio.common.Constants;
import com.eup.codeopsstudio.common.ILog;
import com.eup.codeopsstudio.common.util.FileUtil;
import com.eup.codeopsstudio.common.util.PreferencesUtils;
import com.eup.codeopsstudio.common.util.TextWatcherAdapter;
import com.eup.codeopsstudio.databinding.LayoutCodeEditorBinding;
import com.eup.codeopsstudio.domain.events.EditorModificationEvent;
import com.eup.codeopsstudio.editor.ContextualCodeEditor;
import com.eup.codeopsstudio.editor.event.IndexingEvent;
import com.eup.codeopsstudio.editor.langs.textmate.provider.JsonLanguageInfoProvider;
import com.eup.codeopsstudio.models.logger.Logger;
import com.eup.codeopsstudio.pane.Pane;
import com.eup.codeopsstudio.R;
import com.eup.codeopsstudio.databinding.LayoutDialogTextInputBinding;
import com.eup.codeopsstudio.databinding.LayoutReplaceInFileBinding;
import com.eup.codeopsstudio.ui.editor.code.breadcrumb.pane.CrumbTreePane;
import com.eup.codeopsstudio.util.BaseUtil;
import com.eup.codeopsstudio.util.BinaryFileChecker ;
import com.eup.codeopsstudio.util.EncodingDetector;
import com.eup.codeopsstudio.util.Wizard;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.apache.commons.io.FileUtils;
import org.greenrobot.eventbus.EventBus;
import org.jetbrains.annotations.Contract;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.regex.PatternSyntaxException;

import io.github.rosemoe.sora.event.ContentChangeEvent;
import io.github.rosemoe.sora.event.EventReceiver;
import io.github.rosemoe.sora.event.PublishSearchResultEvent;
import io.github.rosemoe.sora.event.SelectionChangeEvent;
import io.github.rosemoe.sora.langs.textmate.registry.ThemeRegistry;
import io.github.rosemoe.sora.widget.EditorSearcher;

/**
 * CodeEditorPane is a pane subset to handle code editing
 *
 * <p>TODO
 *
 * <ol>
 *   <li>Support to select custom syntax highlighting
 *   <li>Reload editor file
 *   <li>Reload editor file with charset
 *   <li>Save as
 *   <li>Statistics
 *   <li>Support 'Smooth mode' @see CodeEditor#setBasicDisplayMode
 *   <li>Use editor color scheme to paint bread-crumbs and other editor components
 * </ol>
 *
 * @author Etido Peter
 * @version 0.0.5
 * @see Pane
 */
public class CodeEditorPane extends Pane implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String TAG = "CodeEditorPane";
    private static final String LANG_SCOPE_PATH = "editor/textmate/language_scopes.json";
    private static final int CONTENT_CHANGE_CHECK_DELAY_MS = 50;
    private final Logger logger = new Logger(Logger.LogClass.IDE);
    private PopupMenu searchMenu;
    private File mEditorFile;
    private LayoutCodeEditorBinding binding;
    private EditorSearcher.SearchOptions searchOptions = new EditorSearcher.SearchOptions(false,
        false);
    private int isMatchCaseSelected = -1;
    private int selectedItem = -1;
    private boolean isModified = false;
    private boolean isStoppingSearch = false;

    public CodeEditorPane(Context context, String title) {
        this(context, title, /* generate new uuid= */ true);
    }

    public CodeEditorPane(Context context, String title, boolean generateUUID) {
        super(context, title, generateUUID);
    }

    @Override
    public View onCreateView() {
        binding = LayoutCodeEditorBinding.inflate(LayoutInflater.from(getContext()));
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view) {
        super.onViewCreated(view);
        logger.attach(requireActivity() /*shared activity scope*/);

        PreferencesUtils
            .getDefaultPreferences()
            .registerOnSharedPreferenceChangeListener(this);

        try {
            ThemeRegistry
                .getInstance()
                .setTheme(binding.editor.isUIDarkMode() ? "darcula" : "quietlight");
            binding.editor.ensureTextmateTheme();
            binding.editor.resetColorScheme();
        } catch (Exception e) {
            logger.w(TAG, e.getMessage());
        }

        updateAlertVisibility(false);

        binding.searchPanel.prev.setOnClickListener(v -> binding.editor.navigatePreviousSearch());
        binding.searchPanel.next.setOnClickListener(v -> binding.editor.navigateNextSearch());
        binding.searchPanel.replace.setOnClickListener(v -> displayTextReplacementDialog());
        binding.searchPanel.moreOptions.setOnClickListener(v -> initSearchPanelMenu());

        updateCrumbPanelVisibility();
        binding.breadCrumbBar.setFile(mEditorFile);
        if (binding.breadCrumbBar.getAdapter() != null) {
            binding.breadCrumbBar
                .getAdapter()
                .setOnItemClickListener((anchorView, crumb, position) -> new CrumbTreePane(getContext(), anchorView).setPath(crumb.getFilePath()));
        }

        readFile(mEditorFile);
        checkEditorConfigurations();
    }

    @Override
    public void onSelected() {
        super.onSelected();
        if (binding != null) binding.editor.requestFocus();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        PreferencesUtils
            .getDefaultPreferences()
            .unregisterOnSharedPreferenceChangeListener(this);
        if (!binding.editor.isReleased()) {
            binding.editor.release();
        }
        binding = null;
    }

    @Override
    public void persist() {
        super.persist();
        var cursor = binding.editor.getCursor();
        addArguments("left_column", cursor.getLeftColumn());
        addArguments("left_line", cursor.getLeftLine());
        addArguments("file_path", getFilePath());
        addArguments("editor_content", binding.editor
            .getText()
            .toString());
    }

    public String getFilePath() {
        return mEditorFile.getAbsolutePath();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences pref, String key) {
        if (Objects.equals(key, Constants.SharedPreferenceKeys.KEY_CODE_EDITOR_NAV_PANEL)) {
            updateCrumbPanelVisibility();
        } else if (Objects.equals(key,
            Constants.SharedPreferenceKeys.KEY_CODE_EDITOR_AUTO_CLOSE_BRACKET)) {
            refreshEditorLanguageSyntax();
        }
    }

    public void refreshEditorLanguageSyntax() {
        refreshEditorLanguageSyntax(PreferencesUtils.enableAutoComplete(),
            PreferencesUtils.enableBracketAutoClosing());
    }

    public void refreshEditorLanguageSyntax(boolean enableAutoCompleteWindow,
        boolean enableBracketAutoClosing) {
        try {
            Pair<String, String> languageInfo = getEditorLanguageInfo(mEditorFile);
            if (languageInfo == null) {
                ILog.debug(TAG, "Failed to refresh editor language configurations");
                return;
            }
            String langExt = languageInfo.first;
            String langScope = languageInfo.second;
            binding.editor.refreshEditorLanguageSyntax(langExt, langScope,
                enableAutoCompleteWindow, enableBracketAutoClosing);
        } catch (Exception e) {
            var err = "Failed to refresh editor language configurations";
            logger.e(TAG, err);
            ILog.debug(TAG, err, e);
        }
    }

    @Nullable
    private Pair<String, String> getEditorLanguageInfo(File file) throws IOException {
        if (isInvalidContext()) return null;

        InputStream is = requireContext()
            .getAssets()
            .open(LANG_SCOPE_PATH);
        var provider = new JsonLanguageInfoProvider(is);
        String scope = provider.getScope(FileUtil.getFileExtension(file));
        String extension = provider.getLanguageExtension(scope);
        return new Pair<>(extension, scope);
    }

    private boolean isInvalidContext() {
        if (getContext() == null) {
            logger.w(TAG, "INVALID EDITOR STATE! RESTART IS REQUIRED");
            ILog.debug(TAG, "Context is null");
            return true;
        }
        return false;
    }

    private void updateCrumbPanelVisibility() {
        if (binding != null) {
            binding.breadCrumbBar.setVisible(PreferencesUtils.displayNavigationPanel());
        }
    }

    public File getFile() {
        return mEditorFile;
    }

    public void setFile(File file) {
        this.mEditorFile = file;
    }

    private void readFile(@NonNull File file) {
        setLoading(true);
        Charset detectedCharset =
            EncodingDetector.detectFileEncoding(PreferencesUtils.getCurrentBufferSize(), file);
        try {
            if (!EncodingDetector.isSupportedEncoding(detectedCharset) || isBinaryFile(file)) {
                logger.d(TAG,
                    "Unsupported charset detected: " + detectedCharset.name() + " " + "for "
                        + "file " + file.getName());
                updateAlertVisibility(true);
                openSearchPanel(false);
            }
        } catch (Exception e) {
            logger.e(TAG, e.getMessage());
        }
        if (detectedCharset != null) readFileWithCharset(file, detectedCharset);
    }

    private void readFileWithCharset(@NonNull File file, @NonNull Charset charset) {
        AsyncTask.runNonCancelable(() -> FileUtils.readFileToString(file, charset), (result,
            throwable) -> {
            setLoading(false);
            if (result != null) {
                binding.editor.setText(result, null);
                loadEditorLanguage(file);
                logger.i(TAG, getString(R.string.act_code_editor_pane_open_file, getTitle(),
                    file.getAbsolutePath()));
            }
            if (throwable != null) {
                String errorMessage = String.format("%s %s%n%s",
                    getString(R.string.alrt_text_parsing_error), file.getAbsolutePath(),
                    throwable.getLocalizedMessage());
                logger.e(TAG, errorMessage);
            }
        });
    }

    private void setLoading(boolean loading) {
        if (binding == null) return;
        binding.progressbar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private void loadEditorLanguage(File file) {
        try {
            Pair<String, String> languageInfo = getEditorLanguageInfo(file);
            if (languageInfo == null) return;
            String extension = languageInfo.first;
            String scope = languageInfo.second;
            binding.editor.setEditorLanguage(extension, scope,
                PreferencesUtils.enableAutoComplete(),
                PreferencesUtils.enableBracketAutoClosing(), false);
        } catch (Exception e) {
            var err = "Failed to load editor language configurations";
            logger.w(TAG, err);
            ILog.error(TAG, err, e);
        }
    }

    /**
     * Checks if a file is binary.
     *
     * @param file The file to be checked.
     * @return True if the file contains binary content; false if it is not binary.
     */
    private boolean isBinaryFile(File file) throws IOException {
        return BinaryFileChecker.isBinaryFile(file, false, PreferencesUtils.getCurrentBufferSize());
    }

    private void updateAlertVisibility(boolean isAlert) {
        if (isAlert) {
            openSearchPanel(false);
            binding.editor.setVisibility(View.GONE);
            binding.editorAlertLayout.rootContainer.setVisibility(View.VISIBLE);
            binding.breadCrumbBar.setVisibility(View.GONE);
            binding.editorAlertLayout.actionButton.setText(getString(R.string.open_anyway));
            binding.editorAlertLayout.alertMessage.setText(getString(R.string.alrt_unsupported_txt_encoding));
            binding.editorAlertLayout.actionButton.setOnClickListener(v -> updateAlertVisibility(false));
        } else {
            binding.editor.setVisibility(View.VISIBLE);
            binding.editorAlertLayout.rootContainer.setVisibility(View.GONE);
            binding.breadCrumbBar.setVisibility(View.VISIBLE);
        }
    }

    private void initSearchPanelMenu() {
        if (isInvalidContext()) return;

        searchMenu = new PopupMenu(requireContext(), binding.searchPanel.moreOptions);
        searchMenu.inflate(com.eup.codeopsstudio.R.menu.menu_search_options);
        searchMenu.setOnMenuItemClickListener(this::onMenuItemClick);

        if (selectedItem != -1) {
            searchMenu
                .getMenu()
                .findItem(selectedItem)
                .setChecked(true);
        }
        if (isMatchCaseSelected != -1) {
            searchMenu
                .getMenu()
                .findItem(isMatchCaseSelected)
                .setChecked(true);
        }
        searchMenu.show();
    }

    private boolean onMenuItemClick(MenuItem item) {
        boolean isChecked = item.isChecked();
        item.setChecked(!isChecked);

        final int itemId = item.getItemId();
        final int regexId = com.eup.codeopsstudio.R.id.search_option_regex;
        final int wholeWordId = com.eup.codeopsstudio.R.id.search_option_whole_word;
        final int matchCaseId = com.eup.codeopsstudio.R.id.search_option_match_case;
        final int closeId = com.eup.codeopsstudio.R.id.close_search_options;

        if (itemId == regexId) {
            selectedItem = isChecked ? -1 : regexId;
        } else if (itemId == wholeWordId) {
            selectedItem = isChecked ? -1 : wholeWordId;
        } else if (itemId == matchCaseId) {
            isMatchCaseSelected = isChecked ? -1 : matchCaseId;
        } else if (itemId == closeId) {
            getEditor()
                .getSearcher()
                .stopSearch();
            openSearchPanel(false);
        }

        boolean ignoreCase = !searchMenu
            .getMenu()
            .findItem(com.eup.codeopsstudio.R.id.search_option_match_case)
            .isChecked();
        boolean regex = searchMenu
            .getMenu()
            .findItem(com.eup.codeopsstudio.R.id.search_option_regex)
            .isChecked();
        boolean wholeWord = searchMenu
            .getMenu()
            .findItem(com.eup.codeopsstudio.R.id.search_option_whole_word)
            .isChecked();

        int searchType = EditorSearcher.SearchOptions.TYPE_NORMAL;
        if (regex) {
            searchType = EditorSearcher.SearchOptions.TYPE_REGULAR_EXPRESSION;
        } else if (wholeWord) {
            searchType = EditorSearcher.SearchOptions.TYPE_WHOLE_WORD;
        }

        searchOptions = new EditorSearcher.SearchOptions(searchType, ignoreCase);
        commitSearch();
        return true;
    }

    private void commitSearch() {
        if (isStoppingSearch) return;

        var query = binding.searchPanel.searchInput.getEditableText();
        if (!query
            .toString()
            .isEmpty()) {
            try {
                binding.editor
                    .getSearcher()
                    .search(query.toString(), searchOptions);
            } catch (PatternSyntaxException e) {
                logger.e(TAG, "Failed to commit search " + e.getMessage());
            }
        } else {
            binding.editor
                .getSearcher()
                .stopSearch();
        }
    }

    private void updatePositionText() {
        if (!binding.searchPanel.searchInput
            .getEditableText()
            .toString()
            .isEmpty()) {
            binding.searchPanel.searchResult.setText(binding.editor.getMatchingSearchResult(false));
        } else {
            binding.searchPanel.searchResult.setText(binding.editor.getSelectedText(false));
        }
    }

    private void displayTextReplacementDialog() {
        if (isInvalidContext()) return;

        var inflate = LayoutReplaceInFileBinding.inflate(LayoutInflater.from(getContext()));
        var builder = new MaterialAlertDialogBuilder(requireContext());

        builder.setView(inflate.getRoot());
        builder.setTitle(R.string.replace_in_file);
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.setPositiveButton(R.string.replace, (dialog, which) -> {
            if (inflate.tilName.getEditText() != null) {
                binding.editor.replaceSearch(inflate.tilName
                    .getEditText()
                    .getText()
                    .toString());
            } else {
                logger.e(TAG, "Text replacement failed, text to replace is empty");
            }
        });

        builder.setNeutralButton(R.string.replaceAll, (dialog, which) -> {
            if (inflate.tilName.getEditText() != null) {
                binding.editor.replaceAllSearch(inflate.tilName
                    .getEditText()
                    .getText()
                    .toString());
            } else {
                logger.e(TAG, "Text replacement failed, text to replace is empty");
            }
        });
        builder.show();
    }

    public void undo() {
        if (binding == null) return;
        if (binding.editor.canUndo()) {
            binding.editor.undo();
        }
    }

    public void redo() {
        if (binding == null) return;
        if (binding.editor.canRedo()) {
            binding.editor.redo();
        }
    }

    public boolean canUndo() {
        return binding != null && binding.editor.canUndo();
    }

    public boolean canRedo() {
        return binding != null && binding.editor.canRedo();
    }

    @Nullable
    @Contract(pure = true)
    public EventReceiver<IndexingEvent> indexingEventReceiver() {
        if (binding == null) return null;

        return (event, data) -> {
            ContextualCodeEditor cce = (ContextualCodeEditor) event.getEditor();
            binding.progressbar.setVisibility(cce.isIndexing() ? View.VISIBLE : View.GONE);
        };
    }

    private void enableEditorFeatures() {
        binding.searchPanel.searchInput.addTextChangedListener(new TextWatcherAdapter() {
            @Override
            public void afterTextChanged(@NonNull Editable s) {
                if (isStoppingSearch) return;
                commitSearch();
            }
        });

        binding.editor.subscribeEvent(SelectionChangeEvent.class,
            (event, data) -> updatePositionText());

        binding.editor.subscribeEvent(ContentChangeEvent.class,
            (event, data) -> binding.editor.postDelayedInLifecycle(() -> {
            if (mEditorFile == null) {
                return;
            }

            if (!mEditorFile.exists()) {
                logger.i(TAG,
                    "Failed to read editor modification status: File does " + "not " + "exist - "
                        + mEditorFile.getAbsolutePath() + " Try " + "saving" + " " + "this editor");
                return;
            }

            AsyncTask.runNonCancelable(() -> {
                String editorContent = binding.editor
                    .getText()
                    .toString();
                int bufferSize = PreferencesUtils.getCurrentBufferSize();
                var cs = EncodingDetector.detectFileEncoding(bufferSize, mEditorFile);
                var originalFileContent = FileUtils.readFileToString(mEditorFile, cs);
                return !originalFileContent.contentEquals(editorContent);
            }, (isEditorModified, th) -> {
                if (th == null) {
                    setModified(isEditorModified);
                } else {
                    logger.e(TAG, "Failed to read editor modification status: " + th.getMessage());
                }
            });
        }, CONTENT_CHANGE_CHECK_DELAY_MS));

        binding.editor.subscribeEvent(PublishSearchResultEvent.class,
            (event, data) -> updatePositionText());
        binding.editor.subscribeEvent(IndexingEvent.class, indexingEventReceiver());
        updatePositionText();
    }

    public void openSearchPanel(boolean opened) {
        openSearchPanel(opened, false);
    }

    /**
     * Gets the visibility code of the search panel.
     *
     * @return the visibility code of the search panel {@link View#getVisibility()}
     */
    public int getSearchPanelVisibility() {
        return binding.searchPanel
            .getRoot()
            .getVisibility();
    }

    public void doJumpToLine() {
        if (binding == null) return;

        int totalLineCount = binding.editor.getLineCount();
        if (totalLineCount == -1) return;

        var hint = String.format("%s...%s", 1, totalLineCount);
        final var inflate = LayoutDialogTextInputBinding.inflate(LayoutInflater.from(getContext()));

        var dialog = new MaterialAlertDialogBuilder(Objects.requireNonNull(getContext()));
        dialog.setView(inflate.getRoot());
        dialog.setTitle(R.string.menu_jump_to_line);
        dialog.setNegativeButton(R.string.cancel, null);
        dialog.setCancelable(false);

        inflate.tilName.setHint(hint);
        Objects
            .requireNonNull(inflate.tilName.getEditText())
            .setInputType(InputType.TYPE_CLASS_NUMBER);

        dialog.setOnDismissListener(di -> {
            var str = inflate.tilName
                .getEditText()
                .getText()
                .toString();
            if (!Wizard.isEmpty(str) && Integer.parseInt(str) <= totalLineCount) {
                di.dismiss();
            }
        });
        dialog.setPositiveButton(R.string.ok, (d, which) -> {
            var lineToJump = Integer.parseInt(inflate.tilName
                .getEditText()
                .getText()
                .toString());
            if (lineToJump > totalLineCount) {
                inflate.tilName.setError(getString(R.string.msg_invalid_jump_line));
            } else {
                inflate.tilName.setErrorEnabled(false);
                binding.editor.jumpToLine((lineToJump == 0) ? lineToJump : lineToJump - 1);
            }
        });
        dialog.show();
    }

    public boolean isModified() {
        return isModified;
    }

    public void setModified(boolean modified) {
        isModified = modified;
        EventBus
            .getDefault()
            .post(new EditorModificationEvent(modified));
    }

    public void makeReadOnly(boolean readOnly) {
        if (binding == null) return;

        if (readOnly) {
            binding.editor.setEditable(false);
            openSearchPanel(false, true);
        } else {
            binding.editor.setEditable(true);
            openSearchPanel(false, false);
        }
    }

    public void openSearchPanel(boolean opened, boolean disableReplace) {
        binding.searchPanel.replace.setEnabled(!disableReplace);

        if (opened) {
            isStoppingSearch = false;
            binding.editor
                .getSearcher()
                .stopSearch();
            binding.searchPanel
                .getRoot()
                .setVisibility(View.VISIBLE);
            BaseUtil.showSoftInput(binding.searchPanel.searchInput);
        } else {
            isStoppingSearch = true;
            binding.editor
                .getSearcher()
                .stopSearch();
            binding.searchPanel
                .getRoot()
                .setVisibility(View.GONE);
        }
    }

    public boolean isReadOnlyMode() {
        if (binding != null) {
            return !binding.editor.isEditable();
        }
        return false;
    }

    public void saveEditor() {
        saveEditor(true);
    }

    /**
     * Clear persisted content {@see BaseFragment} for how the editor contents are persisted
     *
     * @param recreateIfDeleted recreates the editor file in case it was deleted
     */
    public void saveEditor(boolean recreateIfDeleted) {
        AsyncTask.runOnUiThread(() -> getEditor().setIndexing(true));

        AsyncTask.runNonCancelable(() -> {
            if (recreateIfDeleted && !mEditorFile.exists() && mEditorFile.createNewFile()) {
                logger.i(TAG, "File recreated for " + getTitle() + " editor");
            }

            saveEditorContent(mEditorFile, binding.editor
                .getText()
                .toString());
            return null;
        }, (result, throwable) -> {
            if (throwable == null) {
                addArguments("editor_content", ""); // persisted editor content
            } else {
                logger.e(TAG, "Unable to save file: " + mEditorFile.getAbsolutePath() + "\nReason: "
                    + throwable.getMessage());
            }
        });
    }

    public ContextualCodeEditor getEditor() {
        return binding.editor;
    }

    private void saveEditorContent(File file, String content) throws IOException {
        Charset encoding = EncodingDetector.getEncoding(PreferencesUtils.getDefaultFileEncoding());
        FileUtils.writeStringToFile(file, content, encoding);
        setModified(false);
    }

    private void checkEditorConfigurations() {
        IdeApplication app = IdeApplication.getInstance();
        CompletableFuture<Void> configFuture = app.getEditorConfigFuture();

        getEditor().setIndexing(true);

        configFuture.thenAcceptAsync(aVoid -> {
            getEditor().setIndexing(false);
            enableEditorFeatures();
        }, ContextCompat.getMainExecutor(requireContext()));

        configFuture.exceptionally(throwable -> {
            getEditor().setIndexing(false);
            logger.e(TAG, getString(R.string.failed_to_init_editor));
            return null;
        });
    }
}