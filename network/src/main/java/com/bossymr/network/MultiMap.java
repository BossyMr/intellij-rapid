package com.bossymr.network;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Stream;

public class MultiMap<K, V> implements Map<K, List<V>> {

    private final Map<K, List<V>> delegate;

    public MultiMap() {
        this.delegate = new HashMap<>();
    }

    public MultiMap(@NotNull Map<K, List<V>> delegate) {
        this.delegate = delegate;
    }

    public @NotNull Stream<Map.Entry<K, V>> stream() {
        return delegate.entrySet().stream()
                .flatMap(entrySet -> entrySet.getValue().stream()
                        .map(value -> Map.entry(entrySet.getKey(), value)));
    }

    public @Nullable V first(@NotNull K key) {
        if (delegate.containsKey(key)) {
            List<V> values = delegate.get(key);
            return values.size() > 0 ? values.get(0) : null;
        }
        return null;
    }

    public void add(@NotNull K key, @Nullable V value) {
        delegate.computeIfAbsent(key, (ignored) -> new ArrayList<>());
        delegate.get(key).add(value);
    }

    public void set(@NotNull K key, @Nullable V value) {
        delegate.put(key, new ArrayList<>());
        delegate.get(key).add(value);
    }

    public boolean containsElement(@NotNull V value) {
        return delegate.values().stream().anyMatch(list -> list.contains(value));
    }

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return delegate.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return delegate.containsValue(value);
    }

    @Override
    public List<V> get(Object key) {
        return delegate.get(key);
    }

    @Nullable
    @Override
    public List<V> put(K key, List<V> value) {
        return delegate.put(key, value);
    }

    @Override
    public List<V> remove(Object key) {
        return delegate.remove(key);
    }

    @Override
    public void putAll(@NotNull Map<? extends K, ? extends List<V>> m) {
        delegate.putAll(m);
    }

    @Override
    public void clear() {
        delegate.clear();
    }

    @Override
    public @NotNull Set<K> keySet() {
        return delegate.keySet();
    }

    @Override
    public @NotNull Collection<List<V>> values() {
        return delegate.values();
    }

    @Override
    public @NotNull Set<Entry<K, List<V>>> entrySet() {
        return delegate.entrySet();
    }
}
