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
