package com.bookingsystem.service;

import com.bookingsystem.dto.reservation.ReservationRequest;
import com.bookingsystem.dto.reservation.ReservationResponse;
import com.bookingsystem.entity.Reservation;
import com.bookingsystem.entity.ReservationStatus;
import com.bookingsystem.entity.Resource;
import com.bookingsystem.entity.Role;
import com.bookingsystem.entity.User;
import com.bookingsystem.exception.BadRequestException;
import com.bookingsystem.exception.ReservationConflictException;
import com.bookingsystem.exception.ResourceNotFoundException;
import com.bookingsystem.repository.ReservationRepository;
import com.bookingsystem.repository.ReservationSpecification;
import com.bookingsystem.repository.ResourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ResourceRepository resourceRepository;

    /**
     * Creates a reservation. The owner is ALWAYS the authenticated principal - never read from
     * the request body - so a user can never create a reservation on someone else's behalf.
     */
    @Transactional
    public ReservationResponse create(ReservationRequest request, User principal) {
        if (!request.endTime().isAfter(request.startTime())) {
            throw new BadRequestException("endTime must be after startTime");
        }

        Resource resource = resourceRepository.findByIdForUpdate(request.resourceId())
            .orElseThrow(() -> new ResourceNotFoundException(
                "Resource to reserve not found with id: " + request.resourceId()));

        if (!resource.isAvailable()) {
            throw new BadRequestException("Resource '" + resource.getName() + "' is not currently available for booking");
        }

        var overlapping = reservationRepository.findByResourceIdAndStatusNotAndStartTimeLessThanAndEndTimeGreaterThan(
                resource.getId(), ReservationStatus.CANCELLED, request.endTime(), request.startTime());
        if (!overlapping.isEmpty()) {
            throw new ReservationConflictException(
                    "Resource '" + resource.getName() + "' is already booked for the requested time window");
        }

        Reservation reservation = Reservation.builder()
                .resource(resource)
                .user(principal)
                .startTime(request.startTime())
                .endTime(request.endTime())
                .status(ReservationStatus.PENDING)
                .price(request.price())
                .notes(request.notes())
                .build();

        return ReservationResponse.from(reservationRepository.save(reservation));
    }

    /**
     * ADMIN sees every reservation matching the filters; USER sees only their own,
     * regardless of what filters are supplied.
     */
    @Transactional(readOnly = true)
    public Page<ReservationResponse> list(User principal, ReservationStatus status,
                                           BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            throw new BadRequestException("minPrice must not be greater than maxPrice");
        }

        Long ownerFilter = principal.getRole() == Role.ADMIN ? null : principal.getId();
        var spec = ReservationSpecification.build(ownerFilter, status, minPrice, maxPrice);
        return reservationRepository.findAll(spec, pageable).map(ReservationResponse::from);
    }

    @Transactional(readOnly = true)
    public ReservationResponse getById(Long id, User principal) {
        Reservation reservation = findEntity(id);
        assertOwnerOrAdmin(reservation, principal);
        return ReservationResponse.from(reservation);
    }

    /** ADMIN-only: transition a reservation's status (e.g. PENDING -> CONFIRMED, or -> CANCELLED). */
    @Transactional
    public ReservationResponse updateStatus(Long id, ReservationStatus newStatus) {
        Reservation reservation = findEntity(id);
        reservation.setStatus(newStatus);
        return ReservationResponse.from(reservationRepository.save(reservation));
    }

    /** ADMIN-only: full update of a reservation's details. */
    @Transactional
    public ReservationResponse update(Long id, ReservationRequest request) {
        Reservation reservation = findEntity(id);

        if (!request.endTime().isAfter(request.startTime())) {
            throw new BadRequestException("endTime must be after startTime");
        }

        Resource resource = resourceRepository.findById(request.resourceId())
            .orElseThrow(() -> new ResourceNotFoundException(
                "Replacement resource not found with id: " + request.resourceId()));

        reservation.setResource(resource);
        reservation.setStartTime(request.startTime());
        reservation.setEndTime(request.endTime());
        reservation.setPrice(request.price());
        reservation.setNotes(request.notes());

        return ReservationResponse.from(reservationRepository.save(reservation));
    }

    /** Owner may cancel their own PENDING/CONFIRMED reservation; ADMIN may cancel any. */
    @Transactional
    public ReservationResponse cancel(Long id, User principal) {
        Reservation reservation = findEntity(id);
        assertOwnerOrAdmin(reservation, principal);

        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new BadRequestException("Reservation is already cancelled");
        }

        reservation.setStatus(ReservationStatus.CANCELLED);
        return ReservationResponse.from(reservationRepository.save(reservation));
    }

    /** ADMIN-only: permanently delete a reservation. */
    @Transactional
    public void delete(Long id) {
        Reservation reservation = findEntity(id);
        reservationRepository.delete(reservation);
    }

    private void assertOwnerOrAdmin(Reservation reservation, User principal) {
        boolean isAdmin = principal.getRole() == Role.ADMIN;
        boolean isOwner = reservation.getUser().getId().equals(principal.getId());
        if (!isAdmin && !isOwner) {
            throw new AccessDeniedException("You may only access your own reservations");
        }
    }

    private Reservation findEntity(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found with id: " + id));
    }
}
