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

package com.eup.codeopsstudio.ui.pane;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu.OnMenuItemClickListener;

import com.eup.codeopsstudio.common.models.BooleanResult;
import com.eup.codeopsstudio.pane.Pane;
import com.eup.codeopsstudio.pane.PaneFactory;
import com.google.android.material.tabs.TabLayout;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

/**
 * Defines the contract for managing {@link Pane} instances within a tabbed interface.
 * Implementations typically coordinate a {@link TabLayout} with a content display area
 * (like a {@link android.widget.ViewFlipper}) to handle pane lifecycle, selection, and UI
 * synchronization.
 * <p>
 * Extends {@link TabLayout.OnTabSelectedListener} to handle direct tab interactions.
 *
 * @author Etido Peter
 * @see TabLayout
 * @see Pane
 * @since 1.0.3 beta
 */
public interface PaneWindow extends TabLayout.OnTabSelectedListener {

    /**
     * Called when a tab enters the selected state. Implementations should update the displayed
     * content
     * and internal selection state.
     *
     * @param tab The tab that was selected.
     */
    @Override
    void onTabSelected(@NonNull TabLayout.Tab tab);

    /**
     * Called when a tab exits the selected state. Implementations can perform cleanup or state
     * changes
     * for the unselected pane.
     *
     * @param tab The tab that was unselected.
     */
    @Override
    void onTabUnselected(TabLayout.Tab tab);

    /**
     * Called when a tab that is already selected is chosen again by the user. Implementations might
     * use this to show a context menu or perform another action.
     *
     * @param tab The tab that was reselected.
     */
    @Override
    void onTabReselected(@NonNull TabLayout.Tab tab);

    /**
     * Adds the specified pane to the end of the list.
     * Selection behavior depends on the implementation (often selects if it's the first pane).
     *
     * @param pane The pane to add. Must not be null.
     * @return {@code true} if the pane was added successfully, {@code false} if it already
     * exists or failed to add.
     */
    boolean add(@NonNull Pane pane);

    /**
     * Adds the specified pane at the given index.
     * Selection behavior depends on the implementation.
     *
     * @param pane  The pane to add. Must not be null.
     * @param index The index at which to insert the pane.
     * @return {@code true} if the pane was added successfully, {@code false} if it already
     * exists or failed to add.
     * @throws IndexOutOfBoundsException if the index is out of range (index < 0 || index > size()).
     */
    boolean add(@NonNull Pane pane, int index);

    /**
     * Adds the specified pane to the end of the list, explicitly controlling selection.
     *
     * @param pane   The pane to add. Must not be null.
     * @param select {@code true} to select the newly added pane, {@code false} otherwise.
     * @return {@code true} if the pane was added successfully, {@code false} if it already
     * exists or failed to add.
     */
    boolean add(@NonNull Pane pane, boolean select);

    /**
     * Adds the specified pane at the given index, explicitly controlling selection.
     * This is the most specific method for adding panes.
     *
     * @param pane   The pane to add. Must not be null.
     * @param index  The index at which to insert the pane.
     * @param select {@code true} to select the newly added pane, {@code false} otherwise.
     * @return {@code true} if the pane was added successfully, {@code false} if it already
     * exists or failed to add.
     * @throws IndexOutOfBoundsException if the index is out of range (index < 0 || index > size()).
     */
    boolean add(@NonNull Pane pane, int index, boolean select);

    /**
     * Adds all panes from the specified collection. Ignores panes already present.
     *
     * @param collection The collection of panes to add. Must not be null.
     * @return {@code true} if all panes in the collection were new and added successfully,
     * {@code false} otherwise.
     */
    boolean addAll(@NonNull Collection<? extends Pane> collection);

    /**
     * Checks the configured default behavior for relative close operations.
     *
     * @return {@code true} if the default is configured to close only the first adjacent tab,
     * {@code false} otherwise.
     * @see #closeTabsRelativeToFirst(boolean)
     */
    boolean canCloseTabsRelativeToFirst();

    /**
     * Closes (removes) the specified pane, if it is not pinned.
     * Convenience method often equivalent to {@code remove(pane)}.
     *
     * @param pane The pane to close. Must not be null.
     * @return {@code true} if the pane was successfully closed, {@code false} otherwise (e.g.,
     * pinned or not found).
     */
    boolean close(@NonNull Pane pane);

