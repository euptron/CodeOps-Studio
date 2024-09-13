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
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.eup.codeopsstudio.common.Constants;
import com.eup.codeopsstudio.res.R;
import com.eup.codeopsstudio.util.EncodingDetector;
import com.eup.codeopsstudio.common.util.PreferencesUtils;
import java.util.List;

public class DefaultFileEncodingDialogPreference extends Preference {

  private int selectedEncodingIndex;

  public DefaultFileEncodingDialogPreference(Context context) {
    super(context);
  }

  public DefaultFileEncodingDialogPreference(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public DefaultFileEncodingDialogPreference(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

  @Override
  protected void onClick() {
    super.onClick();

    List<String> encodings = EncodingDetector.getSupportedEncodings();

    selectedEncodingIndex = encodings.indexOf(getPersistedString(Constants.FALLBACK_FILE_ENCODING));

    new MaterialAlertDialogBuilder((AppCompatActivity) getContext())
        .setTitle(R.string.pref_editor_file_title_default_file_encoding_dialog_title)
        .setSingleChoiceItems(
            encodings.toArray(new String[0]),
            selectedEncodingIndex,
            (dialog, which) -> {
              persistString(encodings.get(which));
              notifyChanged();
              //dialog.cancel();
              dialog.dismiss();
            })
        .setNegativeButton(R.string.close, (d, w) -> d.dismiss())
        .setCancelable(false)
        .show();
  }

  @Override
  protected boolean persistString(String encoding) {
    var pref = PreferencesUtils.getDefaultPreferences();
    var editor = pref.edit();
    editor.putString(SharedPreferenceKeys.KEY_CODE_EDITOR_DEFAULT_FILE_ENCODING, encoding);
    return editor.commit();
  }

  @Override
  protected String getPersistedString(String fallbackEncoding) {
    return PreferencesUtils.getDefaultFileEncoding(fallbackEncoding);
  }

  private void resetEncoding() {
    PreferencesUtils.setDefaultFileEncoding(Constants.FALLBACK_FILE_ENCODING);
  }
}
