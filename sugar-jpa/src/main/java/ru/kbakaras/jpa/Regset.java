package ru.kbakaras.jpa;

import org.springframework.util.Assert;
import ru.kbakaras.sugar.compare.Equivalence;
import ru.kbakaras.sugar.entity.IEntity;
import ru.kbakaras.sugar.entity.IReg;
import ru.kbakaras.sugar.listeners.SetWrapper;
import ru.kbakaras.sugar.listeners.StateChangeListener;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

/**
 * Базовый класс, на основе которого реализуется работа с детальными записями в Master/Detail-ситуациях.
 * Например: строки документа, записи регистра остатков, роспись и т.п.
 * @author kbakaras
 *
 * @param <E>
 */
public class Regset<E extends IReg> implements Serializable, Iterable<E>, Cloneable {
    private static final long serialVersionUID = 1L;

    private SetWrapper<E> list;
    private Set<E> deleted;

    private BiConsumer<E, E> replaceValue;
    private BiPredicate<E, E> addValue;

    private Regset(Collection<E> list) {
        if (list == null) {
            this.list = new SetWrapper<E>(new HashSet<E>());
        } else if (list instanceof HashSet) {
			this.list = new SetWrapper<E>((HashSet<E>) list);
        } else {
			this.list = new SetWrapper<E>(new HashSet<E>(list));
        }
    }

    public class RSIterator implements Iterator<E> {
        private Iterator<E> setIterator;
        private E last;

        private RSIterator() {
            setIterator = list.iterator();
        }

        public boolean hasNext() {
            return setIterator.hasNext();
        }

        public E next() {
            last = setIterator.next();
            return last;
        }

        public void remove() {
            remember(last);
            setIterator.remove();
        }
        
    }

    /**
     * Помещает удаляемый элемент в специальный список.
     * @param element Удаляемый элемент.
     */
    protected void remember(E element) {
        if (element.getId() != null) {
            if (deleted == null) deleted = new HashSet<E>();
            deleted.add(element);
        }
    }
    public void delete(E element) {
        if (list.contains(element)) {
            remember(element);
            list.remove(element);
        }
    }

    /**
     * Сначала выполняется поиск элемента среди удалённых. Если удаётся найти,
     * вызывается метод для замены значений replaceValue. После этого элемент
     * восстанавливается из удалённых и добавляется в основной список.</br>
     * Если среди удалённых эквивалентный элемент не обнаружен, происходит
     * непосредственное добавление в основной список.
     * @param reg Элемент для добавления
     */
    public void add(E reg) {
        if (deleted != null) {
            Iterator<E> iterator = deleted.iterator();
            while (iterator.hasNext()) {
                E dr = iterator.next();
                if (dr.equivalent(reg)) {
                    replaceValue.accept(dr, reg);
                    list.add(dr);
                    iterator.remove();
                    return;
                }
            }
        }
        list.add(reg);
    }

    /**
     * Сначала будет выполнен поиск элемента среди существующих по признакам эквивалентности.
     * Если удастся найти эквивалентный элемент, будет вызван метод addValue для инкрементации
     * значения. Если эквивалентный элемент найти не удаётся, осуществляется FallBack на
     * метод add.
     * @param reg Элемент для добавления
     */
    public void addSmart(E reg) {
        if (!(reg instanceof Equivalence)) throw new IllegalArgumentException();

        Iterator<E> iterator = list.iterator();
        while (iterator.hasNext()) {
            E found = iterator.next();
            if (((Equivalence) found).equivalent(reg)) {
                if (!addValue.test(found, reg)) {
                    remember(found);
                    iterator.remove();
                }
                return;
            }
        }

        add(reg);
    }

    public boolean isEmpty() {
        return list.isEmpty();
    }
    public int size() {
        return list.size();
    }

    public E get(int index) {
		return null;
	}

    public boolean hasDeleted() {
        return deleted != null && !deleted.isEmpty();
    }
    public Set<E> getDeleted() {
        return deleted;
    }
    public void clearDeleted() {
        if (deleted != null) deleted.clear();
    }

    /**
     * Помечает все элементы как удалённые
     */
    public Regset<E> clear() {
        for (E element: list) {
            remember(element);
        }
        list.clear();
        return this;
    }

