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
 * questions or need additional information. Email: etido.up@gmail.com
 */

package com.codeops.plugin.sdk.api;

import com.codeops.plugin.sdk.PluginContext;
import com.codeops.plugin.sdk.model.BasePluginDescriptor;
import com.codeops.plugin.sdk.model.BasePluginManifest;
import com.codeops.plugin.sdk.model.BasePluginType;
import com.codeops.plugin.sdk.exception.PluginException;

public interface IPlugin {
    /**
     * @return unique reverse-domain identifier e.g. com.acme.word-counter
     */
    String getId();

    /**
     * @return human-readable display name
     */
    String getName();

    /**
     * @return semantic version string e.g. 1.2.0
     */
    String getVersion();

    /**
     * @return transport/execution type — JS, DEX, APK, TCP, etc.
     */
    BasePluginType getType();

    /**
     * @return full parsed manifest for this plugin
     */
    BasePluginManifest getManifest();

    /**
     * @return runtime state snapshot — used by registry and UI
     */
    BasePluginDescriptor getDescriptor();

    /**
     * Called once after the plugin is loaded and its engine initialized. Equivalent to
     * Activity.onCreate — do one-time set up here.
     *
     * @param context host-provided context giving access to the bridge API
     * @throws PluginException if initialisation fails
     */
    void onCreate(PluginContext context) throws PluginException;

    /** Called when the plugin becomes visible / relevant. Equivalent to Activity.onStart. */
    void onStart() throws PluginException;

    /**
     * Called when the plugin is in the foreground and receiving events. Equivalent to
     * Activity.onResume.
     */
    void onResume() throws PluginException;

    /**
     * Called when the plugin is partially obscured or temporarily inactive. Equivalent to
     * Activity.onPause — save lightweight state here.
     */
    void onPause();

    /**
     * Called when the plugin is no longer visible. Equivalent to Activity.onStop — release heavy
     * resources.
     */
    void onStop();

    /**
     * Called when the plugin is being permanently unloaded. Equivalent to Activity.onDestroy —
     * clean up everything. Must not throw; log and swallow any errors.
     */
    void onDestroy();

    /**
     * Dispatches a named hook to the plugin's execution engine. The engine interprets the hook name
     * and arguments in a type-specific way: - JS engine calls the exported JS function with that
     * name - DEX engine calls the corresponding method on the IPlugin implementation - TCP engine
     * sends a JSON-RPC notification
     *
     * @param hook hook name e.g. "onEditorChange", "onFileSave"
     * @param args arguments passed to the hook handler
     */
    void dispatchHook(String hook, Object... args);

    /**
     * Same as {@link #dispatchHook} but blocks until the hook returns a value. Use for hooks that
     * provide data (completions, diagnostics, etc.).
     *
     * @param hook hook name
     * @param timeoutMs maximum wait time in milliseconds
     * @param args arguments
     * @return hook return value, or null on timeout / error
     */
    Object dispatchHookSync(String hook, long timeoutMs, Object... args);

    /**
     * @return true if plugin code is loaded into memory
     */
    boolean isLoaded();

    /**
     * @return true if plugin is in ACTIVE state and processing events
     */
    boolean isActive();
}
