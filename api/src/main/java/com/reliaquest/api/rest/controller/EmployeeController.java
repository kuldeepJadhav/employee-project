package com.reliaquest.api.rest.controller;

import com.reliaquest.api.dto.EmployeeCreateRequest;
import com.reliaquest.api.dto.EmployeeDTO;
import com.reliaquest.api.service.EmployeeService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/employee")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class EmployeeController implements IEmployeeController<EmployeeDTO, EmployeeCreateRequest> {
    private final EmployeeService employeeService;

    @Override
    public ResponseEntity<List<EmployeeDTO>> getAllEmployees() {
        List<EmployeeDTO> allEmployees = employeeService.getAllEmployees();
        return new ResponseEntity<>(allEmployees, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<EmployeeDTO>> getEmployeesByNameSearch(String searchString) {
        return new ResponseEntity<>(employeeService.getEmployeesByNameSearch(searchString), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<EmployeeDTO> getEmployeeById(@PathVariable String id) {
        EmployeeDTO employeeById = employeeService.getEmployeeById(id);
        return new ResponseEntity<>(employeeById, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Integer> getHighestSalaryOfEmployees() {
        return new ResponseEntity<>(employeeService.getHighestSalaryOfEmployees(), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<String>> getTopTenHighestEarningEmployeeNames() {
        List<String> topTenNames = employeeService.getTopTenHighestEarningEmployeeNames();
        return new ResponseEntity<>(topTenNames, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<EmployeeDTO> createEmployee(@Valid EmployeeCreateRequest employeeInput) {
        return new ResponseEntity<>(employeeService.createEmployee(employeeInput), HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<String> deleteEmployeeById(@PathVariable String id) {
        return new ResponseEntity<>(employeeService.deleteEmployeeById(id), HttpStatus.NO_CONTENT);
    }
}
