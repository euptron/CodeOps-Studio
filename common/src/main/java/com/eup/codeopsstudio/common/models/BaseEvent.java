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

package com.eup.codeopsstudio.common.models;

/**
 * Foundational event class
 *
 * @param <T> the event type to hold
 */
public abstract class BaseEvent<T> {

    private final T content;
    private boolean hasBeenHandled = false;

    /**
     * Constructor for Event.
     *
     * @param content The content of the event.
     */
    public BaseEvent(T content) {
        this.content = content;
    }

    /**
     * Returns the content and prevents its use again.
     *
     * @return The content if not handled, otherwise null.
     */
    public T getContentIfNotHandled() {
        if (hasBeenHandled) {
            return null;
        } else {
            hasBeenHandled = true;
            return content;
        }
    }

    /**
     * Returns whether the event has been handled.
     */
    public final boolean getHasBeenHandled() {
        return hasBeenHandled;
    }

    /**
     * Returns the content, even if it's already been handled.
     *
     * @return The content.
     */
    public T peekContent() {
        return content;
    }
}
