package com.greenstones.dto;

@FunctionalInterface
public interface Mapping<I, O> {

	void map(Source<I> source, Target<O> target);
}
