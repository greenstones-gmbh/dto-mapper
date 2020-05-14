package com.greenstones.dto.demo;

import java.util.ArrayList;
import java.util.Collection;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Department {

	@Id
	String id;
	String name;

	@OneToMany(mappedBy = "department")
	Collection<Employee> employees = new ArrayList<Employee>();

}
