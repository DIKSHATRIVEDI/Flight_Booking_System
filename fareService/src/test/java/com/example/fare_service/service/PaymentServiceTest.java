package com.example.fare_service.service;

import com.example.fare_service.dto.BookingDTO;
import com.example.fare_service.dto.ResponseDTO;
import com.example.fare_service.feign.BookingInterface;
import com.example.fare_service.model.Payment;
import com.example.fare_service.repository.PaymentRepository;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class PaymentServiceTest {

    @InjectMocks
    private PaymentService paymentService;

    @Mock
    private BookingInterface bookingInterface;

    @Mock
    private PaymentRepository paymentRepository;

    @BeforeEach
    void setup() {
        // Set Stripe secret key to avoid NullPointerException
        ReflectionTestUtils.setField(paymentService, "secretKey", "sk_test_mock");
        paymentService.init(); // Set Stripe.apiKey
    }

    @Test
    void createPaymentSessionSuccess() throws Exception {
        // Arrange
        BookingDTO bookingDTO = new BookingDTO();
        bookingDTO.id=1L;
        bookingDTO.totalFare=500.0;

        when(bookingInterface.getById(1L)).thenReturn(ResponseEntity.ok(bookingDTO));

        // Mock Stripe Session
        Session session = mock(Session.class);
        when(session.getId()).thenReturn("sess_123");
        when(session.getUrl()).thenReturn("https://mock-stripe-url");

        // Mock static Stripe method
        try (MockedStatic<Session> mockedSession = mockStatic(Session.class)) {
            mockedSession.when(() -> Session.create(any(SessionCreateParams.class)))
                    .thenReturn(session);

            // Act
            ResponseDTO responseDTO = paymentService.createPaymentSession(1L);

            // Assert
            assertEquals("PENDING", responseDTO.getStatus());
            assertEquals("sess_123", responseDTO.getSessionId());
            assertEquals("https://mock-stripe-url", responseDTO.getPaymentUrl());

            verify(paymentRepository, times(1)).save(any(Payment.class));
        }
    }

    @Test
    void createPaymentSessionBookingNotFound() {
        when(bookingInterface.getById(99L)).thenReturn(ResponseEntity.notFound().build());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            paymentService.createPaymentSession(99L);
        });

        assertTrue(exception.getMessage().contains("Booking not found"));
    }
}
