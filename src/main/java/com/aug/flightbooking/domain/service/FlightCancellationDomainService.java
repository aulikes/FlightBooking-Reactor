package com.aug.flightbooking.domain.service;

import com.aug.flightbooking.domain.model.airline.Flight;
import com.aug.flightbooking.domain.model.checkin.Ticket;

import java.util.List;

/**
 * Servicio de dominio que coordina la lógica cuando un vuelo es cancelado.
 * Se encarga de aplicar las reglas necesarias para dejar coherente el estado del sistema,
 * afectando tanto al vuelo como a los tickets asociados.
 */
public class FlightCancellationDomainService {

    /**
     * Cancela un vuelo junto con todos los tiquetes asociados a él.
     *
     * @param flight  Instancia del vuelo a cancelar.
     * @param tickets Lista de tiquetes que estaban emitidos para ese vuelo.
     */
    public void cancelFlightAndTickets(Flight flight, List<Ticket> tickets) {
        // Cancela el vuelo (invoca la lógica de dominio en la entidad Flight)
        flight.cancel();

        // Itera sobre todos los tickets y los cancela uno por uno
        for (Ticket ticket : tickets) {
            ticket.cancel(); // La entidad Ticket maneja sus propias validaciones internas
        }
    }

    FlightCancellationDomainService
}

