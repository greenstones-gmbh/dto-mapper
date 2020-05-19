package com.greenstones.dto.n;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import com.greenstones.dto.n.PropMapping.SinglePropertySelector;

public class Props {

	public static <I, O, IP, OP> Mapping<I, O> copy(PropertySelector<I, O, IP, OP> propSelector) {

		Mapping<I, O> consumer = (source, target) ->

		propSelector.select(source, target).forEach(prop -> {
			@SuppressWarnings("unchecked")
			IP ip = (IP) source.get(prop.from);
			OP op = prop.transform.apply(ip);
			target.set(prop.to, op);
		});

		return consumer;
	}

	public static <I, O> PropertySelector<I, O, Object, Object> all() {

		PropertySelector<I, O, Object, Object> propSelector = (source,
				target) -> source.getProps().stream().filter(target::accept).map(p -> PropMapping.from(p));

		return propSelector;

	}

	public static <I, O> PropertySelector<I, O, Object, Object> except(String... names) {
		List<String> excludes = Arrays.asList(names);
		PropertySelector<I, O, Object, Object> propSelector = (source, target) -> source
				.getProps()
					.stream()
					.filter(target::accept)
					.filter(p -> !excludes.contains(p))
					.map(p -> PropMapping.from(p));

		return propSelector;

	}

	public static <I, O> PropertySelector<I, O, Object, Object> props(String... names) {
		PropertySelector<I, O, Object, Object> propSelector = (source,
				target) -> Arrays.asList(names).stream().map(p -> PropMapping.from(p));

		return propSelector;

	}

	public static <I, O> SinglePropertySelector<I, O, Object, Object> prop(String prop) {
		return prop(prop, prop, v -> v);
	}

	public static <I, O> SinglePropertySelector<I, O, Object, Object> prop(String from, String to) {
		return prop(from, to, v -> v);
	}

	public static <I, O, IP, OP> SinglePropertySelector<I, O, IP, OP> prop(String prop, Function<IP, OP> transform) {
		return prop(prop, prop, transform);

	}

	public static <I, O, IP, OP> SinglePropertySelector<I, O, IP, OP> prop(String from, String to,
			Function<IP, OP> transform) {
		PropMapping<IP, OP> m = new PropMapping<IP, OP>(from, to, transform);
		return new PropMapping.SinglePropertySelector<>(m);

	}

	public static <I, O, IP, OP> SinglePropertySelector<I, O, IP, OP> prop(String prop, Mapper<IP, OP> mapper) {
		return prop(prop, prop, mapper::map);
	}

}