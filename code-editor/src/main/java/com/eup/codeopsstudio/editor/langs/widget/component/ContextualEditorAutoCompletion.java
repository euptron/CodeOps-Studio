/*
 * This file is part of CodeOps Studio.
 * CodeOps Studio - Code anywhere anytime
 * https://github.com/euptron/CodeOps-Studio
 * Copyright (C) 2024-2026 Etido Peter
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

import android.content.Context;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.ListAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.eup.codeopsstudio.common.ILog;
import com.eup.codeopsstudio.editor.ContextualCodeEditor;

import java.lang.ref.WeakReference;
import java.util.List;

import io.github.rosemoe.sora.event.ColorSchemeUpdateEvent;
import io.github.rosemoe.sora.lang.Language;
import io.github.rosemoe.sora.lang.completion.CompletionCancelledException;
import io.github.rosemoe.sora.lang.completion.CompletionItem;
import io.github.rosemoe.sora.lang.completion.CompletionPublisher;
import io.github.rosemoe.sora.lang.styling.Styles;
import io.github.rosemoe.sora.lang.styling.StylesUtils;
import io.github.rosemoe.sora.text.CharPosition;
import io.github.rosemoe.sora.text.Content;
import io.github.rosemoe.sora.text.ContentReference;
import io.github.rosemoe.sora.text.Cursor;
import io.github.rosemoe.sora.text.TextReference;
import io.github.rosemoe.sora.widget.component.CompletionLayout;
import io.github.rosemoe.sora.widget.component.EditorAutoCompletion;
import io.github.rosemoe.sora.widget.component.EditorCompletionAdapter;
import io.github.rosemoe.sora.widget.schemes.EditorColorScheme;

/**
 * Auto complete window for editing code quicker
 *
 * @author Etido Peter
 */
public class ContextualEditorAutoCompletion extends EditorAutoCompletion {

    public static final String TAG = "ContextualEditorAutoCompletion";
    private static final long SHOW_PROGRESS_BAR_DELAY = 50;
    private final ContextualCodeEditor editor; // was editor
    protected ContextualEditorCompletionAdapter mAdapter;
    protected boolean mCancelShowUp = false;
    protected long mRequestTime;
    protected int mMaxHeight;
    protected CompletionThread mCompletionThread;
    protected CompletionPublisher mPublisher;
    protected WeakReference<List<CompletionItem>> mLastAttachedItems;
    protected int mCurrentSelection = -1;
    private ContextualCompletionLayout layout; // was completion layout
    private long requestShow = 0;
    private long requestHide = -1;
    private boolean enabled = true;

    /**
     * Create a panel instance for the given editor
     *
     * @param codeEditor Target editor
     */
    public ContextualEditorAutoCompletion(@NonNull ContextualCodeEditor codeEditor) {
        super(codeEditor);
        this.editor = codeEditor;
        mAdapter    = new ContextualEditorCompletionAdapter();
        setLayout(new ContextualCompletionLayout(editor));
        editor.subscribeEvent(ColorSchemeUpdateEvent.class,
            ((event, unsubscribe) -> applyColorScheme()));
    }

    public void setLayout(@NonNull ContextualCompletionLayout layout) {
        this.layout = layout;
        layout.setEditorCompletion(this);
        setContentView(layout.inflate(editor.getContext()));
        applyColorScheme();
        if (mAdapter != null) {
            this.layout.getCompletionList().setAdapter(mAdapter);
        }
    }

    public void setAdapter(@Nullable ContextualEditorCompletionAdapter mAdapter) {
        this.mAdapter = mAdapter;
        if (mAdapter == null) {
            this.mAdapter = new ContextualEditorCompletionAdapter();
        }

        layout.getCompletionList().setAdapter(mAdapter);
    }

    /**
     * Auto-completion Analyzing thread
     *
     * @author Rosemoe
     */
    public final class CompletionThread extends Thread implements TextReference.Validator {

