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
 
   package com.eup.codeopsstudio.pane;

import android.content.Context;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import com.google.gson.Gson;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.UUID;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A Pane is a dynamic user interface element designed for integration into various parts of an
 * application's user interface, providing a flexible and dynamic interaction paradigm.
 *
 * <p>Major extensions of this Pane class include:
 *
 * <ul>
 *   <li>{@link TextPane}
 *   <li>{@link EditorPane}
 *   <li>{@link FragmentPane}
 * </ul>
 *
 * <p>This abstract Pane class serves as a fundamental building block within the WeStudio Code
 * Editor's tabbed system, representing an individual tabbed interface element that manages a
 * workspace for editing and displaying code and content.
 *
 * <p>This class provides a foundation for custom pane implementations and includes methods to
 * create, manage, and interact with panes. Always call {@code createView()} before interacting with
 * a pane to prevent crashes. Note that this class does not support it's own thread, integrate a thread or leverage @link ComponentActivity.runOnUiThread(Runnable)
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
 *         <li>Added support for persisting pane contents .
 *       </ul>
 * </ul>
 *
 * @see TextPane
 * @see EditorPane
 * @see FragmentPane
 * @author EUP
 * @version 0.4
 */
public abstract class Pane {

  /** Enum for pane states. */
  // TODO: Migrate enum pane state to static final int representation
  public enum PaneState {
    /** Invalid state used as a null value. */
    INVALID_STATE,

    /** Not yet created. */
    INITIALIZING,

    /** Initialized and (view) created. */
    CREATED,

    /** Initialized, created, and selected. */
    STARTED,

    /** Created and paused. */
    PAUSED,

    /** Created, started, and resumed. */
    RESUMED,

    /** Created and destroyed. */
    DESTROYED
  }

  // The current state of this pane
  PaneState mState = PaneState.INVALID_STATE;

  // The title of the pane, typically used as the tab name
  String mTitle;

  // Indicates whether the pane is pinned
  boolean mPinned;

  // The context in which this pane is operating
  Context mContext;

  // The view generated for this pane
  View mView;

  // The unique identifier for this pane
  UUID mUUID;

  // The tag of the pane, typically used when matching subclasses
  public final String TAG = getClass().getSimpleName();

  // Indicates if the view for this pane has been created
  protected boolean hasPerformedOnViewCreated = false;

  /**
   * Indicates if this pane is currently selected. This has nothing to do with the lifecycle
   *
   * @return true if this pane has currently invoked #onSelected
   * @see #onSelected()
   */
  protected boolean isSelected = false;

  // Indicates whether createView() method has been executed for this pane
  protected boolean hasPerformedCreateView = false;

  // List to store generated UUIDs for this pane
  final List<UUID> generatedIDS = new ArrayList<>();

  // keeps track of all pane arguments
  final HashMap<String, Object> mArguments = new HashMap<>();

  public Pane(Context context, String title) {
    this(context, title, true);
  }

  /**
   * Constructor for initializing a Pane.
   *
   * @param context The application context.
   * @param title The title of the Pane.
   */
  public Pane(Context context, String title, boolean generateUUID) {
    mContext = context;
    mTitle = title;
    mState = PaneState.INITIALIZING;
    mPinned = false;
    if (generateUUID) {
      generateUUID();
    }
  }

  /**
   * Get the title of this Pane.
   *
   * @return The title of the Pane.
   */
  public String getTitle() {
    return mTitle;
  }

  /**
   * Set the title of this pane
   *
   * @param title the title of this pane
   */
  public void setTitle(String title) {
    mTitle = title;
  }

  /**
   * Check if the Pane is pinned.
   *
   * @return True if the Pane is pinned, false otherwise.
   */
  public boolean isPinned() {
    return mPinned;
  }

  /**
   * Set whether the Pane is pinned.
   *
   * @param pinned True to pin the Pane, false to unpin it.
   */
  public void setPinned(boolean pinned) {
    mPinned = pinned;
  }

