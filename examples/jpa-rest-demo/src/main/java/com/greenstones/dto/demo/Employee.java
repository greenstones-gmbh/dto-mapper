package com.greenstones.dto.demo;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Employee {

	@Id
	String username;
	String firstName;
	String lastName;

	@ManyToOne
	Department department;

}
