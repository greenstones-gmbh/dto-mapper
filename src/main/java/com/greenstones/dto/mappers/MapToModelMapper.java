package com.greenstones.dto.mappers;

import java.util.Map;

import com.greenstones.dto.ModelMapper;
import com.greenstones.dto.wrappers.BeanWrapper;
import com.greenstones.dto.wrappers.MapWrapper;

public class MapToModelMapper<E> extends ModelMapper<Map<String, Object>, E> {

	public MapToModelMapper(Class<E> type) {
		super(factory(type), in -> new MapWrapper(in), out -> new BeanWrapper<E>(out));
	}

}