  public boolean isSelected() {
    return this.isSelected;
  }

  public void setSelected(boolean select) {
    this.isSelected = select;
  }

  /**
   * Return the {@link Context} this pane is currently associated with.
   *
   * @see #requireContext()
   */
  @Nullable
  public Context getContext() {
    if (mContext == null) {
      return null;
    } else {
      return mContext;
    }
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
    if (context == null) {
      throw new IllegalStateException("Pane " + this + " not attached to a initalized.");
    }
    return context;
  }

  /**
   * Return the {@link FragmentActivity} this fragment is currently associated with. May return
   * {@code null} if the fragment is associated with a {@link Context} instead.
   *
   * @see #requireActivity()
   */
  @Nullable
  public final FragmentActivity getActivity() {
    if (mContext == null) {
      return null;
    }
    if (mContext instanceof FragmentActivity) {
      return (FragmentActivity) mContext;
    } else {
      return null;
    }
  }

  /**
   * Return the {@link FragmentActivity} this pane is currently associated with.
   *
   * @throws IllegalStateException if not currently associated with an activity or if associated
   *     only with a context.
   * @see #getActivity()
   */
  @NonNull
  public final FragmentActivity requireActivity() {
    FragmentActivity activity = getActivity();
    if (activity == null) {
      throw new IllegalStateException("Pane " + this + " not attached to an activity.");
    }
    return activity;
  }

  /**
   * Call a method in the associated fragment (if any) without any arguments.
   *
   * @param fragmentTag The fragment tag.
   * @param methodName The name of the method to call.
   */
  protected void callFragmentMethod(String fragmentTag, String methodName) {
    callFragmentMethod(fragmentTag, methodName, (Object[]) null);
  }

  /**
   * Call a method in the associated fragment (if any).
   *
   * @param fragmentTag The fragment tag
   * @param methodName The name of the method to call.
   * @param args Any arguments to pass to the method.
   */
  protected void callFragmentMethod(String fragmentTag, String methodName, Object... args) {
    FragmentActivity activity = getActivity();
    if (activity != null) {
      Fragment fragment = activity.getSupportFragmentManager().findFragmentByTag(fragmentTag);
      if (fragment != null) {
        try {
          Method method = fragment.getClass().getMethod(methodName, getArgumentsTypes(args));
          method.invoke(fragment, args);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
          e.printStackTrace();
        }
      }
    }
  }

  /**
   * Retrieves the types of arguments for the method to be invoked dynamically.
   *
   * @param args The arguments for the method.
   * @return An array containing the types of arguments.
   */
  private Class<?>[] getArgumentsTypes(Object[] args) {
    if (args == null) {
      return new Class<?>[0];
    }
    Class<?>[] argTypes = new Class[args.length];
    for (int i = 0; i < args.length; i++) {
      argTypes[i] = args[i].getClass();
    }
    return argTypes;
  }

  /**
   * Called to have the pane instantiate its user interface.
   *
   * @return the view for the panes UI.
   */
  public View createView() {
    mView = onCreateView();
    if (mView != null) {
      mState = PaneState.CREATED;
      onViewCreated(mView);
      hasPerformedCreateView = true;
    }
    return mView;
  }

  /**
   * Subclasses must override this method to create the view for the pane. This method should return
   * the generated view that represents the user interface of the pane.
   *
   * @return The view representing the user interface of the pane.
   */
  protected abstract View onCreateView();

  /**
   * Called after the {@link #createView()} method to handle post-creation operations on the pane's
   * view. Subclasses can override this method to perform additional setup or customization on the
   * view generated by {@link #createView()}.
   *
   * @param view The view returned by the {@link #createView()} method. Can be {@code null} if the
   *     creation of the view fails. Implementations should check for null to handle such scenarios
   *     gracefully.
   */
  public void onViewCreated(@NonNull View view) {
    // Update the flag indicating whether the view is created
    hasPerformedOnViewCreated = true;
  }

