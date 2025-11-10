package com.cobanoglu.airlinemanagement.controller;

import com.cobanoglu.airlinemanagement.dto.PassengerBookingDTO;
import com.cobanoglu.airlinemanagement.dto.PassengerDTO;
import com.cobanoglu.airlinemanagement.service.BookingService;
import com.cobanoglu.airlinemanagement.service.PassengerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/passengers")
@RequiredArgsConstructor
@Tag(name = "Passengers", description = "Passenger management (Admin only, except /me/bookings)")
public class PassengerController {

    private final PassengerService passengerService;
    private final BookingService bookingService;

    @Operation(
            summary = "Create a new passenger",
            description = "Creates a new passenger record. Admin access only."
    )
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<PassengerDTO> createPassenger(@Valid @RequestBody PassengerDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(passengerService.createPassenger(dto));
    }

    @Operation(
            summary = "Update passenger information",
            description = "Updates an existing passenger's details by ID. Admin access only."
    )
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<PassengerDTO> updatePassenger(
            @PathVariable Long id,
            @Valid @RequestBody PassengerDTO dto
    ) {
        return ResponseEntity.ok(passengerService.updatePassenger(id, dto));
    }

    @Operation(
            summary = "List all passengers",
            description = "Returns a paginated list of all passengers. Admin access only."
    )
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<Page<PassengerDTO>> listPassengers(Pageable pageable) {
        return ResponseEntity.ok(passengerService.listPassengers(pageable));
    }

    @Operation(
            summary = "Check if email is unique.",
            description = "Verifies whether the given email address is not already registered to any passenger. Admin access only."
    )
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/check-email")
    public ResponseEntity<Boolean> isEmailUnique(@RequestParam String email) {
        boolean unique = passengerService.findByEmailUnique(email);
        return ResponseEntity.ok(unique);
    }

    @Operation(
            summary = "Get passenger by ID",
            description = "Retrieves passenger details by passenger ID. Admin access only."
    )
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id:\\d+}")
    public ResponseEntity<PassengerDTO> getPassengerById(@PathVariable Long id) {
        return ResponseEntity.ok(passengerService.getPassengerById(id));
    }

    @Operation(
            summary = "Update loyalty points",
            description = "Increases or decreases the passenger’s loyalty points by the given delta value. Admin access only."
    )
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/loyalty")
    public ResponseEntity<Void> updateLoyaltyPoints(@PathVariable Long id, @RequestParam int delta) {
        passengerService.updateLoyaltyPoints(id, delta);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Get passenger booking history",
            description = "Returns a list of all bookings made by a specific passenger. Accessible by Admin and User."
    )
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @GetMapping("/{id}/bookings")
    public ResponseEntity<List<PassengerBookingDTO>> getPassengerBookingHistory(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.getBookingHistoryByPassenger(id));
    }
}
