package ru.kbakaras.sugar.tree;

@SuppressWarnings("rawtypes")
public class MetaRootSimple<C extends MetaNode> extends MetaRoot<Object, Object, C> {
    public MetaRootSimple() {
		super();
	}
	public MetaRootSimple(MappedTree tree) {
        super(tree);
    }
}