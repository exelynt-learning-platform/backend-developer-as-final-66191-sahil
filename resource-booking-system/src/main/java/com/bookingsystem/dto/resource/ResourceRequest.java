package com.bookingsystem.dto.resource;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record ResourceRequest(
        @NotBlank(message = "Name is required") String name,
        @NotBlank(message = "Type is required") String type,
        String description,
        String location,
        @Positive(message = "Capacity must be a positive number") int capacity,
        Boolean available
) {
}
