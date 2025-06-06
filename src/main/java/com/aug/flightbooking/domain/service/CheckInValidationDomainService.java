package com.aug.flightbooking.domain.service;

import com.aug.flightbooking.domain.model.airline.Flight;
import com.aug.flightbooking.domain.model.checkin.Ticket;

import java.time.LocalDateTime;

/**
 * Servicio de dominio que encapsula la lógica de validación para el proceso de check-in.
 * Permite centralizar reglas que podrían involucrar múltiples agregados (como Ticket y Flight).
 */
public class CheckInValidationDomainService {

    // Margen permitido para check-in: desde 24h antes hasta 2h antes del vuelo
    private static final int HOURS_BEFORE_DEPARTURE_ALLOWED = 24;
    private static final int HOURS_BEFORE_DEPARTURE_LIMIT = 2;

    /**
     * Determina si se puede realizar el check-in para un vuelo en un momento dado.
     *
     * @param flight El vuelo correspondiente al tiquete.
     * @param ticket El tiquete sobre el cual se intenta hacer check-in.
     * @param checkInTime El momento en que se intenta hacer check-in.
     * @return true si es permitido, false si está fuera del rango de tiempo.
     */
    public boolean canCheckIn(Flight flight, Ticket ticket, LocalDateTime checkInTime) {
        LocalDateTime departureTime = flight.getDepartureTime();

        LocalDateTime checkInWindowStart = departureTime.minusHours(HOURS_BEFORE_DEPARTURE_ALLOWED);
        LocalDateTime checkInWindowEnd = departureTime.minusHours(HOURS_BEFORE_DEPARTURE_LIMIT);

        return !checkInTime.isBefore(checkInWindowStart) && !checkInTime.isAfter(checkInWindowEnd);
    }
}

