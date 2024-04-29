package com.bossymr.network;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * A {@code MultiMap} is a map which can contain multiple values for a single key. This map is similar to a
 * {@code Map<K, List<V>>}, however, it still represents each mapping separately.
 *
 * @param <K> the type of keys.
 * @param <V> the type of values.
 */
public class MultiMap<K, V> extends AbstractMap<K, V> {

    private final Map<K, Collection<Entry<K, V>>> delegate;

    private final Supplier<? extends Collection<Entry<K, V>>> supplier;

    /**
     * Creates a new {@code MultiMap} which stores mappings in instances of {@link ArrayList}. This means that multiple
     * identical mappings are allowed.
     */
    public MultiMap() {
        this(ArrayList::new);
    }

    @Override
    public V put(K key, V value) {
        delegate.computeIfAbsent(key, k -> supplier.get()).add(new Node<>(key, value));
        return null;
    }

    public void putAll(K key, Collection<V> values) {
        Collection<Entry<K, V>> entries = delegate.computeIfAbsent(key, k -> supplier.get());
        for (V value : values) {
            entries.add(new Node<>(key, value));
        }
    }

    public void set(K key, V value) {
        Collection<Entry<K, V>> entries = delegate.computeIfAbsent(key, unused -> supplier.get());
        entries.clear();
        entries.add(new Node<>(key, value));
    }

    public @NotNull Collection<V> getAll(@NotNull K key) {
        return new ValueList(key);
    }

    @Override
    public @NotNull Set<Entry<K, V>> entrySet() {
        return new EntrySet();
    }

    /**
     * Creates a new {@code MultiMap} which stores mappings in instances of the specified supplier. The collections
     * provided by this method must be mutable.
     *
     * @param supplier a supplier which returns instances of a collection.
     */
    public MultiMap(@NotNull Supplier<? extends Collection<Entry<K, V>>> supplier) {
        this.delegate = new HashMap<>();
        this.supplier = supplier;
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
        public V setValue(V value) {
            V previousValue = this.value;
            this.value = value;
            return previousValue;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Node<?, ?> node = (Node<?, ?>) o;
            return Objects.equals(key, node.key) && Objects.equals(value, node.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(key, value);
        }

        @Override
        public String toString() {
            return key + "=" + value;
        }
    }

    /**
     * A {@code List} which encompasses all values mapped to a specific key.
     */
    private final class ValueList extends AbstractCollection<V> {

        private final @NotNull K key;

        public ValueList(@NotNull K key) {
            this.key = key;
        }

        @Override
        public @NotNull Iterator<V> iterator() {
            if (!delegate.containsKey(key)) {
                return Collections.emptyIterator();
            }
            return new EntryIterator<V>(new ArrayList<>(delegate.get(key))) {
                @Override
                protected V get(@NotNull Entry<K, V> entry) {
                    return entry.getValue();
                }
            };
        }

        @Override
        public int size() {
            if (!delegate.containsKey(key)) {
                return 0;
            }
            return delegate.get(key).size();
        }
    }

    /**
     * A {@code Set} which encompasses all entries in this map.
     */
    private final class EntrySet extends AbstractSet<Map.Entry<K, V>> {

        @Override
        public @NotNull Iterator<Entry<K, V>> iterator() {
            List<Entry<K, V>> entrySet = delegate.values().stream()
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
            return new EntryIterator<Entry<K, V>>(entrySet) {
                @Override
                protected Entry<K, V> get(@NotNull Entry<K, V> entry) {
                    return entry;
                }
            };
        }

        @Override
        public int size() {
            return delegate.values().stream()
                    .mapToInt(Collection::size)
                    .sum();
        }
    }

    /**
     * An {@code Iterator} which iterates through a subset of entries.
     *
     * @param <T> the return type of the iterator.
     */
    private abstract class EntryIterator<T> implements Iterator<T> {

        private final @NotNull List<Map.Entry<K, V>> entrySet;

        private Map.Entry<K, V> current, next;
        private int index;

        /**
         * Creates a new {@code EntryIterator} which will iterate through the specified subset.
         *
         * @param entrySet the subset of entries to iterate through.
         */
        public EntryIterator(@NotNull List<Map.Entry<K, V>> entrySet) {
            this.entrySet = entrySet;
            if (!(entrySet.isEmpty())) {
                next = entrySet.get(0);
            }
        }

        /**
         * Computes the value corresponding to the current entry.
         *
         * @param entry the current entry.
         * @return the value corresponding to this entry.
         */
        protected abstract T get(@NotNull Map.Entry<K, V> entry);

        @Override
        public void remove() {
            if (current == null) {
                // #next() has not been called yet or #remove() has already been called for this entry
                throw new IllegalArgumentException();
            }
            // We need to remove the element both from the specified entrySet, and from the actual underlying map
            // Since the map is made up of many lists, the specified entrySet can't be the same as delegate#entrySet().
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
