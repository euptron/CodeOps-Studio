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
import androidx.annotation.Nullable;
import java.io.File;
import java.io.IOException;

/**
 * An interface to provide easy access to online or offline documents
 *
 * @author EUP
 */
public interface DocumentServer {

  /**
   * Retrives the contents of a document either from a file or url
   *
   * @param url The document url
   * @param isFile True if we @param url is a file dir
   * @return The contents of a document
   */
  public byte[] getContent(String url, boolean isFile);

  /**
   * Get the document name
   *
   * @param name The document name
   */
  public String getName(String name);

  /**
   * Retrives the uniform resource locator of the document
   *
   * <p>
   *
   * @see #getContent must be implemented before calling this
   * @return The url
   */
  public String getUrl();

  /**
   * Returns the document
   *
   * @param document The document
   * @throws IOException If an I/O error occurs.
   */
  public Document getDocument(@NonNull Document document) throws IOException;

  /**
   * Sets the directory for caching documents.
   *
   * @param directory The directory for storing cached documents.
   */
  public void setCacheDirectory(@Nullable File directory);

  /**
   * Retrieves the cache directory.
   *
   * @return The cache directory.
   */
  public File getCacheDirectory();

  /**
   * Sets a new document event
   *
   * @param event The document event
   */
  public void onDocumentEventChange(@NonNull DocumentEvent event);
}
