package com.greenstones.dto.mappers;

import java.util.HashMap;
import java.util.Map;

import com.greenstones.dto.ModelMapper;
import com.greenstones.dto.wrappers.BeanWrapper;
import com.greenstones.dto.wrappers.MapWrapper;

public class ModelToMapMapper<E> extends ModelMapper<E, Map<String, Object>> {

	public ModelToMapMapper() {
		super(HashMap<String, Object>::new, in -> new BeanWrapper<E>(in), out -> new MapWrapper(out));
	}
	
	public ModelToMapMapper(String def) {
		this();
		with(def);
	}
	
}