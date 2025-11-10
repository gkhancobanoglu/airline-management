package com.cobanoglu.airlinemanagement.service;

import com.cobanoglu.airlinemanagement.dto.PassengerDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PassengerService {

    PassengerDTO createPassenger(PassengerDTO passengerDTO);

    PassengerDTO updatePassenger(Long id, PassengerDTO passengerDTO);

    PassengerDTO getPassengerById(Long id);

    Page<PassengerDTO> listPassengers(Pageable pageable);

    boolean findByEmailUnique(String email);

    void updateLoyaltyPoints(Long passengerId, int delta);
}
