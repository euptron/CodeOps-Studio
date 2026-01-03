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

package com.eup.codeopsstudio.domain;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.eup.codeopsstudio.common.Constants;
import com.eup.codeopsstudio.models.user.User;
import com.eup.codeopsstudio.util.Wizard;

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
 * @author Etido Peter
 */
public class FormatDateUseCase {

    private final SimpleDateFormat formatter;

  public FormatDateUseCase(@NonNull User user) {
    formatter = new SimpleDateFormat(user.getPreferredDateFormat(), user.getPreferredLocale());
    String tz =
        Wizard.isEmpty(user.getPreferredDateFormat())
            ? Constants.PREFERRED_TIME_ZONE
            : user.getPreferredDateFormat();
    formatter.setTimeZone(TimeZone.getTimeZone(tz));
  }

  public FormatDateUseCase(@NonNull User user, @Nullable String timeZone) {
    formatter = new SimpleDateFormat(user.getPreferredDateFormat(), user.getPreferredLocale());
    formatter.setTimeZone(TimeZone.getTimeZone(timeZone));
    }

    public FormatDateUseCase(@NonNull String datePattern) {
        this(datePattern, Locale.getDefault(Locale.Category.FORMAT), null);
    }

    public FormatDateUseCase(@NonNull String datePattern, @NonNull Locale dateLocale,
        @Nullable String timeZone) {
        formatter = new SimpleDateFormat(datePattern, dateLocale);
        if (Wizard.isEmpty(timeZone)) return;
        formatter.setTimeZone(TimeZone.getTimeZone(timeZone));
    }

    public FormatDateUseCase(@NonNull String datePattern, @NonNull Locale dateLocale) {
        this(datePattern, dateLocale, null);
    }

    public String format(long timestamp) {
        return format(new Date(timestamp));
    }

    public String format(Date date) {
        return formatter.format(date);
    }
}
