package org.butu.sugar.entity;

import java.io.Serializable;

/**
 * Базовый интерфейс для всех сущностей.
 * @param <K> Тип для первичного ключа сущности
 */
public interface IEntity<K> extends Serializable {
    K getId();
    void setId(K id);
}