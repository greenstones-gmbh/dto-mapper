
package com.greenstones.dto;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.greenstones.dto.simple.SimpleMapper;

public class ModelToMapFluentTests {

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

		Mapper<Employee, Map<String, Object>> mapper = SimpleMapper.<Employee>toMap();
		Map<String, Object> data = mapper.map(emp1);

		assertThat(data, notNullValue());
		assertThat(data.keySet(), empty());

	}

	@Test
	void allProps() {

		Mapper<Employee, Map<String, Object>> mapper = SimpleMapper.<Employee>toMap().copyAll();
		Map<String, Object> data = mapper.map(emp1);

		assertThat(data, notNullValue());
		assertThat(data.keySet(), containsInAnyOrder("username", "firstName", "lastName", "department"));

		assertThat(data.get("username"), is("u1"));
		assertThat(data.get("firstName"), is("Adam"));
		assertThat(data.get("lastName"), is("Rees"));
		assertThat(data.get("department"), notNullValue());

	}

	@Test
	void copyProps() {

		Mapper<Employee, Map<String, Object>> mapper = SimpleMapper
				.<Employee>toMap()
					.copy("username", "firstName", "lastName")
					.copy(Props.prop("username").<String, String>with(e -> e.toUpperCase()).to("user"));
		Map<String, Object> data = mapper.map(emp1);

		assertThat(data, notNullValue());
		assertThat(data.keySet(), containsInAnyOrder("username", "firstName", "lastName", "user"));

		assertThat(data.get("username"), is("u1"));
		assertThat(data.get("firstName"), is("Adam"));
		assertThat(data.get("lastName"), is("Rees"));
		assertThat(data.get("user"), is("U1"));

	}

	@Test
	void excludeProps() {

		Mapper<Employee, Map<String, Object>> mapper = SimpleMapper.<Employee>toMap().except("department");
		Map<String, Object> data = mapper.map(emp1);

		assertThat(data, notNullValue());
		assertThat(data.keySet(), containsInAnyOrder("username", "firstName", "lastName"));

		assertThat(data.get("username"), is("u1"));
		assertThat(data.get("firstName"), is("Adam"));
		assertThat(data.get("lastName"), is("Rees"));

	}

	@Test
	void addProps() {

		Mapper<Employee, Map<String, Object>> mapper = SimpleMapper
				.<Employee>toMap()
					.copy("firstName", "lastName")
					.add("fullName", e -> e.getFirstName() + " " + e.getLastName());
		Map<String, Object> data = mapper.map(emp1);

		assertThat(data, notNullValue());
		assertThat(data.keySet(), containsInAnyOrder("fullName", "firstName", "lastName"));

		assertThat(data.get("fullName"), is("Adam Rees"));
		assertThat(data.get("firstName"), is("Adam"));
		assertThat(data.get("lastName"), is("Rees"));

	}

	@Test
	void copyNestedProps() {

		Mapper<Employee, Map<String, Object>> mapper = SimpleMapper
				.<Employee>toMap()
					.copy("firstName", "lastName")
					.copy(Props.prop("department.name").to("departmentName"));
		Map<String, Object> data = mapper.map(emp1);

		assertThat(data, notNullValue());
		assertThat(data.keySet(), containsInAnyOrder("departmentName", "firstName", "lastName"));

		assertThat(data.get("departmentName"), is("Sales"));
		assertThat(data.get("firstName"), is("Adam"));
		assertThat(data.get("lastName"), is("Rees"));

	}

	@Test
	void joinNestedList() {

		Mapper<Department, Map<String, Object>> departmentMapper = SimpleMapper
				.<Department>toMap()
					.except("employees")
					.copy(Props
							.prop("employees")
								.to("employeeNames")
								.<Employee, CharSequence>with(a -> a.getUsername())
								.collector(Collectors.joining(", ")));

		Map<String, Object> data = departmentMapper.map(dep);

		assertThat(data, notNullValue());

		assertThat(data.keySet(), containsInAnyOrder("id", "name", "employeeNames"));
		assertThat(data.get("id"), is("dep1"));
		assertThat(data.get("name"), is("Sales"));
		assertThat(data.get("employeeNames"), is("u1, u2"));

	}

	@Test
	void copyNestedEntiry() {

		Mapper<Department, Map<String, Object>> departmentMapper = SimpleMapper
				.<Department>toMap()
					.copy("id", "name")
					.add("employeesCount", d -> d.getEmployees().size());

		Mapper<Employee, Map<String, Object>> mapper = SimpleMapper
				.<Employee>toMap()
					.copy("firstName", "lastName")
					.copy(Props.prop("department").with(departmentMapper));

		Map<String, Object> data = mapper.map(emp1);

		assertThat(data, notNullValue());
		assertThat(data.keySet(), containsInAnyOrder("department", "firstName", "lastName"));

		assertThat(data.get("firstName"), is("Adam"));
		assertThat(data.get("lastName"), is("Rees"));

		assertThat(data.get("department"), notNullValue());
		Map<String, Object> departmentData = (Map<String, Object>) data.get("department");
		assertThat(departmentData.keySet(), containsInAnyOrder("id", "name", "employeesCount"));
		assertThat(departmentData.get("id"), is("dep1"));
		assertThat(departmentData.get("name"), is("Sales"));
		assertThat(departmentData.get("employeesCount"), is(2));

	}
}
