package org.butu.sugar.dates;

import org.butu.sugar.compare.CompareUtils;
import org.butu.sugar.entity.EntityUtils;

import java.io.Serializable;
import java.time.LocalDate;

public class Interval implements Serializable {
    public final LocalDate begin;
    public final LocalDate end;

    public Interval(LocalDate begin, LocalDate end) {
        this.begin = begin;
        this.end = end;
    }

    @Override
    public int hashCode() {
        return EntityUtils.compoundHashCode(begin, end);
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj) || getClass().equals(obj.getClass())
                && CompareUtils.nullsOrEquals(begin, ((Interval) obj).begin)
                && CompareUtils.nullsOrEquals(end,   ((Interval) obj).end);
    }

    public static Interval EMPTY = new Interval(null, null);
}