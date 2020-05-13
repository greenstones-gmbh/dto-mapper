package com.greenstones.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Employee {

	String username;
	String firstName;
	String lastName;

	Department department;

}
