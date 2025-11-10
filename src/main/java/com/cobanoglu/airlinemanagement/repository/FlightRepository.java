package com.cobanoglu.airlinemanagement.repository;

import com.cobanoglu.airlinemanagement.entity.Flight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FlightRepository extends JpaRepository<Flight, Long> {

    List<Flight> findByAirlineId(Long airlineId);

    boolean existsByFlightNumberAndAirlineIdAndOriginAndDepartureTimeBetween(
            String flightNumber,
            Long airlineId,
            String origin,
            LocalDateTime startOfDay,
            LocalDateTime endOfDay
    );

    boolean existsByFlightNumberAndAirlineIdAndOriginAndDepartureTimeBetweenAndIdNot(
            String flightNumber,
            Long airlineId,
            String origin,
            LocalDateTime startOfDay,
            LocalDateTime endOfDay,
            Long id
    );

    boolean existsByFlightNumberAndAirlineIdAndDepartureTimeLessThanAndArrivalTimeGreaterThan(
            String flightNumber,
            Long airlineId,
            LocalDateTime arrivalTime,
            LocalDateTime departureTime
    );

    boolean existsByFlightNumberAndAirlineIdAndDepartureTimeLessThanAndArrivalTimeGreaterThanAndIdNot(
            String flightNumber,
            Long airlineId,
            LocalDateTime arrivalTime,
            LocalDateTime departureTime,
            Long id
    );

}
