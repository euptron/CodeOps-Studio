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
 
   package com.eup.codeopsstudio.listeners;

import com.google.android.material.tabs.TabLayout;

/**
 * Interface definition for a callback to be invoked when a pane tab is selected or interacted with.
 * Implementations of this interface can be used to perform actions on pane tabs such as closing, pinning, etc.
 * 
 * @author EUP
 */
public interface onPaneTabSelectedListener {
	/**
	 * Called when a pane tab is closed.
	 * 
	 * @param tab The TabLayout.Tab representing the pane tab to be closed.
	 */
	void close(TabLayout.Tab tab);

	/**
	 * Called when all pane tabs except the specified tab are closed.
	 * 
	 * @param tabToKeep The TabLayout.Tab to keep open while closing others.
	 */
	void closeOthers(TabLayout.Tab tabToKeep);

	/**
	 * Called when all pane tabs are closed.
	 */
	void closeAll();

	/**
	 * Called when the pane tab to the right of the current tab is closed.
	 * 
	 * @param tab The TabLayout.Tab representing the reference tab.
	 * @param first Closes only the next unpinned pane tab if true
	 */
	void closeToRightOf(TabLayout.Tab tab, boolean first);

	/**
	 * Called when the pane tabs to the left of the current tab is closed.
	 * 
	 * @param tab The TabLayout.Tab representing the reference tab.
	 * @param first Closes only the next unpinned pane tab if true
	 */
	void closeToLeftOf(TabLayout.Tab tab, boolean first);

	/**
	 * Called when a pane tab is pinned.
	 * 
	 * @param tab The TabLayout.Tab representing the pane tab to be pinned.
	 * @param isPinned pinned True of pane is pinned
	 */
	void pinPaneTab(TabLayout.Tab tab, boolean isPinned);
}
