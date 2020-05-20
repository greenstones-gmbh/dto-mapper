package com.greenstones.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import com.greenstones.dto.bean.BeanWrapper;
import com.greenstones.dto.simple.Field;
import com.greenstones.dto.simple.FieldParser;
import com.greenstones.dto.simple.SimpleMapper;

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

	public Mapper<I, O> with(Mapping<I, O> mapping) {
//		List<Mapping<I, O>> ms = new ArrayList<Mapping<I, O>>();
//		ms.addAll(this.mappings);
//		ms.add(mapping);
//		return new Mapper<I, O>(instanceFactory, ms, sourceFactory, targetFactory);
//		
		mappings.add(mapping);
		return this;
	}

	// builders

	public <V> Mapper<I, O> add(String prop, Function<I, V> transform) {
		Mapping<I, O> reducer = (in, out) -> {
			V ip = transform.apply(in.val());
			out.set(prop, ip);
		};
		return with(reducer);
	}

	public <IP, OP> Mapper<I, O> copy(PropertySelector<IP, OP> propSelector) {
		return with(Props.copy(propSelector));
	}

	// fluent api
	public <IP, OP> Mapper<I, O> copyAll() {
		return with(Props.copy(Props.all()));
	}

	public <IP, OP> Mapper<I, O> except(String... props) {
		return with(Props.copy(Props.except(props)));
	}

	public <IP, OP> Mapper<I, O> copy(String... props) {
		return with(Props.copy(Props.props(props)));
	}

	public <IP, OP> Mapper<I, O> copy(String prop) {
		return with(Props.copy(Props.prop(prop)));
	}

	public <IP, OP> Mapper<I, O> with(String def) {
		List<Field> fields = FieldParser.parse(def);
		Field.addMappings(this, fields);
		return this;
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

}