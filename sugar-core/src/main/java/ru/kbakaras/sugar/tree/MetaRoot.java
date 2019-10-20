package ru.kbakaras.sugar.tree;

import java.util.Iterator;
import java.util.Set;

@SuppressWarnings("rawtypes")
public abstract class MetaRoot<M, V, C extends MetaNode> extends MetaNode<M, V> implements Iterable<C> {
	public MetaRoot() {
		this(new MappedTree());
	}
    public MetaRoot(MappedTree tree) {
        super(tree);
    }

    @SuppressWarnings("unchecked")
    public Iterator<C> iterator() {
        return new MetaNodeIterator(tree, childrenClass());
    }

    @SuppressWarnings("unchecked")
	public Set<M> getSetOfMaps() {
        return tree.getSetOfMaps((Class<M>) MetaNode.mcMapClass.get(this.getClass()), 0);
    }

    public C getChild() {
        return MetaNode.createNode(childrenClass(), tree.getBranch(0));
    }

    public C getChild(Object childMap) {
        return MetaNode.createNode(childrenClass(), tree.getBranch(childMap));
    }

    public C createChild(Object map) {
        return MetaNode.createNode(childrenClass(), tree.createBranch(map));
    }

	public C[] getChildren() {
        return MetaNode.getChildren(childrenClass(), tree);
    }

    @SuppressWarnings("unchecked")
	private Class<C> childrenClass() {
    	return (Class<C>) MetaNode.mcChildrenClass.get(this.getClass());
    }
}