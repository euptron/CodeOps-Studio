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
 * questions or need additional information. Email: euptron@gmail.com
 */

package com.eup.codeopsstudio.util;

import androidx.annotation.NonNull;

import com.eup.codeopsstudio.common.ILog;

import org.apache.commons.io.IOUtils;
import org.mozilla.universalchardet.UniversalDetector;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * A class to detect the encoding of a file
 *
 * <p>In CodeOps Studio a file is opened using it's own encoding as detected by the
 * {@link EncodingDetector} if the detector fails it default to UTF-8.
 * Making this useful for editing
 * files as it is and saving files universally using a single charset.
 * </p>
 *
 * @author Etido Peter
 */
public class EncodingDetector {

    public static final String LOG_TAG = "EncodingDetector";
    private static final String DEF_FILE_ENCODING_ERR = "Error detecting file encoding";

    private EncodingDetector() {
        // default
    }

    public static Charset detectFileEncoding(byte[] buffer, final int bytesRead) {
        Charset foundCharSet = StandardCharsets.UTF_8; // default charset
        try {
            UniversalDetector detector = new UniversalDetector();
            detector.handleData(buffer, 0, bytesRead);
            detector.dataEnd();
            String encoding = detector.getDetectedCharset();
            if (encoding != null) {
                foundCharSet = findEncoding(encoding.trim());
            }
            detector.reset();
        } catch (Exception e) {
            ILog.error(LOG_TAG, DEF_FILE_ENCODING_ERR, e);
        }
        return foundCharSet;
    }

    public static Charset findEncoding(String charsetDef) {
        try {
            return Charset.forName(charsetDef);
        } catch (RuntimeException e) {
            ILog.error(LOG_TAG, Objects.requireNonNull(e.getMessage()));
        }
        ILog.debug(LOG_TAG,
            "No matching encoding found for " + charsetDef + ". Using default charset.");
        return StandardCharsets.UTF_8;
    }

    public static Charset detectFileEncoding(int size, File file) {
        Charset charSet = StandardCharsets.UTF_8;
        try {
            charSet = detectFileEncoding(size, new FileInputStream(file));
        } catch (FileNotFoundException e) {
            ILog.error(LOG_TAG, DEF_FILE_ENCODING_ERR, e);
        }
        return charSet;
    }

    public static Charset detectFileEncoding(int size, @NonNull InputStream in) {
        byte[] buffer = new byte[size];
        Charset foundCharSet = StandardCharsets.UTF_8;
        try {
            UniversalDetector detector = new UniversalDetector();
            int bytesRead;
            while ((bytesRead = in.read(buffer)) > 0 && !detector.isDone()) {
                detector.handleData(buffer, 0, bytesRead);
            }
            detector.dataEnd();
            String encoding = detector.getDetectedCharset();
            if (encoding != null) {
                foundCharSet = findEncoding(encoding.trim());
            }
            detector.reset();
        } catch (IOException e) {
            ILog.error(LOG_TAG, DEF_FILE_ENCODING_ERR, e);
        } finally {
            IOUtils.closeQuietly(in);
        }
        return foundCharSet;
    }

    public static Charset getEncoding(String charsetDef) {
        var availableCharsets = Charset.availableCharsets();
        for (Map.Entry<String, Charset> entry : availableCharsets.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(charsetDef)) {
                Charset charset = entry.getValue();
                if (charset.isRegistered()) {
                    ILog.debug(LOG_TAG, "Mapped encoding " + charsetDef + " to charset " + charset);
                    return charset;
                }
            }
        }
        ILog.debug(LOG_TAG,
            "No matching encoding found for " + charsetDef + ". Using default charset.");
        return StandardCharsets.UTF_8;
    }

    @NonNull
    public static List<String> getSupportedEncodings() {
        List<String> list = new ArrayList<>();
        for (Charset charset : getSupportedCharsets()) {
            String name = charset.name();
            list.add(name);
        }
        return Collections.unmodifiableList(list);
    }

    public static boolean isSupportedEncoding(Charset charset) {
        return getSupportedCharsets().contains(charset);
    }

    @NonNull
    public static Collection<Charset> getSupportedCharsets() {
        List<Charset> list = new ArrayList<>();
        for (Charset charset : Charset.availableCharsets().values()) {
            if (charset.isRegistered()) {
                list.add(charset);
            }
        }
        return Collections.unmodifiableList(list);
    }
}