        private final Bundle extraData;
        private final CharPosition requestPosition;
        private final Language targetLanguage;
        private final ContentReference contentRef;
        private final CompletionPublisher localPublisher;
        private long requestTimestamp;
        private boolean isAborted;

        public CompletionThread(long requestTime, @NonNull CompletionPublisher publisher) {
            requestTimestamp = requestTime;
            requestPosition  = editor.getCursor().left();
            targetLanguage   = editor.getEditorLanguage();
            contentRef       = new ContentReference(editor.getText());
            contentRef.setValidator(this);
            localPublisher = publisher;
            extraData      = editor.getExtraArguments();
            isAborted      = false;
        }

        @Override
        public void run() {
            try {
                targetLanguage.requireAutoComplete(contentRef, requestPosition, localPublisher,
                    extraData);
                if (localPublisher.hasData()) {
                    if (mCompletionThread == Thread.currentThread()) {
                        localPublisher.updateList(true);
                    }
                } else {
                    editor.postInLifecycle(ContextualEditorAutoCompletion.this::hide);
                }
                editor.postInLifecycle(() -> setLoading(false));
            } catch (CompletionCancelledException e) {
                ILog.warning(TAG, "Completion is cancelled");
            } catch (Exception e) {
                ILog.error(TAG, "Failed to run Auto Completion");
            }
        }

        @Override
        public void validate() {
            if (mRequestTime != requestTimestamp || isAborted) {
                throw new CompletionCancelledException();
            }
        }

        /**
         * Abort the completion thread
         */
        public void cancel() {
            isAborted = true;
            int level = targetLanguage.getInterruptionLevel();
            if (level == Language.INTERRUPTION_LEVEL_STRONG) {
                interrupt();
            }
            localPublisher.cancel();
        }

        public boolean isCancelled() {
            return isAborted;
        }
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (!enabled) {
            hide();
        }
    }

    @Override
    public boolean isCompletionInProgress() {
        final CompletionThread thread = mCompletionThread;
        return super.isShowing() || requestShow > requestHide || (thread != null
            && thread.isAlive());
    }

    /**
     * Some layout may support to display more animations, this method provides control over the
     * animation of the layout。
     *
     * @see CompletionLayout#setEnabledAnimation(boolean)
     */
    @Override
    public void setEnabledAnimation(boolean enabledAnimation) {
        layout.setEnabledAnimation(enabledAnimation);
    }

    @Override
    public void show() {
        if (mCancelShowUp || !isEnabled()) {
            return;
        }
        requestShow = System.currentTimeMillis();
        final long requireRequest = mRequestTime;
        editor.postDelayedInLifecycle(() -> {
            if (requestHide < requestShow && mRequestTime == requireRequest) {
                super.show();
            }
        }, 70);
    }

    @Override
    public void hide() {
        super.dismiss();
        cancelCompletion();
        requestHide = System.currentTimeMillis();
    }

    @Override
    public Context getContext() {
        return editor.getContext();
    }

    @Override
    public int getCurrentPosition() {
        return mCurrentSelection;
    }

    @Override
    public void applyColorScheme() {
        if (editor != null) {
            EditorColorScheme colors = editor.getColorScheme();
            layout.onApplyColorScheme(colors);
        }
    }

    @Override
    public void setLoading(boolean loading) {
        if (loading) {
            editor.postDelayedInLifecycle(() -> layout.setLoading(true), SHOW_PROGRESS_BAR_DELAY);
        } else {
            layout.setLoading(false);
        }
    }

    /**
     * Move selection down
     */
    @Override
    public void moveDown() {
        AdapterView<ListAdapter> adpView = layout.getCompletionList();
        if (mCurrentSelection + 1 >= adpView.getAdapter().getCount()) {
            return;
        }
        mCurrentSelection++;
        ((EditorCompletionAdapter) adpView.getAdapter()).notifyDataSetChanged();
        doEnsurePosition();
    }

