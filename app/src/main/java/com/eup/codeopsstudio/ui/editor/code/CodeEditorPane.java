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
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.util.Pair;

import com.eup.codeopsstudio.R;
import com.eup.codeopsstudio.common.AsyncTask;
import com.eup.codeopsstudio.common.Constants;
import com.eup.codeopsstudio.common.ILog;
import com.eup.codeopsstudio.common.util.FileUtil;
import com.eup.codeopsstudio.common.util.PreferencesUtils;
import com.eup.codeopsstudio.common.util.TextWatcherAdapter;
import com.eup.codeopsstudio.databinding.LayoutCodeEditorBinding;
import com.eup.codeopsstudio.databinding.LayoutDialogTextInputBinding;
import com.eup.codeopsstudio.domain.events.EditorModificationEvent;
import com.eup.codeopsstudio.editor.ContextualCodeEditor;
import com.eup.codeopsstudio.editor.event.IndexingEvent;
import com.eup.codeopsstudio.editor.langs.textmate.provider.JsonLanguageInfoProvider;
import com.eup.codeopsstudio.models.logger.Logger;
import com.eup.codeopsstudio.pane.Pane;
import com.eup.codeopsstudio.ui.editor.code.breadcrumb.pane.CrumbTreePane;
import com.eup.codeopsstudio.ui.editor.code.manager.FileOperationsManager;
import com.eup.codeopsstudio.ui.editor.code.manager.SearchManager;
import com.eup.codeopsstudio.util.BaseUtil;
import com.eup.codeopsstudio.util.EncodingDetector;
import com.eup.codeopsstudio.util.Wizard;
import com.eup.codeopsstudio.ui.pane.factory.PaneFactoryImpl;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.apache.commons.io.FileUtils;
import org.greenrobot.eventbus.EventBus;
import org.jetbrains.annotations.Contract;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import io.github.rosemoe.sora.event.ContentChangeEvent;
import io.github.rosemoe.sora.event.EventReceiver;
import io.github.rosemoe.sora.event.PublishSearchResultEvent;
import io.github.rosemoe.sora.event.SelectionChangeEvent;

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
    public static final String KEY_LEFT_COLUMN = "left_column";
    public static final String KEY_LEFT_LINE = "left_line";
    public static final String KEY_FILE_PATH = "file_path";
    public static final String KEY_FILE_EXTENSION = "file_extension";
    public static final String KEY_EDITOR_CONTENT = "editor_content";
    
    private static final String KEY_FILE_MTIME = "file_mtime";
    private static final String KEY_FILE_SIZE = "file_size";
    private static final String KEY_WAS_DIRTY = "was_dirty";
    private static final String LANG_SCOPE_PATH = "editor/textmate/language_scopes.json";
    private static final int CONTENT_CHANGE_CHECK_DELAY_MS = 50;
    
    private final Logger logger = new Logger(Logger.LogClass.IDE);
    private File mEditorFile;
    private LayoutCodeEditorBinding binding;
    private boolean isModified = false;
    private String fileExtension;
    private String fileScope;
    private SearchManager searchManager;
    private FileOperationsManager fileOperationsManager;
    private boolean isContentLoaded = false;
    
    public CodeEditorPane(Context context, String title) {
        this(context, title, true);
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
        logger.attach(requireActivity());
        PreferencesUtils.getDefaultPreferences().registerOnSharedPreferenceChangeListener(this);

        searchManager = new SearchManager(requireContext(), binding);
        searchManager.applyPanelClickListeners();
        fileOperationsManager = new FileOperationsManager(requireContext(), logger, binding.editor);
        updateAlertVisibility(false);
        updateCrumbPanelVisibility();

        binding.breadCrumbBar.setFile(mEditorFile);
        if (binding.breadCrumbBar.getAdapter() != null) {
            binding.breadCrumbBar.getAdapter()
                                 .setOnItemClickListener((anchorView, crumb, position) -> new CrumbTreePane(getContext(), anchorView).setPath(crumb.getFilePath()));
        }
        
        // --- tasking stuff
        applyEditorTheme();

        if (mEditorFile == null) {
            restoreFileFromArguments();
        }
        
        // Setup empty editor - NO CONTENT LOADING
        setupEmptyEditor();
        enableEditorFeatures();
    }

    @Override
    protected void onViewLaidOut(@NonNull View view) {
        super.onViewLaidOut(view);
        logger.i(TAG, "Editor UI ready - content will load on selection: " + getTitle());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        PreferencesUtils.getDefaultPreferences().unregisterOnSharedPreferenceChangeListener(this);
        binding.editor.release();
        binding = null;
    }
    
    @Override
    public void onSelected() {
        super.onSelected();
        if (binding != null) binding.editor.requestFocus();
        
        if (!isContentLoaded) {
            ILog.debug(TAG, "Loading content on selection: " + getTitle());
            loadEditorContentOnSelection();
        }
    }

    @Override
    public void persist() {
        super.persist();
        var cursor = binding.editor.getCursor();
        addArguments(KEY_LEFT_COLUMN, cursor.getLeftColumn());
        addArguments(KEY_LEFT_LINE, cursor.getLeftLine());
        addArguments(KEY_FILE_PATH, mEditorFile.getAbsolutePath());
        addArguments(KEY_FILE_EXTENSION, fileExtension);
        
        // Save file metadata for change detection
        if (mEditorFile.exists()) {
            addArguments(KEY_FILE_MTIME, mEditorFile.lastModified());
            addArguments(KEY_FILE_SIZE, mEditorFile.length());
        }
        
        if (isModified()) {
            addArguments(KEY_EDITOR_CONTENT, binding.editor.getText().toString());
            addArguments(KEY_WAS_DIRTY, true);
        } else {
            addArguments(KEY_EDITOR_CONTENT, "");
            addArguments(KEY_WAS_DIRTY, false);
        }
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
    
    private void loadEditorContentOnSelection() {
        boolean hasPersistedChanges = hasPersistedEditorChanges();
        
        if (hasPersistedChanges) {
            // We have unsaved changes - check file state
            boolean fileChangedExternally = hasFileChangedExternally();
            
            if (fileChangedExternally) {
                showFileModifiedDialog();
            } else {
                restoreFromPersistence();
                completeLazyLoading();
            }
        } else {
            // No persisted changes - load fresh file content
            readFileContent();
            completeLazyLoading();
        }
    }
    
    private void setupEmptyEditor() {
        binding.editor.setText("", null);
        loadEditorLanguage(mEditorFile);
        setModified(false);
        // isContentLoaded remains false
    }
    
    private void completeLazyLoading() {
        isContentLoaded = true;
        ILog.debug(TAG, "Completed lazy loading for: " + getTitle());
    }
    
    public boolean hasPersistedEditorChanges() {
        final Map<String, Object> args = getArguments();
        if (args == null) return false;
        
        final  boolean wasDirty = PaneFactoryImpl.requireBoolean(KEY_WAS_DIRTY, args);
        final String persistedContent = PaneFactoryImpl.requireString(KEY_EDITOR_CONTENT, args);
        
        return wasDirty && persistedContent != null && !persistedContent.isEmpty();
    }
    
    private boolean hasFileChangedExternally() {
        final Map<String, Object> args = getArguments();
        if (args == null || mEditorFile == null || !mEditorFile.exists()) return false;
        
        long persistedMTime = PaneFactoryImpl.requireLong(KEY_FILE_MTIME, args);
        long persistedSize = PaneFactoryImpl.requireLong(KEY_FILE_SIZE, args);
        
        long currentMTime = mEditorFile.lastModified();
        long currentSize = mEditorFile.length();
        
        return persistedMTime != currentMTime || persistedSize != currentSize;
    }
    
    private void showFileModifiedDialog() {
        String fileName = mEditorFile.getName();
        String message = getString(R.string.file_modified_externally, fileName);
        
        new MaterialAlertDialogBuilder(requireContext())
            .setTitle(fileName)
            .setMessage(message)
            .setPositiveButton(R.string.reload_file, (dialog, which) -> {
                showReloadConfirmationDialog();
            })
            .setNegativeButton(R.string.keep_changes, (dialog, which) -> {
                restoreFromPersistence();
                completeLazyLoading();
            })
            .setCancelable(false)
            .show();
    }
    
    private void showReloadConfirmationDialog() {
        new MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.warning)
            .setMessage(R.string.reload_will_lose_changes)
            .setPositiveButton(R.string.yes_reload, (dialog, which) -> {
                readFileContent();
                completeLazyLoading();
            })
            .setNegativeButton(R.string.cancel, (dialog, which) -> {
                restoreFromPersistence();
                completeLazyLoading();
            })
            .setCancelable(false)
            .show();
    }
    
    private void restoreFromPersistence() {
        setLoading(true);
        
        final Map<String, Object> args = getArguments();
        if (args == null) return;
        
        String persistedContent = PaneFactoryImpl.requireString(KEY_EDITOR_CONTENT, args);
        if (persistedContent != null) {
            binding.editor.setText(persistedContent);
            binding.editor.setLanguageExtension(PaneFactoryImpl.requireString(KEY_FILE_EXTENSION, args));
            
            int leftLine = PaneFactoryImpl.requireInt(KEY_LEFT_LINE, args);
            int leftColumn = PaneFactoryImpl.requireInt(KEY_LEFT_COLUMN, args);
            
            Content text = binding.editor.getText();
            int totalLines = text.getLineCount();
            
            // Only set cursor if position is valid
            if (leftLine >= 0 && leftLine < totalLines) {
                int maxColumn = text.getColumnCount(leftLine);
                if (leftColumn >= 0 && leftColumn <= maxColumn) {
                    binding.editor.getCursor().set(leftLine, leftColumn);
                } else {
                    // Fallback: set to start of line
                    binding.editor.getCursor().set(leftLine, 0);
                }
            } else {
                // Fallback: set to start of document
                binding.editor.getCursor().set(0, 0);
            }
        
            setModified(true);
            loadEditorLanguage(mEditorFile);
            setLoading(false);
            
            logger.i(TAG, "Restored editor content from persistence");
        }
    }
    
    private void readFileContent() {
        setLoading(true);
        fileOperationsManager.readFile(mEditorFile, () -> {
            updateAlertVisibility(true);
            searchManager.openSearchPanel(false);
        }, result -> {
            binding.editor.setText(result, null);
            loadEditorLanguage(mEditorFile);
            setModified(false);
            
            if (!isContentLoaded) {
                completeLazyLoading();
            }
            
            setLoading(false);
            logger.i(TAG, "File content loaded: " + getTitle());
        });
    }
    
    public void showSnackBar(@NonNull String message) {
        if (binding == null) return;

        var snackBarBuilder = showSnackBarInternal(message);

        if (snackBarBuilder != null) {
            snackBarBuilder.create();
        } else {
            ILog.debug(TAG, "BaseUtil.SnackBarBuilder is null");
        }
    }
    
    public BaseUtil.SnackBarBuilder showSnackBarInternal(@NonNull String message) {
        if (binding == null) return null;

        return BaseUtil.newSnackBarBuilder()
                      .setMessage(message)
                      .setView(binding.editor)
                      .setMessageMaxLines(6)
                      .setDuration(BaseUtil.SnackBarBuilder.DURATION.LONG);
    }

    public void refreshEditorLanguageSyntax() {
        loadEditorLanguageInternal(PreferencesUtils.enableAutoComplete(),
            PreferencesUtils.enableBracketAutoClosing(), true, mEditorFile);
    }

    private void loadEditorLanguageInternal(boolean autoComplete, boolean autoCloseBrackets, boolean refresh, @NonNull File file) {
        try {
            Pair<String, String> languageInfo = getEditorLanguageInfo(file);

            if (languageInfo == null) {
                ILog.warning(TAG, "Language Info is null");
                return;
            }

            fileExtension = languageInfo.first;
            fileScope = languageInfo.second;
            
            binding.editor.setEditorLanguage(fileExtension, fileScope, autoComplete, autoCloseBrackets, refresh);
            if (refresh) applyEditorTheme();
        } catch (Exception e) {
            String clause = (refresh ? getString(R.string.refresh).toLowerCase()
                : getString(R.string.load).toLowerCase());
            String msg = getString(R.string.msg_editor_load_configs_failed, clause);
            logger.e(TAG, msg, e);
            showSnackBar(msg);
        }
    }

    @Nullable
    private Pair<String, String> getEditorLanguageInfo(@NonNull File file) throws IOException {
        if (isInvalidContext()) return null;

        InputStream is = getAssets().open(LANG_SCOPE_PATH);
        var provider = new JsonLanguageInfoProvider(is);
        String extension = FileUtil.getFileExtension(file);
        String scope = provider.getScope(extension);
        Set<String> extensions = provider.getLanguageExtensions(scope);
        String scopedExtensions = Arrays.toString(extensions.toArray());
        ILog.debug(TAG, String.format("File: %s, Extension: '%s', Scope: '%s', Shared Extensions: %s", file.getName(), extension, scope, scopedExtensions));
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

    private void applyEditorTheme() {
        try {
            boolean isDarkMode = binding.editor.isUIDarkMode();
            String lightTheme = ContextualCodeEditor.THEME_QUIET_LIGHT;
            String darkTheme = ContextualCodeEditor.THEME_DARCULA;
            
            binding.editor.updateTextMateTheme(isDarkMode ? darkTheme : lightTheme);
        } catch (Exception e) {
            logger.e(TAG, e.getMessage(), e);
        }
    }

    private void updateCrumbPanelVisibility() {
        if (binding != null) {
            binding.breadCrumbBar.setVisible(PreferencesUtils.displayNavigationPanel());
        }
    }

    public boolean canRedo() {
        return binding != null && binding.editor.canRedo();
    }

    public boolean canUndo() {
        return binding != null && binding.editor.canUndo();
    }

    public void doJumpToLine() {
        if (binding == null) return;

        int totalLineCount = binding.editor.getLineCount();
        if (totalLineCount == -1) return;
        
        final var inflate = LayoutDialogTextInputBinding.inflate(LayoutInflater.from(getContext()));
        
        if (inflate.tilName == null || inflate.tilName.getEditText() == null) {
           ILog.error(TAG, "Error: TIL name or its edittext = null");
           return;
        }
        
        final var tilName = inflate.tilName;
        final var tilNameEditText = inflate.tilName.getEditText();
        
        tilName.setHint(String.format("1...%s", totalLineCount));
        tilNameEditText.setInputType(InputType.TYPE_CLASS_NUMBER);

        var builder = new MaterialAlertDialogBuilder(requireContext());
        builder.setView(inflate.getRoot());
        builder.setTitle(R.string.menu_jump_to_line);
        builder.setNegativeButton(R.string.cancel, null);
        builder.setCancelable(false);

        AlertDialog dialog = builder.create();
        
        dialog.setOnShowListener(d -> {
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setEnabled(false);
            
            tilNameEditText.requestFocus();
            tilNameEditText.addTextChangedListener(new TextWatcherAdapter() {
                @Override
                public void afterTextChanged(@NonNull Editable editable) {
                    if (Wizard.isEmpty(editable.toString())) return;
                    
                    try {
                        var lineToJump = Integer.parseInt(editable.toString());

                        if (lineToJump < 1 || lineToJump > totalLineCount) {
                            positiveButton.setEnabled(false);
                            tilName.setError(getString(R.string.msg_invalid_jump_line));
                            tilName.setErrorEnabled(true);
                        } else {
                            positiveButton.setEnabled(true);
                            tilName.setErrorEnabled(false);
                        }
                    } catch (NumberFormatException e) {
                        positiveButton.setEnabled(false);
                        tilName.setError(getString(R.string.msg_invalid_jump_line));
                        tilName.setErrorEnabled(true);
                    }
                }
            });
            
            positiveButton.setOnClickListener(v -> {
                var jumpText = tilNameEditText.getText().toString();
                
                try {
                    // reject empty or invalid input
                    boolean condition1 = Wizard.isEmpty(jumpText);
                    var lineToJump = Integer.parseInt(jumpText);
                    boolean condition2 = lineToJump < 1 || lineToJump > totalLineCount;
                    
                    if (condition1 || condition2) {
                        tilName.setError(getString(R.string.msg_invalid_jump_line));
                        tilName.setErrorEnabled(true);
                        return;
                    }
                    
                    int targetLine = lineToJump - 1;
                    binding.editor.jumpToLine(targetLine);
                    dialog.dismiss();
                } catch (NumberFormatException e) {
                    tilName.setError(getString(R.string.msg_invalid_jump_line));
                    tilName.setErrorEnabled(true);
                }
            });
        });
        
        dialog.show();
    }
    
    public File getFile() {
        return mEditorFile;
    }

    public void setFile(File file) {
        this.mEditorFile = file;
    }

    public String getFilePath() {
        return mEditorFile.getAbsolutePath();
    }

    public SearchManager getSearchManager() {
        return searchManager;
    }

    @Nullable
    @Contract(pure = true)
    public EventReceiver<IndexingEvent> indexingEventReceiver() {
        if (binding == null) return null;

        return (event, data) -> {
            ContextualCodeEditor cce = (ContextualCodeEditor) event.getEditor();
            setLoading(cce.isIndexing());
        };
    }

    public boolean isModified() {
        return isModified;
    }

    public boolean isReadOnlyMode() {
        if (binding == null) return false;
        return !binding.editor.isEditable();
    }

    public void makeReadOnly(boolean readOnly) {
        if (binding == null || searchManager == null) return;

        if (readOnly) {
            binding.editor.setEditable(false);
            searchManager.openSearchPanel(false, true);
        } else {
            binding.editor.setEditable(true);
            searchManager.openSearchPanel(false, false);
        }
    }

    public void redo() {
        if (binding == null) return;
        binding.editor.redo();
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
        runOnUiThread(() -> getEditor().setIndexing(true));

        AsyncTask.runNonCancelable(() -> {
            if (recreateIfDeleted && !mEditorFile.exists() && mEditorFile.createNewFile()) {
                logger.i(TAG, "File recreated for " + getTitle() + " editor");
            }

            fileOperationsManager.saveEditorContent(mEditorFile, binding.editor.getText()
                                                                               .toString());
            return null;
        }, (result, throwable) -> {
            if (throwable == null) {
                ILog.info(TAG, "Successfully saved editor file, any persisted data was cleared to"
                    + " save memory");
                addArguments(KEY_EDITOR_CONTENT, ""); // persisted editor content
                setModified(false);
            } else {
                var msg = "Error occurred while saving file: " + mEditorFile.getAbsolutePath()
                    + ", Reason: " + throwable.getMessage();
                logger.e(TAG, msg);
            }
            runOnUiThread(() -> getEditor().setIndexing(false));
        });
    }

    public ContextualCodeEditor getEditor() {
        return binding.editor;
    }

    public void setModified(boolean modified) {
        isModified = modified;
        EventBus.getDefault().post(new EditorModificationEvent(modified));
    }

    public void undo() {
        if (binding == null) return;
        binding.editor.undo();
    }
    
    private void enableEditorFeatures() {
        try {
            searchManager.applySearchTextChangedListener();

            binding.editor.subscribeEvent(SelectionChangeEvent.class,
                (event, data) -> searchManager.updatePositionText());
    
            binding.editor.subscribeEvent(ContentChangeEvent.class,
                (event, data) -> {
                if (mEditorFile == null) {
                    return;
                }
    
                if (!mEditorFile.exists()) {
                    ILog.debug(TAG, String.format("File: %s does not exist", mEditorFile.getPath()));
                    return;
                }
    
                AsyncTask.runNonCancelable(() -> {
                    String editorContent = binding.editor.getText().toString();
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
            }, CONTENT_CHANGE_CHECK_DELAY_MS);
    
            binding.editor.subscribeEvent(PublishSearchResultEvent.class,
                (event, data) -> searchManager.updatePositionText());
            binding.editor.subscribeEvent(IndexingEvent.class, indexingEventReceiver());
            searchManager.updatePositionText();
        } catch (Exception e) {
            logger.e(TAG, getString(R.string.failed_to_init_editor), e);
            showSnackBar(getString(R.string.failed_to_init_editor) + ", Reason: " + e.getMessage());
        }
    }

    private void loadEditorLanguage(@NonNull File file) {
        loadEditorLanguageInternal(PreferencesUtils.enableAutoComplete(),
            PreferencesUtils.enableBracketAutoClosing(), false, file);
    }

    private void setLoading(boolean loading) {
        if (binding == null) return;
        binding.progressbar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private void updateAlertVisibility(boolean show) {
        if (show) {
            searchManager.openSearchPanel(false);
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
    
    private void restoreFileFromArguments() {
        final Map<String, Object> args = getArguments();
        if (args != null) {
            String filePath = PaneFactoryImpl.requireString(KEY_FILE_PATH, args);
            if (filePath != null && !filePath.isEmpty()) {
                mEditorFile = new File(filePath);
                ILog.debug(TAG, "Restored mEditorFile from arguments: " + filePath);
            }
        }
    }
}