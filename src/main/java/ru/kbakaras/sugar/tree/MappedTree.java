package org.butu.sugar.tree;

import org.butu.sugar.compare.ComparatorNullable;
import org.butu.sugar.dates.DateUtils;
import org.butu.sugar.lazy.Lazy;
import org.butu.sugar.lazy.MapCache;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Отсортированное дерево. Каждый узел дерева имеет ключ, а также может иметь значение (Object).
 * @author   kbakaras
 */
public class MappedTree implements Serializable, Iterable<MappedTree>, Cloneable {
    public interface TreeFilter {
        public boolean filter(MappedTree branch);
    }

    private static final long serialVersionUID = 1L;

    public static final int STATE_GENERAL = 0;
    public static final int STATE_CACHE = 1;

    /**
     * кэш: уровень - размер
     **/
    transient private MapCache<Integer, Integer> sizeCache;
    transient private MapCache<Integer, int[]> shiftsCache;

    private class MappedTreeIterator implements Iterator<MappedTree> {
        private MappedTree current = null;
        private MappedTree[] values = null;
        private int currentIndex = -1;

        public MappedTreeIterator() {
            if (hasChildren()) values = children.values().toArray(new MappedTree[children.size()]);
        }

        public boolean hasNext() {
            return values != null && currentIndex < values.length - 1;
        }

        public MappedTree next() {
            current = values[++currentIndex];
            return current;
        }

        public void remove() {
            removeBranch(current);
            current = null;
            values[currentIndex] = null;
        }
    }

    protected Map<Object, MappedTree> children;
    private Object map;
    private Object value;
    private MappedTree parent;
    private int level;
    private int currentState = STATE_CACHE;

    @SuppressWarnings("unchecked")
    private Comparator<Object> comparator = ComparatorNullable.getInstance();

    // [+] Accessors
    public Object getValue() {
        return value;
    }
    public MappedTree setValue(Object value) {
        this.value = value;
        return this;
    }

    public MappedTree getParent() {
        return parent;
    }
    public Collection<MappedTree> getChildren() {
        return children == null ? null : children.values();
    }
    /**
     * @return Множество ключей дочерних узлов
     */
    public Set<Object> getChildrenMaps() {
        return children == null ? null : children.keySet();
    }

    public Object getMap() {
        return map;
    }

    public int getLevel() {
        return level;
    }
    // [.]

    /**
     * Создание новой корневой ветви
     */
    public MappedTree() {}
    /**
     * Создание новой ветви для последующего помещения внутрь какого-либо существующего узла.
     * @param map
     */
    public MappedTree(Object map) {
        this.map = map;
    }
    public MappedTree(Comparator<Object> comparator) {
        this();
        this.comparator = comparator;
    }
    public MappedTree(Object map, Comparator<Object> comparator) {
        this(map);
        this.comparator = comparator;
    }
    protected MappedTree(MappedTree parent, Object map) {
        this.comparator = parent.comparator;
        this.parent = parent;
        this.map = map;
        level = parent.level + 1;
    }

    protected void lazyInitChildren() {
        if (children == null) {
            children = new TreeMap<Object, MappedTree>(comparator);
        }
    }

    /**
     * Возвращает индекс элемента, находящегося на конце указанной цепочки узлов, для соответствующего
     * уровня. Т.е. последний элемент в массиве path должен соответствовать значению, которое вернёт
     * вызов функции <i>getBtanch(path.length, index)</i>, где <b>index</b> - наш искомый индекс.
     * @param path Путь к узлу
     * @return Индекс элемента на соответствующем уровне
     */
    public int getIndex(Object...path) {
        return doGetIndex(0, path);
    }
    @SuppressWarnings("unchecked")
    private int doGetIndex(int level, Object...path) {
        boolean flag = false;
        int index = 0;
        if (level < path.length - 1) {
            for (MappedTree branch: this) {
                if (branch.getMap().equals(path[level])) {
                    index += branch.doGetIndex(level + 1, path);
                    flag = true;
                    break;
                } else {
                    index += branch.size(path.length - level - 2);
                }
            }
        } else {
            if (hasChildren()) {
                Object[] array = children.keySet().toArray(new Object[size()]);
                index = Arrays.binarySearch(array, path[level], ComparatorNullable.getInstance());
                flag = index >= 0;
            }
        }

        if (flag) return index;
        else throw new IllegalArgumentException();
    }

