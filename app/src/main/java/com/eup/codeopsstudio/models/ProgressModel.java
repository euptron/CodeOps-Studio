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

package com.eup.codeopsstudio.models;

/**
 * A progress indicator domain
 *
 * @author Etido Peter
 */
public class ProgressModel {
  private final boolean isInDeterminate;
  private final int progressValue;
  private final boolean isComplete;

  public ProgressModel(boolean isInDeterminate, int progressValue, boolean isComplete) {
    this.isInDeterminate = isInDeterminate;
    this.progressValue = progressValue;
    this.isComplete = isComplete;
  }

  public boolean isInDeterminate() {
    return this.isInDeterminate;
  }

  public int getProgressValue() {
    return this.progressValue;
  }

  public boolean isComplete() {
    return this.isComplete;
  }
}
