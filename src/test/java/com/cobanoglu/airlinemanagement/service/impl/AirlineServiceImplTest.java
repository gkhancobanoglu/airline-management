package com.cobanoglu.airlinemanagement.service.impl;

import com.cobanoglu.airlinemanagement.dto.AirlineDTO;
import com.cobanoglu.airlinemanagement.entity.Airline;
import com.cobanoglu.airlinemanagement.exception.BadRequestException;
import com.cobanoglu.airlinemanagement.exception.NotFoundException;
import com.cobanoglu.airlinemanagement.mapper.AirlineMapper;
import com.cobanoglu.airlinemanagement.repository.AirlineRepository;
import com.cobanoglu.airlinemanagement.repository.BookingRepository;
import com.cobanoglu.airlinemanagement.repository.FlightRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AirlineServiceImplTest {

    @Mock
    private AirlineRepository airlineRepository;

    @Mock
    private FlightRepository flightRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private AirlineMapper airlineMapper;

    @InjectMocks
    private AirlineServiceImpl airlineService;

    private AirlineDTO dto;
    private Airline airline;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        dto = new AirlineDTO();
        dto.setId(1L);
        dto.setName("Turkish Airlines");
        dto.setCountry("Turkey");
        dto.setFleetSize("350");
        dto.setCodeIATA("TK");
        dto.setCodeICAO("THY");

        airline = new Airline();
        airline.setId(1L);
        airline.setName("Turkish Airlines");
        airline.setCountry("Turkey");
        airline.setFleetSize("350");
        airline.setCodeIATA("TK");
        airline.setCodeICAO("THY");
    }

    @Test
    void createAirline_success() {
        when(airlineRepository.existsByCodeIATA("TK")).thenReturn(false);
        when(airlineRepository.existsByCodeICAO("THY")).thenReturn(false);
        when(airlineMapper.toEntity(dto)).thenReturn(airline);
        when(airlineRepository.save(any(Airline.class))).thenReturn(airline);
        when(airlineMapper.toDto(airline)).thenReturn(dto);

        AirlineDTO result = airlineService.createAirline(dto);

        assertNotNull(result);
        assertEquals("TK", result.getCodeIATA());
        verify(airlineRepository).save(any(Airline.class));
    }

    @Test
    void createAirline_duplicateIATA_throwsBadRequest() {
        when(airlineRepository.existsByCodeIATA("TK")).thenReturn(true);
        assertThrows(BadRequestException.class, () -> airlineService.createAirline(dto));
    }

    @Test
    void createAirline_duplicateICAO_throwsBadRequest() {
        when(airlineRepository.existsByCodeIATA("TK")).thenReturn(false);
        when(airlineRepository.existsByCodeICAO("THY")).thenReturn(true);
        assertThrows(BadRequestException.class, () -> airlineService.createAirline(dto));
    }

    @Test
    void updateAirline_success() {
        when(airlineRepository.findById(1L)).thenReturn(Optional.of(airline));
        when(airlineRepository.existsByCodeIATAAndIdNot("TK", 1L)).thenReturn(false);
        when(airlineRepository.existsByCodeICAOAndIdNot("THY", 1L)).thenReturn(false);
        when(airlineRepository.save(any(Airline.class))).thenReturn(airline);
        when(airlineMapper.toDto(airline)).thenReturn(dto);

        dto.setCountry("Türkiye");
        AirlineDTO result = airlineService.updateAirline(1L, dto);

        assertNotNull(result);
        assertEquals("Türkiye", result.getCountry());
        verify(airlineRepository).save(any(Airline.class));
    }

    @Test
    void updateAirline_noChanges_throwsBadRequest() {
        when(airlineRepository.findById(1L)).thenReturn(Optional.of(airline));
        when(airlineRepository.existsByCodeIATAAndIdNot(anyString(), anyLong())).thenReturn(false);
        when(airlineRepository.existsByCodeICAOAndIdNot(anyString(), anyLong())).thenReturn(false);

        AirlineDTO identicalDto = new AirlineDTO();
        identicalDto.setName("Turkish Airlines");
        identicalDto.setCountry("Turkey");
        identicalDto.setFleetSize("350");
        identicalDto.setCodeIATA("TK");
        identicalDto.setCodeICAO("THY");

        assertThrows(BadRequestException.class, () -> airlineService.updateAirline(1L, identicalDto));
    }

    @Test
    void updateAirline_notFound_throwsException() {
        when(airlineRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> airlineService.updateAirline(99L, dto));
    }

    @Test
    void deleteAirline_success() {
        when(airlineRepository.findById(1L)).thenReturn(Optional.of(airline));
        when(bookingRepository.existsByFlight_Airline_Id(1L)).thenReturn(false);
        when(flightRepository.findByAirlineId(1L)).thenReturn(List.of());

        airlineService.deleteAirline(1L);

        verify(airlineRepository).delete(airline);
    }

    @Test
    void deleteAirline_hasBookings_throwsBadRequest() {
        when(airlineRepository.findById(1L)).thenReturn(Optional.of(airline));
        when(bookingRepository.existsByFlight_Airline_Id(1L)).thenReturn(true);

        assertThrows(BadRequestException.class, () -> airlineService.deleteAirline(1L));
    }

    @Test
    void deleteAirline_notFound_throwsException() {
        when(airlineRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> airlineService.deleteAirline(99L));
    }

    @Test
    void listAirlines_returnsPage() {
        Page<Airline> page = new PageImpl<>(List.of(airline));
        when(airlineRepository.findAll(any(PageRequest.class))).thenReturn(page);
        when(airlineMapper.toDto(any(Airline.class))).thenReturn(dto);

        Page<AirlineDTO> result = airlineService.listAirlines(PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
        verify(airlineRepository).findAll(any(PageRequest.class));
    }

    @Test
    void getAirlineById_success() {
        when(airlineRepository.findById(1L)).thenReturn(Optional.of(airline));
        when(airlineMapper.toDto(airline)).thenReturn(dto);

        AirlineDTO result = airlineService.getAirlineById(1L);

        assertNotNull(result);
        assertEquals("TK", result.getCodeIATA());
    }

    @Test
    void getAirlineById_notFound_throwsException() {
        when(airlineRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> airlineService.getAirlineById(99L));
    }
}