  /**
   * Get the root view for the pane's layout (the one returned by {@link #createView}), if provided.
   *
   * @return The panes's root view, or null if it has no layout.
   */
  @Nullable
  public View getView() {
    return mView;
  }

  /**
   * Get the root view for the pane's layout (the one returned by {@link #createView}).
   *
   * @throws IllegalStateException if no view was returned by {@link #createView}.
   * @see #getView()
   */
  @NonNull
  public final View requireView() {
    View view = getView();
    if (view == null) {
      throw new IllegalStateException(
          "Pane "
              + this
              + " did not return a View from"
              + " createView() or this was called before createView().");
    }
    return view;
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
   * Checks if the view associated with the pane is created.
   *
   * @return {@code true} if the view is created, {@code false} otherwise.
   */
  public boolean hasPerformedOnViewCreated() {
    return hasPerformedOnViewCreated;
  }

  /**
   * Checks if the view associated with the pane has invoked #createView().
   *
   * @return {@code true} if the #createView() has been called, {@code false} otherwise.
   */
  public boolean hasPerformedCreateView() {
    return hasPerformedCreateView;
  }

  /** Sets the state of the pane to CREATED. */
  public void onCreated() {
    mState = PaneState.CREATED;
  }

  /**
   * Called when the pane is selected. Updates the state of the pane to STARTED if it was previously
   * paused, otherwise sets it to STARTED.
   */
  public void onSelected() {
    PaneState current = mState;
    if (current != null && current == PaneState.PAUSED) {
      onResume();
      isSelected = true;
    } else if (mState != null) {
      mState = PaneState.STARTED;
      isSelected = true;
    }
  }

  /** Called when the pane is unselected. Updates the state of the pane to PAUSED. */
  public void onUnselected() {
    isSelected = false;
    onPause();
  }

  /** Called when the pane is reselected. This method is a no-operation (no-op). */
  public void onReselected() {
    // No-op
  }

  /** Pauses the pane by setting its state to PAUSED. */
  protected void onPause() {
    mState = PaneState.PAUSED;
  }

  /** Resumes the pane by setting its state to RESUMED. */
  protected void onResume() {
    mState = PaneState.RESUMED;
  }

  /** Destroys the pane by setting its state to DESTROYED and resetting view-related flags. */
  public void onDestroy() {
    mState = PaneState.DESTROYED;
    hasPerformedCreateView = false;
    hasPerformedOnViewCreated = false;
    isSelected = false;
  }

  /**
   * Persists the state of the pane.
   *
   * <p>Subclasses should override this method to implement custom persistence behavior.
   */
  public void persist() {
    if (!hasPerformedCreateView) {
      throw new IllegalStateException(getClass().getSimpleName() + " did not invoke createView()");
    }
  }

  /**
   * A pane uuid can be anything, may be {@code null}
   *
   * @return The pane uuid
   */
  @Nullable
  public UUID getUUID() {
    return mUUID;
  }

  public void setUUID(UUID id) {
    this.mUUID = id;
  }

  /**
   * Generates a unique UUID for the pane, ensuring it is not a duplicate of any previously
   * generated UUID. If the list of generated UUIDs is empty, the generated UUID is assigned without
   * further checks.
   */
  private void generateUUID() {
    // Generate a new UUID.
    UUID newUUID = UUID.randomUUID();

    // Check if the list of generated UUIDs is empty.
    if (generatedIDS.isEmpty()) {
      // If the list is empty, assign the generated UUID directly.
      this.mUUID = newUUID;
      // Add the UUID to the list of generated UUIDs.
      generatedIDS.add(mUUID);
    } else {
      // If the list is not empty, check if the generated UUID is unique.
      if (!hasDuplicateUUID(newUUID)) {
        // If the new UUID is unique, assign it to the pane's UUID and add it to the list of
        // generated UUIDs.
        this.mUUID = newUUID;
        generatedIDS.add(mUUID);
      } else {
        // If a duplicate UUID is generated, recursively call generateUUID to ensure uniqueness.
        generateUUID();
      }
    }
  }

  /**
   * Checks if the given UUID is already generated for this pane.
   *
   * @param uuid The UUID to check for duplication.
   * @return True if the UUID is already generated, false otherwise.
   */
  private boolean hasDuplicateUUID(UUID uuid) {
    return generatedIDS.contains(uuid);
  }

  /**
   * Return a localized string from the application's package's default string table.
   *
   * @param resId Resource id for the string
   */
  @NonNull
  public final String getString(@StringRes int resId) {
    return mContext.getResources().getString(resId);
  }

  /**
   * Return a localized formatted string from the application's package's default string table,
   * substituting the format arguments as defined in {@link java.util.Formatter} and {@link
   * java.lang.String#format}.
   *
   * @param resId Resource id for the format string
   * @param formatArgs The format arguments that will be used for substitution.
   */
  @NonNull
  public final String getString(@StringRes int resId, Object... formatArgs) {
    return mContext.getResources().getString(resId, formatArgs);
  }

  /**
   * Retrieves the saved state associated with the specified key.
   *
   * @param key The key to identify the saved state.
   * @return The saved state object associated with the specified key, or {@code null} if not found.
   */
  public Object getArgumentValue(@NonNull String key) {
    Object state = null;
    for (Entry<String, Object> entry : mArguments.entrySet()) {
      String entryKey = entry.getKey();
      if (entryKey != null && entryKey.equals(key)) {
        state = entry.getValue();
        break;
      }
    }
    return state;
  }

  /**
   * Get the json arguments for the pane
   *
   * @see PaneState
   * @return The panes's argument
   */
  public final String getArgumentString() {
    return new Gson().toJson(mArguments);
  }

  public final <V> void addArguments(final String key, final V value) {
    mArguments.put(key, value);
  }

  public HashMap<String, Object> getArguments() {
    addArguments("conicalName", getClass().getCanonicalName());
    addArguments("class_name", getClass().getSimpleName());
    addArguments("title", mTitle);
    addArguments("pinned", mPinned);
    addArguments("selected", isSelected);
    addArguments("uuid", mUUID.toString());
    return this.mArguments;
  }

  /**
   * Compares this pane with the specified object for equality. Subclasses cannot override this
   * method.
   *
   * @param obj The object to compare for equality.
   * @return True if the specified object is equal to this pane, false otherwise.
   */
  @Override
  public final boolean equals(@Nullable Object obj) {
    if (this == obj) return true;
    if (obj == null || getClass() != obj.getClass()) return false;
    Pane other = (Pane) obj;
    return Objects.equals(mUUID, other.mUUID);
  }

  /**
   * Returns a hash code value for the pane. Subclasses cannot override this method.
   *
   * @return A hash code value for this pane.
   */
  @Override
  public final int hashCode() {
    return Objects.hash(mUUID);
  }

  /**
   * Returns a string representation of the pane.
   *
   * @return A string representing the pane with its class name, unique identifier, title, and
   *     pinned status.
   */
  @NonNull
  @Override
  public String toString() {
    return String.format(
        "%s@%s (hashCode=%d, uuid=%s, title=%s, pinned=%s)",
        getClass().getSimpleName(),
        Integer.toHexString(System.identityHashCode(this)),
        hashCode(),
        mUUID.toString(),
        mTitle,
        mPinned);
  }

  /**
   * Converts the Pane object to a JSONObject representation.
   *
   * @return JSONObject representing the Pane object.
   */
  public JSONObject toJsonObject() {
    try {
      JSONObject json = new JSONObject();
      json.put("class_name", getClass().getSimpleName());
      json.put("memoryAddress", Integer.toHexString(System.identityHashCode(this)));
      json.put("hashCode", hashCode());
      json.put("uuid", mUUID.toString());
      json.put("title", mTitle);
      json.put("pinned", mPinned);
      json.put("arguments", mArguments);
      return json;
    } catch (JSONException err) {
      err.printStackTrace();
      return null;
    }
  }
}
