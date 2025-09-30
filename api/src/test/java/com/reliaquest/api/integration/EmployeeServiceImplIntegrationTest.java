package com.reliaquest.api.integration;

import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reliaquest.api.ApiApplication;
import com.reliaquest.api.dto.EmployeeCreateRequest;
import com.reliaquest.api.dto.EmployeeDTO;
import com.reliaquest.api.dto.api.response.EmployeeListResponse;
import com.reliaquest.api.dto.api.response.EmployeeResponse;
import java.util.Arrays;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, classes = ApiApplication.class)
@TestPropertySource(properties = {"server.api.url=http://localhost:9097"})
class EmployeeServiceImplIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private ClientAndServer mockServer;
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockServer = ClientAndServer.startClientAndServer(9097);
    }

    @AfterEach
    void tearDown() {
        mockServer.stop();
    }

    @Test
    void shouldGetListOfEmployees() throws Exception {
        // Given
        EmployeeDTO testEmployee = createTestEmployee();
        EmployeeListResponse mockResponse = new EmployeeListResponse();
        mockResponse.setData(Arrays.asList(testEmployee));

        String responseJson = objectMapper.writeValueAsString(mockResponse);

        new MockServerClient("localhost", 9097)
                .when(request().withMethod("GET").withPath("/employee"))
                .respond(response()
                        .withStatusCode(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(responseJson));

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
    void shouldCreateEmployee() throws Exception {
        // Given
        EmployeeCreateRequest createRequest = new EmployeeCreateRequest();
        createRequest.setName("Jane Smith");
        createRequest.setSalary(60000);
        createRequest.setAge(25);
        createRequest.setTitle("Senior Developer");

        EmployeeDTO createdEmployee = createTestEmployee();
        createdEmployee.setName("Jane Smith");
        createdEmployee.setSalary(60000);
        createdEmployee.setAge(25);
        createdEmployee.setTitle("Senior Developer");

        EmployeeResponse mockResponse = new EmployeeResponse();
        mockResponse.setData(createdEmployee);

        String responseJson = objectMapper.writeValueAsString(mockResponse);
        String requestJson = objectMapper.writeValueAsString(createRequest);

        new MockServerClient("localhost", 9097)
                .when(request().withMethod("POST").withPath("/employee"))
                .respond(response()
                        .withStatusCode(201)
                        .withHeader("Content-Type", "application/json")
                        .withBody(responseJson));

        mockMvc.perform(post("/v1/employee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("Jane Smith"))
                .andExpect(jsonPath("$.salary").value(60000))
                .andExpect(jsonPath("$.age").value(25))
                .andExpect(jsonPath("$.title").value("Senior Developer"));
    }

    private EmployeeDTO createTestEmployee() {
        EmployeeDTO employee = new EmployeeDTO();
        employee.setId(UUID.randomUUID());
        employee.setName("John Doe");
        employee.setSalary(50000);
        employee.setAge(30);
        employee.setTitle("Software Engineer");
        employee.setEmail("john.doe@example.com");
        return employee;
    }
}
