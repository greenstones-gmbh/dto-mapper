package com.greenstones.dto;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

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

}
