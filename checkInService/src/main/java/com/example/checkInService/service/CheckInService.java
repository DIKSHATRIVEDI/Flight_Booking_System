package com.example.checkInService.service;

import com.example.checkInService.dto.BookingResponseDTO;
import com.example.checkInService.dto.CheckInRequestDTO;
import com.example.checkInService.dto.PassengerDTO;
import com.example.checkInService.exception.CheckInException;
import com.example.checkInService.feign.BookingInterface;
import com.example.checkInService.model.CheckIn;
import com.example.checkInService.repository.CheckInRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
public class CheckInService {

    @Autowired
    private BookingInterface bookingInterface;

    @Autowired
    private CheckInRepository checkInRepository;

    public void checkInAllPassengers(Long bookingId) {
       // Get booking info
        ResponseEntity<BookingResponseDTO> response = bookingInterface.getById(bookingId);
        if (response == null || !response.getStatusCode().is2xxSuccessful()) {
            throw new CheckInException("Failed to fetch booking data from Booking Service.");
        }
        BookingResponseDTO booking = response.getBody();

        // Validate booking
        if (booking == null || !"CONFIRMED".equalsIgnoreCase(booking.status)) {
            throw new CheckInException("Booking not confirmed or not found.");
        }

        //  Loop over all passengers
        for (PassengerDTO passenger : booking.passengers) {
            try{
                // Check if already checked-in
                Optional<CheckIn> existing = checkInRepository.findByBookingIdAndPassengerId(bookingId, passenger.getPassengerId());
                if (existing.isPresent()) {
                    continue; // Skip already checked-in passenger
                }

                // Create new check-in
                CheckIn checkIn = new CheckIn();
                checkIn.setBookingId(bookingId);
                checkIn.setPassengerId(passenger.getPassengerId());
                checkIn.setSeatNumber("A" + new Random().nextInt(30));
                checkIn.setCheckInTime(LocalDateTime.now());
                checkIn.setCheckedIn(true);

                checkInRepository.save(checkIn);

            }catch(Exception e){
                throw new CheckInException("Error while checking in passenger ID: " + passenger.getPassengerId(), e);
            }
        }
    }
}
