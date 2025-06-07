package com.aug.flightbooking.domain.model.reservation;

import java.time.Instant;

/**
 * Representa una reserva de vuelo realizada por un cliente.
 * Es el Aggregate Root del contexto de reservas.
 */
public class Reservation {

    private Long id;
    private Long flightId;
    private PassengerInfo passengerInfo;
    private Instant createdAt;
    private ReservationStatus status;

    private Reservation(Long flightId, PassengerInfo passengerInfo, ReservationStatus status, Instant createdAt) {
        this.flightId = flightId;
        this.passengerInfo = passengerInfo;
        this.status = status;
        this.createdAt = createdAt;
    }

    /**
     * Crea una nueva reserva con estado inicial CREATED.
     */
    public static Reservation create(Long flightId, PassengerInfo passengerInfo, Instant createdAt) {
        return new Reservation(flightId, passengerInfo, ReservationStatus.CREATED, createdAt);
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

    public Long getId() {
        return id;
    }

    public Long getFlightId() {
        return flightId;
    }

    public PassengerInfo getPassengerInfo() {
        return passengerInfo;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public ReservationStatus getStatus() {
        return status;
    }
}
