package com.aug.flightbooking.domain.model.airline;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Representa un vuelo operado por una aerolínea específica.
 * Incluye su estado, horarios y lógica de transición de estados.
 */
public class Flight {

    private final Long id;
    private final Long airlineId;
    private final String flightNumber;
    private final String origin;
    private final String destination;
    private final LocalDateTime departureTime;
    private final LocalDateTime arrivalTime;

    private FlightStatus status;

    /**
     * Constructor principal para crear un vuelo.
     */
    public Flight(Long id, Long airlineId, String flightNumber, String origin, String destination,
                  LocalDateTime departureTime, LocalDateTime arrivalTime) {
        this.id = Objects.requireNonNull(id);
        this.airlineId = Objects.requireNonNull(airlineId);
        this.flightNumber = Objects.requireNonNull(flightNumber);
        this.origin = Objects.requireNonNull(origin);
        this.destination = Objects.requireNonNull(destination);
        this.departureTime = Objects.requireNonNull(departureTime);
        this.arrivalTime = Objects.requireNonNull(arrivalTime);
        this.status = FlightStatus.CREATED;
    }

    // =============================
    // MÉTODOS DE NEGOCIO DEL VUELO
    // =============================

    /**
     * Marca el vuelo como programado (SCHEDULED).
     */
    public void schedule() {
        if (status != FlightStatus.CREATED) {
            throw new IllegalStateException("Only CREATED flights can be scheduled.");
        }
        this.status = FlightStatus.SCHEDULED;
    }

    /**
     * Marca el inicio del abordaje (BOARDING).
     */
    public void startBoarding() {
        if (status != FlightStatus.SCHEDULED) {
            throw new IllegalStateException("Only SCHEDULED flights can start boarding.");
        }
        this.status = FlightStatus.BOARDING;
    }

    /**
     * Marca el vuelo como retrasado (DELAYED).
     */
    public void delay() {
        if (status != FlightStatus.SCHEDULED && status != FlightStatus.BOARDING) {
            throw new IllegalStateException("Only SCHEDULED or BOARDING flights can be delayed.");
        }
        this.status = FlightStatus.DELAYED;
    }

    /**
     * Finaliza el vuelo con éxito.
     */
    public void finish() {
        if (status != FlightStatus.BOARDING && status != FlightStatus.DELAYED) {
            throw new IllegalStateException("Only BOARDING or DELAYED flights can be finished.");
        }
        this.status = FlightStatus.FINISHED;
    }

    /**
     * Cancela el vuelo.
     */
    public void cancel() {
        if (status == FlightStatus.FINISHED || status == FlightStatus.CANCELLED) {
            throw new IllegalStateException("Finished or already cancelled flights cannot be cancelled.");
        }
        this.status = FlightStatus.CANCELLED;
    }

    // =============================
    // MÉTODOS GET
    // =============================

    public Long getId() {
        return id;
    }

    public Long getAirlineId() {
        return airlineId;
    }

    public String getFlightNumber() {
        return flightNumber;
    }

    public String getOrigin() {
        return origin;
    }

    public String getDestination() {
        return destination;
    }

    public LocalDateTime getDepartureTime() {
        return departureTime;
    }

    public LocalDateTime getArrivalTime() {
        return arrivalTime;
    }

    public FlightStatus getStatus() {
        return status;
    }
}
