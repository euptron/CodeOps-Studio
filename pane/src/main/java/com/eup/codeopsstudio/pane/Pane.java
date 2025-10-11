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

package com.eup.codeopsstudio.pane;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.SystemClock;
import android.view.View;
import android.view.ViewTreeObserver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.eup.codeopsstudio.common.ILog;
import com.eup.codeopsstudio.pane.exception.PaneAccessException;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A dynamic user interface element designed mainly for integration with {@code PaneWindow} to
 * provide Pane-Tab sync, outside this course it can be used in various parts of an application's
 * user interface, providing a flexible interaction paradigm.
 *
 * <p>Major extensions of this Pane class include:
 *
 * <ul>
 *   <li>{@link TextPane}
 *   <li>{@link EditorPane}
 *   <li>{@link FragmentPane}
 * </ul>
 *
 * <p>This abstract Pane class serves as a fundamental building block within the legacy WeStudio now
 * (CodeOps Studio) Editor tab system, representing an individual tabbed interface element that
 * manages a workspace for editing and displaying code and content.
 *
 * <p>This class provides a foundation for custom pane implementations and includes methods to
 * create, manage, and interact with panes. Always call {@code createView()} before interacting with
 * a pane to prevent crashes. Note that this class does not support its own thread, integrate a
 * thread or leverage {@link androidx.activity.ComponentActivity#runOnUiThread(Runnable)} or
 * {@link #runOnUiThread(Runnable)} and {@link #runOnBackgroundThread(Runnable)}
 *
 * <p><b>CHANGE LOG</b>
 *
 * <ul>
 *   <li>Version 0.1
 *       <ul>
 *         <li>Established foundation of pane system
 *       </ul>
 *   <li>Version 0.2
 *       <ul>
 *         <li>Introduced support for fragments
 *       </ul>
 *   <li>Version 0.3
 *       <ul>
 *         <li>Introduced UUID for pane identification
 *         <li>Added support for webview
 *       </ul>
 *   <li>Version 0.4
 *       <ul>
 *         <li>Implemented pane arguments
 *         <li>Added proper contextualisation of panes in their respective host {@link
 *             androidx.activity.ComponentActivity}
 *         <li>Added support for persisting pane contents.
 *       </ul>
 *   <li>Version 0.6
 *       <ul>
 *         <li>Added {@link #destroy()}
 *         <li>Added {@link #onDestroyView()}
 *         <li>Revamped class for efficiency
 *         <li>Defer heavy task to {@link #onViewLaidOut(View)}  class for efficiency
 *       </ul>
 * </ul>
 *
 * @author Etido Peter
 * @version 0.6
 * @see TextPane
 * @see EditorPane
 * @see FragmentPane
 */
public abstract class Pane {

    public static final String KEY_UUID = "uuid";
    public static final String KEY_TITLE = "title";
    /**
     * To support backward compatibility
     */
    public static final String PANE_VERSION = "0.5";
    public static final String KEY_PINNED = "pinned";
    public static final String KEY_SELECTED = "selected";
    public static final String KEY_ARGUMENTS = "arguments";
    public static final String KEY_CLASS_NAME = "className";
    public static final String KEY_PANE_VERSION = "paneVersion";
    public static final String KEY_CANONICAL_NAME = "canonicalName";
    private static final Gson GSON = new Gson();
    private static final List<UUID> sGeneratedIds = new ArrayList<>();
    private final Map<String, Object> mArguments = new HashMap<>();
    private final WeakReference<Context> mContextRef; // Prevents Activity leaks
    private final ExecutorService backgroundExecutor;
    private long paneCreateStartTime;
    private UUID mId;
    private View mView; // Null before createView() and after destruction
    private String mTitle;
    private boolean mIsPinned;
    private boolean mIsSelected;
    private boolean mHasPerformedCreateView;
    private boolean mHasPerformedOnViewCreated;
    private PaneState mState;
    private ViewTreeObserver.OnGlobalLayoutListener layoutListener;
    private boolean isViewLaidOut = false;

    protected Pane(Context context, String title) {
        this(context, title, true);
    }

    /**
     * Constructor for initializing a Pane.
     *
     * @param context      The application context.
     * @param title        The title of the Pane.
     * @param generateUUID Whether to generate an ID
     */
    protected Pane(Context context, String title, boolean generateUUID) {
        mContextRef = new WeakReference<>(context);
        mTitle      = title;
        mState      = PaneState.INITIALIZING;
        mIsPinned   = false;

        this.backgroundExecutor = Executors.newSingleThreadExecutor();

        if (generateUUID) {
            mId = generateUUID(); // auto-generate ID for new pane
        }
    }

    protected synchronized UUID generateUUID() {
        UUID generatedId;
        do {
            generatedId = UUID.randomUUID();
        } while (sGeneratedIds.contains(generatedId));
        sGeneratedIds.add(generatedId);
        return generatedId;
    }

    /**
     * Adds arguments (data) required by the pane to function.
     *
     * <p>These arguments could be used in persisting data to a given pane.
     *
     * @param key   the placeholder used to access an argument
     * @param value the typed value to be associated with the specified key
     */
    public final <V> void addArguments(final String key, final V value) {
        mArguments.put(key, value);
    }

    /**
     * Called to have the pane instantiate its user interface.
     *
     * @return the view for the panes UI.
     */
    public View createView() {
        paneCreateStartTime = millsNow();
        mView               = onCreateView();
        if (mView != null) {
            onViewCreated(mView);
            performOnViewLaidOut(mView);
            mHasPerformedCreateView = true;
        }
        return mView;
    }

    /**
     * Subclasses must override this method to create the view for the pane. This method should
     * return
     * the generated view that represents the user interface of the pane.
     *
     * @return The view representing the UI of the pane.
     */
    protected abstract View onCreateView();

    /**
     * Called after the {@link #createView()} method to handle post-creation operations on the
     * pane's
     * view. Subclasses can override this method to perform additional setup or customization on the
     * view generated by {@link #createView()}.
     *
     * @param view The view returned by the {@link #createView()} method. Can be {@code null} if the
     *             creation of the view fails. Implementations should check for null to handle
     *             such scenarios
     *             gracefully.
     */
    public void onViewCreated(@NonNull View view) {
        mState                     = PaneState.CREATED;
        mHasPerformedOnViewCreated = true;
    }

    private void performOnViewLaidOut(@NonNull View view) {
        //--- since 0.6
        // Check if view is already laid out (can happen in some cases)
        if (view.getWidth() > 0 && view.getHeight() > 0) {
            onViewLaidOut(view);
            return;
        }

        layoutListener = () -> {
            if (mState == PaneState.DESTROYED || getContext() == null) {
                safelyRemoveLayoutListener(view);
                return;
            }

            if (view.getWidth() > 0 && view.getHeight() > 0) {
                safelyRemoveLayoutListener(view);
                onViewLaidOut(view);
            }
        };

        view.getViewTreeObserver().addOnGlobalLayoutListener(layoutListener);

        // Safety check in case the layout listener doesn't fire
        view.post(() -> {
            if (!isViewLaidOut && view.getWidth() > 0 && view.getHeight() > 0) {
                safelyRemoveLayoutListener(view);
                onViewLaidOut(view);
            }
        });
    }

    private void safelyRemoveLayoutListener(@NonNull View view) {
        if (layoutListener != null) {
            try {
                final ViewTreeObserver vto = view.getViewTreeObserver();
                if (vto.isAlive()) {
                    vto.removeOnGlobalLayoutListener(layoutListener);
                }
            } catch (Exception e) {
                ILog.warning("Pane", "Error removing layout listener", e);
            } finally {
                layoutListener = null;
            }
        }
    }

    /**
     * Called after the view hierarchy associated with this pane has been measured and laid out.
     *
     * <p>This method is invoked when the view has valid dimensions (width and height greater
     * than 0)
     * and is the appropriate place to perform operations that require knowledge of the view's size,
     * such as initializing components that depend on layout measurements.</p>
     *
     * <p>Override this method to defer heavy operations that were previously done in
     * {@link #onViewCreated(View)}. This allows for a more fluid UI/UX by avoiding ANR
     * (Application Not Responding)
     * at startup time. Heavy operations such as file reading, complex view setup, or any task
     * that might
     * block the main thread should be performed here to ensure the UI is responsive during
     * initial layout.</p>
     *
     * <p><b>Note:</b> This method includes built-in performance monitoring that logs the layout
     * inflation time
     * and optionally shows a debug SnackBar when {@link #enabledDebug()} returns true.</p>
     *
     * @param view The pane's root view that has been laid out and has valid dimensions.
     */
    protected void onViewLaidOut(@NonNull View view) {
        isViewLaidOut = true;
        long paneReadyDuration = elapsedTime(paneCreateStartTime);
        String msg = "Layout inflation took: " + paneReadyDuration + "ms";
        ILog.debug("Pane Startup", msg);
        if (enabledDebug()) {
            showSnackBar(msg);
        }
    }

    public void showSnackBar(@NonNull String message) {
        final Snackbar snackbar = Snackbar.make(requireContext(), requireView(), message,
            BaseTransientBottomBar.LENGTH_SHORT);
        snackbar.setTextMaxLines(3);
        snackbar.show();
    }
    
    /**
     * Invokes a call to reload an action
     * <p> Override to implement reload function
     */
    public reload() {
      // No-op
    }

    /**
     * Return the {@link Context} this pane is currently associated with.
     *
     * @throws IllegalStateException if not currently associated with a context.
     * @see #getContext()
     */
    @NonNull
    public final Context requireContext() {
        Context context = getContext();
        if (context == null) throw new IllegalStateException(this + " not attached to a context");
        return context;
    }

    /**
     * Return the {@link Context} this pane is currently associated with.
     *
     * @see #requireContext()
     */
    @Nullable
    public Context getContext() {
        return mContextRef.get();
    }

    /**
     * Get the root view for the pane's layout (the one returned by {@link #createView}).
     *
     * @throws IllegalStateException if {@link #createView()} was not called first, the pane was
     *                               destroyed, no view was returned by {@link #onCreateView}
     * @see #getView()
     */
    @NonNull
    public final View requireView() {
        View v = getView();
        if (v == null) throw new IllegalStateException(this + " has no attached View");
        return v;
    }

    /**
     * Get the root view for the pane's layout (the one returned by {@link #createView}), if
     * provided.
     *
     * @return The panes's root view, or null if it has no layout.
     */
    @Nullable
    public View getView() {
        return mView;
    }

    protected boolean enabledDebug() {
        return false;
    }

    protected long elapsedTime(long startTime) {
        return millsNow() - startTime;
    }

    protected long millsNow() {
        return SystemClock.uptimeMillis();
    }

    @NonNull
    public static Pane deserialize(PaneFactory factory, JSONObject json) {
        try {
            Pane pane = factory.createPane(json);
            pane.restore(factory.getID(json), factory.getArguments());
            return pane;
        } catch (Exception unknownError) {
            throw new PaneAccessException("Pane deserialization failed", unknownError);
        }
    }

    public void restore(UUID existingId, Map<String, Object> savedState) {
        if (mId == null) {
            mId = existingId;
            synchronized (Pane.class) {
                sGeneratedIds.add(mId);
            }
        }
        mArguments.putAll(savedState);
    }

    /**
     * Permanently destroys the pane, triggering the {@link #onDestroy()} lifecycle method to
     * release
     * all resources. After invocation, the pane becomes invalid and should be dereferenced.
     *
     * <p>This method is marked {@code final} to enforce a consistent destruction process.
     * Subclasses
     * should override {@link #onDestroy()} or {@link #onDestroyView()} to implement custom cleanup
     * logic.
     *
     * @see #onDestroy() For internal state cleanup.
     * @see #onDestroyView() For view-specific resource cleanup.
     */
    public final void destroy() {
        onDestroy();
    }

    /**
     * Destroys the pane and releases all associated resources, transitioning it to an unusable
     * state.
     * After destruction, only {@link #getTitle()} remains accessible - all other pane operations
     * will
     * become invalid. This method should be called when the pane is permanently removed from the
     * UI.
     *
     * <p><b>Resource cleanup performed:</b>
     *
     * <ul>
     *   <li>Transitions pane state to {@link PaneState#DESTROYED}
     *   <li>Releases view hierarchy through {@link #onDestroyView()} and nullifies view reference
     *   <li>Clears all pane arguments
     *   <li>Removes UUID from global registry
     *   <li>Resets internal state flags (creation, selection, pinning status)
     * </ul>
     *
     * <p><b>Implementation Requirements:</b>
     *
     * <ul>
     *   <li>Subclasses should override {@link #onDestroyView()} to clean up view-specific resources
     *       rather than overriding this method directly.
     * </ul>
     *
     * @see #getTitle()
     * @see #onDestroyView()
     * @see PaneState#DESTROYED
     */
    protected void onDestroy() {
        mState = PaneState.DESTROYED;
        if (mView != null) {
            onDestroyView();
            mView = null;
        }

        mArguments.clear();

        synchronized (Pane.class) {
            sGeneratedIds.remove(mId);
        }

        mIsPinned                  = false;
        mIsSelected                = false;
        mHasPerformedCreateView    = false;
        mHasPerformedOnViewCreated = false;
        ILog.debug(getClassName(), getTitle() + " onDestroy called. Current state: " + mState);
        backgroundExecutor.shutdownNow();
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    /**
     * Get the name of a {@code Pane} sub-class, typically used when matching subclasses
     *
     * @return the class name
     */
    public String getClassName() {
        return getClass().getSimpleName();
    }

    /**
     * Subclasses override this to release view-specific resources (e.g., listeners, adapters).
     *
     * <p>Called before the pane's root view is set to null.
     */
    protected void onDestroyView() {
        isViewLaidOut = false;
        
        try {
            safelyRemoveLayoutListener(requireView());
        } catch (Exception e) {
            ILog.error("Pane", "Failed to remove layout listener, maybe attached view is null:", e);
        }
    }

    /**
     * Get the JSON arguments for the pane.
     *
     * @return The JSON representation of the pane arguments.
     */
    public final String getArgumentString() {
        return GSON.toJson(getArguments());
    }

    public Map<String, Object> getArguments() {
        Map<String, Object> copy = new LinkedHashMap<>();

        // Metadata
        copy.put(KEY_UUID, mId != null ? mId.toString() : null);
        copy.put(KEY_TITLE, mTitle);
        copy.put(KEY_PINNED, mIsPinned);
        copy.put(KEY_SELECTED, mIsSelected);
        copy.put(KEY_PANE_VERSION, PANE_VERSION);
        copy.put(KEY_CLASS_NAME, getClass().getSimpleName());
        copy.put(KEY_CANONICAL_NAME, getClass().getCanonicalName());

        // Copy user arguments (excluding metadata keys)
        for (Entry<String, Object> entry : mArguments.entrySet()) {
            String entryKey = entry.getKey();
            if (entryKey != null && !isReservedKey(entryKey)) {
                copy.put(entryKey, entry.getValue());
            }
        }
        return copy;
    }

    private boolean isReservedKey(String key) {
        return key.equals(KEY_UUID) || key.equals(KEY_TITLE) || key.equals(KEY_PINNED)
            || key.equals(KEY_SELECTED) || key.equals(KEY_PANE_VERSION)
            || key.equals(KEY_CLASS_NAME) || key.equals(KEY_CANONICAL_NAME);
    }

    /**
     * Get the value associated with the specified key within the pane arguments.
     *
     * @param key The key to identify the saved state.
     * @return The value associated with the specified key, or {@code null} if not found.
     */
    public Object getArgumentValue(@NonNull String key) {
        return getArguments().get(key);
    }

    public AssetManager getAssets() {
        return requireContext().getAssets();
    }

    /**
     * Retrieves the current state of the pane.
     *
     * @return The current state of the pane.
     */
    public PaneState getState() {
        return mState;
    }

    /**
     * Return a localized string from the application's package's default string table.
     *
     * <p>For more insight see {@link Context#getString(int)}
     *
     * @param resId Resource id for the string
     */
    @NonNull
    public final String getString(@StringRes int resId) {
        return requireContext().getString(resId);
    }

    /**
     * Return a localized formatted string from the application's package's default string table.
     *
     * <p>For more insight see {@link Context#getString(int, Object...)}
     *
     * @param resId      Resource id for the format string
     * @param formatArgs The format arguments that will be used for substitution.
     */
    @NonNull
    public final String getString(@StringRes int resId, Object... formatArgs) {
        return requireContext().getString(resId, formatArgs);
    }

    public final String getTID() {
        return mTitle.isEmpty() ? "NO-TITLE" : mTitle + "{" + mId.toString() + "}";
    }

    @Nullable
    public UUID getUUID() {
        return mId;
    }

    public void setUUID(UUID id) {
        this.mId = id;
    }

    /**
     * Checks if the view associated with the pane has invoked #createView().
     *
     * @return {@code true} if the #createView() has been called, {@code false} otherwise.
     * @see #createView()
     */
    public boolean hasPerformedCreateView() {
        return mHasPerformedCreateView;
    }

    /**
     * Checks if the view associated with the pane is created.
     *
     * @return {@code true} if the view is created, {@code false} otherwise.
     * @see #onViewCreated(View);
     */
    public boolean hasPerformedOnViewCreated() {
        return mHasPerformedOnViewCreated;
    }

    public boolean isPinned() {
        return mIsPinned;
    }

    public void setPinned(boolean pinned) {
        mIsPinned = pinned;
    }

    public boolean isSelected() {
        return this.mIsSelected;
    }

    public void setSelected(boolean select) {
        this.mIsSelected = select;
    }

    /**
     * Called when the pane is reselected.
     *
     * <p>Subclasses override this to implement reselection logic
     */
    public void onReselected() {
        // No-op
    }

    public void onSelected() {
        PaneState current = mState;
        if (current == null) return;

        if (current == PaneState.PAUSED) {
            onResume();
        } else {
            mState = PaneState.STARTED;
        }
        mIsSelected = true;
    }

    /**
     * Transitions the pane to the RESUMED state.
     *
     * @throws IllegalStateException If current state is not {@link PaneState#PAUSED}
     */
    protected void onResume() {
        if (mState != PaneState.PAUSED) {
            throw new IllegalStateException("Cannot resume from state: " + mState);
        }
        mState = PaneState.RESUMED;
    }

    /**
     * Called when the pane is reselected.
     *
     * <p>Subclasses override this to further implement unselection logic.
     */
    public void onUnselected() {
        mIsSelected = false;
        onPause();
    }

    protected void onPause() {
        if (mState == PaneState.DESTROYED) {
            throw new IllegalStateException("Cannot pause a destroyed pane");
        }
        mState = PaneState.PAUSED;
    }

    /**
     * Invoke on UI thread to persists the state of the pane.
     *
     * <p>Subclasses should override this method to implement custom persistence behavior.
     *
     * @throws IllegalStateException if pane was destroyed or if pane has not created its view.
     */
    public void persist() {
        if (mState == PaneState.DESTROYED) {
            throw new IllegalStateException("Cannot persist a destroyed pane");
        }

        if (!mHasPerformedCreateView) {
            throw new IllegalStateException(
                getClass().getSimpleName() + " did not invoke createView()");
        }
    }

    public void runOnBackgroundThread(Runnable runnable) {
        backgroundExecutor.submit(runnable);
    }

    public void runOnUiThread(Runnable runnable) {
        requireActivity().runOnUiThread(runnable);
    }

    /**
     * Return the {@link FragmentActivity} this pane is currently associated with.
     *
     * @throws IllegalStateException if not currently associated with an activity or if associated
     *                               only with a context.
     * @see #getActivity()
     */
    @NonNull
    public final FragmentActivity requireActivity() {
        FragmentActivity activity = getActivity();
        if (activity == null) {
            throw new IllegalStateException(this + " not attached to an activity");
        }
        return activity;
    }

    /**
     * Return the {@link FragmentActivity} this fragment is currently associated with. May return
     * {@code null} if the fragment is associated with a {@link Context} instead.
     *
     * @see #requireActivity()
     */
    @Nullable
    public final FragmentActivity getActivity() {
        Context c = getContext();

        if (c instanceof FragmentActivity activity) {
            return activity;
        } else {
            return null;
        }
    }

    public JSONObject serialize() {
        try {
            JSONObject json = new JSONObject();
            JSONObject argJson = new JSONObject();

            for (Entry<String, Object> entry : getArguments().entrySet()) {
                String key = entry.getKey();
                if (key != null) argJson.put(key, entry.getValue());
            }

            json.put(KEY_ARGUMENTS, argJson);

            return json;
        } catch (JSONException e) {
            throw new PaneAccessException("Pane serialization failed", e);
        }
    }

    /**
     * Calls a method with no parameters in the associated fragment.
     *
     * @param fragmentTag Tag identifying the fragment
     * @param methodName  Name of the method to invoke
     * @see #callFragmentMethod(String, String, Object[])
     */
    protected void callFragmentMethod(String fragmentTag, String methodName) {
        callFragmentMethod(fragmentTag, methodName, (Object[]) null);
    }

    /**
     * Calls a method in the associated fragment, auto-detecting parameter types from arguments.
     *
     * <p><b>Note:</b> Null arguments are not supported. Use {@link #callFragmentMethod(String,
     * String, Class[], Object[])} for methods requiring null parameters.
     *
     * @param fragmentTag Tag identifying the fragment
     * @param methodName  Name of the method to invoke
     * @param args        Non-null arguments (types auto-detected)
     * @throws IllegalArgumentException If any argument is null
     */
    protected void callFragmentMethod(String fragmentTag, String methodName, Object... args) {
        callFragmentMethod(fragmentTag, methodName, getArgumentsTypes(args), args);
    }

    /**
     * Calls a method in the associated fragment with explicit parameter types.
     *
     * <p>Use this overload when:
     *
     * <ul>
     *   <li>Method has null arguments
     *   <li>Primitive types need explicit boxing (e.g., int → Integer.class)
     * </ul>
     *
     * <pre>{@code
     * class MyFragment extends Fragment {
     *   public static final String TAG = "MyFragment";
     *   public void exampleMethod(String name, Integer age) {
     *     //...
     *   }
     * }
     * }</pre>
     * <p>
     * Example usage:
     *
     * <pre>{@code
     * // Normal call
     * Class<?>[] paramTypes = new Class<?>[]{String.class, Integer.class};
     * Object[] args = new Object[]{"In Memory of Dr. Peter Umoren Asanga", 65};
     * callFragmentMethod(MyFragment.TAG, "exampleMethod", paramTypes, args);
     * }</pre>
     *
     * <pre>{@code
     * // Call with null
     * Class<?>[] paramTypes = new Class<?>[]{String.class, Integer.class};
     * Object[] args = new Object[]{"I love you Dad", null};
     * callFragmentMethod(MyFragment.TAG, "exampleMethod", paramTypes, args);
     * }</pre>
     *
     * <pre>{@code
     * // Call for methods with no parameters
     * callFragmentMethod(MyFragment.TAG, "exampleMethod", new Class<?>[0], null);
     * }</pre>
     *
     * @param fragmentTag Tag identifying the fragment.
     * @param methodName  Name of the method to invoke.
     * @param argsTypes   Array of parameter types (e.g., String.class, Integer.class)
     * @param args        Arguments (may include nulls)
     * @throws IllegalArgumentException if something went wrong.
     */
    protected void callFragmentMethod(String fragmentTag, String methodName, Class<?>[] argsTypes,
        Object... args) {
        FragmentActivity activity = requireActivity();
        Fragment fragment = activity.getSupportFragmentManager().findFragmentByTag(fragmentTag);

        if (fragment == null) {
            throw new IllegalArgumentException("Fragment with tag '" + fragmentTag + "' not found");
        }

        if (methodName == null) {
            throw new IllegalArgumentException("Method name cannot be null");
        }

        if (fragment.isDetached() || !fragment.isAdded()) {
            throw new IllegalStateException(
                "Fragment not attached, cannot call method: " + methodName);
        }

        String fN = fragment.getClass().getSimpleName();

        try {
            Method method = fragment.getClass().getMethod(methodName, argsTypes);
            method.invoke(fragment, args);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Method '" + methodName + "' not found in " + fN, e);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException(
                "Method '" + methodName + "' is not accessible in " + fN, e);
        } catch (InvocationTargetException e) {
            throw new IllegalArgumentException("Method '" + methodName + "' in fragment " + fN
                + "threw an exception", e.getCause());
        } catch (SecurityException e) {
            throw new IllegalArgumentException(
                "Security violation accessing '" + methodName + "' in " + fN, e);
        } catch (ExceptionInInitializerError e) {
            throw new IllegalArgumentException("Class initialization failed for " + fN, e);
        }
    }

    private Class<?>[] getArgumentsTypes(Object[] args) {
        if (args == null) return new Class<?>[0];

        Class<?>[] argTypes = new Class<?>[args.length];
        for (int i = 0; i < args.length; i++) {
            if (args[i] == null) {
                throw new IllegalArgumentException(
                    "Argument " + i + " is null. Use callFragmentMethod() with explicit types.");
            }
            argTypes[i] = args[i].getClass();
        }
        return argTypes;
    }

    public enum PaneState {
        /**
         * Invalid state not yet created.
         */
        INITIALIZING,

        /**
         * Initialized and view created.
         */
        CREATED,

        /**
         * Initialized, created, and selected.
         */
        STARTED,

        /**
         * Created and paused.
         */
        PAUSED,

        /**
         * Created, started, and resumed.
         */
        RESUMED,

        /**
         * Created and destroyed.
         */
        DESTROYED
    }

    public static final class PaneConstants {

        public static final String TEXT_PANE_ARGUMENT_KEY = "content:text-pane";
        public static final String EDITOR_PANE_ARGUMENT_KEY = "content:editor-pane";

        private PaneConstants() {
            // Hide
        }
    }

    @Override
    public final boolean equals(@Nullable Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        // Much is not known about a pane subclass, hence java.util.UUID was considered for
        // uniqueness
        Pane otherPane = (Pane) other;
        return Objects.equals(mId, otherPane.mId) && mId != null;
    }

    @Override
    public final int hashCode() {
        return Objects.hash(mId);
    }

    @NonNull
    @Override
    public String toString() {
        return String.format(Locale.ENGLISH, "%s@%s (hashCode=%d, uuid=%s, title=%s, pinned=%s)",
            getClass().getSimpleName(), Integer.toHexString(System.identityHashCode(this)),
            hashCode(),
            mId != null ? mId : "null", mTitle, mIsPinned);
    }
}