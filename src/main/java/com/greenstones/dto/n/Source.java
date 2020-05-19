package com.greenstones.dto.n;

import java.util.Set;

public interface Source<E> {
	Object get(String prop);

	Set<String> getProps();

	E val();
}