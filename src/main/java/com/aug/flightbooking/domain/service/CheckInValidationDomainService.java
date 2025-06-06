package com.aug.flightbooking.domain.service;


import com.aug.flightbooking.domain.model.airline.Flight;
import com.aug.flightbooking.domain.model.checkin.Ticket;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Servicio de dominio encargado de validar si se permite realizar el check-in
 * para un ticket en un vuelo específico.
 */
public class CheckInValidationDomainService {

    // Margen de tiempo permitido para el check-in antes del vuelo
    private static final Duration CHECKIN_START_OFFSET = Duration.ofHours(24);
    private static final Duration CHECKIN_END_OFFSET = Duration.ofHours(2);

    /**
     * Verifica si el check-in está permitido según la hora de salida del vuelo
     * y las reglas definidas por el negocio.
     *
     * @param flight Vuelo relacionado al check-in
     * @param ticket Ticket asociado al pasajero
     * @param checkInTime Momento en el que se intenta hacer el check-in
     * @return true si el check-in está permitido, false en caso contrario
     */
    public boolean canCheckIn(Flight flight, Ticket ticket, LocalDateTime checkInTime) {
        LocalDateTime departureTime = flight.getDepartureTime();

        // Calcula la ventana válida para hacer check-in
        LocalDateTime checkInStart = departureTime.minus(CHECKIN_START_OFFSET);
        LocalDateTime checkInEnd = departureTime.minus(CHECKIN_END_OFFSET);

        // Verifica que la hora actual esté dentro del rango permitido
        return !checkInTime.isBefore(checkInStart) && !checkInTime.isAfter(checkInEnd);
    }
}
