package com.greenstones.dto.demo;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class JpaRestDemoApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void contextLoads() throws Exception {
		this.mockMvc
				.perform(get("/deps_with_mapper"))
					.andDo(print())
					.andExpect(status().isOk())
					.andExpect(content()
							.json("[{\"name\":\"Sales\",\"employeeNames\":\"u1, u2\",\"id\":\"dep1\",\"employees\":[{\"firstName\":\"Adam\",\"lastName\":\"Rees\",\"username\":\"u1\"},{\"firstName\":\"Alison\",\"lastName\":\"Jones\",\"username\":\"u2\"}]}]"));
	}

	@Test
	void put() throws Exception {
		this.mockMvc
				.perform(post("/deps").contentType(MediaType.APPLICATION_JSON).content("{  \"name\": \"Sales\" }"))
					.andDo(print())
					.andExpect(status().isOk())
					.andExpect(content().json("{  \"name\": \"Sales\", \"id\": \"1\" }"));
	}
}
