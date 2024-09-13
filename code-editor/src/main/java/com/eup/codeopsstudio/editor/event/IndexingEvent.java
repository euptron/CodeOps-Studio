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
 
    
  
  
 package com.eup.codeopsstudio.editor.event;

import androidx.annotation.NonNull;
import io.github.rosemoe.sora.event.Event;
import io.github.rosemoe.sora.widget.CodeEditor;

/**
 * Report editor indexing
 *
 * @author EUP
 */
public class IndexingEvent extends Event {

  private final boolean isIndexing;

  public IndexingEvent(@NonNull CodeEditor editor, boolean indexing) {
    super(editor);
    isIndexing = indexing;
  }

  public boolean isIndexing() {
    return isIndexing;
  }
}
