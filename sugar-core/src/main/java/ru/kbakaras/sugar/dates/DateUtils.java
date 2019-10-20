package ru.kbakaras.sugar.dates;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DateUtils {
    public static String[] WEEKDAYS = {null, "Воскресенье", "Понедельник", "Вторник", "Среда", "Четверг", "Пятница", "Суббота"};
    public static String[] WEEKDAYS_SHORT = {null, "ВС", "ПН", "ВТ", "СР", "ЧТ", "ПТ", "СБ"};
    public static String[] MONTHS = {"январь", "февраль", "март", "апрель", "май", "июнь", "июль", "август", "сентябрь", "октябрь", "ноябрь", "декабрь"};
    
    public static SimpleDateFormat FORMAT_XSD = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    public static void clearTime(Calendar calendar) {
        calendar.clear(Calendar.HOUR);
        calendar.clear(Calendar.MINUTE);
        calendar.clear(Calendar.SECOND);
        calendar.clear(Calendar.MILLISECOND);
        calendar.clear(Calendar.AM_PM);
        calendar.clear(Calendar.HOUR_OF_DAY);
    }

    public static Calendar dateOnly(Calendar date) {
        Calendar od = (Calendar)date.clone();
        clearTime(od);
        return od;
    }

    /**
     * @param date Дата, на основе полей которой создаётся новый объект.
     * @return Новый объект на основе полей ГОД, МЕСЯЦ и ДАТА, взятых из указанной даты.
     */
    public static Calendar cleanDate(Calendar date) {
        return new GregorianCalendar(
                date.get(Calendar.YEAR),
                date.get(Calendar.MONTH),
                date.get(Calendar.DATE));
    }

    /**
     * Удобный метод для добавления дней к дате
     * @param date Исходная дата
     * @param daysToAdd Количество дней для добавления. Может быть отрицательным
     * @return Новую дату (создаётся именно новый экземпляр), к которй добавлено указанное
     * количество дней относительно исходной.
     */
    public static Calendar dateAdd(Calendar date, int daysToAdd) {
        Calendar newDate = (Calendar) date.clone();
        newDate.add(Calendar.DATE, daysToAdd);
        return newDate;
    }
    /**
     * Удобный метод для добавления рабочих дней к дате
     * @param date Исходная дата
     * @param daysToAdd Количество дней для добавления. Может быть отрицательным
     * @return Новую дату (создаётся именно новый экземпляр), к которй добавлено указанное
     * количество дней относительно исходной.
     */
    public static Calendar workingDateAdd(Calendar date, int daysToAdd) {
    	Calendar newDate = (Calendar) date.clone();

    	if (daysToAdd == 0) {
    		return newDate;
    	}

    	int weeks = daysToAdd / 5;
    	int add = daysToAdd % 5;

        newDate.add(Calendar.DATE, weeks*7);

        if (daysToAdd > 0) {
            if (add > 0) {
        		do {
        			newDate.add(Calendar.DATE, 1);
        			skipForward(newDate);
        			add--;
        		} while (add > 0);
        	} else {
        		skipForward(newDate);
        	}
        } else {
            if (add < 0) {
        		do {
        			newDate.add(Calendar.DATE, -1);
        			skipBackward(newDate);
        			add++;
        		} while (add < 0);
        	} else {
        		skipBackward(newDate);
        	}
        }


        return newDate;
    }
    private static void skipForward(Calendar date) {
		int dayOfWeek = date.get(Calendar.DAY_OF_WEEK);
		if (dayOfWeek == Calendar.SATURDAY) {
			date.add(Calendar.DATE, 2);
		} else if (dayOfWeek == Calendar.SUNDAY) {
			date.add(Calendar.DATE, 1);
		}
    }
    private static void skipBackward(Calendar date) {
		int dayOfWeek = date.get(Calendar.DAY_OF_WEEK);
		if (dayOfWeek == Calendar.SATURDAY) {
			date.add(Calendar.DATE, -1);
		} else if (dayOfWeek == Calendar.SUNDAY) {
			date.add(Calendar.DATE, -2);
		}
    }

    /**
     * Исходная дата не изменяется в результате выполнения данного метода. Все преобразования
     * происходят с клоном, полученным от исходной даты.
     * @param date Исходная дата
     * @return Дату начала месяца, к которому принадлежит исходная дата
     */
    public static Calendar monthStart(Calendar date) {
        Calendar calendar = (Calendar) date.clone();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        clearTime(calendar);
        return calendar;
    }
    public static Calendar monthEnd(Calendar date) {
        Calendar calendar = (Calendar) date.clone();
        calendar.set(Calendar.DAY_OF_MONTH, date.getActualMaximum(Calendar.DAY_OF_MONTH));
        clearTime(calendar);
        return calendar;
    }

    public static String format(Calendar date) {
        if (date != null) {
            return String.format("%1$td.%1$tm.%1$tY", date);
        } else {
            return "";
        }
    }

    /**
     * @param date Исходная дата
     * @return Представление исходной даты в C-формате
     */
    public static String formatC(Calendar date) {
        if (date != null) {
            return String.format("%1$tY-%1$tm-%1$td", date);
        } else {
            return "";
        }
    }
    public static String format(Date date) {
        if (date != null) {
            return String.format("%1$td.%1$tm.%1$tY", date);
        } else {
            return "";
        }
    }
    public static String formatShort(Calendar date) {
        if (date != null) {
            return String.format("%1$td.%1$tm.%1$ty", date);
        } else {
            return "";
        }
    }
    public static String formatBrief(Calendar date) {
        if (date != null) {
            return String.format("%1$td.%1$tm", date);
        } else {
            return "";
        }
    }
    public static String formatTime(Calendar date) {
        if (date != null) {
            return String.format("%1$tH:%1$tM:%1$tS,%1$tL", date);
        } else {
            return "";
        }
    }

    /**
     * @param date Исходная дата
     * @return Строковое представление исходной даты в формате 'ГГГГММДД'
     */
    public static String formatRaw(Calendar date) {
        if (date != null) {
            return String.format("%1$tY%1$tm%1$td", date);
        } else {
            return "";
        }
    }

    public static String formatDateBriefTime(Calendar date) {
        if (date != null) {
            return String.format("%1$td.%1$tm.%1$tY %1$tH:%1$tM", date);
        } else {
            return "";
        }
    }
    
    public static String formatBriefTime(Calendar date) {
        if (date != null) {
            return String.format("%1$tH:%1$tM", date);
        } else {
            return "";
        }
    }

    public static String formatDateWatchTime(Calendar date) {
        if (date != null) {
            return String.format("%1$td.%1$tm.%1$tY %1$tH:%1$tM:%1$tS", date);
        } else {
            return "";
        }
    }

    public static String formatWatchTime(Calendar date) {
        if (date != null) {
            return String.format("%1$tH:%1$tM:%1$tS", date);
        } else {
            return "";
        }
    }
    
    public static String formatXsd(Calendar date) {
        if (date != null) {
            return FORMAT_XSD.format(date);
        } else {
            return "";
        }
    }

    public static boolean isWeekStart(Calendar date) {
        return date.getFirstDayOfWeek() - date.get(Calendar.DAY_OF_WEEK) == 0;
    }

    /**
     * @return Возвращает понедельник той недели, к которой относится указанная дата.
     * Указанная дата не изменяется (всегда создаётся копия даты).
     */
    public static Calendar weekStart(Calendar date) {
        Calendar calendar = (Calendar) date.clone();
        int val = (calendar.getFirstDayOfWeek() - calendar.get(Calendar.DAY_OF_WEEK) - 7) % 7;
        if (val != 0) calendar.add(Calendar.DAY_OF_MONTH, val);
        clearTime(calendar);

        return calendar;
    }
    /**
     * @return Возвращает воскресенье той недели, к которой относится указанная дата.
     * Указанная дата не изменяется (всегда создаётся копия даты).
     */
    public static Calendar weekEnd(Calendar date) {
        Calendar calendar = (Calendar)date.clone();
        int val = (calendar.getFirstDayOfWeek() - calendar.get(Calendar.DAY_OF_WEEK) + 6) % 7;
        if (val != 0) calendar.add(Calendar.DAY_OF_MONTH, val);

        return calendar;
    }

    /**
     * @return Дату следующего (от текущей даты) понедельника
     */
    public static Calendar nextMonday() {
        return nextDayOfWeek(Calendar.MONDAY, Calendar.getInstance());
    }
    
    /**
     * @return Дату вчера
     */
    public static Calendar yesterday() {
    	Calendar date = Calendar.getInstance();
    	date.add(Calendar.DATE, -1);
    	return dateOnly(date);
    }
    
    /**
     * @return Дату следующего (от передаваемой даты) дня недели
     */
    public static Calendar nextDayOfWeek(int dayOfWeek, Calendar calendar) {
        calendar = (Calendar)calendar.clone();
        calendar.add(Calendar.DAY_OF_MONTH, 7);

        int val = (dayOfWeek - calendar.get(Calendar.DAY_OF_WEEK) - 7) % 7;
        if (val != 0) calendar.add(Calendar.DAY_OF_MONTH, val);

        return calendar;
    }
    
    public static Calendar convert(Date date) {
        if (date != null) {
            Calendar calendar = new GregorianCalendar();
            calendar.setTime(date);
            return calendar;
        } else {
            return null;
        }
    }
    
    /**
     * Преобразование даты к строковой форме. Используется, например, для сохранения параметров типа
     * дата в текстовых файлах настроек.
     * @param date Исходная дата
     * @return Строка с датой в формате ISO. Например, 2000-02-01 - первое Февраля 2000-го года
     */
    public static String toString(Calendar date) {
        if (date != null) {
            return String.format("%tF", date);
        } else {
            return "";
        }
    }
    /**
     * Преобразование даты, сохранённой в форме текста методом <i>toString</i>, к типу данных
     * <b>Calendar</b>.
     * @param str Исходная строка
     * @return Дата, полученная в результате преобразования, или <b>null</b>, если аргумент равен
     * <b>null</b> или пуст.
     */
    public static Calendar fromString(String str) {
        if (str != null && !str.isEmpty()) {
            Pattern pattern = Pattern.compile("(\\d\\d\\d\\d)-(\\d\\d)-(\\d\\d)");
            Matcher matcher = pattern.matcher(str);

            if (matcher.matches()) {
                return new GregorianCalendar(
                        Integer.parseInt(matcher.group(1)),
                        Integer.parseInt(matcher.group(2)) - 1,
                        Integer.parseInt(matcher.group(3)));
            } else {
                throw new IllegalArgumentException(str);
            }
        } else {
            return null;
        }
    }
    /**
     * Преобразование даты, из текстового представления в формате, как принято в России, к типу
     * данных <b>Calendar</b>.
     * @param str Исходная строка
     * @return Дата, полученная в результате преобразования, или <b>null</b>, если аргумент равен
     * <b>null</b> или пуст.
     */
    public static Calendar fromStringRu(String str) {
        if (str != null && !str.isEmpty()) {
            Pattern pattern = Pattern.compile("(\\d\\d).(\\d\\d).(\\d\\d\\d\\d)");
            Matcher matcher = pattern.matcher(str);

            if (matcher.matches()) {
                return new GregorianCalendar(
                        Integer.parseInt(matcher.group(3)),
                        Integer.parseInt(matcher.group(2)) - 1,
                        Integer.parseInt(matcher.group(1)));
            } else {
                throw new IllegalArgumentException(str);
            }
        } else {
            return null;
        }
    }
    /**
     * Преобразование даты, из текстового представления в формате, как принято в России, к типу
     * данных <b>Calendar</b>.
     * @param str Исходная строка
     * @return Дата, полученная в результате преобразования, или <b>null</b>, если аргумент равен
     * <b>null</b> или пуст.
     */
    public static Calendar fromStringRuShort(String str) {
        if (str != null && !str.isEmpty()) {
            Pattern pattern = Pattern.compile("(\\d\\d).(\\d\\d).(\\d\\d)");
            Matcher matcher = pattern.matcher(str);

            if (matcher.matches()) {
                return new GregorianCalendar(
                        Integer.parseInt(matcher.group(3)) + 2000,
                        Integer.parseInt(matcher.group(2)) - 1,
                        Integer.parseInt(matcher.group(1)));
            } else {
                throw new IllegalArgumentException(str);
            }
        } else {
            return null;
        }
    }
    
    public static Calendar fromXsdString(String str) {
    	if (str != null && !str.isEmpty()) {
    		try {
	    		Date d = FORMAT_XSD.parse(str);
	    		Calendar c = Calendar.getInstance();
	    		c.setTime(d);
	    		return c;
    		} catch (ParseException ex) {
    			throw new RuntimeException(ex);
    		}
    	} else {
    		return null;
    	}
    }

    /**
     * @param begin Начало интервала
     * @param end Конец интервала
     * @return Строковое представление разницы между двумя интервалами времени в миллисекундах
     */
    public static String msLength(Calendar begin, Calendar end) {
        return String.format("%d ms", end.getTimeInMillis() - begin.getTimeInMillis());
    }

    /**
     * 
     * @param begin Начало интервала
     * @param end Конец интервала
     * @return Количество прошедших недель между двумя датами
     */
    public static int countWeeks(Calendar begin, Calendar end){
    	Calendar wbegin = weekStart(begin);
    	Calendar wend = weekStart(end);
    	int count = 0;
        while (wbegin.compareTo(wend) < 0) {
            wbegin.add(Calendar.DAY_OF_YEAR, 7);
            ++count;
        }
    	return count;
    }

    /**
     * 
     * @param begin Начало интервала
     * @param end Конец интервала
     * @return Количество дней между двумя датами (включительно)
     */
    public static int countDays(Calendar begin, Calendar end){
        Calendar wbegin = (Calendar) begin.clone();
        int count = 0;
        while (wbegin.compareTo(end) <= 0) {
            wbegin.add(Calendar.DATE, 1);
            ++count;
        }
        return count;
    }
    
    /**
     * 
     * @param date Дата
     * @return true - выходной, false - рабочий день
     */
    public static boolean isWeekend(Calendar date) {
    	int dayOfWeek = date.get(Calendar.DAY_OF_WEEK);
		return dayOfWeek == Calendar.SATURDAY ||
				dayOfWeek == Calendar.SUNDAY;
    }
    
    public static Calendar getLocalized(Calendar date) {
    	if (!date.getTimeZone().equals(TimeZone.getDefault())) {
	    	Calendar date1 = Calendar.getInstance();
	    	date1.set(Calendar.HOUR, date.get(Calendar.HOUR));
	    	date1.set(Calendar.MINUTE, date.get(Calendar.MINUTE));
	    	date1.set(Calendar.SECOND, date.get(Calendar.SECOND));
	    	date1.set(Calendar.MILLISECOND, date.get(Calendar.MILLISECOND));
	    	date1.set(Calendar.AM_PM, date.get(Calendar.AM_PM));
	    	date1.set(Calendar.HOUR_OF_DAY, date.get(Calendar.HOUR_OF_DAY));
	    	date1.set(Calendar.DATE, date.get(Calendar.DATE));
	    	date1.set(Calendar.MONTH, date.get(Calendar.MONTH));
	    	date1.set(Calendar.YEAR, date.get(Calendar.YEAR));
	    	return date1;
    	} else {
    		return date;
    	}
    }
}