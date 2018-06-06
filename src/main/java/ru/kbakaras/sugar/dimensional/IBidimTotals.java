package ru.kbakaras.sugar.dimensional;

public interface IBidimTotals<K, T> {
    T get(int index);
    T get(K key);
}