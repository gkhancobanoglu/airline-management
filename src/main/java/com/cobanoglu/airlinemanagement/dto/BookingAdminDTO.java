package com.cobanoglu.airlinemanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BookingAdminDTO {
    private Long id;
    private String flightNumber;
    private String origin;
    private String destination;
    private String passengerName;
    private String passengerEmail;
    private String seatNumber;
    private String bookingStatus;
    private BigDecimal price;
}
