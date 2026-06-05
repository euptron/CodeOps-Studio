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
 * questions or need additional information. Email: euptron@gmail.com
 */
package com.eup.codeopsstudio.plugin

import android.graphics.drawable.Drawable
import androidx.recyclerview.widget.DiffUtil
import java.util.Objects

class PluginItem @JvmOverloads constructor(
    @JvmField val id: String,
    @JvmField val name: String?,
    @JvmField val author: String?,
    @JvmField val icon: Drawable?,
    @JvmField val description: String?,
    @JvmField val downloadUrl: String? = null
) {
    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val that = o as PluginItem
        return id == that.id
                && name == that.name
                && author == that.author
                && description == that.description
    }

    override fun hashCode(): Int {
        return Objects.hash(id, name, description)
    }

    companion object {
        @JvmField
        val DIFF_CALLBACK: DiffUtil.ItemCallback<PluginItem?> =
            object : DiffUtil.ItemCallback<PluginItem?>() {
                override fun areItemsTheSame(oldItem: PluginItem, newItem: PluginItem): Boolean {
                    return oldItem.id == newItem.id
                }

                override fun areContentsTheSame(
                    oldItem: PluginItem, newItem: PluginItem
                ): Boolean {
                    return oldItem == newItem
                }
            }
    }
}
