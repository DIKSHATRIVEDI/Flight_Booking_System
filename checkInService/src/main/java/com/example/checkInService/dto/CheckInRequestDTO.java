package com.example.checkInService.dto;

import lombok.Data;

@Data
public class CheckInRequestDTO {
    private Long bookingId;
    private Long passengerId;
}
