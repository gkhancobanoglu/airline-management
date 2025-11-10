package com.cobanoglu.airlinemanagement.dto;

import com.cobanoglu.airlinemanagement.entity.BookingStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PassengerBookingDTO {
    private Long bookingId;
    private String flightNumber;
    private String origin;
    private String destination;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private BookingStatus bookingStatus;
    private String seatNumber;
    private BigDecimal price;
    private int loyaltyEarned;

}
