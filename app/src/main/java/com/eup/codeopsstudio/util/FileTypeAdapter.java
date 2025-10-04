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
 * questions or need additional information. Email: etido.up@gmail.com
 */

package com.eup.codeopsstudio.util;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.File;
import java.io.IOException;

/**
 * @author Etido Peter
 */
public class FileTypeAdapter extends TypeAdapter<File> {
    @Override
    public void write(@NonNull JsonWriter out, File value) throws IOException {
        out.value(value == null ? null : value.getPath());
    }

    @Override
    public File read(@NonNull JsonReader in) throws IOException {
        String path = in.nextString();
        return (path == null || path.isEmpty()) ? null : new File(path);
    }

    public static Gson createFileAwareGson() {
        return new GsonBuilder().registerTypeAdapter(File.class, new FileTypeAdapter()).create();
    }
}
