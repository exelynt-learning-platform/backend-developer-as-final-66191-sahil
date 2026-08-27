package com.bookingsystem.controller;

import com.bookingsystem.dto.reservation.ReservationRequest;
import com.bookingsystem.dto.reservation.ReservationResponse;
import com.bookingsystem.dto.reservation.ReservationStatusUpdateRequest;
import com.bookingsystem.entity.ReservationStatus;
import com.bookingsystem.entity.User;
import com.bookingsystem.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
@Tag(name = "Reservations", description = "Bookings for resources. USER sees/manages only their own; ADMIN has full access.")
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    @Operation(summary = "Create a reservation. Owner is always taken from the JWT, never from the request body.")
    public ResponseEntity<ReservationResponse> create(@Valid @RequestBody ReservationRequest request,
                                                        @AuthenticationPrincipal User principal) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reservationService.create(request, principal));
    }

    @GetMapping
    @Operation(summary = "List reservations. ADMIN sees all; USER sees only their own. " +
            "Supports filtering by status/minPrice/maxPrice, pagination and sorting. " +
            "Defaults to 20 results per page sorted by id ascending.")
    public ResponseEntity<Page<ReservationResponse>> list(
            // The service uses the principal's role to determine the query scope.
            @AuthenticationPrincipal User principal,
            @RequestParam(required = false) ReservationStatus status,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(reservationService.list(principal, status, minPrice, maxPrice, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a single reservation. Owner or ADMIN only.")
    public ResponseEntity<ReservationResponse> getById(@PathVariable Long id, @AuthenticationPrincipal User principal) {
        return ResponseEntity.ok(reservationService.getById(id, principal));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Fully update a reservation (ADMIN only)")
    public ResponseEntity<ReservationResponse> update(@PathVariable Long id,
                                                       @Valid @RequestBody ReservationRequest request,
                                                       @AuthenticationPrincipal User principal) {
        return ResponseEntity.ok(reservationService.update(id, request, principal));
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Change a reservation's status, e.g. PENDING -> CONFIRMED (ADMIN only)")
    public ResponseEntity<ReservationResponse> updateStatus(@PathVariable Long id,
                                                             @Valid @RequestBody ReservationStatusUpdateRequest request) {
        return ResponseEntity.ok(reservationService.updateStatus(id, request.status()));
    }

    @PutMapping("/{id}/cancel")
    @Operation(summary = "Cancel a reservation. Owner may cancel their own; ADMIN may cancel any.")
    public ResponseEntity<ReservationResponse> cancel(@PathVariable Long id, @AuthenticationPrincipal User principal) {
        return ResponseEntity.ok(reservationService.cancel(id, principal));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Permanently delete a reservation (ADMIN only)")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        reservationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
