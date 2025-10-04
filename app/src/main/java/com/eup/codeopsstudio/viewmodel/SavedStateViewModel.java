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

package com.eup.codeopsstudio.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

import com.eup.codeopsstudio.ui.editor.BaseFragment;

import java.io.File;
import java.util.List;

/**
 * ViewModel to persist data for later
 *
 * @author Etido Peter
 */
public class SavedStateViewModel extends ViewModel {

    private static final String OPENED_FILES_KEY = "opened.code.editor.files";
    private static final String TREE_VIEW_FRAGMENT_STATE_KEY =
        "treeview.fragment.stored.tree" + ".state";
    private static final String ACTION_SHEET_STATE_KEY = "base.fragment.action.bottomsheet.state";

    private final SavedStateHandle mState;

    public SavedStateViewModel(SavedStateHandle savedStateHandle) {
        mState = savedStateHandle;
    }

    /**
     * @see BaseFragment for action sheet
     */
    public LiveData<Integer> getActionSheetState() {
        return mState.getLiveData(ACTION_SHEET_STATE_KEY);
    }

    public LiveData<List<File>> getOpenedFiles() {
        return mState.getLiveData(OPENED_FILES_KEY);
    }

    public LiveData<String> getTreeViewFragmentTreeState() {
        return mState.getLiveData(TREE_VIEW_FRAGMENT_STATE_KEY);
    }

    public void saveActionSheetState(int treeState) {
        mState.set(ACTION_SHEET_STATE_KEY, treeState);
    }

    public void saveOpenedFiles(List<File> openedFiles) {
        mState.set(OPENED_FILES_KEY, openedFiles);
    }

    public void saveTreeViewFragmentTreeState(String treeState) {
        mState.set(TREE_VIEW_FRAGMENT_STATE_KEY, treeState);
    }
}
