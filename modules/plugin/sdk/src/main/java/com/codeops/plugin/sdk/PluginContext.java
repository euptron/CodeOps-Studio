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

package com.codeops.plugin.sdk;

import com.codeops.plugin.sdk.api.IPluginBridge;
import com.codeops.plugin.sdk.model.BasePluginManifest;

import java.io.File;

/**
 * Immutable context object passed to every plugin at creation time.
 *
 * <p>Carries the runtime context (e.g. Android Context), the host bridge API, and the plugin's
 * scoped storage directory.
 *
 * <p>The runtime context is abstracted via the generic type {@code T} to avoid direct dependencies
 * on platform-specific classes (like {@code android.content.Context}) within the SDK.
 *
 * <p>Plugins must not store a reference to the Android Context beyond their own lifecycle — use
 * getApplicationContext() only.
 */
public final class PluginContext<T> {

    private final T runtimeContext;
    private final IPluginBridge bridge;
    private final BasePluginManifest manifest;
    private final File storageDir;

    public PluginContext(
            T runtimeContext, IPluginBridge bridge, BasePluginManifest manifest, File storageDir) {
        this.runtimeContext = runtimeContext;
        this.bridge = bridge;
        this.manifest = manifest;
        this.storageDir = storageDir;
    }

    /**
     * The runtime context.
     *
     * @return the platform-specific runtime context (e.g., android.content.Context)
     */
    public T getRuntimeContext() {
        return runtimeContext;
    }

    /**
     * The full host plugin API bridge.
     *
     * <p>This is used by non-application plugins to access global objects within the host
     * application.
     *
     * @return the plugin bridge
     */
    public IPluginBridge getBridge() {
        return bridge;
    }

    /** The manifest of the plugin that owns this context. */
    public BasePluginManifest getManifest() {
        return manifest;
    }

    /**
     * Plugin-scoped private storage directory. Guaranteed to exist and be writable. e.g.
     * /data/data/com.euptron.codeops/files/plugins/com.acme.myplugin/
     */
    public File getStorageDir() {
        return storageDir;
    }

    /** Convenience — the plugin's ID from the manifest. */
    public String getPluginId() {
        return manifest.getId();
    }

    /**
     * A provider interface for abstracting the retrieval of the runtime context. This allows other
     * classes to return the context object without the SDK calling platform-specific APIs
     * explicitly.
     */
    public interface MockContext<T> {
        T get();
    }
}
