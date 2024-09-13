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
 
   package com.eup.codeopsstudio.ui.editor.actions;

/**
 * A simple model for editor actions or shortcuts. Represents actions or shortcuts that users can
 * interact with in a code editor. Each action consists of a name (label) and, optionally, a value
 * (text) to be inserted.
 *
 * @author EUP
 */
public class EditorAction {

  private String name;
  private String value;

  /**
   * Constructs an EditorAction with the given name (label).
   *
   * @param name The name (label) of the editor action.
   */
  public EditorAction(String name) {
    this.name = name;
  }

  /**
   * Constructs an EditorAction with the given name (label) and value (text).
   *
   * @param name The name (label) of the editor action.
   * @param value The value (text) to be inserted associated with the editor action.
   */
  public EditorAction(String name, String value) {
    this.name = name;
    this.value = value;
  }

  /**
   * Retrieves the name (label) of the editor action.
   *
   * @return The name (label) of the editor action.
   */
  public String getName() {
    return this.name;
  }

  /**
   * Sets the name (label) of the editor action.
   *
   * @param name The name (label) to set for the editor action.
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Retrieves the value (text) to be inserted associated with the editor action.
   *
   * @return The value (text) to be inserted associated with the editor action.
   */
  public String getValue() {
    return this.value;
  }

  /**
   * Sets the value (text) to be inserted associated with the editor action.
   *
   * @param value The value (text) to associate with the editor action.
   */
  public void setValue(String value) {
    this.value = value;
  }
}
