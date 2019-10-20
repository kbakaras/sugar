package ru.kbakaras.sugar.fispa;

import java.util.Collection;
import java.util.function.Function;
import java.util.function.ToLongFunction;

public class FispaRequestOffset<F, S> extends FispaRequest<F, S> {

    public final Long offset;


    public FispaRequestOffset(F filter, S sorting, Long limit, Long offset) {
        super(filter, sorting, limit);
        this.offset = offset;
    }


    @Override
    protected <E extends Collection> FispaResponse<E> adapt(ToLongFunction<F> totalCountFunction, Function<PageRequest<F, S>, E> pageFunction) {
        return new FispaResponse<>(
                pageFunction.apply(new FispaPageRequest(offset)),
                totalCountFunction.applyAsLong(filter),
                offset, null, null
        );
    }

    @Override
    protected <E extends Collection> boolean inconsistent(FispaResponse<E> response) {

        if (response.list.size() < limit) {

            // Размер полученного списка имеет право быть меньше запрошенного лимита только
            // если он равен общему числу элементов
            return response.list.size() != Math.max(0, response.totalCount - response.offset);

        } else {

            // Если размер полученного списка равен запрошенному лимиту, то в сумме со
            // сдвигом не должен превосходить общее количество элементов
            return response.list.size() + response.offset > response.totalCount;

        }

    }

}