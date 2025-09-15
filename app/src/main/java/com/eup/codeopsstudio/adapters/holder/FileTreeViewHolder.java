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

package com.eup.codeopsstudio.adapters.holder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;

import com.eup.codeopsstudio.R;
import com.eup.codeopsstudio.databinding.LayoutFileTreeItemBinding;
import com.eup.codeopsstudio.models.ExtensionTable;
import com.eup.codeopsstudio.tv.model.TreeNode;
import com.eup.codeopsstudio.util.BaseUtil;

import java.io.File;

public class FileTreeViewHolder extends TreeNode.BaseNodeViewHolder<File> {

    private LayoutFileTreeItemBinding binding;

    public FileTreeViewHolder(@NonNull Context context) {
        super(context);
    }

    @Override
    public View createNodeView(@NonNull TreeNode node, @NonNull File file) {
        binding = LayoutFileTreeItemBinding.inflate(LayoutInflater.from(context));
        View root = applyPadding(node, binding, BaseUtil.dp(8));
        binding.title.setText(file.getName());

        if (file.isFile()) {
            binding.icon.setImageResource(ExtensionTable.getExtensionIcon(file.getName()));
        } else if (file.isDirectory()) {
            binding.icon.setImageResource(R.drawable.ic_chevron_right);
            rotateChevron(node.isExpanded());
        }
        return root;
    }

    protected RelativeLayout applyPadding(@NonNull final TreeNode node,
        @NonNull final LayoutFileTreeItemBinding binding, final int padding) {
        final RelativeLayout root = binding.getRoot();
        root.setPaddingRelative(root.getPaddingLeft() + (padding * (node.getLevel()
            - 1)), root.getPaddingTop(), root.getPaddingRight(), root.getPaddingBottom());
        return root;
    }

    public void rotateChevron(boolean expanded) {
        setLoading(false);
        int rotateDegree = expanded ? 90 : 0;
        binding.icon.setRotation(rotateDegree);
    }

    public void setLoading(boolean loading) {
        final int index;
        if (loading) {
            index = 1;
        } else {
            index = 0;
        }
        binding.viewFlipper.setDisplayedChild(index);
    }
}
