package com.greenstones.dto.simple;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.greenstones.dto.Mapper;
import com.greenstones.dto.Props;

public class Field {
	public String name;

	public List<Field> fields = new ArrayList<>();

	public Field(String name) {
		super();
		this.name = name;
	}

	public Field add(String child) {
		fields.add(new Field(child));
		return this;
	}

	public Field add(Field child) {
		fields.add(child);
		return this;
	}

	@Override
	public String toString() {

		return name + asString(fields);
	}

	public static String asString(List<Field> fields) {
		String children = fields.parallelStream().map(f -> f.toString()).collect(Collectors.joining(", "));
		return (fields.isEmpty() ? "" : "{ " + children + " }");

	}

	public static <I, O> void addMappings(Mapper<I, O> mapper, List<Field> fields) {
		fields.forEach(f -> {
			if (f.fields.isEmpty())
				mapper.copy(f.name);
			else {
				Mapper<Object, Map<String, Object>> m = SimpleMapper.<Object>toMap();
				addMappings(m, f.fields);
				mapper.copy(Props.prop(f.name).with(m));
			}
		});

	}
	
	
	

}