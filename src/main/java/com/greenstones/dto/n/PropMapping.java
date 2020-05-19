package com.greenstones.dto.n;

import java.util.ArrayList;
import java.util.function.Function;
import java.util.stream.Stream;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class PropMapping<IP, OP> {
	String from;
	String to;
	Function<IP, OP> transform = null;

	public static PropMapping<Object, Object> from(String from) {
		return new PropMapping<>(from, from, v -> v);
	}

	public static <IP, OP> PropMapping<IP, OP> from(String from, Function<IP, OP> transform) {
		return new PropMapping<>(from, from, transform);
	}

	public PropMapping<IP, OP> to(String propTo) {
		return new PropMapping<>(this.from, propTo, this.transform);
	}

	@AllArgsConstructor
	public static class SinglePropertySelector<I, O, IP, OP> implements PropertySelector<I, O, IP, OP> {

		PropMapping<IP, OP> propMapping;

		@Override
		public Stream<PropMapping<IP, OP>> select(Source<I> source, Target<O> target) {
			ArrayList<PropMapping<IP, OP>> propMappings = new ArrayList<PropMapping<IP, OP>>();
			propMappings.add(propMapping);
			return propMappings.stream();
		}

		public SinglePropertySelector<I, O, IP, OP> to(String prop) {
			return new SinglePropertySelector<>(propMapping.to(prop));
		}

	}

}
