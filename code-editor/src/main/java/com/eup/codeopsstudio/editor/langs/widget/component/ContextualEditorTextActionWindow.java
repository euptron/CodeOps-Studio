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

package com.eup.codeopsstudio.editor.langs.widget.component;

import android.annotation.SuppressLint;
import android.graphics.RectF;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.TooltipCompat;

import com.eup.codeopsstudio.editor.ContextualCodeEditor;
import com.eup.codeopsstudio.editor.R;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.elevation.SurfaceColors;

import io.github.rosemoe.sora.event.HandleStateChangeEvent;
import io.github.rosemoe.sora.event.InterceptTarget;
import io.github.rosemoe.sora.event.LongPressEvent;
import io.github.rosemoe.sora.event.ScrollEvent;
import io.github.rosemoe.sora.event.SelectionChangeEvent;
import io.github.rosemoe.sora.event.Unsubscribe;
import io.github.rosemoe.sora.text.Cursor;
import io.github.rosemoe.sora.widget.EditorTouchEventHandler;
import io.github.rosemoe.sora.widget.component.EditorTextActionWindow;

/**
 * This window will show when selecting text to present text actions.
 *
 * @author Etido Peter
 */
public class ContextualEditorTextActionWindow extends EditorTextActionWindow {

    private static final long DELAY = 200;
    private final ContextualCodeEditor editor; // was code editor
    private final ImageButton pasteBtn;
    private final ImageButton copyBtn;
    private final ImageButton cutBtn;
    private final ImageButton selectAllBtn;
    private final ImageButton longSelectBtn;
    private final ImageButton expandSelectionBtn;
    private final ImageButton formatBtn;
    private final View rootView;
    private final EditorTouchEventHandler handler;
    private long lastScroll;
    private int lastPosition;
    private int lastCause;

    private boolean enabled = true;
    private int windowCornerRadius = 4; // original 5 | 32 gives rounded effect

    /**
     * Create a panel for the given editor
     *
     * @param editor Target editor
     */
    public ContextualEditorTextActionWindow(ContextualCodeEditor editor) {
        super(editor);
        this.editor = editor;
        handler     = editor.getEventHandler();

        // Since popup window does provide decor view, we have to pass null to this method
        @SuppressLint("InflateParams") View root = LayoutInflater.from(editor.getContext())
                                                                 .inflate(R.layout.contextual_text_compose_panel, null);

        pasteBtn           = root.findViewById(R.id.panel_btn_paste);
        copyBtn            = root.findViewById(R.id.panel_btn_copy);
        cutBtn             = root.findViewById(R.id.panel_btn_cut);
        selectAllBtn       = root.findViewById(R.id.panel_btn_select_all);
        longSelectBtn      = root.findViewById(R.id.panel_btn_long_select);
        expandSelectionBtn = root.findViewById(R.id.panel_btn_expand_selection);
        formatBtn          = root.findViewById(R.id.panel_btn_format);

        pasteBtn.setOnClickListener(this);
        copyBtn.setOnClickListener(this);
        cutBtn.setOnClickListener(this);
        selectAllBtn.setOnClickListener(this);
        longSelectBtn.setOnClickListener(this);
        expandSelectionBtn.setOnClickListener(this);
        formatBtn.setOnClickListener(this);

        GradientDrawable gd = new GradientDrawable();
        gd.setCornerRadius(windowCornerRadius * editor.getDpUnit());
        gd.setColor(SurfaceColors.SURFACE_1.getColor(editor.getContext()));
        gd.setStroke(1, MaterialColors.getColor(editor.getContext(),
            com.google.android.material.R.attr.colorOutline, 0));
        root.setBackground(gd);
        setContentView(root);
        setSize(0, (int) (this.editor.getDpUnit() * 48));

        rootView = root;
        editor.subscribeEvent(SelectionChangeEvent.class, this);
        editor.subscribeEvent(ScrollEvent.class, ((event, unsubscribe) -> {
            long last = lastScroll;
            lastScroll = System.currentTimeMillis();
            if (lastScroll - last < DELAY && lastCause != SelectionChangeEvent.CAUSE_SEARCH) {
                runPostDisplay();
            }
        }));
        editor.subscribeEvent(HandleStateChangeEvent.class, ((event, unsubscribe) -> {
            if (event.isHeld()) {
                runPostDisplay();
            }
        }));
        editor.subscribeEvent(LongPressEvent.class, ((event, unsubscribe) -> {
            if (editor.getCursor().isSelected() && lastCause == SelectionChangeEvent.CAUSE_SEARCH) {
                int idx = event.getIndex();
                if (idx >= editor.getCursor().getLeft() && idx <= editor.getCursor().getRight()) {
                    lastCause = 0;
                    displayWindow();
                }
                event.intercept(InterceptTarget.TARGET_EDITOR);
            }
        }));
        editor.subscribeEvent(HandleStateChangeEvent.class, ((event, unsubscribe) -> {
            if (!event.getEditor().getCursor().isSelected()
                && event.getHandleType() == HandleStateChangeEvent.HANDLE_TYPE_INSERT
                && !event.isHeld()) {
                displayWindow();
                // Also, post to hide the window on handle disappearance
                editor.postDelayedInLifecycle(new Runnable() {
                    @Override
                    public void run() {
                        if (!editor.getEventHandler().shouldDrawInsertHandle() && !editor
                            .getCursor().isSelected()) {
                            dismiss();
                        } else if (!editor.getCursor().isSelected()) {
                            editor.postDelayedInLifecycle(this, 100);
                        }
                    }
                }, 100);
            }
        }));

        getPopup().setAnimationStyle(io.github.rosemoe.sora.R.style.text_action_popup_animation);
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (!enabled) {
            dismiss();
        }
    }

