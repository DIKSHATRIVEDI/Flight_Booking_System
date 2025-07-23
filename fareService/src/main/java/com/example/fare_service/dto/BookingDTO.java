package com.example.fare_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class BookingDTO {

    public Long id;
    public Long flightId;
    public Long userId;
    public int numberOfPassenegers;
    public String bookingDate;
    public String status;
    public double totalFare;

}
