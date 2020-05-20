package com.greenstones.dto;

import java.util.Arrays;
import java.util.List;
import java.util.stream.StreamSupport;

import com.greenstones.dto.PropertyMapping.SinglePropertySelector;

public class Props {

	public static <I, O, IP, OP> Mapping<I, O> copy(PropertySelector<IP, OP> propSelector) {

		Mapping<I, O> consumer = (source, target) ->

		propSelector.select(source, target).forEach(prop -> {
			Object in = source.get(prop.from);

			if (in instanceof Iterable<?>) {
				@SuppressWarnings("unchecked")
				Iterable<IP> iterable = (Iterable<IP>) in;
				target
						.set(prop.to,
								StreamSupport
										.stream(iterable.spliterator(), false)
											.map(i -> prop.transform.apply(i))
											.collect(prop.collector));
			} else {
				@SuppressWarnings("unchecked")
				IP ip = (IP) in;
				OP op = prop.transform.apply(ip);
				target.set(prop.to, op);
			}

		});

		return consumer;
	}

	public static PropertySelector<Object, Object> all() {

		PropertySelector<Object, Object> propSelector = (source,
				target) -> source.getProps().stream().filter(target::accept).map(p -> PropertyMapping.from(p));

		return propSelector;

	}

	public static PropertySelector<Object, Object> except(String... names) {
		List<String> excludes = Arrays.asList(names);
		PropertySelector<Object, Object> propSelector = (source, target) -> source
				.getProps()
					.stream()
					.filter(target::accept)
					.filter(p -> !excludes.contains(p))
					.map(p -> PropertyMapping.from(p));

		return propSelector;

	}

	public static PropertySelector<Object, Object> props(String... names) {
		PropertySelector<Object, Object> propSelector = (source,
				target) -> Arrays.asList(names).stream().map(p -> PropertyMapping.from(p));

		return propSelector;

	}

	public static SinglePropertySelector<Object, Object> prop(String from) {
		return new PropertyMapping.SinglePropertySelector<>(PropertyMapping.from(from));

	}

}