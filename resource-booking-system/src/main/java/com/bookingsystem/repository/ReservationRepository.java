package com.bookingsystem.repository;

import com.bookingsystem.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    @Query("select r from Reservation r where r.resource.id = :resourceId and r.id <> :reservationId " +
            "and r.status <> :cancelledStatus and r.startTime < :newEnd and r.endTime > :newStart")
    List<Reservation> findOverlappingExcludingReservation(
            @Param("resourceId") Long resourceId,
            @Param("reservationId") Long reservationId,
            @Param("cancelledStatus") com.bookingsystem.entity.ReservationStatus cancelledStatus,
            @Param("newEnd") LocalDateTime newEnd,
            @Param("newStart") LocalDateTime newStart);

    boolean existsByResourceIdAndStatusNot(Long resourceId, com.bookingsystem.entity.ReservationStatus status);
}
