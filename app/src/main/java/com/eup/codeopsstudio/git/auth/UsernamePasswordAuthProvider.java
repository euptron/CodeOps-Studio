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

package com.eup.codeopsstudio.git.auth;

import androidx.annotation.NonNull;

import com.eup.codeopsstudio.git.UserConfig;

import org.eclipse.jgit.api.TransportCommand;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

public class UsernamePasswordAuthProvider implements AuthProvider {
    private final UserConfig userConfig;

    public UsernamePasswordAuthProvider(@NonNull UserConfig userConfig) {
        this.userConfig = userConfig;
    }

    @Override
    public void configureCommand(@NonNull TransportCommand<?, ?> command) {
        command.setCredentialsProvider(new UsernamePasswordCredentialsProvider(userConfig.userName(), userConfig.password()));
    }
}