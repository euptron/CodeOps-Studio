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

package com.eup.codeopsstudio.logging;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Log {

  private int mIcon;
  private CharSequence mTag;
  private CharSequence mMessage;
  private CharSequence mDateFormat;
  private CharSequence mLogLevel;

  // unique ID for this class
  private UUID id;
  private static final List<UUID> generatedIds = new ArrayList<>();

  /**
   * Basic log
   *
   * @param message log message
   */
  public Log(CharSequence message) {
    this(null, 0, null, null, message);
  }

  /**
   * Diagnostics log
   *
   * @param icon diagnostics icon resource
   * @param message diagnostics message
   */
  public Log(int icon, CharSequence message) {
    this(null, icon, null, null, message);
  }

  /**
   * Normal log
   *
   * @param tag log tag
   * @param level the log level
   * @param message log message
   */
  public Log(CharSequence tag, CharSequence level, CharSequence message) {
    this(null, 0, tag, level, message);
  }

  /**
   * Debug log
   *
   * @param date log date
   * @param tag log tag
   * @param level the log level
   * @param message log message
   */
  public Log(CharSequence date, CharSequence tag, CharSequence level, CharSequence message) {
    this(date, 0, tag, level, message);
  }

  /**
   * Verbose log
   *
   * @param date log date
   * @param icon log icon resource
   * @param tag log tag
   * @param level the log level
   * @param message log message
   */
  public Log(
      CharSequence date, int icon, CharSequence tag, CharSequence level, CharSequence message) {
    mDateFormat = date;
    mIcon = icon;
    mTag = tag;
    mLogLevel = level;
    mMessage = message;
    this.id = generateUUID();
  }

  public int getIcon() {
    return mIcon;
  }

  public CharSequence getTag() {
    return mTag;
  }

  public CharSequence getMessage() {
    return mMessage;
  }

  public CharSequence getDateFormat() {
    return mDateFormat;
  }

  public CharSequence getLevel() {
    return mLogLevel;
  }

  public UUID getID() {
    return this.id;
  }

  protected UUID generateUUID() {
    UUID generatedId = UUID.randomUUID();
    if (isUniqueId(generatedId)) {
      generatedIds.add(generatedId);
      return generatedId;
    } else {
      return generateUUID();
    }
  }

  private boolean isUniqueId(UUID id) {
    return !generatedIds.contains(id);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Log log = (Log) o;
    return Objects.equals(mMessage, log.getMessage())
        && Objects.equals(mTag, log.getTag())
        && Objects.equals(mDateFormat, log.getDateFormat())
        && Objects.equals(mLogLevel, log.getLevel());
  }

  @Override
  public int hashCode() {
    return Objects.hash(new Object[] {mMessage, mTag, mDateFormat, mLogLevel});
  }
}
