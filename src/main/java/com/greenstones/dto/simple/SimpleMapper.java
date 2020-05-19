package com.greenstones.dto.simple;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import com.greenstones.dto.Mapper;
import com.greenstones.dto.Source;
import com.greenstones.dto.Target;
import com.greenstones.dto.bean.BeanWrapper;

public class SimpleMapper {

	public static <I> Mapper.Builder<I, Map<String, Object>> modelToData() {
		Function<I, Source<I>> sourceFactory = in -> new BeanWrapper<I>(in);
		Function<Map<String, Object>, Target<Map<String, Object>>> targetFactory = out -> new MapWrapper(out);

		Mapper.Builder<I, Map<String, Object>> mapper = new Mapper.Builder<I, Map<String, Object>>(
				HashMap<String, Object>::new, sourceFactory, targetFactory);
		return mapper;

	}

	public static <O> Mapper.Builder<Map<String, Object>, O> dataToModel(Class<O> toType) {
		Function<Map<String, Object>, Source<Map<String, Object>>> sourceFactory = in -> new MapWrapper(in);
		Function<O, Target<O>> targetFactory = out -> new BeanWrapper<O>(out);

		Mapper.Builder<Map<String, Object>, O> mapper = new Mapper.Builder<Map<String, Object>, O>(toType,
				sourceFactory, targetFactory);
		return mapper;
	}

}
