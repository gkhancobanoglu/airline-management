package com.cobanoglu.airlinemanagement.service.impl;

import com.cobanoglu.airlinemanagement.dto.AirlineDTO;
import com.cobanoglu.airlinemanagement.entity.Airline;
import com.cobanoglu.airlinemanagement.exception.BadRequestException;
import com.cobanoglu.airlinemanagement.exception.NotFoundException;
import com.cobanoglu.airlinemanagement.mapper.AirlineMapper;
import com.cobanoglu.airlinemanagement.repository.AirlineRepository;
import com.cobanoglu.airlinemanagement.repository.BookingRepository;
import com.cobanoglu.airlinemanagement.repository.FlightRepository;
import com.cobanoglu.airlinemanagement.service.AirlineService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AirlineServiceImpl implements AirlineService {

    private final AirlineRepository airlineRepository;
    private final FlightRepository flightRepository;
    private final BookingRepository bookingRepository;
    private final AirlineMapper airlineMapper;

    @Override
    public AirlineDTO createAirline(AirlineDTO dto) {
        normalizeCodes(dto);
        validateUniqueCodes(dto, null);

        Airline airline = airlineMapper.toEntity(dto);
        Airline saved = airlineRepository.save(airline);
        return airlineMapper.toDto(saved);
    }

    @Override
    public AirlineDTO updateAirline(Long id, AirlineDTO dto) {
        Airline existing = getAirlineOrThrow(id);

        normalizeCodes(dto);
        validateUniqueCodes(dto, id);

        if (isNoChange(existing, dto)) {
            throw new BadRequestException("No changes detected. Update operation skipped.");
        }

        updateExistingAirline(existing, dto);
        Airline updated = airlineRepository.save(existing);
        return airlineMapper.toDto(updated);
    }

    @Override
    public void deleteAirline(Long id) {
        Airline airline = getAirlineOrThrow(id);

        if (bookingRepository.existsByFlight_Airline_Id(id)) {
            throw new BadRequestException("Airline cannot be deleted: There are passengers booked on its flights.");
        }

        flightRepository.deleteAll(flightRepository.findByAirlineId(id));
        airlineRepository.delete(airline);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AirlineDTO> listAirlines(Pageable pageable) {
        return airlineRepository.findAll(pageable).map(airlineMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public AirlineDTO getAirlineById(Long id) {
        Airline airline = getAirlineOrThrow(id);
        return airlineMapper.toDto(airline);
    }

    private Airline getAirlineOrThrow(Long id) {
        return airlineRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Airline not found with id: " + id));
    }

    private void normalizeCodes(AirlineDTO dto) {
        dto.setCodeIATA(dto.getCodeIATA().trim().toUpperCase());
        dto.setCodeICAO(dto.getCodeICAO().trim().toUpperCase());
        dto.setName(dto.getName().trim());
        dto.setCountry(dto.getCountry().trim());
        dto.setFleetSize(dto.getFleetSize().trim());
    }

    private void validateUniqueCodes(AirlineDTO dto, Long excludeId) {
        if (excludeId == null) {
            if (airlineRepository.existsByCodeIATA(dto.getCodeIATA())) {
                throw new BadRequestException("An airline with this IATA code already exists.");
            }
            if (airlineRepository.existsByCodeICAO(dto.getCodeICAO())) {
                throw new BadRequestException("An airline with this ICAO code already exists.");
            }
        } else {
            if (airlineRepository.existsByCodeIATAAndIdNot(dto.getCodeIATA(), excludeId)) {
                throw new BadRequestException("Another airline already uses this IATA code.");
            }
            if (airlineRepository.existsByCodeICAOAndIdNot(dto.getCodeICAO(), excludeId)) {
                throw new BadRequestException("Another airline already uses this ICAO code.");
            }
        }
    }

    private boolean isNoChange(Airline existing, AirlineDTO dto) {
        return existing.getName().equals(dto.getName()) &&
                existing.getCountry().equals(dto.getCountry()) &&
                existing.getFleetSize().equals(dto.getFleetSize()) &&
                existing.getCodeIATA().equalsIgnoreCase(dto.getCodeIATA()) &&
                existing.getCodeICAO().equalsIgnoreCase(dto.getCodeICAO());
    }

    private void updateExistingAirline(Airline existing, AirlineDTO dto) {
        existing.setName(dto.getName());
        existing.setCountry(dto.getCountry());
        existing.setFleetSize(dto.getFleetSize());
        existing.setCodeIATA(dto.getCodeIATA());
        existing.setCodeICAO(dto.getCodeICAO());
    }
}
