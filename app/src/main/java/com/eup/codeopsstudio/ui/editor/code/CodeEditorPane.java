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

package com.eup.codeopsstudio.ui.editor.code;

import static android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import static com.eup.codeopsstudio.common.Constants.SharedPreferenceKeys;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.Editable;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import androidx.appcompat.widget.PopupMenu;
import androidx.annotation.NonNull;
import com.blankj.utilcode.util.KeyboardUtils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.eup.codeopsstudio.common.AsyncTask;
import com.eup.codeopsstudio.common.util.FileUtil;
import com.eup.codeopsstudio.common.util.PreferencesUtils;
import com.eup.codeopsstudio.common.util.TextWatcherAdapter;
import com.eup.codeopsstudio.databinding.LayoutCodeEditorBinding;
import com.eup.codeopsstudio.editor.ContextualCodeEditor;
import com.eup.codeopsstudio.editor.event.IndexingEvent;
import com.eup.codeopsstudio.editor.langs.textmate.provider.JsonLanguageInfoProvider;
import com.eup.codeopsstudio.events.EditorModificationEvent;
import com.eup.codeopsstudio.logging.Logger;
import com.eup.codeopsstudio.pane.Pane;
import com.eup.codeopsstudio.res.R;
import com.eup.codeopsstudio.res.databinding.LayoutDialogTextInputBinding;
import com.eup.codeopsstudio.res.databinding.LayoutReplaceInFileBinding;
import com.eup.codeopsstudio.ui.editor.code.breadcrumb.pane.CrumbTreePane;
import com.eup.codeopsstudio.util.BinaryFileChecker;
import com.eup.codeopsstudio.util.EncodingDetector;
import com.eup.codeopsstudio.util.Wizard;
import io.github.rosemoe.sora.event.ContentChangeEvent;
import io.github.rosemoe.sora.event.PublishSearchResultEvent;
import io.github.rosemoe.sora.event.SelectionChangeEvent;
import io.github.rosemoe.sora.langs.textmate.registry.ThemeRegistry;
import io.github.rosemoe.sora.widget.EditorSearcher;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.regex.PatternSyntaxException;
import org.apache.commons.io.FileUtils;
import org.greenrobot.eventbus.EventBus;

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
 *   <li>Use editor color scheme to paint bread curmbs and other editpr components
 * </ol>
 *
 * @see Pane
 * @version 0.0.1
 * @author EUP
 */
public class CodeEditorPane extends Pane implements OnSharedPreferenceChangeListener {

  private static final String LOG_TAG = "CodeEditorPane";

  private Logger logger = new Logger(Logger.LogClass.IDE);
  private PopupMenu searchMenu;
  private File mEditorFile;
  private LayoutCodeEditorBinding binding;
  private EditorSearcher.SearchOptions searchOptions =
      new EditorSearcher.SearchOptions(false, false);
  private int isMatchCaseSelected = -1;
  private static final String LANG_SCOPE_PATH = "editor/textmate/language_scopes.json";
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
    PreferencesUtils.getDefaultPreferences().registerOnSharedPreferenceChangeListener(this);

    try {
      ThemeRegistry.getInstance()
          .setTheme(binding.editor.isUIDarkMode() ? "darcula" : "quietlight");
      binding.editor.ensureTextmateTheme();
    } catch (Exception e) {
      logger.w(LOG_TAG, e.getMessage());
    }

    updateAlertVisibility(false);

    binding.searchPanel.prev.setOnClickListener(v -> binding.editor.navigatePreviousSearch());
    binding.searchPanel.next.setOnClickListener(v -> binding.editor.navigateNextSearch());
    binding.searchPanel.replace.setOnClickListener(v -> displayTextReplacementDialog());
    binding.searchPanel.moreOptions.setOnClickListener(v -> initSearchPanelMenu());

