package com.greenstones.dto.simple;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.greenstones.dto.Mapper;
import com.greenstones.dto.Props;
import com.greenstones.dto.Source;
import com.greenstones.dto.Target;
import com.greenstones.dto.bean.BeanWrapper;

public class SimpleMapper {

	public static <I> Mapper<I, Map<String, Object>> toMap() {
		Function<I, Source<I>> sourceFactory = in -> new BeanWrapper<I>(in);
		Function<Map<String, Object>, Target<Map<String, Object>>> targetFactory = out -> new MapWrapper(out);

		Mapper<I, Map<String, Object>> mapper = new Mapper<I, Map<String, Object>>(HashMap<String, Object>::new,
				sourceFactory, targetFactory);
		return mapper;

	}

	public static <O> Mapper<Map<String, Object>, O> mapTo(Class<O> toType) {
		Function<Map<String, Object>, Source<Map<String, Object>>> sourceFactory = in -> new MapWrapper(in);
		Function<O, Target<O>> targetFactory = out -> new BeanWrapper<O>(out);

		Mapper<Map<String, Object>, O> mapper = new Mapper<Map<String, Object>, O>(Mapper.factory(toType),
				sourceFactory, targetFactory);
		return mapper;
	}

	public static Mapper<Object, Map<String, Object>> with(String def) {
		Mapper<Object, Map<String, Object>> m = SimpleMapper.<Object>toMap();
		List<com.greenstones.dto.simple.Field> fields = FieldParser.parse(def);
		Field.addMappings(m, fields);
		return m;

	}

}
