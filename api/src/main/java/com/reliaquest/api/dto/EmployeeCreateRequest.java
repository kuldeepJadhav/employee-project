package com.reliaquest.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.NoArgsConstructor;



@Data
@NoArgsConstructor
public class EmployeeCreateRequest {
    
    @NotBlank(message = "Name cannot be blank")
    private String name;
    
    @NotNull(message = "Salary cannot be null")
    @Positive(message = "Salary must be greater than 0")
    private Integer salary;
    
    @NotNull(message = "Age cannot be null")
    @Min(value = 16, message = "Age must be at least 16")
    @Max(value = 75, message = "Age must be at most 75")
    private Integer age;
    
    @NotBlank(message = "Title cannot be blank")
    private String title;
}
