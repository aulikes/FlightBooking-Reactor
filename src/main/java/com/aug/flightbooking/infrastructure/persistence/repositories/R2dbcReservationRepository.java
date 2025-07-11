package com.aug.flightbooking.infrastructure.persistence.repositories;

import com.aug.flightbooking.infrastructure.persistence.entities.ReservationEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

import java.time.Instant;

public interface R2dbcReservationRepository extends ReactiveCrudRepository<ReservationEntity, Long> {

    /**
     * Equivale a la siguiente consulta SQL ->
     * SELECT * FROM reservations WHERE status = :status AND created_at < :threshold;
     */
    Flux<ReservationEntity> findByStatusAndCreatedAtBefore(String status, Instant threshold);
}

