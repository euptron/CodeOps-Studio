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
 
   package com.eup.codeopsstudio.ui.settings.api;

/**
 * Defines a change log item
 *
 * @since Tue, 22-07-2023
 * @version 0.1
 */
public class ChangelogItem {
  /** Defines a release type of an app */
  public enum ReleaseType {
    /**
     * A version of the app that's in early development. It's usually not stable and might have
     * several bugs.
     */
    ALPHA("alpha"),
    /**
     * A more polished version compared to alpha, released to a limited group of users for testing
     * and feedback. Bugs are still expected
     */
    BETA("beta"),
    /**
     * A near-final version that's considered stable and ready for release unless any critical
     * issues are found.
     */
    RELEASE_CANDIDATE("release-candidate"),
    /**
     * The official version of the app that's intended for the general public. It has undergone
     * testing and bug fixing.
     */
    STABLE("stable"),
    /** A version of the app that's released before the stable version. */
    PRE_RELEASE("pre-release");

    public String releaseName;

    ReleaseType(String releaseName) {
      this.releaseName = releaseName;
    }

    public static ReleaseType get(String name) {
      if (name == null) return ALPHA;

      for (ReleaseType value : values()) {
        if (value.releaseName.equals(name)) {
          return value;
        }
      }
      return ALPHA;
    }
  }

  private String version;
  private String description;
  private long releaseDate;
  private boolean isVersioned;
  private boolean isExpanded = false;
  private ReleaseType releaseType;

  public ChangelogItem(
      String version,
      String description,
      long releaseDate,
      boolean isVersioned,
      ReleaseType releaseType) {
    this.version = version;
    this.description = description;
    this.releaseDate = releaseDate;
    this.isVersioned = isVersioned;
    this.isExpanded = isExpanded;
    this.releaseType = releaseType;
  }

  public String getVersionName() {
    return this.version.trim();
  }

  public void setVersionName(String version) {
    this.version = version;
  }

  public String getDescription() {
    return this.description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public long getReleaseDate() {
    return this.releaseDate;
  }

  public void setReleaseDate(long releaseDate) {
    this.releaseDate = releaseDate;
  }

  public boolean hasVersionName() {
    return this.isVersioned;
  }

  public void hasVersionName(boolean isVersioned) {
    this.isVersioned = isVersioned;
  }

  public boolean getIsExpanded() {
    return this.isExpanded;
  }

  public void setIsExpanded(boolean isExpanded) {
    this.isExpanded = isExpanded;
  }

  public ReleaseType getReleaseType() {
    return this.releaseType;
  }

  public void setReleaseType(ReleaseType releaseType) {
    this.releaseType = releaseType;
  }
}
