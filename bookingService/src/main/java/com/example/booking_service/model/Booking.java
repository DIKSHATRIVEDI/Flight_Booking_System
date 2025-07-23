package com.example.booking_service.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Flight ID must not be null")
    private Long flightId;

    @NotNull(message = "User ID must not be null")
    private Long userId;

    @Min(value = 1, message = "There must be at least 1 passenger")
    private int numberOfPassengers;

    @NotBlank(message = "Booking date must not be blank")
    private String bookingDate;

    @NotBlank(message = "Status must not be blank")
    private String status;

    @Positive(message = "Total fare must be a positive value")
    private double totalFare;

    @OneToMany(mappedBy="booking", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<Passenger> passengers;
}
