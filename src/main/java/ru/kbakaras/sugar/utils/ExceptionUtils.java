package org.butu.sugar.utils;

public class ExceptionUtils {
    public static String getMessage(Throwable e) {
        String msg = null;
        Throwable t = e;
        while (t != null) {
            msg = t.getMessage();
            if (msg != null) break;
            t = t.getCause();
        }

        if (msg == null) msg = e.toString();
        return msg;
    }

    /**
     * @param e Исключение, которое надо проанализировать
     * @param clazz Класс исключения для сопоставления
     * @return true, если указанное исключение вызвано исключением, которое является
     * инстансом указанного класса.
     */
    public static boolean causedBy(Throwable e, Class<? extends Throwable> clazz) {
        Throwable t = e;
        while (t != null) {
            if (clazz.isInstance(t)) return true;
            t = t.getCause();
        }
        return false;
    }
}