    /**
     * Get the view root of the panel.
     *
     * <p>Root view is {@link android.widget.LinearLayout} Inside is a {@link
     * android.widget.HorizontalScrollView}
     *
     * @see R.id#panel_root
     * @see R.id#panel_btn_select_all
     * @see R.id#panel_btn_copy
     * @see R.id#panel_btn_cut
     * @see R.id#panel_btn_paste
     */
    @Override
    public ViewGroup getView() {
        return (ViewGroup) getPopup().getContentView();
    }

    @Override
    public void onReceive(@NonNull SelectionChangeEvent event, @NonNull Unsubscribe unsubscribe) {
        if (handler.hasAnyHeldHandle()) {
            return;
        }
        lastCause = event.getCause();

        if (event.isSelected()) {
            // Always post show. See #193
            if (event.getCause() != SelectionChangeEvent.CAUSE_SEARCH) {
                editor.postInLifecycle(this::displayWindow);
            } else {
                dismiss();
            }
            lastPosition = -1;
        } else {
            boolean show = false;
            if (event.getCause() == SelectionChangeEvent.CAUSE_TAP
                && event.getLeft().index == lastPosition && !isShowing() && !editor.getText()
                                                                                   .isInBatchEdit()
                && editor.isEditable()) {
                editor.postInLifecycle(this::displayWindow);
                show = true;
            } else {
                dismiss();
            }
            if (event.getCause() == SelectionChangeEvent.CAUSE_TAP && !show) {
                lastPosition = event.getLeft().index;
            } else {
                lastPosition = -1;
            }
        }
    }

    @Override
    public void displayWindow() {
        updateButtonState();
        int top;
        Cursor cursor = editor.getCursor();
        if (cursor.isSelected()) {
            RectF leftRect = editor.getLeftHandleDescriptor().position;
            RectF rightRect = editor.getRightHandleDescriptor().position;
            int top1 = selectTopRect(leftRect);
            int top2 = selectTopRect(rightRect);
            top = Math.min(top1, top2);
        } else {
            top = selectTopRect(editor.getInsertHandleDescriptor().position);
        }
        top = Math.max(0, Math.min(top, editor.getHeight() - getHeight() - 5));
        float handleLeftX = editor.getOffset(editor.getCursor().getLeftLine(), editor.getCursor()
                                                                                     .getLeftColumn());
        float handleRightX = editor.getOffset(editor.getCursor().getRightLine(), editor.getCursor()
                                                                                       .getRightColumn());
        int panelX = (int) ((handleLeftX + handleRightX) / 2f - rootView.getMeasuredWidth() / 2f);
        setLocationAbsolutely(panelX, top);
        show();
    }

