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
import java.util.Iterator;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.greenstones.dto.data.Data;

public class DataToModelTests {

	@Test
	void copy() {

		Data data = new Data();
		data.put("username", "u1");
		data.put("firstName", "Adam");
		data.put("lastName", "Rees");

		Mapper<Data, Employee> mapper = Data
				.dataToModel(Employee.class)
					.copy(props("username", "firstName", "lastName"))
					.build();
		Employee emp = mapper.map(data);

		assertThat(emp, notNullValue());
		assertThat(emp.getUsername(), is("u1"));
		assertThat(emp.getFirstName(), is("Adam"));
		assertThat(emp.getLastName(), is("Rees"));
		assertThat(emp.getDepartment(), nullValue());

		mapper = Data.dataToModel(Employee.class).copy(props("firstName", "lastName")).build();
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

		Mapper<Data, Employee> mapper = Data.dataToModel(Employee.class).copy(all()).build();
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

		Mapper<Data, Employee> mapper = Data.dataToModel(Employee.class).copy(except("username")).build();

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

		Mapper<Data, Employee> mapper = Data
				.dataToModel(Employee.class)
					.copy(all())
					.add("firstName", d -> d.get("fullName").toString().split(" ")[0])
					.add("lastName", d -> d.get("fullName").toString().split(" ")[1])
					.build();
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

		Mapper<Data, Employee> mapper = Data.dataToModel(Employee.class).copy(all()).addMapping((d, emp) -> {
			String[] names = d.get("fullName").toString().split(" ");
			emp.val().setFirstName(names[0]);
			emp.val().setLastName(names[1]);

		}).build();
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

		Mapper<Data, Employee> mapper = Data
				.dataToModel(Employee.class)
					.copy(except("department"))
					.copy(prop("department").with(d -> {
						System.err.println(d);
						Department dep = new Department();
						Data dd = (Data) d;
						dep.setId((String) dd.get("id"));
						dep.setName((String) dd.get("name"));
						return dep;
					}))
					.build();

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

		Mapper<Data, Department> depMapper = Data.dataToModel(Department.class).copy(all()).build();
		Mapper<Data, Employee> mapper = Data
				.dataToModel(Employee.class)
					.copy(except("department"))
					.copy(prop("department").with(depMapper))
					.build();

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

		Mapper<Data, Employee> mapper = Data.dataToModel(Employee.class).copy(all()).build();
		Mapper<Data, Department> depMapper = Data
				.dataToModel(Department.class)
					.copy(props("id", "name"))
					.copy(prop("employees").with(mapper).collector(Collectors.toSet()))
					.build();

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
