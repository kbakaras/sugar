package org.butu.sugar.listeners.test;

import org.butu.sugar.listeners.IListeners;
import org.butu.sugar.listeners.ListenersHandler;
import org.butu.sugar.listeners.ListenersParent;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.List;

/**
 * Создано: kbakaras, в день: 30.11.2017.
 */
public class ListenersHandlerTest {
    private boolean notified = false;
    private boolean parentCreated = false;

    @Test
    public void testAddNotifyRemove() throws NoSuchFieldException, IllegalAccessException {
        IListeners<ListenerTest> listeners = (IListeners<ListenerTest>) ListenersHandler.of(ListenerTest.class);
        ListenerTest notifier = ListenersHandler.notifierOf(listeners);

        Field fieldListeners = ListenersHandler.class.getDeclaredField("listeners");
        fieldListeners.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<ListenerTest> list = (List<ListenerTest>) fieldListeners.get(Proxy.getInvocationHandler(listeners));

        ListenerTest listener = this::contentChanged;

        listeners.addListener(listener);
        Assert.assertTrue("Добавление слушателя", list.size() == 1);

        notifier.contentChanged();
        Assert.assertTrue("Вызов слушателей", notified);

        listeners.removeListener(this::contentChanged);
        Assert.assertFalse("Удаление недобавленного слушателя", list.size() == 0);

        listeners.removeListener(listener);
        Assert.assertTrue("Удаление добавленного слушателя", list.size() == 0);
    }

    ListenerTest notifier1 = null;

    @Test
    public void testParentAddRemove() {
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

        Assert.assertTrue("Подключение родительского слушателя", notifier1 == notifier);

        listeners.removeListener(listener);

        Assert.assertTrue("Отключение родительского слушателя", notifier1 == null);
    }

    private void contentChanged() {
        notified = true;
    }

    public interface ListenerTest {
        void contentChanged();
    }
}