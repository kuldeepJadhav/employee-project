package com.reliaquest.api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.reliaquest.api.dto.EmployeeCreateRequest;
import com.reliaquest.api.dto.EmployeeDTO;
import com.reliaquest.api.dto.api.response.EmployeeListResponse;
import com.reliaquest.api.dto.api.response.EmployeeResponse;
import com.reliaquest.api.dto.api.response.GenericResponse;
import com.reliaquest.api.exception.ApiException;
import com.reliaquest.api.utils.Utils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@TestPropertySource(properties = {
    "server.api.url=http://localhost:8112/api/v1"
})
class EmployeeServiceImplTest {

    @Mock
    private WebClient webClient;


    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestBodySpec requestBodySpec;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @Mock
    private Utils utils;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    private final String serverBaseUrl = "http://localhost:8112/api/v1";
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
    void shouldGetAllEmployees() {
        // Given
        List<EmployeeDTO> expectedEmployees = Arrays.asList(testEmployee);
        EmployeeListResponse response = new EmployeeListResponse();
        response.setData(expectedEmployees);
        ResponseEntity<EmployeeListResponse> responseEntity = new ResponseEntity<>(response, HttpStatus.OK);

        when(webClient.method(any())).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(utils.addExceptionHandling(responseSpec)).thenReturn(responseSpec);
        when(responseSpec.toEntity(EmployeeListResponse.class)).thenReturn(Mono.just(responseEntity));

        // When
        List<EmployeeDTO> result = employeeService.getAllEmployees();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("John Doe", result.get(0).getName());
        assertEquals(50000, result.get(0).getSalary());
    }


    @Test
    void shouldGetEmployeeById() {
        // Given
        String employeeId = "123";
        EmployeeResponse response = new EmployeeResponse();
        response.setData(testEmployee);
        ResponseEntity<EmployeeResponse> responseEntity = new ResponseEntity<>(response, HttpStatus.OK);

        when(webClient.method(any())).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(utils.addExceptionHandling(responseSpec)).thenReturn(responseSpec);
        when(responseSpec.toEntity(EmployeeResponse.class)).thenReturn(Mono.just(responseEntity));

        // When
        EmployeeDTO result = employeeService.getEmployeeById(employeeId);

        // Then
        assertNotNull(result);
        assertEquals("John Doe", result.getName());
        assertEquals(50000, result.getSalary());
    }

    @Test
    void shouldHandleNoResponseForEmployeeById() {
        // Given
        String employeeId = "123";
        ApiException apiException = new ApiException("Employee not found", HttpStatus.NOT_FOUND);
        
        when(webClient.method(any())).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(utils.addExceptionHandling(responseSpec)).thenReturn(responseSpec);
        when(responseSpec.toEntity(EmployeeResponse.class)).thenReturn(Mono.error(apiException));

        // When & Then
        ApiException exception = assertThrows(ApiException.class, () -> 
            employeeService.getEmployeeById(employeeId));
        
        assertEquals("Employee not found", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    }

    @Test
    void shouldSearchOnEmployeeName() {
        // Given
        EmployeeDTO employee1 = new EmployeeDTO();
        employee1.setName("John Doe");
        employee1.setSalary(50000);

        EmployeeDTO employee2 = new EmployeeDTO();
        employee2.setName("Jane Smith");
        employee2.setSalary(60000);

        EmployeeDTO employee3 = new EmployeeDTO();
        employee3.setName("Johnny Walker");
        employee3.setSalary(70000);

        List<EmployeeDTO> allEmployees = Arrays.asList(employee1, employee2, employee3);
        EmployeeListResponse response = new EmployeeListResponse();
        response.setData(allEmployees);
        ResponseEntity<EmployeeListResponse> responseEntity = new ResponseEntity<>(response, HttpStatus.OK);

        when(webClient.method(any())).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(utils.addExceptionHandling(responseSpec)).thenReturn(responseSpec);
        when(responseSpec.toEntity(EmployeeListResponse.class)).thenReturn(Mono.just(responseEntity));

        // When
        List<EmployeeDTO> result = employeeService.getEmployeesByNameSearch("John");

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(emp -> emp.getName().contains("John")));
    }

