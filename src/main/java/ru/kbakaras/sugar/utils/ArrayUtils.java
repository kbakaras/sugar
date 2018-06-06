package ru.kbakaras.sugar.utils;

import ru.kbakaras.sugar.lazy.Lazy;

import java.lang.reflect.Array;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.IntStream;

public class ArrayUtils {
    /**
     * Находит индекс элемента в массиве методом прямого перебора. Применяется в тех
     * случаях, когда массив не должен быть отсортирован.
     * @param items Массив
     * @param item Искомый элемент
     * @return индекс элемента в массиве, или -1, если искомый элемент не найден
     */
    public static <T> int findIndex(T[] items, T item) {
        return IntStream.range(0, items.length)
                .filter(i -> items[i] == item)
                .findFirst().orElse(-1);
    }

    public static String toHexString(byte[] array) {
        if (array == null) return null;
        return hexString(array).toString(); 
    }
    public static String toHexStringWithDashes(byte[] array) {
        if (array == null) return null;
        StringBuilder hex = hexString(array);
        return
                (hex.substring(24, 32) + "-" +
                 hex.substring(20, 24) + "-" +
                 hex.substring(16, 20) + "-" +
                 hex.substring(0, 4) + "-" +
                 hex.substring(4, 16));
    }
    private static StringBuilder hexString(byte[] array) {
        final StringBuilder hex = new StringBuilder(2 * array.length);
        for (final byte b: array) {
            hex.append(HEXES.charAt((b & 0xF0) >> 4)).append(HEXES.charAt((b & 0x0F)));
        }
        return hex;
    }

    public static byte[] fromHexString(String hex) {
        byte[] array = new byte[hex.length() / 2];

        for (int i = 0; i < array.length; i++) {
            String c1 = hex.substring(i*2, i*2 + 1);
            String c2 = hex.substring(i*2 + 1, i*2 + 2);
            array[i] = (byte) (HEXES.indexOf(c1) * 16 + HEXES.indexOf(c2));
        }

        return array;
    }
    public static byte[] fromHexStringWithDashes(String uid) {
        String[] parts = uid.split("-");
        return ArrayUtils.fromHexString((parts[3] + parts[4] + parts[2] + parts[1] + parts[0]).toUpperCase());
    }

    private static Lazy<SecureRandom> lRandom = Lazy.of(SecureRandom::new);
    public static byte[] randomUUIDbytes() {
        byte[] randomBytes = new byte[16];
        lRandom.get().nextBytes(randomBytes);
        randomBytes[6]  &= 0x0f;  /* clear version        */
        randomBytes[6]  |= 0x40;  /* set to version 4     */
        randomBytes[8]  &= 0x3f;  /* clear variant        */
        randomBytes[8]  |= 0x80;  /* set to IETF variant  */
        return randomBytes;
    }

    @SuppressWarnings("unchecked")
	public static <T> T[] cast(Object[] array, Class<T> clazz) {
        List<T> newList = new ArrayList<T>();
        for (Object e: array) {
            newList.add(clazz.cast(e));
        }
        return newList.toArray((T[]) Array.newInstance(clazz, newList.size()));
    }

    public static <E, T extends E> List<T> cast(Collection<E> list, Class<E> classE, Class<T> classT) {    	
        List<T> newList = new ArrayList<T>();
        for (Object e: list) {
            newList.add(classT.cast(e));
        }
        return newList;
    }

    @SuppressWarnings("unchecked")
	public static <T> List<T[]> split(Collection<T> list, Class<T> clazz, int portion) {
        List<T[]> result = new ArrayList<T[]>();
        T[] value = list.toArray((T[]) Array.newInstance(clazz, list.size()));
        if (value.length > portion) {
            int index = 0;
            while (index < value.length) {
                int end = index + portion;
                if (end > value.length) {
                    end = value.length;
                }
                result.add(Arrays.copyOfRange(value, index, end));
                index += portion;
            }
        } else if (value.length != 0) {
            result.add(value);
        }
        return result;
    }
	
	public static boolean isNullOrEmpty(Object[] array) {
		return array == null || array.length == 0;
	}
	
	/**
	 * Возвращает массив с одним элементом <i>null</i>, если передаваемый аргумент-массив пустой 
	 */
	@SuppressWarnings("unchecked")
	public static <T> T[] getOneElementsIfEmpty(T[] array, Class<T> tClass) {
		return array == null || array.length == 0 ? (T[])Array.newInstance(tClass, 1) : array;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T findElementByClass(Object[] array, Class<T> tClass) {
		for (Object e : array) {
			if (e.getClass().equals(tClass)) {
				return (T)e;
			}
		}
		return null;
	}

    private static final String HEXES = "0123456789ABCDEF";
}