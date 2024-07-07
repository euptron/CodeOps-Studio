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
import com.eup.codeopsstudio.pane.Rap;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * View Model to keep a reference to the current and opened pane states in the editor
 * <p> Class is not currently used 
 * @author EUP
 */
public class PaneViewModel extends ViewModel {

  private MutableLiveData<List<Rap>> rapsLiveData = new MutableLiveData<>(new LinkedList<>());
  public static final Pair<Integer, Rap> INVALID_CURRENT_RAP = Pair.create(-1, null);
  private MutableLiveData<Pair<Integer, Rap>> currentRandomAccessPane = new MutableLiveData<>(INVALID_CURRENT_RAP);

  /**
   * Set the list of {@see Rap} (RandomAccessPanes)  to be opened in the editor.
   *
   * @param randomAccessPanes The list of random access panes to open
   */
  public void setRandomAccessPane(@NonNull List<Rap> randomAccessPanes) {
    rapsLiveData.setValue(randomAccessPanes);
  }

  /**
   * Adds a new {@code Rap} to the editor
   *
   * @param randomAccessPane The {@see Rap} representation of a {@link Pane} opened in the editor
   */
  public void addRandomAccessPane(@NonNull Rap randomAccessPane) {
    final List<Rap> raps = rapsLiveData.getValue();
    Objects.requireNonNull(raps).add(randomAccessPane);
    rapsLiveData.setValue(raps);
  }

  /**
   * Removes a {@code Rap} reprsentation of a pane from the editor
   *
   * @param rap The Rap to remove
   */
  public void removeRandomAccessPane(@NonNull Rap randomAccessPane) {
    final List<Rap> raps = rapsLiveData.getValue();
    Objects.requireNonNull(raps).remove(randomAccessPane);
    rapsLiveData.setValue(raps);
  }

  /** Clears all opened pane raps */
  public void clearRandomAccessPanes() {
    rapsLiveData.setValue(new LinkedList<>());
    setCurrentRap(INVALID_CURRENT_RAP);
  }

  @VisibleForTesting
  public LiveData<List<Rap>> getRapsLiveData() {
    return rapsLiveData;
  }

  @NonNull
  public List<Rap> getRaps() {
    return rapsLiveData.getValue() == null
        ? new LinkedList<>()
        : rapsLiveData.getValue();
  }

  /**
   * Updates a particular {@code Rap} in the #rrapsLiveData List
   *
   * @param updateRap The {@code Rap} containing an update
   */
  public void updateRap(Rap updateRap) {
    List<Rap> rapList = rapsLiveData.getValue();
    if (rapList != null) {
      for (int i = 0; i < rapList.size(); i++) {
        Rap rap = rapList.get(i);
        if (rap.IDENTIFIER.equals(rap.IDENTIFIER)) {
          rapList.set(i, updateRap);
          rapsLiveData.setValue(rapList);
          break;
        }
      }
    }
  }

  /**
   * Gets the position of the currently selected opened {@link Pane} {@code Rap}
   *
   * @return The selected Rap position
   */
  public int getCurrentRapPosition() {
    if (this.currentRandomAccessPane.getValue() == null) {
      return -1;
    }
    return this.currentRandomAccessPane.getValue().first;
  }

  /**
   * Gets the Rap that is currently opened and selected in the editor
   *
   * @return The selected Rap
   */
  public Rap getCurrentRap() {
    if (this.currentRandomAccessPane.getValue() == null) {
      return null;
    }
    return this.currentRandomAccessPane.getValue().second;
  }

  /**
   * Sets the Current {@code Pane} Rap at the selected tab position
   *
   * @param position The index or position of the {@code Pane} Rap
   * @param randomAccessPane The {@link Rap} associated with the selected tab position
   */
  public void setCurrentRap(final int position, @Nullable final Rap randomAccessPane) {
    setCurrentRap(Pair.create(position, randomAccessPane));
  }

  /**
   * Sets the Current {@code Pane} Rap at the selected tab position
   *
   * @param pair The pair associated with the selected tab position and Rap
   */
  public void setCurrentRap(Pair<Integer, Rap> pair) {
    this.currentRandomAccessPane.setValue(pair);
  }

  /**
   * Add an observer to the current random access pane
   *
   * @param lifecycleOwner The lifecycle owner.
   * @param observer The observer.
   */
  public void observeCurrentRap(
      LifecycleOwner lifecycleOwner, Observer<Pair<Integer, Rap>> observer) {
    this.currentRandomAccessPane.observe(lifecycleOwner, observer);
  }

  /**
   * Add an observer to the opened random access pane
   *
   * @param lifecycleOwner The lifecycle owner.
   * @param observer The observer.
   */
  public void observerRaps(
      LifecycleOwner lifecycleOwner, Observer<List<Rap>> observer) {
    this.rapsLiveData.observe(lifecycleOwner, observer);
  }
}
