package com.aug.flightbooking.domain.models.reservation;

import com.aug.flightbooking.domain.exceptions.ReservationChangeStatusException;

/**
 * Enum que establece los métodos que se deben ejecutar cuando se requiere cambiar de estado.
 * De esta manera los cambios de estado no son públicos y se deben realizar por este medio, para mejor control del dominio
 */
public enum ReservationStatusAction {
    PENDING {
        @Override
        public void apply(Reservation reservation) throws ReservationChangeStatusException {
            reservation.markAsPending();
        }
    },
    EMITTED {
        @Override
        public void apply(Reservation reservation) throws ReservationChangeStatusException {
            reservation.markAsEmitted();
        }
    },
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
