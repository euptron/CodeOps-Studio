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
 
   package com.eup.codeopsstudio.common.models;

import androidx.annotation.NonNull;
import com.blankj.utilcode.util.FileIOUtils;
import com.eup.codeopsstudio.common.models.DocumentServer;
import java.io.File;
import com.blankj.utilcode.util.FileUtils;

/**
 * A basic representation of a document in CodeOps Studio 
 *
 * @author EUP
 */
public final class Document  {

  private final File baseFile;
  private final byte[] content;
  private final boolean isModified;
  private final String name;

  /**
   * Converts a file to document
   *
   * @param file The file to be converted
   */
  public static Document toDocument(File file) {
    byte[] content = FileIOUtils.readFile2BytesByStream(file);
    return new Document(file, content, false, file.getName());
  }

  public Document(File baseFile, byte[] content, String name) {
    this(baseFile, content, false, name);
  }

  public Document(File baseFile, byte[] content, boolean isModified, String name) {
    this.baseFile = baseFile;
    this.content = content;
    this.isModified = isModified;
    this.name = name;
  }

  public File getBaseFile() {
    return this.baseFile;
  }

  public byte[] getContent() {
    return this.content;
  }

  public boolean getIsModified() {
    return this.isModified;
  }

  public String getName() {
    return this.name;
  }

  public String getFormat() {
    return FileUtils.getFileExtension(baseFile);
  }

  public String getSize() {
    return FileUtils.getSize(baseFile);
  }

  @Override
  public boolean equals(Object ob) {
    return ob == this;
  }

  /**
   * This returns the hash code for this <code>Document</code>.
   *
   * @return <code>int</code> hash code
   */
  @Override
  public int hashCode() {
    return super.hashCode();
  }

  @Override
  public String toString() {
    return "Document[baseFile="
        + baseFile
        + ", content="
        + content
        + ", isModified="
        + isModified
        + ", name="
        + name
        + "]";
  }

  public enum MimeType {
    /** Image files only */
    IMAGE("image/*"),

    /** Text files only */
    TEXT("text/*"),

    /** Audio files only */
    AUDIO("audio/*"),

    /** Video files only */
    VIDEO("video/*"),

    /** All files */
    ALL("*/*");

    private final String mType;

    MimeType(String type) {
      mType = type;
    }

    @Override
    public String toString() {
      return mType;
    }
  }
}
