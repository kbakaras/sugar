package org.butu.sugar.utils;

import org.butu.sugar.lazy.Lazy;
import org.butu.sugar.lazy.MapCache;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Currency;

public class PriceUtils {
    private static Lazy<DecimalFormat> formatterInteger =
            Lazy.of(() -> {
                DecimalFormatSymbols sepSymbol = new DecimalFormatSymbols();
                sepSymbol.setGroupingSeparator(' ');
                DecimalFormat formatter = (DecimalFormat) NumberFormat.getIntegerInstance();
                formatter.setDecimalFormatSymbols(sepSymbol);
                return formatter;
            });
    private static MapCache<String, DecimalFormat> mcFormattersCurrency =
            MapCache.of(currencyCode -> {
                DecimalFormatSymbols sepSymbol = new DecimalFormatSymbols();
                sepSymbol.setGroupingSeparator(' ');
                DecimalFormat formatter = (DecimalFormat) NumberFormat.getCurrencyInstance();
                formatter.setDecimalFormatSymbols(sepSymbol);
                formatter.setCurrency(Currency.getInstance(currencyCode));
                return formatter;
            });
	private static MapCache<Integer, DecimalFormat> formatters2Scales =
            MapCache.of(precision -> {
                char[] zeroArray = new char[precision];
                Arrays.fill(zeroArray, '0');
                DecimalFormat df = new DecimalFormat("#,##0." + new String(zeroArray));
                DecimalFormatSymbols customSymbol = new DecimalFormatSymbols();
                customSymbol.setDecimalSeparator('.');
                customSymbol.setGroupingSeparator(' ');
                ((DecimalFormat)df).setDecimalFormatSymbols(customSymbol);
                df.setGroupingUsed(true);
                return df;
            });

    public static String format(Integer price) {
        if (price == null || price == 0) {
            return "";
        }
        return formatterInteger.get().format(price.longValue());
    }
    
    public static String formatCurrency(Float price, String currencyCode) {
    	if (price == null) {
            return "";
        }
        return mcFormattersCurrency.get(currencyCode).format(price.doubleValue());
    }
    
    public static String formatCurrency(Float price) {
    	return formatCurrency(price, "RUR");
    }
    
    public static String format0(BigDecimal price) {
        if (price == null || compare00(price)) {
            return "";
        }
        return format0(Math.round(price.floatValue()));
    }
    
    public static String format(BigDecimal price, int scale) {
        if (price == null || compareScale(price, scale)) {
            return "";
        }
        return compareScale(price.remainder(BigDecimal.ONE), scale) ? format(price.intValue()) : formatters2Scales.get(scale).format(price); 
    }
    
    public static String format(BigDecimal price) {
    	return format(price, 2);
    }
    
    public static boolean compareScale(BigDecimal v, int scale) {
    	BigDecimal zero = BigDecimal.ZERO.setScale(scale);
    	BigDecimal sv = v.setScale(scale);
    	return zero.equals(sv);
    }
    
    public static boolean compare00(BigDecimal v) {
    	return compareScale(v, 2);
    }
    
    /**
     * Форматирует суммовые числа. Возвращает пустую строку, если число равно нулю или null.
     * @param price Число, которое необходимо отформатировать
     * @return Строку, содержащую отформатированное представление числа.
     */
    public static String format0(Integer price) {
        if (price == null || price == 0) {
            return "";
        }
        Integer value = Math.round(price.floatValue());
        return formatterInteger.get().format(value.longValue());
    }

    public static Integer round99(BigDecimal src) {
        int value = src.intValue();
        if (value < 25) {
            return 49;
        } else {
            float f = ((float) (value - 25)) / (float) 50;
            return Float.valueOf(f).intValue() * 50 + 49;
        }
    }
    
    public static String formatPlainString(BigDecimal value, String currencyCode) {
    	value = value.setScale(2, BigDecimal.ROUND_HALF_UP);
    	return value.toString() + " " + currencyCode;
    }
}