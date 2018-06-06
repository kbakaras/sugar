package ru.kbakaras.sugar.tree;

import java.util.Iterator;

@SuppressWarnings("rawtypes")
class MetaNodeIterator<C extends MetaNode> implements Iterator<C> {
    private Iterator<MappedTree> iter;
    private Class<C> clazz;

    public MetaNodeIterator(MappedTree tree, Class<C> clazz) {
        this.iter = tree.iterator();
        this.clazz = clazz;
    }

    public boolean hasNext() {
        return iter.hasNext();
    }
    public C next() {
        return MetaNode.createNode(clazz, iter.next());
    }

    public void remove() {
        iter.remove();
    }
}