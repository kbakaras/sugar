package ru.kbakaras.sugar.listeners.test;

import org.junit.jupiter.api.Test;
import ru.kbakaras.sugar.listeners.IListeners;
import ru.kbakaras.sugar.listeners.ListenersHandler;
import ru.kbakaras.sugar.listeners.ListenersParent;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ListenersHandlerTest {
    private boolean notified = false;
    private boolean parentCreated = false;

    @Test
    void testAddNotifyRemove() throws NoSuchFieldException, IllegalAccessException {
        IListeners<ListenerTest> listeners = (IListeners<ListenerTest>) ListenersHandler.of(ListenerTest.class);
        ListenerTest notifier = ListenersHandler.notifierOf(listeners);

        Field fieldListeners = ListenersHandler.class.getDeclaredField("listeners");
        fieldListeners.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<ListenerTest> list = (List<ListenerTest>) fieldListeners.get(Proxy.getInvocationHandler(listeners));

        ListenerTest listener = this::contentChanged;

        listeners.addListener(listener);
        assertTrue(list.size() == 1);

        notifier.contentChanged();
        assertTrue(notified);

        listeners.removeListener(this::contentChanged);
        assertFalse(list.size() == 0);

        listeners.removeListener(listener);
        assertTrue(list.size() == 0);
    }

    ListenerTest notifier1 = null;

    @Test
    void testParentAddRemove() {
        IListeners<ListenerTest> listeners = (IListeners<ListenerTest>) ListenersHandler.of(ListenerTest.class,
                new ListenersParent<ListenerTest>() {
                    @Override
                    public void addListenerParent(ListenerTest notifier) {
                        notifier1 = notifier;
                    }
                    @Override
                    public void removeListenerParent(ListenerTest notifier) {
                        notifier1 = null;
                    }
                });
        ListenerTest notifier = ListenersHandler.notifierOf(listeners);

        ListenerTest listener = this::contentChanged;

        listeners.addListener(listener);

        assertTrue(notifier1 == notifier);

        listeners.removeListener(listener);

        assertTrue(notifier1 == null);
    }

    private void contentChanged() {
        notified = true;
    }

    public interface ListenerTest {
        void contentChanged();
    }
}