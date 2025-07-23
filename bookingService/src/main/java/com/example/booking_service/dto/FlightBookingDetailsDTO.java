package com.example.booking_service.dto;

import lombok.NoArgsConstructor;

import java.util.List;
@NoArgsConstructor
public class FlightBookingDetailsDTO {

    //public FlightResponseDTO flight;
    public List<PassengerDTO> passengers;

    public FlightBookingDetailsDTO(List<PassengerDTO> passengers) {
        //this.flight = flight;
        this.passengers = passengers;
    }
}
