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

package com.eup.codeops.ast.core.transversal;

import androidx.annotation.NonNull;
import com.eup.codeops.ast.core.node.Node;
import com.eup.codeops.ast.core.visitors.NodeVisitor;

/**
 * Defines a contract for traversing a data structure, such as an Abstract Syntax Tree (AST). This
 * interface provides a standardized way of iterating over nodes in a data structure, allowing for
 * flexible and reusable traversal logic.
 *
 * @param <T> the type of data stored in the nodes
 * @version 1.0
 * @since 1.0
 * @author EUP (2024-08-18)
 */
public interface Traversable<T> {

  /**
   * Traverses the data structure starting from the given node, applying the provided visitor to
   * each node.
   *
   * @param node the starting node for traversal
   * @param visitor the visitor to apply to each node
   * @param next the strategy for determining the next nodes
   * @return the traversed node
   */
  Node<T> traverse(@NonNull Node<T> node, @NonNull NodeVisitor<T> visitor, @NonNull INext<T> next);
}
