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

package com.codeops.plugin.sdk.model;

import java.util.Objects;

/**
 * Describes a plugin in the most abstracted way possible
 *
 */
public record BasePlugin<C>(C cue, BasePluginManifest pluginManifest) {
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        var currentManifest = pluginManifest();
        var otherManifest = ((BasePlugin<?>) o).pluginManifest();

        if (currentManifest == null) return false;

        return Objects.equals(currentManifest.getId(), otherManifest.getId())
                && Objects.equals(currentManifest.getName(), otherManifest.getName())
                && Objects.equals(currentManifest.getMinAPI(), otherManifest.getMinAPI())
                && Objects.equals(currentManifest.getVersion(), otherManifest.getVersion())
                && Objects.equals(currentManifest.getType(), otherManifest.getType());
    }

    @Override
    public int hashCode() {
        var manifest = pluginManifest();
        if (manifest == null) return 1;

        String id = manifest.getId();
        String name = manifest.getName();
        String minApi = manifest.getMinAPI();
        String version = manifest.getVersion();
        BasePluginType type = manifest.getType();
        return Objects.hash(id, name, minApi, version, type);
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public String toString() {
        var manifest = pluginManifest();
        if (manifest == null) return "BasePlugin{}";

        String id = manifest.getId();
        String name = manifest.getName();
        String minApi = manifest.getMinAPI();
        String version = manifest.getVersion();
        BasePluginType type = manifest.getType();

        return "BasePlugin{"
                + "id="
                + id
                + ", name="
                + name
                + ", minApi="
                + minApi
                + ", version="
                + version
                + ", type="
                + type
                + "}";
    }
}
