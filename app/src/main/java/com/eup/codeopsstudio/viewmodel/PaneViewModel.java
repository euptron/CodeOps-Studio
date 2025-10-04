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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.core.util.Pair;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;

import com.eup.codeopsstudio.pane.Pane;
import com.eup.codeopsstudio.pane.RandomAccessPane;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * View Model to keep a reference to the current and opened pane states in the editor
 *
 * <p>Class is not currently used
 *
 * @author Etido Peter
 */
public class PaneViewModel extends ViewModel {

    public static final Pair<Integer, RandomAccessPane> INVALID_CURRENT_RAP = Pair.create(-1, null);
    private final MutableLiveData<List<RandomAccessPane>> rapsLiveData =
        new MutableLiveData<>(new LinkedList<>());
    private final MutableLiveData<Pair<Integer, RandomAccessPane>> currentRandomAccessPane =
        new MutableLiveData<>(INVALID_CURRENT_RAP);

    /**
     * Adds a new {@code RandomAccessPane} to the editor
     *
     * @param randomAccessPane The {@see RandomAccessPane} representation of a {@link Pane}
     *                         opened in the editor
     */
    public void addRandomAccessPane(@NonNull RandomAccessPane randomAccessPane) {
        final List<RandomAccessPane> randomAccessPanes = rapsLiveData.getValue();
        Objects.requireNonNull(randomAccessPanes).add(randomAccessPane);
        rapsLiveData.setValue(randomAccessPanes);
    }

    /**
     * Clears all opened pane raps
     */
    public void clearRandomAccessPanes() {
        rapsLiveData.setValue(new LinkedList<>());
        setCurrentRap(INVALID_CURRENT_RAP);
    }

    /**
     * Gets the RandomAccessPane that is currently opened and selected in the editor
     *
     * @return The selected RandomAccessPane
     */
    public RandomAccessPane getCurrentRap() {
        if (this.currentRandomAccessPane.getValue() == null) {
            return null;
        }
        return this.currentRandomAccessPane.getValue().second;
    }

    /**
     * Sets the Current {@code Pane} RandomAccessPane at the selected tab position
     *
     * @param pair The pair associated with the selected tab position and RandomAccessPane
     */
    public void setCurrentRap(Pair<Integer, RandomAccessPane> pair) {
        this.currentRandomAccessPane.setValue(pair);
    }

    /**
     * Gets the position of the currently selected opened {@link Pane} {@code RandomAccessPane}
     *
     * @return The selected RandomAccessPane position
     */
    public int getCurrentRapPosition() {
        if (this.currentRandomAccessPane.getValue() == null) {
            return -1;
        }
        return this.currentRandomAccessPane.getValue().first;
    }

    @NonNull
    public List<RandomAccessPane> getRaps() {
        return rapsLiveData.getValue() == null ? new LinkedList<>() : rapsLiveData.getValue();
    }

    @VisibleForTesting
    public LiveData<List<RandomAccessPane>> getRapsLiveData() {
        return rapsLiveData;
    }

    /**
     * Add an observer to the current random access pane
     *
     * @param lifecycleOwner The lifecycle owner.
     * @param observer       The observer.
     */
    public void observeCurrentRap(LifecycleOwner lifecycleOwner,
        Observer<Pair<Integer, RandomAccessPane>> observer) {
        this.currentRandomAccessPane.observe(lifecycleOwner, observer);
    }

    /**
     * Add an observer to the opened random access pane
     *
     * @param lifecycleOwner The lifecycle owner.
     * @param observer       The observer.
     */
    public void observerRaps(LifecycleOwner lifecycleOwner,
        Observer<List<RandomAccessPane>> observer) {
        this.rapsLiveData.observe(lifecycleOwner, observer);
    }

    /**
     * Removes a {@code RandomAccessPane} reprsentation of a pane from the editor
     *
     * @param rap The RandomAccessPane to remove
     */
    public void removeRandomAccessPane(@NonNull RandomAccessPane randomAccessPane) {
        final List<RandomAccessPane> randomAccessPanes = rapsLiveData.getValue();
        Objects.requireNonNull(randomAccessPanes).remove(randomAccessPane);
        rapsLiveData.setValue(randomAccessPanes);
    }

    /**
     * Sets the Current {@code Pane} RandomAccessPane at the selected tab position
     *
     * @param position         The index or position of the {@code Pane} RandomAccessPane
     * @param randomAccessPane The {@link RandomAccessPane} associated with the selected tab
     *                         position
     */
    public void setCurrentRap(final int position,
        @Nullable final RandomAccessPane randomAccessPane) {
        setCurrentRap(Pair.create(position, randomAccessPane));
    }

    /**
     * Set the list of {@see RandomAccessPane} (RandomAccessPanes) to be opened in the editor.
     *
     * @param randomAccessPanes The list of random access panes to open
     */
    public void setRandomAccessPane(@NonNull List<RandomAccessPane> randomAccessPanes) {
        rapsLiveData.setValue(randomAccessPanes);
    }

    /**
     * Updates a particular {@code RandomAccessPane} in the #rrapsLiveData List
     *
     * @param updateRandomAccessPane The {@code RandomAccessPane} containing an update
     */
    public void updateRap(RandomAccessPane updateRandomAccessPane) {
        List<RandomAccessPane> randomAccessPaneList = rapsLiveData.getValue();
        if (randomAccessPaneList != null) {
            for (int i = 0; i < randomAccessPaneList.size(); i++) {
                RandomAccessPane randomAccessPane = randomAccessPaneList.get(i);
                if (randomAccessPane.getArguments().equals(updateRandomAccessPane.getArguments())) {
                    randomAccessPaneList.set(i, updateRandomAccessPane);
                    rapsLiveData.setValue(randomAccessPaneList);
                    break;
                }
            }
        }
    }
}
