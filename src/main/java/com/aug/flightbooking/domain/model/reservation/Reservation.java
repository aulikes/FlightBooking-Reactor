package com.aug.flightbooking.domain.model.reservation;
import lombok.Getter;
import lombok.NonNull;

import java.time.LocalDateTime;

/**
 * Representa una reserva de vuelo realizada por un cliente.
 * Esta clase es la raíz del agregado del contexto de reservas.
 */
@Getter
public class Reservation {

    private Long id;
    private final Long flightId;
    private final PassengerData passengerData;
    private ReservationStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Constructor principal para crear una nueva reserva.
     * Inicializa la reserva con estado CREATED y marca la hora de creación.
     */
    public Reservation(@NonNull Long flightId, @NonNull PassengerData passengerData) {
        this.flightId = flightId;
        this.passengerData = passengerData;
        this.status = ReservationStatus.CREATED;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    /**
     * Método de fábrica para crear una reserva con ID (por ejemplo, luego de guardarla en base de datos).
     */
    public static Reservation withId(Long id, Long flightId, PassengerData passengerData, ReservationStatus status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        Reservation reservation = new Reservation(flightId, passengerData);
        reservation.id = id;
        reservation.status = status;
        reservation.createdAt = createdAt;
        reservation.updatedAt = updatedAt;
        return reservation;
    }

    /**
     * Marca la reserva como pendiente luego de ser enviada a la aerolínea.
     */
    public void markAsPending() {
        this.status = ReservationStatus.PENDING;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Marca la reserva como fallida si ocurrió un error al notificar a la aerolínea.
     */
    public void markAsFailed() {
        this.status = ReservationStatus.FAILED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Marca la reserva como confirmada exitosamente.
     */
    public void markAsConfirmed() {
        this.status = ReservationStatus.CONFIRMED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Cancela la reserva por cualquier motivo externo.
     */
    public void cancel() {
        this.status = ReservationStatus.CANCELLED;
        this.updatedAt = LocalDateTime.now();
    }
}
