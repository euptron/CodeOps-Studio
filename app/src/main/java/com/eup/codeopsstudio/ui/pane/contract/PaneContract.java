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

package com.eup.codeopsstudio.ui.pane.contract;

import androidx.annotation.NonNull;

import com.eup.codeopsstudio.ui.pane.PaneWindow;

import java.util.function.Consumer;

/**
 * A contract that defines how a {@link PaneWindow} interacts with its panes.
 *
 * <p>This class provides the rules and guidelines for pane management and communication within the
 * {@link PaneWindow} system in a decoupled and scalable fashion.
 *
 * @param <I> the Input type to be processed
 * @param <O> the Output type of the processed contract
 * @author Etido Peter
 * @see PaneWindow
 * @see PaneContracts
 */
public abstract class PaneContract<I, O> {
    /**
     * Publish a new contract that processes the given input and consumes the output as callback.
     *
     * @param contract the input to process
     * @param consumer the output to consume
     */
    public abstract void publish(@NonNull I contract, @NonNull Consumer<O> consumer);
}
