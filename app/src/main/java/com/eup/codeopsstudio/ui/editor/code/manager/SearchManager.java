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
 * questions or need additional information. Email: etido.up@gmail.com
 */

package com.eup.codeopsstudio.ui.editor.code.manager;

import android.content.Context;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;

import com.eup.codeopsstudio.R;
import com.eup.codeopsstudio.common.ILog;
import com.eup.codeopsstudio.common.util.TextWatcherAdapter;
import com.eup.codeopsstudio.databinding.LayoutCodeEditorBinding;
import com.eup.codeopsstudio.databinding.LayoutReplaceInFileBinding;
import com.eup.codeopsstudio.util.BaseUtil;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.regex.PatternSyntaxException;

import io.github.rosemoe.sora.widget.EditorSearcher;

/**
 * @author Etido Peter
 */
public class SearchManager {
    public static final String TAG = "SearchManager";
    private final Context context;
    private final LayoutCodeEditorBinding binding;
    private PopupMenu searchMenu;
    private EditorSearcher.SearchOptions searchOptions = new EditorSearcher.SearchOptions(false,
        false);
    private int isMatchCaseSelected = -1;
    private int selectedItem = -1;
    private boolean isStoppingSearch = false;

    public SearchManager(@NonNull Context context, @NonNull LayoutCodeEditorBinding binding) {
        this.context = context;
        this.binding = binding;
    }

    public void applyPanelClickListeners() {
        binding.searchPanel.prev.setOnClickListener(v -> binding.editor.navigatePreviousSearch());
        binding.searchPanel.next.setOnClickListener(v -> binding.editor.navigateNextSearch());
        binding.searchPanel.replace.setOnClickListener(v -> displayTextReplacementDialog());
        binding.searchPanel.moreOptions.setOnClickListener(v -> initSearchPanelMenu());
    }

    private void initSearchPanelMenu() {
        searchMenu = new PopupMenu(context, binding.searchPanel.moreOptions);
        searchMenu.inflate(R.menu.menu_search_options);
        searchMenu.setOnMenuItemClickListener(this::onMenuItemClick);

        if (selectedItem != -1) {
            searchMenu.getMenu().findItem(selectedItem).setChecked(true);
        }
        if (isMatchCaseSelected != -1) {
            searchMenu.getMenu().findItem(isMatchCaseSelected).setChecked(true);
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
            binding.editor.getSearcher().stopSearch();
            openSearchPanel(false);
        }

        boolean ignoreCase = !searchMenu.getMenu()
                                        .findItem(com.eup.codeopsstudio.R.id.search_option_match_case)
                                        .isChecked();
        boolean regex = searchMenu.getMenu()
                                  .findItem(com.eup.codeopsstudio.R.id.search_option_regex)
                                  .isChecked();
        boolean wholeWord = searchMenu.getMenu()
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

    public void openSearchPanel(boolean opened) {
        openSearchPanel(opened, false);
    }

    public void openSearchPanel(boolean opened, boolean disableReplace) {
        binding.searchPanel.replace.setEnabled(!disableReplace);

        if (opened) {
            isStoppingSearch = false;
            binding.editor.getSearcher().stopSearch();
            binding.searchPanel.getRoot().setVisibility(View.VISIBLE);
            BaseUtil.showSoftInput(binding.searchPanel.searchInput);
        } else {
            isStoppingSearch = true;
            binding.editor.getSearcher().stopSearch();
            binding.searchPanel.getRoot().setVisibility(View.GONE);
        }
    }

    private void commitSearch() {
        if (isStoppingSearch) return;

        var query = binding.searchPanel.searchInput.getEditableText();
        if (!query.toString().isEmpty()) {
            try {
                binding.editor.getSearcher().search(query.toString(), searchOptions);
            } catch (PatternSyntaxException e) {
                ILog.error(TAG, "Failed to commit search " + e.getMessage(), e);
            }
        } else {
            binding.editor.getSearcher().stopSearch();
        }
    }

    private void displayTextReplacementDialog() {
        var inflate = LayoutReplaceInFileBinding.inflate(LayoutInflater.from(context));
        var builder = new MaterialAlertDialogBuilder(context);

        builder.setView(inflate.getRoot());
        builder.setTitle(R.string.replace_in_file);
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.setPositiveButton(R.string.replace, (dialog, which) -> {
            if (inflate.tilName.getEditText() != null) {
                binding.editor.replaceSearch(inflate.tilName.getEditText().getText().toString());
            } else {
                ILog.error(TAG, "Text replacement failed, text to replace is empty");
            }
        });

        builder.setNeutralButton(R.string.replaceAll, (dialog, which) -> {
            if (inflate.tilName.getEditText() != null) {
                binding.editor.replaceAllSearch(inflate.tilName.getEditText().getText()
                                                               .toString(), () -> ILog.info(TAG,
                    "Text replacement successful"));
            } else {
                ILog.error(TAG, "Text replacement failed, text to replace is empty");
            }
        });
        builder.show();
    }

    public void applySearchTextChangedListener() {
        binding.searchPanel.searchInput.addTextChangedListener(new TextWatcherAdapter() {
            @Override
            public void afterTextChanged(@NonNull Editable s) {
                if (isStoppingSearch) return;
                commitSearch();
            }
        });
    }

    public void updatePositionText() {
        if (!binding.searchPanel.searchInput.getEditableText().toString().isEmpty()) {
            binding.searchPanel.searchResult.setText(binding.editor.getMatchingSearchResult(false));
        } else {
            binding.searchPanel.searchResult.setText(binding.editor.getSelectedText(false));
        }
    }
}
