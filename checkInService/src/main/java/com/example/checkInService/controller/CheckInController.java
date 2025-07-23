package com.example.checkInService.controller;

import com.example.checkInService.service.CheckInService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/checkin")
public class CheckInController {

    @Autowired
    private CheckInService checkInService;

    // Check-in all passengers using only booking ID
    @PostMapping("/{bookingId}")
    public ResponseEntity<String> checkInAllPassengers(@PathVariable Long bookingId) {
        checkInService.checkInAllPassengers(bookingId);
        return ResponseEntity.ok("All passengers checked in successfully for booking ID: " + bookingId);
    }
}

