package com.example.checkInService.feign;

import com.example.checkInService.dto.BookingResponseDTO;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name="BOOKING-SERVICE")
public interface BookingInterface {
    @GetMapping("/booking/get/{id}")
    public ResponseEntity<BookingResponseDTO> getById(@PathVariable Long id);
}
