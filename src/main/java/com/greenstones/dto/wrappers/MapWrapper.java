package com.greenstones.dto.wrappers;

import java.util.Map;
import java.util.Set;

import com.greenstones.dto.Source;
import com.greenstones.dto.Target;

public class MapWrapper implements Source<Map<String, Object>>, Target<Map<String, Object>> {

	Map<String, Object> node;

	public MapWrapper(Map<String, Object> node) {
		this.node = node;
	}

	@Override
	public Map<String, Object> val() {
		return node;
	}

	@Override
	public void set(String prop, Object v) {
		node.put(prop, v);
	}

	@Override
	public Object get(String prop) {
		return node.get(prop);
	}

	@Override
	public Set<String> getProps() {
		return node.keySet();
	}

	@Override
	public boolean accept(String prop) {
		return true;
	}

}