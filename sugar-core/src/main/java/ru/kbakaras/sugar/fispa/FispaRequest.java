package ru.kbakaras.sugar.fispa;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.ToLongFunction;
import java.util.stream.Collector;

/**
 * Базовый класс для объектов Fispa - объектов, позволяющих формировать запросы
 * с поддержкой фильтрации, сортировки и пагинации. Классы-наследники реализуют
 * специфичные варианты пагинации: два высокоуровневых (с сатурацией) и один
 * низкоуровневый (без сатурации).<br/><br/>
 *
 * <b>1) PAGE ({@link FispaRequestPage})</b>: высокоуровневый способ навигации,
 * позволяющий задать желаемые размер и индекс страницы. Если искомая страница
 * остутствует, будет выдана последняя страница (если это возможно), а в ответе
 * будут указаны адаптированные, итоговые координаты полученной страницы.<br/><br/>
 *
 * <b>2) SHIFT ({@link FispaRequestShift})</b>: высокоуровневый способ навигации,
 * позволяющий задать желаемые размер и смещение фрагмента. Если искомый фрагмент
 * отсутствует, либо размеры фрагмента пересекают доступное количество элементов,
 * координаты фрагмента будут пересчитаны (сатурированы) с учётом имеющегося количества
 * элементов в источнике. В ответе будут указаны адаптированные, итоговые координаты
 * полученного фрагмента.<br/><br/>
 *
 * <b>3) OFFSET ({@link FispaRequestOffset})</b>: низкоуровневый способ навигации,
 * позволяющий задать желаемые размер и смещение фрагмента. Эти размер и смещение
 * будут переданы источнику записей дословно, без адаптации. Таким образом, данный
 * вариант не обеспечивает сатурацию (если необходимо, клиент должен позаботиться об
 * этом сам). В результате вызова может быть возвращён пустой список, даже если в
 * источнике есть данные, но запрошенный фрагмент "промахивается" мимо них.<br/><br/>
 *
 * Для любого варианта поддерживается возможность включения/выключения поддержки
 * консистентности ({@link #checkConsistency(boolean)}, {@link #checkConsistency(int)}).
 * <br/><br/>
 *
 * @param <F> Тип, используемый для задания фильтров
 * @param <S> Тип, используемый для задания сортировки
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public abstract class FispaRequest<F, S> {

    private static final int CHECK_COUNT = 10;

    public final F filter;
    public final S sorting;

    public final Long limit;

    private boolean checkConsistency = true;
    private int checkCount = CHECK_COUNT;


    protected FispaRequest(F filter, S sorting, Long limit) {

        this.filter  = filter;
        this.sorting = sorting;

        this.limit   = limit;

    }

    /**
     * Главный клиентский метод, позволяющий получить фрагмен записей, заданный
     * данным объектом Fispa. Для работы методу необходимо передать функции, обеспечивающие
     * доступ к источнику записей:
     * @param totalCountFunction Функция, получающая на вход параметр фильтрации и возвращающая
     *                           количество записей с учётом фильтрации, имеющихся в источнике.
     * @param pageFunction       Функция получает на вход объект с параметрами фильтрации, сортировки,
     *                           смещения и размера, выполняет запрос к источнику данных и возвращает
     *                           результат в виде коллекции.
     * @param <E>                Тип коллекции, с которым работает источник данных.
     * @return Объект, содержащий запрошенный фрагмент данных, полученных из источника, а также
     * фактические его координаты в источнике (после адаптации, если таковая применялась).
     */
    public <E extends Collection> FispaResponse<E> page(ToLongFunction<F> totalCountFunction,
                                 Function<PageRequest<F, S>, E> pageFunction) {

        FispaResponse<E> response;
        int check = 0;

        do {

            response = adapt(totalCountFunction, pageFunction);
            check++;

        } while (checkConsistency && check < checkCount && inconsistent(response));

        return response;

    }


    /**
     * Метод позволяет включить или выключить поддержку консистентности результата,
     * возвращаемого методом {@link #page(ToLongFunction, Function)}.<br/><br/>
     *
     * Так как между получением максимального количества записей в источнике и непосредственным
     * запросом данных из (возможно адаптированного) фрагмента состояние источника может
     * измениться (могут появиться новые записи, или исчезнуть старые), возможно появление
     * неконсистентности между полученной коллекцией записей и её координатами.<br/><br/>
     *
     * По умолчанию защита от неконсистентности включена. При получении результата выполняется
     * проверка консистентности коллекции и его координат, и, в случае необходимости, происходит
     * повторная адаптация и новый запрос данных из источника. Это повторяется до получения
     * консистентного результата, либо до истечения максимального количества попыток.
     *
     */
    public FispaRequest<F, S> checkConsistency(boolean check) {
        this.checkConsistency = check;
        return this;
    }

    /**
     * Метод позволяет включить поддержку консистентности результата, возвращаемого методом
     * {@link #page(ToLongFunction, Function)}, и задать максимальное количество проверок.
     * Если при выполнении запроса будет исчерпано максимальное количество проверок, будет
     * возвращён результат без гарантии его консистентности.<br/><br/>
     * См. {@link #checkConsistency(boolean)}.
     */
    public FispaRequest<F, S> checkConsistency(int checkCount) {
        this.checkCount = checkCount;
        return checkConsistency(true);
    }


    protected abstract <E extends Collection> FispaResponse<E> adapt(ToLongFunction<F> totalCountFunction,
                                              Function<PageRequest<F, S>, E> pageFunction);

    protected abstract <E extends Collection> boolean inconsistent(FispaResponse<E> response);


    /**
     * Интерфейс для объектов, содержащих параметры, необходимые для запроса определённого
     * фрагмента данных из источника.
     */
    public interface PageRequest<F, S> {
        F getFilter();
        S getSorting();
        long getOffset();
        long getLimit();
    }

    protected class FispaPageRequest implements PageRequest<F, S> {

        @Getter
        private final long offset;

        protected FispaPageRequest(long offset) {
            this.offset = offset;
        }

        @Override
        public F getFilter() {
            return filter;
        }

        @Override
        public S getSorting() {
            return sorting;
        }

        public long getLimit() {
            return limit;
        }

    }


    public static <T> Collector<T, List<T>, T> composeCollector(Function<List<T>, T> finalizer) {
        return Collector.of(
                ArrayList::new,
                List::add,
                (left, right) -> {
                    left.addAll(right);
                    return left;
                },
                list -> {
                    if (list.size() == 1) {
                        return list.get(0);
                    } else if (list.size() > 1) {
                        return finalizer.apply(list);
                    } else {
                        return null;
                    }
                }
        );
    }

}