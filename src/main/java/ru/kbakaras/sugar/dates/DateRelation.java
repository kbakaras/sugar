package org.butu.sugar.dates;

import java.time.LocalDate;

/**
 * Создано: kbakaras, в день: 08.02.2018.
 */
public class DateRelation {
    private LocalDate date;

    private DateRelation(LocalDate date) {
        this.date = date;
    }

    public int relatively(LocalDate date) {
        int compare = date.compareTo(this.date);
        if (compare < 0) {
            return FUTURE;
        } else if (compare > 0) {
            return PAST;
        } else {
            return NOW;
        }
    }

    public int relativelyToday() {
        return relatively(LocalDate.now());
    }

    public static DateRelation of(LocalDate date) {
        return new DateRelation(date);
    }

    public static final int PAST   = 0;
    public static final int NOW    = 1;
    public static final int FUTURE = 2;
}