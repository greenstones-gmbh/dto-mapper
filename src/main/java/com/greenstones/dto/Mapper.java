package com.greenstones.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import com.greenstones.dto.bean.BeanWrapper;

public class Mapper<I, O> {

	Supplier<O> instanceFactory;
	Function<I, Source<I>> sourceFactory;
	Function<O, Target<O>> targetFactory;

	List<Mapping<I, O>> mappings = new ArrayList<Mapping<I, O>>();

	public Mapper(Supplier<O> instanceFactory, List<Mapping<I, O>> mappings, Function<I, Source<I>> sourceFactory,
			Function<O, Target<O>> targetFactory) {
		super();
		this.instanceFactory = instanceFactory;
		this.mappings = mappings;

		this.sourceFactory = sourceFactory;
		this.targetFactory = targetFactory;

	}

	public O map(I in) {
		O out = instanceFactory.get();
		populate(in, out);
		return out;
	}

	protected void populate(I in, O out) {
		Source<I> source = sourceFactory.apply(in);
		Target<O> target = targetFactory.apply(out);
		mappings.forEach(r -> {
			r.map(source, target);
		});
	}

	public static class Builder<I, O> {

		List<Mapping<I, O>> mappings = new ArrayList<Mapping<I, O>>();

		Supplier<O> instanceFactory;
		Function<I, Source<I>> sourceFactory;
		Function<O, Target<O>> targetFactory;

		public Builder(Class<O> type, Function<I, Source<I>> sourceFactory, Function<O, Target<O>> targetFactory) {
			this(() -> {
				try {
					return type.newInstance();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}, sourceFactory, targetFactory);
		}

		public Builder(Supplier<O> instanceFactory, Function<I, Source<I>> sourceFactory,
				Function<O, Target<O>> targetFactory) {
			super();
			this.instanceFactory = instanceFactory;
			this.sourceFactory = sourceFactory;
			this.targetFactory = targetFactory;

		}

		public Mapper.Builder<I, O> addMapping(Mapping<I, O> mapping) {
			mappings.add(mapping);
			return this;
		}

		public <V> Mapper.Builder<I, O> add(String prop, Function<I, V> transform) {
			Mapping<I, O> reducer = (in, out) -> {
				V ip = transform.apply(in.val());
				out.set(prop, ip);
			};
			addMapping(reducer);
			return this;
		}

		public <IP, OP> Mapper.Builder<I, O> copy(PropertySelector<IP, OP> propSelector) {
			return addMapping(Props.copy(propSelector));
		}

		public Mapper<I, O> build() {
			return new Mapper<I, O>(instanceFactory, mappings, sourceFactory, targetFactory);
		}

		public static <I, O> Mapper.Builder<I, O> mapper(Class<I> fromType, Class<O> toType) {
			Function<I, Source<I>> sourceFactory = in -> new BeanWrapper<I>(in);
			Function<O, Target<O>> targetFactory = out -> new BeanWrapper<O>(out);
			Mapper.Builder<I, O> mapper = new Mapper.Builder<I, O>(toType, sourceFactory, targetFactory);
			return mapper;
		}

	}

}