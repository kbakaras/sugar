package ru.kbakaras.sugar.tree;

import java.util.Iterator;
import java.util.Set;

@SuppressWarnings("rawtypes")
public abstract class MetaBranch<M, V, P extends MetaNode, C extends MetaNode> extends MetaNode<M, V> implements Iterable<C> {
    public MetaBranch(MappedTree tree) {
        super(tree);
    }

    @SuppressWarnings("unchecked")
    public Iterator<C> iterator() {
        return new MetaNodeIterator(tree, childrenClass());
    }

    @SuppressWarnings("unchecked")
	public P getParent() {
        return MetaNode.createNode(
        		(Class<P>) mcParentClass.get(this.getClass()),
        		tree.getParent());
    }

    /**
     * @return Первый дочерний узел, если имеется. Иначе null.
     */
    public C getChild() {
        return MetaNode.createNode(childrenClass(), tree.getBranch(0));
    }

    public C getChild(Object childMap) {
        return MetaNode.createNode(childrenClass(), tree.getBranch(childMap));
    }

    public C createChild(Object map) {
        return MetaNode.createNode(childrenClass(), tree.createBranch(map));
    }

    public <T> Set<T> getSetOfMaps(Class<T> clazz) {
        return tree.getSetOfMaps(clazz, 0);
    }

    public C[] getChildren() {
        return MetaNode.getChildren(childrenClass(), tree);
    }

    @SuppressWarnings("unchecked")
	private Class<C> childrenClass() {
    	return (Class<C>) MetaNode.mcChildrenClass.get(this.getClass());
    }
}