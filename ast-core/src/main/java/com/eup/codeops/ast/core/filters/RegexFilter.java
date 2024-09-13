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

package com.eup.codeops.ast.core.filters;

import java.util.regex.Pattern;

/**
 * A filter that uses regular expressions (Regex) to match strings.
 *
 * @version 1.0
 * @since 1.0
 * @author EUP (2024-08-22)
 */
public class RegexFilter extends Filter<String> {

  /** The compiled regular expression pattern. */
  protected Pattern pattern;

  /**
   * Constructs a RegexFilter with multiple regular expressions.
   *
   * @param regex an array of regular expressions to match
   */
  public RegexFilter(String[] regex) {
    this(String.join("|", regex));
  }

  /**
   * Constructs a RegexFilter with a single regular expression.
   *
   * @param regex the regular expression to match
   */
  public RegexFilter(String regex) {
    this.pattern = Pattern.compile(regex);
  }

  /**
   * Filters the given string based on the regex.
   *
   * @param value the string to filter
   * @return true if the string matches the regex, false otherwise
   */
  @Override
  public boolean filter(String value) {
    return pattern.matcher(value).matches();
  }

  /**
   * Returns a string representation of this filter, including the regex.
   *
   * @return a string representation of this filter
   */
  @Override
  public String toString() {
    String clazzName = getClass().getSimpleName();
    String pattern = this.pattern.toString();
    return String.format("%s[%s]", clazzName, pattern);
  }
}
