package com.greenstones.dto;

import static com.greenstones.dto.Props.all;
import static com.greenstones.dto.Props.except;
import static com.greenstones.dto.Props.prop;
import static com.greenstones.dto.Props.props;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

public class DataToModelTests {

	@Test
	void copy() {

		Map<String, Object> data = new HashMap<>();
		data.put("username", "u1");
		data.put("firstName", "Adam");
		data.put("lastName", "Rees");

		Mapper.MapToModel<Employee> mapper = new Mapper.MapToModel<>(Employee.class)
				.copy(props("username", "firstName", "lastName"));
		Employee emp = mapper.map(data);

		assertThat(emp, notNullValue());
		assertThat(emp.getUsername(), is("u1"));
		assertThat(emp.getFirstName(), is("Adam"));
		assertThat(emp.getLastName(), is("Rees"));
		assertThat(emp.getDepartment(), nullValue());

		mapper = new Mapper.MapToModel<>(Employee.class).copy(props("firstName", "lastName"));
		emp = mapper.map(data);

		assertThat(emp, notNullValue());
		assertThat(emp.getUsername(), nullValue());
		assertThat(emp.getFirstName(), is("Adam"));
		assertThat(emp.getLastName(), is("Rees"));
		assertThat(emp.getDepartment(), nullValue());

	}

	@Test
	void copyAll() {

		Map<String, Object> data = new HashMap<>();
		data.put("username", "u1");
		data.put("firstName", "Adam");
		data.put("lastName", "Rees");
		data.put("propNotExists", "Rees1");

		Mapper.MapToModel<Employee> mapper = new Mapper.MapToModel<>(Employee.class).copy(all());
		Employee emp = mapper.map(data);

		assertThat(emp, notNullValue());
		assertThat(emp.getUsername(), is("u1"));
		assertThat(emp.getFirstName(), is("Adam"));
		assertThat(emp.getLastName(), is("Rees"));

	}

	@Test
	void exclude() {

		Map<String, Object> data = new HashMap<>();
		data.put("username", "u1");
		data.put("firstName", "Adam");
		data.put("lastName", "Rees");
		data.put("propNotExists", "Rees1");

		Mapper.MapToModel<Employee> mapper = new Mapper.MapToModel<>(Employee.class).copy(except("username"));

		Employee emp = mapper.map(data);

		assertThat(emp, notNullValue());
		assertThat(emp.getUsername(), nullValue());
		assertThat(emp.getFirstName(), is("Adam"));
		assertThat(emp.getLastName(), is("Rees"));

	}

	@Test
	void add() {

		Map<String, Object> data = new HashMap<>();
		data.put("username", "u1");
		data.put("fullName", "Adam Rees");

		Mapper.MapToModel<Employee> mapper = new Mapper.MapToModel<>(Employee.class)
				.copy(all())
					.add("firstName", d -> d.get("fullName").toString().split(" ")[0])
					.add("lastName", d -> d.get("fullName").toString().split(" ")[1]);
		Employee emp = mapper.map(data);

		assertThat(emp, notNullValue());
		assertThat(emp.getUsername(), is("u1"));
		assertThat(emp.getFirstName(), is("Adam"));
		assertThat(emp.getLastName(), is("Rees"));

	}

	@Test
	void reducer() {

		Map<String, Object> data = new HashMap<>();
		data.put("username", "u1");
		data.put("fullName", "Adam Rees");

		Mapper.MapToModel<Employee> mapper = new Mapper.MapToModel<>(Employee.class).copy(all()).with((d, emp) -> {
			String[] names = d.get("fullName").toString().split(" ");
			emp.val().setFirstName(names[0]);
			emp.val().setLastName(names[1]);

		});
		Employee emp = mapper.map(data);

		assertThat(emp, notNullValue());
		assertThat(emp.getUsername(), is("u1"));
		assertThat(emp.getFirstName(), is("Adam"));
		assertThat(emp.getLastName(), is("Rees"));

	}

