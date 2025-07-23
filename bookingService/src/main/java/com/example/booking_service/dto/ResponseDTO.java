package com.example.booking_service.dto;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class ResponseDTO {
    public String paymentUrl;
    public String sessionId;
    public String status;

}
