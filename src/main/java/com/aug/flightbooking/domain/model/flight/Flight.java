package com.aug.flightbooking.domain.model.flight;

import java.time.Instant;
import java.util.Objects;

/**
 * Aggregate Root que representa un vuelo y su disponibilidad.
 */
public class Flight {

    private final Long id;
    private final Airline airline; // Value Object interno
    private final String flightCode;
    private final String origin;
    private final String destination;
	private final int totalSeats;
    private int reservedSeats;
    private final Instant scheduledDeparture;
    private final Instant scheduledArrival;
    private FlightStatus status;

    // Constructor privado
    private Flight(Long id, Airline airline, String flightCode, String origin, String destination,
                   int totalSeats, int reservedSeats, Instant scheduledDeparture,
                   Instant scheduledArrival, FlightStatus status) {
        this.id = id;
        this.airline = Objects.requireNonNull(airline, "airline no puede ser null");
        this.flightCode = Objects.requireNonNull(flightCode, "flightCode no puede ser null");
        this.origin = Objects.requireNonNull(origin, "origin no puede ser null");
        this.destination = Objects.requireNonNull(destination, "destination no puede ser null");
        this.scheduledDeparture = Objects.requireNonNull(scheduledDeparture, "scheduledDeparture no puede ser null");
        this.scheduledArrival = Objects.requireNonNull(scheduledArrival, "scheduledArrival no puede ser null");
        this.status = Objects.requireNonNull(status, "status no puede ser null");

        this.totalSeats = totalSeats;
        this.reservedSeats = reservedSeats;
    }

    /**
     * Fábrica para crear un nuevo vuelo programado.
     */
    public static Flight create(Airline airline, String flightCode, String origin, String destination,
                        int totalSeats, int reservedSeats, Instant scheduledDeparture, Instant scheduledArrival) {

        return new Flight(null, airline, flightCode, origin, destination, totalSeats, reservedSeats,
                scheduledDeparture, scheduledArrival, FlightStatus.SCHEDULED);
    }

    /**
     * Fábrica para construir un vuelo desde base de datos.
     */
    public static Flight fromPersistence(Long id, Airline airline, String flightCode,
                                         String origin, String destination, int totalSeats, int reservedSeats,
                                         Instant scheduledDeparture, Instant scheduledArrival, FlightStatus status) {
        if (id == null) throw new IllegalArgumentException("El id no puede ser nulo");

        return new Flight(id, airline, flightCode, origin, destination, totalSeats, reservedSeats,
                scheduledDeparture, scheduledArrival, status);
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

    public Instant getScheduledDeparture() {
        return scheduledDeparture;
    }

    public Instant getScheduledArrival() {
        return scheduledArrival;
    }

    public FlightStatus getStatus() {
        return status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Flight flight)) return false;
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
