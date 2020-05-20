package com.greenstones.dto.mappers;

public class Mapper extends ModelToMapMapper<Object> {
	public Mapper() {
		super();
	}

	public static Mapper from(String def) {
		return new Mapper().with(def);
	}
}