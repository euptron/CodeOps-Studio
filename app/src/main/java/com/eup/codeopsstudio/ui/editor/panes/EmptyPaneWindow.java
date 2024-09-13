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
 
   package com.eup.codeopsstudio.ui.editor.panes;

import android.content.Context;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;
import com.eup.codeopsstudio.databinding.EmptyPaneWindowBinding;
import com.eup.codeopsstudio.pane.Pane;
import com.eup.codeopsstudio.res.R;
import com.eup.codeopsstudio.viewmodel.MainViewModel;

public class EmptyPaneWindow extends Pane {

  private MainViewModel mViewModel;
  private LifecycleOwner lifecycleOwner;
  private EmptyPaneWindowBinding binding;
  private SpannableString styledString;

  public EmptyPaneWindow(
      MainViewModel viewModel, LifecycleOwner lifecycleOwner, Context context, String title) {
    super(context, title);
    this.lifecycleOwner = lifecycleOwner;
    this.mViewModel = viewModel;
  }

  @Override
  public View onCreateView() {
    binding = EmptyPaneWindowBinding.inflate(LayoutInflater.from(getContext()));
    return binding.getRoot();
  }

  @Override
  public void onViewCreated(View view) {
    super.onViewCreated(view);
    mViewModel = new ViewModelProvider((ViewModelStoreOwner) getContext()).get(MainViewModel.class);
    mViewModel
        .getDrawerInstance()
        .observe(
            lifecycleOwner,
            isDrawerLayout -> {
              if (isDrawerLayout) {
                styledString =
                    new SpannableString(
                        getContext()
                                .getString(
                                    R.string.open_file_tree,
                                    getContext().getString(R.string.explorer)) // index 18-30
                            + "\n"
                            + getContext()
                                .getString(
                                    R.string.open_build_actions,
                                    getContext()
                                        .getString(R.string.build_actions))); // index 25 - 37

                ClickableSpan opentreeSpan =
                    new ClickableSpan() {
                      @Override
                      public void onClick(View widget) {
                        mViewModel.setDrawerState(true);
                      }
                    };
                ClickableSpan openactionSpan =
                    new ClickableSpan() {
                      @Override
                      public void onClick(View widget) {
                        mViewModel.setBottomSheetExpanded(true);
                      }
                    };

                // Styled open primary side bar
                styledString.setSpan(opentreeSpan, 16, 24, 0);
                // Styled open build actions
                styledString.setSpan(openactionSpan, 45, 52, 0);
                // the url and clickable styles.
                binding.prompt.setMovementMethod(LinkMovementMethod.getInstance());
                binding.prompt.setSelectAllOnFocus(false);
                binding.prompt.setFocusable(false);
                binding.prompt.setText(styledString);
              } else {
                styledString =
                    new SpannableString(
                        getContext()
                            .getString(
                                R.string.open_build_actions,
                                getContext().getString(R.string.build_actions)));
                ClickableSpan openactionSpan =
                    new ClickableSpan() {

                      @Override
                      public void onClick(View widget) {
                        mViewModel.setBottomSheetExpanded(true);
                      }
                    };
                // Styled open build actions
                styledString.setSpan(openactionSpan, 13, 26, 0);
                //  the url and clickable styles.
                binding.prompt.setMovementMethod(LinkMovementMethod.getInstance());
                binding.prompt.setSelectAllOnFocus(false);
                binding.prompt.setFocusable(false);
                binding.prompt.setText(styledString);
              }
            });
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    binding = null;
  }
}
