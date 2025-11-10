package com.cobanoglu.airlinemanagement.service.impl;

import com.cobanoglu.airlinemanagement.dto.PassengerDTO;
import com.cobanoglu.airlinemanagement.entity.Passenger;
import com.cobanoglu.airlinemanagement.exception.BadRequestException;
import com.cobanoglu.airlinemanagement.exception.NotFoundException;
import com.cobanoglu.airlinemanagement.mapper.PassengerMapper;
import com.cobanoglu.airlinemanagement.repository.PassengerRepository;
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

class PassengerServiceImplTest {

    @Mock private PassengerRepository passengerRepository;
    @Mock private PassengerMapper passengerMapper;

    @InjectMocks
    private PassengerServiceImpl passengerService;

    private Passenger passenger;
    private PassengerDTO dto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        passenger = new Passenger();
        passenger.setId(1L);
        passenger.setName("John");
        passenger.setSurname("Doe");
        passenger.setEmail("john@example.com");
        passenger.setLoyaltyPoints(100);

        dto = new PassengerDTO();
        dto.setId(1L);
        dto.setName("John");
        dto.setSurname("Doe");
        dto.setEmail("john@example.com");
        dto.setLoyaltyPoints(100);
    }

    @Test
    void createPassenger_success() {
        when(passengerRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(passengerMapper.toEntity(dto)).thenReturn(passenger);
        when(passengerRepository.save(any())).thenReturn(passenger);
        when(passengerMapper.toDto(passenger)).thenReturn(dto);

        PassengerDTO result = passengerService.createPassenger(dto);

        assertNotNull(result);
        verify(passengerRepository).save(any());
    }

    @Test
    void createPassenger_emailExists_throwsBadRequest() {
        when(passengerRepository.existsByEmail("john@example.com")).thenReturn(true);
        assertThrows(BadRequestException.class, () -> passengerService.createPassenger(dto));
    }

    @Test
    void updatePassenger_success() {
        Passenger updatedEntity = new Passenger();
        updatedEntity.setName("Johnny");
        updatedEntity.setSurname("Doe");
        updatedEntity.setEmail("john@example.com");
        updatedEntity.setLoyaltyPoints(120);

        PassengerDTO updatedDto = new PassengerDTO();
        updatedDto.setName("Johnny");
        updatedDto.setSurname("Doe");
        updatedDto.setEmail("john@example.com");
        updatedDto.setLoyaltyPoints(120);

        when(passengerRepository.findById(1L)).thenReturn(Optional.of(passenger));
        when(passengerMapper.toDto(any())).thenReturn(updatedDto);
        when(passengerRepository.save(any())).thenReturn(updatedEntity);

        PassengerDTO result = passengerService.updatePassenger(1L, updatedDto);

        assertNotNull(result);
        assertEquals("Johnny", result.getName());
        verify(passengerRepository).save(any());
    }

    @Test
    void updatePassenger_notFound_throwsNotFound() {
        when(passengerRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> passengerService.updatePassenger(1L, dto));
    }

    @Test
    void updatePassenger_emailConflict_throwsBadRequest() {
        PassengerDTO newDto = new PassengerDTO();
        newDto.setName("John");
        newDto.setSurname("Doe");
        newDto.setEmail("new@example.com");
        newDto.setLoyaltyPoints(100);

        when(passengerRepository.findById(1L)).thenReturn(Optional.of(passenger));
        when(passengerRepository.existsByEmail("new@example.com")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> passengerService.updatePassenger(1L, newDto));
    }

    @Test
    void updatePassenger_noChanges_throwsBadRequest() {
        when(passengerRepository.findById(1L)).thenReturn(Optional.of(passenger));
        assertThrows(BadRequestException.class, () -> passengerService.updatePassenger(1L, dto));
    }

    @Test
    void getPassengerById_success() {
        when(passengerRepository.findById(1L)).thenReturn(Optional.of(passenger));
        when(passengerMapper.toDto(passenger)).thenReturn(dto);

        PassengerDTO result = passengerService.getPassengerById(1L);

        assertNotNull(result);
        verify(passengerRepository).findById(1L);
    }

    @Test
    void getPassengerById_notFound_throwsException() {
        when(passengerRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> passengerService.getPassengerById(1L));
    }

    @Test
    void listPassengers_success() {
        when(passengerRepository.findAll(any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(passenger)));
        when(passengerMapper.toDto(any())).thenReturn(dto);

        Page<PassengerDTO> result = passengerService.listPassengers(PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
        verify(passengerRepository).findAll(any(PageRequest.class));
    }

    @Test
    void findByEmailUnique_trueWhenNotExists() {
        when(passengerRepository.existsByEmail("a@b.com")).thenReturn(false);
        assertTrue(passengerService.findByEmailUnique("a@b.com"));
    }

    @Test
    void findByEmailUnique_falseWhenExists() {
        when(passengerRepository.existsByEmail("a@b.com")).thenReturn(true);
        assertFalse(passengerService.findByEmailUnique("a@b.com"));
    }


    @Test
    void updateLoyaltyPoints_success_increasePoints() {
        when(passengerRepository.findById(1L)).thenReturn(Optional.of(passenger));

        passengerService.updateLoyaltyPoints(1L, 50);

        assertEquals(150, passenger.getLoyaltyPoints());
        verify(passengerRepository).save(any());
    }

    @Test
    void updateLoyaltyPoints_success_negativeDoesNotGoBelowZero() {
        passenger.setLoyaltyPoints(20);
        when(passengerRepository.findById(1L)).thenReturn(Optional.of(passenger));

        passengerService.updateLoyaltyPoints(1L, -50);

        assertEquals(0, passenger.getLoyaltyPoints());
    }

    @Test
    void updateLoyaltyPoints_notFound_throwsNotFound() {
        when(passengerRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> passengerService.updateLoyaltyPoints(1L, 10));
    }
}