    /**
     * Closes all panes that are not currently pinned.
     *
     * @return {@code true} if any pane was closed, {@code false} otherwise.
     */
    boolean closeAll();

    /**
     * Closes all panes that are not currently pinned.
     *
     * @param closeUnpinned {@code true} to close only unpinned panes when closing/opening
     *                      otherwise {@code false} to close both pinned and unpinned
     *                      panes with exception of the
     *                      {@link com.eup.codeopsstudio.ui.editor.panes.WelcomePane}
     * @return {@code true} if any pane was closed, {@code false} otherwise.
     */
    boolean closeAll(boolean closeUnpinned);

    /**
     * Closes all panes except for the specified {@code paneToKeep} and any other pinned panes.
     *
     * @param paneToKeep The pane instance to keep open. Must not be null and must be present.
     * @return {@code true} if any other pane was closed, {@code false} otherwise.
     */
    boolean closeOthers(@NonNull Pane paneToKeep);

    /**
     * Configures the default behavior for relative close operations like "Close Tabs to the
     * Right/Left".
     * This often controls whether only the first adjacent tab or all subsequent tabs are closed
     * by default
     * when triggered via a menu action.
     *
     * @param close {@code true} to configure the default relative close action to target only
     *              the first adjacent tab,
     *              {@code false} to target all subsequent tabs.
     */
    void closeTabsRelativeToFirst(boolean close);

    /**
     * Closes panes positioned to the left of the specified reference pane.
     * Pinned panes in the range will be skipped.
     *
     * @param pane      The reference pane. Tabs to the left of this pane are targeted. Must not
     *                  be null.
     * @param firstOnly If {@code true}, only close the single tab immediately to the left (if
     *                  unpinned).
     *                  If {@code false}, close all tabs to the left (if unpinned).
     * @return {@code true} if the operation was attempted (does not guarantee panes were
     * actually closed if none existed or all were pinned).
     */
    boolean closeToLeftOf(@NonNull Pane pane, boolean firstOnly);

    /**
     * Closes panes positioned to the right of the specified reference pane.
     * Pinned panes in the range will be skipped.
     *
     * @param pane      The reference pane. Tabs to the right of this pane are targeted. Must not
     *                  be null.
     * @param firstOnly If {@code true}, only close the single tab immediately to the right (if
     *                  unpinned).
     *                  If {@code false}, close all tabs to the right (if unpinned).
     * @return {@code true} if the operation was attempted (does not guarantee panes were
     * actually closed if none existed or all were pinned).
     */
    boolean closeToRightOf(@NonNull Pane pane, boolean firstOnly);

    /**
     * Checks if the specified pane instance is currently managed by this window.
     *
     * @param pane The pane instance to check. Must not be null.
     * @return {@code true} if the pane is present, {@code false} otherwise.
     */
    boolean contains(@NonNull Pane pane);

    void createEmptyPaneWindow(@NonNull View view);

    /**
     * Completely dismantles the PaneWindow, releasing all held resources to prevent memory leaks.
     * This is called when the owning component (e.g., Fragment, Activity) is being destroyed.
     */
    void destroy();

    default <T> T findPane(Class<T> clazz) {
        T found = null;
        for (var pane : getPanes()) {
            found = PaneFactory.getPane(pane, clazz);
        }
        return found;
    }

    /**
     * Gets the list of currently managed panes.
     * Implementations should consider returning an immutable list or a copy
     * to prevent external modification issues.
     *
     * @return The list of panes.
     */
    @NonNull
    List<Pane> getPanes();

    default <T> T findPane(Class<T> clazz, BooleanResult<T> result) {
        T found;
        for (var pane : getPanes()) {
            found = PaneFactory.getPane(pane, clazz);
            if (found != null && result.process(found)) {
                return found;
            }
        }
        return null;
    }

    /**
     * Retrieves a list of unique class names for all currently open panes.
     * Useful for analytics or lightweight state saving.
     *
     * @return A list of fully qualified class names for the open panes.
     */
    @NonNull
    List<String> getOpenedPanesClassNames();

    /**
     * Persists the current state and order of open panes (optional operation).
     * Implementations might save pane types, state, and order to SharedPreferences, a database,
     * or a file.
     */
    void persistPanes();

