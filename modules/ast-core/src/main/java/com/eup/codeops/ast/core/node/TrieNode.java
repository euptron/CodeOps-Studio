package com.eup.codeops.ast.core.node;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import com.eup.codeops.ast.core.util.TreePrintStyles;
import com.eup.codeops.ast.core.util.TreePrinter;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class TrieNode<T> extends AbstractNode<T> {

  // Flag indicating if the path to this node forms a complete word.
  private boolean isEndOfWord;

  /**
   * Creates a new TrieNode.
   *
   * <p>The created TireNode is explicitly the root node The root node doesn't represent a specific
   * character or sequence, hence null the value of the root node is assigned as null
   */
  public TrieNode() {
    super(null, ROOT_LEVEL, /*Generate ID=*/ true);
    this.isEndOfWord = false; // Root is typically not end of a word itself
  }

  /**
   * Creates a new TrieNode representing a specific sequence.
   *
   * <p>The created node is created {@code UNDEFINED_LEVEL}, to allow it's parent to set it
   *
   * @see AbstractNode#updateChildProps(Node<T>)
   */
  public TrieNode(T sequence) {
    super(sequence, UNDEFINED_LEVEL, true);
    Objects.requireNonNull(sequence, "TrieNode sequence cannot be null");
    this.isEndOfWord = false;
  }

  @Override
  public Node<T> setValue(T value) {
    super.setValue(value);
    setEndOfWord(value != null);
    return this;
  }

  /**
   * Checks if this node marks the end of a complete word.
   *
   * @return {@code true} if it's the end of a word, {@code false} otherwise.
   */
  public boolean isEndOfWord() {
    return isEndOfWord;
  }

  /**
   * Sets whether this node marks the end of a complete word.
   *
   * @param endOfWord {@code true} to mark as the end of a word, {@code false} otherwise.
   */
  public void setEndOfWord(boolean endOfWord) {
    isEndOfWord = endOfWord;
  }

  /**
   * Finds or adds a child node based on its value. If a child with the same value exists, it's
   * returned. If not, the provided child node is validated and added, then returned. This is the
   * primary method for building the Trie structure correctly.
   *
   * @param child The potential child node to add (must be TrieNode with non-null value).
   * @return The existing or newly added child node corresponding to the value.
   */
  @Override
  public Node<T> addChild(Node<T> child) {
    validateTrieNodeChild(child);

    T childValue = child.getValue();
    TrieNode<T> existingChild = findChild(this, childValue);

    if (existingChild != null) {
      if (child instanceof TrieNode && ((TrieNode<T>) child).isEndOfWord()) {
        existingChild.setEndOfWord(true);
      }
      return existingChild;
    } else {
      super.addChild(child);
      return child; // Return the newly added child
    }
  }

  @Override
  public Node<T> addChildren(List<Node<T>> children) {
    for (Node<T> child : children) {
      addChild(child);
    }
    return this;
  }

  @Override
  public Node<T> addChildren(Node<T>[] children) {
    for (Node<T> child : children) {
      addChild(child);
    }
    return this;
  }

  public TrieNode<T> getChildByValue(T sequence) {
    return findChild(this, sequence);
  }

  public boolean removeChildByValue(T sequence) {
    int removalIndex = INVALID_LOCATION;
    TrieNode<T> childToRemove = getChildByValue(sequence);

    if (childToRemove != null) {
      removalIndex = super.removeChild(childToRemove);
    }

    return removalIndex >= 0;
  }

  @Override
  public int removeChild(Node<T> child) {
    if (!(child instanceof TrieNode)) return INVALID_LOCATION;
    return super.removeChild(child);
  }

  @Override
  public Node<T> addChild(Node<T> child, int index) {
    throw new UnsupportedOperationException(
        "Adding TrieNode child at a specific index is not supported.");
  }

  @Override
  public Node<T> addChild(Node<T> child, Comparator<Node<T>> comparator) {
    throw new UnsupportedOperationException(
        "Adding TrieNode child with a comparator is not supported.");
  }

  @Override
  public Node<T> addChildren(List<Node<T>> children, Comparator<Node<T>> comparator) {
    throw new UnsupportedOperationException(
        "Adding multiple TrieNode children with a comparator is not supported.");
  }

  @Override
  public Node<T> sortChildren(Comparator<Node<T>> comparator) {
    throw new UnsupportedOperationException("Sorting TrieNode children is not supported.");
  }

  @Override
  public Node<T> updateChildren(List<Node<T>> children, Comparator<Node<T>> comparator) {
    throw new UnsupportedOperationException(
        "Updating/Replacing all TrieNode children is not supported.");
  }

  @Override
  public void removeRange(int fromIndex, int toIndex) {
    throw new UnsupportedOperationException(
        "Removing a range of TrieNode children by index is not supported.");
  }

  private void validateTrieNodeChild(Node<T> child) {
    if (!(child instanceof TrieNode)) {
      throw new IllegalArgumentException(
          "Child must be an instance of TrieNode. Found: " + child.getClass().getName());
    }

    TrieNode<T> trieChild = (TrieNode<T>) child;
    T childSequence = trieChild.getValue();

    if (childSequence == null) {
      // Only the root node can have a null sequence
      throw new IllegalArgumentException("TrieNode children must have a non-null sequence value.");
    }
  }

  private TrieNode<T> findChild(TrieNode<T> node, T sequence) {
    for (Node<T> child : node.getChildren()) {
      if (Objects.equals(child.getValue(), sequence)) {
        if (child instanceof TrieNode) {
          return (TrieNode<T>) child;
        } else {
          System.err.println("Warning: Found a non-TrieNode child in TrieNode's children list.");
          return null;
        }
      }
    }
    return null; // Not found
  }

  /**
   * Provides a JSON representation of the TrieNode, including its character and endOfWord status,
   * and recursively includes children.
   *
   * @param indent Indentation level for formatting.
   * @return A JSON string fragment representing this node and its descendants.
   */
  @Override
  public String toJsonString(int indent) {
    String indentStr = "\t".repeat(indent);
    String childIndentStr = "\t".repeat(indent + 1);
    StringBuilder sb = new StringBuilder();

    sb.append(indentStr).append("{\n");
    // Add TrieNode specific fields first
    sb.append(childIndentStr)
        .append("\"value\": ")
        .append(escapeValueJson(getValue()))
        .append(",\n");
    sb.append(childIndentStr).append("\"isEndOfWord\": ").append(isEndOfWord).append(",\n");

    // Add relevant fields inherited from AbstractNode using getters
    sb.append(childIndentStr).append("\"id\": \"").append(getID()).append("\",\n");
    sb.append(childIndentStr).append("\"level\": ").append(getLevel()).append(",\n");
    sb.append(childIndentStr).append("\"index\": ").append(getIndex()).append(",\n"); // Example

    // Handle children array recursively
    sb.append(childIndentStr).append("\"children\": [\n");
    List<Node<T>> childrenList = super.getChildren(); // Get children from AbstractNode
    for (int i = 0; i < childrenList.size(); i++) {
      Node<T> child = childrenList.get(i);
      // Recursively call toJsonString for children (assuming they are TrieNodes)
      if (child instanceof TrieNode) {
        sb.append(((TrieNode<T>) child).toJsonString(indent + 2));
      } else {
        // Handle unexpected non-TrieNode child if strict validation wasn't enforced everywhere
        sb.append(childIndentStr)
            .append("\t{ \"error\": \"Non-TrieNode child\", \"value\": ")
            .append(escapeValueJson(child.getValue()))
            .append(" }");
      }

      if (i < childrenList.size() - 1) {
        sb.append(",\n");
      } else {
        sb.append("\n");
      }
    }
    sb.append(childIndentStr).append("]\n"); // Close children array
    sb.append(indentStr).append("}"); // Close JSON object for this node

    return sb.toString();
  }

  private String escapeValueJson(T value) {
    if (value == null) return "null";
    String s = value.toString();
    // Basic JSON string escaping
    s =
        s.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\b", "\\b")
            .replace("\f", "\\f")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t");
    return "\"" + s + "\"";
  }

  @Override
  public String toString() {
    String baseInfo = (getValue() == null) ? "<root>" : "'" + getValue().toString() + "'";
    return String.format(
        "TrieNode[value=%s, isEndOfWord=%s] -> {%s}", baseInfo, isEndOfWord, super.toString());
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), isEndOfWord);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof TrieNode)) return false;
    if (!super.equals(o)) return false; // Check base class equality first
    TrieNode<?> trieNode = (TrieNode<?>) o;
    return isEndOfWord == trieNode.isEndOfWord;
  }

  public static @NonNull String trimStartOf(@NonNull String s, @NonNull String prefix) {
    if (s.startsWith(prefix)) {
      return s.substring(prefix.length());
    }
    return s;
  }

  @VisibleForTesting
  public static void insertWord(TrieNode<Character> root, String word, String tag) {
    TrieNode<Character> currentNode = root;
    for (char ch : word.toCharArray()) {
      TrieNode<Character> childNode = currentNode.getChildByValue(ch);
      if (childNode == null) {
        // Character not found, create and add new node
        childNode = new TrieNode<>(ch);
        currentNode.addChild(childNode); // Use the corrected addChild
      }
      // Move to the next node
      currentNode = childNode;
    }
    // Mark the end of the word and set the tag
    currentNode.setEndOfWord(true);
    currentNode.addTag(tag);
  }

  public static void main(String[] args) {
    TrieNode<Character> root = new TrieNode<>();

    insertWord(root, "cat", "Animal");
    insertWord(root, "car", "Vehicle");
    insertWord(root, "cart", "Shopping");
    insertWord(root, "dog", "Animal");
    insertWord(root, "do", "Verb");

    // --- Verification (Optional) ---
    // Check if "cat" exists and has the right tag
    TrieNode<Character> temp = root.getChildByValue('c');
    if (temp != null) temp = temp.getChildByValue('a');
    if (temp != null) temp = temp.getChildByValue('t');
    if (temp != null && temp.isEndOfWord()) {
      System.out.println("Found 'cat', Tag: " + temp.getTag(0));
    } else {
      System.out.println("'cat' not found or not marked as end.");
    }

    // Check if "car" exists and has the right tag
    temp = root.getChildByValue('c');
    if (temp != null) temp = temp.getChildByValue('a');
    if (temp != null) temp = temp.getChildByValue('r');
    if (temp != null && temp.isEndOfWord()) {
      System.out.println("Found 'car', Tag: " + temp.getTag(0));
    } else {
      System.out.println("'car' not found or not marked as end.");
    }

    // Check if "do" exists and has the right tag
    temp = root.getChildByValue('d');
    if (temp != null) temp = temp.getChildByValue('o');
    if (temp != null && temp.isEndOfWord()) {
      System.out.println("Found 'do', Tag: " + temp.getTag(0));
    } else {
      System.out.println("'do' not found or not marked as end.");
    }
    // Check if "dog" exists and has the right tag (tests branching after 'do')
    temp = root.getChildByValue('d');
    if (temp != null) temp = temp.getChildByValue('o');
    if (temp != null) temp = temp.getChildByValue('g');
    if (temp != null && temp.isEndOfWord()) {
      System.out.println("Found 'dog', Tag: " + temp.getTag(0));
    } else {
      System.out.println("'dog' not found or not marked as end.");
    }
    
    System.out.println("---------------------------");

    TreePrinter<Character> printer = new TreePrinter<>(root);
    printer.setPrintStyle(TreePrintStyles.UNICODE_ROUNDED);

    System.out.println("--- Printing Trie to Console ---");
    System.out.println(printer.toString());
  }
}
