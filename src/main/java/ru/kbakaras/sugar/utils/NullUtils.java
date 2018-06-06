package ru.kbakaras.sugar.utils;

import java.math.BigDecimal;
import java.util.function.Function;

public class NullUtils {
    public static <T> T isNull(T obj, T elseObj) {
        if (obj != null) {
            return obj;
        } else {
            return elseObj;
        }
    }

    public static int toInt(Integer value) {
        return value != null ? value.intValue() :  0;
    }

    public static int toInt(BigDecimal value) {
        return value != null ? Math.round(value.floatValue()) : 0;
    }

    public static int toInt(Float value) {
    	if (value != null) {
    		return Math.round(value.floatValue());
        } else {
            return 0;
        }
    }

    public static int toInt(Object value) {
    	if (value instanceof Integer) {
    		return toInt((Integer)value);
    	} else if (value instanceof Float) {
    		return toInt((Float)value);
    	} else if (value instanceof BigDecimal) {
    		return toInt((BigDecimal)value);
    	} else if (value == null) {
    		return 0;
    	}
    	throw new IllegalArgumentException(String.valueOf(value));
    }
    
    public static <T, V> V returnIfNotNull(T arg, java.util.function.Function<T, V> func) {
    	return arg != null ? func.apply(arg) : null;
    }
    
    public static boolean isNotNullOr(Object... args) {
    	for (Object arg : args) {
    		if (arg != null) return true;
    	}
    	return false;
    }
    
    public static boolean isNotNullAnd(Object... args) {
    	for (Object arg : args) {
    		if (arg == null) return false;
    	}
    	return true;
    }

    public static <T, V> V mapNullable(T object, Function<T, V> function) {
        return object != null ? function.apply(object) : null;
    }
}