package com.reliaquest.api.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EmployeeDTO {

    private UUID id;
    @JsonAlias("employee_name")
    private String name;
    @JsonAlias("employee_salary")
    private Integer salary;
    @JsonAlias("employee_age")
    private Integer age;
    @JsonAlias("employee_title")
    private String title;
    @JsonAlias("employee_email")
    private String email;
}
