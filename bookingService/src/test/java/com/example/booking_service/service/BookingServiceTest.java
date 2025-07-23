package com.example.booking_service.service;

import com.example.booking_service.dto.*;
import com.example.booking_service.exception.BookingException;
import com.example.booking_service.feign.FareInterface;
import com.example.booking_service.feign.FlightInterface;
import com.example.booking_service.feign.ProfileInterface;
import com.example.booking_service.model.Booking;
import com.example.booking_service.model.Passenger;
import com.example.booking_service.repository.BookingRepository;
import com.example.booking_service.repository.PassengerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.ResponseEntity;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BookingServiceTest {

    @InjectMocks
    private BookingService bookingService;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private PassengerRepository passengerRepository;

    @Mock
    private FlightInterface flightInterface;

    @Mock
    private FareInterface fareInterface;

    @Mock
    private ProfileInterface profileInterface;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private BookingDTO mockBookingDTO() {
        PassengerDTO passenger = new PassengerDTO();
        passenger.firstName = "John";
        passenger.lastName = "Doe";
        passenger.email = "john@example.com";
        passenger.gender = "Male";
        passenger.aadharNumber = 123456789076L;

        BookingDTO bookingDTO = new BookingDTO();
        bookingDTO.flightId = 1L;
        bookingDTO.passengers = List.of(passenger);
        return bookingDTO;
    }

    private FlightResponseDTO mockFlightResponseDTO() {
        FlightResponseDTO flight = new FlightResponseDTO();
        flight.id = 1L;
        flight.availableSeats = 10;
        flight.fare = 500.0;
        return flight;
    }

    @Test
    void createBookingSuccess() {
        BookingDTO bookingDTO = mockBookingDTO();
        when(flightInterface.getFlightById(1L)).thenReturn(mockFlightResponseDTO());
        when(bookingRepository.findByFlightIdAndAadharNumber(anyLong(), anyLong())).thenReturn(Optional.empty());

        Booking savedBooking = new Booking();
        savedBooking.setId(1L);
        when(bookingRepository.save(any())).thenReturn(savedBooking);

        Optional<Booking> result = bookingService.createBooking(bookingDTO, 101L);
        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
    }

    @Test
    void createBookingFlightNotFound() {
        BookingDTO bookingDTO = mockBookingDTO();
        when(flightInterface.getFlightById(1L)).thenThrow(new RuntimeException("Service Down"));

        Exception exception = assertThrows(Exception.class, () -> bookingService.createBooking(bookingDTO, 101L));
        assertTrue(exception.getMessage().contains("Failed to fetch flight"));
    }

    @Test
    void confirmBookingSuccess() {
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setStatus("PENDING");
        booking.setFlightId(1L);
        Passenger passenger = new Passenger();
        booking.setPassengers(List.of(passenger));

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any())).thenReturn(booking);

        Optional<Booking> result = bookingService.confirmBooking(1L);
        assertTrue(result.isPresent());
        assertEquals("CONFIRMED", result.get().getStatus());
    }

    @Test
    void cancelBookingSuccess() {
        when(bookingRepository.existsById(1L)).thenReturn(true);
        doNothing().when(bookingRepository).deleteById(1L);

        assertTrue(bookingService.cancelBooking(1L));
    }

    @Test
    void cancelBookingNotExists() {
        when(bookingRepository.existsById(2L)).thenReturn(false);
        assertFalse(bookingService.cancelBooking(2L));
    }

    @Test
    void getBookingsByUserIdSuccess() {
        UserDTO user = new UserDTO();
        user.setId(101L);
        when(profileInterface.getUserByUserId(101L)).thenReturn(ResponseEntity.ok(user));

        Booking booking = new Booking();
        when(bookingRepository.findByUserId(101L)).thenReturn(List.of(booking));

        List<Booking> result = bookingService.getBookingsByUserId(101L);
        assertEquals(1, result.size());
    }

    @Test
    void getBookingByIdSuccess() {
        Booking booking = new Booking();
        booking.setId(1L);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        Optional<BookingResponseDTO> result = bookingService.getBookingById(1L);
        assertTrue(result.isPresent());
    }
}
