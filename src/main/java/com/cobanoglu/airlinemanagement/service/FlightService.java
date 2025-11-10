package com.cobanoglu.airlinemanagement.service;

import com.cobanoglu.airlinemanagement.dto.FlightDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FlightService {

    FlightDTO createFlight(FlightDTO flightDTO);

    FlightDTO updateFlight(Long id, FlightDTO flightDTO);

    void deleteFlight(Long id);

    Page<FlightDTO> listFlights(Pageable pageable);

    FlightDTO getFlightById(Long id);

}
