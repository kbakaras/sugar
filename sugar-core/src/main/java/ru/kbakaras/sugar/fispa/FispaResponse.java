package ru.kbakaras.sugar.fispa;

import java.util.Collection;

public class FispaResponse<E extends Collection> {

    public final E list;

    public final long totalCount;
    public final long offset;

    public final Long page;
    public final Long pageCount;

    public FispaResponse(E list, long totalCount, long offset, Long page, Long pageCount) {

        this.list = list;

        this.totalCount = totalCount;
        this.offset     = offset;
        this.page       = page;
        this.pageCount  = pageCount;

    }

}