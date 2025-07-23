package com.example.booking_service.service;

import com.example.booking_service.dto.*;
import com.example.booking_service.exception.BookingException;
import com.example.booking_service.exception.FlightNotFoundException;
import com.example.booking_service.exception.PassengerSaveException;
import com.example.booking_service.feign.FareInterface;
import com.example.booking_service.feign.FlightInterface;
import com.example.booking_service.feign.ProfileInterface;
import com.example.booking_service.model.Booking;
import com.example.booking_service.model.Passenger;
import com.example.booking_service.repository.BookingRepository;
import com.example.booking_service.repository.PassengerRepository;
import feign.FeignException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class BookingService {

    @Autowired
    BookingRepository bookingRepository;

    @Autowired
    FlightInterface flightInterface;

    @Autowired
    PassengerRepository passengerRepository;

    @Autowired
    FareInterface fareInterface;

    @Autowired
    ProfileInterface profileInterface;

    public Optional<Booking> createBooking(BookingDTO bookingDTO, Long userId) {
        FlightResponseDTO flightResponseDTO;
        try {
            flightResponseDTO = flightInterface.getFlightById(bookingDTO.flightId);
            System.out.println("Fetched flight  " + flightResponseDTO);
        } catch (Exception e) {
            throw new FlightNotFoundException("Failed to fetch flight: " + e.getMessage());
        }

        if (flightResponseDTO == null || flightResponseDTO.availableSeats < bookingDTO.passengers.size()) {
            throw new BookingException("Invalid Flight ID or No Seats Available");
        }

        for (PassengerDTO passengerDTO : bookingDTO.passengers) {
            Optional<Booking> existing = bookingRepository.findByFlightIdAndAadharNumber(
                    bookingDTO.flightId,
                    passengerDTO.aadharNumber
            );
            if (existing.isPresent()) {
                throw new BookingException("Passenger is already booked on this flight.");
            }
        }

        Booking booking = new Booking();
        booking.setFlightId(bookingDTO.flightId);
        booking.setUserId(userId);
        booking.setNumberOfPassengers(bookingDTO.passengers.size());
        booking.setBookingDate(LocalDateTime.now().toString());
        booking.setStatus("PENDING");
        booking.setTotalFare(flightResponseDTO.fare * bookingDTO.passengers.size());

        List<Passenger> passengers = new ArrayList<>();
        for (PassengerDTO passengerDTO : bookingDTO.passengers) {
            Passenger passenger = new Passenger();
            passenger.setFirstName(passengerDTO.firstName);
            passenger.setLastName(passengerDTO.lastName);
            passenger.setEmail(passengerDTO.email);
            passenger.setGender(passengerDTO.gender);
            passenger.setAadharNumber(passengerDTO.aadharNumber);
            passenger.setFlightId(bookingDTO.flightId);
            passenger.setBooking(booking);
            passengers.add(passenger);
        }

        Booking savedBooking;
        try {
            booking.setPassengers(passengers);
            savedBooking = bookingRepository.save(booking);
        } catch (Exception e) {
            throw new BookingException("Failed to save booking: " + e.getMessage());
        }

        for (Passenger p : passengers) {
            p.setBooking(savedBooking);
        }

        try {
            passengerRepository.saveAll(passengers);
        } catch (Exception e) {
            throw new PassengerSaveException("Failed to save passengers: " + e.getMessage());
        }

        return Optional.of(savedBooking);

    }

    public ResponseDTO createBookingAndInitiatePayment(BookingDTO bookingDTO, Long userId) {
        Optional<Booking> savedBooking = createBooking(bookingDTO, userId);
        if (savedBooking.isEmpty()) {
            throw new BookingException("Booking failed");
        }

        Booking booking = savedBooking.get();

        // Call Fare Service to initiate payment
        try {
            return fareInterface.initiatePayment(booking.getId());
        } catch (Exception e) {
            throw new BookingException("Payment initiation failed: " + e.getMessage());
        }
    }

    public Optional<Booking> confirmBooking(Long bookingId) {
        Optional<Booking> bookingOpt;

        try {
            bookingOpt = bookingRepository.findById(bookingId);
        } catch (Exception e) {
            System.out.println("Error fetching booking " + e.getMessage());
            return Optional.empty();
        }

        if (bookingOpt.isPresent()) {
            Booking booking = bookingOpt.get();
            if ("CONFIRMED".equals(booking.getStatus())) {
                return Optional.of(booking);
            }
            booking.setStatus("CONFIRMED");
            int seatsToBook = bookingOpt.get().getPassengers().size();
            //flightInterface.updateSeats(bookingOpt.get().getFlightId(), seatsToBook);

            try {
                flightInterface.updateSeats(booking.getFlightId(), seatsToBook);
            } catch (FeignException e) {
                throw new BookingException("Seat update failed via Flight Service: " + e.contentUTF8());
            }

            try {
                  Booking updatedBooking=bookingRepository.save(booking);

                return Optional.of(updatedBooking);


            } catch (Exception e) {
                throw new BookingException("Failed to update booking status: " + e.getMessage());
            }


        }

        return Optional.empty();
    }

    public Optional<BookingResponseDTO> getBookingById(Long id) {
        try {
            Optional<Booking> bookingOpt = bookingRepository.findById(id);

            if (bookingOpt.isEmpty()) {
                return Optional.empty();
            }

            Booking booking = bookingOpt.get();

            FlightResponseDTO flight = flightInterface.getFlightById(booking.getFlightId());

            // Map to DTO
            BookingResponseDTO response = new BookingResponseDTO();
            response.setId(booking.getId());
            response.setUserId(booking.getUserId());
            response.setFlightId(booking.getFlightId());
            response.setStatus(booking.getStatus());
            response.setBookingDate(LocalDateTime.parse(booking.getBookingDate()));
            response.setNumberOfPassengers(booking.getNumberOfPassengers());
            response.setTotalFare(booking.getTotalFare());
            response.setPassengers(booking.getPassengers());

            // Set Flight Details
            FlightDTO flightDTO = new FlightDTO();
            flightDTO.setFlightNumber(flight.getFlightNumber());
            flightDTO.setAirline(flight.getAirline());
            flightDTO.setSource(flight.getSource());
            flightDTO.setDestination(flight.getDestination());
            flightDTO.setDepartureDate(flight.getDepartureDate());
            flightDTO.setDepartureTime(flight.getDepartureTime());

            // Set FlightDTO into response
            response.setFlight(flightDTO);

            return Optional.of(response);
        } catch (Exception e) {
            throw new BookingException("Error fetching booking by ID: " + e.getMessage());
        }
    }

    public boolean cancelBooking(Long id) {
        try {
            if (!bookingRepository.existsById(id)) return false;
            bookingRepository.deleteById(id);
            return true;
        } catch (Exception e) {
            throw new BookingException("Error cancelling booking: " + e.getMessage());
        }
    }

    public FlightBookingDetailsDTO getPassengersByFlightId(Long flightId) {
        FlightResponseDTO flightResponseDTO;
        flightResponseDTO = flightInterface.getFlightById(flightId);

        if (flightResponseDTO == null) return null;

        List<Passenger> passengers;
        try {
            passengers = passengerRepository.findByFlightId(flightId);
        } catch (Exception e) {
            throw new PassengerSaveException("Error fetching passengers: " + e.getMessage());
        }

        List<PassengerDTO> passengerDTOs = passengers.stream().map(p -> {
            PassengerDTO dto = new PassengerDTO();
            dto.firstName = p.getFirstName();
            dto.lastName = p.getLastName();
            dto.email = p.getEmail();
            dto.gender=p.getGender();
            dto.aadharNumber=p.getAadharNumber();
            return dto;
        }).toList();

        return new FlightBookingDetailsDTO(passengerDTOs);
    }

    public List<Booking> getBookingsByUserId(Long userId) {
        UserDTO userDTO = profileInterface.getUserByUserId(userId).getBody();

        if (userDTO == null) {
            throw new BookingException("User not found with user id: " + userId);
        }

        List<Booking> bookings;
        try {
            bookings = bookingRepository.findByUserIdAndStatus(userId, "CONFIRMED");
        } catch (Exception e) {
            throw new BookingException("Error fetching bookings: " + e.getMessage());
        }
        return bookings;
    }

}
