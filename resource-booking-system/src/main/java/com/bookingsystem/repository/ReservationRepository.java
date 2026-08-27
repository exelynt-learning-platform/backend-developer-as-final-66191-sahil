package com.bookingsystem.repository;

import com.bookingsystem.entity.Reservation;
import com.bookingsystem.entity.ReservationStatus;
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
    @Query("select r from Reservation r where r.resource.id = :resourceId " +
            "and r.status <> :cancelledStatus and r.startTime < :newEnd and r.endTime > :newStart")
    List<Reservation> findActiveOverlappingReservations(
            @Param("resourceId") Long resourceId,
            @Param("cancelledStatus") ReservationStatus cancelledStatus,
            @Param("newEnd") LocalDateTime newEnd,
            @Param("newStart") LocalDateTime newStart);

    @Query("select r from Reservation r where r.resource.id = :resourceId and r.id <> :reservationId " +
            "and r.status <> :cancelledStatus and r.startTime < :newEnd and r.endTime > :newStart")
    List<Reservation> findOverlappingExcludingReservation(
            @Param("resourceId") Long resourceId,
            @Param("reservationId") Long reservationId,
            @Param("cancelledStatus") ReservationStatus cancelledStatus,
            @Param("newEnd") LocalDateTime newEnd,
            @Param("newStart") LocalDateTime newStart);

        boolean existsByResourceIdAndStatusNot(Long resourceId, ReservationStatus status);
}
