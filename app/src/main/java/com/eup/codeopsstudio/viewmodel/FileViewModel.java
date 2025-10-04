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

import androidx.core.util.Pair;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;

import java.io.File;

/**
 * View Model to monitor file changes
 *
 * @author Etido Peter
 */
public class FileViewModel extends ViewModel {

    private final MutableLiveData<File> pickedFile = new MutableLiveData<>();
    private final MutableLiveData<File> pickedFolder = new MutableLiveData<>();
    private final MutableLiveData<Pair<Exception, String>> monitorMessage = new MutableLiveData<>();
    private final MutableLiveData<Pair<Integer, String>> fileWatcherEvents =
        new MutableLiveData<>();

    public MutableLiveData<Pair<Integer, String>> getFileWatcherEvents() {
        return this.fileWatcherEvents;
    }

    public MutableLiveData<Pair<Exception, String>> getMonitorMessage() {
        return this.monitorMessage;
    }

    public MutableLiveData<File> getPickedFile() {
        return this.pickedFile;
    }

    public void setPickedFile(File pickedFile) {
        this.pickedFile.setValue(pickedFile);
    }

    public MutableLiveData<File> getPickedFolder() {
        return this.pickedFolder;
    }

    public void setPickedFolder(File pickedFolder) {
        this.pickedFolder.setValue(pickedFolder);
    }

    public void monitorMessages(LifecycleOwner lifecycleOwner,
        Observer<Pair<Exception, String>> observer) {
        this.monitorMessage.observe(lifecycleOwner, observer);
    }

    public void observePickedFiles(LifecycleOwner lifecycleOwner, Observer<File> observer) {
        this.pickedFile.observe(lifecycleOwner, observer);
    }

    public void observePickedFolders(LifecycleOwner lifecycleOwner, Observer<File> observer) {
        this.pickedFolder.observe(lifecycleOwner, observer);
    }

    public void postFileWatcherEvent(int event, String path) {
        this.fileWatcherEvents.postValue(Pair.create(event, path));
    }

    public void setMonitorMessage(Exception e, String message) {
        setMonitorMessage(Pair.create(e, message));
    }

    public void setMonitorMessage(Pair<Exception, String> monitorMessage) {
        this.monitorMessage.setValue(monitorMessage);
    }
}