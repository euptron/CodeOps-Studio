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

package com.eup.codeopsstudio.ui.editor.code.breadcrumb.model;

import java.io.File;

public class BreadCrumb {

    private String crumbName;
    private File crumbFile;

    public BreadCrumb(String name, File file) {
        this.crumbName = name;
        this.crumbFile = file;
    }

    @Override
    public int hashCode() {
        int result = 18;
        result = 31 * result + (crumbName != null ? crumbName.hashCode() : 0);
        result = 31 * result + (crumbFile != null ? crumbFile.hashCode() : 0);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        BreadCrumb crumb = (BreadCrumb) obj;
        return crumbFile.getAbsolutePath().equals(crumb.getFile().getAbsolutePath());
    }

    public File getFile() {
        return this.crumbFile;
    }

    public void setFile(File file) {
        this.crumbFile = file;
    }

    public static BreadCrumb fileToCrumb(File file) {
        return new BreadCrumb(file.getName(), file);
    }

    public String getFilePath() {
        return this.crumbFile.getAbsolutePath();
    }

    public String getName() {
        return this.crumbName;
    }

    public void setName(String name) {
        this.crumbName = name;
    }
}
