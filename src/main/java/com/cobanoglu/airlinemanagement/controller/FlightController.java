package com.cobanoglu.airlinemanagement.controller;

import com.cobanoglu.airlinemanagement.dto.FlightDTO;
import com.cobanoglu.airlinemanagement.service.FlightService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/flights")
@RequiredArgsConstructor
@Tag(name = "Flights", description = "Endpoints for creating, updating, deleting, and viewing flight records")
public class FlightController {

    private final FlightService flightService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @Operation(
            summary = "Create a new flight (Admin only)",
            description = "Creates a new flight. Validates capacity (50–400), ensures future departure/arrival, and prevents duplicates for the same day and flight number."
    )
    public ResponseEntity<FlightDTO> createFlight(@Valid @RequestBody FlightDTO dto) {
        FlightDTO created = flightService.createFlight(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    @Operation(
            summary = "Update flight details (Admin only)",
            description = "Updates existing flight details like origin, destination, times, base price, and capacity. Validates data consistency."
    )
    public ResponseEntity<FlightDTO> updateFlight(
            @PathVariable Long id,
            @Valid @RequestBody FlightDTO dto) {
        FlightDTO updated = flightService.updateFlight(id, dto);
        return ResponseEntity.ok(updated);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete flight by ID (Admin only)",
            description = "Deletes a flight if no bookings are linked to it."
    )
    public ResponseEntity<Void> deleteFlight(@PathVariable Long id) {
        flightService.deleteFlight(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping
    @Operation(
            summary = "List all flights (Admin & User)",
            description = "Retrieves all flights in paginated format. Accessible by ADMIN and USER roles."
    )
    public ResponseEntity<Page<FlightDTO>> listFlights(
            @Parameter(description = "Pagination parameters: page, size, sort") Pageable pageable) {
        Page<FlightDTO> flights = flightService.listFlights(pageable);
        return ResponseEntity.ok(flights);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping("/{id}")
    @Operation(
            summary = "Get flight by ID (Admin & User)",
            description = "Retrieves detailed information about a specific flight by its ID."
    )
    public ResponseEntity<FlightDTO> getFlightById(@PathVariable Long id) {
        FlightDTO flight = flightService.getFlightById(id);
        return ResponseEntity.ok(flight);
    }
}

