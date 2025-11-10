package com.cobanoglu.airlinemanagement.mapper;

import com.cobanoglu.airlinemanagement.dto.BookingDTO;
import com.cobanoglu.airlinemanagement.entity.Booking;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BookingMapper {

    @Mapping(source = "id", target = "id")
    @Mapping(source = "flight.id", target = "flightId")
    @Mapping(source = "passenger.id", target = "passengerId")
    BookingDTO toDto(Booking booking);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "flightId", target = "flight.id")
    @Mapping(source = "passengerId", target = "passenger.id")
    Booking toEntity(BookingDTO dto);

    List<BookingDTO> toDtoList(List<Booking> bookings);
}
