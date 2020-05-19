package com.greenstones.dto.data;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.greenstones.dto.Mapper;
import com.greenstones.dto.Source;
import com.greenstones.dto.Target;
import com.greenstones.dto.bean.BeanWrapper;

public class Data implements Serializable {

	private static final long serialVersionUID = 1523786861294458406L;

	Map<String, Object> props = new HashMap<String, Object>();

	public Data() {
		super();
	}

	@JsonCreator
	public Data(Map<String, Object> props) {
		super();
		this.props = props;
	}

	@JsonValue
	public Map<String, Object> getProps() {
		return Collections.unmodifiableMap(props);
	}

	public void put(String key, Object value) {
		props.put(key, value);
	}

	public Object get(String key) {
		return props.get(key);
	}

	public void remove(String key) {
		props.remove(key);
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
