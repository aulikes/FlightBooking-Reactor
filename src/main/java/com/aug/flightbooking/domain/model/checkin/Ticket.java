package com.aug.flightbooking.domain.model.checkin;

import com.aug.flightbooking.domain.model.flight.Flight;
import com.aug.flightbooking.domain.service.CheckInValidationDomainService;

import java.time.Instant;
import java.time.LocalDateTime;

/**
 * Representa un tiquete de vuelo emitido a un pasajero.
 * Es un Aggregate Root que encapsula su propio ciclo de vida y reglas de negocio.
 */
public class Ticket {

    private Long id;
    private Long reservationId;
    private Long flightId;
    private LocalDateTime issuedAt;
    private TicketStatus status;

    /**
     * Constructor privado para forzar el uso del método de fábrica estático.
     */
    private Ticket(Long reservationId, Long flightId, TicketStatus status, LocalDateTime issuedAt) {
        this.reservationId = reservationId;
        this.flightId = flightId;
        this.status = status;
        this.issuedAt = issuedAt;
    }

    /**
     * Crea un nuevo tiquete emitido, marcando su estado inicial como EMITTED.
     */
    public static Ticket create(Long reservationId, Long flightId, LocalDateTime issuedAt) {
        return new Ticket(reservationId, flightId, TicketStatus.EMITTED, issuedAt);
    }

    /**
     * Intenta realizar el check-in de este tiquete.
     * Utiliza las reglas del dominio para validar si el check-in es posible.
     */
    public void attemptCheckIn(Flight flight, Instant checkInTime, CheckInValidationDomainService validationService) {
        if (this.status != TicketStatus.EMITTED) {
            throw new IllegalStateException("Solo se puede hacer check-in con tiquetes emitidos.");
        }

        if (!validationService.canCheckIn(flight, this, checkInTime)) {
            throw new IllegalStateException("No se puede hacer check-in en este momento.");
        }

        this.status = TicketStatus.CHECKED_IN;
    }

    /**
     * Marca el tiquete como usado después del proceso de abordaje.
     */
    public void markAsUsed() {
        if (this.status != TicketStatus.CHECKED_IN) {
            throw new IllegalStateException("Solo se puede usar un tiquete que ya hizo check-in.");
        }

        this.status = TicketStatus.USED;
    }

    /**
     * Cancela el tiquete si aún no ha sido usado ni se ha hecho check-in.
     */
    public void cancel() {
        if (this.status == TicketStatus.USED || this.status == TicketStatus.CHECKED_IN) {
            throw new IllegalStateException("No se puede cancelar un tiquete ya utilizado o con check-in.");
        }

        this.status = TicketStatus.CANCELLED;
    }

    // Getters necesarios para lectura o persistencia

    public Long getId() {
        return id;
    }

    public Long getReservationId() {
        return reservationId;
    }

    public Long getFlightId() {
        return flightId;
    }

    public LocalDateTime getIssuedAt() {
        return issuedAt;
    }

    public TicketStatus getStatus() {
        return status;
    }
}
