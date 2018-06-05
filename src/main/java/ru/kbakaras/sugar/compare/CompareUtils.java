package org.butu.sugar.compare;

import org.butu.sugar.utils.ArrayUtils;

import java.util.Calendar;
import java.util.Comparator;

public class CompareUtils {
	
	public static int compare(int n1, int n2) {
		if (n1 > n2) {
			return 1;
		} else if (n1 < n2) {
			return -1;
		} else {
			return 0;
		}
	}
	
    public static boolean equals(Object o1, Object o2) {
        return o1 != null && o1.equals(o2);
    }

    /**
     * @return Результат сравнения двух календарей. Процедура отличается от стандартной equals в
     * классе Calendar. В этой процедуре календари сравниваются на тождественность абсолютного
     * момента и одинаковость смещения зоны.
     */
    public static boolean equals(Calendar o1, Calendar o2) {
        if (o1 != null) {
            if (o1 == o2) return true;
            return
                    o1.compareTo(o2) == 0 &&
                    o1.getTimeZone().getOffset(o1.getTimeInMillis()) == o2.getTimeZone().getOffset(o2.getTimeInMillis());
        } else {
            return false;
        }
    }

    public static boolean nullsOrEquals(Object o1, Object o2) {
        return (o1 == null && o2 == null) || (o1 != null && o1.equals(o2));
    }

    public static boolean nullsOrEquivalent(Equivalence o1, Object o2) {
        return (o1 == null && o2 == null) || (o1 != null && o1.equivalent(o2));
    }
    
    public static <E> boolean nullsOrEquals(E[] a1, E[] a2) {
    	if (a1 == null && a2 == null) {
    		return true;
    	} else if ((a1 == null && a2 != null) || (a2 == null && a1 != null)){
    		return false;
    	} else {
	    	if (a1.length == a2.length) {
	    		for (int i = 0; i < a1.length; ++i) {
	    			if (!a1[i].equals(a2[i])) {
	    				return false;
	    			}
	    		}
	    		return true;
	    	}
	    	return false;
    	}
    }

    public static <E extends Comparable<E>> E max (E value1, E value2) {
        if (value1 != null) {
            if (value2 != null) {
                if (value1.compareTo(value2) > 0) return value1; else return value2;
            } else return value1;
        } else return value2;
    }

    public static <E extends Comparable<E>> E min (E value1, E value2) {
        if (value1 != null) {
            if (value2 != null) {
                if (value1.compareTo(value2) < 0) return value1; else return value2;
            } else return value1;
        } else return value2;
    }
    
    public static class ArrayComparator<T> implements Comparator<T> {
    	private T[] rule;
    	public ArrayComparator(T[] rule) {
    		this.rule = rule;
    	}
		public int compare(T o1, T o2) {
			int i1 = ArrayUtils.findIndex(rule, o1);
			int i2 = ArrayUtils.findIndex(rule, o2);
			if (i1 == -1 && i2 != -1) {
				return 1;
			}
			if (i2 == -1 && i1 != -1) {
				return -1;
			}
			return new Integer(i1).compareTo(i2);
		}
    }
}