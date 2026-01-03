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

package com.eup.codeopsstudio.ui.custom.preference;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;

import com.eup.codeopsstudio.R;
import com.eup.codeopsstudio.common.Constants;
import com.eup.codeopsstudio.common.util.PreferencesUtils;
import com.eup.codeopsstudio.util.EncodingDetector;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

/**
 * @author Etido Peter
 */
public class DefaultFileEncodingDialogPreference extends Preference {

    public DefaultFileEncodingDialogPreference(@NonNull Context context,
        @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public DefaultFileEncodingDialogPreference(@NonNull Context context,
        @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public DefaultFileEncodingDialogPreference(@NonNull Context context,
        @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public DefaultFileEncodingDialogPreference(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onClick() {
        super.onClick();

        List<String> encodings = EncodingDetector.getSupportedEncodings();

        int selectedEncodingIndex =
            encodings.indexOf(getPersistedString(Constants.FALLBACK_FILE_ENCODING));

        new MaterialAlertDialogBuilder(getContext())
            .setTitle(R.string.pref_editor_file_title_default_file_encoding_dialog_title)
            .setSingleChoiceItems(encodings.toArray(new String[0]), selectedEncodingIndex,
                (dialog, which) -> {
                persistString(encodings.get(which));
                notifyChanged();
                // dialog.cancel();
                dialog.dismiss();
            }).setNegativeButton(R.string.close, (d, w) -> d.dismiss()).setCancelable(false).show();
    }

    @Override
    protected boolean persistString(String encoding) {
        var pref = PreferencesUtils.getDefaultPreferences();
        var editor = pref.edit();
        editor.putString(Constants.SharedPreferenceKeys.KEY_CODE_EDITOR_DEFAULT_FILE_ENCODING,
            encoding);
        editor.apply();
        return true;
    }

    @Override
    protected String getPersistedString(String fallbackEncoding) {
        return PreferencesUtils.getDefaultFileEncoding(fallbackEncoding);
    }
}
