package com.greenstones.dto;

@FunctionalInterface
public interface Reducer<E> {

	Data reduce(Data data, E e);
	
}
