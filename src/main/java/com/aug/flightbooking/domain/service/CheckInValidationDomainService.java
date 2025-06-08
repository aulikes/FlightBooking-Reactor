package com.aug.flightbooking.domain.service;

import com.aug.flightbooking.domain.model.flight.Flight;
import com.aug.flightbooking.domain.model.checkin.Ticket;

import java.time.Duration;
import java.time.Instant;
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
     */
    public boolean canCheckIn(Flight flight, Ticket ticket, Instant checkInTime) {
        Instant departureTime = flight.getScheduledDeparture();

        Instant checkInWindowStart = departureTime.minus(Duration.ofHours(HOURS_BEFORE_DEPARTURE_ALLOWED));
        Instant checkInWindowEnd = departureTime.minus(Duration.ofHours(HOURS_BEFORE_DEPARTURE_LIMIT));

        return !checkInTime.isBefore(checkInWindowStart) && !checkInTime.isAfter(checkInWindowEnd);
    }
}

