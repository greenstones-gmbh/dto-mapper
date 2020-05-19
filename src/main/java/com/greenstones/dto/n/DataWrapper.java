package com.greenstones.dto.n;

import java.util.Set;

import com.greenstones.dto.Data;

public class DataWrapper implements Source<Data>, Target<Data> {

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

	@Override
	public boolean accept(String prop) {
		return true;
	}

}