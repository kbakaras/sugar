package org.butu.sugar.function;

import java.util.Objects;

/**
 * РЎРѕР·РґР°РЅРѕ: kbakaras, РІ РґРµРЅСЊ: 03.04.2018.
 */
@FunctionalInterface
public interface ArrayConsumer<T> {
    void accept(T...t);

    default ArrayConsumer<T> andThen(ArrayConsumer<? super T> after) {
        Objects.requireNonNull(after);
        return (T...t) -> { accept(t); after.accept(t); };
    }
}