package com.example.checkInService.dto;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class PassengerDTO {
    private Long passengerId;
    private String firstName;
    private String lastName;
    private String email;
    private String gender;
    private Long aadharNumber;
    private Long flightId;



}
