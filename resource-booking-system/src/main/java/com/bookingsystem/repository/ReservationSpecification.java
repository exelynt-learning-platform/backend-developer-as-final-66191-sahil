package com.bookingsystem.repository;

import com.bookingsystem.entity.Reservation;
import com.bookingsystem.entity.ReservationStatus;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Builds dynamic JPA Specifications for filtering reservations by owner, status and price range.
 */
public final class ReservationSpecification {

    private ReservationSpecification() {
    }

    public static Specification<Reservation> build(Long ownerUserId,
                                                     ReservationStatus status,
                                                     BigDecimal minPrice,
                                                     BigDecimal maxPrice) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (ownerUserId != null) {
                predicates.add(cb.equal(root.get("user").get("id"), ownerUserId));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), minPrice));
            }
            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
