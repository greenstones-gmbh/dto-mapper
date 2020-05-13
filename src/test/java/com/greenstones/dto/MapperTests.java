
package com.greenstones.dto;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.notNullValue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MapperTests {

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

		Mapper<Employee> mapper = Mapper.empty();
		Data data = mapper.map(emp1);

		assertThat(data, notNullValue());
		assertThat(data.getProps().keySet(), empty());

	}

	@Test
	void allProps() {

		Mapper<Employee> mapper = Mapper.allProps();
		Data data = mapper.map(emp1);

		assertThat(data, notNullValue());
		assertThat(data.getProps().keySet(), containsInAnyOrder("username", "firstName", "lastName", "department"));

		assertThat(data.get("username"), is("u1"));
		assertThat(data.get("firstName"), is("Adam"));
		assertThat(data.get("lastName"), is("Rees"));
		assertThat(data.get("department"), notNullValue());
		
		

	}

	@Test
	void copyProps() {

		Mapper<Employee> mapper = Mapper.<Employee>empty().copy("username", "firstName", "lastName");
		Data data = mapper.map(emp1);

		assertThat(data, notNullValue());
		assertThat(data.getProps().keySet(), containsInAnyOrder("username", "firstName", "lastName"));

		assertThat(data.get("username"), is("u1"));
		assertThat(data.get("firstName"), is("Adam"));
		assertThat(data.get("lastName"), is("Rees"));
		
	}

	
	@Test
	void excludeProps() {

		Mapper<Employee> mapper = Mapper.<Employee>allProps().exclude("department");
		Data data = mapper.map(emp1);

		assertThat(data, notNullValue());
		assertThat(data.getProps().keySet(), containsInAnyOrder("username", "firstName", "lastName"));

		assertThat(data.get("username"), is("u1"));
		assertThat(data.get("firstName"), is("Adam"));
		assertThat(data.get("lastName"), is("Rees"));
		
	}
	
	
	
	@Test
	void addProps() {

		Mapper<Employee> mapper = Mapper.<Employee>props("firstName", "lastName").add("fullName", e->e.getFirstName()+" "+e.getLastName());
		Data data = mapper.map(emp1);

		assertThat(data, notNullValue());
		assertThat(data.getProps().keySet(), containsInAnyOrder("fullName", "firstName", "lastName"));

		assertThat(data.get("fullName"), is("Adam Rees"));
		assertThat(data.get("firstName"), is("Adam"));
		assertThat(data.get("lastName"), is("Rees"));
		
	}
	
	
	@Test
	void copyNestedProps() {

		Mapper<Employee> mapper = Mapper.<Employee>props("firstName", "lastName").copyTo("department.name", "departmentName");
		Data data = mapper.map(emp1);

		assertThat(data, notNullValue());
		assertThat(data.getProps().keySet(), containsInAnyOrder("departmentName", "firstName", "lastName"));

		assertThat(data.get("departmentName"), is("Sales"));
		assertThat(data.get("firstName"), is("Adam"));
		assertThat(data.get("lastName"), is("Rees"));
		
	}
	
	
	@Test
	void copyNestedEntiry() {

		Mapper<Department> departmentMapper = Mapper.<Department>props("id", "name").add("employeesCount", d->d.getEmployees().size());
		Mapper<Employee> mapper = Mapper.<Employee>props("firstName", "lastName").copy("department", departmentMapper);
		
		Data data = mapper.map(emp1);

		assertThat(data, notNullValue());
		assertThat(data.getProps().keySet(), containsInAnyOrder("department", "firstName", "lastName"));

		
		assertThat(data.get("firstName"), is("Adam"));
		assertThat(data.get("lastName"), is("Rees"));
	
		assertThat(data.get("department"), notNullValue());
		Data departmentData = (Data)data.get("department");
		assertThat(departmentData.getProps().keySet(), containsInAnyOrder("id", "name", "employeesCount"));
		assertThat(departmentData.get("id"), is("dep1"));
		assertThat(departmentData.get("name"), is("Sales"));
		assertThat(departmentData.get("employeesCount"), is(2));
		
		
	}
}
