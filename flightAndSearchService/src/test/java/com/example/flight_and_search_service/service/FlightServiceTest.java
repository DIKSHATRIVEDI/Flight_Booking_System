package com.example.flight_and_search_service.service;

import com.example.flight_and_search_service.dto.FlightDTO;
import com.example.flight_and_search_service.exception.FlightCreationException;
import com.example.flight_and_search_service.exception.FlightNotFoundException;
import com.example.flight_and_search_service.exception.SeatUpdateException;
import com.example.flight_and_search_service.model.Flight;
import com.example.flight_and_search_service.repository.FlightRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FlightServiceTest {

    @Mock
    private FlightRepository flightRepository;

    @InjectMocks
    private FlightService flightService;

    private FlightDTO flightDTO; // Declare FlightDTO at class level

    private Flight flight;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        flightDTO = new FlightDTO();
        flightDTO.flightNumber = "AI101";
        flightDTO.airline = "Air India";
        flightDTO.source = "Delhi";
        flightDTO.destination = "Mumbai";
        flightDTO.departureDate = LocalDate.of(2025, 5, 20);
        flightDTO.departureTime = String.valueOf(LocalTime.of(10, 30));
        flightDTO.arrivalTime = String.valueOf(LocalTime.of(12, 45));
        flightDTO.availableSeats = 100;
        flightDTO.fare = 5000.0;

        flight = new Flight();
        flight.setId(1L);
        flight.setFlightNumber("AI101");
        flight.setAirline("Air India");
        flight.setSource("Delhi");
        flight.setDestination("Mumbai");
        flight.setDepartureDate(LocalDate.of(2025, 5, 20));
        flight.setDepartureTime(String.valueOf(LocalTime.of(10, 30)));
        flight.setArrivalTime(String.valueOf(LocalTime.of(12, 45)));
        flight.setAvailableSeats(100);
        flight.setFare(5000.0);
    }


    @Test
    void createFlightSuccess() {
        when(flightRepository.findByFlightNumber("AI101")).thenReturn(Optional.empty());
        when(flightRepository.save(any(Flight.class))).thenReturn(flight);

        Flight created = flightService.createFlight(flightDTO);
        assertNotNull(created);
        assertEquals("AI101", created.getFlightNumber());
    }

    @Test
    void createFlightAlreadyExists() {
        when(flightRepository.findByFlightNumber("AI101")).thenReturn(Optional.of(flight));
        assertThrows(FlightCreationException.class, () -> flightService.createFlight(flightDTO));
    }

    @Test
    void getAllFlights() {
        when(flightRepository.findAll()).thenReturn(List.of(flight));
        List<Flight> flights = flightService.getAllFlight();
        assertEquals(1, flights.size());
    }

    @Test
    void getByIdFound() {
        when(flightRepository.findById(1L)).thenReturn(Optional.of(flight));
        Flight result = flightService.getById(1L);
        assertEquals("AI101", result.getFlightNumber());
    }

    @Test
    void getByIdNotFound() {
        when(flightRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(FlightNotFoundException.class, () -> flightService.getById(1L));
    }

    @Test
    void updateFlightSuccess() {
        when(flightRepository.findById(1L)).thenReturn(Optional.of(flight));
        when(flightRepository.save(any(Flight.class))).thenReturn(flight);
        Flight updated = flightService.updateFlight(1L, flightDTO);
        assertEquals("AI101", updated.getFlightNumber());
    }

    @Test
    void deleteFlightSuccess() {
        when(flightRepository.existsById(1L)).thenReturn(true);
        doNothing().when(flightRepository).deleteById(1L);
        assertTrue(flightService.deleteFlight(1L));
    }

    @Test
    void deleteFlightNotFound() {
        when(flightRepository.existsById(1L)).thenReturn(false);
        assertThrows(FlightNotFoundException.class, () -> flightService.deleteFlight(1L));
    }

    @Test
    void searchFlight() {
        LocalDate date = LocalDate.of(2025, 5, 20);
        when(flightRepository.findBySourceAndDestinationAndDepartureDate("Delhi", "Mumbai", date))
                .thenReturn(List.of(flight));

        List<Flight> results = flightService.searchFlight("Delhi", "Mumbai", "2025-05-20");
        assertEquals(1, results.size());
        assertEquals("AI101", results.get(0).getFlightNumber());
    }

    @Test
    void updateSeatsSuccess() {
        when(flightRepository.findById(1L)).thenReturn(Optional.of(flight));
        when(flightRepository.save(any(Flight.class))).thenReturn(flight);

        boolean updated = flightService.updateSeats(1L, 10);
        assertTrue(updated);
        verify(flightRepository, times(1)).save(any(Flight.class));
    }

    @Test
    void updateSeatsInsufficientSeats() {
        flight.setAvailableSeats(5);
        when(flightRepository.findById(1L)).thenReturn(Optional.of(flight));

        assertThrows(SeatUpdateException.class, () -> flightService.updateSeats(1L, 10));
    }

}

