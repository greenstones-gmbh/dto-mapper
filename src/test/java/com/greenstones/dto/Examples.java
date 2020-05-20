package com.greenstones.dto;

import java.util.ArrayList;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.greenstones.dto.mappers.Mapper;
import com.greenstones.dto.mappers.ModelToMapMapper;

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

		Mapper departmentMapper = Mapper.from("{id,name}");

		ModelToMapMapper<Employee> employeeMapper = new ModelToMapMapper<Employee>()
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

		ModelToMapMapper<Employee> employeeMapper = new ModelToMapMapper<Employee>()
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

		// create a generic mapper
		Mapper mapper = Mapper.from("{username,firstName,lastName,department{id,name}}");

		// map an Employee to a map
		Map<String, Object> data = mapper.map(emp1);
		print(data);

		// create a generic mapper for all properties except 'department'
		mapper = new Mapper().except("department");
		data = mapper.map(emp1);
		print(data);

		// create a typed mapper and copy 'username' and add 'fullName'
		ModelToMapMapper<Employee> entityMapper = new ModelToMapMapper<Employee>()
				.copy("username")
					.add("fullName", e -> e.getFirstName() + " " + e.getLastName());
		print(entityMapper.map(emp1));

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
