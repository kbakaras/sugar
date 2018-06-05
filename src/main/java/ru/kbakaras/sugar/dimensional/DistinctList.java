package org.butu.sugar.dimensional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.Collectors;

/**
 * Создано: kbakaras, в день: 05.12.2017.
 */
public class DistinctList<E> extends ArrayList<E> {
    private HashSet<E> set = new HashSet<>();

    public DistinctList() {}
    public DistinctList(int initialCapacity) {
        super(initialCapacity);
    }
    public DistinctList(Collection<? extends E> c) {
        super(c.stream().distinct().collect(Collectors.toList()));
        set.addAll(this);
    }

    public E ensure(E element) {
        if (!set.contains(element)) add(element);
        return element;
    }

    @Override
    public boolean contains(Object o) {
        return set.contains(o);
    }

    @Override
    public Iterator<E> iterator() {
        // TODO Обернуть итератор
        return super.iterator();
    }

    @Override
    public boolean add(E e) {
        if (!set.contains(e)) {
            set.add(e);
            return super.add(e);
        }
        return false;
    }

    @Override
    public boolean remove(Object o) {
        if (set.contains(o)) {
            set.remove(o);
            return super.remove(o);
        }
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return set.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        List<? extends E> list = filter(c);
        if (!list.isEmpty()) {
            set.addAll(list);
            return super.addAll(list);
        }
        return false;
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        List<? extends E> list = filter(c);
        if (!list.isEmpty()) {
            set.addAll(list);
            return super.addAll(index, list);
        }
        return false;
    }

    private <T> List<T> filter(Collection<T> c) {
        return c.stream().filter(o -> !set.contains(o)).collect(Collectors.toList());
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        List<?> list = filter(c);
        if (!list.isEmpty()) {
            set.removeAll(list);
            return super.removeAll(c);
        }
        return false;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        set.retainAll(c);
        return super.retainAll(c);
    }

    @Override
    public void clear() {
        set.clear();
        super.clear();
    }

    /**
     * Замена на новый элемента, находящегося по указанному индексу.
     * Если новый элемент уже присутствовал в списке на указанном месте, ничего не происходит,
     * если он присутствовал на другом месте, элементы меняются местами. Если вставляемый элемент
     * отсутствовал ранее, он заменяет элемент, находящийся на месте вставки.
     * @param index Позиция для вставки
     * @param element Вставляемый элемент
     * @return Элемент, занимавший позицию до замены
     */
    @Override
    public E set(int index, E element) {
        int oldIndex = indexOf(element);

        if (oldIndex == -1) {
            set.remove(get(index));
            return super.set(index, element);
        } else if (index == oldIndex) {
            return super.set(index, element);
        } else {
            super.set(oldIndex, get(index));
            return super.set(index, element);
        }
    }

    @Override
    public void add(int index, E element) {
        int oldIndex = indexOf(element);

        if (oldIndex == -1) {
            set.add(element);
            super.add(element);
        } else if (oldIndex < index) {
            super.add(index, element);
            super.remove(oldIndex);
        } else if (oldIndex > index) {
            super.remove(element);
            super.add(index, element);
        } else {
            super.set(index, element);
        }
    }

    @Override
    public E remove(int index) {
        set.remove(super.get(index));
        return super.remove(index);
    }

    @Override
    public int indexOf(Object o) {
        return set.contains(o) ? super.indexOf(o) : -1;
    }

    @Override
    public int lastIndexOf(Object o) {
        return indexOf(o);
    }

    @Deprecated
    @Override
    public ListIterator<E> listIterator() {
        return super.listIterator();
    }

    @Deprecated
    @Override
    public ListIterator<E> listIterator(int index) {
        return super.listIterator(index);
    }

    @Deprecated
    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        //TODO Что-то нужно сделать
        return super.subList(fromIndex, toIndex);
    }
}