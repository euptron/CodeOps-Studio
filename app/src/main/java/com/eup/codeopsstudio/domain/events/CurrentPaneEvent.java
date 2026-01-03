/*
 * This file is part of CodeOps Studio.
 * CodeOps Studio - Code anywhere anytime
 * https://github.com/euptron/CodeOps-Studio
 * Copyright (C) 2024-2026 Etido Peter
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

package com.eup.codeopsstudio.domain.events;

import androidx.core.util.Pair;

import com.eup.codeopsstudio.MainFragment;
import com.eup.codeopsstudio.common.models.BaseEvent;
import com.eup.codeopsstudio.pane.Pane;
import com.eup.codeopsstudio.ui.editor.BaseFragment;

/**
 * Event for managing panes
 *
 * @author Etido Peter
 * @see MainFragment
 * @see BaseFragment
 */
public class CurrentPaneEvent extends BaseEvent<Pair<Integer, Pane>> {

    /**
     * Constructs a new pane event.
     *
     * @param content The content of the event, the first is the
     *                index or current position of a pane in it's parent
     *                tab-layout and second is the current pane.
     */
    public CurrentPaneEvent(Pair<Integer, Pane> content) {
        super(content);
    }

    public int getIndex() {
        return peekContent().first;
    }

    public Pane getPane() {
        return peekContent().second;
    }
}
