package org.butu.sugar.listeners;

/**
 * Интерфейс доступа к коллекции слушателей.
 * Создано: kbakaras, в день: 30.11.2017.
 */
public interface IListeners<L> {
    void addListener(L listener);
    void removeListener(L listener);
}
