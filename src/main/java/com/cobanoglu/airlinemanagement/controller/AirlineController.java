package com.cobanoglu.airlinemanagement.controller;

import com.cobanoglu.airlinemanagement.dto.AirlineDTO;
import com.cobanoglu.airlinemanagement.service.AirlineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
@RequestMapping("/api/airlines")
@RequiredArgsConstructor
@Tag(name = "Airlines", description = "Endpoints for creating, updating, deleting, and viewing airline records")
public class AirlineController {

    private final AirlineService airlineService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @Operation(
            summary = "Create a new airline (Admin only)",
            description = "Creates a new airline record in the system. Only accessible by users with ADMIN role."
    )
    public ResponseEntity<AirlineDTO> createAirline(@Valid @RequestBody AirlineDTO dto) {
        AirlineDTO created = airlineService.createAirline(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    @Operation(
            summary = "Update airline by ID (Admin only)",
            description = "Updates the details of an existing airline by its ID. Only accessible by ADMIN users."
    )
    public ResponseEntity<AirlineDTO> updateAirline(
            @PathVariable Long id,
            @Valid @RequestBody AirlineDTO dto) {
        AirlineDTO updated = airlineService.updateAirline(id, dto);
        return ResponseEntity.ok(updated);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete airline by ID (Admin only, if no flights linked)",
            description = "Deletes an airline record if it has no associated flights. Only accessible by ADMIN users."
    )
    public ResponseEntity<Void> deleteAirline(@PathVariable Long id) {
        airlineService.deleteAirline(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping
    @Operation(
            summary = "List all airlines with pagination (Admin & User)",
            description = "Retrieves all airlines in a paginated format. Accessible by ADMIN and USER roles."
    )
    public ResponseEntity<Page<AirlineDTO>> listAirlines(
            @Parameter(description = "Pagination parameters: page, size, sort") Pageable pageable) {
        Page<AirlineDTO> result = airlineService.listAirlines(pageable);
        return ResponseEntity.ok(result);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping("/{id}")
    @Operation(
            summary = "Get airline by ID (Admin & User)",
            description = "Retrieves detailed information about a specific airline by its ID. Accessible by ADMIN and USER roles."
    )
    public ResponseEntity<AirlineDTO> getAirlineById(@PathVariable Long id) {
        AirlineDTO dto = airlineService.getAirlineById(id);
        return ResponseEntity.ok(dto);
    }
}
