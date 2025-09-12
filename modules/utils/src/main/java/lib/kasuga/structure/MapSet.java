package lib.kasuga.structure;

import org.jetbrains.annotations.NotNull;

import java.util.*;

public class MapSet<K, V> implements Iterable<V> {
    private Map<K, Set<V>> map = new HashMap<>();

    public void put(K key, V value) {
        map.computeIfAbsent(key, k -> new HashSet<>()).add(value);
    }

    public Set<V> get(K key) {
        return map.getOrDefault(key, Collections.emptySet());
    }

    @NotNull
    @Override
    public Iterator<V> iterator() {
        return map.values().stream()
                .flatMap(Set::stream)
                .iterator();
    }
}
