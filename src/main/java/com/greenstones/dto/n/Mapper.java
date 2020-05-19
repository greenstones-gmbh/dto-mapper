package com.greenstones.dto.n;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import com.greenstones.dto.Data;

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

	public Mapper(Class<O> type, List<Mapping<I, O>> mappings, Function<I, Source<I>> sourceFactory,
			Function<O, Target<O>> targetFactory) {
		this(() -> {
			try {
				return type.newInstance();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}, mappings, sourceFactory, targetFactory);
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

		Class<O> type;
		Function<I, Source<I>> sourceFactory;
		Function<O, Target<O>> targetFactory;

		public Builder(Class<O> type, Function<I, Source<I>> sourceFactory, Function<O, Target<O>> targetFactory) {
			super();
			this.type = type;
			this.sourceFactory = sourceFactory;
			this.targetFactory = targetFactory;

		}

		public Mapper.Builder<I, O> add(Mapping<I, O> mapping) {
			mappings.add(mapping);
			return this;
		}

		public <V> Mapper.Builder<I, O> add(String prop, Function<I, V> transform) {
			Mapping<I, O> reducer = (in, out) -> {
				System.err.println(">>>"+in.val());
				V ip = transform.apply(in.val());
				out.set(prop, ip);
			};
			add(reducer);
			return this;
		}

		public <IP, OP> Mapper.Builder<I, O> copy(PropertySelector<I, O, IP, OP> propSelector) {
			return add(Props.copy(propSelector));
		}

		public Mapper<I, O> build() {
			return new Mapper<I, O>(type, mappings, sourceFactory, targetFactory);
		}

		public static <I, O> Mapper.Builder<I, O> mapper(Class<I> fromType, Class<O> toType) {
			Function<I, Source<I>> sourceFactory = in -> new BeanWrapper<I>(in);
			Function<O, Target<O>> targetFactory = out -> new BeanWrapper<O>(out);
			Mapper.Builder<I, O> mapper = new Mapper.Builder<I, O>(toType, sourceFactory, targetFactory);
			return mapper;
		}

		public static <I> Mapper.Builder<I, Data> modelToData() {
			Function<I, Source<I>> sourceFactory = in -> new BeanWrapper<I>(in);
			Function<Data, Target<Data>> targetFactory = out -> new DataWrapper(out);

			Mapper.Builder<I, Data> mapper = new Mapper.Builder<I, Data>(Data.class, sourceFactory, targetFactory);
			return mapper;

		}

		public static <O> Mapper.Builder<Data, O> dataToModel(Class<O> toType) {
			Function<Data, Source<Data>> sourceFactory = in -> new DataWrapper(in);
			Function<O, Target<O>> targetFactory = out -> new BeanWrapper<O>(out);

			Mapper.Builder<Data, O> mapper = new Mapper.Builder<Data, O>(toType, sourceFactory, targetFactory);
			return mapper;

		}

	}
}