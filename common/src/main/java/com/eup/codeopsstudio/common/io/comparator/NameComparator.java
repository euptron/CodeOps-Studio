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

import com.eup.codeopsstudio.common.models.MetaDocument;

import org.apache.commons.io.IOCase;

import java.util.Comparator;

/**
 * Compare the <strong>names</strong> of two {@link MetaDocument} for order (see {@link
 * MetaDocument#getName()}).
 *
 * <p>This comparator can be used to sort lists or arrays of documents by their name either in a
 * case-sensitive, case-insensitive or system dependent case-sensitive way. A number of singleton
 * instances are provided for the various case sensitivity options (using {@link IOCase}) and the
 * reverse of those options.
 *
 * @author Etido Peter
 */
public class NameComparator extends AbstractComparator<MetaDocument> {

    public static final Comparator<MetaDocument> SENSITIVE = newInstance(IOCase.SENSITIVE);
    public static final Comparator<MetaDocument> SENSITIVE_REVERSE =
        new ReverseComparator<>(SENSITIVE);
    public static final Comparator<MetaDocument> INSENSITIVE = newInstance(IOCase.INSENSITIVE);
    public static final Comparator<MetaDocument> INSENSITIVE_REVERSE =
        new ReverseComparator<>(INSENSITIVE);
    public static final Comparator<MetaDocument> SYSTEM = newInstance(IOCase.SYSTEM);
    public static final Comparator<MetaDocument> SYSTEM_REVERSE = new ReverseComparator<>(SYSTEM);
    private final IOCase ioCase;

    public NameComparator(final IOCase ioCase) {
        this.ioCase = IOCase.value(ioCase, IOCase.SENSITIVE);
    }

    public static NameComparator newInstance(final IOCase ioCase) {
        return new NameComparator(ioCase);
    }

    @Override
    public int compare(final MetaDocument o1, final MetaDocument o2) {
        return ioCase.checkCompareTo(o1.getName(), o2.getName());
    }

    @NonNull
    @Override
    public String toString() {
        return super.toString() + "[ioCase=" + ioCase + "]";
    }
}
