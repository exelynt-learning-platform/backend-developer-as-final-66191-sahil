package com.bookingsystem.controller;

import com.bookingsystem.dto.resource.ResourceRequest;
import com.bookingsystem.dto.resource.ResourceResponse;
import com.bookingsystem.service.ResourceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/resources")
@RequiredArgsConstructor
@Tag(name = "Resources", description = "Bookable resources: rooms, vehicles, equipment. Read: ADMIN & USER. Write: ADMIN only.")
public class ResourceController {

    private final ResourceService resourceService;

    @GetMapping
    @Operation(summary = "List resources (paginated, optionally filtered by type/availability)")
    public ResponseEntity<Page<ResourceResponse>> list(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Boolean available,
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(resourceService.list(type, available, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a single resource by id")
    public ResponseEntity<ResourceResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(resourceService.getById(id));
    }

    @PostMapping
    @Operation(summary = "Create a new resource (ADMIN only)")
    public ResponseEntity<ResourceResponse> create(@Valid @RequestBody ResourceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(resourceService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing resource (ADMIN only)")
    public ResponseEntity<ResourceResponse> update(@PathVariable Long id, @Valid @RequestBody ResourceRequest request) {
        return ResponseEntity.ok(resourceService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a resource (ADMIN only)")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        resourceService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
