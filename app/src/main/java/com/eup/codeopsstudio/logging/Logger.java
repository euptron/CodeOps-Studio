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
 
   package com.eup.codeopsstudio.logging;

import android.text.style.ForegroundColorSpan;
import android.text.Spannable;
import android.text.SpannableString;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import com.eup.codeopsstudio.viewmodel.MainViewModel;

public class Logger {

  public enum LogClass {
    BUILD,
    IDE
  }

  private boolean mAttached;

  private MainViewModel model;
  private ViewModelStoreOwner vmso;
  public LogLevel logLevel;
  public LogClass logClass;

  public Logger(LogClass logClass) {
    this.logClass = logClass;
  }

  public void attach(ViewModelStoreOwner vmso) {
    this.vmso = vmso;
    model = new ViewModelProvider(vmso).get(MainViewModel.class);
    mAttached = true;
  }

  public void d(String message) {
    if (!mAttached) {
      return;
    }
    add(new Log(highlightNumbers(message)));
  }

  public void d(String tag, String message) {
    if (!mAttached) {
      return;
    }
    add(new Log(formatDate(), tag, getLogLevel(LogLevel.DEBUG), highlightNumbers(message)));
  }

  public void e(String tag, String message) {
    if (!mAttached) {
      return;
    }
    add(
        new Log(
            formatDate(), tag, highlightSpan(getLogLevel(LogLevel.ERROR), 0xffff0000), message));
  }

  public void w(String tag, String message) {
    if (!mAttached) {
      return;
    }
    add(new Log(formatDate(), tag, highlightSpan(getLogLevel(LogLevel.WARN), 0xffff7043), message));
  }

  public void i(String tag, String message) {
    if (!mAttached) {
      return;
    }
    add(
        new Log(
            formatDate(),
            tag,
            highlightSpan(getLogLevel(LogLevel.INFO), 0xFF0D47A1),
            highlightNumbers(message)));
  }

  private void add(Log log) {
    if (logClass == null) {
      throw new IllegalArgumentException("LogClass has not been set");
    }
    if (logClass == LogClass.BUILD) {
      ArrayList<Log> currentList = model.getBUILDLogs().getValue();
      if (currentList == null) {
        currentList = new ArrayList<>();
      }
      currentList.add(log);
      model.getBUILDLogs().postValue(currentList);
    } else if (logClass == LogClass.IDE) {
      ArrayList<Log> currentList = model.getIDELogs().getValue();
      if (currentList == null) {
        currentList = new ArrayList<>();
      }
      currentList.add(log);
      model.getIDELogs().postValue(currentList);
    }
  }

  public void clear() {
    if (logClass == null) {
      throw new IllegalArgumentException("LogClass has not been set");
    }
    if (logClass == LogClass.BUILD) {
      model.getBUILDLogs().setValue(new ArrayList<>());
    } else if (logClass == LogClass.IDE) {
      model.getIDELogs().setValue(new ArrayList<>());
    }
  }

  // Method to format the date with color using spans
  private SpannableString formatDate() {
    SpannableString spannableDate = new SpannableString(getTime());
    spannableDate.setSpan(
        new ForegroundColorSpan(0xFF1B5E20),
        0,
        spannableDate.length(),
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    return spannableDate;
  }

  private SpannableString highlightSpan(String message, int color) {
    SpannableString spannableMessage = new SpannableString(message);
    spannableMessage.setSpan(
        new ForegroundColorSpan(color), 0, message.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    return spannableMessage;
  }

  public String getLogLevel(LogLevel logLevel) {
    return LogLevel.getLevel(logLevel);
  }

  private SpannableString highlightNumbers(String message) {
    SpannableString spannableMessage = new SpannableString(message);
    Pattern pattern = Pattern.compile("\\b\\d+\\b"); // Regular expression to match numbers
    Matcher matcher = pattern.matcher(message);
    while (matcher.find()) {
      spannableMessage.setSpan(
          new ForegroundColorSpan(0xFF00FF00),
          matcher.start(),
          matcher.end(),
          Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }
    return spannableMessage;
  }

  public static String getTime() {
    SimpleDateFormat dateFormat =
        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:S", Locale.getDefault());
    return dateFormat.format(new Date());
  }
}
