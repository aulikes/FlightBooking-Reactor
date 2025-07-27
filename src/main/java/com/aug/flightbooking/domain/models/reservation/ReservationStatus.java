package com.aug.flightbooking.domain.models.reservation;

/**
 * Define los posibles estados de una reserva de vuelo.
 * Se utiliza para controlar el ciclo de vida de la reserva.
 */
public enum ReservationStatus {
    CREATED,            //La reserva ha sido creada, pero aún no se ha notificado a la aerolínea.
    PENDING,            //La reserva fue creada y el evento fue enviado exitosamente a la aerolínea.
    EMITTED,            //La reserva está en espera de que se cree el Ticket
    CONFIRMED,          //La reserva fue confirmada exitosamente.
    REJECTED,           //La reserva fue rechazada.
    CANCELLED,          //La reserva fue cancelada.
    FAILED              //Ocurrió un error al intentar notificar la creación de la reserva a la aerolínea.
}