    private int selectTopRect(@NonNull RectF rect) {
        int rowHeight = editor.getRowHeight();
        if (rect.top - rowHeight * 3 / 2F > getHeight()) {
            return (int) (rect.top - (float) (rowHeight * 3) / 2 - getHeight());
        } else {
            return (int) (rect.bottom + (float) rowHeight / 2);
        }
    }

    /**
     * Update the state of paste button
     */
    private void updateButtonState() {
        pasteBtn.setEnabled(editor.hasClip());
        copyBtn.setVisibility(editor.getCursor().isSelected() ? View.VISIBLE : View.GONE);
        cutBtn.setVisibility(
            (editor.getCursor().isSelected() && editor.isEditable()) ? View.VISIBLE : View.GONE);
        pasteBtn.setVisibility(editor.isEditable() ? View.VISIBLE : View.GONE);
        longSelectBtn.setVisibility(
            (!editor.getCursor().isSelected() && editor.isEditable()) ? View.VISIBLE : View.GONE);
        expandSelectionBtn.setVisibility(
            (editor.getCursor().isSelected()) ? View.VISIBLE : View.GONE);
        rootView.measure(View.MeasureSpec.makeMeasureSpec(1000000, View.MeasureSpec.AT_MOST),
            View.MeasureSpec.makeMeasureSpec(100000, View.MeasureSpec.AT_MOST));
        setSize(Math.min(rootView.getMeasuredWidth(), (int) (editor.getDpUnit()
            * 230)), getHeight());
    }

    @Override
    public void show() {
        if (!enabled || editor.getSnippetController().isInSnippet()) {
            return;
        }
        super.show();
    }

    @Override
    public void onClick(@NonNull View view) {
        int id = view.getId();
        if (id == R.id.panel_btn_select_all) {
            attachTooltip(selectAllBtn, "Select all");
            editor.selectAll();
            return;
        } else if (id == R.id.panel_btn_cut) {
            attachTooltip(cutBtn, "Cut");
            if (editor.getCursor().isSelected()) {
                editor.cutText();
            }
        } else if (id == R.id.panel_btn_paste) {
            attachTooltip(pasteBtn, "Paste");
            editor.pasteText();
            editor.setSelection(editor.getCursor().getRightLine(), editor.getCursor()
                                                                         .getRightColumn());
        } else if (id == R.id.panel_btn_copy) {
            attachTooltip(copyBtn, "Copy");
            editor.copyText();
            editor.setSelection(editor.getCursor().getRightLine(), editor.getCursor()
                                                                         .getRightColumn());
        } else if (id == R.id.panel_btn_long_select) {
            attachTooltip(longSelectBtn, "Long select");
            editor.beginLongSelect();
        } else if (id == R.id.panel_btn_format) {
            // TODO: Format btw it must be contextual editor & file must be set
            attachTooltip(formatBtn, "Format");
            return;
        } else if (id == R.id.panel_btn_expand_selection) {
            attachTooltip(expandSelectionBtn, "Expand selection");
            if (editor.getEditable()) {
                // TODO: Handle
            }
        }
        dismiss();
    }

    private void attachTooltip(View anchor, String text) {
        // TODO: Use string res value below supports it
        TooltipCompat.setTooltipText(anchor, text);
    }

    public void setWindowCornerRadius(final int radius) {
        windowCornerRadius = radius;
    }

    private void runPostDisplay() {
        if (!isShowing()) {
            return;
        }
        dismiss();
        if (!editor.getCursor().isSelected()) {
            return;
        }
        editor.postDelayedInLifecycle(new Runnable() {
            @Override
            public void run() {
                if (!handler.hasAnyHeldHandle() && !editor.getSnippetController().isInSnippet()
                    && System.currentTimeMillis() - lastScroll > DELAY && editor.getScroller()
                                                                                .isFinished()) {
                    displayWindow();
                } else {
                    editor.postDelayedInLifecycle(this, DELAY);
                }
            }
        }, DELAY);
    }
}
