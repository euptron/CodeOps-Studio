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

package com.eup.codeopsstudio.palette;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.eup.codeopsstudio.editor.ContextualCodeEditor;
import com.eup.codeopsstudio.palette.registry.PaletteRegistry;
import com.eup.codeopsstudio.util.BaseUtil;
import com.google.android.material.textfield.TextInputEditText;
import java.util.List;
import com.eup.codeopsstudio.R;
import java.lang.ref.WeakReference;
import com.eup.codeopsstudio.ui.editor.code.CodeEditorPane;

/**
 * A modal dialog fragment that serves as the CodeOps Studio command palette.
 *
 * <p>This dialog serves as the central entry point for user interaction, supporting various
 * operational modes including:
 *
 * <ul>
 *   <li><b>Global Mode:</b> General file search and recent commands (Default).
 *   <li><b>Scoped Mode:</b> Context-specific operations like Syntax Picking ("@syntax").
 *   <li><b>Explicit Mode:</b> Direct command execution (e.g., Go to Line ":").
 * </ul>
 *
 * <p>It delegates search logic to the {@link PaletteRegistry} and handles the UI state, including
 * the search bar, list rendering, and keyboard interactions.
 *
 * @author Etido Peter
 */
public class CommandPaletteDialog extends DialogFragment implements View.OnKeyListener {

  private static final int DIALOG_TOP_MARGIN_DP = 48;
  private static final String ARG_HINT = "arg_hint";
  private static final String ARG_PREFIX = "arg_prefix";
  private static final String ARG_PREFIX_VISIBLE = "arg_prefix_visible";

  private PaletteAdapter adapter;
  private PaletteRegistry registry;
  private RecyclerView recyclerView;
  private TextInputEditText etSearch;
  private String internalPrefix = "";
  private boolean isPrefixVisible = true;
  private WeakReference<CodeEditorPane> codeEditorPaneRef;

  public static CommandPaletteDialog newGlobalInstance(CodeEditorPane cep) {
    CommandPaletteDialog fragment = new CommandPaletteDialog();
    fragment.setCodeEditorPane(cep);
    return fragment;
  }

  public static CommandPaletteDialog newExplicitInstance(CodeEditorPane cep, String prefix) {
    return createInstance(cep, prefix, true, null);
  }

  public static CommandPaletteDialog newScopedInstance(
      CodeEditorPane cep, String hiddenPrefix, String customHint) {
    return createInstance(cep, hiddenPrefix, false, customHint);
  }

  private static CommandPaletteDialog createInstance(
      CodeEditorPane cep, String prefix, boolean visible, String hint) {
    CommandPaletteDialog fragment = new CommandPaletteDialog();
    Bundle args = new Bundle();
    args.putString(ARG_PREFIX, prefix);
    args.putBoolean(ARG_PREFIX_VISIBLE, visible);
    if (hint != null) args.putString(ARG_HINT, hint);
    fragment.setArguments(args);
    fragment.setCodeEditorPane(cep);
    return fragment;
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (getArguments() != null) {
      internalPrefix = getArguments().getString(ARG_PREFIX, "");
      isPrefixVisible = getArguments().getBoolean(ARG_PREFIX_VISIBLE, true);
    }
  }

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.dialog_command_palette, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    etSearch = view.findViewById(R.id.etSearch);
    recyclerView = view.findViewById(R.id.recyclerView);

