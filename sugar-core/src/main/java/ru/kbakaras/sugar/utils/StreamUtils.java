package ru.kbakaras.sugar.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collector;

@SuppressWarnings("unused")
public class StreamUtils {

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
