package com.greenstones.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import com.greenstones.dto.fields.Field;
import com.greenstones.dto.fields.FieldParser;
import com.greenstones.dto.wrappers.BeanWrapper;
import com.greenstones.dto.wrappers.MapWrapper;

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

	public Mapper(Supplier<O> instanceFactory, Function<I, Source<I>> sourceFactory,
			Function<O, Target<O>> targetFactory) {
		this(instanceFactory, new ArrayList<Mapping<I, O>>(), sourceFactory, targetFactory);
	}

	public Mapper(Supplier<O> instanceFactory) {
		this(instanceFactory, new ArrayList<Mapping<I, O>>(), in -> new BeanWrapper<I>(in),
				out -> new BeanWrapper<O>(out));
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

	@SuppressWarnings("unchecked")
	public <R extends Mapper<I, O>> R with(Mapping<I, O> mapping) {
		mappings.add(mapping);
		return (R) this;
	}

	// builders

	public <V, R extends Mapper<I, O>> R add(String prop, Function<I, V> transform) {
		Mapping<I, O> reducer = (in, out) -> {
			V ip = transform.apply(in.val());
			out.set(prop, ip);
		};
		return with(reducer);
	}

	public <IP, OP, R extends Mapper<I, O>> R copy(PropertySelector<IP, OP> propSelector) {
		return with(Props.copy(propSelector));
	}

	// fluent api
	public <R extends Mapper<I, O>> R copyAll() {
		return with(Props.copy(Props.all()));
	}

	public <R extends Mapper<I, O>> R except(String... props) {
		return with(Props.copy(Props.except(props)));
	}

	public <R extends Mapper<I, O>> R copy(String... props) {
		return with(Props.copy(Props.props(props)));
	}

	public <R extends Mapper<I, O>> R copy(String prop) {
		return with(Props.copy(Props.prop(prop)));
	}

	@SuppressWarnings("unchecked")
	public <R extends Mapper<I, O>> R with(String def) {
		List<Field> fields = FieldParser.parse(def);
		Field.addMappings(this, fields);
		return (R) this;
	}

	// helpers

	public static <E> Supplier<E> factory(Class<E> type) {
		return () -> {
			try {
				return type.newInstance();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		};
	}

	// mappers

	public static class ModelToMap<E> extends Mapper<E, Map<String, Object>> {

		public ModelToMap() {
			super(HashMap<String, Object>::new, in -> new BeanWrapper<E>(in), out -> new MapWrapper(out));
		}
	}

	public static class MapToModel<E> extends Mapper<Map<String, Object>, E> {

		public MapToModel(Class<E> type) {
			super(factory(type), in -> new MapWrapper(in), out -> new BeanWrapper<E>(out));
		}

	}

	public static class Simple extends ModelToMap<Object> {
		public Simple() {
			super();
		}
	}

	public static Mapper.Simple from(String def) {
		return new Simple().with(def);
	}

}