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
 
package com.eup.codeopsstudio.ui.editor.code.breadcrumb;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.eup.codeopsstudio.ui.editor.code.breadcrumb.adapter.BreadCrumbAdapter;
import com.eup.codeopsstudio.ui.editor.code.breadcrumb.model.BreadCrumb;
import io.github.rosemoe.sora.widget.schemes.EditorColorScheme;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BreadCrumbPanel extends RecyclerView {

  private BreadCrumbAdapter adapter;
  private boolean visible;
  private List<BreadCrumb> breadCrumbs = new ArrayList<>();
  public static final String STORAGE_EMULATED =
      File.separator + "storage" + File.separator + "emulated";
  public static final String STORAGE_EMULATED_0 = STORAGE_EMULATED + File.separator + "0";
  
  public BreadCrumbPanel(Context context) {
    this(context, null);
  }

  public BreadCrumbPanel(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public BreadCrumbPanel(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    init(context);
  }

  private void init(Context context) {
    setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
    adapter = new BreadCrumbAdapter();
    setAdapter(adapter);
    visible = true;
  }

  public BreadCrumbAdapter getAdapter() {
    return this.adapter;
  }

  public void setFile(File file) {
    if (visible && file != null) {
      breadCrumbs.clear();

      while (file != null) {
        if (file.getPath().equals(STORAGE_EMULATED)) {
          break;
        }
        
        var breadCrumb = BreadCrumb.fileToCrumb(file);
        
        if (breadCrumb != null) {
          if (breadCrumb.getFilePath().equals(STORAGE_EMULATED_0)) {
            breadCrumb.setName("root");
          }
          breadCrumbs.add(breadCrumb);
          file = file.getParentFile();
        }
      }

      Collections.reverse(breadCrumbs);
      adapter.notifyDataSetChanged();
      adapter.submitList(breadCrumbs);
      scrollToPosition(adapter.getItemCount() - 1);
    }
  }
  
  public void setVisible(boolean enabled) {
    setVisibility(enabled ? View.VISIBLE : View.GONE);
    this.visible = enabled;
  }
}
