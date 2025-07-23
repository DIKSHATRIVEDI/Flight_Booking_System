package com.example.fare_service.service;

import com.example.fare_service.dto.BookingDTO;
import com.example.fare_service.dto.ResponseDTO;
import com.example.fare_service.exception.PaymentProcessingException;
import com.example.fare_service.exception.ResourceNotFoundException;
import com.example.fare_service.feign.BookingInterface;
import com.example.fare_service.model.Payment;
import com.example.fare_service.repository.PaymentRepository;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
public class PaymentService {

    @Autowired
    private BookingInterface bookingInterface;
    @Autowired
    private PaymentRepository paymentRepository;

    @Value("${stripe.secretKey}")
    private String secretKey;

    @Value("${stripe.webhookSecretKey}")
    private String webhookSecretKey;

    @PostConstruct
    public void init(){
        Stripe.apiKey = secretKey;// Set secret key.
    }

    public ResponseDTO createPaymentSession(Long bookingId) {
        ResponseEntity<BookingDTO> response = bookingInterface.getById(bookingId);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            BookingDTO bookingDTO = response.getBody();

            long amountInPaise = (long)(bookingDTO.totalFare * 100);

            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl("http://localhost:8095/success.html")
                    .setCancelUrl("http://localhost:8095/cancel.html")
                    .addLineItem(
                            SessionCreateParams.LineItem.builder()
                                    .setQuantity(1L)
                                    .setPriceData(
                                            SessionCreateParams.LineItem.PriceData.builder()
                                                    .setCurrency("INR")
                                                    .setUnitAmount(amountInPaise)
                                                    .setProductData(
                                                            SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                    .setName("Flight Booking #" + bookingId)
                                                                    .build()
                                                    ).build()
                                    ).build()
                    ).build();

            try {
                Session session = Session.create(params);

                Payment payment = new Payment();
                payment.setBookingId(bookingId);
                payment.setAmount(bookingDTO.totalFare);
                payment.setStatus("PENDING");
                payment.setSessionId(session.getId());
                paymentRepository.save(payment);

                ResponseDTO responseDTO = new ResponseDTO();
                responseDTO.setPaymentUrl(session.getUrl());
                responseDTO.setSessionId(session.getId());
                responseDTO.setStatus("PENDING");

                return responseDTO;

            } catch (StripeException e) {
                throw new PaymentProcessingException("Error creating Stripe payment session", e);
            }
        } else {
            throw new RuntimeException("Booking not found for ID: " + bookingId);
        }

    }

    public void handleStripeWebhook(HttpServletRequest request) throws IOException, SignatureVerificationException {
        System.out.println("Webhook triggered");
//        String payload = new BufferedReader(new InputStreamReader(request.getInputStream()))
//                .lines().collect(Collectors.joining());

        String payload;
        try {
            payload = StreamUtils.copyToString(request.getInputStream(), StandardCharsets.UTF_8);}
        catch (IOException e) {
            throw new RuntimeException("Failed to read webhook payload", e);
        }

        Event event = Webhook.constructEvent(payload, request.getHeader("Stripe-Signature"), webhookSecretKey);

        if ("checkout.session.completed".equals(event.getType())) {
            Session session = (Session) event.getDataObjectDeserializer().getObject().orElse(null);
            if (session == null) {
                throw new RuntimeException("Webhook session data missing");
            }

            String sessionId = session.getId();

            Payment payment = paymentRepository.findBySessionId(sessionId)
                    .orElseThrow(() -> new ResourceNotFoundException("Payment not found for session: " + sessionId));
            payment.setStatus("SUCCESS");
            paymentRepository.save(payment);

            // Notify Booking Service
            bookingInterface.confirmBooking(payment.getBookingId());
        }
    }
}


//    public ResponseDTO checkoutProducts(RequestDTO productRequest) {
//
//        // Create a PaymentIntent with the order amount and currency
//        SessionCreateParams.LineItem.PriceData.ProductData productData =
//                SessionCreateParams.LineItem.PriceData.ProductData.builder()
//                        .setName(productRequest.getName())
//                        .build();
//
//        // Create new line item with the above product data and associated price
//        SessionCreateParams.LineItem.PriceData priceData =
//                SessionCreateParams.LineItem.PriceData.builder()
//                        .setCurrency(productRequest.getCurrency() != null ? productRequest.getCurrency() : "USD")
//                        .setUnitAmount(productRequest.getAmount())
//                        .setProductData(productData)
//                        .build();
//
//        // Create new line item with the above price data
//        SessionCreateParams.LineItem lineItem =
//                SessionCreateParams
//                        .LineItem.builder()
//                        .setQuantity(productRequest.getQuantity())
//                        .setPriceData(priceData)
//                        .build();
//
//        // Create new session with the line items
//        SessionCreateParams params =
//                SessionCreateParams.builder()
//                        .setMode(SessionCreateParams.Mode.PAYMENT)
//                        .setSuccessUrl("http://localhost:8095/success")
//                        .setCancelUrl("http://localhost:8095/cancel")
//                        .addLineItem(lineItem)
//                        .build();
//
//        // Create new session
//        Session session = null;
//        try {
//            session = Session.create(params);
//        } catch (StripeException e) {
//            //log the error
//        }
//
//        return ResponseDTO
//                .builder()
//                .status("SUCCESS")
//                .message("Payment session created ")
//                .sessionId(session.getId())
//                .sessionUrl(session.getUrl())
//                .build();
//    }



