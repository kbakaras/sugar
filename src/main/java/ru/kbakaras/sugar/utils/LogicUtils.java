package ru.kbakaras.sugar.utils;

public class LogicUtils {
    /**
     * @return <b>true</b>, если все биты, включённые в v2 включены и в v1, иначе <b>false</b>.
     */
    public static boolean ande(int v1, int v2) {
        return (v1 & v2) == v2;
    }

    /**
     * Копирует состояние указанного бита из источника в приёмник.
     * @param destination приёмник.
     * @param source источник.
     * @param bit копируемый бит.
     */
    public static int copyBit(int destination, int source, int bit) {
        if (ande(source, bit)) {
            destination |= bit;
        } else {
            destination &= ~bit;
        }

        return destination;
    }

    /**
     * Приводит в соответствие состояние указанного бита в приёмнике, с состоянием
     * другого указанного бита в источнике.
     * @param destination приёмник.
     * @param source источник.
     * @param sbit бит источника
     * @param dbit бит приёмника
     */
    public static int copyBit(int destination, int dbit, int source, int sbit) {
        if (ande(source, sbit)) {
            destination |= dbit;
        } else {
            destination &= ~dbit;
        }

        return destination;
    }
}