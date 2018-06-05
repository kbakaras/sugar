package org.butu.sugar.dimensional;

import java.util.List;
import java.util.function.Function;

@FunctionalInterface
public interface Integral<T, K, K1, V> {
    T compute(K keyTotal, List<K1> keyList, Function<K1, V> get);
}