package com.greenstones.dto;

import java.util.ArrayList;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class PropertyMapping<IP, OP> {
	String from;
	String to;
	Function<IP, OP> transform = null;
	Collector<OP, ?, ?> collector;

	public static PropertyMapping<Object, Object> from(String from) {
		return new PropertyMapping<>(from, from, v -> v, Collectors.toList());
	}

	public static <IP, OP> PropertyMapping<IP, OP> from(String from, Function<IP, OP> transform) {
		return new PropertyMapping<>(from, from, transform, Collectors.toList());
	}

	public PropertyMapping<IP, OP> to(String propTo) {
		return new PropertyMapping<>(this.from, propTo, this.transform, Collectors.toList());
	}

	public <IP1, OP1> PropertyMapping<IP1, OP1> with(Function<IP1, OP1> transform) {
		return new PropertyMapping<>(this.from, this.to, transform, Collectors.toList());
	}

	public PropertyMapping<IP, OP> collector(Collector<OP, ?, ?> collector) {
		return new PropertyMapping<>(this.from, this.to, this.transform, collector);
	}

	@AllArgsConstructor
	public static class SinglePropertySelector<IP, OP> implements PropertySelector<IP, OP> {

		PropertyMapping<IP, OP> propMapping;

		@Override
		public Stream<PropertyMapping<IP, OP>> select(Source<?> source, Target<?> target) {
			ArrayList<PropertyMapping<IP, OP>> propMappings = new ArrayList<PropertyMapping<IP, OP>>();
			propMappings.add(propMapping);
			return propMappings.stream();
		}

		public SinglePropertySelector<IP, OP> to(String prop) {
			return new SinglePropertySelector<>(propMapping.to(prop));
		}

		public <IP1, OP1> SinglePropertySelector<IP1, OP1> with(Function<IP1, OP1> transform) {
			return new SinglePropertySelector<>(propMapping.with(transform));
		}

		public <IP1, OP1> SinglePropertySelector<IP1, OP1> with(Mapper<IP1, OP1> mapper) {
			return with(mapper::map);
		}

		public SinglePropertySelector<IP, OP> collector(Collector<OP, ?, ?> collector) {
			return new SinglePropertySelector<>(propMapping.collector(collector));
		}

	}

}
