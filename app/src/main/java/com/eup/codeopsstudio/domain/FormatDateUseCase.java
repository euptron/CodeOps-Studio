package com.eup.codeopsstudio.domain;

import com.eup.codeopsstudio.models.user.User;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Domain for formatting date
 *
 * <p>As recommended by the article<a href="https://developer.android.com/topic/architecture>Guide
 * to app architecture</a>.
 *
 * @author EUP
 */
public class FormatDateUseCase {

  private SimpleDateFormat formatter;

  public FormatDateUseCase(User user) {
    formatter = new SimpleDateFormat(user.getPreferredDateFormat(), user.getPreferredLocale());
    // who hates UTC ?
    formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
  }

  public String format(long timestamp) {
    return format(new Date(timestamp));
  }

  public String format(Date date) {
    return formatter.format(date);
  }
}
