package com.aug.flightbooking.domain.model.checkin;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Representa el registro de check-in realizado por un pasajero.
 * Esta clase es una entidad dependiente del agregado Ticket.
 */
public class CheckIn {

    // Hora en la que se realizó el check-in
    private final LocalDateTime checkInTime;

    // Tiempo mínimo y máximo permitido para hacer check-in (en horas)
    private static final long MIN_HOURS_BEFORE_FLIGHT = 2;
    private static final long MAX_HOURS_BEFORE_FLIGHT = 24;

    private CheckIn(LocalDateTime checkInTime) {
        this.checkInTime = checkInTime;
    }

    /**
     * Crea un nuevo check-in validando las restricciones de horario.
     *
     * @param checkInTime Momento en que el pasajero intenta hacer el check-in.
     * @return Instancia válida de CheckIn.
     * @throws IllegalArgumentException si el check-in se realiza fuera del rango permitido.
     */
    public static CheckIn create(LocalDateTime checkInTime) {
        LocalDateTime now = LocalDateTime.now();

        long hoursBefore = Duration.between(now, checkInTime).toHours();

        if (hoursBefore < MIN_HOURS_BEFORE_FLIGHT || hoursBefore > MAX_HOURS_BEFORE_FLIGHT) {
            throw new IllegalArgumentException("El check-in solo puede hacerse entre 24 y 2 horas antes del vuelo.");
        }

        return new CheckIn(checkInTime);
    }

    /**
     * Devuelve la fecha y hora en que se realizó el check-in.
     */
    public LocalDateTime getCheckInTime() {
        return checkInTime;
    }
}