    /**
     * Move selection up
     */
    @Override
    public void moveUp() {
        AdapterView<ListAdapter> adpView = layout.getCompletionList();
        if (mCurrentSelection - 1 < 0) {
            return;
        }
        mCurrentSelection--;
        ((EditorCompletionAdapter) adpView.getAdapter()).notifyDataSetChanged();
        doEnsurePosition();
    }

    /**
     * Make current selection visible
     */
    private void doEnsurePosition() {
        if (mCurrentSelection != -1) {
            layout.ensureListPositionVisible(mCurrentSelection, mAdapter.getItemHeight());
        }
    }

    /**
     * Reject the requests from IME to set composing region/text
     */
    @Override
    public boolean shouldRejectComposing() {
        return mCancelShowUp;
    }

    /**
     * Select current position
     *
     * @return if the action is performed
     */
    @Override
    public boolean select() {
        return select(mCurrentSelection);
    }

    /**
     * Select the given position
     *
     * @param pos Index of auto complete item
     * @return if the action is performed
     */
    @Override
    public boolean select(int pos) {
        if (pos == -1) {
            return false;
        }
        AdapterView<ListAdapter> adpView = layout.getCompletionList();
        CompletionItem item =
            ((ContextualEditorCompletionAdapter) adpView.getAdapter()).getItem(pos);
        Cursor cursor = editor.getCursor();
        final CompletionThread thread = this.mCompletionThread;
        if (!cursor.isSelected() && thread != null) {
            mCancelShowUp = true;
            editor.restartInput();
            editor.getText().beginBatchEdit();
            item.performCompletion(editor, editor.getText(), mCompletionThread.requestPosition);
            editor.getText().endBatchEdit();
            editor.updateCursor();
            mCancelShowUp = false;
            editor.restartInput();
        }
        hide();
        return true;
    }

    /**
     * Stop previous completion thread
     */
    @Override
    public void cancelCompletion() {
        CompletionThread previous = mCompletionThread;
        if (previous != null && previous.isAlive()) {
            previous.cancel();
            previous.requestTimestamp = -1;
        }
        mCompletionThread = null;
    }

    /**
     * Check cursor position's span. If {@link
     * io.github.rosemoe.sora.lang.styling.TextStyle#NO_COMPLETION_BIT} is set, true is returned.
     */
    @Override
    public boolean checkNoCompletion() {
        CharPosition pos = editor.getCursor().left();
        Styles styles = editor.getStyles();
        return StylesUtils.checkNoCompletion(styles, pos);
    }

    /**
     * Start completion at current selection position
     */
    @Override
    public void requireCompletion() {
        if (mCancelShowUp || !isEnabled()) {
            return;
        }
        Content text = editor.getText();
        if (text.getCursor().isSelected() || checkNoCompletion()) {
            hide();
            return;
        }
        if (System.nanoTime() - mRequestTime < editor.getProps().cancelCompletionNs) {
            hide();
            mRequestTime = System.nanoTime();
            return;
        }
        cancelCompletion();
        mRequestTime      = System.nanoTime();
        mCurrentSelection = -1;
        mPublisher        = new CompletionPublisher(editor.getHandler(), () -> {
            List<CompletionItem> items = mPublisher.getItems();
            if (mLastAttachedItems == null || mLastAttachedItems.get() != items) {
                mAdapter.attachValues(this, items);
                mAdapter.notifyDataSetInvalidated();
                mLastAttachedItems = new WeakReference<>(items);
            } else {
                mAdapter.notifyDataSetChanged();
            }
            float newHeight = mAdapter.getItemHeight() * mAdapter.getCount();
            if (newHeight == 0) {
                hide();
            }
            editor.updateCompletionWindowPosition();
            setSize(getWidth(), (int) Math.min(newHeight, mMaxHeight));
            if (!isShowing()) {
                show();
            }
        }, editor.getEditorLanguage().getInterruptionLevel());
        mCompletionThread = new CompletionThread(mRequestTime, mPublisher);
        setLoading(true);
        mCompletionThread.start();
    }

    @Override
    public void setMaxHeight(int height) {
        mMaxHeight = height;
    }
}
