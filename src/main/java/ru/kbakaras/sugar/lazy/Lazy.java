package ru.kbakaras.sugar.lazy;

import java.util.function.Supplier;

/**
 * Создано: kbakaras, в день: 23.11.2017.
 */
public class Lazy<T> {
    private T resource;
    private boolean virgin = true;

    private Supplier<T> supplier;

    public Lazy(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    public T get() {
        if (virgin) {
            resource = supplier.get();
            virgin = false;
        }
        return resource;
    }

    public boolean isVirgin() {
        return virgin;
    }

    /**
     * Вызывается при очистке объекта методом <i>clear()</i>. Базовая реализация не делает ничего.
     * Если необходимо выполнять специфические действия (помимо зачистки ленивого значения) данный
     * метод следует переопределить.
     */
    protected void doClear() {}

    /**
     * Возвращает объект к начальному (неинициированному) состоянию.
     */
    public void clear() {
        if (!virgin) {
            doClear();
            resource = null;
            virgin = true;
        }
    }

    public static <T> Lazy<T> of(Supplier<T> supplier) {
        return new Lazy<>(supplier);
    }
}