    transient private Lazy<MappedTree[]> indexCache;
    private MappedTree[] getArray() {
        if ((currentState & STATE_CACHE) == 0) {
            return children.values().toArray(new MappedTree[children.size()]);
        } else {
            if (indexCache == null) {
                indexCache = Lazy.of(() -> children.values().toArray(new MappedTree[children.size()]));
            }
            return indexCache.get();
        }
    }

    public MappedTree getBranch(int index) {
        return getArray()[index];
    }

    /**
     * @param index Индекс узла, из которого нужно взять значение
     * @return Значение, взятое из указанного узла. Если указанного узла в дереве нет,
     * возвращает <b>null</b>. <u>Особенность</u>: если метод вернул <b>null</b>, это не обязательно
     * обозначает, что в дереве нет искомого узла.
     */
    public Object getValue(int index) {
        MappedTree branch = getBranch(index);
        return branch != null ? branch.getValue() : null;
    }

    /**
     * @param level Уровень, на котором нужно искать узел
     * @param index Индекс искомого узла
     * @return Возвращает узел, находящийся на уровне <b>level</b> от текущего, такой, что при обходе
     * дерева по порядку сортировки он будет иметь указанный индекс. Если такой узел найти не удаётся,
     * выбрасывается исключение IndexOutOfBoundsException.
     */
    public MappedTree getBranch(int level, int index) {
        if (index >= size(level)) throw new IndexOutOfBoundsException();
        return doGetBranch(level, index);
    }
    private MappedTree doGetBranch(int level, int index) {
        if (level == 0) {
            return getBranch(index);
        } else {
            if (size() == 1) {
                return children.values().iterator().next().doGetBranch(level - 1, index);
            } else if ((currentState & STATE_CACHE) == STATE_CACHE){
                int[] shifts = getShiftsCache().get(level);
                int indexEl = Math.abs(Arrays.binarySearch(shifts, index) + 1);
                while (getBranch(indexEl).size(level - 1) == 0) indexEl++;

                return getBranch(indexEl).doGetBranch(
                        level - 1,
                        index - (indexEl != 0 ? shifts[indexEl - 1] : 0));
            } else {
                int count = 0;
                for (MappedTree branch: this) {
                    int size = branch.size(level - 1);
                    if ((count + size) > index) {
                        return branch.doGetBranch(level - 1, index - count);
                    } else {
                        count += size;
                    }
                }
                return null;
            }
        }
    }

    /**
     * @param path Путь к искомому узлу.
     * @return Узел, или null, если по указанному пути не удалось обнаружить узел.
     */
    public MappedTree getBranch(Object ... path) {
        MappedTree tree = this;
        for (Object el: path) {
            if (!tree.hasChildren() || !tree.children.containsKey(el)) {
                return null;
            }
            tree = tree.children.get(el);
        }
        return tree;
    }

    /**
     * @param path Путь к узлу, из которого нужно взять значение
     * @return Значение, взятое из указанного узла. Если указанного узла в дереве нет,
     * возвращает <b>null</b>. <u>Особенность</u>: если метод вернул <b>null</b>, это не обязательно
     * обозначает, что в дереве нет искомого узла.
     */
    public Object getValue(Object ... path) {
        MappedTree branch = getBranch(path);
        return branch != null ? branch.getValue() : null;
    }

    /**
     * Возвращает последний узел дерева из указанного пути. Если в пути имеются несуществующие узлы,
     * они создаются и добавляются к дереву.
     * @param path Путь узлов от вершины дерева.
     * @return Последний узел из указанного пути.
     */
    public MappedTree createBranch(Object... path) {
        MappedTree tree = this;
        for (Object el: path) {
            tree.lazyInitChildren();
            if (!tree.children.containsKey(el)) {
                tree.children.put(el, createSubtree(tree, el));
                tree.invalidate();
            }
            tree = tree.children.get(el);
        }
        return tree;
    }
    /**
     * Добавляет (по мере надобности) в дерево узлы в соответствии с указанными объектами пути.
     * Если в последовательности объектов пути встречается объект со значением <b>null</b>, на этом
     * дальнейшее создание узлов прекращается. То есть не создаются узлы с ключом <b>null</b>.
     * @param path Путь узлов
     * @return Последний созданный узел
     */
    public MappedTree createBranchNonempty(Object...path) {
        Object[] newPath = path;
        for (int i = 0; i < path.length; i++) {
            if (path[i] == null) {
                newPath = Arrays.copyOf(path, i);
                break;
            }
        }
        return createBranch(newPath);
    }

