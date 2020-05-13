package com.greenstones.dto;

import java.util.ArrayList;
import java.util.Collection;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Department {

	String id;
	String name;

	Collection<Employee> employees = new ArrayList<Employee>();

}
