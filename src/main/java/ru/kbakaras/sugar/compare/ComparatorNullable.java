package org.butu.sugar.compare;

import java.io.Serializable;
import java.util.Comparator;

public class ComparatorNullable implements Comparator, Serializable {
    transient private static ComparatorNullable instance;

    @SuppressWarnings("unchecked")
    public int compare(Object o1, Object o2) {
        if (o1 == null && o2 == null) {
            return 0;
        }
        if (o1 == null) {
            return -1;
        }
        if (o2 == null) {
            return 1;
        }
        if (o1 == o2 || o1.equals(o2)) {
            return 0;
        }

        if (o1 instanceof Comparable && o2 instanceof Comparable) {
            try {
                return ((Comparable) o1).compareTo(o2);
            } catch (ClassCastException e) {
                throw new RuntimeException(e);
            }
        }

        if (!o1.getClass().equals(o2.getClass())) {
            return o1.getClass().getName().compareTo(o2.getClass().getName());
        }

        int h1 = o1.hashCode();
        int h2 = o2.hashCode();
        return h1 > h2 ? 1 : -1;
    }

    public static ComparatorNullable getInstance() {
        if (instance == null) {
            instance = new ComparatorNullable();
        }
        return instance;
    }
}