    protected MappedTree createSubtree(MappedTree parent, Object map) {
        return new MappedTree(parent, map);
    }

    public void clear() {
        if (children != null) {
            for (MappedTree branch: children.values()) {
                branch.parent = null;
            }
        }
        children = null;
        value = null;
        invalidate();
    }

    public boolean hasChildren() {
        return children != null && !children.isEmpty();
    }

    public boolean containsBranch(Object ... path) {
        MappedTree tree = this;
        for (Object el: path) {
            if (!tree.hasChildren() || !tree.children.containsKey(el)) {
                return false;
            }
            tree = tree.getBranch(el);
        }
        return true;
    }

    /**
     * @return Количество дочерних ветвей, имеющихся у данной ветви.
     */
    public int size() {
        if (!hasChildren()) {
            return 0;
        } else {
            return children.size();
        }
    }
    /**
     * Количество дочерних ветвей, имеющихся на уровне <b>level</b> от текущего. Если <bl>level</b> == 0
     * результат должен быть аналогичен вызову метода <b>size()</b>.
     * @param level Уровень, на котором необходимо подсчитать количество ветвей.
     * @return Количество ветвей на указанном уровне.
     */
    public int size(int level) {
        if (level == 0) return size();
        else if ((currentState & STATE_CACHE) == STATE_CACHE) {
            return getSizeCache().get(level);
        } else {
            return doSize(level);
        }
    }

    private int doSize(int level) {
        int count = 0;
        for (MappedTree branch: this) {
            count += branch.size(level - 1);
        }
        return count;
    }

    public <T> Set<T> getSetOfMaps(Class<T> clazz, int level) {
        int size = size(level);
        if (size > 0) {
            Set<T> set = new HashSet<T>();
            for (int i = 0; i < size; i++) {
                set.add(clazz.cast(getBranch(level, i).getMap()));
            }
            return set;
        }
        return null;
    }

    /**
     * @return Глубину данной ветви. Если у данной ветви нет дочерних ветвей, то её глубина
     *  считается равной 0.
     */
    public int depth() {
        return doDepth(this, 0);
    }
    private int doDepth(MappedTree branch, int depth) {
        if (branch.hasChildren()) {
            int curDepth = depth;
            for (MappedTree child: branch.getChildren()) {
                curDepth = Math.max(curDepth, doDepth(child, depth + 1));
            }
            return curDepth;
        } else {
            return depth;
        }
    }

