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
import android.os.Environment;
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
import com.eup.codeopsstudio.logger.Logger;
import com.eup.codeopsstudio.palette.CommandPaletteDialog;
import com.eup.codeopsstudio.pane.Pane;
import com.eup.codeopsstudio.ui.editor.code.breadcrumb.pane.CrumbTreePane;
import com.eup.codeopsstudio.ui.editor.code.manager.FileOperationsManager;
import com.eup.codeopsstudio.ui.editor.code.manager.SearchManager;
import com.eup.codeopsstudio.ui.pane.factory.PaneFactoryImpl;
import com.eup.codeopsstudio.util.BaseUtil;
import com.eup.codeopsstudio.util.EncodingDetector;
import com.eup.codeopsstudio.util.Wizard;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import io.github.rosemoe.sora.event.ContentChangeEvent;
import io.github.rosemoe.sora.event.EventReceiver;
import io.github.rosemoe.sora.event.PublishSearchResultEvent;
import io.github.rosemoe.sora.event.SelectionChangeEvent;
import io.github.rosemoe.sora.lang.EmptyLanguage; // Added import
import io.github.rosemoe.sora.text.CharPosition; // Added import
import io.github.rosemoe.sora.text.Content;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedList; // Added import
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.apache.commons.io.FileUtils;
import org.greenrobot.eventbus.EventBus;
import org.jetbrains.annotations.Contract;

/**
 * A specialized pane implementation for code editing operations within CodeOps Studio.
 *
 * <p>This pane serves as the primary code editing interface, providing advanced text editing
 * capabilities with syntax highlighting, file management operations, and real-time content
 * synchronization. It integrates with the broader pane system while maintaining independent editor
 * state and lifecycle management.
 *
 * <h3>Core Responsibilities</h3>
 *
 * <ul>
 *   <li>File content loading, editing, and persistence with charset detection
 *   <li>Syntax highlighting and language-aware editing features
 *   <li>Real-time modification tracking and change detection
 *   <li>Search and navigation management within editor content
 *   <li>File operations (save, reload, save-as, statistics)
 *   <li>Editor state preservation and restoration
 * </ul>
 *
 * <h3>Key Features</h3>
 *
 * <ul>
 *   <li><b>Lazy Loading</b>: Content loads on selection for performance optimization
 *   <li><b>Encoding Support</b>: Multiple charset detection and conversion
 *   <li><b>File Management</b>: Save, save-as, reload with external change detection
 *   <li><b>Syntax highlight</b>: TextMate-based language support with auto-completion
 *   <li><b>State Persistence</b>: Cursor position, content, and modification state restoration
 *   <li><b>Search Integration</b>: Full-featured search and replace functionality
 * </ul>
 *
 * <h3>Lifecycle Management</h3>
 *
 * <p>The pane implements sophisticated lifecycle handling:
 *
 * <ul>
 *   <li><b>View Creation</b>: Basic UI setup and theme application
 *   <li><b>View Layout</b>: Editor features enablement after layout completion <b>Selection</b>:
 *       Content loading triggered by pane activation
 *   <li><b>Persistence</b>: Automatic state saving for session restoration
 * </ul>
 *
 * <h3>Integration Points</h3>
 *
 * <ul>
 *   <li>Extends {@link Pane} for window management integration
 *   <li>Uses {@link ContextualCodeEditor} for core editing capabilities
 *   <li>Collaborates with {@link FileOperationsManager} for file I/O operations
 *   <li>Integrates {@link SearchManager} for text search functionality
 *   <li>Publishes {@link EditorModificationEvent} for UI state synchronization
 * </ul>
 *
 * @author Etido Peter
 * @version 2.0.0
 * @see Pane
 * @see ContextualCodeEditor
 * @see FileOperationsManager
 * @see SearchManager
 * @since 1.0.0
 */
