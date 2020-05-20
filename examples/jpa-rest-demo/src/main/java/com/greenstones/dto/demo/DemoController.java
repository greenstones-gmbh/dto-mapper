package com.greenstones.dto.demo;

import static com.greenstones.dto.Props.prop;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.greenstones.dto.mappers.MapToModelMapper;
import com.greenstones.dto.mappers.Mapper;
import com.greenstones.dto.mappers.ModelToMapMapper;

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
	public Stream<Map<String, Object>> departments() {

		ModelToMapMapper<Department> departmentMapper = new ModelToMapMapper<Department>()
				.with("{id,name, employees{ username, firstName, lastName} }")
					.copy(prop("employees").to("employeeCount").collector(Collectors.counting()));

		return departmentRepository.findAll().stream().map(departmentMapper::map);
	}

	@PostMapping("/deps")
	public Map<String, Object> create_department(@RequestBody Map<String, Object> data) {

		MapToModelMapper<Department> mapToModel = new MapToModelMapper<Department>(Department.class).copyAll();

		Department department = mapToModel.map(data);
		department.setId("1");

		// department=departmentRepository.save(department);

		return Mapper.from("{id,name, employees{ username, firstName, lastName} }").map(department);
	}

}
