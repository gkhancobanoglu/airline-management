package com.cobanoglu.airlinemanagement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BookingCreateRequest {

    @NotNull(message = " Flight ID is required")
    private Long flightId;

    private Long passengerId;

    @NotBlank(message = "Seat number cannot be blank")
    private String seatNumber;
}
