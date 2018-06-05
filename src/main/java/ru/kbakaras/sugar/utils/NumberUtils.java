package org.butu.sugar.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class NumberUtils {
    public static int numberToInt(Number value) {
        if (value != null) {
            return value.intValue();
        } else {
            return 0;
        }
    }
    public static int numberToInt(Object value) {
        return numberToInt((Number)value);
    }

    public static Integer numberToInteger(Number value) {
        if (value != null) {
            return value.intValue();
        } else {
            return null;
        }
    }
    public static Integer numberToInteger(Object value) {
        return numberToInteger((Number) value);
    }

    public static int booleanToInt(boolean value) {
        return value ? 1 : 0;
    }

    public static float round(float source, int scale){
    	int k = (int)Math.pow((double)10, (double)scale);
    	source *= k;
    	source = (float)Math.round(source)/k;
    	return source;
    }
    
    public static double round(double source, int scale){
    	int k = (int)Math.pow((double)10, (double)scale);
    	source *= k;
    	source = (double)Math.round(source)/k;
    	return source;
    }
    
    /**
     * @param ratios Массив коэффициентов
     * @param sum Сумму, которую нужно распределить
     * @return Массив каждый i-ый элемент которого, есть составная часть числа <b>sum</b> в соответствии 
     * с i-ым коэффициентом из массива ratios.<br>
     * <b>Примечание:</b><br>Последний ненулевой элемент в возвращаемом массиве есть результат выражения
     * <b>sum</b> - (сумма всех остальных элементов) 
     * @throws ArithmeticException
     */
    public static int[] distribute(float[] ratios, int sum){
    	int[] result = new int[ratios.length];
    	float sumratio = 0f;
    	int indexEnd = 0;
    	for(int i = ratios.length-1; i>-1; --i){
    		sumratio += ratios[i];
    		if(ratios[i] != 0f && indexEnd == 0) {
    			indexEnd = i;
    		}
    	}
    	int index = 0;
    	int pgsum = 0;
		for (float ratio : ratios) {
			if(index == indexEnd) {
				result[index] = sum - pgsum;
				break;
			}
			result[index] = Math.round((ratio/sumratio) * sum);
			pgsum += result[index];
			index++;
		}
		return result;
    }
    
    /**
     * @param distribution Распределение в соответствии с искомым набором коэффициентов.
     * @param sumRatio Сумма искомого набора коэффициентов
     * @param scale количество знаков после запятой искомого коэффициента
     * @return возвращает набор коэффициентов в соответствии  с передаваемым распределением
     * <b>Примечание:</b><br>Последний ненулевой элемент в возвращаемом массиве есть результат выражения
     * <b>sumRatio</b> - (сумма всех остальных элементов) 
     * @throws ArithmeticException
     */
    public static float[] undistribute(int[] distribution, float sumRatio, int scale){
    	int sum = 0;
    	int index = 0;
    	int indexEnd = 0;
    	for(int i = distribution.length-1; i>-1; --i){
    		sum += distribution[i];
    		if(distribution[i] != 0 && indexEnd == 0) {
    			indexEnd = i;
    		}
    	}
    	float[] result = new float[distribution.length];
    	float pgsum = 0f;
    	for(int value : distribution){
    		if(index == indexEnd) {
				result[index] = round(sumRatio - pgsum, scale);
				break;
			}
    		result[index] = round(((float)value * (float)sumRatio) / (float)sum, scale);
    		pgsum += result[index];
    		index++;
    	}
    	return result;
    }

    public static boolean nullOrZero(BigDecimal number) {
    	return number == null || (BigDecimal.ZERO.compareTo(number) == 0);
    }
    
    public static int calcPlanExecutionPercent(BigDecimal plan, BigDecimal fact) {
		int p;
    	if (fact == null) fact = BigDecimal.ZERO;
    	if (!NumberUtils.nullOrZero(plan)) {
    		p = fact.divide(plan, 2, RoundingMode.CEILING).multiply(new BigDecimal(100)).intValue();
    	} else {
    		p = 100;
    	}
    	return p;
	}
    
    public static BigDecimal scale2(BigDecimal value) {
    	return scale(value, 2);
    }
    
    public static BigDecimal scale3(BigDecimal value) {
    	return scale(value, 3);
    }
    
    public static BigDecimal scale(BigDecimal value, int scale) {
    	if (value != null) {
    		return value.setScale(scale, RoundingMode.HALF_UP);
    	}
    	return null;
    }
}