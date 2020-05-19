package com.greenstones.dto;

import java.util.function.Function;
import java.util.function.Supplier;

public class DataMapper<E> extends AbstractMapper<Data, E> {

	Supplier<E> instanceFactory;

	public DataMapper(Class<E> type) {
		super(type);
	}

	public DataMapper<E> add(Reducer<Data, E> reducer) {
		super.add(reducer);
		return this;
	}

	// ---------------------------

	public DataMapper<E> add(String name, Function<Data, Object> transform) {
		return add(DataProps.add(name, transform));
	}

	public DataMapper<E> props(String... props) {
		return add(DataProps.copy(props));
	}

	public <T> DataMapper<E> copy(String prop, Function<Object, T> transform) {
		return add(DataProps.copy(prop, transform));
	}

	public <T> DataMapper<E> copy(String prop, DataMapper<T> mapper) {
		return add(DataProps.copy(prop, mapper));
	}

	public DataMapper<E> exclude(String... props) {
		return add(DataProps.exclude(props));
	}

	public DataMapper<E> all() {
		return add(DataProps.copyAll());
	}

	public DataMapper<E> except(String... props) {
		return all().exclude(props);
	}

	// -----------------------

	public static <E> DataMapper<E> forType(Class<E> type) {
		return new DataMapper<E>(type);
	}

//
//	public static <E> DataMapper<E> props(Class<E> type, String... props) {
//		return forType(type).props(props);
//	}
//
//	public static <E> DataMapper<E> except(Class<E> type, String... props) {
//		return all(type).exclude(props);
//	}

}
