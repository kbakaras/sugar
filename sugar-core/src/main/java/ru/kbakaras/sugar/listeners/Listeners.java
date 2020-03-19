package ru.kbakaras.sugar.listeners;

public interface Listeners<L> {
    void add(L listener);
    void remove(L listener);
}
