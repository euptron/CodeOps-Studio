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
 * questions or need additional information. Email: etido.up@gmail.com
 */

package com.eup.codeopsstudio.util.manager;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;

import com.eup.codeopsstudio.common.Constants;
import com.eup.codeopsstudio.common.util.PreferencesUtils;
import com.google.android.material.color.DynamicColors;
import com.google.android.material.color.DynamicColorsOptions;

/**
 * This class is used to manage the theme of the application.
 *
 * @author Etido Peter
 */
public class ThemeManager {

    public static final String KEY_THEME = Constants.SharedPreferenceKeys.KEY_APP_THEME;
    public static final String KEY_DYNAMIC_COLORS =
        Constants.SharedPreferenceKeys.KEY_DYNAMIC_COLOURS;

    private ThemeManager() {
        // This class is not meant to be instantiated.
    }

    public static void applyTheme(@NonNull Application application) {
        int mode = PreferencesUtils.getCurrentTheme();
        AppCompatDelegate.setDefaultNightMode(mode);

        final DynamicColors.Precondition precondition = (activity, theme) ->
            PreferencesUtils.useDynamicColors() && DynamicColors.isDynamicColorAvailable();
        DynamicColors.applyToActivitiesIfAvailable(application, new DynamicColorsOptions.Builder()
            .setPrecondition(precondition)
            .build());
    }
}