package ru.kbakaras.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.function.Consumer;
import java.util.function.Supplier;

public interface SugarRepository<T, ID> extends JpaRepository<T, ID> {
    <D extends T> D findCreate(ID id, Supplier<D> instanceSupplier, Consumer<D> instanceSetup);
}
