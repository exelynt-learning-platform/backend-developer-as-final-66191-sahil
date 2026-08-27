package com.bookingsystem.repository;

import com.bookingsystem.entity.Resource;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

/**
 * Builds dynamic JPA Specifications for filtering resources by type and availability.
 */
public final class ResourceSpecification {

    private ResourceSpecification() {
    }

    public static Specification<Resource> build(String type, Boolean available) {
        return (root, query, cb) -> {
            Predicate predicates = cb.conjunction();

            if (type != null && !type.isBlank()) {
                predicates = cb.and(predicates, cb.equal(cb.upper(root.get("type")), type.toUpperCase()));
            }
            if (available != null) {
                predicates = cb.and(predicates, cb.equal(root.get("available"), available));
            }
            return predicates;
        };
    }
}