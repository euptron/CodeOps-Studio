# BidirectionalLayoutManager

## Overview

`BidirectionalLayoutManager` is a **RecyclerView layout manager** that lets you scroll both **vertically** and **horizontally**.

It’s useful when:

* Your items can be wider than the screen
* Standard Android layout managers fail to handle wide content properly

This library keeps scrolling smooth and memory-efficient while preserving RecyclerView recycling.


## Installation

Add this to your Gradle dependencies:
```groovy
dependencies {
    implementation 'io.github.euptron:bidirectional-llm:0.1.0'
}
```

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

### Typical Text Content

```xml
<TextView
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"/>
```

## Java Usage

```java
RecyclerView recyclerView = findViewById(R.id.recyclerView);

BidirectionalLayoutManager layoutManager = new BidirectionalLayoutManager(this);

recyclerView.setLayoutManager(layoutManager);
recyclerView.setAdapter(adapter);

// Optional: Auto-scroll horizontally to content end
layoutManager.setAutoScrollHorizontalEnabled(true);

// Smooth vertical scrolling
recyclerView.smoothScrollToPosition(adapter.getItemCount() - 1);
```

## Kotlin Usage

```kotlin
val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)

val layoutManager = BidirectionalLayoutManager(this)

recyclerView.layoutManager = layoutManager
recyclerView.adapter = adapter

// Optional: Auto-scroll horizontally to content end
layoutManager.isAutoScrollHorizontalEnabled = true

// Smooth vertical scrolling
recyclerView.smoothScrollToPosition(adapter.itemCount - 1)
```

## Recommendations

* Disable **nested scrolling**

```java
recyclerView.setNestedScrollingEnabled(false);
```

* If you're not using `smoothScrollToPosition`, **disable `ItemAnimator`** to avoid horizontal scroll jitters:

```java
recyclerView.setItemAnimator(null);
```

## When to Use

* Items are unbounded in width
* Vertical scrolling is the primary navigation
* Horizontal scrolling is global, not per-item

**Do not use** when:

* Items require independent horizontal scroll states
* You need grid or staggered layouts

## Limitations

* Designed for vertical orientation only
* Not intended as a general-purpose 2D layout manager
* Horizontal scrolling is global, not per-row

## Contributing

Fixes, contributions, issues, and performance improvements are welcome. Please include:

* Android version
* RecyclerView version
* Dataset size
* Reproduction steps (if it's an issue)

## License

Copyright (c) Etido Peter

This module is licensed under the [Apache 2.0](./LICENSE)