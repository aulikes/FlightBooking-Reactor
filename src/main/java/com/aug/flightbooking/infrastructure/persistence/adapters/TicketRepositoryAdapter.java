package com.aug.flightbooking.infrastructure.persistence.adapters;

import com.aug.flightbooking.application.ports.out.TicketRepository;
import com.aug.flightbooking.domain.models.ticket.Ticket;
import com.aug.flightbooking.infrastructure.persistence.entities.TicketEntity;
import com.aug.flightbooking.infrastructure.persistence.mappers.TicketPersistenceMapper;
import com.aug.flightbooking.infrastructure.persistence.repositories.R2dbcTicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Adaptador que implementa el puerto TicketRepository usando R2DBC.
 */
@Component
@RequiredArgsConstructor
public class TicketRepositoryAdapter implements TicketRepository {

    private final R2dbcTicketRepository r2dbcRepository;

    @Override
    public Mono<Ticket> save(Ticket ticket) {
        TicketEntity entity = TicketPersistenceMapper.toEntity(ticket);
        return r2dbcRepository.save(entity)
            .map(TicketPersistenceMapper::toDomain);
    }

    @Override
    public Mono<Ticket> findById (Long id) {
        return r2dbcRepository.findById(id)
                .map(TicketPersistenceMapper::toDomain);
    }

    @Override
    public Mono<Ticket> findByReservationId (Long id) {
        return r2dbcRepository.findByReservationId(id)
                .map(TicketPersistenceMapper::toDomain);
    }
}