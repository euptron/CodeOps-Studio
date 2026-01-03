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

package com.eup.codeopsstudio.logger.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class LogModel {

    private static final List<UUID> generatedIds = new ArrayList<>();
    private final int mIcon;
    private final CharSequence mTag;
    private final CharSequence mMessage;
    private final CharSequence mDateFormat;
    private final CharSequence mLogLevel;
    private final UUID id;

    /**
     * Basic log
     *
     * @param message log message
     */
    public LogModel(CharSequence message) {
        this(null, 0, null, null, message);
    }

    /**
     * Diagnostics log
     *
     * @param icon    diagnostics icon resource
     * @param message diagnostics message
     */
    public LogModel(int icon, CharSequence message) {
        this(null, icon, null, null, message);
    }

    /**
     * Normal log
     *
     * @param tag     log tag
     * @param level   the log level
     * @param message log message
     */
    public LogModel(CharSequence tag, CharSequence level, CharSequence message) {
        this(null, 0, tag, level, message);
    }

    /**
     * Debug log
     *
     * @param date    log date
     * @param tag     log tag
     * @param level   the log level
     * @param message log message
     */
    public LogModel(CharSequence date, CharSequence tag, CharSequence level, CharSequence message) {
        this(date, 0, tag, level, message);
    }

    /**
     * Verbose log
     *
     * @param date    log date
     * @param icon    log icon resource
     * @param tag     log tag
     * @param level   the log level
     * @param message log message
     */
    public LogModel(CharSequence date, int icon, CharSequence tag, CharSequence level,
        CharSequence message) {
        mDateFormat = date;
        mIcon       = icon;
        mTag        = tag;
        mLogLevel   = level;
        mMessage    = message;
        this.id     = generateUUID();
    }

    @Override
    public int hashCode() {
        return Objects.hash(mMessage, mTag, mDateFormat, mLogLevel);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LogModel log = (LogModel) o;
        return Objects.equals(mMessage, log.getMessage()) && Objects.equals(mTag, log.getTag())
            && Objects.equals(mDateFormat, log.getDateFormat())
            && Objects.equals(mLogLevel, log.getLevel());
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

    public int getIcon() {
        return mIcon;
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
}
