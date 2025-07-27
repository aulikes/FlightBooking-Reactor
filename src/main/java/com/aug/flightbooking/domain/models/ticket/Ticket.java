package com.aug.flightbooking.domain.models.ticket;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;

/**
 * Representa un tiquete de vuelo emitido a un pasajero.
 * Es un Aggregate Root que encapsula su propio ciclo de vida y reglas de negocio.
 */
public class Ticket {

    private final Long id;
    private final Long reservationId;
    private final Instant issuedAt;
    private TicketStatus status;

    // Margen permitido para check-in: desde 24h antes hasta 2h antes del vuelo
    private static final int HOURS_BEFORE_DEPARTURE_ALLOWED = 24;
    private static final int HOURS_BEFORE_DEPARTURE_LIMIT = 2;

    /**
     * Constructor privado para forzar el uso del método de fábrica estático.
     */
    private Ticket(Long id, Long reservationId, TicketStatus status, Instant issuedAt) {
        this.id = id;
        this.reservationId = reservationId;
        this.status = status;
        this.issuedAt = issuedAt;
    }

    /**
     * Crea un nuevo tiquete emitido, marcando su estado inicial como EMITTED.
     */
    public static Ticket create(Long reservationId) {
        return new Ticket(null, reservationId, TicketStatus.EMITTED, Instant.now());
    }

    public static Ticket fromPersistence(Long id, Long reservationId,
        TicketStatus ticketStatus, Instant issuedAt) {
        if (id == null) throw new IllegalArgumentException("El id no puede ser nulo");
        return new Ticket(id, reservationId, ticketStatus, issuedAt);
    }

    /**
     * Intenta realizar el check-in de este tiquete.
     * Utiliza las reglas del dominio para validar si el check-in es posible.
     */
    public void attemptCheckIn(Instant departureTime, Instant checkInTime) {
        if (this.status != TicketStatus.EMITTED) {
            throw new IllegalStateException("Solo se puede hacer check-in con tiquetes emitidos.");
        }
        if (!canCheckIn(departureTime, checkInTime)) {
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


    /**
     * Determina si se puede realizar el check-in para un vuelo en un momento dado.
     */
    private boolean canCheckIn(Instant departureTime, Instant checkInTime) {
        Instant checkInWindowStart = departureTime.minus(Duration.ofHours(HOURS_BEFORE_DEPARTURE_ALLOWED));
        Instant checkInWindowEnd = departureTime.minus(Duration.ofHours(HOURS_BEFORE_DEPARTURE_LIMIT));
        return !checkInTime.isBefore(checkInWindowStart) && !checkInTime.isAfter(checkInWindowEnd);
    }

    public Long getId() {
        return id;
    }

    public Long getReservationId() {
        return reservationId;
    }

    public Instant getIssuedAt() {
        return issuedAt;
    }

    public TicketStatus getStatus() {
        return status;
    }
}