public class CodeEditorPane extends Pane
    implements SharedPreferences.OnSharedPreferenceChangeListener {

  public static final String TAG = "CodeEditorPane";
  public static final String KEY_LEFT_COLUMN = "left_column";
  public static final String KEY_LEFT_LINE = "left_line";
  public static final String KEY_FILE_PATH = "file_path";
  public static final String KEY_FILE_EXTENSION = "file_extension";
  public static final String KEY_EDITOR_CONTENT = "editor_content";

  private static final String KEY_FILE_MTIME = "file_mtime";
  private static final String KEY_FILE_SIZE = "file_size";
  private static final String KEY_WAS_DIRTY = "was_dirty";
  private static final String LANG_SCOPE_PATH = Constants.TEXTMATE_ASSET_SCOPE_PATH;
  private static final int CONTENT_CHANGE_CHECK_DELAY_MS = 50;
  private static final int CURSOR_HISTORY_LIMIT = 50;

  private final Logger logger = new Logger(Logger.LogClass.IDE);
  private File mEditorFile;
  private LayoutCodeEditorBinding binding;
  private boolean isModified = false;
  private String fileExtension;
  private String fileScope;
  private SearchManager searchManager;
  private FileOperationsManager fileOperationsManager;
  private boolean isContentLoaded = false;
  private boolean isHardwareAccelerated = false;
  private Charset currentCharset = StandardCharsets.UTF_8;
  private final LinkedList<CharPosition> previousCursorPositions = new LinkedList<>();
  private final LinkedList<CharPosition> nextCursorPositions = new LinkedList<>();
  private boolean navigationInProgress = false;

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

    applyEditorTheme();

    searchManager = new SearchManager(requireContext(), binding);
    searchManager.applyPanelClickListeners();
    fileOperationsManager = new FileOperationsManager(requireContext(), logger, binding.editor);
    updateAlertVisibility(false);
    updateCrumbPanelVisibility();

    binding.breadCrumbBar.setFile(mEditorFile);
    if (binding.breadCrumbBar.getAdapter() != null) {
      binding
          .breadCrumbBar
          .getAdapter()
          .setOnItemClickListener(
              (anchorView, crumb, position) ->
                  new CrumbTreePane(getContext(), anchorView).setPath(crumb.getFilePath()));
    }

    if (mEditorFile == null) {
      restoreFileFromArguments();
    }
  }

  @Override
  protected void onViewLaidOut(@NonNull View view) {
    super.onViewLaidOut(view);
    logger.i(TAG, "Editor UI ready - content will load on selection: " + getTitle());
    setupEmptyEditor();
    enableEditorFeatures();

    // Notify listeners that the editor is fully initialized and ready.
    // We check isSelected() so we only notify for the pane that is
    // currently active and waiting for this signal.
    if (isSelected() && getEditor() != null) {
      ILog.debug(
          TAG, "Editor is fully laid out and ready, posting EditorReadyEvent for: " + getTitle());
      EventBus.getDefault().post(new com.eup.codeopsstudio.domain.events.EditorReadyEvent(this));
    }
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    ILog.debug(TAG, "onDestroyView started for " + getTitle());
    try {
      if (EventBus.getDefault().isRegistered(this)) {
        EventBus.getDefault().unregister(this);
      }
    } catch (Exception e) {
      // Ignore if not registered
    }
    PreferencesUtils.getDefaultPreferences().unregisterOnSharedPreferenceChangeListener(this);
    if (binding != null && binding.editor != null) {
      try {
        binding.editor.release();
      } catch (Exception e) {
        logger.e(TAG, "Error releasing editor: " + e.getMessage(), e);
      }
    }
    previousCursorPositions.clear();
    nextCursorPositions.clear();
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
    Objects.requireNonNull(key);
    switch (key) {
      case Constants.SharedPreferenceKeys.KEY_CODE_EDITOR_NAV_PANEL:
        updateCrumbPanelVisibility();
        break;
      case Constants.SharedPreferenceKeys.KEY_CODE_EDITOR_AUTO_CLOSE_BRACKET:
        refreshEditorLanguageSyntax();
        break;
      case Constants.SharedPreferenceKeys.KEY_CODE_EDITOR_HARDWARE_ACCELERATION:
        isHardwareAccelerated = PreferencesUtils.enableHardWareAcceleration();
        break;
      default: // Nothing
    }
  }

  public void reloadFile() {
    if (mEditorFile == null || !mEditorFile.exists()) {
      showSnackBar(getString(R.string.file_not_found));
      return;
    }

    if (isModified()) {
      showReloadConfirmationDialog();
    } else {
      performReload();
    }
  }

  public void reloadFileWithCharset(@NonNull Charset charset) {
    if (mEditorFile == null || !mEditorFile.exists()) {
      showSnackBar(getString(R.string.file_not_found));
      return;
    }

    this.currentCharset = charset;
    if (isModified()) {
      showReloadWithCharsetConfirmationDialog(charset);
    } else {
      performReloadWithCharset(charset);
    }
  }

  public void showCharsetSelectionDialog() {
    List<String> charsets = EncodingDetector.getSupportedEncodings();
    int defaultCharsetIndex = charsets.indexOf(Constants.FALLBACK_FILE_ENCODING);
    if (defaultCharsetIndex < 0) {
      defaultCharsetIndex = charsets.indexOf(StandardCharsets.UTF_8.name());
    }

    new MaterialAlertDialogBuilder(requireContext())
        .setTitle(R.string.select_charset)
        .setSingleChoiceItems(
            charsets.toArray(new String[0]),
            defaultCharsetIndex,
            (dialog, which) -> {
              Charset selectedCharset = EncodingDetector.findEncoding(charsets.get(which));
              reloadFileWithCharset(selectedCharset);
              dialog.dismiss();
            })
        .setNegativeButton(R.string.cancel, null)
        .show();
  }

  public void saveAs() {
    if (binding == null || mEditorFile == null) return;

    String currentContent = binding.editor.getText().toString();
    if (currentContent.isEmpty()) {
      showSnackBar(getString(R.string.no_content_to_save));
      return;
    }

    showSaveAsDialog();
  }

  public void showStatistics() {
    if (binding == null) return;

    binding.editor.setIndexing(true);

    AsyncTask.runNonCancelable(
        () -> {
          return calculateFileStatistics();
        },
        (stats, throwable) -> {
          binding.editor.setIndexing(false);
          if (throwable == null && stats != null) {
            var message = new StringBuilder();
            message
                .append("File Name: ")
                .append(mEditorFile != null ? mEditorFile.getName() : "Untitled")
                .append("\n");
            message.append("File Size: ").append(formatFileSize(stats.fileSize)).append("\n");
            message.append("Total Lines: ").append(stats.totalLines).append("\n");
            message.append("Total Words: ").append(stats.totalWords).append("\n");
            message.append("Total Characters: ").append(stats.totalChars).append("\n");
            message
                .append("Total Characters (no spaces): ")
                .append(stats.totalCharsNoSpaces)
                .append("\n");
            message.append("Encoding: ").append(stats.encoding).append("\n");
            message.append("Line Separator: ").append(stats.lineSeparator);

            new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.file_statistics)
                .setMessage(message.toString())
                .setPositiveButton(R.string.close, null)
                .show();
          } else {
            String errorMsg =
                getString(R.string.failed_to_calculate_statistics)
                    + ": "
                    + (throwable != null ? throwable.getMessage() : "Unknown error");
            showSnackBar(errorMsg);
            logger.e(TAG, errorMsg, throwable);
          }
        });
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
    if (mEditorFile != null) {
      loadEditorLanguage(mEditorFile);
    }
    setModified(false);
  }

  private void completeLazyLoading() {
    isContentLoaded = true;
    ILog.debug(TAG, "Completed lazy loading for: " + getTitle());
  }

  public boolean hasPersistedEditorChanges() {
    final Map<String, Object> args = getArguments();
    if (args == null) return false;

    final boolean wasDirty = PaneFactoryImpl.requireBoolean(KEY_WAS_DIRTY, args);
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
        .setPositiveButton(
            R.string.reload_file,
            (dialog, which) -> {
              showReloadConfirmationDialog();
            })
        .setNegativeButton(
            R.string.keep_changes,
            (dialog, which) -> {
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
        .setPositiveButton(
            R.string.yes_reload,
            (dialog, which) -> {
              readFileContent();
              completeLazyLoading();
            })
        .setNegativeButton(
            R.string.cancel,
            (dialog, which) -> {
              restoreFromPersistence();
              completeLazyLoading();
            })
        .setCancelable(false)
        .show();
  }

  private void showReloadWithCharsetConfirmationDialog(Charset charset) {
    new MaterialAlertDialogBuilder(requireContext())
        .setTitle(R.string.warning)
        .setMessage(getString(R.string.reload_with_charset_warning, charset.displayName()))
        .setPositiveButton(
            R.string.yes_reload,
            (dialog, which) -> {
              performReloadWithCharset(charset);
            })
        .setNegativeButton(R.string.cancel, null)
        .setCancelable(false)
        .show();
  }

  private void performReload() {
    setLoading(true);
    readFileContent();
    setLoading(false);
    showSnackBar(getString(R.string.file_reloaded));
  }

  private void performReloadWithCharset(Charset charset) {
    setLoading(true);
    readFileContentWithCharset(charset);
    setLoading(false);
    showSnackBar(getString(R.string.file_reloaded_with_charset, charset.displayName()));
  }

  private void showSaveAsDialog() {
    final var inflate = LayoutDialogTextInputBinding.inflate(LayoutInflater.from(getContext()));

    if (inflate.tilName == null || inflate.tilName.getEditText() == null) {
      ILog.error(TAG, "Error: TIL name or its edittext = null");
      return;
    }

    final var tilName = inflate.tilName;
    final var tilNameEditText = inflate.tilName.getEditText();

    tilName.setHint(R.string.enter_filename);
    String defaultName = mEditorFile != null ? mEditorFile.getName() : "new_file.txt";
    tilNameEditText.setText(defaultName);
    tilNameEditText.setSelection(defaultName.length());

    var builder = new MaterialAlertDialogBuilder(requireContext());
    builder.setView(inflate.getRoot());
    builder.setTitle(R.string.save_as);
    builder.setNegativeButton(R.string.cancel, null);
    builder.setPositiveButton(R.string.ok, null);
    builder.setCancelable(false);

    AlertDialog dialog = builder.create();

    dialog.setOnShowListener(
        d -> {
          Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
          positiveButton.setEnabled(true);

          positiveButton.setOnClickListener(
              v -> {
                var fileName = tilNameEditText.getText().toString();

                if (Wizard.isEmpty(fileName)) {
                  tilName.setError(getString(R.string.filename_cannot_be_empty));
                  return;
                }

                performSaveAs(fileName);
                dialog.dismiss();
              });
        });

    dialog.show();
  }

  private void performSaveAs(String fileName) {
    if (Wizard.isEmpty(fileName)) {
      showSnackBar(getString(R.string.filename_cannot_be_empty));
      return;
    }

    fileName = cleanFileName(fileName);

    File parentDir =
        mEditorFile != null
            ? mEditorFile.getParentFile()
            : Environment.getExternalStorageDirectory();

    if (parentDir == null || !FileUtil.createOrExistsDir(parentDir)) {
      showSnackBar(getString(R.string.invalid_directory));
      return;
    }

    String finalFileName = handleFileExtension(fileName, mEditorFile);

    File targetFile = new File(parentDir, finalFileName);

    if (targetFile.equals(mEditorFile)) {
      showSnackBar(getString(R.string.same_file_error));
      return;
    }

    if (targetFile.exists()) {
      showOverwriteConfirmationDialog(targetFile, finalFileName);
    } else {
      saveToFile(targetFile);
    }
  }

  private String cleanFileName(String fileName) {
    if (FileUtil.isSpace(fileName)) return "unnamed_file";

    String cleaned = fileName.replaceAll("[<>:\"/\\\\|?*]", "_");
    cleaned = cleaned.trim().replaceAll("^\\.+|\\.+$", "");

    if (FileUtil.isSpace(cleaned)) {
      cleaned = "unnamed_file";
    }

    return cleaned;
  }

  private String handleFileExtension(String fileName, File originalFile) {
    // If fileName already has a valid extension, use it as is
    if (hasValidExtension(fileName)) {
      return fileName;
    }

    // Only add extension if original file has a valid extension
    String originalExt = FileUtil.getFileExtension(originalFile);
    if (!FileUtil.isSpace(originalExt)) {
      return fileName + "." + originalExt;
    }

    // No valid extension in original file, return as is
    return fileName;
  }

  private boolean hasValidExtension(String fileName) {
    if (FileUtil.isSpace(fileName)) return false;

    String extension = FileUtil.getFileExtension(fileName);
    return !FileUtil.isSpace(extension);
  }

  private void showOverwriteConfirmationDialog(File targetFile, String fileName) {
    new MaterialAlertDialogBuilder(requireContext())
        .setTitle(R.string.file_exists)
        .setMessage(getString(R.string.overwrite_file_confirmation, fileName))
        .setPositiveButton(
            R.string.overwrite,
            (dialog, which) -> {
              saveToFile(targetFile);
            })
        .setNegativeButton(R.string.cancel, null)
        .show();
  }

  private void saveToFile(File targetFile) {
    setLoading(true);

    AsyncTask.runNonCancelable(
        () -> {
          String content = binding.editor.getText().toString();
          FileUtils.writeStringToFile(targetFile, content, currentCharset);
          return targetFile;
        },
        (result, throwable) -> {
          setLoading(false);
          if (throwable == null) {
            setFile(result);
            setModified(false);
            showSnackBar(getString(R.string.file_saved_as, result.getName()));
            logger.i(TAG, "File saved as: " + result.getAbsolutePath());
          } else {
            String errorMsg =
                getString(R.string.msg_save_as_failed) + ": " + throwable.getMessage();
            showSnackBar(errorMsg);
            logger.e(TAG, errorMsg, throwable);
          }
        });
  }

  private FileStats calculateFileStatistics() {
    FileStats stats = new FileStats();

    if (mEditorFile != null && mEditorFile.exists()) {
      stats.fileSize = mEditorFile.length();
      stats.encoding = currentCharset.displayName();
    }

    String content = binding.editor.getText().toString();
    stats.totalLines = binding.editor.getLineCount();
    stats.totalChars = content.length();
    stats.totalCharsNoSpaces = content.replaceAll("\\s", "").length();
    stats.totalWords = content.trim().isEmpty() ? 0 : content.trim().split("\\s+").length;
    stats.lineSeparator = System.getProperty("line.separator");

    return stats;
  }

  private String formatFileSize(long size) {
    if (size <= 0) return "0 B";

    if (size < 1024) {
      return size + " B";
    } else if (size < 1024 * 1024) {
      return String.format("%.1f KB", size / 1024.0);
    } else if (size < 1024 * 1024 * 1024) {
      return String.format("%.1f MB", size / (1024.0 * 1024.0));
    } else {
      return String.format("%.1f GB", size / (1024.0 * 1024.0 * 1024.0));
    }
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
      if (mEditorFile != null) {
        loadEditorLanguage(mEditorFile);
      }
      setLoading(false);

      logger.i(TAG, "Restored editor content from persistence");
    }
  }

  private void readFileContent() {
    setLoading(true);
    fileOperationsManager.readFile(
        mEditorFile,
        () -> {
          updateAlertVisibility(true);
          searchManager.openSearchPanel(false);
        },
        result -> {
          binding.editor.setText(result, null);
          if (mEditorFile != null) {
            loadEditorLanguage(mEditorFile);
          }
          setModified(false);

          if (!isContentLoaded) {
            completeLazyLoading();
          }

          setLoading(false);
          logger.i(TAG, "File content loaded: " + getTitle());
        });
  }

  private void readFileContentWithCharset(Charset charset) {
    setLoading(true);
    fileOperationsManager.readFileWithCharset(
        mEditorFile,
        charset,
        () -> {
          updateAlertVisibility(true);
          searchManager.openSearchPanel(false);
        },
        result -> {
          binding.editor.setText(result, null);
          if (mEditorFile != null) {
            loadEditorLanguage(mEditorFile);
          }
          setModified(false);
          currentCharset = charset;

          if (!isContentLoaded) {
            completeLazyLoading();
          }

          setLoading(false);
          logger.i(TAG, "File content loaded with charset " + charset + ": " + getTitle());
        });
  }

  public void showSnackBar(@NonNull String message) {
    if (binding == null || binding.editor == null) return;

    if (!binding.editor.isAttachedToWindow()) {
      return;
    }

    var snackBarBuilder = showSnackBarInternal(message);

    if (snackBarBuilder != null) {
      snackBarBuilder.create();
    } else {
      ILog.debug(TAG, "BaseUtil.SnackBarBuilder is null");
    }
  }

  public BaseUtil.SnackBarBuilder showSnackBarInternal(@NonNull String message) {
    if (binding == null || binding.editor == null) return null;

    if (!binding.editor.isAttachedToWindow()) {
      return null;
    }

    return BaseUtil.newSnackBarBuilder()
        .setMessage(message)
        .setView(binding.editor)
        .setMessageMaxLines(6)
        .setDuration(BaseUtil.SnackBarBuilder.DURATION.LONG);
  }

  public void refreshEditorLanguageSyntax() {
    loadEditorLanguageInternal(
        PreferencesUtils.enableAutoComplete(),
        PreferencesUtils.enableBracketAutoClosing(),
        true,
        mEditorFile);
  }

  private void loadEditorLanguageInternal(
      boolean autoComplete, boolean autoCloseBrackets, boolean refresh, @NonNull File file) {
    try {
      Pair<String, String> languageInfo = getEditorLanguageInfo(file);

      if (languageInfo == null) {
        ILog.warning(TAG, "Language Info is null");
        return;
      }

      fileExtension = languageInfo.first;
      fileScope = languageInfo.second;

      if (refresh) binding.editor.refreshTheme();

      binding.editor.setEditorLanguage(
          fileExtension, fileScope, autoComplete, autoCloseBrackets, refresh);
    } catch (Exception e) {
      String clause =
          (refresh
              ? getString(R.string.refresh).toLowerCase()
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
    ILog.debug(
        TAG,
        String.format(
            "File: %s, Extension: '%s', Scope: '%s', Shared Extensions: %s",
            file.getName(), extension, scope, scopedExtensions));
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

      binding.editor.applyTheme(isDarkMode ? darkTheme : lightTheme);
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
    CommandPaletteDialog.newScopedInstance(this, ":", getString(R.string.menu_jump_to_line))
        .show(requireActivity().getSupportFragmentManager(), "goto_line_palette");
  }
  
  public void doJumpToLineX() {
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
    builder.setPositiveButton(R.string.ok, null);
    builder.setCancelable(false);

    AlertDialog dialog = builder.create();

    dialog.setOnShowListener(
        d -> {
          Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
          positiveButton.setEnabled(false);

          tilNameEditText.requestFocus();
          tilNameEditText.addTextChangedListener(
              new TextWatcherAdapter() {
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

          positiveButton.setOnClickListener(
              v -> {
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
    if (binding != null) {
      binding.breadCrumbBar.setFile(file);
    }
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

    AsyncTask.runNonCancelable(
        () -> {
          if (recreateIfDeleted && !mEditorFile.exists()) {
            if (mEditorFile.createNewFile()) {
              logger.i(TAG, "File recreated for " + getTitle() + " editor");
            } else {
              throw new IOException("Failed to recreate file: " + mEditorFile.getAbsolutePath());
            }
          }

          fileOperationsManager.saveEditorContent(mEditorFile, binding.editor.getText().toString());
          return null;
        },
        (result, throwable) -> {
          getEditor().setIndexing(false);
          if (throwable == null) {
            ILog.info(
                TAG,
                "Successfully saved editor file, any persisted data was cleared to save memory");
            addArguments(KEY_EDITOR_CONTENT, ""); // persisted editor content
            setModified(false);
          } else {
            var msg =
                "Error occurred while saving file: "
                    + mEditorFile.getAbsolutePath()
                    + ", Reason: "
                    + throwable.getMessage();
            logger.e(TAG, msg);
            showSnackBar(msg);
          }
        });
  }

  public ContextualCodeEditor getEditor() {
    if (!hasPerformedOnViewCreated() | binding == null) {
      ILog.warning(TAG, "Editor not available - view not created or binding null");
      return null;
    }
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

      binding.editor.subscribeEvent(
          SelectionChangeEvent.class,
          (event, data) -> {
            searchManager.updatePositionText();

            if (!navigationInProgress
                && event.getCause() != SelectionChangeEvent.CAUSE_TEXT_MODIFICATION
                && !event.isSelected()) {
              CharPosition currentPos = event.getLeft();
              if (previousCursorPositions.isEmpty()
                  || !previousCursorPositions.getLast().equals(currentPos)) {
                previousCursorPositions.add(currentPos.fromThis());
                if (previousCursorPositions.size() > CURSOR_HISTORY_LIMIT) {
                  previousCursorPositions.removeFirst();
                }
                nextCursorPositions.clear();
              }
            }
          });

      binding.editor.subscribeEvent(
          ContentChangeEvent.class,
          (event, data) ->
              binding.editor.postDelayedInLifecycle(
                  () -> {
                    if (mEditorFile == null) {
                      return;
                    }

                    if (!mEditorFile.exists()) {
                      ILog.debug(
                          TAG, String.format("File: %s does not exist", mEditorFile.getPath()));
                      return;
                    }

                    AsyncTask.runNonCancelable(
                        () -> {
                          String editorContent = binding.editor.getText().toString();
                          int bufferSize = PreferencesUtils.getCurrentBufferSize();
                          var cs = EncodingDetector.detectFileEncoding(bufferSize, mEditorFile);
                          var originalFileContent = FileUtils.readFileToString(mEditorFile, cs);
                          return !originalFileContent.contentEquals(editorContent);
                        },
                        (isEditorModified, th) -> {
                          if (th == null) {
                            setModified(isEditorModified);
                          } else {
                            logger.e(
                                TAG,
                                "Failed to read editor modification status: " + th.getMessage());
                          }
                        });
                  },
                  CONTENT_CHANGE_CHECK_DELAY_MS));

      binding.editor.subscribeEvent(
          PublishSearchResultEvent.class, (event, data) -> searchManager.updatePositionText());
      binding.editor.subscribeEvent(IndexingEvent.class, indexingEventReceiver());
      searchManager.updatePositionText();
    } catch (Exception e) {
      String reason = (e.getMessage() == null) ? "" : e.getMessage();
      String msg = getString(R.string.failed_to_init_editor) + ", Reason: " + reason;
      logger.e(TAG, msg, e);
      showSnackBar(msg);
    }
  }

  private void loadEditorLanguage(@NonNull File file) {
    loadEditorLanguageInternal(
        PreferencesUtils.enableAutoComplete(),
        PreferencesUtils.enableBracketAutoClosing(),
        false,
        file);
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
      binding.editorAlertLayout.alertMessage.setText(
          getString(R.string.alrt_unsupported_txt_encoding));
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

  // --- Start
  /** Navigates to the previous cursor position in the history. */
  public void navigateToPreviousCursorPosition() {
    if (!previousCursorPositions.isEmpty()) {
      navigationInProgress = true;
      CharPosition currentPos = getEditor().getCursor().left().fromThis();
      nextCursorPositions.add(currentPos);

      CharPosition lastPos = previousCursorPositions.removeLast();
      getEditor().setSelection(lastPos.line, lastPos.column);
      navigationInProgress = false;
    }
  }

  /** Navigates to the next cursor position in the history. */
  public void navigateToNextCursorPosition() {
    if (!nextCursorPositions.isEmpty()) {
      navigationInProgress = true;
      CharPosition currentPos = getEditor().getCursor().left().fromThis();
      previousCursorPositions.add(currentPos);

      CharPosition nextPos = nextCursorPositions.removeLast();
      getEditor().setSelection(nextPos.line, nextPos.column);
      navigationInProgress = false;
    }
  }

  public boolean canNavigateToPrevious() {
    return !previousCursorPositions.isEmpty();
  }

  public boolean canNavigateToNext() {
    return !nextCursorPositions.isEmpty();
  }

  public void increaseIndent() {
    if (getEditor() != null) {
      getEditor().indentLines(false);
    }
  }

  public void decreaseIndent() {
    if (getEditor() != null) {
      getEditor().unindentSelection();
    }
  }

  public void setSoftWrapEnabled(boolean enabled) {
    if (getEditor() != null) {
      getEditor().setWordwrap(enabled);
    }
  }

  public boolean isSoftWrapEnabled() {
    if (getEditor() == null) return false;
    return getEditor().isWordwrap();
  }

  public void setSmoothModeEnabled(boolean enabled) {
    if (getEditor() != null) {
      if (!isSmoothModeEnabled()) {
        BaseUtil.displayDialog(requireContext(), R.string.msg_lite_mode_description);
      }
      getEditor().setBasicDisplayMode(enabled);
    }
  }

  public boolean isSmoothModeEnabled() {
    if (getEditor() == null) return false;
    return getEditor().isBasicDisplayMode();
  }

  public void showLanguagePicker() {
    CommandPaletteDialog.newScopedInstance(this, "@syntax -", getString(R.string.select_language_mode))
        .show(requireActivity().getSupportFragmentManager(), "language_picker_palette");
  }

  private static class FileStats {
    long fileSize;
    int totalLines;
    int totalWords;
    int totalChars;
    int totalCharsNoSpaces;
    String encoding;
    String lineSeparator;
  }
}
