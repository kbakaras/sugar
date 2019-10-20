package ru.kbakaras.sugar.fispa;

import java.util.Collection;
import java.util.function.Function;
import java.util.function.ToLongFunction;

public class FispaRequestPage<F, S> extends FispaRequest<F, S> {

    public final Long page;


    public FispaRequestPage(F filter, S sorting, Long limit, Long page) {
        super(filter, sorting, limit);
        this.page = page;
    }


    @Override
    protected <E extends Collection> FispaResponse<E> adapt(ToLongFunction<F> totalCountFunction, Function<PageRequest<F, S>, E> pageFunction) {

        long totalCount = totalCountFunction.applyAsLong(filter);
        long pageCount  = pageCount(totalCount);
        Long page       = pageIndex(pageCount);
        long offset     = pageOffset(page);

        return new FispaResponse<>(
                pageFunction.apply(new FispaPageRequest(offset)),
                totalCount, offset, page, pageCount
        );

    }

    private long pageCount(long totalCount) {

        if (totalCount > 0) {
            if (this.limit > 0) {
                return (totalCount - 1) / this.limit + 1;
            } else if (this.limit == 0) {
                return 1;
            } else {
                throw new IllegalArgumentException("Negative limit for pagination");
            }

        } else {
            return 0;
        }

    }

    private Long pageIndex(long pageCount) {
        if (pageCount > 0) {
            return Math.min(pageCount - 1, this.page);
        } else {
            return null;
        }
    }

    private long pageOffset(Long pageIndex) {
        return pageIndex != null ? pageIndex * this.limit : 0;
    }


    @Override
    protected <E extends Collection> boolean inconsistent(FispaResponse<E> response) {

        if (response.totalCount == 0) {

            // Если общее количество элементов = 0, то страница должна быть пустой
            return !response.list.isEmpty();

        } else if (response.page == response.pageCount - 1) {

            // Если мы на последней странице, количество элементов списка должно соответствовать
            // общему количеству элементов
            return response.offset + response.list.size() != response.totalCount;

        } else {

            // Если мы не на последней странице, количество элементов списка должно быть равно
            // запрошенному размеру страницы (проверяется на меньше, так как больше оно быть не может)
            return response.list.size() < limit;

        }

    }

}