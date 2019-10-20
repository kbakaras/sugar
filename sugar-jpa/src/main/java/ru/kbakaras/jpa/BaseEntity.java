package ru.kbakaras.jpa;

import ru.kbakaras.sugar.entity.IEntity;

import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Version;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

@MappedSuperclass
public abstract class BaseEntity<ID> implements IEntity<ID> {
    @Id
    protected ID id;

    @Version
    protected int version;


    @Override
    public ID getId() {
        return id;
    }
    @Override
    public void setId(ID id) {
        this.id = id;
    }

    public int getVersion() {
        return version;
    }


    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)) {
            return true;
        } else {
            if (this.getClass().equals(obj.getClass())) {
                return this.getId().equals(((BaseEntity) obj).getId());
            }
            return false;
        }
    }


    /**
     * Метод предназначен для инициализации нового создаваемого элемента.
     * Данный метод будет вызван в процессе создания нового элемента
     * с помощью фабричного статического метода {@link BaseEntity#newElement(Class)}.</br>
     * Дочерние классы могут переопределять данный метод для специфической инициализации.
     */
    protected abstract void newElement();

    /**
     * Фабричный метод, предназначенный для создания новых элементов.
     * Создаёт новый экземпляр с помощью конструктора без параметров из указанного
     * класса, а затем вызывает метод {@link #newElement()} для выполнения инициализации.<br/>
     * Конструктор класса не обязательно должен быть публичным.
     * @param clazz Класс-сущность, новый элемент которой нужно создать.
     */
    public static <E extends ProperEntity> E newElement(Class<E> clazz) {
        try {
            E element;

            Constructor<E> constructor = clazz.getDeclaredConstructor();

            if (constructor.isAccessible()) {
                element = constructor.newInstance();
            } else {
                constructor.setAccessible(true);
                element = constructor.newInstance();
                constructor.setAccessible(false);
            }

            element.newElement();
            return element;

        } catch (InstantiationException   |
                IllegalAccessException    |
                NoSuchMethodException     |
                InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}