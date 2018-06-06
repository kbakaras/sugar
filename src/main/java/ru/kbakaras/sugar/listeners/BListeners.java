package ru.kbakaras.sugar.listeners;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

/**
 * Реализация коллекции слушателей (Listeners) для использования в Observable-классах.<br>
 * Содержит метод notify для уведомления слушателей о произошедших событиях. При уведомлении
 * информация передаётся в форме списка параметров для вызываемого метода слушателя (сам
 * метод указывается строковым именем).
 * @author kbakaras
 *
 * @param <L> Класс для слушателей.
 */
public class BListeners<L> {
    private Set<L> listeners;
    private Method[] methods;
    private String[] names;
    private Class<L> listenerInterface;

    public static class NotificationException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public NotificationException(Throwable cause) {
            super(cause);
        }
    }

    @SuppressWarnings("unchecked")
    public BListeners(Class<?> listenerInterface) {
        this.listenerInterface = (Class<L>) listenerInterface;
        methods = listenerInterface.getMethods();
        names = new String[methods.length];
        for (int i = 0; i < methods.length; i++) {
            String name = methods[i].getName();
            for (int j = 0; j < i; j++) {
                if (names[j].equals(name)) {
                    throw new UnsupportedOperationException("Перегруженные методы не поддерживаются!");
                }
            }
            names[i] = name;
        }
    }

    private Method getMethod(String name) {
        for (int i = 0; i < names.length; i++) {
            if (names[i].equals(name)) {
                return methods[i];
            }
        }
        return null;
    }

    protected void parentListenerAdd() {}
    protected void parentListenerRemove() {}

    public void addListener(L listener) {
        if (listener != null) {
            if (listeners == null) {
                listeners = new HashSet<L>();
                parentListenerAdd();
            }
            listeners.add(listener);
        }
    }
    public void removeListener(L listener) {
        if (listeners != null) {
            listeners.remove(listener);
            if (listeners.isEmpty()) {
                listeners = null;
                parentListenerRemove();
            }
        }
    }

    /**
     * Уведомление слушателей о возникшем событии.
     * @param event Строковое название метода слушателя, который необходимо вызвать.
     * @param args массив параметров для передачи в вызываемый метод
     */
    public void notifyListeners(String event, Object...args) {
        if (listeners != null) {
            Method method = getMethod(event);
            if (method != null) {
                for (L listener: listeners) {
                    try {
                        method.invoke(listener, args);
                    } catch (InvocationTargetException e) {
                        throw new NotificationException(e);
                    } catch (IllegalAccessException e) {
                        throw new NotificationException(e);
                    }
                }
            } else {
                throw new RuntimeException("Listener [" + listenerInterface.getSimpleName() + "] does not support method [" + event + "]!");
            }
        }
    }

    public boolean isEmpty() {
        return listeners == null || listeners.isEmpty();
    }

    /**
     * Метод предназначен для удобства создания объектов. Запись с помощью этого метода
     * получается короче, чем создание с помощбю конструктора.
     * @param clazz
     * @return
     */
    public static <L> BListeners<L> newInstance(Class<L> clazz) {
    	return new BListeners<L>(clazz);
    }
}