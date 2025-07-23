package com.example.booking_service.dto;

import com.example.booking_service.model.Passenger;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingResponseDTO {
    public Long id;
    public Long flightId;
    public Long userId;
    public FlightDTO flight;
    public int numberOfPassengers;
    public LocalDateTime bookingDate;
    public String status;
    public double totalFare;
    public List<Passenger> passengers;
    
}
