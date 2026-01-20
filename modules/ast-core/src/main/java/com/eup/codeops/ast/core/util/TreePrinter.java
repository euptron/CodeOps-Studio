package com.eup.codeops.ast.core.util;

import com.eup.codeops.ast.core.node.Node;
import com.eup.codeops.ast.core.transversal.DFS;
import com.eup.codeops.ast.core.transversal.IExit;
import com.eup.codeops.ast.core.transversal.INext;
import com.eup.codeops.ast.core.visitors.NodeVisitor;
import java.util.*;

public class TreePrinter<T> {

  private Node<T> root;
  private TreePrintStyle printStyle;
  private Set<Node<T>> visitedNodes = new HashSet<>();
  private StringBuilder treeBuilder = new StringBuilder();

  /**
   * Creates new tree printer with default style
   *
   * @param root Starting node of the tree
   */
  public TreePrinter(Node<T> root) {
    this.root = root;
    this.printStyle = TreePrintStyles.DEFAULT;
  }

  public void setPrintStyle(TreePrintStyle printStyle) {
    this.printStyle = printStyle;
  }

  private String getValue(Node<T> node) {
    String value = "";
    boolean isCycle = visitedNodes.contains(node);
    if (node.isRoot()) {
      value = "[ROOT]";
    } else if (node.getValue() != null) {
      value = node.getValue().toString();
    }
    return value + (isCycle ? getAnnotation("CYCLE") : "");
  }

  private String getAnnotation(String annotation) {
    return printStyle.getAnnotationPrefix() + annotation + printStyle.getAnnotationSuffix();
  }

  private String getIndentMarker(Node<T> node) {
    String spaces = printStyle.getHorizontalIndentMarker();
    if (node.isLastChild()) {
      return spaces;
    } else {
      return spaces.replaceFirst(
          TreePrintStyle.DEFAULT_PADDING, printStyle.getVerticalIndentMarker());
    }
  }

  private String getPointer(Node<T> node) {
    if (node.isRoot()) {
      return "";
    } else if (node.isLastChild()) {
      return printStyle.getLastChildPointer();
    } else {
      return printStyle.getNonLastChildPointer();
    }
  }

  private String getPadding(Node<T> node) {
    String spaces = printStyle.getHorizontalIndentMarker();
    List<String> parts = new ArrayList<>();
    Node<T> current = node.getParent();
    while (current != null && !current.isRoot()) {
      parts.add(getIndentMarker(current));
      current = current.getParent();
    }
    Collections.reverse(parts);
    return String.join("", parts);
  }

  private void appendLine(Node<T> node) {
    if (treeBuilder.length() > 0) treeBuilder.append("\n");
    treeBuilder.append(getPadding(node));
    treeBuilder.append(getPointer(node));
    treeBuilder.append(getValue(node));
  }

  @Override
  public final String toString() {
    NodeVisitor<T> visitor =
        node -> {
          appendLine(node);
          visitedNodes.add(node);
        };
    INext<T> next = Node::hasChildren;
    IExit<T> exit = node -> node.isRoot() && node.isLeaf();
    new DFS<T>().traverse(root, visitor, next, exit);
    return treeBuilder.toString();
  }
}
