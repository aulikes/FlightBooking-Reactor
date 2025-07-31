package com.aug.flightbooking.infrastructure.persistence.repositories;

import com.aug.flightbooking.infrastructure.persistence.entities.ReservationEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface R2dbcReservationRepository extends ReactiveCrudRepository<ReservationEntity, Long> {

    /**
     * Equivale a la siguiente consulta SQL ->
     * SELECT * FROM reservations WHERE status = :status AND created_at < :threshold;
     */
    Flux<ReservationEntity> findByStatusAndCreatedAtBefore(String status, Instant threshold);

    @Query("SELECT * FROM reservation WHERE status IN (:statuses) AND created_at < :threshold")
    Flux<ReservationEntity> findByStatusesAtBefore(
            @Param("statuses") List<String> statuses,
            @Param("threshold") Instant threshold
    );

}

