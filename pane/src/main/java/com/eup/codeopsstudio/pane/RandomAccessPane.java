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

package com.eup.codeopsstudio.pane;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

/**
 * The {@code RandomAccessPane} class represents a {@link Pane} fundamentals.
 *
 * <p>Alias: Random Access Pane This class shouldn't be sub-classed. If you need additional fields,
 * either they should be generic enough to be added here, or you should pass the information as a
 * string argument preferably a JSON string as it's more flexible.
 *
 * <p>Intent usage:
 *
 * <p>Parse into intent:
 *
 * <pre>{@code
 * myJson = "[{"title":"Welcome","pinned":false,"selected":true,"uuid":"7728d540-c7b0-4b6f-8911-66dc18a5c04b","class_name":"EditableTextPane","conicalName":"com.eup.codeopsstudio.pane.EditableTextPane","editable_content":"fd"}]"
 * myIntent.putExtra("pane_state", new RandomAccessPane(myJson));
 * }</pre>
 *
 * <p>Access from intent parcel
 *
 * <pre>{@code
 *   Bundle data = getIntent().getExtras();
 *   RandomAccessPane RandomAccessPane = (RandomAccessPane) data.getParcelable("pane_state");
 *   if (RandomAccessPane != null) {
 * 	   // Handle
 *   }
 * }</pre>
 *
 * @author Etido Peter
 * @since 0.0.1
 */
public final class RandomAccessPane implements Parcelable {

    /**
     * Creates a new instance of the Parcelable class, instantiating it from the given Parcel.
     */
    public static final Parcelable.Creator<RandomAccessPane> CREATOR = new Parcelable.Creator<>() {
        @Override
        public RandomAccessPane createFromParcel(Parcel in) {
            return new RandomAccessPane(in);
        }

        @Override
        public RandomAccessPane[] newArray(int size) {
            return new RandomAccessPane[size];
        }
    };

    private final String arguments;
    private final String identifier;

    /**
     * Constructs a RandomAccessPane object from a {@link Pane}.
     *
     * @param arguments  A JSON string arguments associated to the Pane
     * @param identifier The pane uuid used to identify a pane state
     */
    public RandomAccessPane(String arguments, String identifier) {
        this.arguments  = arguments;
        this.identifier = identifier;
    }

    /**
     * Constructs a RandomAccessPane object from a Parcel.
     *
     * @param in The Parcel from which to read the object's state.
     */
    public RandomAccessPane(@NonNull Parcel in) {
        arguments  = in.readString();
        identifier = in.readString();
    }

    /**
     * Describes the kinds of special objects contained in the Parcelable instance's marshaled
     * representation. Subclasses can override this method to provide additional information about
     * special objects contained in the parcel.
     *
     * @return A bitmask indicating the set of special object types marshaled by this Parcelable
     * object instance.
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Writes object's state to a Parcel. Subclasses can override this method to write object's
     * state
     * to parcel if needed.
     *
     * @param dest  The Parcel in which to place the object's state.
     * @param flags Additional flags about how the object should be written.
     */
    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(arguments);
        dest.writeString(identifier);
    }

    public String getArguments() {
        return arguments;
    }
}
