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

package com.eup.codeops.ast.core.util;

public abstract class TreePrinter {

  private Object root;
  private StringBuilder sb;
  private String pointerFotrLast = "└─";
  private String pointerForOther = "├─";
  private boolean isInstantPrintNeeded;

  public TreePrinter(Object root) {
    this.root = root;
  }

  public abstract Object[] getChild(Object obj);

  public abstract String getValue(Object obj);

  public abstract boolean isLeaf(Object obj);

  public void visitAndPrint() {
    isInstantPrintNeeded = true;
    visitRoot();
  }

  public StringBuilder visitAndReturn() {
    isInstantPrintNeeded = false;
    sb = new StringBuilder();
    visitRoot();
    return this.sb;
  }

  private void visitRoot() {
    if (isInstantPrintNeeded) {
      instantPrint(root, "", "");
    } else {
      appendToSb(root, "", "");
    }

    if (isLeaf(root)) return;

    Object[] childs = getChild(root);
    for (int i = 0; i < childs.length; i++) {
      if (i == childs.length - 1) visitNode(childs[i], "", pointerFotrLast, false);
      else visitNode(childs[i], "", pointerForOther, true);
    }
  }

  private void visitNode(Object node, String padding, String pointer, boolean hasMore) {
    if (isInstantPrintNeeded) {
      instantPrint(node, padding, pointer);
    } else {
      appendToSb(node, padding, pointer);
    }

    if (isLeaf(node)) return;

    StringBuilder paddingBuilder = new StringBuilder(padding);
    paddingBuilder.append(hasMore ? "│  " : "   ");

    Object[] childs = getChild(node);
    for (int i = 0; i < childs.length; i++) {
      if (i == childs.length - 1)
        visitNode(childs[i], paddingBuilder.toString(), pointerFotrLast, false);
      else visitNode(childs[i], paddingBuilder.toString(), pointerForOther, true);
    }
  }

  private void instantPrint(Object obj, String padding, String pointer) {
    System.out.println(padding + pointer + getValue(obj));
  }

  private void appendToSb(Object obj, String padding, String pointer) {
    sb.append("\n" + padding + pointer + getValue(obj));
  }
}
