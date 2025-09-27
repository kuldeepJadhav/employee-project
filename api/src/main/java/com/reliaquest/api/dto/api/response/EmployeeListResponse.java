package com.reliaquest.api.dto.api.response;

import com.reliaquest.api.dto.EmployeeDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
public class EmployeeListResponse extends Response<List<EmployeeDTO>>{
}
