package com.cobanoglu.airlinemanagement.dto;

import com.cobanoglu.airlinemanagement.entity.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingResponse {
    private Long bookingId;
    private BookingStatus status;
    private BigDecimal finalPrice;
    private String message;
}
