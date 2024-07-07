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
 
   package com.eup.codeopsstudio.pane;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import java.util.UUID;

/**
 * The {@code Rap} class represents a {@link Pane} fundamentals.
 *
 * <p>Alias: Random Access Pane This class shouldn't be sub-classed. If you needs additional fields,
 * either they should be generic enough to be added here, or you should pass the information as a
 * string argument preferably a JSON string as it's more flexible.
 *
 * <p>Intent usage:
 *
 * <p>Parse into intent:
 *
 * <pre>{@code
 * myJson = "[{"title":"Welcome","pinned":false,"selected":true,"uuid":"7728d540-c7b0-4b6f-8911-66dc18a5c04b","class_name":"EditableTextPane","conicalName":"com.eup.codeopsstudio.pane.EditableTextPane","editable_content":"fd"}]"
 * myIntent.putExtra("pane_state", new Rap(myJson));
 * }</pre>
 *
 * <p>Access from intent parcel
 *
 * <pre>{@code
 *   Bundle data = getIntent().getExtras();
 *   Rap Rap = (Rap) data.getParcelable("pane_state");
 *   if (Rap != null) {
 * 	 // TODO: Handle
 *   }
 * }</pre>
 *
 * @author EUP
 * @since 0.0.1
 */
public final class Rap implements Parcelable {

  public final String ARGUMENTS;
  public final String IDENTIFIER;

  /**
   * Constructs a Rap object from a {@link Pane}.
   *
   * @param arguments A JSON string arguments associated to the Pane
   * @param identifier The pane uuid used to identify a pane state
   */
  public Rap(String arguments, String identifier) {
    this.ARGUMENTS = arguments;
    this.IDENTIFIER = identifier;
  }

  /**
   * Constructs a Rap object from a Parcel.
   *
   * @param in The Parcel from which to read the object's state.
   */
  public Rap(Parcel in) {
    ARGUMENTS = in.readString();
    IDENTIFIER = in.readString();
  }

  /**
   * Describes the kinds of special objects contained in the Parcelable instance's marshaled
   * representation. Subclasses can override this method to provide additional information about
   * special objects contained in the parcel.
   *
   * @return A bitmask indicating the set of special object types marshaled by this Parcelable
   *     object instance.
   */
  @Override
  public int describeContents() {
    return 0;
  }

  /**
   * Writes object's state to a Parcel. Subclasses can override this method to write object's state
   * to parcel if needed.
   *
   * @param dest The Parcel in which to place the object's state.
   * @param flags Additional flags about how the object should be written.
   */
  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(ARGUMENTS);
    dest.writeString(IDENTIFIER);
  }

  /** Creates a new instance of the Parcelable class, instantiating it from the given Parcel. */
  public static final Parcelable.Creator<Rap> CREATOR =
      new Parcelable.Creator<Rap>() {
        @Override
        public Rap createFromParcel(Parcel in) {
          return new Rap(in);
        }

        @Override
        public Rap[] newArray(int size) {
          return new Rap[size];
        }
      };
}