	@Test
	void nested() {

		Map<String, Object> depData = new HashMap<String, Object>();
		depData.put("id", "dep1");
		depData.put("name", "Sales");

		Map<String, Object> data = new HashMap<>();
		data.put("username", "u1");
		data.put("firstName", "Adam");
		data.put("lastName", "Rees");
		data.put("department", depData);

		Mapper.MapToModel<Employee> mapper = new Mapper.MapToModel<>(Employee.class)
				.copy(except("department"))
					.copy(prop("department").with(d -> {
						Department dep = new Department();
						@SuppressWarnings("unchecked")
						Map<String, Object> dd = (Map<String, Object>) d;
						dep.setId((String) dd.get("id"));
						dep.setName((String) dd.get("name"));
						return dep;
					}));

		Employee emp = mapper.map(data);

		assertThat(emp, notNullValue());
		assertThat(emp.getUsername(), is("u1"));
		assertThat(emp.getFirstName(), is("Adam"));
		assertThat(emp.getLastName(), is("Rees"));

		assertThat(emp.getDepartment(), notNullValue());
		assertThat(emp.getDepartment().getId(), is("dep1"));
		assertThat(emp.getDepartment().getName(), is("Sales"));

	}

	@Test
	void nestedMapper() {

		Map<String, Object> depData = new HashMap<String, Object>();
		depData.put("id", "dep1");
		depData.put("name", "Sales");

		Map<String, Object> data = new HashMap<>();
		data.put("username", "u1");
		data.put("firstName", "Adam");
		data.put("lastName", "Rees");
		data.put("department", depData);

		Mapper.MapToModel<Department> depMapper = new Mapper.MapToModel<>(Department.class).copy(all());
		Mapper.MapToModel<Employee> mapper = new Mapper.MapToModel<>(Employee.class)
				.copy(except("department"))
					.copy(prop("department").with(depMapper));

		Employee emp = mapper.map(data);

		assertThat(emp, notNullValue());
		assertThat(emp.getUsername(), is("u1"));
		assertThat(emp.getFirstName(), is("Adam"));
		assertThat(emp.getLastName(), is("Rees"));

		assertThat(emp.getDepartment(), notNullValue());
		assertThat(emp.getDepartment().getId(), is("dep1"));
		assertThat(emp.getDepartment().getName(), is("Sales"));

	}

	@SuppressWarnings("unchecked")
	@Test
	void nestedListMapper() {

		Map<String, Object> depData = new HashMap<String, Object>();
		depData.put("id", "dep1");
		depData.put("name", "Sales");
		depData.put("employees", new ArrayList<Map<String, Object>>());
		{
			Map<String, Object> data = new HashMap<>();
			data.put("username", "u1");
			data.put("firstName", "Adam");
			data.put("lastName", "Rees");
			((ArrayList<Map<String, Object>>) depData.get("employees")).add(data);
		}
		{
			Map<String, Object> data = new HashMap<>();
			data.put("username", "u2");
			data.put("firstName", "Alison");
			data.put("lastName", "Jones");
			((ArrayList<Map<String, Object>>) depData.get("employees")).add(data);
		}

		Mapper.MapToModel<Employee> mapper = new Mapper.MapToModel<>(Employee.class).copy(all());
		Mapper.MapToModel<Department> depMapper = new Mapper.MapToModel<>(Department.class)
				.copy(props("id", "name"))
					.copy(prop("employees").with(mapper).collector(Collectors.toSet()));

		Department dep = depMapper.map(depData);

		assertThat(dep, notNullValue());
		assertThat(dep.getId(), is("dep1"));
		assertThat(dep.getName(), is("Sales"));

		assertThat(dep.getEmployees(), notNullValue());
		assertThat(dep.getEmployees().size(), is(2));

		Iterator<Employee> iterator = dep.getEmployees().iterator();
		Employee emp1 = iterator.next();
		Employee emp2 = iterator.next();

		assertThat(emp1.getUsername(), is("u1"));
		assertThat(emp1.getFirstName(), is("Adam"));
		assertThat(emp1.getLastName(), is("Rees"));

		assertThat(emp2.getUsername(), is("u2"));
		assertThat(emp2.getFirstName(), is("Alison"));
		assertThat(emp2.getLastName(), is("Jones"));
	}

}
