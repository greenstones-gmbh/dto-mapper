package com.greenstones.dto;

import java.util.stream.Stream;

@FunctionalInterface
public interface PropertySelector<IP, OP> {

	Stream<PropertyMapping<IP, OP>> select(Source<?> source, Target<?> target);

}