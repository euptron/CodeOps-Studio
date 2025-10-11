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
 * questions or need additional information. Email: etido.up@gmail.com
 */

package com.eup.codeopsstudio.util;

import androidx.annotation.NonNull;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

/**
 * A specialized {@link Map} implementation that can be "frozen" to prevent further modifications.
 * <p>
 * This class wraps a standard {@link HashMap} and adds a {@code frozen} state.
 * When the map is frozen, any attempt to modify it (e.g., {@code put}, {@code remove}, {@code
 * clear})
 * will result in an {@link UnsupportedOperationException}.
 * </p>
 * <p>
 * When frozen, the collections returned by {@code keySet()}, {@code values()}, and {@code
 * entrySet()}
 * are unmodifiable views of the underlying map's collections.
 * </p>
 * <p>
 * The map can be unfrozen using the {@link #unfreeze()} method, allowing modifications again.
 * </p>
 * <p>
 * This class provides convenient static factory methods like {@link #of(Object...)} and
 * {@link #duplicateOf(Object...)} to create pre-populated and frozen instances.
 * The {@link #chain(Consumer)} method allows for a fluent way to build and populate a map.
 * </p>
 * <p>
 * Note: Null keys are not permitted and will result in a {@link NullPointerException} for
 * operations like {@code put}, {@code get}, and {@code containsKey}.
 * </p>
 *
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 * @author Etido Peter
 */
public class MorphMap<K, V> implements Map<K, V> {

    private static final String KEY_NON_NULL_MESSAGE = "Key can not be null";
    private final HashMap<K, V> map;
    private boolean frozen;

    public MorphMap() {
        this.map    = new HashMap<>();
        this.frozen = false;
    }

    public MorphMap(Map<? extends K, ? extends V> source) {
        this.map = new HashMap<>(source);
        if (source instanceof MorphMap) {
            this.frozen = ((MorphMap<?, ?>) source).frozen;
        } else {
            this.frozen = false;
        }
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        Objects.requireNonNull(key, KEY_NON_NULL_MESSAGE);
        return map.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    @Override
    public V get(Object key) {
        Objects.requireNonNull(key, KEY_NON_NULL_MESSAGE);
        return map.get(key);
    }

    @Override
    public V put(K key, V value) {
        if (frozen) throw new UnsupportedOperationException();
        Objects.requireNonNull(key, KEY_NON_NULL_MESSAGE);
        return map.put(key, value);
    }

    @Override
    public V remove(Object key) {
        if (frozen) throw new UnsupportedOperationException();
        return map.remove(key);
    }

    @Override
    public void putAll(@NonNull Map<? extends K, ? extends V> m) {
        if (frozen) throw new UnsupportedOperationException();
        Objects.requireNonNull(m, "Map to PutAll can not be null");
        map.putAll(m);
    }

    @Override
    public void clear() {
        if (frozen) throw new UnsupportedOperationException();
        map.clear();
    }

    @NonNull
    @Override
    public Set<K> keySet() {
        return (frozen) ? Collections.unmodifiableSet(map.keySet()) : map.keySet();
    }

    @NonNull
    @Override
    public Collection<V> values() {
        return frozen ? Collections.unmodifiableCollection(map.values()) : map.values();
    }

    @NonNull
    @Override
    public Set<Entry<K, V>> entrySet() {
        return frozen ? Collections.unmodifiableSet(map.entrySet()) : map.entrySet();
    }

    public static <K, V> MorphMap<K, V> chain(Consumer<Map<K, V>> block) {
        MorphMap<K, V> map = new MorphMap<>();
        block.accept(map);
        return new MorphMap<>(map);
    }

    public static <K, V> MorphMap<K, V> duplicateOf(Object... values) {
        MorphMap<K, V> map = new MorphMap<>();

        for (int i = 0; i < values.length; i += 2) {
            @SuppressWarnings("unchecked") K key = (K) values[i];
            @SuppressWarnings("unchecked") V value = (V) values[i];
            map.put(key, value);
        }
        return map.freeze();
    }

    public synchronized MorphMap<K, V> freeze() {
        this.frozen = true;
        return this;
    }

    public boolean isFrozen() {
        return frozen;
    }

    public static <K, V> MorphMap<K, V> of(Object... keyValuePairs) {
        if (keyValuePairs.length % 2 != 0) {
            throw new IllegalArgumentException("Key-value pairs must be even");
        }

        MorphMap<K, V> map = new MorphMap<>(); // Defensive copy

        for (int i = 0; i < keyValuePairs.length; i += 2) {
            @SuppressWarnings("unchecked") K key = (K) keyValuePairs[i];
            @SuppressWarnings("unchecked") V value = (V) keyValuePairs[i + 1];
            map.put(key, value);
        }

        return map.freeze();
    }

    public synchronized MorphMap<K, V> unfreeze() {
        this.frozen = false;
        return this;
    }
}

