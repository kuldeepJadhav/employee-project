package com.reliaquest.api.rest.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reliaquest.api.dto.EmployeeCreateRequest;
import com.reliaquest.api.dto.EmployeeDTO;
import com.reliaquest.api.exception.ApiException;
import com.reliaquest.api.service.EmployeeService;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest({EmployeeController.class, com.reliaquest.api.rest.controller.advice.ControllerAdvice.class})
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeService employeeService;

    @Autowired
    private ObjectMapper objectMapper;

    private EmployeeDTO testEmployee;
    private EmployeeCreateRequest testCreateRequest;

    @BeforeEach
    void setUp() {
        testEmployee = new EmployeeDTO();
        testEmployee.setId(UUID.randomUUID());
        testEmployee.setName("John Doe");
        testEmployee.setSalary(50000);
        testEmployee.setAge(30);
        testEmployee.setTitle("Software Engineer");
        testEmployee.setEmail("john.doe@example.com");

        testCreateRequest = new EmployeeCreateRequest();
        testCreateRequest.setName("Jane Smith");
        testCreateRequest.setSalary(60000);
        testCreateRequest.setAge(25);
        testCreateRequest.setTitle("Senior Developer");
    }

    @Test
    void shouldGetEmployeeList() throws Exception {
        // Given
        List<EmployeeDTO> employees = Arrays.asList(testEmployee);
        when(employeeService.getAllEmployees()).thenReturn(employees);

        // When & Then
        mockMvc.perform(get("/v1/employee"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("John Doe"))
                .andExpect(jsonPath("$[0].salary").value(50000))
                .andExpect(jsonPath("$[0].age").value(30))
                .andExpect(jsonPath("$[0].title").value("Software Engineer"));
    }

    @Test
    void shouldReturnEmptyListIfServiceReturnsNoResult() throws Exception {
        // Given
        when(employeeService.getAllEmployees()).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/v1/employee"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void shouldGetEmployeeByEmployeeId() throws Exception {
        // Given
        String employeeId = "123";
        when(employeeService.getEmployeeById(employeeId)).thenReturn(testEmployee);

        // When & Then
        mockMvc.perform(get("/v1/employee/{id}", employeeId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.salary").value(50000))
                .andExpect(jsonPath("$.age").value(30))
                .andExpect(jsonPath("$.title").value("Software Engineer"));
    }

    @Test
    void shouldHandle404ForEmployeeNotFound() throws Exception {
        // Given
        String employeeId = "999";
        when(employeeService.getEmployeeById(employeeId))
                .thenThrow(new ApiException("Entity not found", HttpStatus.NOT_FOUND));

        // When & Then
        mockMvc.perform(get("/v1/employee/{id}", employeeId)).andExpect(status().isNotFound());
    }

    @Test
    void shouldSearchEmployeesByName() throws Exception {
        // Given
        String searchName = "John";
        List<EmployeeDTO> filteredEmployees = Arrays.asList(testEmployee);
        when(employeeService.getEmployeesByNameSearch(searchName)).thenReturn(filteredEmployees);

        // When & Then
        mockMvc.perform(get("/v1/employee/search/{searchString}", searchName))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("John Doe"));
    }

    @Test
    void shouldHandleEmptyEmployeeListInSearch() throws Exception {
        // Given
        String searchName = "NonExistent";
        when(employeeService.getEmployeesByNameSearch(searchName)).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/v1/employee/search/{searchString}", searchName))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void shouldGetHighestSalary() throws Exception {
        // Given
        Integer highestSalary = 100000;
        when(employeeService.getHighestSalaryOfEmployees()).thenReturn(highestSalary);

        // When & Then
        mockMvc.perform(get("/v1/employee/highestSalary"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").value(100000));
    }

    @Test
    void shouldGetTop10HighestPaidEmployees() throws Exception {
        // Given
        List<String> topTenNames =
                Arrays.asList("Alice", "Bob", "Charlie", "David", "Eve", "Frank", "Grace", "Henry", "Ivy", "Jack");
        when(employeeService.getTopTenHighestEarningEmployeeNames()).thenReturn(topTenNames);

        // When & Then
        mockMvc.perform(get("/v1/employee/topTenHighestEarningEmployeeNames"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isNotEmpty())
                .andExpect(jsonPath("$[0]").value("Alice"))
                .andExpect(jsonPath("$[9]").value("Jack"));
    }

    @Test
    void shouldCreateAEmployee() throws Exception {
        // Given
        when(employeeService.createEmployee(any(EmployeeCreateRequest.class))).thenReturn(testEmployee);

        // When & Then
        mockMvc.perform(post("/v1/employee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testCreateRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.salary").value(50000))
                .andExpect(jsonPath("$.age").value(30))
                .andExpect(jsonPath("$.title").value("Software Engineer"));
    }

    @Test
    void shouldHandleInvalidRequestForCreateEmployee() throws Exception {
        // Given - Invalid request with blank name
        EmployeeCreateRequest invalidRequest = new EmployeeCreateRequest();
        invalidRequest.setName(""); // Blank name
        invalidRequest.setSalary(50000);
        invalidRequest.setAge(30);
        invalidRequest.setTitle("Software Engineer");

        // When & Then
        mockMvc.perform(post("/v1/employee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.name").value("Name cannot be blank"));
    }

    @Test
    void shouldHandleInvalidSalaryInCreateEmployee() throws Exception {
        // Given - Invalid request with negative salary
        EmployeeCreateRequest invalidRequest = new EmployeeCreateRequest();
        invalidRequest.setName("John Doe");
        invalidRequest.setSalary(-1000); // Negative salary
        invalidRequest.setAge(30);
        invalidRequest.setTitle("Software Engineer");

        // When & Then
        mockMvc.perform(post("/v1/employee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.salary").value("Salary must be greater than 0"));
    }

    @Test
    void shouldHandleInvalidAgeInCreateEmployee() throws Exception {
        // Given - Invalid request with age below minimum
        EmployeeCreateRequest invalidRequest = new EmployeeCreateRequest();
        invalidRequest.setName("John Doe");
        invalidRequest.setSalary(50000);
        invalidRequest.setAge(15); // Below minimum age
        invalidRequest.setTitle("Software Engineer");

        // When & Then
        mockMvc.perform(post("/v1/employee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.age").value("Age must be at least 16"));
    }

    @Test
    void shoulHandleInvalidMaxAgeInCreateEmployee() throws Exception {
        // Given - Invalid request with age above maximum
        EmployeeCreateRequest invalidRequest = new EmployeeCreateRequest();
        invalidRequest.setName("John Doe");
        invalidRequest.setSalary(50000);
        invalidRequest.setAge(80); // Above maximum age
        invalidRequest.setTitle("Software Engineer");

        // When & Then
        mockMvc.perform(post("/v1/employee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.age").value("Age must be at most 75"));
    }

    @Test
    void shouldHandleBlankTitleInCreateEmployee() throws Exception {
        // Given - Invalid request with blank title
        EmployeeCreateRequest invalidRequest = new EmployeeCreateRequest();
        invalidRequest.setName("John Doe");
        invalidRequest.setSalary(50000);
        invalidRequest.setAge(30);
        invalidRequest.setTitle(""); // Blank title

        // When & Then
        mockMvc.perform(post("/v1/employee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.title").value("Title cannot be blank"));
    }

    @Test
    void shouldDeleteEmployeeById() throws Exception {
        // Given
        String employeeId = "123";
        String successMessage = "Employee deleted successfully";
        when(employeeService.deleteEmployeeById(employeeId)).thenReturn(successMessage);

        // When & Then
        mockMvc.perform(delete("/v1/employee/{id}", employeeId))
                .andExpect(status().isNoContent())
                .andExpect(jsonPath("$").value("Employee deleted successfully"));
    }
}
