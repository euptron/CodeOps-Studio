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
 
   package com.eup.codeopsstudio.ui.settings.api;

/** A class that defines an about item */
public class AboutItems {

  private int icon;
  private String title;
  private String summary;

  public AboutItems() {
    // Empty constructor
  }

  /**
   * An item with 3 parameter
   *
   * @param icon The icon resource integer
   * @param Title The title of this Item
   * @param summary The description this item
   */
  public AboutItems(int icon, String title, String summary) {
    this.icon = icon;
    this.title = title;
    this.summary = summary;
  }

  /**
   * An item with 2 parameter
   *
   * @param name The name of this Item
   * @param summary The description this item
   */
  public AboutItems(String title, String summary) {
    this.title = title;
    this.summary = summary;
  }

  public int getIcon() {
    return this.icon;
  }

  public void setIcon(int icon) {
    this.icon = icon;
  }

  public String getTitle() {
    return this.title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getSummary() {
    return this.summary;
  }

  public void setSummary(String summary) {
    this.summary = summary;
  }
}
