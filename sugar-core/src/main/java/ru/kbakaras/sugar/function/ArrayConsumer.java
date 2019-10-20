package ru.kbakaras.sugar.function;

import java.util.Objects;

/**
 * Создано: kbakaras, в день: 03.04.2018.
 */
@FunctionalInterface
public interface ArrayConsumer<T> {
    void accept(T...t);

    default ArrayConsumer<T> andThen(ArrayConsumer<? super T> after) {
        Objects.requireNonNull(after);
        return (T...t) -> { accept(t); after.accept(t); };
    }
}