    updateCrumbPanelVisibility();
    binding.breadCrumbBar.setFile(mEditorFile);
    binding
        .breadCrumbBar
        .getAdapter()
        .setOnItemClickListener(
            (anchorView, crumb, position) -> {
              new CrumbTreePane(getContext(), anchorView).setPath(crumb.getFilePath());
            });

    readFile(mEditorFile);
    configureEditor();
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
  public void onDestroy() {
    super.onDestroy();
    PreferencesUtils.getDefaultPreferences().unregisterOnSharedPreferenceChangeListener(this);
    if (binding.editor != null && !binding.editor.isReleased()) {
      binding.editor.release();
    }
    if (binding != null) {
      binding = null;
    }
  }

  @Override
  public void onSharedPreferenceChanged(SharedPreferences pref, String key) {
    switch (key) {
      case SharedPreferenceKeys.KEY_CODE_EDITOR_NAV_PANEL:
        updateCrumbPanelVisibility();
        break;
      case SharedPreferenceKeys.KEY_CODE_EDITOR_AUTO_COMPLETE:
        /*invoke to reapply auto complete config*/
        refreshEditorLanguageSyntax(PreferencesUtils.enableAutoComplete());
        break;
    }
  }

  @Override
  public void persist() {
    super.persist();
    var cursor = binding.editor.getCursor();
    addArguments("left_column", cursor.getLeftColumn());
    addArguments("left_line", cursor.getLeftLine());
    addArguments("file_path", getFilePath());
    addArguments("editor_content", binding.editor.getText().toString());
  }

  private void readFile(@NonNull File file) {
    setLoading(true);
    Charset detectedCharset = EncodingDetector.detectFileEncoding(file);
    try {
      if (!EncodingDetector.isSupportedEncoding(detectedCharset) || isBinaryFile(file)) {
        logger.d(
            LOG_TAG,
            "Unsupported charset detected: "
                + detectedCharset.name()
                + " for file "
                + file.getName());
        updateAlertVisibility(true);
        openSearchPanel(false);
      }
    } catch (Exception e) {
      logger.e(LOG_TAG, e.getMessage());
    }
    if (detectedCharset != null) readFileWithCharset(file, detectedCharset);
  }

  private void readFileWithCharset(@NonNull File file, @NonNull Charset charset) {
    AsyncTask.runNonCancelable(
        () -> FileUtils.readFileToString(file, charset),
        (result, throwable) -> {
          setLoading(false);
          if (result != null) {
            binding.editor.setText(result, null);
            loadEditorLanguage(file);
            logger.i(
                LOG_TAG,
                getString(
                    R.string.act_code_editor_pane_open_file, getTitle(), file.getAbsolutePath()));
          }
          if (throwable != null) {
            String errorMessage =
                String.format(
                    "%s %s%n%s",
                    getString(R.string.alrt_text_parsing_error),
                    file.getAbsolutePath(),
                    throwable.getLocalizedMessage());
            logger.e(LOG_TAG, errorMessage);
          }
        });
  }

  private void setLoading(boolean loading) {
    binding.progressbar.setVisibility(loading ? View.VISIBLE : View.GONE);
  }

  private void loadEditorLanguage(File file) {
    try {
      binding.editor.setEditorLanguage(
          getEditorLanguagScope(file),
          PreferencesUtils.enableAutoComplete(),
          PreferencesUtils.enableBracketAutoClosing());
    } catch (Exception e) {
      logger.e(LOG_TAG, "Failed to load editor language configurations");
      // logger.d(e.getLocalizedMessage());
    }
  }

  public void refreshEditorLanguageSyntax() {
    refreshEditorLanguageSyntax(true);
  }

  public void refreshEditorLanguageSyntax(boolean enableAutoComplete) {
    try {
      binding.editor.refreshEditorLanguageSyntax(
          getEditorLanguagScope(getFile()), enableAutoComplete);
    } catch (Exception e) {
      logger.e(LOG_TAG, "Failed to refresh editor language configurations");
      // logger.d(e.getLocalizedMessage());
    }
  }

