package com.aug.flightbooking.domain.model.checkin;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Representa el proceso de Check-in de un ticket.
 * Solo puede realizarse dentro de una ventana de tiempo antes del vuelo.
 */
public class CheckIn {

    // Tiempo mínimo antes de la salida del vuelo para iniciar check-in (24 horas)
    private static final int MIN_HOURS_BEFORE_FLIGHT = 24;

    // Tiempo máximo antes de la salida del vuelo para cerrar check-in (2 horas)
    private static final int MAX_HOURS_BEFORE_FLIGHT = 2;

    private final LocalDateTime checkedInAt;
    private final String seatNumber;

    /**
     * Crea un nuevo check-in verificando que esté dentro del rango permitido.
     *
     * @param flightDepartureTime fecha y hora de salida del vuelo asociado al ticket
     * @param checkInTime          fecha y hora en que se realiza el check-in
     * @param seatNumber           número de asiento asignado
     */
    public CheckIn(LocalDateTime flightDepartureTime, LocalDateTime checkInTime, String seatNumber) {
        validateCheckInTime(flightDepartureTime, checkInTime);
        this.checkedInAt = Objects.requireNonNull(checkInTime, "Check-in time cannot be null");
        this.seatNumber = Objects.requireNonNull(seatNumber, "Seat number cannot be null");
    }

    /**
     * Verifica que la hora de check-in esté dentro del rango permitido.
     */
    private void validateCheckInTime(LocalDateTime flightDepartureTime, LocalDateTime checkInTime) {
        if (flightDepartureTime == null || checkInTime == null) {
            throw new IllegalArgumentException("Both flight and check-in time are required.");
        }

        long hoursBefore = java.time.Duration.between(checkInTime, flightDepartureTime).toHours();

        if (hoursBefore < MAX_HOURS_BEFORE_FLIGHT || hoursBefore > MIN_HOURS_BEFORE_FLIGHT) {
            throw new IllegalStateException("Check-in must be done between 24 and 2 hours before flight.");
        }
    }

    public LocalDateTime getCheckedInAt() {
        return checkedInAt;
    }

    public String getSeatNumber() {
        return seatNumber;
    }
}
