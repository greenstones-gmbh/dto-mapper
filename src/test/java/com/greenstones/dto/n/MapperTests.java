
package com.greenstones.dto.n;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.greenstones.dto.Data;
import com.greenstones.dto.Department;
import com.greenstones.dto.Employee;
import com.greenstones.dto.n.PropMapping.SinglePropertySelector;

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

		Mapper<Employee, Data> mapper = Mapper.Builder.<Employee>modelToData().build();
		Data data = mapper.map(emp1);

		assertThat(data, notNullValue());
		assertThat(data.getProps().keySet(), empty());

	}

	@Test
	void allProps() {

		Mapper<Employee, Data> mapper = Mapper.Builder.<Employee>modelToData().copy(Props.all()).build();
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

		Mapper<Employee, Data> mapper = Mapper.Builder
				.<Employee>modelToData()
					.copy(Props.props("username", "firstName", "lastName"))
					.build();
		Data data = mapper.map(emp1);

		assertThat(data, notNullValue());
		assertThat(data.getProps().keySet(), containsInAnyOrder("username", "firstName", "lastName"));

		assertThat(data.get("username"), is("u1"));
		assertThat(data.get("firstName"), is("Adam"));
		assertThat(data.get("lastName"), is("Rees"));

	}

	@Test
	void excludeProps() {

		Mapper<Employee, Data> mapper = Mapper.Builder.<Employee>modelToData().copy(Props.except("department")).build();
		Data data = mapper.map(emp1);

		assertThat(data, notNullValue());
		assertThat(data.getProps().keySet(), containsInAnyOrder("username", "firstName", "lastName"));

		assertThat(data.get("username"), is("u1"));
		assertThat(data.get("firstName"), is("Adam"));
		assertThat(data.get("lastName"), is("Rees"));

	}

	@Test
	void addProps() {

		Mapper<Employee, Data> mapper = Mapper.Builder
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

		Mapper<Employee, Data> mapper = Mapper.Builder
				.<Employee>modelToData()
					.copy(Props.props("firstName", "lastName"))
					.copy(Props.<Employee, Data>prop("department.name").to("departmentName"))
					.build();
		Data data = mapper.map(emp1);

		assertThat(data, notNullValue());
		assertThat(data.getProps().keySet(), containsInAnyOrder("departmentName", "firstName", "lastName"));

		assertThat(data.get("departmentName"), is("Sales"));
		assertThat(data.get("firstName"), is("Adam"));
		assertThat(data.get("lastName"), is("Rees"));

	}

	@Test
	void copyNestedEntiry() {

		Mapper<Department, Data> departmentMapper = Mapper.Builder
				.<Department>modelToData()
					.copy(Props.props("id", "name"))
					.add("employeesCount", d -> d.getEmployees().size())
					.build();
		Mapper<Employee, Data> mapper = Mapper.Builder
				.<Employee>modelToData()
					.copy(Props.props("firstName", "lastName"))
					.copy(Props.prop("department", departmentMapper))
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
