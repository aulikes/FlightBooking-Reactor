package com.aug.flightbooking.domain.models.reservation;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Set;

/**
 * Controla las transiciones válidas entre estados de una reserva.
 */
public class ReservationStateMachine {

    private static final EnumMap<ReservationStatus, Set<ReservationStatus>> validTransitions =
            new EnumMap<>(ReservationStatus.class);

    static {
        validTransitions.put(ReservationStatus.CREATED,
                EnumSet.of(ReservationStatus.PENDING, ReservationStatus.FAILED));

        validTransitions.put(ReservationStatus.PENDING,
                EnumSet.of(ReservationStatus.EMITTED, ReservationStatus.REJECTED, ReservationStatus.FAILED));

        validTransitions.put(ReservationStatus.EMITTED,
                EnumSet.of(ReservationStatus.CANCELLED, ReservationStatus.CONFIRMED));

        validTransitions.put(ReservationStatus.CONFIRMED,
                EnumSet.of(ReservationStatus.CANCELLED));

        validTransitions.put(ReservationStatus.REJECTED,
                EnumSet.noneOf(ReservationStatus.class));

        validTransitions.put(ReservationStatus.CANCELLED,
                EnumSet.noneOf(ReservationStatus.class));

        validTransitions.put(ReservationStatus.FAILED,
                EnumSet.noneOf(ReservationStatus.class));
    }

    /**
     * Verifica si una transición de estado es válida según las reglas del dominio.
     */
    public static boolean canTransition(ReservationStatus from, ReservationStatus to) {
        return validTransitions.getOrDefault(from, EnumSet.noneOf(ReservationStatus.class)).contains(to);
    }
}
