package com.aug.flightbooking.application.command;

import java.time.LocalDate;

/**
 * Representa el comando para solicitar la creación de una reserva.
 * Contiene únicamente los datos necesarios para iniciar el proceso de reserva.
 */
public record CreateReservationCommand(
        Long flightId,
        String fullName,
        String documentId
) {
}
