package ru.kbakaras.sugar.lazy;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Карта, значения которой заполняются лениво при первом запросе.
 * За заполнение значений ответчает функция, передаваемая в параметре конструктора.
 */
@SuppressWarnings("unused")
public class MapCache<K, V> implements Iterable<Map.Entry<K, V>> {

    private Function<K, V> function;
    private Map<K, V> map = new HashMap<>();

    @SuppressWarnings("WeakerAccess")
    public MapCache(Function<K, V> function) {
        this.function = function;
    }

    public V get(K key) {
        return get(key, null);
    }
    public V get(K key, Consumer<V> postProcess) {
        if (map.containsKey(key)) {
            return map.get(key);

        } else {
            V object = function.apply(key);
            map.put(key, object);

            if (postProcess != null) {
                postProcess.accept(object);
            }

            return object;
        }
    }

    /**
     * @see Map#put(Object, Object)
     */
    public V put(K key, V value) {
        return map.put(key, value);
    }

    public Map<K, V> getMap() {
        return map;
    }

    /**
     * Удаление из кэша единственного элемента.
     */
    public void remove(K key) {
        map.remove(key);
    }

    /**
     * Удаляет из кэша все ключи, значение для которых <b>null</b>.
     */
    public void removeNulls() {
        map.entrySet().removeIf(entry -> entry.getValue() == null);
    }

    /**
     * Очистка кэша.
     */
    public void clear() {
        map.clear();
    }

    public boolean contains(K key) {
    	return map.containsKey(key);
    }
    public boolean isEmpty() {
        return map.isEmpty();
    }


    @Override
    public Iterator<Entry<K, V>> iterator() {
        return map.entrySet().iterator();
    }


    public static <K, V> MapCache<K, V> of(Function<K, V> function) {
        return new MapCache<>(function);
    }

}