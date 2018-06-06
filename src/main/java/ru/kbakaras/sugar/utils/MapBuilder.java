package ru.kbakaras.sugar.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MapBuilder<K, V> {
    private Map<K, V> map;

    private MapBuilder(K key, V value) {
        map = new HashMap<>();
        map.put(key, value);
    }

    public static <K, V> MapBuilder<K, V> map(K key, V value) {
        return new MapBuilder<K, V>(key, value);
    }

    public MapBuilder<K, V> put(K key, V value) {
        map.put(key, value);
        return this;
    }

    public Map<K, V> get() {
        return map;
    }

    public Map<K, V> getUnmodifiable() {
        return Collections.unmodifiableMap(map);
    }
}