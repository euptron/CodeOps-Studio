/*
 * This file is part of CodeOps Studio.
 * CodeOps Studio - Code anywhere anytime
 * https://github.com/euptron/CodeOps-Studio
 * Copyright (C) 2024-2025 Etido Peter
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

package com.eup.codeopsstudio.models.logger;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

import com.eup.codeopsstudio.common.Constants;
import com.eup.codeopsstudio.domain.FormatDateUseCase;
import com.eup.codeopsstudio.models.user.User;
import com.eup.codeopsstudio.util.Wizard;
import com.eup.codeopsstudio.viewmodel.MainViewModel;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Logger {

    private final LogClass logClass;
    private boolean isAttached;
    private MainViewModel model;

    public Logger(@NonNull LogClass logClass) {
        this.logClass = logClass;
    }

    public void attach(@NonNull ViewModelStoreOwner owner) {
        model      = new ViewModelProvider(owner).get(MainViewModel.class);
        isAttached = true;
    }

    public void d(@NonNull String message) {
        if (!isAttached) return;
        add(new Log(highlightNumbers(message)));
    }

    private void add(@NonNull Log log) {
        if (logClass == LogClass.BUILD) {
            ArrayList<Log> currentList = model
                .getBUILDLogs()
                .getValue();
            if (currentList == null) currentList = new ArrayList<>();

            currentList.add(log);
            model
                .getBUILDLogs()
                .postValue(currentList);
        } else if (logClass == LogClass.IDE) {
            ArrayList<Log> currentList = model
                .getIDELogs()
                .getValue();
            if (currentList == null) currentList = new ArrayList<>();

            currentList.add(log);
            model
                .getIDELogs()
                .postValue(currentList);
        }
    }

    @NonNull
    private SpannableString highlightNumbers(String message) {
        SpannableString spannableMessage = new SpannableString(message);
        Pattern pattern = Pattern.compile("\\b\\d+\\b"); // Regular expression to match numbers
        Matcher matcher = pattern.matcher(message);
        while (matcher.find()) {
            spannableMessage.setSpan(new ForegroundColorSpan(0xFF00FF00), matcher.start(),
                matcher.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return spannableMessage;
    }

    public void d(String tag, String message) {
        if (!isAttached) return;
        add(new Log(formatDate(), tag, getLogLevel(LogLevel.DEBUG), highlightNumbers(message)));
    }

    @NonNull
    private SpannableString formatDate() {
        SpannableString spannableDate = new SpannableString(getTime());
        spannableDate.setSpan(new ForegroundColorSpan(0xFF1B5E20), 0, spannableDate.length(),
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannableDate;
    }

    public static String getTime() {
        var formatter = new FormatDateUseCase(User.newInstance(Constants.IDE_LOGS_DATE_FORMAT));
        return formatter.format(Wizard.getTime());
    }

    public String getLogLevel(LogLevel logLevel) {
        return LogLevel.getLevel(logLevel);
    }

    public void e(String tag, String message) {
        if (!isAttached) return;
        add(new Log(formatDate(), tag, highlightSpan(getLogLevel(LogLevel.ERROR), 0xffff0000),
            message));
    }

    @NonNull
    private SpannableString highlightSpan(@NonNull String message, int color) {
        SpannableString spannableMessage = new SpannableString(message);
        spannableMessage.setSpan(new ForegroundColorSpan(color), 0, message.length(),
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannableMessage;
    }

    public void i(String tag, String message) {
        if (!isAttached) return;
        add(new Log(formatDate(), tag, highlightSpan(getLogLevel(LogLevel.INFO), 0xFF0D47A1),
            highlightNumbers(message)));
    }

    public void w(String tag, String message) {
        if (!isAttached) return;
        add(new Log(formatDate(), tag, highlightSpan(getLogLevel(LogLevel.WARN), 0xffff7043),
            message));
    }

    public void clear() {
        if (logClass == LogClass.BUILD) {
            model
                .getBUILDLogs()
                .setValue(new ArrayList<>());
        } else if (logClass == LogClass.IDE) {
            model
                .getIDELogs()
                .setValue(new ArrayList<>());
        }
    }

    public enum LogClass {
        BUILD,
        IDE
    }
}
