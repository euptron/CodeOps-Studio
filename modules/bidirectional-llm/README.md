# BidirectionalLayoutManager

## Overview

`BidirectionalLayoutManager` is a custom `RecyclerView.LayoutManager` built to solve a common but poorly-supported UI problem on Android: **vertically scrolling lists whose individual items may extend far beyond the horizontal viewport**.

This scenario is typical in:

* Logcat / logging consoles
* Code editors and viewers
* Terminal output
* Diagnostic or debug panels
* Any dataset where lines are unbounded in width

Standard Android layout tools do not handle this case correctly or efficiently. This module provides a stable, RecyclerView-native solution.

---

## Problem Statement

`LinearLayoutManager` supports scrolling on a single axis. When list items are wider than the screen:

* Horizontal overflow is clipped
* Wrapping the `RecyclerView` in a `HorizontalScrollView` breaks recycling and measurement
* Nested scrolling introduces jitter, excessive layout passes, and memory pressure

These workarounds violate RecyclerView’s design assumptions and often fail under heavy datasets (e.g. thousands of log lines).

---

## Solution

`BidirectionalLayoutManager` extends `LinearLayoutManager` (vertical) and introduces **controlled, global horizontal scrolling** while preserving:

* Correct view recycling
* Deterministic layout positions
* Stable smooth scrolling
* Adapter update safety

Each item is allowed to measure to its natural width, while the `RecyclerView` itself remains a fixed-width viewport.

---

## Design Principles

* Vertical layout and recycling are fully delegated to `LinearLayoutManager`
* Horizontal movement is applied **only during scroll operations**
* Layout positions remain free of scroll-side effects
* Measured item widths are cached per adapter position
* Horizontal scroll range is derived from the widest known item

This ensures stability during:

* Smooth scrolling
* Adapter mutations (add/remove/move/update)
* Relayouts and configuration changes

---

## Included Components

### 1. BidirectionalLayoutManager

A vertical `LayoutManager` with support for horizontal scrolling across all visible children.

Responsibilities:

* Measure items with `UNSPECIFIED` width to capture true content size
* Cache measured widths per adapter position
* Compute horizontal scroll range from the widest item
* Apply horizontal offset uniformly via `offsetChildrenHorizontal`
* Maintain scroll bounds and offset consistency

### 2. BidirectionalSmoothScroller

A specialized `RecyclerView.SmoothScroller` designed specifically for this layout manager.

Key characteristics:

* Animates only on the vertical axis
* Explicitly prevents horizontal scroll adjustment
* Preserves the current horizontal offset during smooth scrolls

Supported alignment modes:

* `SNAP_TO_TOP`
* `SNAP_TO_BOTTOM` (default, suitable for logs)
* `SNAP_TO_CENTER`

This avoids jitter and unintended horizontal jumps when smooth scrolling through very wide items.

---

## XML Configuration (Critical)

### RecyclerView

The `RecyclerView` must define a fixed horizontal viewport.

```xml
<androidx.recyclerview.widget.RecyclerView
    android:id="@+id/recyclerView"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:overScrollMode="never" />
```

Rules:

* `layout_width` **must** be `match_parent`
* `wrap_content` is not supported

---

### Item Root Layout

Each item must be allowed to expand horizontally.

```xml
<LinearLayout
    android:layout_width="wrap_content"
    android:layout_height="wrap_content">
```

Rules:

* Root width must be `wrap_content`
* `match_parent` will break horizontal scrolling

---

### Typical Text Content

```xml
<TextView
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:singleLine="true" />
```

---

## Java Usage

```java
RecyclerView recyclerView = findViewById(R.id.recyclerView);

BidirectionalLayoutManager layoutManager =
        new BidirectionalLayoutManager(this);

recyclerView.setLayoutManager(layoutManager);
recyclerView.setAdapter(adapter);
```

### Optional: Auto-scroll horizontally to content end

Useful for log viewers where new lines may extend further than previous ones.

```java
layoutManager.setAutoScrollHorizontalEnabled(true);
```

### Smooth vertical scrolling

```java
recyclerView.smoothScrollToPosition(adapter.getItemCount() - 1);
```

Smooth scrolling remains vertically animated while horizontal offset is preserved.

---

## Nested Scrolling (Strong Recommendation)

**Disable nested scrolling** when using this layout manager.

```java
recyclerView.setNestedScrollingEnabled(false);
```

Reasons:

* Prevents competing scroll dispatch between parents and children
* Avoids scroll jitter when both axes are active
* Improves performance on large datasets

This is especially important in:

* CoordinatorLayout
* BottomSheetBehavior
* Nested scrolling containers

---

## ItemAnimator Considerations

Default `RecyclerView.ItemAnimator` implementations may introduce visual instability when frequent horizontal offsets are applied.

For high-frequency updates (e.g. live logs):

```java
recyclerView.setItemAnimator(null);
```

This is optional but recommended for maximum stability.

---

## Performance Notes

* Designed to handle thousands of items (e.g. 5k–10k log lines)
* Width caching avoids repeated expensive measurements
* Horizontal scrolling is O(1) per frame

For bursty datasets, consider periodically resetting your backing list to release memory.

---

## When This LayoutManager Is Appropriate

Use this when:

* Items are unbounded in width
* Vertical scrolling is the primary navigation
* Horizontal scrolling is global, not per-item

Do not use this when:

* Items require independent horizontal scroll states
* You need grid or staggered layouts

---

## Limitations

* Designed for vertical orientation only
* Not intended as a general-purpose 2D layout manager
* Horizontal scrolling is global, not per-row

---

## Contributing

This module exists to solve real-world RecyclerView limitations under heavy and unconventional workloads.

If you encounter:

* Edge cases
* Performance regressions
* Adapter interaction issues
* OEM-specific behavior

Contributions, issues, and improvements are welcome.

Please include:

* Android version
* RecyclerView version
* Dataset size
* Reproduction steps or sample project

---

## License

Copyright (c) Etido Peter

This module is licensed under the [Apache 2.0](./LICENSE)