
package com.greenstones.dto;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SimpleMapperTests {

	private Department dep;
	private Employee emp1;
	private Employee emp2;

	@BeforeEach
	public void setup() {
		dep = new Department();
		dep.setName("Sales");
		dep.setId("dep1");

		emp1 = new Employee();
		emp1.setUsername("u1");
		emp1.setFirstName("Adam");
		emp1.setLastName("Rees");
		emp1.setDepartment(dep);

		emp2 = new Employee();
		emp2.setUsername("u2");
		emp2.setFirstName("Alison");
		emp2.setLastName("Jones");
		emp2.setDepartment(dep);

		dep.getEmployees().add(emp1);
		dep.getEmployees().add(emp2);
	}

	@Test
	void noProps() {

		Mapper.Simple mapper = Mapper.from("{ username, firstName, lastName, department { id, name }}");
		Map<String, Object> data = mapper.map(emp1);

		assertThat(data, notNullValue());

		assertThat(data.keySet(), containsInAnyOrder("department", "firstName", "lastName", "username"));

		assertThat(data.get("firstName"), is("Adam"));
		assertThat(data.get("lastName"), is("Rees"));
		assertThat(data.get("username"), is("u1"));

		assertThat(data.get("department"), notNullValue());
		@SuppressWarnings("unchecked")
		Map<String, Object> departmentData = (Map<String, Object>) data.get("department");
		assertThat(departmentData.keySet(), containsInAnyOrder("id", "name"));
		assertThat(departmentData.get("id"), is("dep1"));
		assertThat(departmentData.get("name"), is("Sales"));

	}

}
