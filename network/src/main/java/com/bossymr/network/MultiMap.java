package com.bossymr.network;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Supplier;

public class MultiMap<K, V> extends AbstractMap<K, V> {

    private final @NotNull Map<K, Collection<Entry<K, V>>> delegate;
    private final @NotNull Supplier<? extends Collection<Entry<K, V>>> supplier;

    public MultiMap() {
        this(ArrayList::new);
    }

    public MultiMap(@NotNull MultiMap<K, V> delegate) {
        this(delegate, ArrayList::new);
    }

    public MultiMap(@NotNull Supplier<? extends Collection<Map.Entry<K, V>>> supplier) {
        this.delegate = new HashMap<>();
        this.supplier = supplier;
    }

    public MultiMap(@NotNull MultiMap<K, V> delegate, @NotNull Supplier<? extends Collection<Entry<K, V>>> supplier) {
        this.delegate = new HashMap<>();
        this.delegate.putAll(delegate.delegate);
        this.supplier = supplier;
    }

    @Override
    public V put(K key, V value) {
        delegate.computeIfAbsent(key, unused -> supplier.get());
        delegate.get(key).add(new Node<>(key, value));
        return null;
    }

    public void set(K key, V value) {
        delegate.computeIfAbsent(key, unused -> supplier.get());
        delegate.get(key).clear();
        delegate.get(key).add(new Node<>(key, value));
    }

    public @NotNull Collection<V> getAll(@NotNull K key) {
        return new ValueList(key);
    }

    @Override
    public @NotNull Set<Entry<K, V>> entrySet() {
        return new EntrySet();
    }

    private static final class Node<K, V> implements Map.Entry<K, V> {

        private final K key;
        private V value;

        public Node(K key, V value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public K getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return value;
        }

        @Override
        public V setValue(V newValue) {
            V previousValue = value;
            value = newValue;
            return previousValue;
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) return true;
            if (object == null || getClass() != object.getClass()) return false;
            Node<?, ?> node = (Node<?, ?>) object;
            return Objects.equals(value, node.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }
    }

    final class ValueList extends AbstractCollection<V> {

        private final @NotNull K key;

        public ValueList(@NotNull K key) {
            delegate.computeIfAbsent(key, unused -> supplier.get());
            this.key = key;
        }

        @Override
        public @NotNull Iterator<V> iterator() {
            return new EntryIterator<V>() {
                @Override
                protected @NotNull List<Entry<K, V>> getEntrySet() {
                    return new ArrayList<>(delegate.get(key));
                }

                @Override
                protected V get(@NotNull Entry<K, V> entry) {
                    return entry.getValue();
                }
            };
        }

        @Override
        public int size() {
            return delegate.get(key).size();
        }
    }

    final class EntrySet extends AbstractSet<Map.Entry<K, V>> {
        @Override
        public @NotNull Iterator<Entry<K, V>> iterator() {
            return new EntryIterator<Entry<K, V>>() {
                @Override
                protected @NotNull List<Entry<K, V>> getEntrySet() {
                    List<Map.Entry<K, V>> entrySet = new ArrayList<>();
                    for (Entry<K, Collection<Entry<K, V>>> entry : delegate.entrySet()) {
                        entrySet.addAll(entry.getValue());
                    }
                    return entrySet;
                }

                @Override
                protected Entry<K, V> get(@NotNull Entry<K, V> entry) {
                    return entry;
                }
            };
        }

        @Override
        public int size() {
            List<Map.Entry<K, V>> entrySet = new ArrayList<>();
            for (Entry<K, Collection<Entry<K, V>>> entry : delegate.entrySet()) {
                entrySet.addAll(entry.getValue());
            }
            return entrySet.size();
        }
    }

    private abstract class EntryIterator<T> implements Iterator<T> {

        private final @NotNull List<Map.Entry<K, V>> entrySet;

        private Map.Entry<K, V> current, next;
        private int index;

        public EntryIterator() {
            this.entrySet = getEntrySet();
            if (!(entrySet.isEmpty())) {
                next = entrySet.get(0);
            }
        }

        protected abstract @NotNull List<Map.Entry<K, V>> getEntrySet();

        protected abstract T get(@NotNull Map.Entry<K, V> entry);

        @Override
        public void remove() {
            if (current == null) {
                throw new IllegalArgumentException();
            }
            entrySet.remove(current);
            delegate.get(current.getKey()).remove(current);
            current = null;
        }

        @Override
        public boolean hasNext() {
            return next != null;
        }

        @Override
        public T next() {
            if (next == null) {
                throw new NoSuchElementException();
            }
            index += 1;
            current = next;
            if (index >= entrySet.size()) {
                next = null;
            } else {
                next = entrySet.get(index);
            }
            return get(current);
        }
    }

}
