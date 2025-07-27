package com.aug.flightbooking.infrastructure.persistence.mappers;

import com.aug.flightbooking.domain.models.ticket.Ticket;
import com.aug.flightbooking.domain.models.ticket.TicketStatus;
import com.aug.flightbooking.infrastructure.persistence.entities.TicketEntity;

/**
 * Mapper responsable de convertir entre la entidad de dominio Ticket
 * y la entidad de persistencia TicketEntity.
 */
public class TicketMapper {

    public static TicketEntity toEntity(Ticket ticket) {
        return new TicketEntity(
            null,
            ticket.getReservationId(),
            ticket.getIssuedAt(),
            ticket.getStatus().name()
        );
    }

    public static Ticket toDomain(TicketEntity entity) {
        return Ticket.fromPersistence(
            entity.getId(),
            entity.getReservationId(),
            TicketStatus.valueOf(entity.getStatus()),
            entity.getIssuedAt()
        );
    }
}