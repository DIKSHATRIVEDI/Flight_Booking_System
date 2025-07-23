package com.example.fare_service.controller;

import com.example.fare_service.dto.RequestDTO;
import com.example.fare_service.dto.ResponseDTO;
import com.example.fare_service.service.PaymentService;
import com.stripe.exception.SignatureVerificationException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/fare")
@RequiredArgsConstructor
public class PaymentController {

    private PaymentService paymentService;

    @Autowired
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/initiate/{bookingId}")
    public ResponseEntity<ResponseDTO> initiatePayment(@PathVariable Long bookingId) {
        ResponseDTO response = paymentService.createPaymentSession(bookingId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handleStripeWebhook(HttpServletRequest request) throws IOException, SignatureVerificationException {
        System.out.println("Webhook triggered");

        try {
            paymentService.handleStripeWebhook(request);
            return ResponseEntity.ok("Webhook handled");
        } catch (Exception e) {
            System.err.println("Error handling webhook: " + e.getMessage());
            e.printStackTrace(); // Print full stack trace
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Webhook handling failed");
        }
    }
}