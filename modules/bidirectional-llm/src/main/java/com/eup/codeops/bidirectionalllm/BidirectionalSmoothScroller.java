package com.eup.codeops.bidirectionalllm;

import android.content.Context;
import android.graphics.PointF;
import android.util.DisplayMetrics;
import android.view.View;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

/**
 * A {@link RecyclerView.SmoothScroller} implementation designed for use with {@link
 * BidirectionalLayoutManager}.
 *
 * <p>This smooth scroller performs animated scrolling along the vertical axis while explicitly
 * preventing horizontal scroll adjustments/jitter. It ensures that smooth scrolling remains stable when
 * child views extend beyond the horizontal viewport. 
 *
 * <p>Multiple alignment modes are supported to accommodate common vertical scrolling patterns such
 * as log viewers, search results, and list navigation.
 *
 * <h2>Alignment Modes</h2>
 *
 * <ul>
 *   <li>{@link Alignment#SNAP_TO_TOP} – Aligns the target item to the top of the viewport
 *   <li>{@link Alignment#SNAP_TO_BOTTOM} – Aligns the target item to the bottom of the viewport
 *   <li>{@link Alignment#SNAP_TO_CENTER} – Centers the target item within the viewport
 * </ul>
 *
 * <p>This class is intended to be used internally by {@link BidirectionalLayoutManager} and is not
 * a general-purpose bidirectional smooth scroller.
 *
 * @author Etido Peter
 * @see RecyclerView.SmoothScroller
 * @see LinearSmoothScroller
 * @see BidirectionalLayoutManager
 */
public class BidirectionalSmoothScroller extends LinearSmoothScroller {

  public enum Alignment {
    SNAP_TO_TOP,
    SNAP_TO_BOTTOM,
    SNAP_TO_CENTER
  }

  private static final float DEFAULT_MILLISECONDS_PER_INCH = 25f;
  private static final int TARGET_SEEK_SCROLL_DISTANCE_PX = 10000;
  private static final float TARGET_SEEK_EXTRA_SCROLL_RATIO = 1.2f;

  private final Alignment mAlignment;
  private final float mMillisecondsPerInch;

  public BidirectionalSmoothScroller(Context context) {
    this(context, Alignment.SNAP_TO_BOTTOM, DEFAULT_MILLISECONDS_PER_INCH);
  }

  public BidirectionalSmoothScroller(Context context, Alignment alignment) {
    this(context, alignment, DEFAULT_MILLISECONDS_PER_INCH);
  }

  /**
   * Full Control Constructor.
   *
   * @param context Context
   * @param alignment SNAP_TO_TOP, SNAP_TO_BOTTOM, or SNAP_TO_CENTER
   * @param millisecondsPerInch Time in ms to scroll 1 inch. Lower = Faster.
   */
  public BidirectionalSmoothScroller(Context context, Alignment alignment, float millisecondsPerInch) {
    super(context);
    this.mAlignment = alignment;
    this.mMillisecondsPerInch =
        millisecondsPerInch > 0 ? millisecondsPerInch : DEFAULT_MILLISECONDS_PER_INCH;
  }

  @Override
  public int calculateDxToMakeVisible(View view, int snapPreference) {
    return 0; // prevent horizontal scrolls offset
  }

  @Override
  public int calculateDyToMakeVisible(View view, int snapPreference) {
    final RecyclerView.LayoutManager layoutManager = getLayoutManager();
    if (layoutManager == null || !layoutManager.canScrollVertically()) {
      return 0;
    }

    final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) view.getLayoutParams();
    final int top = layoutManager.getDecoratedTop(view) - params.topMargin;
    final int bottom = layoutManager.getDecoratedBottom(view) + params.bottomMargin;
    final int start = layoutManager.getPaddingTop();
    final int end = layoutManager.getHeight() - layoutManager.getPaddingBottom();

    switch (mAlignment) {
      case SNAP_TO_TOP:
        return start - top;
      case SNAP_TO_BOTTOM:
        return end - bottom;
      case SNAP_TO_CENTER:
        int viewHeight = bottom - top;
        int boxHeight = end - start;
        // Center point of the view
        int viewCenter = top + (viewHeight / 2);
        // Center point of the parent
        int boxCenter = start + (boxHeight / 2);
        return boxCenter - viewCenter;
      default:
        return super.calculateDtToFit(top, bottom, start, end, snapPreference);
    }
  }

  @Override
  protected void updateActionForInterimTarget(Action action) {
    PointF scrollVector = computeScrollVectorForPosition(getTargetPosition());
    if (scrollVector == null || (scrollVector.x == 0 && scrollVector.y == 0)) {
      final int target = getTargetPosition();
      action.jumpTo(target);
      stop();
      return;
    }

    normalize(scrollVector);
    mTargetVector = scrollVector;

    // Force Horizontal interim target to 0
    mInterimTargetDx = 0;
    mInterimTargetDy = (int) (TARGET_SEEK_SCROLL_DISTANCE_PX * scrollVector.y);

    final int time = calculateTimeForScrolling(TARGET_SEEK_SCROLL_DISTANCE_PX);

    action.update(
        0 /*dx*/,
        (int) (mInterimTargetDy * TARGET_SEEK_EXTRA_SCROLL_RATIO),
        (int) (time * TARGET_SEEK_EXTRA_SCROLL_RATIO),
        mLinearInterpolator);
  }

  @Override
  protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
    return mMillisecondsPerInch / displayMetrics.densityDpi;
  }
}
