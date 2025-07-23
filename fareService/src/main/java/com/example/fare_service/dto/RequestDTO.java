package com.example.fare_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestDTO {
    private Long amount;
    private Long quantity;
    private String name;
    private String currency;
}