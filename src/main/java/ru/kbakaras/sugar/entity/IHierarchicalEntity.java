package org.butu.sugar.entity;

public interface IHierarchicalEntity<E extends IEntity> extends IEntity {
	public E getParent();
	public void setParent(E parent);

	/**
	 * Используется при построении иерархических списков.
	 * @return <b>true</b>, если данный элемент является (или может являться) родителем для дочерних
	 * элементов, <b>false</b> - в противном случае.
	 */
	public boolean isFolder();
}