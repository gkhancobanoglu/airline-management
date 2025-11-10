package com.cobanoglu.airlinemanagement.mapper;

import com.cobanoglu.airlinemanagement.dto.PassengerDTO;
import com.cobanoglu.airlinemanagement.entity.Passenger;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PassengerMapper {

    @Mapping(source = "id", target = "id")
    PassengerDTO toDto(Passenger passenger);

    @Mapping(source = "id", target = "id")
    Passenger toEntity(PassengerDTO passengerDTO);

    List<PassengerDTO> toDtoList(List<Passenger> passengers);
}
