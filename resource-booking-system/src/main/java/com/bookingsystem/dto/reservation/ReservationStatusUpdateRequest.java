package com.bookingsystem.dto.reservation;

import com.bookingsystem.entity.ReservationStatus;
import jakarta.validation.constraints.NotNull;

public record ReservationStatusUpdateRequest(
        @NotNull(message = "status is required") ReservationStatus status
) {
}
