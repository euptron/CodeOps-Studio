/*************************************************************************
 * This file is part of CodeOps Studio.
 * CodeOps Studio - code anywhere anytime
 * https://github.com/euptron/CodeOps-Studio
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
 
   package com.eup.codeopsstudio.ui.custom.preference;

import static com.eup.codeopsstudio.common.Constants.SharedPreferenceKeys;

import android.content.Context;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.eup.codeopsstudio.res.R;
import com.eup.codeopsstudio.res.databinding.LayoutDialogTextInputBinding;
import com.eup.codeopsstudio.common.util.PreferencesUtils;

public class DialogEditTextPreference extends Preference {

  public DialogEditTextPreference(Context context) {
    super(context);
  }

  public DialogEditTextPreference(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public DialogEditTextPreference(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

  @Override
  protected void onClick() {
    super.onClick();
    LayoutDialogTextInputBinding binding =
        LayoutDialogTextInputBinding.inflate(LayoutInflater.from((AppCompatActivity) getContext()));
    binding.tilName.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
    new MaterialAlertDialogBuilder((AppCompatActivity) getContext())
        .setTitle(R.string.pref_editor_code_editor_summ_cursor_blnk_dialog_title)
        .setMessage(R.string.pref_editor_code_editor_summ_cursor_blnk_dialog_msg)
        .setPositiveButton(
            android.R.string.ok,
            (d, w) -> {
              var cursorBlinkPeriod =
                  Integer.parseInt(binding.tilName.getEditText().getEditableText().toString());
              persistInt(cursorBlinkPeriod);
              notifyChanged();
            })
        .setNegativeButton(android.R.string.cancel, (d, w) -> d.dismiss())
        .setNeutralButton(R.string.reset, (d, w) -> resetCursorBlinkPeriod())
        .setView(binding.getRoot())
        .setCancelable(false)
        .show();
    binding.tilName.getEditText().setText(String.valueOf(getPersistedInt(500)));
  }

  private void resetCursorBlinkPeriod() {
    var pref = PreferencesUtils.getDefaultPreferences();
    pref.edit().putInt(SharedPreferenceKeys.KEY_CODE_EDITOR_CURSOR_BLINK_PERIOD, 500).apply();
  }

  @Override
  protected int getPersistedInt(int fallback) {
    return PreferencesUtils.getCursorBlinkPeriod(fallback);
  }

  @Override
  protected boolean persistInt(int cursorBlinkPeriod) {
    var pref = PreferencesUtils.getDefaultPreferences();
    pref.edit()
        .putInt(SharedPreferenceKeys.KEY_CODE_EDITOR_CURSOR_BLINK_PERIOD, cursorBlinkPeriod)
        .apply();
    return true;
  }
}
