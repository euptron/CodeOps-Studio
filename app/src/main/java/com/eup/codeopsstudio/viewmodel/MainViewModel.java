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
 
   package com.eup.codeopsstudio.viewmodel;

import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.eup.codeopsstudio.common.ContextManager;
import com.eup.codeopsstudio.res.R;
import com.eup.codeopsstudio.logging.Log;
import java.io.File;
import java.util.ArrayList;

/**
 * View Model to manage functions for WeStudio
 *
 * @author EUP
 */
public class MainViewModel extends ViewModel {

  private final MutableLiveData<Boolean> mDrawerState = new MutableLiveData<>(false);
  private final MutableLiveData<String> mToolbarTitle = new MutableLiveData<>();
  private final MutableLiveData<String> mToolbarSubTitle = new MutableLiveData<>();
  private MutableLiveData<String> mCurrentState = new MutableLiveData<>();
  private final MutableLiveData<Boolean> mDrawerInstance = new MutableLiveData<>(false);
  private final MutableLiveData<Boolean> bottomSheetExpanded = new MutableLiveData<>(false);
  private final MutableLiveData<Integer> mBottomSheetState =
      new MutableLiveData<>(BottomSheetBehavior.STATE_COLLAPSED);
  private MutableLiveData<Boolean> canRequestStoragePermission = new MutableLiveData<>(false);
  private MutableLiveData<Boolean> addSettingsPane = new MutableLiveData<>(false);
  private MutableLiveData<File> mWebViewPaneFile = new MutableLiveData<>();
  private MutableLiveData<File> mTreeFragmentViewFile = new MutableLiveData<>();
  private MutableLiveData<File> mOpenEditorFile = new MutableLiveData<>();
  private MutableLiveData<ArrayList<Log>> mIDELogs;
  private MutableLiveData<ArrayList<Log>> mBUILDLogs;
   
  public MainViewModel() {
    mToolbarTitle.setValue(ContextManager.getStringRes(R.string.app_name));
  }

  public LiveData<Integer> getBottomSheetState() {
    return mBottomSheetState;
  }

  public MutableLiveData<String> getCurrentState() {
    if (mCurrentState == null) {
      mCurrentState = new MutableLiveData<>(null);
    }
    return mCurrentState;
  }

  public void setCurrentState(@Nullable String message) {
    mCurrentState.setValue(message);
  }

  public LiveData<Boolean> getDrawerState() {
    return mDrawerState;
  }

  public void setDrawerState(boolean isOpen) {
    mDrawerState.setValue(isOpen);
  }

  public LiveData<Boolean> getDrawerInstance() {
    return mDrawerInstance;
  }

  public void setDrawerInstance(boolean isDrawerLayout) {
    mDrawerInstance.setValue(isDrawerLayout);
  }

  public LiveData<String> getToolbarTitle() {
    return mToolbarTitle;
  }

  public void setToolbarTitle(@Nullable String title) {
    mToolbarTitle.setValue(title);
  }

  public LiveData<String> getToolbarSubTitle() {
    return mToolbarSubTitle;
  }

  public void setToolbarSubTitle(@Nullable String title) {
    mToolbarSubTitle.setValue(title);
  }

  public void setBottomSheetState(@BottomSheetBehavior.State int bottomSheetState) {
    mBottomSheetState.setValue(bottomSheetState);
  }

  public void setWebViewPaneFile(File file) {
    mWebViewPaneFile.setValue(file);
  }

  public MutableLiveData<File> getWebViewPaneFile() {
    return mWebViewPaneFile;
  }

  public void setTreeViewFragmentTreeDir(File file) {
    mTreeFragmentViewFile.setValue(file);
  }

  public void openEditorFile(File file) {
    mOpenEditorFile.setValue(file);
  }

  public LiveData<Boolean> getBottomSheetExpanded() {
    return bottomSheetExpanded;
  }

  public void setBottomSheetExpanded(boolean expand) {
    bottomSheetExpanded.setValue(expand);
  }

  public void observeEditorFileOpening(LifecycleOwner lifecycleOwner, Observer<File> observer) {
    mOpenEditorFile.observe(lifecycleOwner, observer);
  }

  public void observeSetTreeViewFragmentFile(
      LifecycleOwner lifecycleOwner, Observer<File> observer) {
    this.mTreeFragmentViewFile.observe(lifecycleOwner, observer);
  }

  public LiveData<Boolean> canRequestStoragePermission() {
    return this.canRequestStoragePermission;
  }

  public void setCanRequestStoragePermissionState(boolean enabled) {
    this.canRequestStoragePermission.setValue(enabled);
  }

  public LiveData<Boolean> addSettingsPane() {
    return this.addSettingsPane;
  }

  public void addSettingsPane(boolean enabled) {
    this.addSettingsPane.setValue(enabled);
  }

  public MutableLiveData<ArrayList<Log>> getIDELogs() {
    if (mIDELogs == null) {
      mIDELogs = new MutableLiveData<ArrayList<Log>>();
    }
    return mIDELogs;
  }

  public MutableLiveData<ArrayList<Log>> getBUILDLogs() {
    if (mBUILDLogs == null) {
      mBUILDLogs = new MutableLiveData<ArrayList<Log>>();
    }
    return mBUILDLogs;
  }
}
