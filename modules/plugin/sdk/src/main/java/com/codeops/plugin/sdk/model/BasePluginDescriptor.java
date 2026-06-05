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
 * Describes the current state of a registred/parsed plugin
 *
 * @author euptron
 */
public abstract class BasePluginDescriptor {

    public enum State {
        /** Manifest parsed, not yet loaded into an engine. */
        INSTALLED,
        /** Engine initialized, lifecycle started. */
        ACTIVE,
        /** Temporarily paused (e.g. app backgrounded). */
        PAUSED,
        /** Stopped but not uninstalled — can be restarted. */
        STOPPED,
        /** Fully unloaded, engine disposed. */
        UNLOADED,
        /** Failed to load or crashed at runtime. */
        ERROR
    }

    private final String id;
    private final String name;
    private final String version;
    private final BasePluginType type;
    private State state;
    private String errorMessage;
    private long loadedAtMs;

    protected BasePluginDescriptor(BasePluginManifest manifest) {
        this.id = manifest.getId();
        this.name = manifest.getName();
        this.version = manifest.getVersion();
        this.type = manifest.getType();
        this.state = State.INSTALLED;
    }

    public String getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getVersion() {
        return this.version;
    }

    public BasePluginType getType() {
        return this.type;
    }

    public State getState() {
        return this.state;
    }

    public void setState(State state) {
        this.state = state;
        if (state == State.ACTIVE) this.loadedAtMs = System.currentTimeMillis();
    }

    public String getErrorMessage() {
        return this.errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.state = State.ERROR;
        this.errorMessage = errorMessage;
    }

    public long getLoadedAtMs() {
        return this.loadedAtMs;
    }

    public boolean isActive() {
        return state == State.ACTIVE;
    }

    public boolean isInError() {
        return state == State.ERROR;
    }

    @Override
    public String toString() {
        return "BasePluginDescriptor[id="
                + id
                + ", name="
                + name
                + ", version="
                + version
                + ", type="
                + type
                + ", state="
                + state
                + ", errorMessage="
                + errorMessage
                + ", loadedAtMs="
                + loadedAtMs
                + "]";
    }
}
