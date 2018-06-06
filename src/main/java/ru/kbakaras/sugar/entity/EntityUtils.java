package ru.kbakaras.sugar.entity;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public  class EntityUtils {
    public static <T extends IEntity> String toCharSequence(T[] entities) {
        if (entities == null || entities.length == 0) {
            return null;
        }
        StringBuffer buf = new StringBuffer(",");
        for (T e: entities) {
            buf.append(e.getId());
            buf.append(',');
        }
        return buf.toString();
    }

    public static <T extends IEntity> String toCharSequence(Collection<T> entities) {
        if (entities == null || entities.size() == 0) {
            return null;
        }
        StringBuffer buf = new StringBuffer(",");
        for (T e: entities) {
            buf.append(e.getId());
            buf.append(',');
        }
        return buf.toString();
    }

    public static int compoundHashCode(Object...values) {
        int hash = 17;
        for (Object value: values) {
            hash = hash*37 + (value != null ? value.hashCode() : 0);
        }
        return hash;
    }
    
    public static <T extends IHierarchicalEntity<T>> String getPathString(IHierarchicalEntity<T> e, Class<T> clazz, String property) {
		StringBuffer sb = new StringBuffer();
    	T[] path = getPath(e, clazz);
		for (int i = 0; i < path.length; ++i) {
			Object value;
			try {
				Field f = clazz.getDeclaredField(property);
				f.setAccessible(true);
				value = f.get(path[i]);
			} catch (Exception e1) {
				throw new RuntimeException(e1);
			}
			sb.append(String.valueOf(value));
			if (i != path.length-1) {
				sb.append(" -> ");
			}
		}
		return sb.toString();
	}
    
    @SuppressWarnings("unchecked")
	public static <T extends IHierarchicalEntity<T>> T[] getPath(IHierarchicalEntity<T> e, Class<T> clazz) {
		List<IHierarchicalEntity<T>> lists = new ArrayList<IHierarchicalEntity<T>>();
		lists.add(e);
		T parent = e.getParent();
		while (parent != null) {
			lists.add(parent);
			parent = parent.getParent();
		}
		List<IHierarchicalEntity<T>> result = new ArrayList<IHierarchicalEntity<T>>();
		for (int i = lists.size()-1; i >= 0; --i) {
			result.add(lists.get(i));
		}
		return result.toArray((T[])Array.newInstance(clazz, result.size()));
	}
    
    public static <T extends IEntity> Integer[] toIds(Collection<T> entities) {
    	if (entities == null) return null;
        List<Integer> ids = new ArrayList<Integer>(entities.size());
        for (T e: entities) {
            ids.add((Integer)e.getId());
        }
        return ids.toArray(new Integer[ids.size()]);
    }
    
    public static <T extends IEntity> Integer[] toIds(T[] entities) {
    	return toIds(Arrays.asList(entities));
    }
    
    public static <T> LinkedHashMap<T, String> createKeyDescrMap(Collection<T> keys, Function<T, String> desc) {
    	LinkedHashMap<T, String> map = new LinkedHashMap<T, String>();
    	for (T key : keys) {
    		map.put(key, desc.apply(key));
    	}
    	return map;
    }
    
    @SuppressWarnings("unchecked")
	public static <T> Map<T, String> createMapFromAnnotation(Class<?> entityClass, Class<T> keyClass, String patternStr) {
    	Map<T, String> map = new HashMap<T, String>();
    	Pattern pattern = Pattern.compile(patternStr);
        Field[] fields = entityClass.getDeclaredFields();
        for (Field field: fields) {
            if ((field.getModifiers() & Modifier.STATIC) > 0 && field.getType().equals(keyClass)) {
                Matcher matcher = pattern.matcher(field.getName());
                if (matcher.matches()) {
                    try {
                        T value = (T)field.get(entityClass);
                        Description description = field.getAnnotation(Description.class);
                        String descriptionStr = description == null ? null : description.value();
                        map.put(value, descriptionStr);
                    } catch (IllegalArgumentException | IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    	return map;
    }
}