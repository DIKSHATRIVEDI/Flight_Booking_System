package com.example.fare_service.feign;

import com.example.fare_service.dto.BookingDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

@FeignClient("BOOKING-SERVICE")
public interface BookingInterface {
    @GetMapping("/booking/get/{id}")
    public ResponseEntity<BookingDTO> getById(@PathVariable Long id);
    @PutMapping("/booking/confirm/{bookingId}")
    public ResponseEntity<String> confirmBooking(@PathVariable Long bookingId);
}