  private String getEditorLanguagScope(File file) throws IOException {
    var is = FileUtil.openAssetFile(getContext(), LANG_SCOPE_PATH);
    var provider = JsonLanguageInfoProvider.getInstance(getContext(), is);
    return provider.getScope(FileUtil.getFileExtension(file));
  }

  /**
   * Checks if a file is binary.
   *
   * @param file The file to be checked.
   * @return True if the file contains binary content; false if it is not binary.
   */
  private boolean isBinaryFile(File file) throws IOException {
    return BinaryFileChecker.isBinaryFile(file);
  }

  private void updateAlertVisibility(boolean isAlert) {
    if (isAlert) {
      openSearchPanel(false);
      binding.editor.setVisibility(View.GONE);
      binding.editorAlertLayout.root.setVisibility(View.VISIBLE);
      binding.breadCrumbBar.setVisibility(View.GONE);
      binding.editorAlertLayout.actionButton.setText(getContext().getString(R.string.open_anyway));
      binding.editorAlertLayout.alertMessage.setText(
          getContext().getString(R.string.alrt_unsupported_txt_encoding));
      binding.editorAlertLayout.actionButton.setOnClickListener(v -> updateAlertVisibility(false));
    } else {
      binding.editor.setVisibility(View.VISIBLE);
      binding.editorAlertLayout.root.setVisibility(View.GONE);
      binding.breadCrumbBar.setVisibility(View.VISIBLE);
    }
  }

  private void initSearchPanelMenu() {
    searchMenu = new PopupMenu(getContext(), binding.searchPanel.moreOptions);
    searchMenu.inflate(R.menu.menu_search_options);
    searchMenu.setOnMenuItemClickListener(item -> onMenuItemClick(item));
    /** Check the previously selected item, if any */
    if (selectedItem != -1) {
      searchMenu.getMenu().findItem(selectedItem).setChecked(true);
    }
    if (isMatchCaseSelected != -1) {
      searchMenu.getMenu().findItem(isMatchCaseSelected).setChecked(true);
    }
    searchMenu.show();
  }

  private boolean onMenuItemClick(MenuItem item) {
    var isChecked = item.isChecked();
    item.setChecked(!isChecked);

    var itemId = item.getItemId();
    var regexId = R.id.search_option_regex;
    var wholeWordId = R.id.search_option_whole_word;
    var matchCaseId = R.id.search_option_match_case;
    var closeId = R.id.close_search_options;

    if (itemId == regexId) {
      selectedItem = isChecked ? -1 : regexId;
    } else if (itemId == wholeWordId) {
      selectedItem = isChecked ? -1 : wholeWordId;
    } else if (itemId == matchCaseId) {
      isMatchCaseSelected = isChecked ? -1 : matchCaseId;
    } else if (itemId == closeId) {
      getEditor().getSearcher().stopSearch();
      openSearchPanel(false);
    }

    boolean ignoreCase = !searchMenu.getMenu().findItem(R.id.search_option_match_case).isChecked();
    boolean regex = searchMenu.getMenu().findItem(R.id.search_option_regex).isChecked();
    boolean wholeWord = searchMenu.getMenu().findItem(R.id.search_option_whole_word).isChecked();

    int searchType = EditorSearcher.SearchOptions.TYPE_NORMAL;
    if (regex) {
      searchType = EditorSearcher.SearchOptions.TYPE_REGULAR_EXPRESSION;
    } else if (wholeWord) {
      searchType = EditorSearcher.SearchOptions.TYPE_WHOLE_WORD;
    }

    searchOptions = new EditorSearcher.SearchOptions(searchType, ignoreCase);
    tryCommitSearch();
    return true;
  }

