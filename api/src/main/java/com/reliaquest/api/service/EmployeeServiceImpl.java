package com.reliaquest.api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.reliaquest.api.dto.EmployeeCreateRequest;
import com.reliaquest.api.dto.api.response.GenericResponse;
import com.reliaquest.api.exception.ApiException;
import com.reliaquest.api.utils.Retry;
import com.reliaquest.api.dto.EmployeeDTO;
import com.reliaquest.api.dto.api.response.EmployeeListResponse;
import com.reliaquest.api.dto.api.response.EmployeeResponse;
import com.reliaquest.api.utils.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.reliaquest.api.utils.Constants.EMPLOYEE;
import static com.reliaquest.api.utils.Constants.EMPLOYEE_BY_ID;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class EmployeeServiceImpl implements EmployeeService {

    private final WebClient webClient;

    @Value("${server.api.url:http://localhost:8112/api/v1}")
    private String serverBaseUrl;

    private final Utils utils;

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    @Retry
    public List<EmployeeDTO> getAllEmployees() {
        String url = serverBaseUrl + EMPLOYEE;
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);
        WebClient.ResponseSpec resSpec = webClient
                .method(HttpMethod.GET)
                .uri(builder.build().toString())
                .retrieve();
        resSpec = utils.addExceptionHandling(resSpec);
        ResponseEntity<EmployeeListResponse> respRes = resSpec.toEntity(EmployeeListResponse.class)
                .block();
        if (!Objects.isNull(respRes)) {
            EmployeeListResponse body = respRes.getBody();
            return Objects.isNull(body) ? new ArrayList<>() : body.getData();
        }
        return new ArrayList<>();
    }

    @Override
    @Retry
    public EmployeeDTO getEmployeeById(String id) {
        String url = serverBaseUrl + EMPLOYEE_BY_ID.replace(":id", id);
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);
        WebClient.ResponseSpec resSpec = webClient
                .method(HttpMethod.GET)
                .uri(builder.build().toString())
                .retrieve();
        resSpec = utils.addExceptionHandling(resSpec);
        ResponseEntity<EmployeeResponse> respRes = resSpec.toEntity(EmployeeResponse.class)
                .block();
        if (!Objects.isNull(respRes)) {
            EmployeeResponse body = respRes.getBody();
            return body.getData();
        }
        throw new ApiException("Employee not found for id " + id , HttpStatus.NOT_FOUND);
    }

    @Override
    public List<EmployeeDTO> getEmployeesByNameSearch(String name) {
        return getAllEmployees()
                .stream()
                .filter(e -> e.getName().contains(name))
                .collect(Collectors.toList());
    }

    @Override
    public Integer getHighestSalaryOfEmployees() {
        return getAllEmployees()
                .stream()
                .map(e -> e.getSalary())
                .max(Integer::compareTo)
                .orElse(0);
    }

    @Override
    public List<String> getTopTenHighestEarningEmployeeNames() {
        return getAllEmployees()
                .stream()
                .sorted((e1, e2) -> Integer.compare(e2.getSalary(), e1.getSalary())) // Descending order by salary
                .limit(10) // Top 10
                .map(EmployeeDTO::getName) // Extract names
                .collect(Collectors.toList());
    }

    @Override
    public EmployeeDTO createEmployee(EmployeeCreateRequest employeeInput) {
        try {
            String url = serverBaseUrl + EMPLOYEE;
            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);
            WebClient.ResponseSpec resSpec = webClient.post()
                    .uri(builder.build().toString())
                    .bodyValue(mapper.writeValueAsString(employeeInput))
                    .retrieve();
            resSpec = utils.addExceptionHandling(resSpec);
            ResponseEntity<EmployeeResponse> respRes = resSpec.toEntity(EmployeeResponse.class)
                    .block();
            if (!Objects.isNull(respRes)) {
                EmployeeResponse body = respRes.getBody();
                return body.getData();
            }
        } catch (JsonProcessingException e) {
            throw new ApiException("Error occurred while creating employee", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        throw new ApiException("No response from server for employee creation", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    @Retry
    public String deleteEmployeeById(String id) {
        EmployeeDTO employeeById = getEmployeeById(id);
        String name = employeeById.getName();
        String url = serverBaseUrl + EMPLOYEE;
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);
        WebClient.ResponseSpec resSpec = null;
        //try {
            resSpec = webClient
                    .method(HttpMethod.DELETE)
                    .uri(builder.build().toString())
                    .bodyValue(Map.of("name", name))
                    .retrieve();
//        } catch (JsonProcessingException e) {
//            throw new ApiException("Error occurred while creating delete request for employee", HttpStatus.INTERNAL_SERVER_ERROR);
//        }
        resSpec = utils.addExceptionHandling(resSpec);
        ResponseEntity<GenericResponse> respRes = resSpec.toEntity(GenericResponse.class)
                .block();
        return respRes.getBody().getData();
    }
}
