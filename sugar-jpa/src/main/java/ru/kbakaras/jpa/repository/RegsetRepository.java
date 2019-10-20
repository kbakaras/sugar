package ru.kbakaras.jpa.repository;

import ru.kbakaras.jpa.Regset;
import ru.kbakaras.sugar.entity.IReg;

public interface RegsetRepository<R extends IReg> {
    void save(Regset<R> regset);
    void save(Regset<R> regset, int portion);
}