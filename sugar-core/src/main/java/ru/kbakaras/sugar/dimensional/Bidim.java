package ru.kbakaras.sugar.dimensional;

import ru.kbakaras.sugar.tree.MappedTree;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Объект для хранения двумерных структур. Содержит ключи-измерения для строк,
 * ключи-измерения для столбцов. На пересечении находится значение.
 * Создано: kbakaras, в день: 04.12.2017.
 */
public class Bidim<R, C, V> {
    private DistinctList<R> dimRow;
    private DistinctList<C> dimCol;
    private MappedTree values;

    public Bidim(Collection<R> dimRow, Collection<C> dimCol) {
        this.dimRow = Optional.ofNullable(dimRow).map(DistinctList::new).orElse(new DistinctList<>());
        this.dimCol = Optional.ofNullable(dimCol).map(DistinctList::new).orElse(new DistinctList<>());
        this.values = new MappedTree();
    }

    public void setValue(R keyRow, C keyCol, V value) {
        values.createBranch(dimRow.ensure(keyRow), dimCol.ensure(keyCol)).setValue(value);
        invalidateTotals(keyRow, keyCol);
    }
    @SuppressWarnings("unchecked")
    public V getValue(R keyRow, C keyCol) {
        if (dimRow.contains(keyRow) && dimCol.contains(keyCol)) {
            return (V) values.getValue(keyRow, keyCol);
        } else {
            return null;
        }
    }

    public void setValue(int idxRow, int idxCol, V value) {
        setValue(dimRow.get(idxRow), dimCol.get(idxCol), value);
    }
    @SuppressWarnings("unchecked")
    public V getValue(int idxRow, int idxCol) {
        return (V) values.getValue(dimRow.get(idxRow), dimCol.get(idxCol));
    }

    public DistinctList<R> getDimRow() {
        return dimRow;
    }
    public DistinctList<C> getDimCol() {
        return dimCol;
    }

    public String getDebugString() {
        return values.getDebugString();
    }

    private void invalidateTotals(R keyRow, C keyCol) {
        if (totalsRow != null) totalsRow.invalidate(keyRow);
        if (totalsCol != null) totalsCol.invalidate(keyCol);
    }

    private BidimTotals<R, C, ?> totalsRow;
    public <T> IBidimTotals<R, T> totalsRow(Integral<T, R, C, V> integral) {
        if (totalsRow != null) totalsRow.dispose();
        BidimTotals<R, C, T> totals = new BidimTotals<>(dimRow, dimCol, this::getValue, integral);
        totalsRow = totals;
        return totals;
    }

    private BidimTotals<C, R, ?> totalsCol;
    public <T> IBidimTotals<C, T> totalsCol(Integral<T, C, R, V> integral) {
        if (totalsCol != null) totalsCol.dispose();
        BidimTotals<C, R, T> totals = new BidimTotals<>(dimCol, dimRow, (c, r) -> getValue(r, c), integral);
        totalsCol = totals;
        return totals;
    }

    private class BidimTotals<K, K1, T> implements IBidimTotals<K, T> {
        private boolean isDisposed = false;

        private DistinctList<K>  dim;
        private DistinctList<K1> dim1;

        private BiFunction<K, K1, V> get;
        private Integral<T, K, K1, V> integral;

        private Map<K, T> totals = new HashMap<>();

        @SuppressWarnings("unchecked")
        private BidimTotals(DistinctList<K> dim, DistinctList<K1> dim1, BiFunction<K, K1, V> get, Integral<T, K, K1, V> integral) {
            this.dim  = dim;
            this.dim1 = dim1;

            this.get      = get;
            this.integral = integral;
        }

        private void checkDisposed() {
            if (isDisposed) throw new BidimTotalsException("Bidim Totals object is disposed!");
        }

        public T get(int index) {
            checkDisposed();
            return get(dim.get(index));
        }
        public T get(K key) {
            checkDisposed();
            T total = totals.get(key);
            if (total == null) {
                Function<K1, V> func = k1 -> get.apply(key, k1);
                totals.put(key, total = integral.compute(key, dim1, func));
            }
            return total;
        }

        void invalidate(K key) {
            checkDisposed();
            totals.remove(key);
        }
        void invalidateAll() {
            checkDisposed();
            totals.clear();
        }

        void dispose() {
            isDisposed = true;
            dim  = null;
            dim1 = null;
        }
    }
}