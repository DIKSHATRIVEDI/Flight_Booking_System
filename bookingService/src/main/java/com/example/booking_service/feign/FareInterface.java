package com.example.booking_service.feign;

import com.example.booking_service.dto.ResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient("FARE-SERVICE")
public interface FareInterface {
    @PostMapping("/fare/initiate/{bookingId}")
    ResponseDTO initiatePayment(@PathVariable Long bookingId);

}
