# DTO Mapper

![Java CI with Maven](https://github.com/greenstones-gmbh/dto-mapper/workflows/Java%20CI%20with%20Maven/badge.svg)
![Maven Package](https://github.com/greenstones-gmbh/dto-mapper/workflows/Maven%20Package/badge.svg)

DTO Mapper is an intelligent way to expose domain model objects via REST API without DTOs. You can include, exclude or transform attributes using a fluent-style API.  

```java
Department dep = new Department();
Employee emp1 = new Employee();
..
emp1.setDepartment(dep);

Mapper mapper = Mapper.from("{username,firstName,lastName,department{id,name}}");

Map<String, Object> data = mapper.map(emp1);
print(data);

/* Output:
{
  "username" : "u1",
  "firstName" : "Adam",
  "lastName" : "Rees",
  "department" : {
    "name" : "Sales",
    "id" : "dep1"
  }
}
*/

## Features

- Map complex domain entities
- Include, exclude, transform or add attributes
- Map nested objects
- Copy nested properties to top-level properties, and vice versa
- Customize output based on request parameters 


## Examples
Include, exclude, transform or add attributes

```java
Department dep = new Department();
dep.setId("dep1");
dep.setName("Sales");

Employee emp1 = new Employee();
emp1.setUsername("u1");
emp1.setFirstName("Adam");
emp1.setLastName("Rees");
emp1.setDepartment(dep);

// create a generic mapper
Mapper mapper = Mapper.from("{username,firstName,lastName,department{id,name}}");

// map an Employee to a map
Map<String, Object> data = mapper.map(emp1);
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

// create a generic mapper for all properties except 'department'
mapper = new Mapper().except("department");
data = mapper.map(emp1);
print(data);

/* Output:
{
  "firstName" : "Adam",
  "lastName" : "Rees",
  "username" : "u1"
}
*/


// create a typed mapper and copy 'username' and add 'fullName'
ModelToMapMapper<Employee> entityMapper = new ModelToMapMapper<Employee>()
	.copy("username")
	.add("fullName", e -> e.getFirstName() + " " + e.getLastName());
print(entityMapper.map(emp1));

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

Mapper departmentMapper = Mapper.from("{id,name}");

ModelToMapMapper<Employee> employeeMapper = new ModelToMapMapper<Employee>()
	.copyAll()
	.copy(Props.prop("department").with(departmentMapper));

Map<String, Object> data = employeeMapper.map(emp1);
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

ModelToMapMapper<Employee> employeeMapper = new ModelToMapMapper<Employee>()
	.copy("username", "firstName", "lastName")
	.copy(Props.prop("department.name").to("departmentName"));

Map<String, Object> data = employeeMapper.map(emp1);
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
		department=departmentRepository.save(department);

		return Mapper.from("{id,name, employees{ username, firstName, lastName} }").map(department);
	}
0.5-SNAPSHOT
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
    "id": "dep1",
    "name": "Sales",
    "employeeCount": 2,
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
	<version>0.5-SNAPSHOT</version>
</dependency>
```
