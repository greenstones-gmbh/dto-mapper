package com.greenstones.dto.demo;

import java.util.List;
import java.util.stream.Stream;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.greenstones.dto.Data;
import com.greenstones.dto.Mapper;

@RestController
@Transactional
public class DemoController {

	@Autowired
	DepartmentRepository departmentRepository;

	@GetMapping("/deps")
	public List<Department> departments_throws_errors() {
		return departmentRepository.findAll();
	}

	@GetMapping("/deps_with_mapper")
	public Stream<Data> departments() {
		Mapper<Employee> empMapper = Mapper.<Employee>allProps().exclude("department");
		Mapper<Department> depMapper = Mapper.<Department>allProps().copy("employees", empMapper);

		return depMapper.map(departmentRepository.findAll());
	}

}
