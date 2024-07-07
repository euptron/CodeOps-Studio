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
 
   package com.eup.codeopsstudio.events;

import com.eup.codeopsstudio.pane.Pane;
import com.eup.codeopsstudio.common.models.BaseEvent;

/**
 * Event for managing panes
 * @see MainFragment
 * @see BaseFragment
 */
public class PaneEvent extends BaseEvent {

  final Pane pane;
  
  public PaneEvent(Pane pane) {
    this.pane = pane;
  }
  
  public Pane getPane() {
    return this.pane;
  }
}