    @Test
    void shouldHandleNoMatchForNameSearch() {
        // Given
        List<EmployeeDTO> allEmployees = Arrays.asList(testEmployee);
        EmployeeListResponse response = new EmployeeListResponse();
        response.setData(allEmployees);
        ResponseEntity<EmployeeListResponse> responseEntity = new ResponseEntity<>(response, HttpStatus.OK);

        when(webClient.method(any())).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(utils.addExceptionHandling(responseSpec)).thenReturn(responseSpec);
        when(responseSpec.toEntity(EmployeeListResponse.class)).thenReturn(Mono.just(responseEntity));

        // When
        List<EmployeeDTO> result = employeeService.getEmployeesByNameSearch("NonExistent");

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldGetHighestSalary() {
        // Given
        EmployeeDTO employee1 = new EmployeeDTO();
        employee1.setSalary(50000);

        EmployeeDTO employee2 = new EmployeeDTO();
        employee2.setSalary(80000);

        EmployeeDTO employee3 = new EmployeeDTO();
        employee3.setSalary(60000);

        List<EmployeeDTO> allEmployees = Arrays.asList(employee1, employee2, employee3);
        EmployeeListResponse response = new EmployeeListResponse();
        response.setData(allEmployees);
        ResponseEntity<EmployeeListResponse> responseEntity = new ResponseEntity<>(response, HttpStatus.OK);

        when(webClient.method(any())).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(utils.addExceptionHandling(responseSpec)).thenReturn(responseSpec);
        when(responseSpec.toEntity(EmployeeListResponse.class)).thenReturn(Mono.just(responseEntity));

        // When
        Integer result = employeeService.getHighestSalaryOfEmployees();

        // Then
        assertNotNull(result);
        assertEquals(80000, result);
    }

    @Test
    void shouldHandleEmptyListResponseForHighestSalary() {
        // Given
        List<EmployeeDTO> emptyList = Collections.emptyList();
        EmployeeListResponse response = new EmployeeListResponse();
        response.setData(emptyList);
        ResponseEntity<EmployeeListResponse> responseEntity = new ResponseEntity<>(response, HttpStatus.OK);

        when(webClient.method(any())).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(utils.addExceptionHandling(responseSpec)).thenReturn(responseSpec);
        when(responseSpec.toEntity(EmployeeListResponse.class)).thenReturn(Mono.just(responseEntity));

        // When
        Integer result = employeeService.getHighestSalaryOfEmployees();

        // Then
        assertNotNull(result);
        assertEquals(0, result);
    }

    @Test
    void shouldGetTop10HighestPaidEmployee() {
        // Given
        List<EmployeeDTO> employees = Arrays.asList(
                createEmployee("Alice", 100000),
                createEmployee("Bob", 90000),
                createEmployee("Charlie", 80000),
                createEmployee("David", 70000),
                createEmployee("Eve", 60000),
                createEmployee("Frank", 50000),
                createEmployee("Grace", 40000),
                createEmployee("Henry", 30000),
                createEmployee("Ivy", 20000),
                createEmployee("Jack", 10000),
                createEmployee("Kate", 5000) // This should be excluded
        );

        EmployeeListResponse response = new EmployeeListResponse();
        response.setData(employees);
        ResponseEntity<EmployeeListResponse> responseEntity = new ResponseEntity<>(response, HttpStatus.OK);

        when(webClient.method(any())).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(utils.addExceptionHandling(responseSpec)).thenReturn(responseSpec);
        when(responseSpec.toEntity(EmployeeListResponse.class)).thenReturn(Mono.just(responseEntity));

        // When
        List<String> result = employeeService.getTopTenHighestEarningEmployeeNames();

        // Then
        assertNotNull(result);
        assertEquals(10, result.size());
        assertEquals("Alice", result.get(0)); // Highest salary
        assertEquals("Bob", result.get(1));
        assertEquals("Jack", result.get(9)); // 10th highest
        assertFalse(result.contains("Kate")); // Should be excluded
    }

    @Test
    void shouldCreateEmployee() throws JsonProcessingException {
        // Given
        EmployeeResponse response = new EmployeeResponse();
        response.setData(testEmployee);
        ResponseEntity<EmployeeResponse> responseEntity = new ResponseEntity<>(response, HttpStatus.CREATED);

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(anyString())).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(utils.addExceptionHandling(responseSpec)).thenReturn(responseSpec);
        when(responseSpec.toEntity(EmployeeResponse.class)).thenReturn(Mono.just(responseEntity));

        // When
        EmployeeDTO result = employeeService.createEmployee(testCreateRequest);

        // Then
        assertNotNull(result);
        assertEquals("John Doe", result.getName());
        assertEquals(50000, result.getSalary());
    }

    @Test
    void deleteEmployee() {
        // Given
        String employeeId = "123";
        EmployeeDTO employeeToDelete = createEmployee("John Doe", 50000);
        
        // Mock GET employee by ID response
        EmployeeResponse getEmployeeResponse = new EmployeeResponse();
        getEmployeeResponse.setData(employeeToDelete);
        ResponseEntity<EmployeeResponse> getResponseEntity = new ResponseEntity<>(getEmployeeResponse, HttpStatus.OK);
        
        // Mock DELETE employee response
        GenericResponse deleteResponse = new GenericResponse();
        deleteResponse.setData("Employee John Doe deleted successfully");
        ResponseEntity<GenericResponse> deleteResponseEntity = new ResponseEntity<>(deleteResponse, HttpStatus.OK);

        // Mock GET employee by ID
        when(webClient.method(any())).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(utils.addExceptionHandling(responseSpec)).thenReturn(responseSpec);
        when(responseSpec.toEntity(EmployeeResponse.class)).thenReturn(Mono.just(getResponseEntity));

        // Mock DELETE employee
        when(webClient.method(HttpMethod.DELETE)).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(utils.addExceptionHandling(responseSpec)).thenReturn(responseSpec);
        when(responseSpec.toEntity(GenericResponse.class)).thenReturn(Mono.just(deleteResponseEntity));

        // When
        String result = employeeService.deleteEmployeeById(employeeId);

        // Then
        assertNotNull(result);
        assertEquals("Employee John Doe deleted successfully", result);
    }

    private EmployeeDTO createEmployee(String name, Integer salary) {
        EmployeeDTO employee = new EmployeeDTO();
        employee.setName(name);
        employee.setSalary(salary);
        return employee;
    }
}
