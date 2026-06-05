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
import com.codeops.plugin.sdk.exception.PluginException;
import com.codeops.plugin.sdk.model.BasePluginManifest;

/**
 * Loader contract.
 *
 * Each plugin type has exactly one loader implementation.
 * The TransportRouter iterates registered loaders and delegates
 * to the first one that returns true from {@link #canLoad}.
 */
public interface IPluginLoader {

    /**
     * Returns true if this loader handles the given manifest's type.
     * Must be fast — no I/O, no heavy computation.
     */
    boolean canLoad(BasePluginManifest manifest);

    /**
     * Load and return a fully initialised, lifecycle-ready plugin instance.
     *
     * The implementation is responsible for:
     *   1. Reading the plugin's source / bytecode / package
     *   2. Creating and initialising the appropriate engine
     *   3. Calling engine.loadSource(...)
     *   4. Returning a plugin instance ready for onCreate()
     *
     * Security checks and compatibility checks are handled by the
     * abstract {@link IPluginLoader}
     * base class BEFORE this method is called.
     *
     * @param manifest  parsed, validated manifest
     * @param context   host plugin context
     * @return fully prepared IPlugin instance
     * @throws PluginException if loading fails for any reason
     */
    IPlugin load(BasePluginManifest manifest, PluginContext<?> context) throws PluginException;

    /**
     * Unload a plugin — stop its engine, release resources.
     * Called by LifecycleManager after onStop() / onDestroy().
     *
     * @param plugin  the plugin to unload
     * @throws PluginException if unloading fails
     */
    void unload(IPlugin plugin) throws PluginException;
}