    public MappedTree findByMap(Object searchMap) {
        if (searchMap == null) {
            return null;
        }
        if (searchMap.equals(map)) {
            return this;
        } else if (children != null) {
            for (MappedTree tree: children.values()) {
                MappedTree found = tree.findByMap(searchMap);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    public String getDebugString() {
        String str = map == null ? "null" : convertToString(map);
        if (value != null) str += "(" + value + ")";
        str += "\n";
        str += getDebugString(this, "    ");
        return str;
    }
    private String getDebugString(MappedTree branch, String level) {
        String str = "";
        for (MappedTree branch1: branch) {
            str += level + convertToString(branch1.map);
            if (branch1.value != null) str += "(" + branch1.value + ")";
            str += "\n";
            str += getDebugString(branch1, level + "    ");
        }
        return str;
    }
    private static String convertToString(Object value) {
        if (value instanceof Calendar) {
            Calendar date = (Calendar) value;
            return DateUtils.format(date) + "[" + date.getTimeInMillis() + "] " + date.getTimeZone();
        } else {
            return value == null ? "[NULL]" : value.toString();
        }
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof MappedTree) {
            if (map != null) {
                return map.equals(((MappedTree)o).map);
            }
        }
        return false;
    }
    public String toString() {
        return map != null ? map.toString() : "[NULL]";
    }
    public int hashCode() {
        return map == null ? 0 : map.hashCode();
    }

    /**
     * Возвращает итератор для дочерних элементов данного узла. Итератор допускает удаление дочерних
     * узлов с помощью метода <b>remove</b>, а также любое добавление новых дочерних узлов.</br>
     * Добавление узлов по мере итерации по данному итератору никак его не затрагивает. Данный итератор
     * запоминает массив элементов на момент вызова данного метода и итерация происходит именно по
     * этому массиву.
     */
    public Iterator<MappedTree> iterator() {
        return new MappedTreeIterator();
    }

    public void changeState(int state) {
        if ((state & STATE_CACHE) == 0) {
            sizeCache = null;
            shiftsCache = null;
        }

        currentState = state;
        if (hasChildren()) {
            for (MappedTree child: this) {
                child.changeState(state);
            }
        }
    }

    /**
     * Очищает кэш размеров дочерних ветвей и кэши shifts, очищает также подобные кэши родителей
     **/
    public void invalidate() {
        if ((currentState & STATE_CACHE) == STATE_CACHE){
            if (sizeCache != null) sizeCache.clear();
            if (shiftsCache != null) shiftsCache.clear();
            if (indexCache != null) indexCache.clear();
        }
        if (parent != null) parent.invalidate();
    }

    private MapCache<Integer, Integer> getSizeCache() {
        if (sizeCache == null) {
            sizeCache = MapCache.of(this::doSize);
        }
        return sizeCache;
    }
    private MapCache<Integer, int[]> getShiftsCache() {
        if (shiftsCache == null) {
            shiftsCache = MapCache.of(level -> {
                int length = size() - 1;
                int[] result = new int[size() - 1];

                result[0] = getBranch(0).size(level - 1);
                for (int index = 1; index < length; index++) {
                    result[index] = result[index - 1] + getBranch(index).size(level - 1);
                }

                return result;
            });
        }
        return shiftsCache;
    }

    private transient MapCache<Class<?>, Method> mcMethods =
            MapCache.of(value -> {
                try {
                    Method method = value.getDeclaredMethod("clone");
                    method.setAccessible(true);
                    return method;
                } catch (SecurityException e) {
                    throw new RuntimeException(e);
                } catch (NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
            });
    private Object cloneObject(Object object) {
        if (object == null) {
            return null;
        } else if (object instanceof Cloneable) {
            try {
                return mcMethods.get(object.getClass()).invoke(object);
            } catch (IllegalArgumentException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        } else if (object instanceof Integer) {
            return object;
        } else {
            throw new RuntimeException("MappedTree value Class not clonable: " + object.getClass().getSimpleName());
        }
    }

    public MappedTree clone() {
        MappedTree mt = new MappedTree();
        mt.map = this.map;
        mt.value = cloneObject(this.value);
        for (MappedTree branch: this) clone(mt, branch);

        return mt;
    }
    private void clone(MappedTree clone, MappedTree source) {
        MappedTree newBranch = clone.createBranch(source.map);
        newBranch.setValue(cloneObject(source.getValue()));
        for (MappedTree branch: source) clone(newBranch, branch);
    }

    /**
     * Перемещает указанную ветку в указанное место (делает её дочерней, по отношению
     * к указанной ветви и удаляет из списка дочерних предыдущего родителя).
     * @param branch
     * @param destination
     */
    public static void transferBranch(MappedTree branch, MappedTree destination) {
        MappedTree parent = branch.parent;
        if (parent != null) {
            parent.children.remove(branch.map);
        }

        destination.lazyInitChildren();
        destination.children.put(branch.map, branch);
        branch.parent = destination;
        branch.level = destination.level + 1;
        // TODO Продумать инвалидацию.
        // TODO Сделать как возможность замены ветки, так и слияние ветвей.
        /*if (!destination.children.containsKey(branch.map)) {
            destination.children.put(branch.map, branch);
        }*/
    }

    /**
     * Объединяет детей передаваемого дерева путем вызова<br>
     * destination.createBranch(child.getMap()).setValue(child.getValue())<br>
     * рекурсивно для каждой ветки. Таким образом destination получает<br>
     * все узлы и их значения дерева copy
     */
    public static void mergeChildren(MappedTree copy, MappedTree destination) {
        if (copy.size() == 0) {
            return;
        } else {
            for (MappedTree child: copy) {
                MappedTree newBranch = destination.createBranch(child.getMap());
                newBranch.setValue(child.getValue());
                mergeChildren(child, newBranch);
            }
        }
    }

    public static void removeBranch(MappedTree branch) {
        if (branch.parent != null) {
            if (branch.parent.children.remove(branch.map) != null) {
                branch.parent.invalidate();
            }
            branch.parent = null;
        }
    }
    /**
     * Удаление указанной ветви из дерева.
     * @param branch Узел, задающий начало удаляемой ветви
     * @param removeEmptyParents Флаг удаления родительских ветвей, если после удаления
     * указанной ветви они становятся пустыми (т.е. удаляемая ветвь была единственной веткой
     * родителя).
     */
    public static void removeBranch(MappedTree branch, boolean removeEmptyParents) {
        if (branch.parent != null) {
            if (branch.parent.children.remove(branch.map) != null) {
                if (removeEmptyParents && !branch.parent.hasChildren() && branch.parent.parent != null) {
                    removeBranch(branch.parent, true);
                } else {
                    branch.parent.invalidate();
                }
            }
            branch.parent = null;
        }
    }

    /**
     * Добавляет указанное значение типа <i>Integer</i> к значению, содержащемуся в указанном узле дерева.
     * Если результат сложения отличается от 0, то он помещается в качестве значения в узел, если
     * результат равен 0, то в узел помещается <b>null</b>.
     * @param branch Узел
     * @param value Значение. Если <b>null</b>, то ничего не происходит.
     */
    public static void addInteger(MappedTree branch, Integer value) {
        if (value != null && value != 0) {
            Integer old = (Integer) branch.getValue();
            if (old != null) {
                old += value;
                branch.setValue(old != 0 ? old : null);
            } else {
                branch.setValue(value);
            }
        }
    }

    public static boolean equalNodes(MappedTree tree1, MappedTree tree2) {
        Object value1 = tree1.getValue();
        Object value2 = tree2.getValue();
        if ((value1 != null && !value1.equals(value2)) || (value2 != null && !value2.equals(value1))) {
            return false;
        } else {
            value1 = tree1.getMap();
            value2 = tree2.getMap();
            if ((value1 != null && !value1.equals(value2)) || (value2 != null && !value2.equals(value1))) {
                return false;
            }
        }
        return true;
    }
    public static boolean equalTrees(MappedTree tree1, MappedTree tree2) {
        if (!equalNodes(tree1, tree2)) {
            return false;
        } else if (!tree1.hasChildren() && !tree2.hasChildren()) {
            return true;
        } else if (tree1.hasChildren() && tree2.hasChildren()) {
            int size = tree1.getChildren().size();
            if (size != tree2.getChildren().size()) {
                return false;
            }

            MappedTree[] children1 = new MappedTree[size];
            tree1.getChildren().toArray(children1);
            MappedTree[] children2 = new MappedTree[size];
            tree2.getChildren().toArray(children2);
            for (int i = 0; i < size; i++) {
                if (!equalTrees(children1[i], children2[i])) {
                    return false;
                }
            }

            return true;
        } else {
            return false;
        }
    }

    public static void replaceMap(MappedTree branch, Object newMap) {
        branch.parent.children.remove(branch.map);
        branch.parent.children.put(newMap, branch);
        branch.map = newMap;
    }

    /**
     * @param mt Исходное дерево
     * @param filter Фильтр
     * @return Дерево, полученое путём наложения фильтра на исходное дерево
     */
    public static MappedTree filter(MappedTree mt, TreeFilter filter) {
        if (filter.filter(mt)) {
            MappedTree result = new MappedTree(mt.getMap());
            result.setValue(mt.getValue());
            filter(result, mt, filter);
        }

        return null;
    }
    private static void filter(MappedTree result, MappedTree branch, TreeFilter filter) {
        for (MappedTree child: branch) {
            if (filter.filter(child)) {
                MappedTree nextResult = result.createBranch(child.getMap());
                nextResult.setValue(child.getValue());
                filter(nextResult, child, filter);
            }
        }
    }

    public static String getDebugEdgeList(MappedTree tree) {
        return getDebugEdgeList(tree, "{{ROOT}}", new int[] {0});
    }
    private static String getDebugEdgeList(MappedTree tree, String parent, int[] counter) {
        String str = "";
        for (MappedTree branch: tree) {
            String label = convertToString(branch.getMap());
            String id = counter[0] + "-" + label;
            str += parent + "\t" + id + "\t" + label;
            if (branch.value != null) str += "(" + branch.value + ")";
            str += "\n";

            counter[0]++;
            str = getDebugEdgeList(branch, id, counter) + str;
        }
        return str;
    }
}