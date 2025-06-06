package com.aug.flightbooking.domain.model.airline;

import java.time.LocalDateTime;

package com.aug.reservavuelos.domain.flight;

import com.aug.flightbooking.domain.service.FlightCancellationDomainService;
import com.aug.reservavuelos.domain.flight.service.FlightCancellationDomainService;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Representa un vuelo operado por una aerolínea.
 * Esta clase es una entidad dependiente del Aggregate Root Airline.
 */
@Getter
public class Flight {

    private Long id;
    private String origin;
    private String destination;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private FlightStatus status;

    /**
     * Constructor con campos obligatorios.
     */
    public Flight(String origin, String destination, LocalDateTime departureTime, LocalDateTime arrivalTime) {
        this.origin = origin;
        this.destination = destination;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.status = FlightStatus.SCHEDULED;
    }

    /**
     * Cancela el vuelo utilizando el servicio de dominio,
     * que contiene la lógica de negocio que valida si se puede cancelar o no.
     *
     * @param cancellationTime Fecha y hora actual en que se intenta la cancelación.
     * @param cancellationService Servicio de dominio con la lógica de validación.
     */
    public void cancel(LocalDateTime cancellationTime, FlightCancellationDomainService cancellationService) {
        if (!cancellationService.canCancel(this, cancellationTime)) {
            throw new IllegalStateException("El vuelo no puede ser cancelado en este momento.");
        }

        this.status = FlightStatus.CANCELLED;
    }
}
