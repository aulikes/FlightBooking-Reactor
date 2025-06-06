package com.aug.flightbooking.domain.model.airline;

import java.time.LocalDateTime;

/**
 * Representa un vuelo de una aerolínea en el sistema.
 * Es el Aggregate Root del contexto de aerolínea.
 */
public class Flight {

    private Long id;
    private Airline airline; // Value Object interno
    private String flightCode;
    private String origin;
    private String destination;
    private LocalDateTime scheduledDeparture;
    private LocalDateTime scheduledArrival;
    private FlightStatus status;

    // Constructor privado
    private Flight(Airline airline, String flightCode, String origin, String destination,
                   LocalDateTime scheduledDeparture, LocalDateTime scheduledArrival) {
        this.airline = airline;
        this.flightCode = flightCode;
        this.origin = origin;
        this.destination = destination;
        this.scheduledDeparture = scheduledDeparture;
        this.scheduledArrival = scheduledArrival;
        this.status = FlightStatus.SCHEDULED;
    }

    /**
     * Fábrica para crear un nuevo vuelo programado.
     */
    public static Flight scheduleFlight(Airline airline, String flightCode, String origin, String destination,
                                        LocalDateTime scheduledDeparture, LocalDateTime scheduledArrival) {
        return new Flight(airline, flightCode, origin, destination, scheduledDeparture, scheduledArrival);
    }

    /**
     * Marca el vuelo como en proceso de abordaje.
     */
    public void startBoarding() {
        if (status != FlightStatus.SCHEDULED) {
            throw new IllegalStateException("Solo se puede iniciar abordaje de un vuelo programado.");
        }
        this.status = FlightStatus.BOARDING;
    }

    /**
     * Marca el vuelo como en el aire.
     */
    public void takeOff() {
        if (status != FlightStatus.BOARDING) {
            throw new IllegalStateException("Solo se puede despegar después de haber iniciado abordaje.");
        }
        this.status = FlightStatus.IN_AIR;
    }

    /**
     * Marca el vuelo como aterrizado.
     */
    public void land() {
        if (status != FlightStatus.IN_AIR) {
            throw new IllegalStateException("Solo se puede aterrizar un vuelo que esté en el aire.");
        }
        this.status = FlightStatus.FINISHED;
    }

    /**
     * Marca el vuelo como cancelado.
     */
    public void cancel() {
        if (status == FlightStatus.FINISHED || status == FlightStatus.IN_AIR) {
            throw new IllegalStateException("No se puede cancelar un vuelo que ya despegó o aterrizó.");
        }
        this.status = FlightStatus.CANCELLED;
    }

    // Getters

    public Long getId() {
        return id;
    }

    public Airline getAirline() {
        return airline;
    }

    public String getFlightCode() {
        return flightCode;
    }

    public String getOrigin() {
        return origin;
    }

    public String getDestination() {
        return destination;
    }

    public LocalDateTime getScheduledDeparture() {
        return scheduledDeparture;
    }

    public LocalDateTime getScheduledArrival() {
        return scheduledArrival;
    }

    public FlightStatus getStatus() {
        return status;
    }
}
