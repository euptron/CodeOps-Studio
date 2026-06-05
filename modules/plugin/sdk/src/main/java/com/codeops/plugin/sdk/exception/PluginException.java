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

package com.codeops.plugin.sdk.exception;

public class PluginException extends Exception {

    public enum Code {
        /** Manifest file missing, malformed, or failed schema validation. */
        MANIFEST_INVALID,
        /** Plugin type is not supported on this device or API level. */
        TYPE_UNSUPPORTED,
        /** APK signature, hash mismatch, or permission violation. */
        SECURITY_VIOLATION,
        /** Device API level below the plugin's declared minSdk. */
        INCOMPATIBLE_SDK,
        /** Plugin source file not found or unreadable. */
        SOURCE_NOT_FOUND,
        /** Engine failed to initialize (Rhino context, ClassLoader, etc.). */
        ENGINE_INIT_FAILED,
        /** A hook or command execution threw an error. */
        EXECUTION_FAILED,
        /** Plugin exceeded its CPU time or memory budget. */
        RESOURCE_LIMIT_EXCEEDED,
        /** Lifecycle method called in wrong order or on disposed plugin. */
        LIFECYCLE_ERROR,
        /** Catch-all for unexpected internal failures. */
        INTERNAL_ERROR
    }

    private final Code code;

    public PluginException(String message) {
        this(message, Code.INTERNAL_ERROR);
    }

    public PluginException(String message, Code code) {
        this(message, code, null);
    }

    public PluginException(String message, Throwable cause) {
        this(message, null, cause);
    }

    public PluginException(String message, Code code, Throwable cause) {
        super(message, cause);
        this.code = (code == null) ? Code.INTERNAL_ERROR : code;
    }


    public Code getCode() {
        return code;
    }

    @Override
    public String toString() {
        return "PluginException[" + code + "]: " + getMessage();
    }
}
