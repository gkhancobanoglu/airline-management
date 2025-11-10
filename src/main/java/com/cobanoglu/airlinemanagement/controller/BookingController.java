package com.cobanoglu.airlinemanagement.controller;

import com.cobanoglu.airlinemanagement.dto.*;
import com.cobanoglu.airlinemanagement.service.BookingService;
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

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@Tag(name = "Bookings", description = "Manage flight bookings (Admin & User)")
public class BookingController {

    private final BookingService bookingService;

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @PostMapping
    @Operation(summary = "Create booking", description = "Creates a new booking for a flight and passenger.")
    public ResponseEntity<BookingResponse> createBooking(@Valid @RequestBody BookingCreateRequest request) {
        return ResponseEntity.ok(bookingService.createBooking(request));
    }

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel booking", description = "Cancels a booking and processes refund and loyalty updates.")
    public ResponseEntity<Void> cancelBooking(@PathVariable Long id) {
        bookingService.cancelBooking(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    @Operation(summary = "List all bookings (Admin only)", description = "Lists all bookings with paging support.")
    public ResponseEntity<Page<BookingAdminDTO>> listAllBookings(Pageable pageable) {
        return ResponseEntity.ok(bookingService.listAllBookings(pageable));
    }

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @GetMapping("/{id}")
    @Operation(summary = "Get booking by ID", description = "Retrieves booking details by ID (User can only view own).")
    public ResponseEntity<BookingDTO> getBookingById(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.getBookingById(id));
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/me")
    @Operation(summary = "Get my bookings", description = "Retrieves all bookings for the currently logged-in passenger.")
    public ResponseEntity<List<PassengerBookingDTO>> getMyBookings() {
        return ResponseEntity.ok(bookingService.getCurrentUserBookings());
    }
}
