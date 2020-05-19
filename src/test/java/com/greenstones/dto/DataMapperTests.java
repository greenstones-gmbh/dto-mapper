package com.greenstones.dto;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import static org.hamcrest.Matchers.*;

import org.junit.jupiter.api.Test;

public class DataMapperTests {

	@Test
	void copy() {

		Data data = new Data();
		data.put("username", "u1");
		data.put("firstName", "Adam");
		data.put("lastName", "Rees");

		DataMapper<Employee> mapper = DataMapper.forType(Employee.class).props("username", "firstName", "lastName");
		Employee emp = mapper.map(data);

		assertThat(emp, notNullValue());
		assertThat(emp.getUsername(), is("u1"));
		assertThat(emp.getFirstName(), is("Adam"));
		assertThat(emp.getLastName(), is("Rees"));
		assertThat(emp.getDepartment(), nullValue());

		mapper = DataMapper.forType(Employee.class).props("firstName", "lastName");
		emp = mapper.map(data);

		assertThat(emp, notNullValue());
		assertThat(emp.getUsername(), nullValue());
		assertThat(emp.getFirstName(), is("Adam"));
		assertThat(emp.getLastName(), is("Rees"));
		assertThat(emp.getDepartment(), nullValue());

	}

	@Test
	void copyAll() {

		Data data = new Data();
		data.put("username", "u1");
		data.put("firstName", "Adam");
		data.put("lastName", "Rees");
		data.put("propNotExists", "Rees1");

		DataMapper<Employee> mapper = DataMapper.forType(Employee.class).all();
		Employee emp = mapper.map(data);

		assertThat(emp, notNullValue());
		assertThat(emp.getUsername(), is("u1"));
		assertThat(emp.getFirstName(), is("Adam"));
		assertThat(emp.getLastName(), is("Rees"));

	}

	@Test
	void exclude() {

		Data data = new Data();
		data.put("username", "u1");
		data.put("firstName", "Adam");
		data.put("lastName", "Rees");
		data.put("propNotExists", "Rees1");

		DataMapper<Employee> mapper = DataMapper.forType(Employee.class).except("username");
		Employee emp = mapper.map(data);

		assertThat(emp, notNullValue());
		assertThat(emp.getUsername(), nullValue());
		assertThat(emp.getFirstName(), is("Adam"));
		assertThat(emp.getLastName(), is("Rees"));

	}

	@Test
	void add() {

		Data data = new Data();
		data.put("username", "u1");
		data.put("fullName", "Adam Rees");

		DataMapper<Employee> mapper = DataMapper
				.forType(Employee.class)
					.all()
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

		Data data = new Data();
		data.put("username", "u1");
		data.put("fullName", "Adam Rees");

		DataMapper<Employee> mapper = DataMapper.forType(Employee.class).all().add((emp, d) -> {
			String[] names = d.get("fullName").toString().split(" ");
			emp.setFirstName(names[0]);
			emp.setLastName(names[1]);
			return emp;

		});
		Employee emp = mapper.map(data);

		assertThat(emp, notNullValue());
		assertThat(emp.getUsername(), is("u1"));
		assertThat(emp.getFirstName(), is("Adam"));
		assertThat(emp.getLastName(), is("Rees"));

	}

	@Test
	void nested() {

		Data depData = new Data();
		depData.put("id", "dep1");
		depData.put("name", "Sales");

		Data data = new Data();
		data.put("username", "u1");
		data.put("firstName", "Adam");
		data.put("lastName", "Rees");
		data.put("department", depData);

		DataMapper<Employee> mapper = DataMapper.forType(Employee.class).except("department").copy("department", d -> {
			Department dep = new Department();
			Data dd = (Data) d;
			dep.setId((String) dd.get("id"));
			dep.setName((String) dd.get("name"));

			return dep;
		});
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

		Data depData = new Data();
		depData.put("id", "dep1");
		depData.put("name", "Sales");

		Data data = new Data();
		data.put("username", "u1");
		data.put("firstName", "Adam");
		data.put("lastName", "Rees");
		data.put("department", depData);

		DataMapper<Department> depMapper = DataMapper.forType(Department.class).all();
		DataMapper<Employee> mapper = DataMapper
				.forType(Employee.class)
					.except("department")
					.copy("department", depMapper);
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
	void nestedListMapper() {

		Data depData = new Data();
		depData.put("id", "dep1");
		depData.put("name", "Sales");
		depData.put("employees", new ArrayList<Data>());
		{
			Data data = new Data();
			data.put("username", "u1");
			data.put("firstName", "Adam");
			data.put("lastName", "Rees");
			((ArrayList<Data>) depData.get("employees")).add(data);
		}
		{
			Data data = new Data();
			data.put("username", "u2");
			data.put("firstName", "Alison");
			data.put("lastName", "Jones");
			((ArrayList<Data>) depData.get("employees")).add(data);
		}

		DataMapper<Employee> empMapper = DataMapper.forType(Employee.class).all();
		DataMapper<Department> depMapper = DataMapper.forType(Department.class).props("id","name").copy("employees",empMapper);

		Department dep = depMapper.map(depData);

		assertThat(dep, notNullValue());
		assertThat(dep.getId(), is("dep1"));
		assertThat(dep.getName(), is("Sales"));

		assertThat(dep.getEmployees(), notNullValue());
		assertThat(dep.getEmployees().size(), is(2));
		
		Iterator<Employee> iterator = dep.getEmployees().iterator();
		Employee emp1=iterator.next();
		Employee emp2=iterator.next();
		
		

		assertThat(emp1.getUsername(), is("u1"));
		assertThat(emp1.getFirstName(), is("Adam"));
		assertThat(emp1.getLastName(), is("Rees"));
		
		assertThat(emp2.getUsername(), is("u2"));
		assertThat(emp2.getFirstName(), is("Alison"));
		assertThat(emp2.getLastName(), is("Jones"));
	}

}
