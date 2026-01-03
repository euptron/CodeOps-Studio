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

package com.eup.codeopsstudio.common.io.comparator;

import androidx.annotation.NonNull;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Abstract {@link Comparator} which provides sorting for typed-object arrays and list
 *
 * <p>* @since 1.0.2 (2)
 *
 * @author Etido Peter
 */
public abstract class AbstractComparator<T> implements Comparator<T> {

    @NonNull
    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    public T[] sort(final T[] types) {
        if (types != null) {
            Arrays.sort(types, this);
        }
        return types;
    }

    public List<T> sort(final List<T> types) {
        if (types != null) {
            types.sort(this);
        }
        return types;
    }
}
