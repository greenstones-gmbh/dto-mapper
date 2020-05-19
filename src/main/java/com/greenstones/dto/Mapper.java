package com.greenstones.dto;

import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class Mapper<E> extends AbstractMapper<E, Data> {

	public Mapper() {
		super(Data.class);
	}

	public Mapper<E> add(Reducer<E, Data> reducer) {
		super.add(reducer);
		return this;
	}

	public Stream<Data> map(Iterable<E> iterable) {
		return StreamSupport.stream(iterable.spliterator(), false).map(this::map);
	}

	// ---------------------------

	public Mapper<E> add(String name, Function<E, Object> transform) {
		return add(Props.add(name, transform));
	}

	public Mapper<E> copy(String... props) {
		return add(Props.copy(props));
	}

	public Mapper<E> copyTo(String prop, String toProp) {
		return add(Props.copyTo(prop, toProp));
	}

	public <T> Mapper<E> copy(String prop, Function<T, Object> transform) {
		return add(Props.copy(prop, transform));
	}

	public <T> Mapper<E> copy(String prop, Mapper<T> mapper) {
		return add(Props.copy(prop, mapper));
	}

	public Mapper<E> exclude(String... props) {
		return add(Props.exclude(props));
	}

	// -----------------------

	public static <E> Mapper<E> allProps() {
		return new Mapper<E>().add(Props.copyAll());
	}

	public static <E> Mapper<E> except(String... props) {
		Mapper<E> mapper = allProps();
		return mapper.exclude(props);
	}

	public static <E> Mapper<E> empty() {
		return new Mapper<E>();
	}

	public static <E> Mapper<E> props(String... props) {
		return new Mapper<E>().copy(props);
	}

}
