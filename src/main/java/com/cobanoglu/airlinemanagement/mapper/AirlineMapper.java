package com.cobanoglu.airlinemanagement.mapper;

import com.cobanoglu.airlinemanagement.dto.AirlineDTO;
import com.cobanoglu.airlinemanagement.entity.Airline;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AirlineMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(
            target = "flightIds",
            expression = "java(airline.getFlights() != null ? airline.getFlights().stream().map(f -> f.getId()).toList() : null)"
    )
    AirlineDTO toDto(Airline airline);

    Airline toEntity(AirlineDTO dto);

    List<AirlineDTO> toDtoList(List<Airline> airlines);
}
