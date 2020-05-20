package com.greenstones.dto;

import java.util.ArrayList;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.greenstones.dto.fields.SimpleMapper;

public class Examples {

	public static void main(String[] args) {
		simple();
		complex();
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

		Mapper<Department, Map<String, Object>> departmentMapper = SimpleMapper.<Department>toMap().copy("id", "name");
		Mapper<Employee, Map<String, Object>> employeeMapper = SimpleMapper
				.<Employee>toMap()
					.copyAll()
					.copy(Props.prop("department").with(departmentMapper));

		Map<String, Object> data = employeeMapper.map(emp1);
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

		Mapper<Employee, Map<String, Object>> employeeMapper = SimpleMapper
				.<Employee>toMap()
					.copy("username", "firstName", "lastName")
					.copy(Props.prop("department.name").to("departmentName"));

		Map<String, Object> data = employeeMapper.map(emp1);
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
		Mapper<Employee, Map<String, Object>> mapper = SimpleMapper.<Employee>toMap().except("department");

		// Map an Employee to generic DTO
		Map<String, Object> data = mapper.map(emp1);

		// Print as JSON
		print(data);

		// copy only 'username' and add 'fullName'
		mapper = SimpleMapper
				.<Employee>toMap()
					.copy("username")
					.add("fullName", e -> e.getFirstName() + " " + e.getLastName());
		print(mapper.map(emp1));

	}

	private static void print(Map<String, Object> data) {

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
