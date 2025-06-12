package com.aug.flightbooking.infrastructure.persistence.repository;

import com.aug.flightbooking.infrastructure.persistence.entity.TicketEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface R2dbcTicketRepository extends ReactiveCrudRepository<TicketEntity, Long> {
    Mono<TicketEntity> findByReservationId(Long reservationId);
}