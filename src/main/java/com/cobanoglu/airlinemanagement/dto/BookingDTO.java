package com.cobanoglu.airlinemanagement.dto;

import com.cobanoglu.airlinemanagement.entity.BookingStatus;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BookingDTO {

    private Long id;

    @NotNull(message = "Flight ID is required")
    @Positive(message = "Flight ID must be a positive number")
    private Long flightId;

    @NotNull(message = "Passenger ID is required")
    @Positive(message = "Passenger ID must be a positive number")
    private Long passengerId;

    @NotBlank(message = "Seat number is required")
    @Pattern(regexp = "^[A-Z0-9]{1,5}$", message = "Seat number must contain only letters and digits (max 5 chars)")
    private String seatNumber;

    @NotNull(message = "Booking status is required")
    private BookingStatus bookingStatus;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Price must be a valid decimal number (max 8 digits and 2 decimals)")
    private BigDecimal price;
}
