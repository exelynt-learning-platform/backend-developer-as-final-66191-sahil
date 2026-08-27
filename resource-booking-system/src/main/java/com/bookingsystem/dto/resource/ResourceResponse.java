package com.bookingsystem.dto.resource;

import com.bookingsystem.entity.Resource;

import java.time.Instant;

public record ResourceResponse(
        Long id,
        String name,
        String type,
        String description,
        String location,
        int capacity,
        boolean available,
        Instant createdAt
) {
    public static ResourceResponse from(Resource r) {
        return new ResourceResponse(
                r.getId(), r.getName(), r.getType(), r.getDescription(),
                r.getLocation(), r.getCapacity(), r.isAvailable(), r.getCreatedAt()
        );
    }
}
