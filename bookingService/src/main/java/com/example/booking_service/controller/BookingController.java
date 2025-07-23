package com.example.booking_service.controller;

import com.example.booking_service.dto.BookingDTO;
import com.example.booking_service.dto.BookingResponseDTO;
import com.example.booking_service.dto.FlightBookingDetailsDTO;
import com.example.booking_service.dto.ResponseDTO;
import com.example.booking_service.model.Booking;
import com.example.booking_service.service.BookingService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/booking")
public class BookingController {
    @Autowired
    BookingService bookingService;

    @PostMapping("/create/{flightId}")
    public ResponseEntity<?> create(@PathVariable Long flightId, @RequestBody @Valid BookingDTO bookingDTO, @RequestHeader("X-user-id") @Valid Long userId) {
        //BookingDTO bookingDTO = new BookingDTO();
        bookingDTO.setFlightId(flightId); // set flightId from path

        ResponseDTO response = bookingService.createBookingAndInitiatePayment(bookingDTO, userId);
        try {
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace(); // logs actual stacktrace
            throw new RuntimeException("Error in Booking Controller", e);
        }

    }

    @GetMapping("/get/{id}")
    public ResponseEntity<BookingResponseDTO> getById(@PathVariable @Valid  Long id) {//gets its personal booking
        Optional<BookingResponseDTO> booking=bookingService.getBookingById(id);
        return (booking.isPresent())?ResponseEntity.ok(booking.get()):ResponseEntity.notFound().build();
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> cancel(@PathVariable @Valid  Long id) {
        boolean isCancelled = bookingService.cancelBooking(id);
        return isCancelled
                ? ResponseEntity.ok("Your booking with ID " + id + " has been cancelled.")
                : ResponseEntity.status(HttpStatus.NOT_FOUND).body("Booking with ID " + id + " not found.");
    }

    @GetMapping("/viewPassengers/{flightId}")
    @CircuitBreaker(name = "FLIGHT-AND-SEARCH-SERVICE", fallbackMethod = "flightFallback")
    public ResponseEntity<FlightBookingDetailsDTO> viewPassengersByFlight(@PathVariable @Valid  Long flightId) throws Exception {
        FlightBookingDetailsDTO response = bookingService.getPassengersByFlightId(flightId);
        return (response == null) ? ResponseEntity.notFound().build() : ResponseEntity.ok(response);
    }

    @PutMapping("/confirm/{bookingId}")
    public ResponseEntity<String> confirmBooking(@PathVariable @Valid  Long bookingId) {
        Optional<Booking> bookingOpt = bookingService.confirmBooking(bookingId);

        if (bookingOpt.isPresent()) {
            return ResponseEntity.ok("Booking confirmed with ID: " + bookingOpt.get().getId());
        } else {
            return ResponseEntity.badRequest().body("Booking confirmation failed. Please check the booking ID.");
        }
    }


    public ResponseEntity<String> flightFallback(Exception e) {
        System.out.println("Flight service is down, fallback triggered " + e.getMessage());
        return ResponseEntity.status(503).body("Flight service is down");
    }

    @GetMapping("/get/user/id")//a user can see its booking
    public ResponseEntity<List<Booking>> getBookingByUserId(@RequestHeader("X-user-id") @Valid  Long userId) {
        List<Booking> bookings = bookingService.getBookingsByUserId(userId);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/get/user/{userId}")// admin can see a particular users booking
    public List<Booking> getBookingsByUserId(@PathVariable Long userId) {
        return bookingService.getBookingsByUserId(userId);
    }


}


