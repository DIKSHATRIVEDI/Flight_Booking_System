package com.example.checkInService.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import com.example.checkInService.dto.BookingResponseDTO;
import com.example.checkInService.dto.PassengerDTO;
import com.example.checkInService.exception.CheckInException;
import com.example.checkInService.feign.BookingInterface;
import com.example.checkInService.model.CheckIn;
import com.example.checkInService.repository.CheckInRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CheckInServiceTest {

    @InjectMocks
    private CheckInService checkInService;

    @Mock
    private BookingInterface bookingInterface;

    @Mock
    private CheckInRepository checkInRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void checkInAllPassengersSuccess() {
        // Setup test data
        Long bookingId = 1L;

        PassengerDTO passenger1 = new PassengerDTO();
        passenger1.setPassengerId(101L);

        PassengerDTO passenger2 = new PassengerDTO();
        passenger2.setPassengerId(102L);

        BookingResponseDTO booking = new BookingResponseDTO();
        booking.status = "CONFIRMED";
        booking.passengers = new ArrayList<>();
        booking.passengers.add(passenger1);
        booking.passengers.add(passenger2);

        when(bookingInterface.getById(bookingId)).thenReturn(ResponseEntity.ok(booking));
        when(checkInRepository.findByBookingIdAndPassengerId(anyLong(), anyLong()))
                .thenReturn(Optional.empty());
        when(checkInRepository.save(any(CheckIn.class))).thenAnswer(i -> i.getArguments()[0]);

        // Run service
        assertDoesNotThrow(() -> checkInService.checkInAllPassengers(bookingId));

        // Verify
        verify(checkInRepository, times(2)).save(any(CheckIn.class));
    }

    @Test
    void checkInAllPassengersInvalidBooking() {
        Long bookingId = 1L;
        BookingResponseDTO booking = new BookingResponseDTO();
        booking.status = "CANCELLED";

        when(bookingInterface.getById(bookingId)).thenReturn(ResponseEntity.ok(booking));

        CheckInException ex = assertThrows(CheckInException.class,
                () -> checkInService.checkInAllPassengers(bookingId));

        assertEquals("Booking not confirmed or not found.", ex.getMessage());
        verify(checkInRepository, never()).save(any());
    }

    @Test
    void checkInAllPassengersAlreadyCheckedIn() {
        Long bookingId = 1L;

        PassengerDTO passenger = new PassengerDTO();
        passenger.setPassengerId(201L);

        BookingResponseDTO booking = new BookingResponseDTO();
        booking.status = "CONFIRMED";
        booking.passengers = new ArrayList<>();
        booking.passengers.add(passenger);

        when(bookingInterface.getById(bookingId)).thenReturn(ResponseEntity.ok(booking));
        when(checkInRepository.findByBookingIdAndPassengerId(bookingId, 201L))
                .thenReturn(Optional.of(new CheckIn())); // already checked in

        // Should not throw, but should not save again
        checkInService.checkInAllPassengers(bookingId);
        verify(checkInRepository, never()).save(any());
    }
}
