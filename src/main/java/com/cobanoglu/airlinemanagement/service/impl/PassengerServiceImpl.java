package com.cobanoglu.airlinemanagement.service.impl;

import com.cobanoglu.airlinemanagement.dto.PassengerDTO;
import com.cobanoglu.airlinemanagement.entity.Passenger;
import com.cobanoglu.airlinemanagement.exception.BadRequestException;
import com.cobanoglu.airlinemanagement.exception.NotFoundException;
import com.cobanoglu.airlinemanagement.mapper.PassengerMapper;
import com.cobanoglu.airlinemanagement.repository.PassengerRepository;
import com.cobanoglu.airlinemanagement.service.PassengerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PassengerServiceImpl implements PassengerService {

    private final PassengerRepository passengerRepository;
    private final PassengerMapper passengerMapper;

    @Override
    public PassengerDTO createPassenger(PassengerDTO passengerDTO) {
        validateEmailUnique(passengerDTO.getEmail());

        Passenger passenger = passengerMapper.toEntity(passengerDTO);
        passenger.setLoyaltyPoints(normalizePoints(passenger.getLoyaltyPoints()));

        Passenger saved = passengerRepository.save(passenger);
        return passengerMapper.toDto(saved);
    }

    @Override
    public PassengerDTO updatePassenger(Long id, PassengerDTO passengerDTO) {
        Passenger existing = getPassengerOrThrow(id);

        if (isEmailChanged(existing, passengerDTO.getEmail())) {
            validateEmailUnique(passengerDTO.getEmail());
        }

        Passenger cleaned = normalizePassengerData(passengerDTO);
        boolean noChanges = hasNoChanges(existing, cleaned);

        if (noChanges) {
            throw new BadRequestException("No changes detected. Update operation skipped.");
        }

        applyUpdates(existing, cleaned);
        Passenger updated = passengerRepository.save(existing);
        return passengerMapper.toDto(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public PassengerDTO getPassengerById(Long id) {
        Passenger passenger = getPassengerOrThrow(id);
        return passengerMapper.toDto(passenger);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PassengerDTO> listPassengers(Pageable pageable) {
        return passengerRepository.findAll(pageable).map(passengerMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean findByEmailUnique(String email) {
        return !passengerRepository.existsByEmail(email);
    }

    @Override
    public void updateLoyaltyPoints(Long passengerId, int delta) {
        Passenger passenger = getPassengerOrThrow(passengerId);
        passenger.setLoyaltyPoints(normalizePoints(passenger.getLoyaltyPoints() + delta));
        passengerRepository.save(passenger);
    }

    // ---------- PRIVATE HELPERS ----------

    private Passenger getPassengerOrThrow(Long id) {
        return passengerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Passenger not found with id: " + id));
    }

    private void validateEmailUnique(String email) {
        if (passengerRepository.existsByEmail(email)) {
            throw new BadRequestException("Email already exists: " + email);
        }
    }

    private boolean isEmailChanged(Passenger existing, String newEmail) {
        return !existing.getEmail().equalsIgnoreCase(newEmail);
    }

    private Passenger normalizePassengerData(PassengerDTO dto) {
        Passenger p = new Passenger();
        p.setName(trimOrEmpty(dto.getName()));
        p.setSurname(trimOrEmpty(dto.getSurname()));
        p.setEmail(trimOrEmpty(dto.getEmail()));
        p.setLoyaltyPoints(normalizePoints(dto.getLoyaltyPoints()));
        return p;
    }

    private String trimOrEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    private int normalizePoints(int points) {
        return Math.max(points, 0);
    }

    private boolean hasNoChanges(Passenger existing, Passenger updated) {
        return existing.getName().equals(updated.getName()) &&
                existing.getSurname().equals(updated.getSurname()) &&
                existing.getEmail().equalsIgnoreCase(updated.getEmail()) &&
                existing.getLoyaltyPoints() == updated.getLoyaltyPoints();
    }

    private void applyUpdates(Passenger existing, Passenger updated) {
        existing.setName(updated.getName());
        existing.setSurname(updated.getSurname());
        existing.setEmail(updated.getEmail());
        existing.setLoyaltyPoints(updated.getLoyaltyPoints());
    }
}
