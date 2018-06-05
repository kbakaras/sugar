package org.butu.sugar.tree;

import org.butu.sugar.lazy.MapCache;
import org.butu.sugar.tree.annotations.BranchNode;
import org.butu.sugar.tree.annotations.LeafNode;
import org.butu.sugar.tree.annotations.RootNode;
import org.butu.sugar.tree.annotations.RootNodeSimple;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Общий интерфейс для всех узлов мета-модели дерева
 * @author kbakaras
 */
public abstract class MetaNode<M, V> {
    MappedTree tree;

    public MetaNode(MappedTree tree) {
        this.tree = tree;
    }

    @SuppressWarnings("unchecked")
    public V getValue() {
        return (V) tree.getValue();
    }
    @SuppressWarnings("unchecked")
    public <T> T getValue(Class<T> clazz) {
        return (T) tree.getValue();
    }
    @SuppressWarnings("unchecked")
    public <T> T getValue(Class<T> clazz, Object...path) {
        return (T) tree.getValue(path);
    }

    public void setValue(V value) {
        tree.setValue(value);
    }

    @SuppressWarnings("unchecked")
    public M getMap() {
        return (M) tree.getMap();
    }

    public MappedTree getBranchPure(Object...path) {
        return tree.getBranch(path);
    }

    @SuppressWarnings("rawtypes")
    public <T extends MetaNode> T getBranch(Class<T> clazz, Object...path) {
        MappedTree branch = tree.getBranch(path);
        if (branch != null) return createNode(clazz, branch);
        return null;
    }

    @SuppressWarnings("rawtypes")
    public <T extends MetaNode> T createBranch(Class<T> clazz, Object...path) {
        return createNode(clazz, tree.createBranch(path));
    }

    public boolean containsBranch(Object... path) {
        return tree.containsBranch(path);
    }

    public int size() {
        return tree.size();
    }

    public void clear() {
    	tree.clear();
    }

    public void removeBranch(boolean removeEmptyParents) {
    	MappedTree.removeBranch(tree, removeEmptyParents);
	}

    public String getDebugString() {
    	return tree.getDebugString();
    }

    @SuppressWarnings("rawtypes")
	static MapCache<Class<? extends MetaNode>, Class<?>> mcMapClass =
            MapCache.of(clazz -> {
                if (clazz.isAnnotationPresent(RootNodeSimple.class)) {
                    return Object.class;
                } else if (clazz.isAnnotationPresent(RootNode.class)) {
                    return clazz.getAnnotation(RootNode.class).map();
                } else if (clazz.isAnnotationPresent(BranchNode.class)) {
                    return clazz.getAnnotation(BranchNode.class).map();
                } else if (clazz.isAnnotationPresent(LeafNode.class)) {
                    return clazz.getAnnotation(LeafNode.class).map();
                }
                return null;
            });

    @SuppressWarnings("rawtypes")
	static MapCache<Class<? extends MetaNode>, Class<? extends MetaNode>> mcParentClass =
            MapCache.of(clazz -> {
                if (clazz.isAnnotationPresent(BranchNode.class)) {
                    return clazz.getAnnotation(BranchNode.class).parent();
                } else if (clazz.isAnnotationPresent(LeafNode.class)) {
                    return clazz.getAnnotation(LeafNode.class).parent();
                }
                return null;
            });

    @SuppressWarnings("rawtypes")
	static MapCache<Class<? extends MetaNode>, Class<? extends MetaNode>> mcChildrenClass =
            MapCache.of(clazz -> {
                if (clazz.isAnnotationPresent(RootNodeSimple.class)) {
                    return clazz.getAnnotation(RootNodeSimple.class).children();
                } else if (clazz.isAnnotationPresent(RootNode.class)) {
                    return clazz.getAnnotation(RootNode.class).children();
                } else if (clazz.isAnnotationPresent(BranchNode.class)) {
                    return clazz.getAnnotation(BranchNode.class).children();
                }
                return null;
            });

    @SuppressWarnings("rawtypes")
    static public <T extends MetaNode> T createNode(Class<T> clazz, MappedTree tree) {
        if (tree == null) return null;
        try {
            Constructor<T> constructor = clazz.getConstructor(MappedTree.class);
            constructor.setAccessible(true);
            return constructor.newInstance(tree);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        } catch (SecurityException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("rawtypes")
    static <T extends MetaNode> T[] getChildren(Class<T>clazz, MappedTree tree) {
        @SuppressWarnings("unchecked")
        T[] result = (T[]) Array.newInstance(clazz, tree.size());
        int i = 0;
        for (MappedTree branch: tree) {
            result[i++] = createNode(clazz, branch);
        }
        return result;
    }
}