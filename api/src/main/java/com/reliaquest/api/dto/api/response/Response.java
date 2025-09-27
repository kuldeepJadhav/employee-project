package com.reliaquest.api.dto.api.response;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class Response<R> {
    public String status;
    public R data;
}
