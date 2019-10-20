package ru.kbakaras.jpa;

import javax.persistence.MappedSuperclass;
import java.util.UUID;

/**
 * Базовый класс для (правильных) сущностей. Типизирует BaseClass значением
 * параметра UUID. То есть, ключом для сущности будет являться UUID.
 */
@MappedSuperclass
public abstract class ProperEntity extends BaseEntity<UUID> {
    @Override
    protected void newElement() {
        setId(UUID.randomUUID());
    }
}