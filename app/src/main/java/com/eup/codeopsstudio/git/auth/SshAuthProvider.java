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

package com.eup.codeopsstudio.git.auth;

import androidx.annotation.NonNull;

import com.eup.codeopsstudio.git.RepoConfig;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;

import org.eclipse.jgit.api.TransportCommand;
import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.SshTransport;
import org.eclipse.jgit.util.FS;

import java.io.File;
import java.util.Objects;

public class SshAuthProvider implements AuthProvider {
    private final File sshKey;
    private final String passphrase;

    public SshAuthProvider(@NonNull RepoConfig config) {
        this.sshKey     = Objects.requireNonNull(config.getSshKey(), nullMsg());
        this.passphrase = Objects.requireNonNull(config.getPassphrase(), nullMsg());
    }

    @NonNull
    private String nullMsg() {
        return "Either one or both constructor parameters are null";
    }

    @Override
    public void configureCommand(@NonNull TransportCommand<?, ?> command) {
        JschConfigSessionFactory sessionFactory = new JschConfigSessionFactory() {
            @Override
            protected void configure(org.eclipse.jgit.transport.OpenSshConfig.Host host,
                com.jcraft.jsch.Session session) {
                //no-op
            }

            @Override
            protected JSch createDefaultJSch(FS fs) throws JSchException {
                JSch defaultJSch = super.createDefaultJSch(fs);
                if (passphrase != null) {
                    defaultJSch.addIdentity(sshKey.getAbsolutePath(), passphrase);
                } else {
                    defaultJSch.addIdentity(sshKey.getAbsolutePath());
                }
                return defaultJSch;
            }
        };

        command.setTransportConfigCallback(transport -> {
            if (transport instanceof SshTransport sshTransport) {
                sshTransport.setSshSessionFactory(sessionFactory);
            }
        });
    }
}