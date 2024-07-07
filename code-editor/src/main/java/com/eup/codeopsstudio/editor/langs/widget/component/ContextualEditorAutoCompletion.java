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
 
   package com.eup.codeopsstudio.editor.langs.widget.component;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.AdapterView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.eup.codeopsstudio.editor.ContextualCodeEditor;
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
import java.lang.ref.WeakReference;
import java.util.List;

/**
 * Auto complete window for editing code quicker
 *
 * @author EUP
 */
public class ContextualEditorAutoCompletion extends EditorAutoCompletion {

  protected ContextualEditorCompletionAdapter adapter;
  private ContextualCodeEditor editor; // was editor

  private static final long SHOW_PROGRESS_BAR_DELAY = 50;
  protected boolean cancelShowUp = false;
  protected long requestTime;
  protected int maxHeight;
  protected CompletionThread completionThread;
  protected CompletionPublisher publisher;
  protected WeakReference<List<CompletionItem>> lastAttachedItems;
  protected int currentSelection = -1;
  private ContextualCompletionLayout layout; // was completition layout
  private long requestShow = 0;
  private long requestHide = -1;
  private boolean enabled = true;
  private boolean loading = false;

  /**
   * Create a panel instance for the given editor
   *
   * @param editor Target editor
   */
  public ContextualEditorAutoCompletion(@NonNull ContextualCodeEditor codeEditor) {
    super(codeEditor);
    this.editor = codeEditor;
    adapter = new ContextualEditorCompletionAdapter();
    setLayout(new ContextualCompletionLayout(editor));
    editor.subscribeEvent(
        ColorSchemeUpdateEvent.class, ((event, unsubscribe) -> applyColorScheme()));
  }

