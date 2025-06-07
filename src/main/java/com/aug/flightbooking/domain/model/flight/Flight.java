package com.aug.flightbooking.domain.model.flight;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Aggregate Root que representa un vuelo y su disponibilidad.
 */
public class Flight {

    private Long id;
    private Airline airline; // Value Object interno
    private String flightCode;
    private String origin;
    private String destination;    
	private final int totalSeats;
    private int reservedSeats;
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

    /**
     * Indica si aún hay asientos disponibles.
     */
    public boolean hasAvailableSeats() {
        return reservedSeats < totalSeats;
    }


    /**
     * Intenta reservar un asiento. Retorna true si fue exitoso.
     */
    public boolean tryReserveSeat() {
        if (hasAvailableSeats()) {
            reservedSeats++;
            return true;
        }
        return false;
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

    public int getTotalSeats() {
        return totalSeats;
    }

    public int getReservedSeats() {
        return reservedSeats;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Flight)) return false;
        Flight flight = (Flight) o;
        return Objects.equals(id, flight.id);
    }

    @Override
    public String toString() {
        return "Flight{" +
                "flightCode='" + flightCode + '\'' +
                ", airline=" + airline +
                ", origin='" + origin + '\'' +
                ", destination='" + destination + '\'' +
                ", status=" + status +
                '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
