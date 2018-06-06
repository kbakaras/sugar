package ru.kbakaras.sugar.listeners;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Set;

/**
 * Класс-обёртка для множеств, позволяющий получать уведомления при изменении состояния множества
 * @author kbakaras
 */
public class SetWrapper<E> implements Serializable, Iterable<E> {
	private static final long serialVersionUID = 1L;

	private Set<E> set;

	public SetWrapper(Set<E> list) {
		this.set = list;
	}

    public boolean add(E element) {
    	boolean flag = set.add(element);
    	if (flag) notifyListeners();
    	return flag;
    }
    public boolean remove(E element) {
    	boolean flag = set.remove(element);
    	if (flag) notifyListeners();
    	return flag;
    }

    public boolean contains(E element) {
    	return set.contains(element);
    }

	public void clear() {
		set.clear();
		notifyListeners();
	}

	public int size() {
		return set.size();
	}
	public boolean isEmpty() {
		return set.isEmpty();
	}

    transient private BListeners<StateChangeListener> listeners;
    public void addListener(StateChangeListener listener) {
		if (listeners == null) {
			listeners = BListeners.newInstance(StateChangeListener.class);
		}
    	listeners.addListener(listener);
    }
    public void removeListener(StateChangeListener listener) {
    	if (listeners != null) {
        	listeners.removeListener(listener);
    	}
    }
    protected void notifyListeners() {
    	if (listeners != null) {
        	listeners.notifyListeners(StateChangeListener.EVT_stateChanged);
    	}
	}

	public Iterator<E> iterator() {
		return new IteratorWrapper();
	}

	/**
	 * @return Прямую ссылку на объект. Без обёртки. Необходим для вызова методов множества
	 * без уведомления слушателей. Нужен, например, при создании клона объекта. В обычном
	 * клиентском коде потребность вызывать данный метод мало вероятна.
	 */
	public Set<E> getRawSet() {
		return set;
	}

    private class IteratorWrapper implements Iterator<E> {
        private Iterator<E> setIterator;

        private IteratorWrapper() {
            setIterator = set.iterator();
        }

        public boolean hasNext() {
            return setIterator.hasNext();
        }

        public E next() {
            return setIterator.next();
        }

        public void remove() {
            setIterator.remove();
            notifyListeners();
        }
    }
}