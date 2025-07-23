package com.example.flight_and_search_service.controller;

import com.example.flight_and_search_service.dto.FlightDTO;
import com.example.flight_and_search_service.model.Flight;
import com.example.flight_and_search_service.service.FlightService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class FlightControllerTest {

    private FlightService flightService;

    private FlightController flightController;

    @BeforeEach
    public void setup() throws Exception {
        flightService = mock(FlightService.class);
        flightController = new FlightController();

        // Inject mocked service manually using reflection
        var field = FlightController.class.getDeclaredField("service");
        field.setAccessible(true);
        field.set(flightController, flightService);
    }


    @Test
    public void getAllFlights() {
        Flight flight = new Flight(1L, "AI101", "Air India", "DEL", "BOM",
                LocalDate.now(), "08:00", "10:30", 120, 4500.0);
        when(flightService.getAllFlight()).thenReturn(List.of(flight));

        List<Flight> result = flightController.getAll();

        assertEquals(1, result.size());
        assertEquals("AI101", result.get(0).getFlightNumber());
        verify(flightService).getAllFlight();
    }


    @Test
    public void createFlight() {
        FlightDTO dto = new FlightDTO("AI102", "Indigo", "DEL", "BLR",
                LocalDate.of(2025, 6, 20), "09:00", "12:00", 100, 5000.0);

        Flight flight = new Flight(2L, dto.flightNumber, dto.airline, dto.source, dto.destination,
                dto.departureDate, dto.departureTime, dto.arrivalTime, dto.availableSeats, dto.fare);

        when(flightService.createFlight(dto)).thenReturn(flight);

        Flight result = flightController.create(dto);

        assertNotNull(result);
        assertEquals("AI102", result.getFlightNumber());
        verify(flightService).createFlight(dto);
    }

    @Test
    public void deleteFlightSuccess() {
        when(flightService.deleteFlight(1L)).thenReturn(true);

        var response = flightController.delete(1L);

        assertEquals(204, response.getStatusCodeValue());  // HTTP 204 No Content
        verify(flightService, times(1)).deleteFlight(1L);
    }

    @Test
    public void deleteFlightNotFound() {
        when(flightService.deleteFlight(99L)).thenReturn(false);

        var response = flightController.delete(99L);

        assertEquals(404, response.getStatusCodeValue());  // HTTP 404 Not Found
        verify(flightService, times(1)).deleteFlight(99L);
    }
}
