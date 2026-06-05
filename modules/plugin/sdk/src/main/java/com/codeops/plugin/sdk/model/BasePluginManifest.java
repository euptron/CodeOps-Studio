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

package com.codeops.plugin.sdk.model;

import com.codeops.plugin.sdk.api.IPermission;
import com.codeops.plugin.sdk.api.IPlugin;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BasePluginManifest {

    private final String id;
    private final String name;
    private final String minAPI;
    private final String version;
    private final BasePluginType type;
    // Optional
    private final long size;
    private  final String icon;
    private final String url;
    private final String description;
    private final String category;
    private final String changeNotes;
    private final String vendor;
    private final String vendorEmail;
    private final String vendorUrl;
    private final long releaseDate;
    private final long lastUpdated;
    private final String packageName;
    private final boolean isApplication;
    private final boolean isRestartRequired;
    private final boolean isLicenseOptional;
    private final List<IPlugin> dependencies;
    private final List<IPermission<?>> permissions;
    private final List<String> architectures;

    public BasePluginManifest(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.minAPI = builder.minAPI;
        this.version = builder.version;
        this.type = builder.type;
        this.size = builder.size;
        this.icon = builder.icon;
        this.url = builder.url;
        this.description = builder.description;
        this.category = builder.category;
        this.changeNotes = builder.changeNotes;
        this.vendor = builder.vendor;
        this.vendorEmail = builder.vendorEmail;
        this.vendorUrl = builder.vendorUrl;
        this.releaseDate = builder.releaseDate;
        this.lastUpdated = builder.lastUpdated;
        this.packageName = builder.namespace;
        this.isApplication = builder.isApplication;
        this.isRestartRequired = builder.isRestartRequired;
        this.isLicenseOptional = builder.isLicenseOptional;
        this.dependencies = Collections.unmodifiableList(builder.dependencies);
        this.permissions = Collections.unmodifiableList(builder.permissions);
        this.architectures = Collections.unmodifiableList(builder.architectures);
    }

    public String getId() {
        return this.id;
    }

    public long getSize() {
        return this.size;
    }

    public String getName() {
        return this.name;
    }

    public String getMinAPI() {
        return this.minAPI;
    }

    public String getVersion() {
        return this.version;
    }

    public BasePluginType getType() {
        return this.type;
    }

    public String getUrl() {
        return this.url;
    }

    public String getDescription() {
        return this.description;
    }

    public String getCategory() {
        return this.category;
    }

    public String getChangeNotes() {
        return this.changeNotes;
    }

    public String getVendor() {
        return this.vendor;
    }

    public String getVendorEmail() {
        return this.vendorEmail;
    }

    public String getVendorUrl() {
        return this.vendorUrl;
    }

    public long getReleaseDate() {
        return this.releaseDate;
    }

    public long getLastUpdated() {
        return this.lastUpdated;
    }

    public String getPackageName() {
        return this.packageName;
    }

    public boolean getIsApplication() {
        return this.isApplication;
    }

    public boolean getIsRestartRequired() {
        return this.isRestartRequired;
    }

    public boolean getIsLicenseOptional() {
        return this.isLicenseOptional;
    }

    public List<IPlugin> getDependencies() {
        return this.dependencies;
    }

    public List<IPermission<?>> getPermissions() {
        return this.permissions;
    }

    public List<String> getArchitectures() {
        return architectures;
    }

    public String getIcon() {
        return this.icon;
    }

    public boolean isValid() {
        return id != null
                && !id.isEmpty()
                && name != null
                && !name.isEmpty()
                && minAPI != null
                && !minAPI.isEmpty()
                && version != null
                && !version.isEmpty()
                && type != null
                && type != BasePluginType.UNKNOWN;
    }

    public static final class Builder {
        private String id;
        private String name;
        private String minAPI;
        private String version;
        private BasePluginType type = BasePluginType.UNKNOWN;
        // Optional
        private long size;
        private String url = "";
        private String icon = "";
        private String description = "";
        private String category = "";
        private String changeNotes = "";
        private String vendor = "";
        private String vendorEmail = "";
        private String vendorUrl = "";
        private long releaseDate = -1;
        private long lastUpdated = -1;
        private String namespace = "";
        private boolean isApplication;
        private boolean isRestartRequired;
        private boolean isLicenseOptional;
        private List<IPlugin> dependencies = new ArrayList<>();
        private List<IPermission<?>> permissions = new ArrayList<>();
        private List<String> architectures = new ArrayList<>();

        public Builder setId(String id) {
            this.id = id;
            return this;
        }

        public Builder setSize(long size) {
            this.size = size;
            return this;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setMinAPI(String minAPI) {
            this.minAPI = minAPI;
            return this;
        }

        public Builder setVersion(String version) {
            this.version = version;
            return this;
        }

        public Builder setType(BasePluginType type) {
            this.type = type;
            return this;
        }

        public Builder setUrl(String url) {
            this.url = url;
            return this;
        }

        public Builder setIcon(String icon) {
            this.icon = icon;
            return this;
        }

        public Builder setDescription(String description) {
            this.description = description;
            return this;
        }

        public Builder setCategory(String category) {
            this.category = category;
            return this;
        }

        public Builder setChangeNotes(String changeNotes) {
            this.changeNotes = changeNotes;
            return this;
        }

        public Builder setVendor(String vendor) {
            this.vendor = vendor;
            return this;
        }

        public Builder setVendorEmail(String vendorEmail) {
            this.vendorEmail = vendorEmail;
            return this;
        }

        public Builder setVendorUrl(String vendorUrl) {
            this.vendorUrl = vendorUrl;
            return this;
        }

        public Builder setReleaseDate(long releaseDate) {
            this.releaseDate = releaseDate;
            return this;
        }

        public Builder setLastUpdated(long lastUpdated) {
            this.lastUpdated = lastUpdated;
            return this;
        }

        public Builder setNamespace(String namespace) {
            this.namespace = namespace;
            return this;
        }

        public Builder setIsApplication(boolean isApplication) {
            this.isApplication = isApplication;
            return this;
        }

        public Builder setIsRestartRequired(boolean isRestartRequired) {
            this.isRestartRequired = isRestartRequired;
            return this;
        }

        public Builder setIsLicenseOptional(boolean isLicenseOptional) {
            this.isLicenseOptional = isLicenseOptional;
            return this;
        }

        public Builder setDependencies(List<IPlugin> dependencies) {
            this.dependencies = dependencies;
            return this;
        }

        public Builder setPermission(List<IPermission<?>> permissions) {
            this.permissions = permissions;
            return this;
        }

        public Builder setArchitectures(List<String> list) {
            this.architectures = list;
            return this;
        }

        public BasePluginManifest build() {
            if (id == null || id.isEmpty())
                throw new IllegalStateException("BasePluginManifest requires a non-empty id");
            if (name == null || name.isEmpty())
                throw new IllegalStateException("BasePluginManifest requires a non-empty name");
            if (version == null || version.isEmpty())
                throw new IllegalStateException("BasePluginManifest requires a non-empty version");
            return new BasePluginManifest(this);
        }
    }
}
