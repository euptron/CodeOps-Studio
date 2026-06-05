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
import com.codeops.plugin.sdk.model.BasePluginType;

/**
 * Execution engine contract.
 *
 * Each plugin type has exactly one engine implementation:
 *   JAVASCRIPT  → JSPluginEngine   (Rhino)
 *   DEX         → DexPluginEngine  (DexClassLoader reflection)
 *   APK         → ApkPluginEngine  (AIDL / Messenger)
 *   TCP         → TcpPluginEngine  (JSON-RPC socket)
 *   WASM        → WasmPluginEngine (Wasm runtime)
 *
 * Engines are owned by their plugin instance and must never be shared.
 */
public interface IPluginEngine {

    /**
     * Initialise the engine for a specific plugin.
     * Called once before the first {@link #execute} call.
     *
     * @param context  host context providing bridge API and resources
     * @throws PluginException if the engine cannot be initialised
     */
    void initialize(PluginContext<?> context) throws PluginException;

    /**
     * Execute a named hook asynchronously (fire-and-forget).
     *
     * @param hook  hook name
     * @param args  arguments
     * @throws PluginException if the hook throws or the engine is not ready
     */
    void execute(String hook, Object... args) throws PluginException;

    /**
     * Execute a named hook synchronously and return its result.
     *
     * @param hook       hook name
     * @param timeoutMs  max wait time in milliseconds
     * @param args       arguments
     * @return the hook's return value, or null on timeout / missing hook
     * @throws PluginException if execution fails
     */
    Object executeSync(String hook, long timeoutMs, Object... args) throws PluginException;

    /**
     * Load the plugin's source or bytecode into the engine.
     * Called once after {@link #initialize}.
     *
     * @param source  type-specific: JS string, DEX path, APK package name, etc.
     * @throws PluginException if the source cannot be loaded or parsed
     */
    void loadSource(String source) throws PluginException;

    /**
     * Cleanly dispose of all engine resources.
     * Must be idempotent — safe to call multiple times.
     */
    void dispose();

    /** Returns the plugin type this engine handles. */
    BasePluginType getSupportedType();

    /** Returns true if the engine has been initialised and source loaded. */
    boolean isReady();
}
