package ru.kbakaras.sugar.tree.annotations;

import ru.kbakaras.sugar.tree.MetaNode;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface RootNode {
	Class<?> map();
	Class<? extends MetaNode<?, ?>> children();
}