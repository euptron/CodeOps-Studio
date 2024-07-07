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
 
   package com.eup.codeopsstudio.editor.langs.completion;

import android.graphics.drawable.Drawable;
import androidx.annotation.Nullable;
import io.github.rosemoe.sora.lang.completion.SimpleCompletionItem;

/**
 * ContextualCompletionItem represents a replace action for auto-completion. {@code prefixLength} is
 * the length of prefix (text length you want to replace before the auto-completion position).
 * {@code commitText} is the text you want to replace the original text.
 *
 * <p>Note that you must make sure the start position of replacement is on the same line as
 * auto-completion's required position.
 *
 * @author EUP
 * @see {@code SimpleCompletionItem}
 */
public class ContextualCompletionItem extends SimpleCompletionItem {

  /** Text to display as comphrensive description in adapter */
  @Nullable public CharSequence compDescription;

  public ContextualCompletionItem(int prefixLength, String commitText) {
    this(commitText, prefixLength, commitText);
  }

  public ContextualCompletionItem(CharSequence label, int prefixLength, String commitText) {
    this(label, null, prefixLength, commitText);
  }

  public ContextualCompletionItem(
      CharSequence label, CharSequence desc, int prefixLength, String commitText) {
    this(label, desc, null, prefixLength, commitText);
  }

  public ContextualCompletionItem(
      CharSequence label,
      CharSequence desc,
      CharSequence compDes,
      int prefixLength,
      String commitText) {
    this(label, desc, null, compDes, prefixLength, commitText);
  }

  public ContextualCompletionItem(
      CharSequence label,
      CharSequence desc,
      Drawable icon,
      CharSequence compDes,
      int prefixLength,
      String commitText) {
    super(label, desc, icon, prefixLength, commitText);
    this.compDescription = compDes;
  }

  public ContextualCompletionItem compDes(CharSequence desc) {
    this.compDescription = desc;
    return this;
  }
}
