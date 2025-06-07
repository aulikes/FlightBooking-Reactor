package com.aug.flightbooking.domain.model.reservation;

import com.aug.flightbooking.domain.exception.ReservationChangeStatusException;

/**
 * Enum que establece los métodos que se deben ejecutar cuando se requiere cambiar de estado.
 * De esta manera los cambios de estado no son públicos y se deben realizar por este medio, para mejor control del dominio
 */
public enum ReservationStatusAction {
    CONFIRMED {
        @Override
        public void apply(Reservation reservation) throws ReservationChangeStatusException {
            reservation.markAsConfirmed();
        }
    },
    REJECTED {
        @Override
        public void apply(Reservation reservation) throws ReservationChangeStatusException {
            reservation.markAsRejected();
        }
    },
    FAILED {
        @Override
        public void apply(Reservation reservation) throws ReservationChangeStatusException {
            reservation.markAsFailed();
        }
    };

    public abstract void apply(Reservation reservation) throws ReservationChangeStatusException;
}
