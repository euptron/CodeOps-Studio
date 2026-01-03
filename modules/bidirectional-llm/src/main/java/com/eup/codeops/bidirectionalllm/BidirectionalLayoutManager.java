package com.eup.codeops.bidirectionalllm;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.SparseIntArray;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * A {@link RecyclerView.LayoutManager} that extends {@link LinearLayoutManager} to support
 * <b>bidirectional scrolling</b> for a vertically oriented {@link RecyclerView}, allowing
 * individual items to extend horizontally beyond the viewport while preserving correct recycling,
 * measurement, and scroll behavior.
 *
 * <h2>Design Note (IMPORTANT)</h2>
 *
 * <p>This LayoutManager supports smooth vertical scrolling through a custom {@link
 * RecyclerView.SmoothScroller} implementation that constrains animated scrolling to the vertical
 * axis while preserving the current horizontal scroll offset.
 *
 * <p>This design avoids issues commonly observed when using default smooth scrolling behavior with
 * very wide child views, such as:
 *
 * <ul>
 *   <li>Unintended horizontal offset adjustments
 *   <li>Visual jitter during fast vertical scrolling
 *   <li>Unstable animations when views are frequently recycled
 * </ul>
 *
 * <p>For immediate, non-animated positioning, {@link
 * RecyclerView#scrollToPosition(int)} remains fully supported and free
 * of side effects.
 *
 * <h2>Problem This LayoutManager Solves</h2>
 *
 * <p>Standard {@link LinearLayoutManager} supports only a single scroll axis. When displaying logs,
 * code lines, terminal output, or diagnostics, items often exceed the available screen width.
 *
 * <p>Wrapping a {@link RecyclerView} inside a {@link android.widget.HorizontalScrollView} is
 * incorrect because it:
 *
 * <ul>
 *   <li>Breaks view recycling
 *   <li>Violates RecyclerView’s measurement contract
 *   <li>Causes excessive layout passes and memory pressure
 * </ul>
 *
 * <p>This LayoutManager preserves vertical recycling while enabling controlled, global horizontal
 * scrolling across all visible items.
 *
 * <h2>How It Works Internally</h2>
 *
 * <ul>
 *   <li>Delegates vertical layout and recycling to {@link LinearLayoutManager}
 *   <li>Measures child width using {@link View.MeasureSpec#UNSPECIFIED}
 *   <li>Caches measured widths per adapter position
 *   <li>Uses the widest known item to compute the horizontal scroll range
 *   <li>Applies horizontal scrolling via {@link #scrollHorizontallyBy(int, RecyclerView.Recycler,
 *       RecyclerView.State)} using {@link RecyclerView.LayoutManager#offsetChildrenHorizontal(int)}
 * </ul>
 *
 * <p>Horizontal movement is applied <b>only during scroll operations</b>. Layout positions remain
 * deterministic and free of scroll-side effects, ensuring stability during recycling, relayouts,
 * adapter updates, and smooth scrolling.
 *
 * <h2>Measurement Contract</h2>
 *
 * <p>Each item is measured with an UNSPECIFIED width spec so it can report its natural size. The
 * cached width includes:
 *
 * <ul>
 *   <li>Measured view width
 *   <li>Layout margins
 *   <li>Item decorations (via {@link #calculateItemDecorationsForChild(View,Rect)})
 * </ul>
 *
 * <h2>Correct XML Configuration (CRITICAL)</h2>
 *
 * <h3>RecyclerView XML</h3>
 *
 * <pre>{@code
 * <androidx.recyclerview.widget.RecyclerView
 *     android:id="@+id/recyclerView"
 *     android:layout_width="match_parent"
 *     android:layout_height="match_parent"
 *     android:overScrollMode="never" />
 * }</pre>
 *
 * <ul>
 *   <li>The RecyclerView defines a fixed horizontal viewport
 *   <li>{@code wrap_content} MUST NOT be used for width
 * </ul>
 *
 * <h3>Item Root Layout XML</h3>
 *
 * <pre>{@code
 * <LinearLayout
 *     android:layout_width="wrap_content"
 *     android:layout_height="wrap_content">
 * }</pre>
 *
 * <ul>
 *   <li>Allows the item to expand horizontally based on content
 *   <li>{@code match_parent} WILL break horizontal scrolling
 * </ul>
 *
 * <h3>Typical Text Content</h3>
 *
 * <pre>{@code
 * <TextView
 *     android:layout_width="wrap_content"
 *     android:layout_height="wrap_content"
 *     android:singleLine="true" />
 * }</pre>
 *
 * <h2>Java Usage Example</h2>
 *
 * <pre>{@code
 * RecyclerView recyclerView = findViewById(R.id.recyclerView);
 *
 * BidirectionalLayoutManager layoutManager =
 *         new BidirectionalLayoutManager(this);
 *
 * recyclerView.setLayoutManager(layoutManager);
 * recyclerView.setAdapter(adapter);
 *
 * // Optional: auto-scroll horizontally to the end
 * layoutManager.setAutoScrollHorizontalEnabled(true);
 *
 * // Smooth vertical scroll with stable horizontal positioning
 * recyclerView.smoothScrollToPosition(adapter.getItemCount() - 1);
 * }</pre>
 *
 * <h2>ItemAnimator Considerations</h2>
 *
 * <p>Default {@link RecyclerView.ItemAnimator} implementations may introduce visual jitter when
 * frequent horizontal offsets are applied. Disabling animations during intensive updates can
 * improve stability.
 *
 * <h2>Analogy</h2>
 *
 * <p>The RecyclerView is a fixed window frame. Each item is a potentially wide sheet of paper. This
 * LayoutManager slides those sheets horizontally behind the frame without resizing the frame
 * itself.
 *
 * <h2>Threading Analogy</h2>
 *
 * <p>All operations occur on the UI thread, consistent with RecyclerView’s threading contract.
 *
 * @author Etido Peter
 * @see BidirectionalSmoothScroller
 * @see androidx.recyclerview.widget.RecyclerView
 * @see androidx.recyclerview.widget.RecyclerView.LayoutManager
 * @see androidx.recyclerview.widget.LinearLayoutManager
 */
public class BidirectionalLayoutManager extends LinearLayoutManager {

  private static final String TAG = "BidirectionalLayoutMngr";

  // Cache measured widths of items by position
  private final SparseIntArray itemWidthsCache = new SparseIntArray();

  // Max width measured across currently known items
  private int maxItemWidth = 0;
  // Total content width (maxItemWidth + RV padding)
  private int contentWidth = 0;
  // Current horizontal scroll offset
  private int horizontalScrollOffset = 0;
  // Flag for auto-scrolling horizontally
  private boolean shouldAutoScrollHorizontally = false;

  // Temp rect for measurements
  private final Rect mTempRect = new Rect();

  public BidirectionalLayoutManager(Context context) {
    super(context, VERTICAL, false);
  }

  public BidirectionalLayoutManager(Context context, int orientation, boolean reverseLayout) {
    super(context, orientation, reverseLayout);
    setOrientation(VERTICAL);
  }

  public BidirectionalLayoutManager(
      Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
    setOrientation(VERTICAL);
  }

  @Override
  public boolean canScrollHorizontally() {
    // Only allow scrolling if the content is actually wider than the screen
    return contentWidth > getViewportWidth();
  }

  @Override
  public int scrollHorizontallyBy(
      int dx, @NonNull RecyclerView.Recycler recycler, @NonNull RecyclerView.State state) {
    if (getChildCount() == 0 || dx == 0) return 0;

    int maxOffset = getMaxHorizontalScrollOffset();
    int proposedOffset = horizontalScrollOffset + dx;
    int consumed = dx;

    // Clamp scroll bounds
    if (proposedOffset < 0) {
      consumed = -horizontalScrollOffset;
      horizontalScrollOffset = 0;
    } else if (proposedOffset > maxOffset) {
      consumed = maxOffset - horizontalScrollOffset;
      horizontalScrollOffset = maxOffset;
    } else {
      horizontalScrollOffset = proposedOffset;
    }
    offsetChildrenHorizontal(-consumed);

    return consumed;
  }

  @Override
  public int computeHorizontalScrollRange(@NonNull RecyclerView.State state) {
    return contentWidth;
  }

  @Override
  public int computeHorizontalScrollOffset(@NonNull RecyclerView.State state) {
    return horizontalScrollOffset;
  }

  @Override
  public int computeHorizontalScrollExtent(@NonNull RecyclerView.State state) {
    return getViewportWidth();
  }

  @Override
  public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
    int offsetAtStart = horizontalScrollOffset;
    super.onLayoutChildren(recycler, state);

    if (getItemCount() == 0) {
      resetState();
      return;
    }

    recalculateContentWidth();

    int maxOffset = getMaxHorizontalScrollOffset();
    int targetOffset = horizontalScrollOffset;

    if (shouldAutoScrollHorizontally) {
      targetOffset = maxOffset;
    } else {
      // ensure scroll isn't out of bounds if content shrank
      targetOffset = Math.min(targetOffset, maxOffset);
      targetOffset = Math.max(0, targetOffset);
    }

    if (targetOffset != offsetAtStart) {
      int diff = targetOffset - offsetAtStart;
      offsetChildrenHorizontal(-diff);
      horizontalScrollOffset = targetOffset;
    }
  }

  @Override
  public void layoutDecoratedWithMargins(
      @NonNull View child, int left, int top, int right, int bottom) {
    super.layoutDecoratedWithMargins(child, left, top, right, bottom);
    child.offsetLeftAndRight(-horizontalScrollOffset);
  }

  @Override
  public void measureChildWithMargins(@NonNull View child, int widthUsed, int heightUsed) {
    final RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) child.getLayoutParams();
    final int position = getPosition(child);

    if (position < 0 || position >= getItemCount()) {
      super.measureChildWithMargins(child, widthUsed, heightUsed);
      return;
    }

    int cachedWidth = itemWidthsCache.get(position, -1);
    if (cachedWidth != -1) {
      calculateItemDecorationsForChild(child, mTempRect);
      int decorHeight = mTempRect.top + mTempRect.bottom;

      final int widthSpec = View.MeasureSpec.makeMeasureSpec(cachedWidth, View.MeasureSpec.EXACTLY);
      final int heightSpec =
          getChildMeasureSpec(
              getHeight(),
              getHeightMode(),
              getPaddingTop()
                  + getPaddingBottom()
                  + lp.topMargin
                  + lp.bottomMargin
                  + heightUsed
                  + decorHeight,
              lp.height,
              canScrollVertically());

      child.measure(widthSpec, heightSpec);
      return;
    }

    calculateItemDecorationsForChild(child, mTempRect);
    int decorHeight = mTempRect.top + mTempRect.bottom;
    int decorWidth = mTempRect.left + mTempRect.right;

    // Force UNSPECIFIED width spec to get true content width
    final int widthSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);

    final int heightSpec =
        getChildMeasureSpec(
            getHeight(),
            getHeightMode(),
            getPaddingTop()
                + getPaddingBottom()
                + lp.topMargin
                + lp.bottomMargin
                + heightUsed
                + decorHeight,
            lp.height,
            canScrollVertically());

    child.measure(widthSpec, heightSpec);

    int measuredWidthWithMargins =
        child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin + decorWidth;

    itemWidthsCache.put(position, measuredWidthWithMargins);

    if (measuredWidthWithMargins > maxItemWidth) {
      maxItemWidth = measuredWidthWithMargins;
      contentWidth = maxItemWidth + getPaddingLeft() + getPaddingRight();
      if (horizontalScrollOffset > getMaxHorizontalScrollOffset()) {
        requestLayout();
      }
    }
  }

  @Override
  public void onAdapterChanged(
      @Nullable RecyclerView.Adapter oldAdapter, @Nullable RecyclerView.Adapter newAdapter) {
    super.onAdapterChanged(oldAdapter, newAdapter);
    resetState();
  }

  @Override
  public void onItemsAdded(@NonNull RecyclerView recyclerView, int positionStart, int itemCount) {
    super.onItemsAdded(recyclerView, positionStart, itemCount);
    if (itemCount <= 0) return;

    int size = itemWidthsCache.size();
    SparseIntArray newCache = new SparseIntArray(size + itemCount);

    for (int i = 0; i < size; i++) {
      int key = itemWidthsCache.keyAt(i);
      int value = itemWidthsCache.valueAt(i);

      if (key < positionStart) {
        newCache.put(key, value);
      } else {
        newCache.put(key + itemCount, value);
      }
    }

    itemWidthsCache.clear();
    for (int i = 0; i < newCache.size(); i++) {
      itemWidthsCache.put(newCache.keyAt(i), newCache.valueAt(i));
    }
  }

  @Override
  public void onItemsRemoved(@NonNull RecyclerView recyclerView, int positionStart, int itemCount) {
    super.onItemsRemoved(recyclerView, positionStart, itemCount);
    if (itemCount <= 0) return;

    boolean removedMaxItem = false;

    // First, check if we're removing the widest item(s)
    for (int i = 0; i < itemCount; i++) {
      int position = positionStart + i;
      int cachedWidth = itemWidthsCache.get(position, -1);
      if (cachedWidth != -1 && cachedWidth == maxItemWidth) {
        removedMaxItem = true;
      }
    }

    // Remove the deleted items from cache
    for (int i = 0; i < itemCount; i++) {
      itemWidthsCache.delete(positionStart + i);
    }

    // Shift remaining items
    SparseIntArray newCache = new SparseIntArray(itemWidthsCache.size());
    for (int i = 0; i < itemWidthsCache.size(); i++) {
      int key = itemWidthsCache.keyAt(i);
      int value = itemWidthsCache.valueAt(i);

      if (key >= positionStart + itemCount) {
        newCache.put(key - itemCount, value);
      } else {
        newCache.put(key, value);
      }
    }

    itemWidthsCache.clear();
    for (int i = 0; i < newCache.size(); i++) {
      itemWidthsCache.put(newCache.keyAt(i), newCache.valueAt(i));
    }

    // Recalculate max width if we might have removed the widest item
    if (removedMaxItem) {
      recalculateContentWidth();
    }
  }

  @Override
  public void onItemsMoved(@NonNull RecyclerView recyclerView, int from, int to, int itemCount) {
    super.onItemsMoved(recyclerView, from, to, itemCount);
    // Move is complex - clear cache for affected items and request layout
    for (int i = 0; i < itemCount; i++) {
      itemWidthsCache.delete(from + i);
      // If destination overlaps source, we already cleared it above
      if (to + i < from || to + i >= from + itemCount) {
        itemWidthsCache.delete(to + i);
      }
    }
    requestLayout();
  }

  @Override
  public void onItemsUpdated(@NonNull RecyclerView recyclerView, int positionStart, int itemCount) {
    super.onItemsUpdated(recyclerView, positionStart, itemCount);
    if (itemCount <= 0) return;

    // Check if updated items include the widest
    boolean widestUpdated = false;
    for (int i = 0; i < itemCount; i++) {
      int position = positionStart + i;
      int cachedWidth = itemWidthsCache.get(position, -1);
      if (cachedWidth != -1 && cachedWidth == maxItemWidth) {
        widestUpdated = true;
      }
      // Invalidate cache for updated items
      itemWidthsCache.delete(position);
    }

    // Recalculate if widest was updated
    if (widestUpdated) {
      recalculateContentWidth();
    }
    requestLayout();
  }

  @Override
  public void smoothScrollToPosition(
      RecyclerView recyclerView, RecyclerView.State state, int position) {
    BidirectionalSmoothScroller linearSmoothScroller = new BidirectionalSmoothScroller(recyclerView.getContext());
    linearSmoothScroller.setTargetPosition(position);
    startSmoothScroll(linearSmoothScroller);
  }

  private void recalculateContentWidth() {
    int currentMax = 0;
    for (int i = 0; i < itemWidthsCache.size(); i++) {
      currentMax = Math.max(currentMax, itemWidthsCache.valueAt(i));
    }
    maxItemWidth = currentMax;
    contentWidth = maxItemWidth + getPaddingLeft() + getPaddingRight();
  }

  private void resetState() {
    itemWidthsCache.clear();
    maxItemWidth = 0;
    contentWidth = 0;
    horizontalScrollOffset = 0;
  }

  private int getMaxHorizontalScrollOffset() {
    return Math.max(0, contentWidth - getViewportWidth());
  }

  private int getViewportWidth() {
    return getWidth() - getPaddingLeft() - getPaddingRight();
  }

  public void setAutoScrollHorizontalEnabled(boolean enabled) {
    if (this.shouldAutoScrollHorizontally == enabled) return;

    this.shouldAutoScrollHorizontally = enabled;
    if (enabled) {
      scrollHorizontallyToEnd();
    }
  }

  public void scrollHorizontallyToEnd() {
    int maxOffset = getMaxHorizontalScrollOffset();
    if (horizontalScrollOffset != maxOffset) {
      int diff = maxOffset - horizontalScrollOffset;
      horizontalScrollOffset = maxOffset;
      offsetChildrenHorizontal(-diff);
    }
  }

  public boolean isAutoScrollHorizontalEnabled() {
    return shouldAutoScrollHorizontally;
  }
}
