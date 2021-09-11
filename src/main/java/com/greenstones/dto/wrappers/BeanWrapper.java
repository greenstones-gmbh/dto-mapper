package com.greenstones.dto.wrappers;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.NotReadablePropertyException;

import com.greenstones.dto.Source;
import com.greenstones.dto.Target;

public class BeanWrapper<E> implements Source<E>, Target<E> {

	BeanWrapperImpl bw;
	E val;

	public BeanWrapper(E e) {
		bw = new BeanWrapperImpl(e);
		val = e;
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

		try {
			return bw.getPropertyValue(prop);
		} catch (NotReadablePropertyException e) {
			return getPropValue(val(), prop);

		}

	}

	@Override
	public Set<String> getProps() {
		return Arrays.asList(bw.getPropertyDescriptors()).stream().map(pd -> pd.getName())
				.filter(prop -> !"class".equals(prop)).collect(Collectors.toSet());
	}

	@Override
	public boolean accept(String prop) {
		return getProps().contains(prop);
	}

	static Object getPropValue(Object obj, String name) {

		try {
			return obj.getClass().getMethod("get" + capitalize(name)).invoke(obj);
		} catch (Exception e) {
			try {
				return obj.getClass().getMethod("is" + capitalize(name)).invoke(obj);

			} catch (Exception ee) {

			}
		}
		throw new RuntimeException("cant get prop " + name + " from " + obj);
	}

	static String capitalize(String str) {
		return Character.toTitleCase(str.charAt(0)) + str.substring(1);
	}
}