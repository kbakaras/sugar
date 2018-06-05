package org.butu.sugar.tree;

@SuppressWarnings("rawtypes")
public abstract class MetaLeaf<M, V, P extends MetaNode> extends MetaNode<M, V> {
    public MetaLeaf(MappedTree tree) {
        super(tree);
    }

	@SuppressWarnings("unchecked")
	public P getParent() {
        return MetaNode.createNode(
        		(Class<P>) MetaNode.mcParentClass.get(this.getClass()),
        		tree.getParent());
    }

    public final <T extends MetaNode> T getBranch(Class<T> clazz, Object...path) {
        return null;
    }
}