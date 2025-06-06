package com.aug.flightbooking.domain.service;

import com.aug.flightbooking.domain.model.airline.Flight;
import com.aug.flightbooking.domain.model.checkin.Ticket;

import java.util.List;

/**
 * Servicio de dominio que encapsula la lógica de cancelación de vuelos.
 * Asegura que los efectos de la cancelación se propaguen a los tiquetes relacionados.
 */
public class FlightCancellationDomainService {

    /**
     * Cancela un vuelo y todos sus tiquetes asociados
     *
     * @param flight Vuelo a cancelar.
     * @param tickets Lista de tiquetes asociados al vuelo.
     */
    public void cancelFlight(Flight flight, List<Ticket> tickets) {
        flight.cancel();

        for (Ticket ticket : tickets) {
            ticket.cancel();
        }
    }
}


