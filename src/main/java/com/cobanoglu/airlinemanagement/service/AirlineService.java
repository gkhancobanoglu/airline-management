package com.cobanoglu.airlinemanagement.service;

import com.cobanoglu.airlinemanagement.dto.AirlineDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AirlineService {

    AirlineDTO createAirline(AirlineDTO airlineDTO);

    AirlineDTO updateAirline(Long id, AirlineDTO airlineDTO);

    void deleteAirline(Long id);

    Page<AirlineDTO> listAirlines(Pageable pageable);

    AirlineDTO getAirlineById(Long id);
}