    /**
     * Помечает удовлетворяющие условию элементы, как удалённые
     */
    public void clearIf(Predicate<E> cond) {
    	List<E> deleting = new ArrayList<E>();
		for (E reg : this) {
			if (cond.test(reg)) {
				deleting.add(reg);
			}
		}
		if (!deleting.isEmpty()) {
			for (E element : deleting) {
				delete(element);
			}
		}
    }

    public Iterator<E> iterator() {
        return new RSIterator();
    }

    public Object[] toArray() {
        return list.getRawSet().toArray();
    }
    public <T extends IEntity> T[] toArray(T[] a) {
        return list.getRawSet().toArray(a);
    }

    public void addListener(StateChangeListener listener) {
    	list.addListener(listener);
    }
    public void removeListener(StateChangeListener listener) {
    	list.removeListener(listener);
    }

    @SuppressWarnings("unchecked")
    public Regset<E> clone() {
        try {
            Regset<E> rs = (Regset<E>)super.clone();

            Method method = null;
            if (deleted != null) {
                rs.deleted = deleted.getClass().newInstance();
                for (E element: deleted) {
                    if (method == null) {
                        method = element.getClass().getMethod("clone");
                    }
                    rs.deleted.add((E)method.invoke(element));
                }
            }
            if (list != null) {
                rs.list = list.getClass().newInstance();
                for (E element: list) {
                    if (method == null) {
                        method = element.getClass().getMethod("clone");
                    }
                    rs.list.getRawSet().add((E)method.invoke(element));
                }
            }

            return rs;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (SecurityException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean replacingAdd(E found, E item) {
        replaceValue.accept(found, item);
        return true;
    }

    /**
     * @param list         Исходные данные (любая коллекция). Если null, будет создан
     *                     пустой регсет.
     * @param replaceValue Вызывается из процедуры добавления элемента, в случае, когда
     *                     эквивалентный элемент обнаружен среди помеченных на удаление
     *                     объектов и необходимо вернуть его оттуда в основной набор
     *                     заменив при этом значение на значение из добавляемого элемента.
     *                     Первый аргумент - это элемент, найденный внутри регсета, второй
     *                     аргумент - элемент переданный для добавления или замены в
     *                     соответствующий метод регсета.
     * @param addValue     Вызывается из процедуры добавления элемента, в случае, когда
     *                     эквивалентный элемент обнаружен в наборе и необходимо произвести
     *                     сложение значений. Это предикат, возвращающий <b>true</b>, если
     *                     в результате добавления получилось ненулевое значение, в противном
     *                     случе необходимо вернуть значение <b>false</b> (следовательно,
     *                     значение будет удалено из набора). Первый аргумент - элемент,
     *                     обнаруженный в наборе. К значению этого элемента необходимо добавить
     *                     значение из добавляемого элемента. Второй элемент - добавляемый элемент.
     * @return Регсет, умеющий складывать значения, проинициализированный переданными данными.
     */
    public static <E extends IReg> Regset<E> create(Collection<E> list, BiConsumer<E, E> replaceValue, BiPredicate<E, E> addValue) {
        Assert.notNull(replaceValue, "Non null Replacer must be specified!");
        Assert.notNull(addValue, "Non null Adder must be specified!");

        Regset<E> regset = new Regset<>(list);
        regset.replaceValue = replaceValue;
        regset.addValue     = addValue;
        return regset;
    }

    /**
     * Регсет, умеющий только заменять значения.
     * Аналогичен {@link Regset#create(Collection, BiConsumer, BiPredicate)}, но без
     * предиката для сложения значений.
     */
    public static <E extends IReg> Regset<E> create(Collection<E> list, BiConsumer<E, E> replaceValue) {
        Assert.notNull(replaceValue, "Non null Replacer must be specified!");

        Regset<E> regset = new Regset<>(list);
        regset.replaceValue = replaceValue;
        regset.addValue     = regset::replacingAdd;
        return regset;
    }

    /**
     * replace-метод для тех случаев, когда ничего не должно происходить при замене,
     * в регсете просто остаётся найденный элемент, как есть.
     */
    public static <E extends IReg> void replaceValueNoop(E found, E item) {}
}