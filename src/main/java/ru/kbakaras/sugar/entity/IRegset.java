package org.butu.sugar.entity;

import org.butu.sugar.listeners.StateChangeListener;

/**
 * Создано: kbakaras, в день: 24.11.2017.
 */
public interface IRegset<R extends IReg> {
    void addListener(StateChangeListener listener);
    void removeListener(StateChangeListener listener);

    R[] toArray();
}
