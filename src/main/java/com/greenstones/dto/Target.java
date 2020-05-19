package com.greenstones.dto;

public interface Target<E> {
	void set(String prop, Object v);

	boolean accept(String prop);

	E val();
}