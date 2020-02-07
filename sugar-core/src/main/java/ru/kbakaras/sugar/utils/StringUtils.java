package ru.kbakaras.sugar.utils;

import java.util.List;

@SuppressWarnings("unused")
public class StringUtils {

    /**
     * Выполняет объединение строк из переданного массива в одну строку. При этом строки разделяются
     * между собой указанным разделителем. Элементы массива строк, равные null игнорируются. Разделитель
     * используется только между непустыми строками. Между двумя последовательными строками разделитель
     * используется максимально один раз.<br/><br/>
     * Метод предназначен для получения человеко-читаемых строк на основе массива, некоторые элементы
     * которого могут быть пустыми или отсутствовать. Например ФИО из отдельных частей имени.
     *
     * @param separator Строка, используемая в качестве разделителя. Значение null эквивалентно пустой строке.
     * @param strings   Массив строк, которые нужно объединить через разделитель
     */
    public static String join(String separator, String... strings) {

        if (separator == null) separator = "";

        StringBuilder result = new StringBuilder();

        for (String string : strings) {

            if (string != null && !string.isEmpty()) {
                if (result.length() > 0) {
                    result.append(separator);
                }
                result.append(string);
            }

        }

        return result.toString();

    }

    public static String join(String separator, List<String> stringList) {
        return join(separator, stringList.toArray(new String[0]));
    }

}
