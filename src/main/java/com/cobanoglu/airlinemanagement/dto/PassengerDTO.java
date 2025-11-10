package com.cobanoglu.airlinemanagement.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PassengerDTO {

    private Long id;

    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    @Pattern(
            regexp = "^[A-Za-zğüşöçıİĞÜŞÖÇ\\s'-]+$",
            message = "First name must contain only letters"
    )
    private String name;

    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    @Pattern(
            regexp = "^[A-Za-zğüşöçıİĞÜŞÖÇ\\s'-]+$",
            message = "Last name must contain only letters"
    )
    private String surname;

    @NotBlank(message = "Email is required")
    @Email(message = "Email format is invalid")
    private String email;

    private int loyaltyPoints = 0;
}