  private void tryCommitSearch() {
    if (isStoppingSearch) return;

    var qurey = binding.searchPanel.searchInput.getEditableText();
    if (!qurey.toString().isEmpty()) {
      try {
        binding.editor.getSearcher().search(qurey.toString(), searchOptions);
      } catch (PatternSyntaxException e) {
        logger.e(LOG_TAG, "Failed to commit search " + e.getMessage());
      }
    } else {
      binding.editor.getSearcher().stopSearch();
    }
  }

  private void updatePositionText() {
    if (!binding.searchPanel.searchInput.getEditableText().toString().isEmpty()) {
      binding.searchPanel.searchResult.setText(binding.editor.getMatchingSearchResult(false));
    } else binding.searchPanel.searchResult.setText(binding.editor.getSelectedText(false));
  }

  private void displayTextReplacementDialog() {
    var inflate = LayoutReplaceInFileBinding.inflate(LayoutInflater.from(getContext()));
    var builder = new MaterialAlertDialogBuilder(getContext());
    builder.setView(inflate.getRoot());
    builder.setTitle(R.string.replace_in_file);
    builder.setNegativeButton(android.R.string.cancel, null);
    builder.setPositiveButton(
        R.string.replace,
        (dialog, which) -> {
          binding.editor.replaceSearch(inflate.tilName.getEditText().getText().toString());
        });
    builder.setNeutralButton(
        R.string.replaceAll,
        (dialog, which) -> {
          binding.editor.replaceAllSearch(inflate.tilName.getEditText().getText().toString());
        });
    builder.show();
  }

  public void undo() {
    if (binding.editor.canUndo()) {
      binding.editor.undo();
    }
  }

  public void redo() {
    if (binding.editor.canRedo()) {
      binding.editor.redo();
    }
  }

  public boolean canUndo() {
    return binding.editor.canUndo();
  }

  public boolean canRedo() {
    return binding.editor.canRedo();
  }

  private void configureEditor() {
    binding.searchPanel.searchInput.addTextChangedListener(
        new TextWatcherAdapter() {
          @Override
          public void afterTextChanged(Editable s) {
            if (!isStoppingSearch) {
              tryCommitSearch();
            }
          }
        });

    binding.editor.subscribeEvent(
        SelectionChangeEvent.class,
        (event, data) -> {
          updatePositionText();
        });
    binding.editor.subscribeEvent(
        ContentChangeEvent.class,
        (event, data) -> {
          binding.editor.postDelayedInLifecycle(
              () -> {
                if (mEditorFile != null && mEditorFile.exists()) {
                  AsyncTask.runNonCancelable(
                      () -> {
                        var editorContent = binding.editor.getText().toString();
                        var charset = EncodingDetector.detectFileEncoding(mEditorFile);
                        var originalFileContent = FileUtils.readFileToString(mEditorFile, charset);
                        return !originalFileContent.contentEquals(editorContent);
                      },
                      (isEditorModified, throwable) -> {
                        setModified(isEditorModified);
                        if (throwable != null) {
                          logger.e(
                              LOG_TAG,
                              "Failed to read editor modification status: "
                                  + throwable.getMessage());
                        }
                      });
                } else if (mEditorFile != null && !mEditorFile.exists()) {
                  logger.i(
                      LOG_TAG,
                      "Failed to read editor modification status: File does not exist - "
                          + mEditorFile.getAbsolutePath()
                          + " Try saving this editor");
                }
              },
              /* MilliSeconds= */ 50);
        });
    binding.editor.subscribeEvent(
        PublishSearchResultEvent.class,
        (event, data) -> {
          updatePositionText();
        });

    binding.editor.subscribeEvent(
        IndexingEvent.class,
        (event, data) -> {
          var e = event.getEditor();
          if (e != null && e instanceof ContextualCodeEditor) {
            var cce = (ContextualCodeEditor) e;
            if (cce.isIndexing()) {
              binding.progressbar.setVisibility(View.VISIBLE);
            } else {
              binding.progressbar.setVisibility(View.GONE);
            }
          }
        });
    updatePositionText();
  }

