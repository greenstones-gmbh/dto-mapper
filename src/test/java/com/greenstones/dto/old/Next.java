package com.greenstones.dto.old;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.BeanWrapperImpl;

import com.greenstones.dto.Employee;
import com.greenstones.dto.data.Data;
import com.greenstones.dto.old.Reducer;

import lombok.AllArgsConstructor;

public class Next<I, O> {

	public static class Mapper<I, O> {

		private List<BiConsumer<Source<I>, Target<O>>> reducers = new ArrayList<BiConsumer<Source<I>,Target<O>>>();

		Supplier<O> instanceFactory;
		Function<I, Source<I>> sourceFactory;
		Function<O, Target<O>> targetFactory;

		public Mapper(Supplier<O> instanceFactory, List<BiConsumer<Source<I>, Target<O>>> reducers) {
			super();
			this.instanceFactory = instanceFactory;
			this.reducers = reducers;

		}

		public Mapper(Class<O> type, List<BiConsumer<Source<I>, Target<O>>> reducers) {
			this(() -> {
				try {
					return type.newInstance();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}, reducers);
		}

		public O map(I in) {
			O out = instanceFactory.get();
			populate(in, out);
			return out;
		}

		protected void populate(I in, O out) {
			Source<I> source = sourceFactory.apply(in);
			Target<O> target = targetFactory.apply(out);
			reducers.forEach(r -> {
				r.accept(source,target);
			});
		}

		public static class Builder<I, O> {

			
			List<BiConsumer<Source<I>, Target<O>>> reducers = new ArrayList<BiConsumer<Source<I>,Target<O>>>();
			
			Class<O> type;
			Function<I, Source<I>> sourceFactory;
			Function<O, Target<O>> targetFactory;

			public Builder(Class<O> type, Function<I, Source<I>> sourceFactory, Function<O, Target<O>> targetFactory) {
				super();
				this.type = type;
				this.sourceFactory = sourceFactory;
				this.targetFactory = targetFactory;

			}

//			public <IP, OP> Mapper.Builder<I, O> copy(
//					BiFunction<Source, Target, Stream<PropMapping<IP, OP>>> propSelector) {
//				Reducer<I, O> reducer = (out, in) -> {
//					Source source = sourceFactory.apply(in);
//					Target target = targetFactory.apply(out);
//
//					propSelector.apply(source, target).forEach(prop -> {
//						@SuppressWarnings("unchecked")
//						IP ip = (IP) source.get(prop.from);
//						OP op = prop.transform.apply(ip);
//						target.set(prop.to, op);
//					});
//					return out;
//				};
//				add(reducer);
//				return this;
//			}

			public Mapper.Builder<I, O> add(BiConsumer<Source<I>, Target<O>> consumer) {
				add(consumer);
				return this;
			}

			public <V> Mapper.Builder<I, O> add(String prop, Function<I, V> transform) {
				Reducer<I, O> reducer = (out, in) -> {
					Target<O> target = targetFactory.apply(out);
					V ip = transform.apply(in);
					target.set(prop, ip);
					return out;
				};
				add(reducer);
				return this;
			}

			public <IP, OP> Mapper.Builder<I, O> copy(
					BiFunction<Source<I>, Target<O>, Stream<PropMapping<IP, OP>>> propSelector) {
				return add(Props.copy(propSelector));
			}

			

			public Mapper<I, O> build() {
				return new Mapper<I, O>(type, reducers);
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

	@AllArgsConstructor
	public static class PropMapping<IP, OP> {
		String from;
		String to;
		Function<IP, OP> transform = null;

		public static PropMapping<Object, Object> from(String from) {
			return new PropMapping<Object, Object>(from, from, v -> v);
		}

		public static <IP, OP> PropMapping<IP, OP> from(String from, Function<IP, OP> transform) {
			return new PropMapping<IP, OP>(from, from, transform);
		}

		public static <I, O> BiFunction<Source<I>, Target<O>, Stream<PropMapping<Object, Object>>> all() {

			BiFunction<Source<I>, Target<O>, Stream<PropMapping<Object, Object>>> propSelector = (source,
					target) -> source
							.getProps()
								.stream()
								.filter(target.getProps()::contains)
								.map(p -> PropMapping.from(p));

			return propSelector;

		}

		public static <I, O> BiFunction<Source<I>, Target<O>, Stream<PropMapping<Object, Object>>> except(
				String... names) {
			List<String> excludes = Arrays.asList(names);
			BiFunction<Source<I>, Target<O>, Stream<PropMapping<Object, Object>>> propSelector = (source,
					target) -> source
							.getProps()
								.stream()
								.filter(target.getProps()::contains)
								.filter(p -> !excludes.contains(p))
								.map(p -> PropMapping.from(p));

			return propSelector;

		}

		public static <I, O> BiFunction<Source<I>, Target<O>, Stream<PropMapping<Object, Object>>> props(
				String... names) {
			BiFunction<Source<I>, Target<O>, Stream<PropMapping<Object, Object>>> propSelector = (source,
					target) -> Arrays.asList(names).stream().map(p -> PropMapping.from(p));

			return propSelector;

		}

		public static <I, O> BiFunction<Source<I>, Target<O>, Stream<PropMapping<Object, Object>>> prop(String prop) {
			return prop(prop, prop, v -> v);
		}

		public static <I, O> BiFunction<Source<I>, Target<O>, Stream<PropMapping<Object, Object>>> prop(String from,
				String to) {
			return prop(from, to, v -> v);
		}

		public static <I, O, IP, OP> BiFunction<Source<I>, Target<O>, Stream<PropMapping<IP, OP>>> prop(String prop,
				Function<IP, OP> transform) {
			return prop(prop, prop, transform);

		}

		public static <I, O, IP, OP> BiFunction<Source<I>, Target<O>, Stream<PropMapping<IP, OP>>> prop(String from,
				String to, Function<IP, OP> transform) {
			PropMapping<IP, OP> m = new PropMapping<IP, OP>(from, to, transform);
			ArrayList<PropMapping<IP, OP>> propMappings = new ArrayList<PropMapping<IP, OP>>();
			propMappings.add(m);

			BiFunction<Source<I>, Target<O>, Stream<PropMapping<IP, OP>>> propSelector = (source,
					target) -> propMappings.stream();

			return propSelector;

		}

	}

	public static class Props {

		public static <I, O, IP, OP> BiConsumer<Source<I>, Target<O>> copy(
				BiFunction<Source<I>, Target<O>, Stream<PropMapping<IP, OP>>> propSelector) {

			BiConsumer<Source<I>, Target<O>> consumer = (source, target) ->

			propSelector.apply(source, target).forEach(prop -> {
				@SuppressWarnings("unchecked")
				IP ip = (IP) source.get(prop.from);
				OP op = prop.transform.apply(ip);
				target.set(prop.to, op);
			});

			return consumer;
		}

		public static <I, O> BiFunction<Source<I>, Target<O>, Stream<PropMapping<Object, Object>>> all() {

			BiFunction<Source<I>, Target<O>, Stream<PropMapping<Object, Object>>> propSelector = (source,
					target) -> source
							.getProps()
								.stream()
								.filter(target.getProps()::contains)
								.map(p -> PropMapping.from(p));

			return propSelector;

		}

		public static <I, O> BiFunction<Source<I>, Target<O>, Stream<PropMapping<Object, Object>>> except(
				String... names) {
			List<String> excludes = Arrays.asList(names);
			BiFunction<Source<I>, Target<O>, Stream<PropMapping<Object, Object>>> propSelector = (source,
					target) -> source
							.getProps()
								.stream()
								.filter(target.getProps()::contains)
								.filter(p -> !excludes.contains(p))
								.map(p -> PropMapping.from(p));

			return propSelector;

		}

		public static <I, O> BiFunction<Source<I>, Target<O>, Stream<PropMapping<Object, Object>>> props(
				String... names) {
			BiFunction<Source<I>, Target<O>, Stream<PropMapping<Object, Object>>> propSelector = (source,
					target) -> Arrays.asList(names).stream().map(p -> PropMapping.from(p));

			return propSelector;

		}

		public static <I, O> BiFunction<Source<I>, Target<O>, Stream<PropMapping<Object, Object>>> prop(String prop) {
			return prop(prop, prop, v -> v);
		}

		public static <I, O> BiFunction<Source<I>, Target<O>, Stream<PropMapping<Object, Object>>> prop(String from,
				String to) {
			return prop(from, to, v -> v);
		}

		public static <I, O, IP, OP> BiFunction<Source<I>, Target<O>, Stream<PropMapping<IP, OP>>> prop(String prop,
				Function<IP, OP> transform) {
			return prop(prop, prop, transform);

		}

		public static <I, O, IP, OP> BiFunction<Source<I>, Target<O>, Stream<PropMapping<IP, OP>>> prop(String from,
				String to, Function<IP, OP> transform) {
			PropMapping<IP, OP> m = new PropMapping<IP, OP>(from, to, transform);
			ArrayList<PropMapping<IP, OP>> propMappings = new ArrayList<PropMapping<IP, OP>>();
			propMappings.add(m);

			BiFunction<Source<I>, Target<O>, Stream<PropMapping<IP, OP>>> propSelector = (source,
					target) -> propMappings.stream();

			return propSelector;

		}

	}

	public static interface Source<E> {
		Object get(String prop);

		Set<String> getProps();

		E val();
	}

	public static interface Target<E> {
		void set(String prop, Object v);

		Set<String> getProps();

		E val();
	}

	static class BeanWrapper<E> implements Source<E>, Target<E> {

		BeanWrapperImpl bw;
		E val;

		public BeanWrapper(E e) {
			bw = new BeanWrapperImpl(e);
		}

		public E val() {
			return val;
		}

		@Override
		public void set(String prop, Object v) {
			bw.setPropertyValue(prop, v);
		}

		@Override
		public Object get(String prop) {
			return bw.getPropertyValue(prop);
		}

		@Override
		public Set<String> getProps() {
			return Arrays
					.asList(bw.getPropertyDescriptors())
						.stream()
						.map(pd -> pd.getName())
						.filter(prop -> !"class".equals(prop))
						.collect(Collectors.toSet());
		}

	}

	public static class DataWrapper implements Source<Data>, Target<Data> {

		Data data;

		public DataWrapper(Data out) {
			this.data = out;
		}

		@Override
		public Data val() {
			return data;
		}

		@Override
		public void set(String prop, Object v) {
			data.put(prop, v);
		}

		@Override
		public Object get(String prop) {
			return data.get(prop);
		}

		@Override
		public Set<String> getProps() {
			return data.getProps().keySet();
		}

	}

	public static void main(String[] args) {
		{
			Mapper<Employee, Data> mapper = Next.Mapper.Builder
					.<Employee>modelToData()
						.copy(Props.all())
						.copy(Props.except("a1", ""))
						.copy(Props.props("id", "name"))
						.copy(Props.prop("id"))
						.copy(Props.prop("a", "b"))
						.copy(Props.prop("a", "b", s -> 1l))
						.copy(Props.prop("a", s -> 1l))
						.add("aaa", e -> e.getLastName())
						.build();

			Data data = mapper.map(new Employee());

		}
		{
			// Mapper<Data, Employee> mapper = Next.mapper(Data.class, Employee.class);
			// Employee emp = mapper.map(new Data());
		}

	}

}
