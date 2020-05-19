package com.greenstones.dto;

import java.util.ArrayList;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.greenstones.dto.data.Data;
import com.greenstones.dto.old.Mapper;

public class Examples {

	public static void main(String[] args) {
		nested();
	}

	public static void complex() {

		Department dep = new Department();
		dep.setId("dep1");
		dep.setName("Sales");
		dep.setEmployees(new ArrayList<Employee>());

		Employee emp1 = new Employee();
		emp1.setUsername("u1");
		emp1.setFirstName("Adam");
		emp1.setLastName("Rees");
		emp1.setDepartment(dep);

		// circular dependency
		dep.getEmployees().add(emp1);

		Mapper<Department> departmentMapper = Mapper.props("id", "name");
		Mapper<Employee> employeeMapper = Mapper.<Employee>allProps().copy("department",departmentMapper);

		Data data = employeeMapper.map(emp1);
		print(data);

	}
	
	
	public static void nested() {

		Department dep = new Department();
		dep.setId("dep1");
		dep.setName("Sales");
		dep.setEmployees(new ArrayList<Employee>());

		Employee emp1 = new Employee();
		emp1.setUsername("u1");
		emp1.setFirstName("Adam");
		emp1.setLastName("Rees");
		emp1.setDepartment(dep);

		// circular dependency
		dep.getEmployees().add(emp1);

		Mapper<Employee> employeeMapper = Mapper.<Employee>props("username","firstName","lastName").copyTo("department.name","departmentName");

		Data data = employeeMapper.map(emp1);
		print(data);

	}

	public static void simple() {

		Department dep = new Department();
		dep.setId("dep1");
		dep.setName("Sales");

		Employee emp1 = new Employee();
		emp1.setUsername("u1");
		emp1.setFirstName("Adam");
		emp1.setLastName("Rees");
		emp1.setDepartment(dep);

		// Mapper for all properties except 'department'
		Mapper<Employee> mapper = Mapper.<Employee>allProps().exclude("department");

		// Map an Employee to generic DTO
		Data data = mapper.map(emp1);

		// Print as JSON
		print(data);

		// copy only 'username' and add 'fullName'
		mapper = Mapper.<Employee>props("username").add("fullName", e -> e.getFirstName() + " " + e.getLastName());
		print(mapper.map(emp1));

	}

	private static void print(Data data) {

		try {
			String json = om.writerWithDefaultPrettyPrinter().writeValueAsString(data);
			System.out.println(json);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	static ObjectMapper om = new ObjectMapper();

}
