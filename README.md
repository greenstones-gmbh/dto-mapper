# DTO Mapper

![Java CI with Maven](https://github.com/greenstones-gmbh/dto-mapper/workflows/Java%20CI%20with%20Maven/badge.svg)
![Maven Package](https://github.com/greenstones-gmbh/dto-mapper/workflows/Maven%20Package/badge.svg)

DTO Mapper is an intelligent way to expose domain model objects via REST API without DTOs. You can include, exclude or transform attributes using a fluent-style API.  

## Features

- Map complex domain entities
- Include, exclude, transform or add attributes
- Map nested objects
- Copy nested properties to top-level properties, and vice versa
- Customize output based on request parameters 


## Examples
Include, exclude, transform or add attributes

```java
Employee emp1 = new Employee();
emp1.setUsername("u1");
emp1.setFirstName("Adam");
emp1.setLastName("Rees");
emp1.setDepartment(dep);

// Mapper for all properties except 'department' 
Mapper<Employee> mapper = Mapper.<Employee>allProps().exclude("department");

// Map an Employee to generic DTO
Data data = mapper.map(emp1);

// Print as JSON 
print(data);

/* Output:
{
  "firstName" : "Adam",
  "lastName" : "Rees",
  "username" : "u1"
}
*/



// copy only 'username' and add 'fullName' 
mapper = Mapper.<Employee>props("username").add("fullName", e -> e.getFirstName() + " " + e.getLastName());
print(mapper.map(emp1));

/* Output:
{
  "fullName" : "Adam Rees",
  "username" : "u1"
}
*/


```



Map nested objects

```java

Department dep = new Department();
dep.setId("dep1");
dep.setName("Sales");
dep.setEmployees(new ArrayList<Employee>());

Employee emp1 = new Employee();
emp1.setUsername("u1");
emp1.setFirstName("Adam");
emp1.setLastName("Rees");
emp1.setDepartment(dep);

// circular dependency
dep.getEmployees().add(emp1);

Mapper<Department> departmentMapper = Mapper.props("id", "name");
Mapper<Employee> employeeMapper = Mapper.<Employee>allProps().copy("department",departmentMapper);

Data data = employeeMapper.map(emp1);
print(data);


/* Output:
{
  "firstName" : "Adam",
  "lastName" : "Rees",
  "department" : {
    "name" : "Sales",
    "id" : "dep1"
  },
  "username" : "u1"
}
*/

```


Copy nested properties to top-level properties

```java

Department dep = new Department();
dep.setId("dep1");
dep.setName("Sales");
dep.setEmployees(new ArrayList<Employee>());

Employee emp1 = new Employee();
emp1.setUsername("u1");
emp1.setFirstName("Adam");
emp1.setLastName("Rees");
emp1.setDepartment(dep);

// circular dependency
dep.getEmployees().add(emp1);

Mapper<Employee> employeeMapper = Mapper.<Employee>props("username","firstName","lastName")
	.copyTo("department.name","departmentName");

Data data = employeeMapper.map(emp1);
print(data);


/* Output:
{
  "departmentName" : "Sales",
  "firstName" : "Adam",
  "lastName" : "Rees",
  "username" : "u1"
}
*/

```


JPA Entities

```java

@Entity
@Getter
@Setter
public class Employee {

	@Id
	String username;
	String firstName;
	String lastName;

	@ManyToOne
	Department department;

}


@Entity
@Getter
@Setter
public class Department {

	@Id
	String id;
	String name;

	@OneToMany(mappedBy = "department")
	Collection<Employee> employees = new ArrayList<Employee>();

}


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

```

Output `/deps` :

```java

java.lang.StackOverflowError: null
	at java.lang.ClassLoader.defineClass1(Native Method) ~[na:1.8.0_232]
	at java.lang.ClassLoader.defineClass(ClassLoader.java:756) ~[na:1.8.0_232]
	...
	at java.lang.ClassLoader.loadClass(ClassLoader.java:351) ~[na:1.8.0_232]
	at com.fasterxml.jackson.databind.ser.std.BeanSerializerBase.serializeFields(BeanSerializerBase.java:740) ~[jackson-databind-2.10.4.jar:2.10.4]
	at com.fasterxml.jackson.databind.ser.BeanSerializer.serialize(BeanSerializer.java:166) ~[jackson-databind-2.10.4.jar:2.10.4]


```


Output `/deps_with_mapper` :

```json

[
  {
    "name": "Sales",
    "id": "dep1",
    "employees": [
      {
        "firstName": "Adam",
        "lastName": "Rees",
        "username": "u1"
      },
      {
        "firstName": "Alison",
        "lastName": "Jones",
        "username": "u2"
      }
    ]
  }
]

```


## Setup

```xml
<dependency>
	<groupId>com.greenstones.dto</groupId>
	<artifactId>dto-mapper</artifactId>
	<version>0.1</version>
</dependency>
```
