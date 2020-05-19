package com.greenstones.dto.n;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.BeanWrapperImpl;

public class BeanWrapper<E> implements Source<E>, Target<E> {

	BeanWrapperImpl bw;
	E val;

	public BeanWrapper(E e) {
		bw = new BeanWrapperImpl(e);
		val=e;
	}

	public E val() {
		return val;
	}

	@Override
	public void set(String prop, Object v) {
		bw.setPropertyValue(prop, v);
	}

	@Override
	public Object get(String prop) {
		return bw.getPropertyValue(prop);
	}

	@Override
	public Set<String> getProps() {
		return Arrays
				.asList(bw.getPropertyDescriptors())
					.stream()
					.map(pd -> pd.getName())
					.filter(prop -> !"class".equals(prop))
					.collect(Collectors.toSet());
	}
	
	@Override
	public boolean accept(String prop) {
		return getProps().contains(prop);
	}

}