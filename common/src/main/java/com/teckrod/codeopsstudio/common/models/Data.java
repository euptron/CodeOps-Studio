/*************************************************************************
 * This file is part of CodeOps Studio.
 * CodeOps Studio - code anywhere anytime
 * https://github.com/euptron/CodeOps-Studio
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
 
   package com.eup.codeopsstudio.common.models;

import androidx.annotation.NonNull;
import java.util.Objects;

/**
 * Data Model with key per data
 * @author EUP
 */
public class Data<K, V> {
  public final K key;
  public final V value;

  /**
   * Constructor for a Data.
   *
   * @param key the key object in the Data
   * @param value the value object in the data
   */
  @SuppressWarnings("UnknownNullness") // Generic nullness should come from type annotations.
  public Data(K key, V value) {
    this.key = key;
    this.value = value;
  }

  /**
   * Checks the two objects for equality by delegating to their respective {@link
   * Object#equals(Object)} methods.
   *
   * @param o the {@link Data} to which this one is to be checked for equality
   * @return true if the underlying objects of the Data are both considered equal
   */
  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Data)) {
      return false;
    }
    Data<?, ?> p = (Data<?, ?>) o;
    return Objects.equals(p.key, key);
  }

  /**
   * Compute a hash code using the hash codes of the underlying objects
   *
   * @return a hashcode of the Data
   */
  @Override
  public int hashCode() {
    return (key == null ? 0 : key.hashCode()) ^ (value == null ? 0 : value.hashCode());
  }

  @NonNull
  @Override
  public String toString() {
    return "Data{" + key + " " + value + "}";
  }

  /**
   * Convenience method for creating an appropriately typed data.
   *
   * @param a the key object in the Data
   * @param b the value object in the data
   * @return a Data that is templatized with the types of a and b
   */
  @NonNull
  @SuppressWarnings("UnknownNullness") // Generic nullness should come from type annotations.
  public static <A, B> Data<A, B> create(A a, B b) {
    return new Data<>(a, b);
  }
}