    registry = new PaletteRegistry(requireContext(), etSearch, getCodeEditorPane());
    configRecyclerView();
  }

  @Override
  public void onStart() {
    super.onStart();
    Dialog dialog = getDialog();
    if (dialog != null && dialog.getWindow() != null) {
      final float screenWidthRatio = 0.90f;
      final float screenHeightRatio = 0.75f;

      int displayWidth = (int) (getResources().getDisplayMetrics().widthPixels * screenWidthRatio);
      int displayHeight =
          (int) (getResources().getDisplayMetrics().heightPixels * screenHeightRatio);

      Window window = dialog.getWindow();
      window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
      window.setLayout(displayWidth, ViewGroup.LayoutParams.WRAP_CONTENT);

      WindowManager.LayoutParams params = window.getAttributes();
      params.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;

      ViewGroup.LayoutParams layoutParams = recyclerView.getLayoutParams();

      if (layoutParams instanceof ConstraintLayout.LayoutParams) {
        ConstraintLayout.LayoutParams lp = (ConstraintLayout.LayoutParams) layoutParams;
        lp.height = 0;
        lp.matchConstraintDefaultHeight = ConstraintLayout.LayoutParams.MATCH_CONSTRAINT_WRAP;
        lp.matchConstraintMaxHeight = displayHeight;
        recyclerView.setLayoutParams(lp);
      }

      ViewCompat.setOnApplyWindowInsetsListener(
          window.getDecorView(),
          (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            params.y = systemBars.top + BaseUtil.dpToPx(DIALOG_TOP_MARGIN_DP);
            window.setAttributes(params);
            return insets;
          });
    }
  }

  @Override
  public boolean onKey(View v, int keyCode, KeyEvent event) {
    if (event.getAction() == KeyEvent.ACTION_DOWN) {
      switch (keyCode) {
        case KeyEvent.KEYCODE_ENTER:
        case KeyEvent.KEYCODE_NUMPAD_ENTER:
          handleEnterPress(false);
          return true;
        case KeyEvent.KEYCODE_DPAD_UP:
          handleArrowUp();
          return true;
        case KeyEvent.KEYCODE_DPAD_DOWN:
          handleArrowDown();
          return true;
      }
    }
    return false;
  }

  public void setCodeEditorPane(CodeEditorPane cep) {
    this.codeEditorPaneRef = new WeakReference<>(cep);
  }

  private CodeEditorPane getCodeEditorPane() {
    return (codeEditorPaneRef != null) ? codeEditorPaneRef.get() : null;
  }

  private void configRecyclerView() {
    configRecyclerView(true);
  }

  private void configRecyclerView(boolean killAnimations) {
    LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
    recyclerView.setLayoutManager(layoutManager);
    adapter = new PaletteAdapter();
    recyclerView.setAdapter(adapter);

    if (killAnimations) {
      recyclerView.setItemAnimator(null);
    }

    configSearch();
  }

  private void configSearch() {
    if (!internalPrefix.isEmpty() && !isPrefixVisible) {
      String hint = getArguments().getString(ARG_HINT, "Type to search...");
      if (etSearch != null) etSearch.setHint(hint);
      else etSearch.setHint(hint);
    }

    if (!internalPrefix.isEmpty() && isPrefixVisible) {
      etSearch.setText(internalPrefix);
      etSearch.setSelection(etSearch.getText().length());
    }

    etSearch.addTextChangedListener(
        new TextWatcher() {
          @Override
          public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

          @Override
          public void afterTextChanged(Editable s) {}

          @Override
          public void onTextChanged(CharSequence s, int start, int before, int count) {
            String userInput = s.toString();
            String queryForEngine;

            if (!isPrefixVisible) {
              queryForEngine = internalPrefix + userInput;
            } else {
              queryForEngine = userInput;
            }

            performSearch(queryForEngine);
          }
        });

    if (!isPrefixVisible) {
      performSearch(internalPrefix);
    } else if (etSearch.getText().length() > 0) {
      performSearch(etSearch.getText().toString());
    } else {
      // Load initial state (Recents)
      performSearch("");
    }

    etSearch.setOnKeyListener(this);
    etSearch.requestFocus();
    showKeyboard();
    if (adapter != null) adapter.setSelectedPosition(0);
  }

  private void showKeyboard() {
    if (getDialog() != null && getDialog().getWindow() != null) {
      getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }
  }

  private void performSearch(String query) {
    List<PaletteItem> results = registry.search(query);

    adapter.submitList(
        results,
        () -> {
          // reset selection to TOP
          if (adapter != null) {
            adapter.setSelectedPosition(0);
            recyclerView.scrollToPosition(0);
          }
        });
  }

  private void handleArrowDown() {
    int currentPos = adapter.getSelectedPosition();
    int totalCount = adapter.getItemCount();

    if (currentPos < totalCount - 1) {
      int newPos = currentPos + 1;
      // Skip Headers going down
      if (adapter.getItemViewType(newPos) == PaletteItem.TYPE_HEADER && newPos < totalCount - 1) {
        newPos++;
      }

      adapter.setSelectedPosition(newPos);
      recyclerView.scrollToPosition(newPos);
    }
  }

  private void handleArrowUp() {
    int currentPos = adapter.getSelectedPosition();

    if (currentPos > 0) {
      int newPos = currentPos - 1;
      // Skip Headers going up
      if (adapter.getItemViewType(newPos) == PaletteItem.TYPE_HEADER && newPos > 0) {
        newPos--;
      }

      adapter.setSelectedPosition(newPos);
      recyclerView.scrollToPosition(newPos);
    }
  }

  private void handleEnterPress(boolean dismiss) {
    int currentPos = adapter.getSelectedPosition();
    if (currentPos >= 0 && currentPos < adapter.getItemCount()) {
      PaletteItem item = adapter.getCurrentList().get(currentPos);

      if (item.getType() == PaletteItem.TYPE_COMMAND && item.getAction() != null) {
        item.getAction().run();
        if (dismiss) dismiss();
      }
    }
  }
}
