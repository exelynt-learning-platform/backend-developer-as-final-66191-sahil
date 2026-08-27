package com.bookingsystem.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "resource")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Resource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    /** e.g. ROOM, VEHICLE, EQUIPMENT */
    @Column(nullable = false, length = 50)
    private String type;

    @Column(length = 1000)
    private String description;

    private String location;

    @Column(nullable = false)
    private int capacity;

    @Builder.Default
    @Column(nullable = false)
    private boolean available = true;

    @Builder.Default
    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
}
