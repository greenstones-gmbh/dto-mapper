package com.greenstones.dto.n;

import java.util.stream.Stream;

@FunctionalInterface
public interface PropertySelector<I, O, IP, OP> {

	Stream<PropMapping<IP, OP>> select(Source<I> source, Target<O> target);

}
