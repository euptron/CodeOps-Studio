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
import android.text.InputType;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;

import com.eup.codeopsstudio.R;
import com.eup.codeopsstudio.common.Constants;
import com.eup.codeopsstudio.common.ILog;
import com.eup.codeopsstudio.common.util.PreferencesUtils;
import com.eup.codeopsstudio.databinding.LayoutDialogTextInputBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.function.Consumer;

/**
 * @author Etido Peter
 */
public class DialogEditTextPreference extends Preference {

    private static final String TAG = DialogEditTextPreference.class.getSimpleName();

    public DialogEditTextPreference(@NonNull Context context, @Nullable AttributeSet attrs,
        int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public DialogEditTextPreference(@NonNull Context context, @Nullable AttributeSet attrs,
        int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public DialogEditTextPreference(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public DialogEditTextPreference(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onClick() {
        super.onClick();
        LayoutDialogTextInputBinding binding =
            LayoutDialogTextInputBinding.inflate(LayoutInflater.from(getContext()));

        applyTo(binding.tilName.getEditText(),
            editText -> editText.setInputType(InputType.TYPE_CLASS_NUMBER));
        new MaterialAlertDialogBuilder(getContext())
            .setTitle(R.string.pref_editor_code_editor_summ_cursor_blnk_dialog_title)
            .setMessage(R.string.pref_editor_code_editor_summ_cursor_blnk_dialog_msg)
            .setPositiveButton(android.R.string.ok, (d, w) -> {
                int cursorBlinkPeriod = 0;
                if (binding.tilName.getEditText() != null) {
                    cursorBlinkPeriod = Integer.parseInt(binding.tilName
                        .getEditText()
                        .getEditableText()
                        .toString());
                }
                persistInt(cursorBlinkPeriod);
                notifyChanged();
            })
            .setNegativeButton(android.R.string.cancel, (d, w) -> d.dismiss())
            .setNeutralButton(R.string.reset, (d, w) -> resetCursorBlinkPeriod())
            .setView(binding.getRoot())
            .setCancelable(false)
            .show();
        applyTo(binding.tilName.getEditText(),
            editText -> editText.setText(String.valueOf(getPersistedInt(500))));
    }

    private void resetCursorBlinkPeriod() {
        var pref = PreferencesUtils.getDefaultPreferences();
        pref
            .edit()
            .putInt(Constants.SharedPreferenceKeys.KEY_CODE_EDITOR_CURSOR_BLINK_PERIOD, 500)
            .apply();
    }

    @Override
    protected boolean persistInt(int cursorBlinkPeriod) {
        var pref = PreferencesUtils.getDefaultPreferences();
        pref
            .edit()
            .putInt(Constants.SharedPreferenceKeys.KEY_CODE_EDITOR_CURSOR_BLINK_PERIOD,
                cursorBlinkPeriod)
            .apply();
        return true;
    }

    @Override
    protected int getPersistedInt(int fallback) {
        return PreferencesUtils.getCursorBlinkPeriod(fallback);
    }

    private void applyTo(@Nullable EditText editText, @NonNull Consumer<EditText> application) {
        if (editText == null) {
            ILog.debug(TAG, "EditText = null");
            return;
        }
        application.accept(editText);
    }
}
