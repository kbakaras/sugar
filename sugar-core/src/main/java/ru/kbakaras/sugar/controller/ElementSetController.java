package ru.kbakaras.sugar.controller;

import ru.kbakaras.sugar.listeners.Listeners;
import ru.kbakaras.sugar.listeners.Observatory;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Контроллер (в терминах MVC), обеспечивающий взаимодействие с моделью,
 * представляющей собой набор некоторых элементов. Используется для редактирования
 * набора (множества) значений: выбор (галочками) элементов из некоторого списка,
 * подбор элементов из некоторого исходного множества для формирования
 * результирующего подмножества. Содержащиеся элементы повторяться не могут.
 *
 * @param <E> Параметр для типизации элементов
 */
public class ElementSetController<E> {

    private final Set<E> modelModifiable;
    public final Set<E> model;

    public final Listeners<Consumer<Set<E>>> listeners;
    private final Consumer<Consumer<Set<E>>> notification;

    public ElementSetController(Set<E> contents) {
        modelModifiable = contents != null ? contents : new HashSet<>();
        model = Collections.unmodifiableSet(this.modelModifiable);

        Observatory<Consumer<Set<E>>> observatory = new Observatory<>();
        this.listeners = observatory;
        this.notification = excluded ->
                observatory.notify(excluded, listener -> listener.accept(model));
    }
    public ElementSetController() {
        this(null);
    }


    public void invert(Consumer<Set<E>> excludedListener, E element) {

        if (modelModifiable.contains(element)) {
            modelModifiable.remove(element);
        } else {
            modelModifiable.add(element);
        }

        notification.accept(excludedListener);

    }

    public void change(Consumer<Set<E>> excludedListener, E element, boolean add) {

        if (modelModifiable.contains(element) != add) {
            if (add) {
                modelModifiable.add(element);
            } else {
                modelModifiable.remove(element);
            }

            notification.accept(excludedListener);
        }

    }

    public void addAll(Consumer<Set<E>> excludedListener, Collection<E> elements) {
        if (modelModifiable.addAll(elements)) {
            notification.accept(excludedListener);
        }
    }

    public void clear(Consumer<Set<E>> excludedListener) {
        if (!modelModifiable.isEmpty()) {
            modelModifiable.clear();
            notification.accept(excludedListener);
        }
    }

    /**
     * Полностью замещает старый набор значений новым.
     * @param excludedListener Слушатель, который должен быть исключён из оповещения
     * @param elements         Новый набор значений
     */
    public void reset(Consumer<Set<E>> excludedListener, Collection<E> elements) {
        if (elements != null && !elements.isEmpty()) {
            modelModifiable.clear();
            modelModifiable.addAll(elements);
            notification.accept(excludedListener);
        } else {
            clear(excludedListener);
        }
    }

}
