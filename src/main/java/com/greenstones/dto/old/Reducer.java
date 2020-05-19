package com.greenstones.dto.old;

@FunctionalInterface
public interface Reducer<I, O> {
	O reduce(O data, I e);
}