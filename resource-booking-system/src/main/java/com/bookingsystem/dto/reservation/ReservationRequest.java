package com.bookingsystem.dto.reservation;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Note: there is intentionally no userId field here. The reservation owner is always
 * derived from the authenticated JWT principal in the service layer, never from client input.
 */
public record ReservationRequest(
        @NotNull(message = "resourceId is required") Long resourceId,

        @NotNull(message = "startTime is required")
        @Future(message = "startTime must be in the future")
        LocalDateTime startTime,

        @NotNull(message = "endTime is required")
        @Future(message = "endTime must be in the future")
        LocalDateTime endTime,

        @NotNull(message = "price is required")
        @DecimalMin(value = "0.0", inclusive = true, message = "price must not be negative")
        BigDecimal price,

        @Size(max = 1000, message = "notes must not exceed 1000 characters")
        String notes
) {
}
