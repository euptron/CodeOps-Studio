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
import android.util.AttributeSet;
import android.view.LayoutInflater;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.eup.codeopsstudio.res.R;
import com.eup.codeopsstudio.res.databinding.LayoutMaterialSliderBinding;
import com.eup.codeopsstudio.common.util.PreferencesUtils;

public class TabSizeSeekBarDialogPreference extends Preference {

  private static final int DEFAULT_TAB_SIZE = 4;

  public TabSizeSeekBarDialogPreference(Context context) {
    super(context);
  }

  public TabSizeSeekBarDialogPreference(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public TabSizeSeekBarDialogPreference(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

  @Override
  protected void onClick() {
    super.onClick();
    var binding =
        LayoutMaterialSliderBinding.inflate(LayoutInflater.from((AppCompatActivity) getContext()));
    binding.slider.setValueFrom(1.0f);
    binding.slider.setValueTo(12.0f);
    binding.slider.setValue(getPersistedInt(DEFAULT_TAB_SIZE));
    binding.slider.setStepSize(1.0f);

    new MaterialAlertDialogBuilder((AppCompatActivity) getContext())
        .setTitle(R.string.pref_editor_code_editor_title_tab_size)
        .setPositiveButton(
            R.string.ok,
            (d, w) -> {
              int codeEditorTabSize = (int) binding.slider.getValue();
              persistInt(codeEditorTabSize);
              notifyChanged();
            })
        .setNegativeButton(R.string.cancel, (d, w) -> d.dismiss())
        .setNeutralButton(R.string.reset, (d, w) -> resetTabSize())
        .setView(binding.getRoot())
        .setCancelable(false)
        .show();
  }

  @Override
  protected int getPersistedInt(int size) {
    return PreferencesUtils.getCodeEditorTabSize(size);
  }

  @Override
  protected boolean persistInt(int size) {
    if ((size >= 1) && (size <= 12)) {
      var pref = PreferencesUtils.getDefaultPreferences();
      var editor = pref.edit();
      editor.putInt(SharedPreferenceKeys.KEY_CODE_EDITOR_TAB_SIZE, size);
      return editor.commit();
    }
    return false;
  }

  private void resetTabSize() {
    var pref = PreferencesUtils.getDefaultPreferences();
    pref.edit().putInt(SharedPreferenceKeys.KEY_CODE_EDITOR_TAB_SIZE, DEFAULT_TAB_SIZE).apply();
  }
}
