package com.example.booking_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FlightDTO {
    public String flightNumber;
    public String airline;
    public String source;
    public String destination;
    public LocalDate departureDate;
    public String departureTime;
}
