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

package com.eup.codeopsstudio.ui.explore.template.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.eup.codeopsstudio.common.Constants;

import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * A representation of a project template
 *
 * @author Etido Peter
 */
public class ProjectTemplateModel {

    private String name;
    private String path;
    private String projectType;

    private String author;
    private int versionCode;
    private String versionName;
    private String creationDate;
    private String documentationUrl;
    private String description;

    public ProjectTemplateModel() {
        // Empty constructor
    }

    @Nullable
    public static ProjectTemplateModel fromFile(@NonNull File parent) {
        if (!parent.exists()) {
            return null;
        }

        if (!parent.isDirectory()) {
            return null;
        }

        File infoFile = new File(parent, Constants.PROJECT_TEMPLATE_MODEL_JSON_FILE_PATH);
        if (!infoFile.exists()) {
            return null;
        }

        ProjectTemplateModel template = new ProjectTemplateModel();
        try {
            JSONObject jsonObject = new JSONObject(FileUtils.readFileToString(infoFile,
                StandardCharsets.UTF_8));
            template.setName(jsonObject.getString(Constants.KEY_PROJECT_TEMPLATE_NAME));
            template.setProjectType(jsonObject.getString(Constants.KEY_PROJECT_TEMPLATE_TYPE));
            template.setPath(parent.getAbsolutePath());
            // Template info
            template.setAuthor(jsonObject.getString(Constants.KEY_PROJECT_TEMPLATE_AUTHOR));
            template.setVersion(jsonObject.getInt(Constants.KEY_PROJECT_TEMPLATE_VERSION_CODE));
            template.setVersionName(jsonObject.getString(Constants.KEY_PROJECT_TEMPLATE_VERSION_NAME));
            template.setCreationDate(jsonObject.getString(Constants.KEY_PROJECT_TEMPLATE_CREATION_DATE));
            template.setDocumentationUrl(jsonObject.getString(Constants.KEY_PROJECT_TEMPLATE_DOC_URL));
            template.setDescription(jsonObject.getString(Constants.KEY_PROJECT_TEMPLATE_DESCRIPTION));
            return template;
        } catch (JSONException | IOException e) {
            return null;
        }
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String authorsName) {
        this.author = authorsName;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(String date) {
        this.creationDate = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDocumentationUrl() {
        return documentationUrl;
    }

    public void setDocumentationUrl(String url) {
        this.documentationUrl = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getProjectType() {
        return projectType;
    }

    public void setProjectType(String type) {
        this.projectType = type;
    }

    public int getVersion() {
        return versionCode;
    }

    public void setVersion(int version) {
        this.versionCode = version;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String name) {
        this.versionName = name;
    }
}