package com.cobanoglu.airlinemanagement.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class FlightDTO {

    private Long id;

    @NotBlank(message = "Flight number is required")
    @Pattern(
            regexp = "^[A-Z0-9]{3,8}$",
            message = "Flight number must contain only uppercase letters and digits (3–8 chars)"
    )
    private String flightNumber;

    @NotBlank(message = "Origin is required")
    @Size(min = 2, max = 60, message = "Origin must be between 2 and 60 characters")
    private String origin;

    @NotBlank(message = "Destination is required")
    @Size(min = 2, max = 60, message = "Destination must be between 2 and 60 characters")
    private String destination;

    @NotNull(message = "Departure time is required")
    @Future(message = "Departure time must be in the future")
    private LocalDateTime departureTime;

    @NotNull(message = "Arrival time is required")
    @Future(message = "Arrival time must be in the future")
    private LocalDateTime arrivalTime;

    @NotNull(message = "Base price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Base price must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Base price must be a valid decimal number (max 8 digits, 2 decimals)")
    private BigDecimal basePrice;

    @Min(value = 50, message = "Capacity must be at least 50")
    @Max(value = 400, message = "Capacity cannot exceed 400")
    private int capacity;

    @Min(value = 0, message = "Booked seats cannot be negative")
    private int bookedSeats = 0;

    @NotNull(message = "Airline ID is required")
    private Long airlineId;

    private String airlineName;
}
