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
 * questions or need additional information. Email: euptron@gmail.com
 */

package com.eup.codeopsstudio.common.io.comparator;

import androidx.annotation.NonNull;

import java.util.Comparator;
import java.util.Objects;

/**
 * Reversed {@link Comparator} to compare two typed-objects using the delegate {@link Comparator}.
 *
 * @param <T> the type of objects that may be compared or sorted by this comparator
 * @author Etido Peter
 * @since 1.0.2 (2)
 */
public class ReverseComparator<T> extends AbstractComparator<T> {

    private final Comparator<T> delegate;

    /**
     * Constructs an instance with the specified delegate {@link Comparator}.
     *
     * @param delegate The comparator to delegate to.
     */
    public ReverseComparator(final Comparator<T> delegate) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
    }

    /**
     * Compares its two arguments for order. Returns a negative integer, zero, or a positive integer
     * as the first argument is less than, equal to, or greater than the second.
     *
     * <p>The implementor must ensure that {@link Integer#signum signum}{@code (compare(x, y)) ==
     * -signum(compare(y, x))} for all {@code x} and {@code y}. (This implies that {@code compare(x,
     * y)} must throw an exception if and only if {@code compare(y, x)} throws an exception.)
     *
     * <p>The implementor must also ensure that the relation is transitive: {@code ((compare(x,
     * y)>0)
     * && (compare(y, z)>0))} implies {@code compare(x, z)>0}.
     *
     * <p>Finally, the implementor must ensure that {@code compare(x, y)==0} implies that {@code
     * signum(compare(x, z))==signum(compare(y, z))} for all {@code z}.
     *
     * @param o1 the first object to be compared.
     * @param o2 the second object to be compared.
     * @return a negative integer, zero, or a positive integer as the first argument is less than,
     * equal to, or greater than the second.
     * @throws NullPointerException if an argument is null and this comparator does not permit null
     *                              arguments
     * @throws ClassCastException   if the arguments' types prevent them from being compared by this
     *                              comparator.
     * @apiNote It is generally the case, but <i>not</i> strictly required that {@code (compare(x,
     * y)==0) == (x.equals(y))}. Generally speaking, any comparator that violates this condition
     * should clearly indicate this fact. The recommended language is "Note: this comparator
     * imposes orderings that are inconsistent with equals."
     */
    @Override
    public int compare(final T o1, final T o2) {
        return delegate.compare(o2, o1); // parameters switched round
    }

    /**
     * Returns the String representation of this typed comparator.
     *
     * @return String representation of this typed comparator.
     */
    @NonNull
    @Override
    public String toString() {
        return super.toString() + "[" + delegate + "]";
    }
}
