
package com.greenstones.dto;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.greenstones.dto.Mapper;
import com.greenstones.dto.Props;
import com.greenstones.dto.data.Data;

public class ModelToDataTests {

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

		Mapper<Employee, Data> mapper = Data.<Employee>modelToData().build();
		Data data = mapper.map(emp1);

		assertThat(data, notNullValue());
		assertThat(data.getProps().keySet(), empty());

	}

	@Test
	void allProps() {

		Mapper<Employee, Data> mapper = Data.<Employee>modelToData().copy(Props.all()).build();
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

		Mapper<Employee, Data> mapper = Data
				.<Employee>modelToData()
					.copy(Props.props("username", "firstName", "lastName"))
					.copy(Props.prop("username").<String, String>with(e -> e.toUpperCase()).to("user"))
					.build();
		Data data = mapper.map(emp1);

		assertThat(data, notNullValue());
		assertThat(data.getProps().keySet(), containsInAnyOrder("username", "firstName", "lastName", "user"));

		assertThat(data.get("username"), is("u1"));
		assertThat(data.get("firstName"), is("Adam"));
		assertThat(data.get("lastName"), is("Rees"));
		assertThat(data.get("user"), is("U1"));

	}

	@Test
	void excludeProps() {

		Mapper<Employee, Data> mapper = Data.<Employee>modelToData().copy(Props.except("department")).build();
		Data data = mapper.map(emp1);

		assertThat(data, notNullValue());
		assertThat(data.getProps().keySet(), containsInAnyOrder("username", "firstName", "lastName"));

		assertThat(data.get("username"), is("u1"));
		assertThat(data.get("firstName"), is("Adam"));
		assertThat(data.get("lastName"), is("Rees"));

	}

	@Test
	void addProps() {

		Mapper<Employee, Data> mapper = Data
				.<Employee>modelToData()
					.copy(Props.props("firstName", "lastName"))
					.add("fullName", e -> e.getFirstName() + " " + e.getLastName())
					.build();
		Data data = mapper.map(emp1);

		assertThat(data, notNullValue());
		assertThat(data.getProps().keySet(), containsInAnyOrder("fullName", "firstName", "lastName"));

		assertThat(data.get("fullName"), is("Adam Rees"));
		assertThat(data.get("firstName"), is("Adam"));
		assertThat(data.get("lastName"), is("Rees"));

	}

	@Test
	void copyNestedProps() {

		Mapper<Employee, Data> mapper = Data
				.<Employee>modelToData()
					.copy(Props.props("firstName", "lastName"))
					.copy(Props.prop("department.name").to("departmentName"))
					.build();
		Data data = mapper.map(emp1);

		assertThat(data, notNullValue());
		assertThat(data.getProps().keySet(), containsInAnyOrder("departmentName", "firstName", "lastName"));

		assertThat(data.get("departmentName"), is("Sales"));
		assertThat(data.get("firstName"), is("Adam"));
		assertThat(data.get("lastName"), is("Rees"));

	}

	@Test
	void joinNestedList() {

		Mapper<Department, Data> departmentMapper = Data
				.<Department>modelToData()
					.copy(Props.except("employees"))
					.copy(Props
							.prop("employees")
								.to("employeeNames")
								.<Employee, CharSequence>with(a -> a.getUsername())
								.collector(Collectors.joining(", ")))
					.build();

		Data data = departmentMapper.map(dep);

		assertThat(data, notNullValue());

		assertThat(data.getProps().keySet(), containsInAnyOrder("id", "name", "employeeNames"));
		assertThat(data.get("id"), is("dep1"));
		assertThat(data.get("name"), is("Sales"));
		assertThat(data.get("employeeNames"), is("u1, u2"));

	}

	@Test
	void copyNestedEntiry() {

		Mapper<Department, Data> departmentMapper = Data
				.<Department>modelToData()
					.copy(Props.props("id", "name"))
					.add("employeesCount", d -> d.getEmployees().size())
					.build();

		Mapper<Employee, Data> mapper = Data
				.<Employee>modelToData()
					.copy(Props.props("firstName", "lastName"))
					.copy(Props.prop("department").with(departmentMapper))
					.build();

		Data data = mapper.map(emp1);

		assertThat(data, notNullValue());
		assertThat(data.getProps().keySet(), containsInAnyOrder("department", "firstName", "lastName"));

		assertThat(data.get("firstName"), is("Adam"));
		assertThat(data.get("lastName"), is("Rees"));

		assertThat(data.get("department"), notNullValue());
		Data departmentData = (Data) data.get("department");
		assertThat(departmentData.getProps().keySet(), containsInAnyOrder("id", "name", "employeesCount"));
		assertThat(departmentData.get("id"), is("dep1"));
		assertThat(departmentData.get("name"), is("Sales"));
		assertThat(departmentData.get("employeesCount"), is(2));

	}
}
