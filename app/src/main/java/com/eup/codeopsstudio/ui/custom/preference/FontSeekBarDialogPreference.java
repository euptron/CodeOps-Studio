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

package com.eup.codeopsstudio.ui.custom.preference;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;

import com.eup.codeopsstudio.R;
import com.eup.codeopsstudio.common.Constants;
import com.eup.codeopsstudio.common.util.PreferencesUtils;
import com.eup.codeopsstudio.databinding.LayoutMaterialSliderBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/**
 * @author Etido Peter
 */
public class FontSeekBarDialogPreference extends Preference {

    private static final float DEFAULT_FONT_SIZE = 14.0f;

    public FontSeekBarDialogPreference(@NonNull Context context, @Nullable AttributeSet attrs,
        int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public FontSeekBarDialogPreference(@NonNull Context context, @Nullable AttributeSet attrs,
        int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public FontSeekBarDialogPreference(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public FontSeekBarDialogPreference(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onClick() {
        super.onClick();
        var binding = LayoutMaterialSliderBinding.inflate(LayoutInflater.from(getContext()));
        binding.slider.setValueFrom(6.0f);
        binding.slider.setValueTo(32.0f);
        binding.slider.setValue(getPersistedFloat(DEFAULT_FONT_SIZE));
        binding.slider.setStepSize(1.0f);

        new MaterialAlertDialogBuilder(getContext())
            .setTitle(R.string.pref_editor_code_editor_title_font_size)
            .setPositiveButton(R.string.ok, (d, w) -> {
                var codeEditorFontSize = binding.slider.getValue();
                persistFloat(codeEditorFontSize);
                notifyChanged();
            }).setNegativeButton(R.string.cancel, (d, w) -> d.dismiss())
            .setNeutralButton(R.string.reset, (d, w) -> resetFontSize()).setView(binding.getRoot())
            .setCancelable(false).show();
    }

    @Override
    protected boolean persistFloat(float fontSize) {
        if ((fontSize >= 6) && (fontSize <= 32)) {
            var pref = PreferencesUtils.getDefaultPreferences();
            var editor = pref.edit();
            editor.putFloat(Constants.SharedPreferenceKeys.KEY_CODE_EDITOR_FONT_SIZE, fontSize);
            editor.apply();
            return true;
        }
        return false;
    }

    @Override
    protected float getPersistedFloat(float fallbackFontSize) {
        return PreferencesUtils.getCodeEditorFontSize(fallbackFontSize);
    }

    private void resetFontSize() {
        var pref = PreferencesUtils.getDefaultPreferences();
        pref.edit().putFloat(Constants.SharedPreferenceKeys.KEY_CODE_EDITOR_FONT_SIZE, 14).apply();
    }
}