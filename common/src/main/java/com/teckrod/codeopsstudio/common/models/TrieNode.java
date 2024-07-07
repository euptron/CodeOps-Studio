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

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a node in a trie (prefix tree) data structure. Each node can have multiple children,
 * and it holds a reference to its parent node. The node can store data of a generic type {@code T}.
 *
 * <p>Usage example:
 *
 * <pre>{@code
 * TrieNode<String> rootNode = new TrieNode<>("", null);
 * TrieNode<String> childNode = rootNode.findOrAddChild("example");
 * childNode.setData("data1");
 *
 * System.out.println("Child Node Key: " + childNode.getKey()); // example
 * System.out.println("Child Node Data: " + childNode.getData()); // data1
 * System.out.println("Nested Children Count: " + rootNode.getNestedChildrenCount()); // 1
 * }</pre>
 *
 * @param <T> The type of data stored in the trie nodes.
 * @autor EUP
 */
public final class TrieNode<T> {

  private final String key;
  private final TrieNode<T> parent;
  private final Map<String, TrieNode<T>> children = new HashMap<>();
  private T data;
  private int nestedChildrenCount;

  /**
   * Constructs a TrieNode with the specified key and parent node.
   *
   * @param key The key associated with this node.
   * @param parent The parent node of this node, or null if this is the root node.
   */
  public TrieNode(String key, TrieNode<T> parent) {
    this.key = key;
    this.parent = parent;
    this.nestedChildrenCount = 0;
  }

  /**
   * Finds or adds a child node with the specified key.
   *
   * @param word The key of the child node to find or add.
   * @return The found or newly added child node.
   */
  public TrieNode<T> findOrAddChild(String word) {
    return children.computeIfAbsent(
        word,
        k -> {
          TrieNode<T> node = new TrieNode<>(k, this);
          incrementNestedChildrenCount();
          return node;
        });
  }

  /**
   * Returns the number of direct children of this node.
   *
   * @return The number of direct children.
   */
  public int size() {
    return children.size();
  }

  /**
   * Returns the parent node of this node.
   *
   * @return The parent node, or null if this is the root node.
   */
  public TrieNode<T> getParent() {
    return parent;
  }

  /**
   * Returns the key associated with this node.
   *
   * @return The key of this node.
   */
  public String getKey() {
    return key;
  }

  /**
   * Sets the data associated with this node.
   *
   * @param data The data to set.
   */
  public void setData(T data) {
    this.data = data;
  }

  /**
   * Returns the data associated with this node.
   *
   * @return The data of this node, or null if no data is set.
   */
  public T getData() {
    return data;
  }

  /**
   * Finds a node in the trie with the specified data.
   *
   * @param key The data to find.
   * @return The node containing the specified data, or null if not found.
   */
  public TrieNode<T> findNode(T key) {
    if (data != null && data.equals(key)) return this;
    for (TrieNode<T> child : children.values()) {
      TrieNode<T> found = child.findNode(key);
      if (found != null) return found;
    }
    return null;
  }

  /**
   * Returns the count of all nested children of this node.
   *
   * @return The nested children count.
   */
  public int getNestedChildrenCount() {
    return nestedChildrenCount;
  }

  /** Increments the nested children count for this node and all its ancestors. */
  private void incrementNestedChildrenCount() {
    TrieNode<T> current = this;
    while (current != null) {
      current.nestedChildrenCount++;
      current = current.parent;
    }
  }
}
