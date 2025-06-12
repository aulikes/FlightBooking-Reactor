package com.aug.flightbooking.application.command;

/**
 * Representa el comando para solicitar la creación de una reserva.
 * Contiene únicamente los datos necesarios para iniciar el proceso de reserva.
 */
public record CreateCheckInCommand(
        Long ticketId,
        Long millisecondInstant
) {
}