  private void updateCrumbPanelVisibility() {
    if (binding != null) {
      binding.breadCrumbBar.setVisible(PreferencesUtils.displayNavigationPanel());
    }
  }

  public ContextualCodeEditor getEditor() {
    return binding.editor;
  }

  public String getFilePath() {
    return mEditorFile.getAbsolutePath();
  }

  public File getFile() {
    return mEditorFile;
  }

  public void setFile(File file) {
    this.mEditorFile = file;
  }

  public void openSearchPanel(boolean opened) {
    openSearchPanel(opened, false);
  }

  public void openSearchPanel(boolean opened, boolean disableReplace) {
    if (disableReplace) {
      binding.searchPanel.replace.setEnabled(false);
    } else {
      binding.searchPanel.replace.setEnabled(true);
    }

    if (opened) {
      isStoppingSearch = false;
      binding.editor.getSearcher().stopSearch();
      binding.searchPanel.getRoot().setVisibility(View.VISIBLE);
      KeyboardUtils.showSoftInput(binding.searchPanel.searchInput);
    } else {
      isStoppingSearch = true;
      binding.editor.getSearcher().stopSearch();
      binding.searchPanel.getRoot().setVisibility(View.GONE);
    }
  }

  private boolean isSearchPanelVisible() {
    return binding.searchPanel.getRoot().getVisibility() == View.VISIBLE;
  }

  private boolean isSearchPanelGone() {
    return binding.searchPanel.getRoot().getVisibility() == View.GONE;
  }

  private boolean isSearchPanelInVisible() {
    return binding.searchPanel.getRoot().getVisibility() == View.VISIBLE;
  }

  public void doJumpToLine() {
    if (binding == null) return;

    int totalLineCount = binding.editor.getLineCount();
    if (totalLineCount == -1) return;

    var hint = String.format("%s...%s", 1, totalLineCount);
    final var inflate = LayoutDialogTextInputBinding.inflate(LayoutInflater.from(getContext()));

    var dialog = new MaterialAlertDialogBuilder(getContext());
    dialog.setView(inflate.getRoot());
    dialog.setTitle(R.string.menu_jump_to_line);
    dialog.setNegativeButton(R.string.cancel, null);
    dialog.setCancelable(false);

    inflate.tilName.setHint(hint);
    inflate.tilName.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);

    dialog.setOnDismissListener(
        di -> {
          var str = inflate.tilName.getEditText().getText().toString();
          if (!Wizard.isEmpty(str) && Integer.parseInt(str) <= totalLineCount) {
            di.dismiss();
          }
        });
    dialog.setPositiveButton(
        R.string.ok,
        (d, which) -> {
          var lineToJump = Integer.parseInt(inflate.tilName.getEditText().getText().toString());
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
    EventBus.getDefault().post(new EditorModificationEvent(modified));
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
   * Clear persisted content {@see BaseFragment#. } for how the {@code CodeEditorPane} contents are
   * persisted
   *
   * @param recreateIfDeleted recreates the editor file incase it was deleted
   */
  public void saveEditor(boolean recreateIfDeleted) {

    AsyncTask.runNonCancelable(
        () -> {
          if (recreateIfDeleted && !mEditorFile.exists()) mEditorFile.createNewFile();

          writeEditorContentToFile();
          return null;
        },
        (result, throwable) -> {
          if (throwable == null) {
            addArguments("editor_content", ""); // persisted editor content
          } else {
            logger.e(
                LOG_TAG,
                "Unable to save file: "
                    + mEditorFile.getAbsolutePath()
                    + "\nReason: "
                    + throwable.getMessage());
          }
        });
  }

  private void writeEditorContentToFile() throws IOException {
    FileUtils.writeStringToFile(
        mEditorFile,
        binding.editor.getText().toString(),
        EncodingDetector.getEncoding(PreferencesUtils.getDefaultFileEncoding()));
    setModified(false);
  }
}
