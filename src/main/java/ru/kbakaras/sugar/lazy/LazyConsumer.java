package org.butu.sugar.lazy;

import java.util.function.Consumer;

/**
 * Декоратор, позволяющий сделать ленивого потребителя. Это потребитель,
 * который выполнится лишь однократно, при первом обращении.
 * Создано: kbakaras, в день: 05.03.2018.
 */
public class LazyConsumer<T> implements Consumer<T> {
    private boolean virgin = true;

    private Consumer<T> consumer;

    public LazyConsumer(Consumer<T> consumer) {
        this.consumer = consumer;
    }

    @Override
    public void accept(T t) {
        if (virgin) {
            consumer.accept(t);
            virgin = false;
        }
    }

    public static <T> LazyConsumer<T> of (Consumer<T> consumer) {
        return new LazyConsumer<>(consumer);
    }
}