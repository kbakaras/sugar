package ru.kbakaras.sugar.listeners;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Создано: kbakaras, в день: 30.11.2017.
 */
public class ListenersHandler<L> implements InvocationHandler {
    private List<L> listeners = new ArrayList<>();
    private ListenersParent<L> parent;
    private Map<Method, Invoker> map = new HashMap<>();

    private void notify(Method method, Object[] args) {
        listeners.forEach(listener -> {
            try {
                method.invoke(listener, args);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void addListener(L listener, L notifier) {
        if (listener != null) {
            if (listeners.isEmpty() && parent != null) {
                parent.addListenerParent(notifier);
            }
            listeners.add(listener);
        }
    }
    private void removeListener(L listener, L notifier) {
        if (listener != null) {
            listeners.remove(listener);
            if (listeners.isEmpty() && parent != null) {
                parent.removeListenerParent(notifier);
            }
        }
    }

    @SuppressWarnings({"unchecked", "SuspiciousMethodCalls"})
    private ListenersHandler(Class<L> listenerClass, ListenersParent<L> parent) {
        this.parent = parent;
        try {
            map.put(IListeners.class.getDeclaredMethod("addListener", Object.class),
                    (proxy, method, objects) -> addListener((L) objects[0], (L) proxy));
            map.put(IListeners.class.getDeclaredMethod("removeListener", Object.class),
                    (proxy, method, objects) -> removeListener((L) objects[0], (L) proxy));
            for (Method listenerMethod: listenerClass.getMethods()) {
                map.put(listenerMethod,
                        (proxy, method, objects) -> notify(method, objects));
            }
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        Invoker call = map.get(method);
        if (call != null) {
            call.invoke(proxy, method, args);
        } else if (method.getDeclaringClass().equals(Object.class)) {
            try {
                return method.invoke(this, args);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public static IListeners<?> of(Class<?> listenerClass, ListenersParent<?> parent) {
        return (IListeners<?>) Proxy.newProxyInstance(
                ListenersHandler.class.getClassLoader(),
                new Class[]{listenerClass, IListeners.class},
                new ListenersHandler(listenerClass, parent));
    }
    public static IListeners<?> of(Class<?> listenerClass) {
        return of(listenerClass, null);
    }

    @SuppressWarnings("unchecked")
    public static <L> IListeners<L> ofType(Class<L> listenerClass, ListenersParent<L> parent) {
        return (IListeners<L>) of(listenerClass, parent);
    }
    public static <L> IListeners<L> ofType(Class<L> listenerClass) {
        return ofType(listenerClass, null);
    }

    @SuppressWarnings("unchecked")
    public static <T> IListeners<Consumer<T>> ofConsumer(Class<T> typeClass) {
        return (IListeners<Consumer<T>>) of(Consumer.class);
    }

    @SuppressWarnings("unchecked")
    public static <T> IListeners<Consumer<T>> ofConsumer(Class<T> typeClass, ListenersParent<Consumer<T>> parent) {
        return (IListeners<Consumer<T>>) of(Consumer.class, parent);
    }

    @SuppressWarnings("unchecked")
    public static <L> L notifierOf(IListeners<L> listeners) {
        if (listeners instanceof Proxy) {
            return (L) listeners;
        } else {
            throw new IllegalArgumentException("Argument must be listeners handling Proxy");
        }
    }

    @FunctionalInterface
    private interface Invoker {
        void invoke(Object proxy, Method method, Object[] args);
    }
}