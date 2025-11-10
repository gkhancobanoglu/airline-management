package com.cobanoglu.airlinemanagement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class AirlineDTO {

    private Long id;

    @NotBlank(message = "IATA code is required")
    @Size(min = 2, max = 2, message = "IATA code must be exactly 2 characters long")
    @Pattern(regexp = "^[A-Z0-9]+$", message = "IATA code must contain only uppercase letters or digits")
    private String codeIATA;

    @NotBlank(message = "ICAO code is required")
    @Size(min = 3, max = 3, message = "ICAO code must be exactly 3 characters long")
    @Pattern(regexp = "^[A-Z0-9]+$", message = "ICAO code must contain only uppercase letters or digits")
    private String codeICAO;

    @NotBlank(message = "Airline name is required")
    @Size(min = 2, max = 100, message = "Airline name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "Country is required")
    @Size(min = 2, max = 60, message = "Country name must be between 2 and 60 characters")
    @Pattern(
            regexp = "^[A-Za-zğüşöçıİĞÜŞÖÇ\\s'.-]+$",
            message = "Country name must contain only letters"
    )
    private String country;

    @NotBlank(message = "Fleet size is required")
    @Pattern(regexp = "^[0-9]+$", message = "Fleet size must be a numeric value")
    private String fleetSize;

    private List<Long> flightIds;
}
