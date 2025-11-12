package com.eup.codeopsstudio.util;

import android.view.MotionEvent;

public class TapDetector {
  private static final int TRI_FINGER_COUNT = 3;
  private static final long TAP_TIMEOUT_MS = 500;

  private Runnable onTap;
  private int fingerCount = 0;
  private int currentFingers = 0;
  private long firstTouchTime = 0;

  public TapDetector() {
    this(TRI_FINGER_COUNT);
  }

  public TapDetector(int fingerCount) {
    this.fingerCount = fingerCount;
  }

  public boolean onTouchEvent(MotionEvent event) {
    int action = event.getActionMasked();

    switch (action) {
      case MotionEvent.ACTION_DOWN:
      case MotionEvent.ACTION_POINTER_DOWN:
        currentFingers = event.getPointerCount();
        if (currentFingers == fingerCount) {
          firstTouchTime = millsNow();
        }
        break;
      case MotionEvent.ACTION_UP:
      case MotionEvent.ACTION_POINTER_UP:
      case MotionEvent.ACTION_CANCEL:
        if (currentFingers == fingerCount && elapsedMills(firstTouchTime) < TAP_TIMEOUT_MS) {
          if (onTap != null) onTap.run();
          return true;
        }
        currentFingers = 0;
        break;
    }
    return false;
  }

  public void setOnTapListener(Runnable listener) {
    this.onTap = listener;
  }

  private static long elapsedMills(long startMills) {
    return millsNow() - startMills;
  }

  private static long millsNow() {
    return System.currentTimeMillis();
  }
}
