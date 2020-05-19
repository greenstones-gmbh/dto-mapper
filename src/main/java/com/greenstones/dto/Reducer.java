package com.greenstones.dto;

@FunctionalInterface
public interface Reducer<I, O> {
	O reduce(O data, I e);
}