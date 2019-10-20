package ru.kbakaras.jpa;

import org.junit.jupiter.api.Test;
import ru.kbakaras.jpa.model.TestProperEntity;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class BaseEntityBuildTest {

    @Test
    public void newElementTest() {
        TestProperEntity element = BaseEntity.newElement(TestProperEntity.class);

        assertNotNull(element.getId());
    }

    @Test
    public void newReflectionTest() {
        Class<?> clazz = TestProperEntity.class;

        try {
            Method method = clazz.getMethod("newElement", Class.class);
            TestProperEntity element = (TestProperEntity) method.invoke(null, clazz);

            assertNotNull(element.getId());

        } catch (NoSuchMethodException    |
                IllegalAccessException    |
                InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}