    /**
     * Removes the pane at the specified index.
     *
     * @param index The index of the pane to remove.
     * @return The removed {@link Pane}, or {@code null} if the index was invalid or removal failed.
     * @throws IndexOutOfBoundsException if the index is out of range (index < 0 || index >= size
     *                                   ()).
     */
    Pane remove(int index);

    /**
     * Removes the specified pane instance.
     *
     * @param pane The pane instance to remove. Must not be null.
     * @return {@code true} if the pane was found and removed, {@code false} otherwise (including
     * if pinned).
     */
    boolean remove(@NonNull Pane pane);

    /**
     * Removes the specified pane instance *only if* it is found at the given index.
     * This can act as a safeguard during removal operations.
     *
     * @param pane  The pane instance to remove. Must not be null.
     * @param index The expected index of the pane.
     * @return {@code true} if the pane was found at the index and removed, {@code false}
     * otherwise (not found, wrong index, pinned, etc.).
     * @throws IndexOutOfBoundsException if the index is out of range (index < 0 || index >= size
     *                                   ()).
     */
    boolean remove(@NonNull Pane pane, int index);

    /**
     * Removes all panes contained in the provided list from this window, if they are not pinned.
     *
     * @param paneList The list of panes to remove. Must not be null.
     * @return {@code true} if the window state changed as a result of the call (i.e., at least
     * one pane was removed).
     */
    boolean removeAll(@NonNull List<Pane> paneList);

    /**
     * Removes any persisted pane state information (optional operation).
     */
    void removePersistedPane(@NonNull Pane pane);

    /**
     * Removes all panes within the specified index range (fromIndex inclusive, toIndex exclusive),
     * skipping any panes that are pinned.
     *
     * @param fromIndex The starting index (inclusive).
     * @param toIndex   The ending index (exclusive).
     * @throws IndexOutOfBoundsException if the range is invalid.
     */
    void removeRange(int fromIndex, int toIndex);

    /**
     * Restores the state and order of panes from persisted storage (optional operation).
     * Should be called during initialization, potentially replacing any existing panes.
     *
     * @param onPanesLoaded Callback to invoke with the restored panes.
     */
    void restorePanes(@NonNull Consumer<List<Pane>> onPanesLoaded);

    /**
     * Retains only the panes that are contained in the provided list.
     * Removes all other panes unless they are pinned.
     *
     * @param paneList The list of panes to retain. Must not be null.
     * @return {@code true} if the window state changed as a result of the call (i.e., at least
     * one pane was removed).
     */
    boolean retainAll(@NonNull List<Pane> paneList);

    /**
     * Selects the tab corresponding to the given pane instance.
     * If the pane is not found, no action occurs.
     *
     * @param pane The pane to select. Must not be null.
     */
    void selectTab(@NonNull Pane pane);

    /**
     * Selects the tab at the specified index.
     * If the index is invalid, no action occurs.
     *
     * @param index The index of the tab/pane to select.
     */
    void selectTab(int index);

    /**
     * Displays a contextual popup menu for the given pane, typically anchored to its tab view.
     *
     * @param anchor   The {@link View} to which the popup menu should be anchored. Must not be
     *                 null.
     * @param pane     The {@link Pane} for which the menu is being shown. Must not be null.
     * @param listener The listener to handle menu item clicks. Must not be null.
     */
    void showPopupMenu(@NonNull View anchor, @NonNull Pane pane,
        @NonNull OnMenuItemClickListener listener);

    /**
     * Sets whether icons should be shown in custom tab layouts (if applicable).
     *
     * @param show {@code true} to show icons, {@code false} to hide them.
     */
    void showTabIcons(boolean show);

    /**
     * Synchronizes the UI representation (e.g., title, icon, pin state) of the tab
     * corresponding to the given pane.
     *
     * @param pane The pane whose tab UI needs synchronization. Must not be null.
     */
    void syncTab(@NonNull Pane pane);

    /**
     * Synchronizes the UI representation of all tabs with their corresponding panes.
     */
    void syncTabs();

    /**
     * Updates the pinned state of the specified pane and synchronizes its tab UI.
     *
     * @param pane   The pane to update. Must not be null.
     * @param pinned The desired pinned state.
     */
    void updatePinState(@NonNull Pane pane, boolean pinned);
}
