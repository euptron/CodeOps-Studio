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

package com.eup.codeopsstudio.ui.editor.code.manager;

import android.content.Context;

import androidx.annotation.NonNull;

import com.eup.codeopsstudio.R;
import com.eup.codeopsstudio.common.AsyncTask;
import com.eup.codeopsstudio.common.util.PreferencesUtils;
import com.eup.codeopsstudio.editor.ContextualCodeEditor;
import com.eup.codeopsstudio.models.logger.Logger;
import com.eup.codeopsstudio.util.BinaryFileChecker;
import com.eup.codeopsstudio.util.EncodingDetector;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.function.Consumer;

/**
 * @author Etido Peter
 */
public class FileOperationsManager {
    public static final String TAG = "FileOperationsManager";
    private final Context context;
    private final Logger logger;
    private final ContextualCodeEditor editor;

    public FileOperationsManager(Context context, Logger logger, ContextualCodeEditor editor) {
        this.context = context;
        this.logger  = logger;
        this.editor  = editor;
    }

    public ContextualCodeEditor getEditor() {
        return editor;
    }
    
    public void readFileWithCharset(@NonNull File file, @NonNull Charset charset, @NonNull Runnable onError, @NonNull Consumer<String> onSuccess) {
        AsyncTask.runNonCancelable(() -> {
            try {
                return FileUtils.readFileToString(file, charset);
            } catch (IOException e) {
                throw new RuntimeException("Failed to read file: " + file.getAbsolutePath(), e);
            }
        }, (result, throwable) -> {
            if (throwable != null) {
                logger.e(TAG, "Error reading file: " + throwable.getMessage());
                onError.run();
            } else {
                onSuccess.accept(result);
            }
        });
    }

    public void readFile(@NonNull File file, @NonNull Runnable binaryRunnable,
        @NonNull Consumer<String> result) {
        int bufferSize = PreferencesUtils.getCurrentBufferSize();
        Charset encoding = EncodingDetector.detectFileEncoding(bufferSize, file);
        try {
            if (!EncodingDetector.isSupportedEncoding(encoding) || isBinaryFile(file)) {
                logger.d(TAG,
                    "Unsupported charset detected: " + encoding.name() + " " + "for " + "file "
                        + file.getName());
                binaryRunnable.run();
            }
        } catch (Exception e) {
            logger.e(TAG, e.getMessage());
        }

        if (encoding != null) {
            AsyncTask.runNonCancelable(() -> FileUtils.readFileToString(file, encoding), (content
                , throwable) -> {
                if (throwable != null) {
                    String errorMessage = String.format("%s %s%n%s",
                        context.getString(R.string.alrt_text_parsing_error),
                        file.getAbsolutePath(), throwable.getLocalizedMessage());
                    logger.e(TAG, errorMessage);
                } else {
                    if (content != null) {
                        result.accept(content);
                    }
                }
            });
        }
    }

    private boolean isBinaryFile(File file) throws IOException {
        int bufferSize = PreferencesUtils.getCurrentBufferSize();
        return BinaryFileChecker.isBinaryFile(file, false, bufferSize);
    }

    public void saveEditorContent(File file, String content) throws IOException {
        Charset encoding = EncodingDetector.getEncoding(PreferencesUtils.getDefaultFileEncoding());
        FileUtils.writeStringToFile(file, content, encoding);
    }
}