package com.example.checkInService.dto;

import java.time.LocalDateTime;
import java.util.List;

public class BookingResponseDTO {
    public Long id;
    public Long flightId;
    public Long userId;
    public int numberOfPassengers;
    public LocalDateTime bookingDate;
    public String status;
    public double totalFare;
    public List<PassengerDTO> passengers;
}
