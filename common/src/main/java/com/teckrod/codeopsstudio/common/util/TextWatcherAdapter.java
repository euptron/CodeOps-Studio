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
 
   package com.eup.codeopsstudio.common.util;

import android.text.Editable;
import android.text.TextWatcher;
import androidx.annotation.NonNull;

/**
 * Base class for scenarios where user wants to implement only one method of {@link TextWatcher}.
 */
public class TextWatcherAdapter implements TextWatcher {
  /**
   * This method is called to notify you that, within s, the count characters beginning at start are
   * about to be replaced by new text with length after. It is an error to attempt to make changes
   * to s from this callback.
   */
  @Override
  public void beforeTextChanged(@NonNull CharSequence s, int start, int count, int after) {}

  /**
   * This method is called to notify you that, within s, the count characters beginning at start
   * have just replaced old text that had length before. It is an error to attempt to make changes
   * to s from this callback.
   */
  @Override
  public void onTextChanged(@NonNull CharSequence s, int start, int before, int count) {}

  /**
   * This method is called to notify you that, somewhere within s, the text has been changed. It is
   * legitimate to make further changes to s from this callback, but be careful not to get yourself
   * into an infinite loop, because any changes you make will cause this method to be called again
   * recursively. (You are not told where the change took place because other afterTextChanged()
   * methods may already have made other changes and invalidated the offsets. But if you need to
   * know here, you can use {@link Spannable#setSpan} in {@link #onTextChanged} to mark your place
   * and then look up from here where the span ended up.
   */
  @Override
  public void afterTextChanged(@NonNull Editable s) {}
}
