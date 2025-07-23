package com.example.booking_service.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class BookingDTO {
    @NotNull(message = "Flight ID cannot be null")
    public Long flightId;

    @NotEmpty(message = "Passenger list cannot be empty")
    public List<@NotNull PassengerDTO> passengers;

}
