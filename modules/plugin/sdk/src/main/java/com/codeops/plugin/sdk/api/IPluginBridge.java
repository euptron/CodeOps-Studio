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

/**
 * The API surface that CodeOps Studio exposes to ALL plugin types.
 *
 * <p>This interface is the single contract between host and plugin — every plugin type (JS, DEX,
 * APK, TCP) accesses host functionality exclusively through this interface.
 *
 * <p>The host implements this once; each engine adapter exposes it in a type-appropriate way (e.g.
 * JS engine injects it as a "codeops" global object; DEX engine passes it as a PluginContext).
 */
public interface IPluginBridge {

    // ── Editor ────────────────────────────────────────────────────────────────

    /** Returns the full text content of the currently active editor buffer. */
    String getText();

    /** Replaces the entire content of the active editor buffer. */
    void setText(String text);

    /** Inserts text at the current cursor position. */
    void insertAtCursor(String text);

    /** Inserts text at a specific character offset. */
    void insertAt(String text, int offset);

    /** Returns the currently selected text, or empty string if none. */
    String getSelectedText();

    /** Replaces the current selection with the given text. */
    void replaceSelection(String text);

    /** Returns the absolute file path of the currently open file. */
    String getCurrentFilePath();

    /** Returns the language identifier e.g. "java", "kotlin", "python". */
    String getLanguageId();

    /** Returns the 1-based line number of the cursor. */
    int getCursorLine();

    /** Returns the 0-based column of the cursor. */
    int getCursorColumn();

    /** Opens a file in the editor by absolute path. */
    void openFile(String path);

    /** Saves the currently active file. */
    void saveCurrentFile();

    // ── File System ───────────────────────────────────────────────────────────

    /**
     * Reads a file's content as a UTF-8 string. Paths are validated against the plugin's sandbox
     * directory.
     */
    String readFile(String path);

    /** Writes UTF-8 content to a file, creating it if necessary. Returns true on success. */
    boolean writeFile(String path, String content);

    /** Lists the filenames inside a directory. Returns empty array if none. */
    String[] listDir(String path);

    /** Returns true if the given path exists. */
    boolean fileExists(String path);

    /** Deletes a file. Returns true on success. */
    boolean deleteFile(String path);

    // ── UI ────────────────────────────────────────────────────────────────────

    /**
     * Shows an Android Toast message on the main thread. This is the simplest UI interaction
     * available to plugins.
     *
     * @param message text to display
     */
    void showToast(String message);

    /**
     * Shows a longer-duration Toast (LENGTH_LONG).
     *
     * @param message text to display
     */
    void showToastLong(String message);

    /**
     * Displays a panel inside the CodeOps UI.
     *
     * @param id unique panel identifier
     * @param title panel header text
     * @param content HTML content rendered inside the panel
     */
    void showPanel(String id, String title, String content);

    /** Closes a panel by its identifier. */
    void dismissPanel(String id);

    /**
     * Sets text in the CodeOps status bar. Prefix with your plugin ID to avoid collisions e.g.
     * "[WordCount] 420 words"
     */
    void setStatusBarText(String text);

    /**
     * Registers a command in the CodeOps command palette.
     *
     * @param commandId unique command identifier e.g. "wordCounter.count"
     * @param label human-readable label shown in the palette
     */
    void registerCommand(String commandId, String label);

    /** Shows a simple OK/Cancel dialog. Returns true if user pressed OK. */
    boolean showDialog(String title, String message);

    // ── Build ─────────────────────────────────────────────────────────────────

    /** Runs a Gradle task asynchronously e.g. "assembleDebug". */
    void runBuildTask(String taskName);

    /** Returns the stdout/stderr output of the last completed build task. */
    String getLastBuildOutput();

    /** Returns true if a build task is currently running. */
    boolean isBuilding();

    // ── Terminal ──────────────────────────────────────────────────────────────

    /**
     * Executes a shell command synchronously. Returns combined stdout + stderr output. Restricted
     * to safe commands — no rm -rf, no su, etc.
     */
    String exec(String command);

    /** Opens the CodeOps terminal panel and runs the given command. */
    void openTerminal(String command);

    // ── Settings ──────────────────────────────────────────────────────────────

    /** Returns a plugin-scoped setting value, or null if not set. */
    Object getSetting(String key);

    /** Persists a plugin-scoped setting. */
    void setSetting(String key, Object value);

    /** Returns true if the setting key exists. */
    boolean hasSetting(String key);

    // ── Logging ───────────────────────────────────────────────────────────────

    /** Logs a debug message tagged with the plugin's ID. */
    void log(String message);

    /** Logs a warning message tagged with the plugin's ID. */
    void logWarn(String message);

    /** Logs an error message tagged with the plugin's ID. */
    void logError(String message);
}
