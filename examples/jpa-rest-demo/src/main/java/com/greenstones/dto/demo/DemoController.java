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

import com.greenstones.dto.Mapper;
import com.greenstones.dto.simple.SimpleMapper;

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
		Mapper<Employee, Map<String, Object>> empMapper = SimpleMapper.<Employee>toMap().except("department");
		Mapper<Department, Map<String, Object>> depMapper = SimpleMapper
				.<Department>toMap()
					.copyAll()
					.copy(prop("employees").with(empMapper))
					.copy(prop("employees")
							.to("employeeNames")
								.<Employee, CharSequence>with(e -> e.getUsername())
								.collector(Collectors.joining(", ")));

		return departmentRepository.findAll().stream().map(depMapper::map);
	}

	@PostMapping("/deps")
	public Map<String, Object> create_dep(@RequestBody Map<String, Object> data) {
		Mapper<Map<String, Object>, Department> mapToModel = SimpleMapper.mapTo(Department.class).copyAll();

		Department dep = mapToModel.map(data);
		dep.setId("1");

		Mapper<Department, Map<String, Object>> modelToMap = SimpleMapper.<Department>toMap().copyAll();

		return modelToMap.map(dep);
	}

}
