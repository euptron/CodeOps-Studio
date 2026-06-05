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

/**
 * Plugins are divided into :
 *
 * <ul>
 *   <li>Application
 *   <li>Compressed.
 * </ul>
 *
 * <p>This means is application plugins runs only when installed, they can be headless or
 * interactive in nature. Furthermore, to support other kinds of plugins we use a compressed file
 * (.cosp) to encapsulate the runtime, binaries, metadata and other properties of non-application
 * plugins.
 */
public enum BasePluginType {
    APPLICATION("application") {
        @Override
        public String extension(String ext) {
            return ".apk";
        }
    },
    JAR("jar") {
        @Override
        public String extension(String ext) {
            return ".cosp";
        }
    },
    DEX("dex") {
        @Override
        public String extension(String ext) {
            return ".cosp";
        }
    },
    JSON("json") {
        @Override
        public String extension(String ext) {
            return ".cosp";
        }
    },
    SCRIPT("js") {
        @Override
        public String extension(String ext) {
            return ".cosp";
        }
    },
    UNKNOWN("unknown") {
        @Override
        public String extension(String ext) {
            return null;
        }
    };

    private final String extension;

    BasePluginType(String extension) {
        this.extension = extension;
    }

    @Override
    public String toString() {
        return extension;
    }

    public abstract String extension(String ext);
}
