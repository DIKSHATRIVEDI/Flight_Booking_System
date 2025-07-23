package com.example.booking_service.model;

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
@Entity
public class Passenger {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long passengerId;

    @NotBlank(message = "First name must not be blank")
    @Pattern(regexp = "^[a-zA-Z]+$", message = "Only alphabets Allowed")
    private String firstName;

    @NotBlank(message = "Last name must not be blank")
    @Pattern(regexp = "^[a-zA-Z]+$", message = "Only alphabets Allowed")
    private String lastName;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email must not be blank")
    private String email;

    @NotBlank(message = "Gender must not be blank")
    @Pattern(regexp = "^[a-zA-Z]+$", message = "Only alphabets Allowed")
    private String gender;

    @NotNull(message = "Aadhar number must not be null")
    @Digits(integer = 12, fraction = 0, message = "Aadhar number must be numeric and 12 digits")
    private Long aadharNumber;

    @NotNull(message = "Flight ID must not be null")
    private Long flightId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id")
    @JsonIgnore
    @JsonBackReference
    private Booking booking;

}
