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

import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;

import com.eup.codeopsstudio.IdeApplication;
import com.eup.codeopsstudio.R;
import com.eup.codeopsstudio.common.models.Event;
import com.eup.codeopsstudio.models.logger.Log;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.io.File;
import java.util.ArrayList;

/**
 * View Model to manage functions for CodeOps Studio
 *
 * @author Etido Peter
 */
public class MainViewModel extends ViewModel {

    private final MutableLiveData<String> mToolbarTitle = new MutableLiveData<>();
    private final MutableLiveData<String> mToolbarSubTitle = new MutableLiveData<>();
    private final MutableLiveData<Boolean> mDrawerInstance = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> bottomSheetExpanded = new MutableLiveData<>(false);
    private final MutableLiveData<Integer> mBottomSheetState =
        new MutableLiveData<>(BottomSheetBehavior.STATE_COLLAPSED);
    private final MutableLiveData<Boolean> shouldUpdateMenu = new MutableLiveData<>();
    private final MutableLiveData<Boolean> addSettingsPane = new MutableLiveData<>(false);
    private final MutableLiveData<File> mWebViewPaneFile = new MutableLiveData<>();
    private final MutableLiveData<File> mTreeFragmentViewFile = new MutableLiveData<>();
    private final MutableLiveData<File> mOpenEditorFile = new MutableLiveData<>();
    private final MutableLiveData<File> pickZipFile = new MutableLiveData<>();
    private final MutableLiveData<Boolean> addPane = new MutableLiveData<>(false);
    private final MutableLiveData<Event<Boolean>> exitRequest = new MutableLiveData<>();
    private final MutableLiveData<Event<Boolean>> mDrawerState = new MutableLiveData<>();
    private MutableLiveData<ArrayList<Log>> mIDELogs;
    private MutableLiveData<ArrayList<Log>> mBUILDLogs;

    public MainViewModel() {
        setToolbarTitle(IdeApplication.getGlobalContext().getString(R.string.app_name));
    }

    public boolean addPane() {
        this.addPane.postValue(true);
        return true;
    }

    public LiveData<Boolean> addSettingsPane() {
        return this.addSettingsPane;
    }

    public void addSettingsPane(boolean enabled) {
        this.addSettingsPane.setValue(enabled);
    }

    public void clearExitRequest() {
        exitRequest.setValue(new Event<>(false));
    }

    public void closeDrawer() {
        mDrawerState.postValue(new Event<>(false));
    }

    public LiveData<Boolean> getAddPane() {
        return this.addPane;
    }

    public MutableLiveData<ArrayList<Log>> getBUILDLogs() {
        if (mBUILDLogs == null) {
            mBUILDLogs = new MutableLiveData<>();
        }
        return mBUILDLogs;
    }

    public LiveData<Boolean> getBottomSheetExpanded() {
        return bottomSheetExpanded;
    }

    public void setBottomSheetExpanded(boolean expand) {
        bottomSheetExpanded.setValue(expand);
    }

    public LiveData<Integer> getBottomSheetState() {
        return mBottomSheetState;
    }

    public void setBottomSheetState(@BottomSheetBehavior.State int bottomSheetState) {
        mBottomSheetState.setValue(bottomSheetState);
    }

    public LiveData<Boolean> getDrawerInstance() {
        return mDrawerInstance;
    }

    public void setDrawerInstance(boolean isDrawerLayout) {
        mDrawerInstance.setValue(isDrawerLayout);
    }

    public LiveData<Event<Boolean>> getDrawerState() {
        return mDrawerState;
    }
    
    public boolean isDrawerOpen() {
        Boolean state = mDrawerState.getValue().getContentIfNotHandled();
        return state != null && state;
    }
    
    public void requestOpenDrawer() {
        mDrawerState.postValue(new Event<>(true));
    }
    
    public void requestCloseDrawer() {
        mDrawerState.postValue(new Event<>(false));
    }

    public LiveData<Event<Boolean>> getExitRequest() {
        return exitRequest;
    }

    public MutableLiveData<ArrayList<Log>> getIDELogs() {
        if (mIDELogs == null) {
            mIDELogs = new MutableLiveData<>();
        }
        return mIDELogs;
    }

    public LiveData<Boolean> getShouldUpdateMenu() {
        return shouldUpdateMenu;
    }

    public void setShouldUpdateMenu(boolean update) {
        shouldUpdateMenu.setValue(update);
    }

    public LiveData<String> getToolbarSubTitle() {
        return mToolbarSubTitle;
    }

    public void setToolbarSubTitle(@Nullable String title) {
        mToolbarSubTitle.setValue(title);
    }

    public LiveData<String> getToolbarTitle() {
        return mToolbarTitle;
    }

    public void setToolbarTitle(@Nullable String title) {
        mToolbarTitle.setValue(title);
    }

    public MutableLiveData<File> getWebViewPaneFile() {
        return mWebViewPaneFile;
    }

    public void setWebViewPaneFile(File file) {
        mWebViewPaneFile.setValue(file);
    }

    public LiveData<File> getZipFile() {
        return this.pickZipFile;
    }

    public void setZipFile(File file) {
        this.pickZipFile.setValue(file);
    }
    
    public void observeEditorFileOpening(LifecycleOwner lifecycleOwner, Observer<File> observer) {
        mOpenEditorFile.observe(lifecycleOwner, observer);
    }

    public void observeSetTreeViewFragmentFile(LifecycleOwner lifecycleOwner,
        Observer<File> observer) {
        this.mTreeFragmentViewFile.observe(lifecycleOwner, observer);
    }

    public void openEditorFile(File file) {
        mOpenEditorFile.setValue(file);
    }

    public void requestExit() {
        exitRequest.setValue(new Event<>(true));
    }

    public void setTreeViewFragmentTreeDir(File file) {
        mTreeFragmentViewFile.setValue(file);
    }
}