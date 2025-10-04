/*
 * This file is part of CodeOps Studio.
 * CodeOps Studio - Code anywhere anytime
 * https://github.com/euptron/CodeOps-Studio
 * Copyright (C) 2024-2025 Etido Peter
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
 * questions or need additional information. Email: euptron@gmail.com
 */

package com.eup.codeopsstudio.models;

import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Defines a List of actions
 *
 * @author Etido Peter
 */
public class ActionModel {

    private static final List<UUID> generatedIds = new ArrayList<>();
    // unique ID for this class
    private final UUID id;
    private int icon;
    private String title;
    private String summary;
    private Drawable drawable;
    private String buttonText;

    public ActionModel(int icon, String title, String summary, String buttonText) {
        this.icon       = icon;
        this.title      = title;
        this.summary    = summary;
        this.buttonText = buttonText;
        this.id         = generateUUID();
    }

    public ActionModel(int icon, String title, String summary) {
        this(icon, title, summary, null);
    }

    public ActionModel(int icon, String title) {
        this(icon, title, null);
    }

    public ActionModel(String title) {
        this(0, title);
    }

    public ActionModel(Drawable drawable, String title, String summary, String buttonText) {
        this.drawable   = drawable;
        this.title      = title;
        this.summary    = summary;
        this.buttonText = buttonText;
        this.id         = generateUUID();
    }

    public ActionModel(Drawable drawable, String title, String summary) {
        this(drawable, title, summary, null);
    }

    public ActionModel(Drawable drawable, String title) {
        this(drawable, title, null);
    }

    @Override
    public int hashCode() {
        return Objects.hash(buttonText, drawable, summary, title);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ActionModel that = (ActionModel) o;
        return icon == that.icon && Objects.equals(id, that.id) && Objects.equals(title, that.title)
            && Objects.equals(summary, that.summary) && Objects.equals(drawable, that.drawable)
            && Objects.equals(buttonText, that.buttonText);
    }

    @NonNull
    @Override
    public String toString() {
        return "ActionModel{" + "buttonText='" + buttonText + '\'' + ", id=" + id + ", icon=" + icon
            + ", title='" + title + '\'' + ", summary='" + summary + '\'' + ", drawable=" + drawable
            + '}';
    }

    public String getButtonText() {
        return this.buttonText;
    }

    public void setButtonText(String text) {
        this.buttonText = text;
    }

    public Drawable getDrawable() {
        return drawable;
    }

    public void setDrawable(Drawable drawable) {
        this.drawable = drawable;
    }

    public UUID getID() {
        return this.id;
    }

    public int getIcon() {
        return this.icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public String getSummary() {
        return this.summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    protected UUID generateUUID() {
        UUID generatedId = UUID.randomUUID();
        if (isUniqueId(generatedId)) {
            generatedIds.add(generatedId);
            return generatedId;
        } else {
            return generateUUID();
        }
    }

    private boolean isUniqueId(UUID id) {
        return !generatedIds.contains(id);
    }
}
