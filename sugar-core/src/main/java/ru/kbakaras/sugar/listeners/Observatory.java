package ru.kbakaras.sugar.listeners;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Observatory<L> implements Listeners<L> {

    private List<L> listeners = new ArrayList<>();


    public void add(L listener) {
        listeners.add(listener);
    }

    public void remove(L listener) {
        listeners.remove(listener);
    }


    public void notifyAll(Consumer<L> notification) {
        listeners.forEach(notification);
    }

    public void notify(L excluded, Consumer<L> notification) {
        for (L listener : listeners) {
            if (listener != excluded) {
                notification.accept(listener);
            }
        }
    }

}
