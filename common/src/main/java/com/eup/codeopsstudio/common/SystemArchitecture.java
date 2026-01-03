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

package com.eup.codeopsstudio.common;

import android.os.Build;

import androidx.annotation.NonNull;

import java.util.Arrays;

/**
 * @author Etido Peter
 */
public final class SystemArchitecture {
    public static final String DEVICE_ARCHITECTURE_NOT_SUPPORTED = "Device Not Supported";

    private static final String ARM = "armeabi-v7a";
    private static final String AARCH64 = "arm64-v8a";
    private static final String I686 = "x86";
    private static final String X86_64 = "x86_64";

    @NonNull
    public static String getArchitecture() {
        if (isSupportedArch()) {
            if (supportsArm32Bit()) {
                return ARM;
            } else if (supportsArm64Bit()) {
                return AARCH64;
            } else if (supportsX86_32Bit()) {
                return I686;
            } else if (supportsX86_64Bit()) {
                return X86_64;
            }
        }
        return DEVICE_ARCHITECTURE_NOT_SUPPORTED;
    }

    public static boolean isSupportedArch() {
        return supportsArm32Bit() || supportsArm64Bit() || supportsX86_32Bit()
            || supportsX86_64Bit();
    }

    public static boolean supportsArm32Bit() {
        return Arrays.asList(Build.SUPPORTED_ABIS).contains(ARM);
    }

    public static boolean supportsArm64Bit() {
        return Arrays.asList(Build.SUPPORTED_ABIS).contains(AARCH64);
    }

    public static boolean supportsX86_32Bit() {
        return Arrays.asList(Build.SUPPORTED_ABIS).contains(I686);
    }

    public static boolean supportsX86_64Bit() {
        return Arrays.asList(Build.SUPPORTED_ABIS).contains(X86_64);
    }
}