package com.aug.flightbooking.domain.service;

import com.aug.flightbooking.domain.model.flight.Flight;
import com.aug.flightbooking.domain.model.checkin.Ticket;
import com.aug.flightbooking.domain.model.reservation.Reservation;
import com.aug.flightbooking.domain.model.reservation.ReservationStatus;
import com.aug.flightbooking.domain.model.flight.FlightStatus;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;

/**
 * Servicio de dominio encargado de emitir un tiquete (ticket) de vuelo
 * a partir de una reserva y un vuelo válidos.
 */
public class TicketEmissionDomainService {

    /**
     * Valida todas las reglas de negocio necesarias y crea un nuevo ticket.
     *
     * @param reservation Reserva de vuelo previamente creada
     * @param flight Vuelo asociado a la reserva
     * @return nuevo Ticket listo para ser persistido
     * @throws IllegalArgumentException si alguna regla de negocio es violada
     */
    public Ticket emitTicket(Reservation reservation, Flight flight) {
        validateReservation(reservation);
        validateFlight(flight);
        validateTimingConstraints(flight.getScheduledDeparture());

        return Ticket.create(
                reservation.getId(),
                flight.getId(),
                LocalDateTime.now()
        );
    }

    /**
     * Valida que la reserva esté en estado PENDING.
     */
    private void validateReservation(Reservation reservation) {
        if (reservation == null) {
            throw new IllegalArgumentException("La reserva no puede ser nula.");
        }

        if (!ReservationStatus.PENDING.equals(reservation.getStatus())) {
            throw new IllegalArgumentException("Solo se puede emitir un ticket para reservas en estado PENDING.");
        }
    }

    /**
     * Valida que el vuelo esté activo.
     */
    private void validateFlight(Flight flight) {
        if (flight == null) {
            throw new IllegalArgumentException("El vuelo no puede ser nulo.");
        }

        if (!FlightStatus.SCHEDULED.equals(flight.getStatus())) {
            throw new IllegalArgumentException("Solo se pueden emitir tickets para vuelos en estado SCHEDULED.");
        }
    }

    /**
     * Valida que el horario actual aún permita emitir el ticket.
     */
    private void validateTimingConstraints(Instant departureTime) {
        Instant now = Instant.now();
        Duration diff = Duration.between(now, departureTime);

        if (diff.isNegative()) {
            throw new IllegalArgumentException("El vuelo ya despegó.");
        }

        if (diff.toHours() < 2) {
            throw new IllegalArgumentException("No se puede emitir ticket menos de 2 horas antes del vuelo.");
        }
    }
}
