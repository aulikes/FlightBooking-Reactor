package com.aug.flightbooking.infrastructure.persistence.adapters;

import com.aug.flightbooking.application.ports.out.ReservationRepository;
import com.aug.flightbooking.domain.models.reservation.Reservation;
import com.aug.flightbooking.domain.models.reservation.ReservationStatus;
import com.aug.flightbooking.infrastructure.persistence.entities.ReservationEntity;
import com.aug.flightbooking.infrastructure.persistence.mappers.ReservationMapper;
import com.aug.flightbooking.infrastructure.persistence.repositories.R2dbcReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class ReservationRepositoryAdapter implements ReservationRepository {

    private final R2dbcReservationRepository repository;

    @Override
    public Mono<Reservation> save(Reservation reservation) {
        ReservationEntity entity = ReservationMapper.toEntity(reservation);
        return repository.save(entity)
                .map(ReservationMapper::toDomain);
    }

    @Override
    public Mono<Reservation> findById(Long id) {
        return repository.findById(id)
                .map(ReservationMapper::toDomain);
    }

    @Override
    public Flux<Reservation> findReservationsCreatedBefore(Instant threshold) {
        return repository.findByStatusAndCreatedAtBefore(ReservationStatus.CREATED.name(), threshold)
                .map(ReservationMapper::toDomain);
    }
}
