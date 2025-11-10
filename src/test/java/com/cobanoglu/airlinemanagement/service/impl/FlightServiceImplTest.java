package com.cobanoglu.airlinemanagement.service.impl;

import com.cobanoglu.airlinemanagement.dto.FlightDTO;
import com.cobanoglu.airlinemanagement.entity.Airline;
import com.cobanoglu.airlinemanagement.entity.Flight;
import com.cobanoglu.airlinemanagement.exception.BadRequestException;
import com.cobanoglu.airlinemanagement.exception.FlightConflictException;
import com.cobanoglu.airlinemanagement.exception.NotFoundException;
import com.cobanoglu.airlinemanagement.mapper.FlightMapper;
import com.cobanoglu.airlinemanagement.repository.AirlineRepository;
import com.cobanoglu.airlinemanagement.repository.BookingRepository;
import com.cobanoglu.airlinemanagement.repository.FlightRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FlightServiceImplTest {

    @Mock private FlightRepository flightRepository;
    @Mock private AirlineRepository airlineRepository;
    @Mock private BookingRepository bookingRepository;
    @Mock private FlightMapper flightMapper;

    @InjectMocks
    private FlightServiceImpl flightService;

    private Airline airline;
    private Flight flight;
    private FlightDTO dto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        airline = new Airline();
        airline.setId(1L);
        airline.setName("Turkish Airlines");

        flight = new Flight();
        flight.setId(10L);
        flight.setFlightNumber("TK100");
        flight.setOrigin("Istanbul");
        flight.setDestination("Berlin");
        flight.setDepartureTime(LocalDateTime.now().plusDays(1));
        flight.setArrivalTime(LocalDateTime.now().plusDays(1).plusHours(3));
        flight.setCapacity(150);
        flight.setBasePrice(BigDecimal.valueOf(2000));
        flight.setAirline(airline);

        dto = new FlightDTO();
        dto.setFlightNumber("TK100");
        dto.setOrigin("Istanbul");
        dto.setDestination("Berlin");
        dto.setDepartureTime(LocalDateTime.now().plusDays(1));
        dto.setArrivalTime(LocalDateTime.now().plusDays(1).plusHours(3));
        dto.setCapacity(150);
        dto.setBasePrice(BigDecimal.valueOf(2000));
        dto.setAirlineId(1L);
    }

    @Test
    void createFlight_success() {
        when(airlineRepository.findById(1L)).thenReturn(Optional.of(airline));
        when(flightRepository.existsByFlightNumberAndAirlineIdAndOriginAndDepartureTimeBetween(any(), any(), any(), any(), any()))
                .thenReturn(false);
        when(flightRepository.existsByFlightNumberAndAirlineIdAndDepartureTimeLessThanAndArrivalTimeGreaterThan(any(), any(), any(), any()))
                .thenReturn(false);
        when(flightMapper.toEntity(dto)).thenReturn(flight);
        when(flightRepository.save(any())).thenReturn(flight);
        when(flightMapper.toDto(any())).thenReturn(dto);

        FlightDTO result = flightService.createFlight(dto);

        assertNotNull(result);
        verify(flightRepository).save(any());
        verify(airlineRepository).findById(1L);
    }

    @Test
    void createFlight_airlineNotFound_throwsNotFound() {
        when(airlineRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> flightService.createFlight(dto));
    }

    @Test
    void createFlight_duplicate_throwsConflict() {
        when(airlineRepository.findById(1L)).thenReturn(Optional.of(airline));
        when(flightRepository.existsByFlightNumberAndAirlineIdAndOriginAndDepartureTimeBetween(any(), any(), any(), any(), any()))
                .thenReturn(true);

        assertThrows(FlightConflictException.class, () -> flightService.createFlight(dto));
    }

    @Test
    void createFlight_timeConflict_throwsConflict() {
        when(airlineRepository.findById(1L)).thenReturn(Optional.of(airline));
        when(flightRepository.existsByFlightNumberAndAirlineIdAndOriginAndDepartureTimeBetween(any(), any(), any(), any(), any()))
                .thenReturn(false);
        when(flightRepository.existsByFlightNumberAndAirlineIdAndDepartureTimeLessThanAndArrivalTimeGreaterThan(any(), any(), any(), any()))
                .thenReturn(true);

        assertThrows(FlightConflictException.class, () -> flightService.createFlight(dto));
    }

    @Test
    void createFlight_invalidCapacity_throwsBadRequest() {
        dto.setCapacity(10);
        assertThrows(BadRequestException.class, () -> flightService.createFlight(dto));
    }

    @Test
    void createFlight_invalidDates_throwsBadRequest() {
        dto.setArrivalTime(dto.getDepartureTime().minusHours(2));
        assertThrows(BadRequestException.class, () -> flightService.createFlight(dto));
    }

    @Test
    void updateFlight_success() {
        when(flightRepository.findById(10L)).thenReturn(Optional.of(flight));
        when(flightRepository.existsByFlightNumberAndAirlineIdAndOriginAndDepartureTimeBetweenAndIdNot(any(), any(), any(), any(), any(), any()))
                .thenReturn(false);
        when(flightRepository.existsByFlightNumberAndAirlineIdAndDepartureTimeLessThanAndArrivalTimeGreaterThanAndIdNot(any(), any(), any(), any(), any()))
                .thenReturn(false);
        when(flightRepository.save(any())).thenReturn(flight);
        when(flightMapper.toDto(any())).thenReturn(dto);

        dto.setBasePrice(BigDecimal.valueOf(3000));

        FlightDTO result = flightService.updateFlight(10L, dto);

        assertNotNull(result);
        verify(flightRepository).save(any());
    }

    @Test
    void updateFlight_notFound_throwsNotFound() {
        when(flightRepository.findById(10L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> flightService.updateFlight(10L, dto));
    }

    @Test
    void updateFlight_noChanges_throwsBadRequest() {
        when(flightRepository.findById(10L)).thenReturn(Optional.of(flight));
        dto.setAirlineId(1L);
        dto.setFlightNumber("TK100");
        dto.setOrigin("Istanbul");
        dto.setDestination("Berlin");
        dto.setDepartureTime(flight.getDepartureTime());
        dto.setArrivalTime(flight.getArrivalTime());
        dto.setBasePrice(flight.getBasePrice());
        dto.setCapacity(flight.getCapacity());

        assertThrows(BadRequestException.class, () -> flightService.updateFlight(10L, dto));
    }

    @Test
    void updateFlight_duplicate_throwsConflict() {
        when(flightRepository.findById(10L)).thenReturn(Optional.of(flight));
        when(flightRepository.existsByFlightNumberAndAirlineIdAndOriginAndDepartureTimeBetweenAndIdNot(any(), any(), any(), any(), any(), any()))
                .thenReturn(true);

        assertThrows(FlightConflictException.class, () -> flightService.updateFlight(10L, dto));
    }

    @Test
    void deleteFlight_success() {
        when(flightRepository.findById(10L)).thenReturn(Optional.of(flight));
        when(bookingRepository.existsByFlight_Id(10L)).thenReturn(false);

        flightService.deleteFlight(10L);

        verify(flightRepository).delete(flight);
    }

    @Test
    void deleteFlight_withBookings_throwsBadRequest() {
        when(flightRepository.findById(10L)).thenReturn(Optional.of(flight));
        when(bookingRepository.existsByFlight_Id(10L)).thenReturn(true);

        assertThrows(BadRequestException.class, () -> flightService.deleteFlight(10L));
    }

    @Test
    void deleteFlight_notFound_throwsNotFound() {
        when(flightRepository.findById(10L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> flightService.deleteFlight(10L));
    }

    @Test
    void listFlights_success() {
        when(flightRepository.findAll(any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(flight)));
        when(flightMapper.toDto(any())).thenReturn(dto);

        Page<FlightDTO> result = flightService.listFlights(PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
        verify(flightRepository).findAll(any(PageRequest.class));
    }

    @Test
    void getFlightById_success() {
        when(flightRepository.findById(10L)).thenReturn(Optional.of(flight));
        when(flightMapper.toDto(flight)).thenReturn(dto);

        FlightDTO result = flightService.getFlightById(10L);

        assertNotNull(result);
        verify(flightRepository).findById(10L);
    }

    @Test
    void getFlightById_notFound_throwsNotFound() {
        when(flightRepository.findById(10L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> flightService.getFlightById(10L));
    }
}
