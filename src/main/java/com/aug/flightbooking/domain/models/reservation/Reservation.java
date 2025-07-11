package com.aug.flightbooking.domain.models.reservation;

import com.aug.flightbooking.domain.exceptions.ReservationChangeStatusException;

import java.time.Instant;
import java.util.Objects;

/**
 * Representa una reserva de vuelo realizada por un cliente.
 * Es el Aggregate Root del contexto de reservas.
 */
public class Reservation {

    private final Long id;
    private final Long flightId;
    private final PassengerInfo passengerInfo;
    private final Instant createdAt;
    private ReservationStatus status;

    private Reservation(Long id, Long flightId, PassengerInfo passengerInfo, ReservationStatus status, Instant createdAt) {
        this.id = id;
        this.flightId = Objects.requireNonNull(flightId, "El flightId no puede ser null");
        this.passengerInfo = Objects.requireNonNull(passengerInfo, "El passengerInfo no puede ser null");
        this.status = Objects.requireNonNull(status, "El status no puede ser null");
        this.createdAt = Objects.requireNonNull(createdAt, "El createdAt no puede ser null");
    }

    public static Reservation fromPersistence(
            Long id, Long flightId, PassengerInfo passengerInfo, ReservationStatus status, Instant createdAt) {
        if (id == null) throw new IllegalArgumentException("El id no puede ser nulo");
        return new Reservation(id, flightId, passengerInfo, status, createdAt);
    }

    /**
     * Crea una nueva reserva con estado inicial CREATED.
     */
    public static Reservation create(Long flightId, PassengerInfo passengerInfo) {
        return new Reservation(null, flightId, passengerInfo, ReservationStatus.CREATED, Instant.now());
    }

    /**
     * Marca la reserva como pendiente
     */
    protected void markAsPending(){
        changeStatus(ReservationStatus.PENDING);
    }

    /**
     * Marca la reserva como confirmada
     */
    protected void markAsConfirmed(){
        changeStatus(ReservationStatus.CONFIRMED);
    }

    /**
     * Marca la reserva como confirmada
     */
    protected void markAsRejected(){
        changeStatus(ReservationStatus.REJECTED);
    }

    /**
     * Marca la reserva como confirmada
     */
    protected void markAsFailed(){
        changeStatus(ReservationStatus.FAILED);
    }

    /**
     * Cambia el estado de la reserva, validando la transición.
     */
    private void changeStatus(ReservationStatus newStatus) {
        if (!ReservationStateMachine.canTransition(this.status, newStatus)) {
            throw new ReservationChangeStatusException("Transición no permitida de " + this.status + " a " + newStatus);
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

    @Override
    public String toString() {
        return "Reservation{" +
                "id=" + id +
                ", flightId=" + flightId +
                ", status=" + status.name() +
                ", createdAt=" + createdAt +
                '}';
    }

}
