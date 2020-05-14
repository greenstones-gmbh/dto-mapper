package com.greenstones.dto.demo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class JpaRestDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(JpaRestDemoApplication.class, args);
	}

	@Bean
	CommandLineRunner runner(DepartmentRepository departmentRepository, EmployeeRepository employeeRepository) {
		return args -> {

			Department dep = new Department();
			dep.setName("Sales");
			dep.setId("dep1");

			dep = departmentRepository.save(dep);

			Employee emp1 = new Employee();
			emp1.setUsername("u1");
			emp1.setFirstName("Adam");
			emp1.setLastName("Rees");
			emp1.setDepartment(dep);

			employeeRepository.save(emp1);

			Employee emp2 = new Employee();
			emp2.setUsername("u2");
			emp2.setFirstName("Alison");
			emp2.setLastName("Jones");
			emp2.setDepartment(dep);

			employeeRepository.save(emp2);


		};
	}
}