  @SuppressWarnings("unchecked")
  public void setLayout(@NonNull ContextualCompletionLayout layout) {
    this.layout = layout;
    layout.setEditorCompletion(this);
    setContentView(layout.inflate(editor.getContext()));
    applyColorScheme();
    if (adapter != null) {
      this.layout.getCompletionList().setAdapter(adapter);
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

  public boolean isCompletionInProgress() {
    final CompletionThread thread = completionThread;
    return super.isShowing() || requestShow > requestHide || (thread != null && thread.isAlive());
  }

  /**
   * Some layout may support to display more animations, this method provides control over the
   * animation of the layout。
   *
   * @see CompletionLayout#setEnabledAnimation(boolean)
   */
  public void setEnabledAnimation(boolean enabledAnimation) {
    layout.setEnabledAnimation(enabledAnimation);
  }

  @SuppressWarnings("unchecked")
  public void setAdapter(@Nullable ContextualEditorCompletionAdapter adapter) {
    this.adapter = adapter;
    if (adapter == null) {
      this.adapter = new ContextualEditorCompletionAdapter();
    }

    layout.getCompletionList().setAdapter(adapter);
  }

  @Override
  public void show() {
    if (cancelShowUp || !isEnabled()) {
      return;
    }
    requestShow = System.currentTimeMillis();
    final long requireRequest = requestTime;
    editor.postDelayedInLifecycle(
        () -> {
          if (requestHide < requestShow && requestTime == requireRequest) {
            super.show();
          }
        },
        70);
  }

  public void hide() {
    super.dismiss();
    cancelCompletion();
    requestHide = System.currentTimeMillis();
  }

  public Context getContext() {
    return editor.getContext();
  }

  public int getCurrentPosition() {
    return currentSelection;
  }

  /*
   * Apply colors for self
   * TODO: Set EditorColorScheme in editor this currently isn't set as editor may be null initally
   * For this to work migrate CodeEditorManager#ensureTextmateTheme to Contextual Editor.
   * i.e themes should be set there in cce
   */
  public void applyColorScheme() {
    if (editor != null) {
      EditorColorScheme colors = editor.getColorScheme();
      if (colors == null) {
        colors = EditorColorScheme.getDefault();
      }
      layout.onApplyColorScheme(colors);
    }
  }

  /**
   * Change layout to loading/idle
   *
   * @param state Whether loading
   */
  public void setLoading(boolean state) {
    loading = state;
    if (state) {
      editor.postDelayedInLifecycle(
          () -> {
            if (loading) {
              layout.setLoading(true);
            }
          },
          SHOW_PROGRESS_BAR_DELAY);
    } else {
      layout.setLoading(false);
    }
  }

  /** Move selection down */
  public void moveDown() {
    AdapterView adpView = layout.getCompletionList();
    if (currentSelection + 1 >= adpView.getAdapter().getCount()) {
      return;
    }
    currentSelection++;
    ((EditorCompletionAdapter) adpView.getAdapter()).notifyDataSetChanged();
    ensurePosition();
  }

  /** Move selection up */
  public void moveUp() {
    AdapterView adpView = layout.getCompletionList();
    if (currentSelection - 1 < 0) {
      return;
    }
    currentSelection--;
    ((EditorCompletionAdapter) adpView.getAdapter()).notifyDataSetChanged();
    ensurePosition();
  }

  /** Make current selection visible */
  private void ensurePosition() {
    if (currentSelection != -1)
      layout.ensureListPositionVisible(currentSelection, adapter.getItemHeight());
  }

  /** Reject the requests from IME to set composing region/text */
  public boolean shouldRejectComposing() {
    return cancelShowUp;
  }

  /**
   * Select current position
   *
   * @return if the action is performed
   */
  public boolean select() {
    return select(currentSelection);
  }

  /**
   * Select the given position
   *
   * @param pos Index of auto complete item
   * @return if the action is performed
   */
  public boolean select(int pos) {
    if (pos == -1) {
      return false;
    }
    AdapterView adpView = layout.getCompletionList();
    CompletionItem item = ((ContextualEditorCompletionAdapter) adpView.getAdapter()).getItem(pos);
    Cursor cursor = editor.getCursor();
    final CompletionThread completionThread = this.completionThread;
    if (!cursor.isSelected() && completionThread != null) {
      cancelShowUp = true;
      editor.restartInput();
      editor.getText().beginBatchEdit();
      item.performCompletion(editor, editor.getText(), completionThread.requestPosition);
      editor.getText().endBatchEdit();
      editor.updateCursor();
      cancelShowUp = false;
      editor.restartInput();
    }
    hide();
    return true;
  }

  /** Stop previous completion thread */
  public void cancelCompletion() {
    CompletionThread previous = completionThread;
    if (previous != null && previous.isAlive()) {
      previous.cancel();
      previous.requestTimestamp = -1;
    }
    completionThread = null;
  }

  /**
   * Check cursor position's span. If {@link
   * io.github.rosemoe.sora.lang.styling.TextStyle#NO_COMPLETION_BIT} is set, true is returned.
   */
  public boolean checkNoCompletion() {
    CharPosition pos = editor.getCursor().left();
    Styles styles = editor.getStyles();
    return StylesUtils.checkNoCompletion(styles, pos);
  }

  /** Start completion at current selection position */
  public void requireCompletion() {
    if (cancelShowUp || !isEnabled()) {
      return;
    }
    Content text = editor.getText();
    if (text.getCursor().isSelected() || checkNoCompletion()) {
      hide();
      return;
    }
    if (System.nanoTime() - requestTime < editor.getProps().cancelCompletionNs) {
      hide();
      requestTime = System.nanoTime();
      return;
    }
    cancelCompletion();
    requestTime = System.nanoTime();
    currentSelection = -1;
    publisher =
        new CompletionPublisher(
            editor.getHandler(),
            () -> {
              List<CompletionItem> items = publisher.getItems();
              if (lastAttachedItems == null || lastAttachedItems.get() != items) {
                adapter.attachValues(this, items);
                adapter.notifyDataSetInvalidated();
                lastAttachedItems = new WeakReference<>(items);
              } else {
                adapter.notifyDataSetChanged();
              }
              float newHeight = adapter.getItemHeight() * adapter.getCount();
              if (newHeight == 0) {
                hide();
              }
              editor.updateCompletionWindowPosition();
              setSize(getWidth(), (int) Math.min(newHeight, maxHeight));
              if (!isShowing()) {
                show();
              }
            },
            editor.getEditorLanguage().getInterruptionLevel());
    completionThread = new CompletionThread(requestTime, publisher);
    setLoading(true);
    completionThread.start();
  }

  public void setMaxHeight(int height) {
    maxHeight = height;
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
      requestPosition = editor.getCursor().left();
      targetLanguage = editor.getEditorLanguage();
      contentRef = new ContentReference(editor.getText());
      contentRef.setValidator(this);
      localPublisher = publisher;
      extraData = editor.getExtraArguments();
      isAborted = false;
    }

    /** Abort the completion thread */
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

    @Override
    public void validate() {
      if (requestTime != requestTimestamp || isAborted) {
        throw new CompletionCancelledException();
      }
    }

    @Override
    public void run() {
      try {
        targetLanguage.requireAutoComplete(contentRef, requestPosition, localPublisher, extraData);
        if (localPublisher.hasData()) {
          if (completionThread == Thread.currentThread()) {
            localPublisher.updateList(true);
          }
        } else {
          editor.postInLifecycle(ContextualEditorAutoCompletion.this::hide);
        }
        editor.postInLifecycle(() -> setLoading(false));
      } catch (Exception e) {
        if (e instanceof CompletionCancelledException) {
          Log.v("CompletionThread", "Completion is cancelled");
        } else {
          e.printStackTrace();
        }
      }
    }
  }
}
