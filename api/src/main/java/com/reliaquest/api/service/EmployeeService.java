package com.reliaquest.api.service;

import com.reliaquest.api.dto.EmployeeCreateRequest;
import com.reliaquest.api.dto.EmployeeDTO;
import java.util.List;

public interface EmployeeService {
    List<EmployeeDTO> getAllEmployees();

    EmployeeDTO getEmployeeById(String id);

    List<EmployeeDTO> getEmployeesByNameSearch(String name);

    Integer getHighestSalaryOfEmployees();

    List<String> getTopTenHighestEarningEmployeeNames();

    EmployeeDTO createEmployee(EmployeeCreateRequest employeeInput);

    String deleteEmployeeById(String id);
}
