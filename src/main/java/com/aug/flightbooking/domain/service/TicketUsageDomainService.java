package com.aug.flightbooking.domain.service;

import com.aug.flightbooking.domain.model.flight.Flight;
import com.aug.flightbooking.domain.model.flight.FlightStatus;
import com.aug.flightbooking.domain.model.checkin.Ticket;

/**
 * Servicio de dominio que permite marcar un tiquete como utilizado,
 * siempre que el vuelo esté en curso y el check-in se haya realizado.
 */
public class TicketUsageDomainService {

    /**
     * Marca el tiquete como usado, validando reglas de negocio asociadas al vuelo.
     *
     * @param flight Vuelo asociado al tiquete.
     * @param ticket Tiquete a marcar como usado.
     */
    public void useTicket(Flight flight, Ticket ticket) {
        if (flight.getStatus() != FlightStatus.BOARDING) {
            throw new IllegalStateException("Solo se pueden usar tiquetes cuando el vuelo está en abordaje.");
        }
        ticket.markAsUsed();
    }
}
