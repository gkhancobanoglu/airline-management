package com.cobanoglu.airlinemanagement.mapper;

import com.cobanoglu.airlinemanagement.dto.PassengerBookingDTO;
import com.cobanoglu.airlinemanagement.entity.Booking;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;
import java.util.List;

@Mapper(componentModel = "spring")
public interface PassengerBookingMapper {

    @Mapping(target = "bookingId", source = "id")
    @Mapping(target = "flightNumber", source = "flight.flightNumber")
    @Mapping(target = "origin", source = "flight.origin")
    @Mapping(target = "destination", source = "flight.destination")
    @Mapping(target = "departureTime", source = "flight.departureTime")
    @Mapping(target = "arrivalTime", source = "flight.arrivalTime")
    @Mapping(target = "bookingStatus", source = "bookingStatus")
    @Mapping(target = "seatNumber", source = "seatNumber")
    @Mapping(target = "price", source = "price")
    @Mapping(target = "loyaltyEarned", expression = "java(calculateLoyalty(booking))")
    PassengerBookingDTO toDto(Booking booking);

    List<PassengerBookingDTO> toDtoList(List<Booking> bookings);

    default int calculateLoyalty(Booking booking) {
        if (booking.getPrice() == null) return 0;
        return booking.getPrice().multiply(BigDecimal.valueOf(0.10)).intValue();
    }
}
