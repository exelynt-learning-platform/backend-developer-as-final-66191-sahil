package com.bookingsystem.repository;

import com.bookingsystem.entity.Resource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import java.util.Optional;

public interface ResourceRepository extends JpaRepository<Resource, Long>, JpaSpecificationExecutor<Resource> {

	/**
	 * Locks the resource row while a reservation is being checked and created.
	 * This serializes concurrent booking attempts for the same resource.
	 */
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select r from Resource r where r.id = :id")
	Optional<Resource> findByIdForUpdate(@Param("id") Long id);
}
