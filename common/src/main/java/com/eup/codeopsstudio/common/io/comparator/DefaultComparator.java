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

import java.util.Comparator;

/**
 * Default {@link Comparator} provides a foundation to build upon
 *
 * @param <T> the type of objects that may be compared or sorted by this comparator
 * @author Etido Peter
 * @since 1.0.2 (2)
 */
public class DefaultComparator extends AbstractComparator<Object> {

    public static final Comparator<Object> DEFAULT_COMPARATOR = new DefaultComparator();
    public static final Comparator<Object> DEFAULT_REVERSE =
        new ReverseComparator<Object>(DEFAULT_COMPARATOR);

    public DefaultComparator() {
        // empty
    }

    /**
     * Compares two arguments for order, returns a negative integer, zero, or a positive integer as
     * the first argument == null & the second != null, and second either == null or not, or != null
     * and the second == null.
     *
     * @param o1 the first object to be compared.
     * @param o2 the second object to be compared.
     * @return a negative integer, zero, or a positive integer as the first argument == null and the
     * second != null equal to, or != null and the second == null.
     * @throws ClassCastException if the arguments' types prevent them from being compared by this
     *                            comparator.
     */
    @Override
    public int compare(final Object o1, final Object o2) {
        if (o1 == null && o2 != null) {
            return -1;
        } else if (likeTerms(o1, o2)) {
            return 0;
        } else {
            return 1;
        }
    }

    private boolean likeTerms(Object a, Object b) {
        return a != null && b != null || a == null && b == null;
    }

    /**
     * String representation of this comparator.
     *
     * @return String representation of this comparator.
     */
    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
