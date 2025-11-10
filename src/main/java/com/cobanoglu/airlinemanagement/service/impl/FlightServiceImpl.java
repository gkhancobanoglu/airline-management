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
import com.cobanoglu.airlinemanagement.service.FlightService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class FlightServiceImpl implements FlightService {

    private final FlightRepository flightRepository;
    private final AirlineRepository airlineRepository;
    private final BookingRepository bookingRepository;
    private final FlightMapper flightMapper;

    @Override
    public FlightDTO createFlight(FlightDTO dto) {
        validateCapacityRange(dto.getCapacity());
        validateDates(dto.getDepartureTime(), dto.getArrivalTime());

        if (dto.getBookedSeats() > dto.getCapacity()) {
            throw new BadRequestException("Booked seats cannot exceed capacity");
        }

        Airline airline = getAirlineOrThrow(dto.getAirlineId());
        validateDuplicateFlight(dto, dto.getAirlineId(), null);
        validateTimeConflict(dto, dto.getAirlineId(), null);

        Flight flight = flightMapper.toEntity(dto);
        flight.setAirline(airline);
        flight.setBookedSeats(Math.max(dto.getBookedSeats(), 0));

        Flight saved = flightRepository.save(flight);
        return flightMapper.toDto(saved);
    }

    @Override
    public FlightDTO updateFlight(Long id, FlightDTO dto) {
        Flight existing = getFlightOrThrow(id);
        validateCapacityRange(dto.getCapacity());
        validateDates(dto.getDepartureTime(), dto.getArrivalTime());

        validateDuplicateFlight(dto, existing.getAirline().getId(), id);
        validateTimeConflict(dto, existing.getAirline().getId(), id);

        if (isNoChange(existing, dto)) {
            throw new BadRequestException("No changes detected. Update operation skipped.");
        }

        updateFlightFields(existing, dto);

        Flight updated = flightRepository.save(existing);
        return flightMapper.toDto(updated);
    }

    @Override
    public void deleteFlight(Long id) {
        Flight flight = getFlightOrThrow(id);

        if (bookingRepository.existsByFlight_Id(id)) {
            throw new BadRequestException("Cannot delete flight with existing bookings.");
        }

        flightRepository.delete(flight);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FlightDTO> listFlights(Pageable pageable) {
        return flightRepository.findAll(pageable).map(flightMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public FlightDTO getFlightById(Long id) {
        Flight flight = getFlightOrThrow(id);
        return flightMapper.toDto(flight);
    }

    private Airline getAirlineOrThrow(Long airlineId) {
        return airlineRepository.findById(airlineId)
                .orElseThrow(() -> new NotFoundException("Airline not found with id: " + airlineId));
    }

    private Flight getFlightOrThrow(Long id) {
        return flightRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Flight not found with id: " + id));
    }

    private void validateCapacityRange(int capacity) {
        if (capacity < 50 || capacity > 400) {
            throw new BadRequestException("Flight capacity must be between 50 and 400.");
        }
    }

    private void validateDates(LocalDateTime departure, LocalDateTime arrival) {
        if (departure == null || arrival == null) {
            throw new BadRequestException("Departure and arrival times are required.");
        }
        if (!arrival.isAfter(departure)) {
            throw new BadRequestException("Arrival time must be after departure time.");
        }
    }

    private void validateDuplicateFlight(FlightDTO dto, Long airlineId, Long excludeId) {
        LocalDate day = dto.getDepartureTime().toLocalDate();
        LocalDateTime start = day.atStartOfDay();
        LocalDateTime end = day.plusDays(1).atStartOfDay().minusSeconds(1);

        boolean exists = (excludeId == null)
                ? flightRepository.existsByFlightNumberAndAirlineIdAndOriginAndDepartureTimeBetween(
                dto.getFlightNumber(), airlineId, dto.getOrigin(), start, end)
                : flightRepository.existsByFlightNumberAndAirlineIdAndOriginAndDepartureTimeBetweenAndIdNot(
                dto.getFlightNumber(), airlineId, dto.getOrigin(), start, end, excludeId);

        if (exists) {
            throw new FlightConflictException("A flight with the same number and origin already exists for this airline on that date.");
        }
    }

    private void validateTimeConflict(FlightDTO dto, Long airlineId, Long excludeId) {
        boolean conflict = (excludeId == null)
                ? flightRepository.existsByFlightNumberAndAirlineIdAndDepartureTimeLessThanAndArrivalTimeGreaterThan(
                dto.getFlightNumber(), airlineId, dto.getArrivalTime(), dto.getDepartureTime())
                : flightRepository.existsByFlightNumberAndAirlineIdAndDepartureTimeLessThanAndArrivalTimeGreaterThanAndIdNot(
                dto.getFlightNumber(), airlineId, dto.getArrivalTime(), dto.getDepartureTime(), excludeId);

        if (conflict) {
            throw new FlightConflictException("Time conflict detected: This aircraft (flight number) is already scheduled in the same time range.");
        }
    }

    private boolean isNoChange(Flight existing, FlightDTO dto) {
        return Objects.equals(existing.getFlightNumber(), dto.getFlightNumber().trim().toUpperCase())
                && Objects.equals(existing.getOrigin(), dto.getOrigin().trim())
                && Objects.equals(existing.getDestination(), dto.getDestination().trim())
                && Objects.equals(existing.getDepartureTime().withNano(0), dto.getDepartureTime().withNano(0))
                && Objects.equals(existing.getArrivalTime().withNano(0), dto.getArrivalTime().withNano(0))
                && Objects.equals(existing.getBasePrice(), dto.getBasePrice())
                && existing.getCapacity() == dto.getCapacity()
                && Objects.equals(existing.getAirline().getId(), dto.getAirlineId());
    }

    private void updateFlightFields(Flight existing, FlightDTO dto) {
        existing.setFlightNumber(dto.getFlightNumber().trim().toUpperCase());
        existing.setOrigin(dto.getOrigin().trim());
        existing.setDestination(dto.getDestination().trim());
        existing.setDepartureTime(dto.getDepartureTime().withNano(0));
        existing.setArrivalTime(dto.getArrivalTime().withNano(0));
        existing.setBasePrice(dto.getBasePrice());
        existing.setCapacity(dto.getCapacity());

        if (!Objects.equals(existing.getAirline().getId(), dto.getAirlineId())) {
            Airline newAirline = getAirlineOrThrow(dto.getAirlineId());
            existing.setAirline(newAirline);
        }
    }
}
