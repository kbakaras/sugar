package ru.kbakaras.sugar.fispa;

import java.util.Collection;
import java.util.function.Function;
import java.util.function.ToLongFunction;

public class FispaRequestShift<F, S> extends FispaRequest<F, S> {

    public final Long shift;


    public FispaRequestShift(F filter, S sorting, Long limit, Long shift) {
        super(filter, sorting, limit);
        this.shift = shift;
    }


    @Override
    protected <E extends Collection> FispaResponse<E> adapt(ToLongFunction<F> totalCountFunction, Function<PageRequest<F, S>, E> pageFunction) {

        long totalCount = totalCountFunction.applyAsLong(filter);
        long offset = shiftOffset(totalCount);

        return new FispaResponse<>(
                pageFunction.apply(new FispaPageRequest(offset)),
                totalCount, offset, null, null
        );

    }

    private long shiftOffset(long totalCount) {
        return Math.min(
                Math.max(0, totalCount - this.limit),
                this.shift
        );
    }


    @Override
    protected <E extends Collection> boolean inconsistent(FispaResponse<E> response) {

        if (response.list.size() < limit) {

            // Размер полученного списка имеет право быть меньше запрошенного лимита только
            // если он равен общему числу элементов
            return response.list.size() != response.totalCount;

        } else {

            // Если размер полученного списка равен запрошенному лимиту, то в сумме со
            // сдвигом не должен превосходить общее количество элементов
            return response.list.size() + response.offset > response.totalCount;

        }

    }

}