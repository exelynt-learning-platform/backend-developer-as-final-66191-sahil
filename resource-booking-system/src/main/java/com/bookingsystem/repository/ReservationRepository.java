package com.bookingsystem.repository;

import com.bookingsystem.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long>, JpaSpecificationExecutor<Reservation> {

    /**
     * Finds reservations for a given resource that overlap a given time window and are not cancelled.
     * Used to prevent double-booking. Overlap condition: existing.start < newEnd AND existing.end > newStart
     */
    List<Reservation> findByResourceIdAndStatusNotAndStartTimeLessThanAndEndTimeGreaterThan(
            Long resourceId, com.bookingsystem.entity.ReservationStatus status,
            LocalDateTime newEnd, LocalDateTime newStart);
}
