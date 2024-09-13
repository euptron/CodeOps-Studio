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

import java.util.Objects;

public class Log {

  private int mIcon;
  private CharSequence mTag;
  private CharSequence mMessage;
  private CharSequence mDateFormat;
  private CharSequence mLogLevel;

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

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Log log = (Log) o;
    return mMessage.equals(log.mMessage) && mTag.equals(log.mTag);
  }

  @Override
  public int hashCode() {
    return Objects.hash(mMessage);
  }
}
