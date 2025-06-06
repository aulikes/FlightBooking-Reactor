package com.aug.flightbooking.domain.model.reservation;

import java.time.LocalDateTime;

/**
 * Representa una reserva de vuelo realizada por un cliente.
 * Es el Aggregate Root del contexto de reservas.
 */
public class Reservation {

    private Long id;
    private Long flightId;
    private Long passengerId;
    private LocalDateTime createdAt;
    private ReservationStatus status;

    private Reservation(Long flightId, Long passengerId, ReservationStatus status, LocalDateTime createdAt) {
        this.flightId = flightId;
        this.passengerId = passengerId;
        this.status = status;
        this.createdAt = createdAt;
    }

    /**
     * Crea una nueva reserva con estado inicial CREATED.
     */
    public static Reservation create(Long flightId, Long passengerId, LocalDateTime createdAt) {
        return new Reservation(flightId, passengerId, ReservationStatus.CREATED, createdAt);
    }

    /**
     * Cambia el estado de la reserva, validando la transición.
     */
    public void changeStatus(ReservationStatus newStatus) {
        if (!ReservationStateMachine.canTransition(this.status, newStatus)) {
            throw new IllegalStateException("Transición no permitida de " + this.status + " a " + newStatus);
        }
        this.status = newStatus;
    }

    // Getters

    public Long getId() {
        return id;
    }

    public Long getFlightId() {
        return flightId;
    }

    public Long getPassengerId() {
        return passengerId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public ReservationStatus getStatus() {
        return status;
    }
}
