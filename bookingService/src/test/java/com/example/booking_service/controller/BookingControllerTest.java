package com.example.booking_service.controller;

import com.example.booking_service.dto.BookingDTO;
import com.example.booking_service.dto.BookingResponseDTO;
import com.example.booking_service.dto.FlightBookingDetailsDTO;
import com.example.booking_service.dto.ResponseDTO;
import com.example.booking_service.model.Booking;
import com.example.booking_service.service.BookingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingControllerTest {

    @InjectMocks
    private BookingController bookingController;

    @Mock
    private BookingService bookingService;

    @Test
    void createBooking() {
        BookingDTO dto = new BookingDTO(); // fill with mock data as needed
        ResponseDTO mockResponse = new ResponseDTO();
        mockResponse.paymentUrl="http://localhost:8095/index.html";

        when(bookingService.createBookingAndInitiatePayment(dto, 101L)).thenReturn(mockResponse);

        ResponseEntity<?> response = bookingController.create(dto, 101L);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(mockResponse, response.getBody());
    }

    @Test
    void getBookingByIdFound() {
        BookingResponseDTO booking = new BookingResponseDTO();
        booking.setId(1L);
        when(bookingService.getBookingById(1L)).thenReturn(Optional.of(booking));

        ResponseEntity<BookingResponseDTO> response = bookingController.getById(1L);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(booking, response.getBody());
    }

    @Test
    void getBookingByIdNotFound() {
        when(bookingService.getBookingById(99L)).thenReturn(Optional.empty());

        ResponseEntity<BookingResponseDTO> response = bookingController.getById(99L);
        assertEquals(404, response.getStatusCodeValue());
    }

    @Test
    void cancelBookingSuccess() {
        when(bookingService.cancelBooking(5L)).thenReturn(true);

        ResponseEntity<String> response = bookingController.cancel(5L);
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().contains("cancelled"));
    }

    @Test
    void cancelBookingNotFound() {
        when(bookingService.cancelBooking(5L)).thenReturn(false);

        ResponseEntity<String> response = bookingController.cancel(5L);
        assertEquals(404, response.getStatusCodeValue());
    }

    @Test
    void confirmBookingSuccess() {
        Booking booking = new Booking();
        booking.setId(10L);

        when(bookingService.confirmBooking(10L)).thenReturn(Optional.of(booking));

        ResponseEntity<String> response = bookingController.confirmBooking(10L);
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().contains("confirmed"));
    }

    @Test
    void confirmBookingFail() {
        when(bookingService.confirmBooking(100L)).thenReturn(Optional.empty());

        ResponseEntity<String> response = bookingController.confirmBooking(100L);
        assertEquals(400, response.getStatusCodeValue());
    }

    @Test
    void viewPassengersByFlight() throws Exception {
        FlightBookingDetailsDTO dto = new FlightBookingDetailsDTO();
        when(bookingService.getPassengersByFlightId(5L)).thenReturn(dto);

        ResponseEntity<FlightBookingDetailsDTO> response = bookingController.viewPassengersByFlight(5L);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(dto, response.getBody());
    }

    @Test
    void getBookingsByUserId() {
        Booking b1 = new Booking(); b1.setId(1L);
        Booking b2 = new Booking(); b2.setId(2L);
        List<Booking> list = Arrays.asList(b1, b2);

        when(bookingService.getBookingsByUserId(7L)).thenReturn(list);

        ResponseEntity<List<Booking>> response = bookingController.getBookingByUserId(7L);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(list, response.getBody());
    }
}
