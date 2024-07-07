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
 
   package com.eup.codeopsstudio.common.util;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/** 
* Utility class if you want to only override one method of {@link RecyclerView.OnScrollListener} 
* 
* @author EUP
*/
public class RecyclerViewOnScrollListener extends RecyclerView.OnScrollListener {
	/**
	* Callback method to be invoked when RecyclerView's scroll state changes.
	*
	* @param recyclerView The RecyclerView whose scroll state has changed.
	* @param newState     The updated scroll state. One of {@link #SCROLL_STATE_IDLE},
	*                     {@link #SCROLL_STATE_DRAGGING} or {@link #SCROLL_STATE_SETTLING}.
	*/
	@Override
	public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
	}

	/**
	* Callback method to be invoked when the RecyclerView has been scrolled. This will be
	* called after the scroll has completed.
	* <p>
	* This callback will also be called if visible item range changes after a layout
	* calculation. In that case, dx and dy will be 0.
	*
	* @param recyclerView The RecyclerView which scrolled.
	* @param dx           The amount of horizontal scroll.
	* @param dy           The amount of vertical scroll.
	*/
	@Override
	public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
	}
}
