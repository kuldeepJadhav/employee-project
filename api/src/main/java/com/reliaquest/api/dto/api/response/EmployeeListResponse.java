package com.reliaquest.api.dto.api.response;

import com.reliaquest.api.dto.EmployeeDTO;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class EmployeeListResponse extends Response<List<EmployeeDTO>> {}
