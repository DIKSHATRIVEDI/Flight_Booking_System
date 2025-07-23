package com.example.fare_service.controller;

import com.example.fare_service.dto.ResponseDTO;
import com.example.fare_service.service.PaymentService;
import com.stripe.exception.SignatureVerificationException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class PaymentControllerTest {

    @InjectMocks
    private PaymentController paymentController;

    @Mock
    private PaymentService paymentService;

    @Test
    void initiatePaymentSuccess() {
        // Arrange
        Long bookingId = 1L;
        ResponseDTO mockResponse = new ResponseDTO();
        mockResponse.setStatus("PENDING");
        mockResponse.setSessionId("sess_123");
        mockResponse.setPaymentUrl("http://stripe.com/pay");

        when(paymentService.createPaymentSession(bookingId)).thenReturn(mockResponse);

        // Act
        ResponseEntity<ResponseDTO> response = paymentController.initiatePayment(bookingId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("PENDING", response.getBody().getStatus());
        assertEquals("sess_123", response.getBody().getSessionId());
    }

    @Test
    void handleStripeWebhookSuccess() throws IOException, SignatureVerificationException {
        // Arrange
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        doNothing().when(paymentService).handleStripeWebhook(mockRequest);

        // Act
        ResponseEntity<String> response = paymentController.handleStripeWebhook(mockRequest);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Webhook handled", response.getBody());
    }

    @Test
    void handleStripeWebhookFailure() throws IOException, SignatureVerificationException {
        // Arrange
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        doThrow(new RuntimeException("Webhook processing error")).when(paymentService).handleStripeWebhook(mockRequest);

        // Act
        ResponseEntity<String> response = paymentController.handleStripeWebhook(mockRequest);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Webhook handling failed", response.getBody());
    }
}
