/*************************************************************************
 * This file is part of CodeOps Studio.
 * CodeOps Studio - code anywhere anytime
 * https://github.com/etidoUP/CodeOps-Studio
 * Copyright (C) 2024 EUP
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
 * If you have more questions, feel free to message EUP if you have any
 * questions or need additional information. Email: etido.up@gmail.com
 *************************************************************************/
 
   package com.eup.codeopsstudio.ui.explore.template.models;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;

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

  public static ProjectTemplateModel fromFile(File parent) {
    if (!parent.exists()) {
      return null;
    }

    if (!parent.isDirectory()) {
      return null;
    }

    File infoFile =
        new File(
            parent,
            File.separator
                + ".oxide"
                + File.separator
                + "template"
                + File.separator
                + "info.json");
    if (!infoFile.exists()) {
      return null;
    }

    ProjectTemplateModel template = new ProjectTemplateModel();
    try {
      JSONObject jsonObject =
          new JSONObject(FileUtils.readFileToString(infoFile, StandardCharsets.UTF_8));
      template.setName(jsonObject.getString("name"));
      template.setProjectType(jsonObject.getString("projectType"));
      template.setPath(parent.getAbsolutePath());
      // Template info
      template.setAuthor(jsonObject.getString("author"));
      template.setVersion(jsonObject.getInt("versionCode"));
      template.setVersionName(jsonObject.getString("versionName"));
      template.setCreationDate(jsonObject.getString("creation_date"));
      template.setDocumentationUrl(jsonObject.getString("doc_url"));
      template.setDescription(jsonObject.getString("description"));
      return template;
    } catch (JSONException | IOException e) {
      return null;
    }
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

  public String getAuthor() {
    return author;
  }

  public void setAuthor(String authorsName) {
    this.author = authorsName;
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

  public String getCreationDate() {
    return creationDate;
  }

  public void setCreationDate(String date) {
    this.creationDate = date;
  }

  public String getDocumentationUrl() {
    return documentationUrl;
  }

  public void setDocumentationUrl(String url) {
    this.documentationUrl = url;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }
}
