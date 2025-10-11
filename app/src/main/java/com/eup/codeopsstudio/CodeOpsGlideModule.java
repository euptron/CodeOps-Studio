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

package com.eup.codeopsstudio;

import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.module.AppGlideModule;

/**
 * Glide Configuration Entry Point.
 *
 * <p>This module configures Glide for the application. It is required to resolve a startup
 * warning from {@code com.google.firebase:firebase-inappmessaging-display}, which uses Glide.
 *
 * <p>The {@code @GlideModule} annotation triggers the generation of {@code
 * GeneratedAppGlideModule},
 * which Glide uses for initialization, preventing the warning.
 *
 * <p><b>IMPORTANT:</b> Do not delete this class. It is crucial for Glide's initialization.
 * Deleting it will lead to warnings and potential performance issues with Firebase features.
 *
 * @author Etido Peter
 */
@GlideModule
public final class CodeOpsGlideModule extends AppGlideModule {
    // Intentionally empty to use Glide defaults.
}