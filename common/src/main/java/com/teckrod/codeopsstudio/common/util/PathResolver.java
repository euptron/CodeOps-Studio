/*************************************************************************
 * This file is part of CodeOps Studio.
 * CodeOps Studio - code anywhere anytime
 * https://github.com/euptron/CodeOps-Studio
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
 
   package com.eup.codeopsstudio.common.util;

import java.util.HashMap;
import java.util.Map;
import com.eup.codeopsstudio.common.models.TrieNode;

/**
 * Utility class for resolving paths and managing associated data. Supports adding paths, checking
 * for path existence, retrieving the size of the path resolver, and obtaining the short path
 * representation for a given key.
 *
 * <p>Usage Example:
 *
 * <pre>
 * PathResolver<String> pathResolver = new PathResolver<>("/root", "/");
 * pathResolver.addPath("file1", "/root/idea/pycharm/download/index.html");
 * pathResolver.addPath("file2", "/root/idea/fabrique/download/index.html");
 *
 * System.out.println("Short Path file1: " + pathResolver.getShortPath("file1"));
 * System.out.println("Short Path file2: " + pathResolver.getShortPath("file2"));
 * </pre>
 *
 * Adopted from: <a
 * href="https://github.com/JetBrains/intellij-community/blob/master/platform/util/base/src/com/intellij/filename/UniqueNameBuilder.java">UniqueNameBuilder.java</a>
 *
 * @param <T> The type of data associated with the paths.
 * @autor EUP
 */
public final class PathResolver<T> {
  private static final String VFS_SEPARATOR = "/";
  private final TrieNode<T> root = new TrieNode<>("", null);
  private final String separator;
  private final String rootPath;

  /**
   * Constructs a PathResolver instance.
   *
   * @param rootPath The root path for resolving relative paths.
   * @param separator The separator used in the paths.
   */
  public PathResolver(String rootPath, String separator) {
    this.rootPath = rootPath;
    this.separator = separator;
  }

  /**
   * Checks if the path resolver contains the specified file.
   *
   * @param file The file to check.
   * @return True if the path resolver contains the file, otherwise false.
   */
  public boolean contains(T file) {
    return findNode(file) != null;
  }

  /**
   * Retrieves the number of paths stored in the resolver.
   *
   * @return The size of the path resolver.
   */
  public int size() {
    return root.size();
  }

  /**
   * Adds a path with associated data to the resolver.
   *
   * @param key The key associated with the data.
   * @param path The path to add.
   */
  public void addPath(T key, String path) {
    path = ensureRelativePath(path, rootPath);
    TrieNode<T> current = root;
    String[] pathComponents = path.split(VFS_SEPARATOR);
    for (String component : pathComponents) {
      current = current.findOrAddChild(component);
    }
    current.setData(key);
  }

  /**
   * Retrieves the short path representation for a given key.
   *
   * @param key The key for which to retrieve the short path.
   * @return The short path representation of the key.
   */
  public String getShortPath(T key) {
    TrieNode<T> node = findNode(key);
    if (node == null) return key.toString();

    StringBuilder builder = new StringBuilder();
    TrieNode<T> current = node;

    while (current != root) {
      if (!builder.toString().isEmpty()) builder.insert(0, separator);
      builder.insert(0, current.getKey());
      current = current.getParent();

      if (current.getParent() != null && current.getNestedChildrenCount() > 1) {
        break; // Found a branch point
      }
    }

    if (current != root) {
      builder.insert(0, separator).insert(0, "...");
    }
    return builder.toString();
  }

  /**
   * Retrieves the separator used in the paths.
   *
   * @return The separator used in the paths.
   */
  public String getSeparator() {
    return separator;
  }

  /**
   * Finds the TrieNode associated with the given key.
   *
   * @param key The key to find.
   * @return The TrieNode associated with
   */
  private TrieNode<T> findNode(T key) {
    return root.findNode(key);
  }

  /**
   * Ensures the given path is relative by trimming the root path prefix.
   *
   * @param path The path to ensure is relative.
   * @param prefix The root path prefix to trim.
   * @return The relative path.
   */
  private String ensureRelativePath(String path, String prefix) {
    if (path.startsWith(prefix)) {
      return path.substring(prefix.length());
    }
    return path.startsWith(VFS_SEPARATOR) ? path.substring(1) : path;
    // return path;
  }
}
