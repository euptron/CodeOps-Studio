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

package com.eup.codeops.ast.core.util;

import androidx.annotation.NonNull;
import com.eup.codeops.ast.core.node.Node;
import com.eup.codeops.ast.core.node.TrieNode;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Generates unique shortened display names for file paths using a trie-based approach.
 *
 * <p>Original IntelliJ IDEA implementation: <a
 * href="https://github.com/JetBrains/intellij-community/blob/master/platform/util/base/src/com
 * /intellij/filename/UniqueNameBuilder.java">UniqueNameBuilder.java</a>
 *
 * @param <T> The type of keys associated with paths (typically file references)
 */
public final class UniqueNameBuilder<T> {

  private static final String VFS_SEPARATOR = "/";

  private final String rootPath;
  private final String pathSeparator;
  private final TrieNode<String> rootNode = new TrieNode<>();
  private final Map<T, String> cachedPaths = new HashMap<>();

  public UniqueNameBuilder(String root, String separator) {
    rootPath = root;
    pathSeparator = separator;
  }

  public boolean contains(T file) {
    return cachedPaths.containsKey(file);
  }

  public int size() {
    return cachedPaths.size();
  }

  public void addPath(T key, String path) {
    path = TrieNode.trimStartOf(path, rootPath);
    cachedPaths.put(key, path);

    Node<String> current = rootNode;
    Iterator<String> pathComponentsIterator = new PathComponentsIterator(path);

    while (pathComponentsIterator.hasNext()) {
      String word = pathComponentsIterator.next();
      current = current.addChild(new TrieNode<String>(word));
    }
  }

  public String getShortPath(T key) {
    String path = cachedPaths.get(key);
    if (path == null) return key.toString();

    Node<String> current = rootNode;
    Node<String> fileNameNode = null;
    Node<String> firstNodeWithBranches = null;
    Node<String> firstNodeBeforeNodeWithBranches = null;

    Iterator<String> pathComponentsIterator = new PathComponentsIterator(path);

    while (pathComponentsIterator.hasNext()) {
      String pathComponent = pathComponentsIterator.next();
      current = current.addChild(new TrieNode<String>(pathComponent));

      if (fileNameNode == null) fileNameNode = current;
      if (firstNodeBeforeNodeWithBranches == null
          && firstNodeWithBranches != null
          && current.childrenSize() <= 1) {
        if (current.getParent().getDescendantCount() - current.getParent().childrenSize() < 1) {
          firstNodeBeforeNodeWithBranches = current;
        }
      }

      if (current.childrenSize() != 1 && firstNodeWithBranches == null) {
        firstNodeWithBranches = current;
      }
    }

    if (firstNodeBeforeNodeWithBranches == null) {
      firstNodeBeforeNodeWithBranches = current;
    }

    boolean skipFirstSeparator = true;
    StringBuilder pathBuilder = new StringBuilder();
    for (Node<String> c = firstNodeBeforeNodeWithBranches; c != rootNode; ) {
      if (c != fileNameNode
          && c != firstNodeBeforeNodeWithBranches
          && c.getParent().childrenSize() == 1) {
        pathBuilder.append(pathSeparator);
        pathBuilder.append("…");

        do {
          c = c.getParent();
        } while (c != fileNameNode
            && c.getParent().childrenSize() == 1); // Don't print two or more ellipses in a row.
      } else {
        if (c.getValue().startsWith(VFS_SEPARATOR)) {
          if (!skipFirstSeparator) pathBuilder.append(pathSeparator);
          skipFirstSeparator = false;
          pathBuilder.append(c.getValue(), VFS_SEPARATOR.length(), c.getValue().length());
        } else {
          pathBuilder.append(c.getValue());
        }
        c = c.getParent();
      }
    }
    return pathBuilder.toString();
  }

  public String getSeparator() {
    return pathSeparator;
  }

  private static final class PathComponentsIterator implements Iterator<String> {
    private final String path;
    private int end;
    private int start;

    PathComponentsIterator(String path) {
      this.path = path;
      this.end = path.length();
      this.start = path.lastIndexOf(VFS_SEPARATOR);
    }

    @Override
    public boolean hasNext() {
      return end != 0;
    }

    @Override
    public String next() {
      if (end == 0) throw new NoSuchElementException();

      String pathComponent;

      if (start != -1) {
        pathComponent = path.substring(start, end);
        end = start;
        start = path.lastIndexOf(VFS_SEPARATOR, end - 1);
      } else {
        pathComponent = path.substring(0, end);
        if (!pathComponent.startsWith(VFS_SEPARATOR)) pathComponent = VFS_SEPARATOR + pathComponent;
        end = 0;
      }
      return pathComponent;
    }
  }
}
