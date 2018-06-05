package org.butu.sugar.tree.annotations;

import org.butu.sugar.tree.MetaNode;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Анотация для обозначения простого корневого узла дерева значений. Простой узел не предполагает типизации
 * ключа. Простой узел используется в большинстве случаев. Единственная роль, которую он выполняет быть корнем.
 * @author kbakaras
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface RootNodeSimple {
	Class<? extends MetaNode<?, ?>> children();
}