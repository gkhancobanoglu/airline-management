package com.cobanoglu.airlinemanagement.mapper;

import com.cobanoglu.airlinemanagement.dto.FlightDTO;
import com.cobanoglu.airlinemanagement.entity.Flight;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface FlightMapper {

    @Mapping(source = "id", target = "id")
    @Mapping(source = "airline.id", target = "airlineId")
    @Mapping(source = "airline.name", target = "airlineName")
    FlightDTO toDto(Flight flight);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "airlineId", target = "airline.id")
    Flight toEntity(FlightDTO flightDTO);

    List<FlightDTO> toDtoList(List<Flight> flights);
}
